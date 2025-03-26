package icu.flycode.sdk;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

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
        System.out.println("Exited with code:" + exitCode);
        // 3. 返回读取数据
        System.out.println("评审代码" + diffStr.toString());
    }
}