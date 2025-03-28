package icu.flycode.sdk.domain.service;

import icu.flycode.sdk.instructure.git.GitCommand;
import icu.flycode.sdk.instructure.openai.IOpenAI;
import icu.flycode.sdk.instructure.weixin.WeiXin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class AbstractOpenAiCodeReviewService implements IOpenAiCodeReviewService {
    private final Logger logger = LoggerFactory.getLogger(AbstractOpenAiCodeReviewService.class);

    protected final GitCommand gitCommand;

    protected final IOpenAI iOpenAI;

    protected final WeiXin weiXin;

    public AbstractOpenAiCodeReviewService(GitCommand gitCommand, IOpenAI iOpenAI, WeiXin weiXin) {
        this.gitCommand = gitCommand;
        this.iOpenAI = iOpenAI;
        this.weiXin = weiXin;
    }

    @Override
    public void exec() {
        try {
            // 1.获取提交代码
            String diffCode = getDiffCode();
            // 2. 开始评审代码
            String recommend = codeReview(diffCode);
            // 3. 记录评审日志，返回日志地址
            String logUrl = recordCodeReview(recommend);
            // 4. 发送消息通知
            pushMessage(logUrl);
        }catch (Exception e){
            logger.error("openai-code-review error",e);
        }
    }

    protected abstract String getDiffCode() throws Exception;

    protected abstract String codeReview(String diffCode) throws IOException;

    protected abstract String recordCodeReview(String recommend) throws Exception;

    protected abstract void pushMessage(String logUrl) throws IOException;
}
