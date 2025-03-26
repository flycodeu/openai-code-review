package icu.flycode.sdk;

import com.alibaba.fastjson2.JSON;
import icu.flycode.sdk.domain.models.ChatCompletionRequest;
import icu.flycode.sdk.domain.models.ChatCompletionSyncResponse;
import icu.flycode.sdk.domain.models.Model;
import icu.flycode.sdk.utils.BearerTokenUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class OpenAiCodeReview {
    public static void main(String[] args) throws Exception {
        System.out.println("测试执行");
        // 代码评审
        // 1. 读取Git Diff更改记录
        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "HEAD~1", "HEAD");
        processBuilder.directory(new File("."));
        Process process = processBuilder.start();

        // 读取输出流
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder diffStr = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            diffStr.append(line);
        }

        // 2. 获取退出码
        int exitCode = process.waitFor();
        System.out.println("Exited with code: " + exitCode);
        // 3. 返回读取数据
        System.out.println("diff code: " + diffStr.toString());


        // 4. 调用OpenAI API进行代码评审
        String codedReview = codeReview(diffStr.toString());
        System.out.println("Code review: " + codedReview);
    }


    private static String codeReview(String diffCode) throws Exception {
        String apiKey = "046183b32b904844949bd062b1ab223c.MEgwXNBvYeLMvd51";
        String token = BearerTokenUtils.getToken(apiKey);

        URL url = new URL("  https://open.bigmodel.cn/api/paas/v4/chat/completions");
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
        httpsURLConnection.setRequestMethod("POST");
        httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        httpsURLConnection.setRequestProperty("Content-Type", "application/json");
        httpsURLConnection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        httpsURLConnection.setDoOutput(true);

        String jsonInputString = "{"
                + "\"model\":\"glm-4-flash\","
                + "\"messages\": ["
                + "    {"
                + "        \"role\": \"user\","
                + "        \"content\": \"你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码为: " + diffCode + "\""
                + "    }"
                + "]"
                + "}";



        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());
        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionRequest.Prompt>(){
            {
                add(new ChatCompletionRequest.Prompt("user","你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码为: "));
                add(new ChatCompletionRequest.Prompt("content",diffCode));
            }
        });

        try (OutputStream os = httpsURLConnection.getOutputStream()) {
            byte[] input = JSON.toJSONString(chatCompletionRequest).getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = httpsURLConnection.getResponseCode();
        System.out.println(responseCode);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
        String line;
        StringBuilder content = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            content.append(line);
        }

        bufferedReader.close();
        httpsURLConnection.disconnect();

        System.out.println(content);
        ChatCompletionSyncResponse chatCompletionSyncResponse = JSON.parseObject(content.toString(), ChatCompletionSyncResponse.class);
        String returnContent = chatCompletionSyncResponse.getChoices().get(0).getMessage().getContent();

        return returnContent;
    }
}