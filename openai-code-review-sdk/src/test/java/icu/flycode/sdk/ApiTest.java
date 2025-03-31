package icu.flycode.sdk;

import com.alibaba.fastjson2.JSON;
import icu.flycode.sdk.domain.models.ChatCompletionSyncResponse;
import icu.flycode.sdk.domain.models.Message;
import icu.flycode.sdk.utils.BearerTokenUtils;
import icu.flycode.sdk.utils.WXAccessTokenUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.Test;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Scanner;


public class ApiTest {
    public static void main(String[] args) {
        String apiKey = "046183b32b904844949bd062b1ab223c.MEgwXNBvYeLMvd51";
        String token = BearerTokenUtils.getToken(apiKey);
        System.out.println(token);
    }

    @Test
    public void test_http() throws IOException {
        String apiKey = "046183b32b904844949bd062b1ab223c.MEgwXNBvYeLMvd51";
        String token = BearerTokenUtils.getToken(apiKey);

        URL url = new URL("  https://open.bigmodel.cn/api/paas/v4/chat/completions");
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
        httpsURLConnection.setRequestMethod("POST");
        httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        httpsURLConnection.setRequestProperty("Content-Type", "application/json");
        httpsURLConnection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        httpsURLConnection.setDoOutput(true);

        String code = "print('Hello, World!')";
        String jsonInputString = "{"
                + "\"model\":\"glm-4-flash\","
                + "\"messages\": ["
                + "    {"
                + "        \"role\": \"user\","
                + "        \"content\": \"你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码为: " + code + "\""
                + "    }"
                + "]"
                + "}";


        try (OutputStream os = httpsURLConnection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
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
        System.out.println(returnContent);

    }


    @Test
    public void test_wx() {
        String accessToken = WXAccessTokenUtils.getAccessToken();
        System.out.println("accessToken:" + accessToken);

        Message message = new Message();
        message.put("project", "测试项目");
        message.put("review", "测试内容");
        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s", accessToken);
        sendPostRequest(url, JSON.toJSONString(message));

    }

    private static void sendPostRequest(String urlString, String jsonBody) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
                String response = scanner.useDelimiter("\\A").next();
                System.out.println(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testGetGitConfig() throws Exception {
        // 1. 作者名
        String author = getGitInfo("%an");
        System.out.println("Author: " + author);
        // 2. 日期
        String date = getGitInfo("%cd");
        System.out.println("Date: " + date);
        // 3. 描述
        String description = getGitInfo("%s");
        System.out.println("Description: " + description);
        // 4. 哈希值，用于获取提交代码
        String hashCode = getGitInfo("%h");
        System.out.println("Hash Code: " + hashCode);

        // 5. 获取提交代码
        String diffCode = getDiffCode(hashCode);
        System.out.println(diffCode);
    }


    public String getGitInfo(String tags) throws IOException {
        ProcessBuilder logProcessBuilder = new ProcessBuilder("git", "log", "-1", "--pretty=format:" + tags);
        logProcessBuilder.directory(new File("."));
        Process logProcess = logProcessBuilder.start();
        BufferedReader logReader = new BufferedReader(new InputStreamReader(logProcess.getInputStream()));
        return logReader.readLine();
    }

    public String getDiffCode(String lastCommitHash) throws Exception {
        ProcessBuilder diffProcessBuilder = new ProcessBuilder("git", "diff", lastCommitHash + "^", lastCommitHash);
        diffProcessBuilder.directory(new File("."));
        Process process = diffProcessBuilder.start();
        BufferedReader diffReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder processOutput = new StringBuilder();
        while ((line = diffReader.readLine()) != null) {
            processOutput.append(line).append("\n");
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new Exception("Diff process exited with code " + exitCode);
        }
        return processOutput.toString();
    }

}
