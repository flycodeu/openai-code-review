package icu.flycode.sdk.instructure.git;

import icu.flycode.sdk.utils.RandomUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Git操作
 */

public class GitCommand {
    private final Logger logger = LoggerFactory.getLogger(GitCommand.class);

    /**
     * 生成文件的地址
     */
    private final String githubReviewUrl;

    /**
     * GitHub的token
     */
    private final String githubToken;

    /**
     * 作者
     */
    private final String author;
    /**
     * 分支
     */
    private final String branch;
    /**
     * 项目名
     */
    private final String project;
    /**
     * 消息
     */
    private final String message;

    public GitCommand(String githubReviewUrl, String githubToken, String author, String branch, String project, String message) {
        this.githubReviewUrl = githubReviewUrl;
        this.githubToken = githubToken;
        this.author = author;
        this.branch = branch;
        this.project = project;
        this.message = message;
    }

    /**
     * 读取日志获取项目名、用户名等信息，并且获取提交代码
     * @return
     * @throws Exception
     */
    public String diff() throws Exception {
        // 1. 读取日志
        ProcessBuilder logProcessBuilder = new ProcessBuilder("git", "log", "-1", "--pretty=format:%h");
        logProcessBuilder.directory(new File("."));
        Process logProcess = logProcessBuilder.start();
        BufferedReader logReader = new BufferedReader(new InputStreamReader(logProcess.getInputStream()));
        String latestCommitHash = logReader.readLine();
        logReader.close();
        logProcess.waitFor();

        //2. 执行diff操作
        ProcessBuilder diffProcessBuilder = new ProcessBuilder("git", "diff", latestCommitHash + "^", latestCommitHash);
        diffProcessBuilder.directory(new File("."));
        Process diffProgress = diffProcessBuilder.start();
        BufferedReader diffReader = new BufferedReader(new InputStreamReader(diffProgress.getInputStream()));
        String line;
        StringBuilder diffCode = new StringBuilder();
        while ((line = diffReader.readLine()) != null) {
            diffCode.append(line).append("\n");
        }

        int exitCode = diffProgress.waitFor();
        logger.info("Diff process exited with code {}", exitCode);

        if (exitCode != 0) {
            throw new Exception("Diff process exited with code " + exitCode);
        }
        return diffCode.toString();
    }

    /**
     * 将生成的AI文本写入文件，提交到到指定GitHub
     * @param recommend
     * @return
     * @throws Exception
     */
    public String commitAndPush(String recommend) throws Exception {
        System.out.println(githubToken);
        System.out.println(githubReviewUrl);
        // 1. 连接Git仓库
        Git git = Git.cloneRepository()
                .setURI(githubReviewUrl + ".git")
                .setDirectory(new File("repo"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, ""))
                .call();

        // 2. 创建文件
        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File dateFolder = new File("repo/" + dateFolderName);
        if (!dateFolder.exists()) {
            dateFolder.mkdirs();
        }

        String fileName = project + "-" + branch + "-" + author + "-" + System.currentTimeMillis() + RandomUtils.generateRandomString(4) + ".md";
        File newFile = new File(dateFolder, fileName);
        try (Writer writer = new FileWriter(newFile)) {
            writer.write(recommend);
        }

        // 3. 提交代码
        git.add().addFilepattern(dateFolderName + "/" + fileName).call();
        git.commit().setMessage("add review new file").call();
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, "")).call();
        logger.info("openai-code-review git commit and push done! {}", fileName);

        return githubReviewUrl + "/blob/master" + dateFolderName + "/" + fileName;
    }

    public String getAuthor() {
        return author;
    }

    public String getBranch() {
        return branch;
    }

    public String getProject() {
        return project;
    }

    public String getMessage() {
        return message;
    }
}
