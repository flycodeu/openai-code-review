package icu.flycode.sdk;

import com.alibaba.fastjson2.JSON;
import icu.flycode.sdk.domain.models.ChatCompletionSyncResponse;
import icu.flycode.sdk.utils.BearerTokenUtils;
import org.junit.Test;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;


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
}
