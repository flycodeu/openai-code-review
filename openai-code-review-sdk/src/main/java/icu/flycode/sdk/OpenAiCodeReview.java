package icu.flycode.sdk;

import com.alibaba.fastjson2.JSON;
import icu.flycode.sdk.domain.models.ChatCompletionRequest;
import icu.flycode.sdk.domain.models.ChatCompletionSyncResponse;
import icu.flycode.sdk.domain.models.Message;
import icu.flycode.sdk.domain.models.Model;
import icu.flycode.sdk.domain.service.impl.OpenAiCodeReviewService;
import icu.flycode.sdk.instructure.git.GitCommand;
import icu.flycode.sdk.instructure.openai.IOpenAI;
import icu.flycode.sdk.instructure.openai.impl.ChatGLM;
import icu.flycode.sdk.instructure.weixin.WeiXin;
import icu.flycode.sdk.utils.BearerTokenUtils;
import icu.flycode.sdk.utils.WXAccessTokenUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.SimpleFormatter;

public class OpenAiCodeReview {
    private static final Logger logger = LoggerFactory.getLogger(OpenAiCodeReview.class);
    // 微信配置
    private String weixin_appid = "wxa6a853b4727a89c3";
    private String weixin_secret = "c79ba38e3570992b3904d35c57fb75e8";
    private String wexin_touser = "oNbqZ6vShgDLdznFaExcZl0yJCz8";
    private String weixin_template_id = "wmbDl-7QVjrOL01D5eqFo--9_rYd-hLGciU1L2lsnVA";

    // GLM配置
    private String chatglm_apiHost = "";
    private String chatglm_apiKey = "";
    private String github_review_url = "";

    // Github配置
    private static String github_token = "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";

    private String github_project;
    private String github_branch;

    private String github_author;

    public static void main(String[] args) {
        GitCommand gitCommand = new GitCommand(
                getEnv("GITHUB_REVIEW_URL"),
                github_token,
                getEnv("COMMIT_AUTHOR"),
                getEnv("COMMIT_BRANCH"),
                getEnv("COMMIT_PROJECT"),
                getEnv("COMMIT_MESSAGE")
        );

        WeiXin weiXin = new WeiXin(
                getEnv("WEIXIN_APPID"),
                getEnv("WEIXIN_SECRET"),
                getEnv("WEIXIN_TOUSER"),
                getEnv("WEIXIN_TEMPLATE_ID")
        );

        IOpenAI chatGLM = new ChatGLM(
                getEnv("CHATGLM_APIHOST"),
                getEnv("CHATGLM_APIKEY")
        );


        OpenAiCodeReviewService openAiCodeReviewService = new OpenAiCodeReviewService(gitCommand, chatGLM, weiXin);
        openAiCodeReviewService.exec();
        logger.info("openai-code-review finished");
    }

    public static String getEnv(String key) {
        String token = System.getenv(key);
        if (null == token || token.isEmpty()) {
            throw new RuntimeException("GitHub_Token is empty");
        }
        return token;
    }

//    public static void main(String[] args) throws Exception {
//        System.out.println("openai代码评审");
//        String token = System.getenv("GITHUB_TOKEN");
//        if (null == token || token.isEmpty()) {
//            throw new RuntimeException("GitHub_Token is empty");
//        }
//
//        // 代码评审
//        // 1. 读取Git Diff更改记录
//        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "HEAD~1", "HEAD");
//        processBuilder.directory(new File("."));
//        Process process = processBuilder.start();
//
//        // 读取输出流
//        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//        String line;
//        StringBuilder diffStr = new StringBuilder();
//        while ((line = bufferedReader.readLine()) != null) {
//            diffStr.append(line);
//        }
//
//        // 2. 获取退出码
//        int exitCode = process.waitFor();
//        System.out.println("Exited with code: " + exitCode);
//        // 3. 返回读取数据
//        System.out.println("diff code: " + diffStr.toString());
//
//
//        // 4. 调用OpenAI API进行代码评审
//        String codedReview = codeReview(diffStr.toString());
//        System.out.println("Code review: " + codedReview);
//
//        // 5. 写入日志
//        String logsUrl = writeLogs(token, codedReview);
//        System.out.println("logs write:"+logsUrl);
//
//        // 6. 调用公众号
//        pushMessage(logsUrl);
//        System.out.println("Message pushed:"+logsUrl);
//    }
//
//
//    private static String codeReview(String diffCode) throws Exception {
//        String apiKey = "046183b32b904844949bd062b1ab223c.MEgwXNBvYeLMvd51";
//        String token = BearerTokenUtils.getToken(apiKey);
//
//        HttpsURLConnection httpsURLConnection = getHttpsURLConnection(token);
//
//
//        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
//        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());
//        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionRequest.Prompt>() {
//            {
//                add(new ChatCompletionRequest.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码为: "));
//                add(new ChatCompletionRequest.Prompt("user", diffCode));
//            }
//        });
//
//        try (OutputStream os = httpsURLConnection.getOutputStream()) {
//            byte[] input = JSON.toJSONString(chatCompletionRequest).getBytes(StandardCharsets.UTF_8);
//            os.write(input, 0, input.length);
//        }
//
//        int responseCode = httpsURLConnection.getResponseCode();
//        System.out.println(responseCode);
//
//        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
//        String line;
//        StringBuilder content = new StringBuilder();
//        while ((line = bufferedReader.readLine()) != null) {
//            content.append(line);
//        }
//
//        bufferedReader.close();
//        httpsURLConnection.disconnect();
//
//        System.out.println(content);
//        ChatCompletionSyncResponse chatCompletionSyncResponse = JSON.parseObject(content.toString(), ChatCompletionSyncResponse.class);
//        String returnContent = chatCompletionSyncResponse.getChoices().get(0).getMessage().getContent();
//
//        return returnContent;
//    }
//
//    private static HttpsURLConnection getHttpsURLConnection(String token) throws IOException {
//        URL url = new URL("https://open.bigmodel.cn/api/paas/v4/chat/completions");
//        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
//        httpsURLConnection.setRequestMethod("POST");
//        httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
//        httpsURLConnection.setRequestProperty("Content-Type", "application/json");
//        httpsURLConnection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
//        httpsURLConnection.setDoOutput(true);
//        return httpsURLConnection;
//    }
//
//    /**
//     * 写入日志到指定仓库
//     *
//     * @param token
//     * @param log
//     * @return
//     * @throws GitAPIException
//     */
//    private static String writeLogs(String token, String log) throws GitAPIException {
//        // 1. 连接Git仓库
//        Git git = Git.cloneRepository()
//                .setURI("https://github.com/flycodeu/openai-code-review-logs.git")
//                .setDirectory(new File("repo"))
//                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, ""))
//                .call();
//
//        // 2. 创建文件夹
//        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
//        File dateFolder = new File("repo/" + dateFolderName);
//        if (!dateFolder.exists()) {
//            dateFolder.mkdirs();
//        }
//
//        // 3. 写入日志文件
//        String fileName = generateRandomString(12) + ".md";
//        File file = new File(dateFolder, fileName);
//        try (FileWriter writer = new FileWriter(file)) {
//            writer.write(log);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        // 4. 提交并推送更改
//        git.add().addFilepattern(dateFolderName + "/" + fileName).call();
//        git.commit().setMessage("Add new log via Github Actions").call();
//        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, "")).call();
//
//        return "https://github.com/flycodeu/openai-code-review-logs/blob/master/" + dateFolderName + "/" + fileName;
//
//    }
//
//
//    /**
//     * 随机字母作为名称
//     *
//     * @param length
//     * @return
//     */
//    private static String generateRandomString(int length) {
//        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
//        Random random = new Random();
//        StringBuilder sb = new StringBuilder(length);
//        for (int i = 0; i < length; i++) {
//            sb.append(characters.charAt(random.nextInt(characters.length())));
//        }
//        return sb.toString();
//    }
//
//
//    public static void pushMessage(String logUrl) {
//        String accessToken = WXAccessTokenUtils.getAccessToken();
//        System.out.println("accessToken:" + accessToken);
//
//        Message message = new Message();
//        message.put("project", "openai-code-review-logs");
//        message.put("review", logUrl);
//        message.setUrl(logUrl);
//        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s", accessToken);
//        sendPostRequest(url, JSON.toJSONString(message));
//    }
//
//    private static void sendPostRequest(String urlString, String jsonBody) {
//        try {
//            URL url = new URL(urlString);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/json; utf-8");
//            conn.setRequestProperty("Accept", "application/json");
//            conn.setDoOutput(true);
//
//            try (OutputStream os = conn.getOutputStream()) {
//                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
//                os.write(input, 0, input.length);
//            }
//
//            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
//                String response = scanner.useDelimiter("\\A").next();
//                System.out.println(response);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}