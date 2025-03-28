package icu.flycode.sdk.instructure.openai.impl;

import com.alibaba.fastjson2.JSON;
import icu.flycode.sdk.instructure.openai.IOpenAI;
import icu.flycode.sdk.instructure.openai.dto.ChatCompletionDto;
import icu.flycode.sdk.instructure.openai.dto.ChatCompletionSyncDto;
import icu.flycode.sdk.utils.BearerTokenUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ChatGLM implements IOpenAI {

    private final String apiKey;

    private final String apiHost;

    public ChatGLM(String apiKey, String apiHost) {
        this.apiKey = apiKey;
        this.apiHost = apiHost;
    }

    @Override
    public ChatCompletionSyncDto getAiResponse(ChatCompletionDto chatCompletionDto) throws IOException {
        // 1. 生成token
        String token = BearerTokenUtils.getToken(apiKey);
        // 2. 发送请求
        URL url = new URL(apiHost);
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
        httpsURLConnection.setRequestMethod("POST");
        httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        httpsURLConnection.setRequestProperty("Content-Type", "application/json");
        httpsURLConnection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        httpsURLConnection.setDoOutput(true);
        // 3. 处理响应
        try (OutputStream os = httpsURLConnection.getOutputStream()) {
            byte[] bytes = JSON.toJSONString(chatCompletionDto).getBytes(StandardCharsets.UTF_8);
            os.write(bytes, 0, bytes.length);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
        String line;
        StringBuilder content = new StringBuilder();
        while ((line = in.readLine()) != null) {
            content.append(line);
        }

        in.close();
        httpsURLConnection.disconnect();
        // 4. 解析响应
        return JSON.parseObject(content.toString(), ChatCompletionSyncDto.class);
    }
}
