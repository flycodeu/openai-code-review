package icu.flycode.sdk.domain.service.impl;

import icu.flycode.sdk.domain.models.ChatCompletionRequest;
import icu.flycode.sdk.domain.models.Model;
import icu.flycode.sdk.domain.service.AbstractOpenAiCodeReviewService;
import icu.flycode.sdk.instructure.git.GitCommand;
import icu.flycode.sdk.instructure.openai.IOpenAI;
import icu.flycode.sdk.instructure.openai.dto.ChatCompletionDto;
import icu.flycode.sdk.instructure.openai.dto.ChatCompletionSyncDto;
import icu.flycode.sdk.instructure.weixin.WeiXin;
import icu.flycode.sdk.instructure.weixin.dto.TemplateMessageDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OpenAiCodeReviewService extends AbstractOpenAiCodeReviewService {
    public OpenAiCodeReviewService(GitCommand gitCommand, IOpenAI iOpenAI, WeiXin weiXin) {
        super(gitCommand, iOpenAI, weiXin);
    }

    @Override
    protected String getDiffCode() throws Exception {
        return gitCommand.diff();
    }

    @Override
    protected String codeReview(String diffCode) throws IOException {
        ChatCompletionDto chatCompletionRequest = new ChatCompletionDto();
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());
        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionDto.Prompt>() {
            {
                add(new ChatCompletionDto.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码为: "));
                add(new ChatCompletionDto.Prompt("user", diffCode));
            }
        });

        ChatCompletionSyncDto aiResponse = iOpenAI.getAiResponse(chatCompletionRequest);
        ChatCompletionSyncDto.Message message = aiResponse.getChoices().get(0).getMessage();
        return message.getContent();
    }

    @Override
    protected String recordCodeReview(String recommend) throws Exception {
        return gitCommand.commitAndPush(recommend);
    }

    @Override
    protected void pushMessage(String logUrl) throws IOException {
        Map<String, Map<String, String>> data = new HashMap<>();
        TemplateMessageDTO.put(TemplateMessageDTO.TemplateKey.REPO_NAME, gitCommand.getProject(), data);
        TemplateMessageDTO.put(TemplateMessageDTO.TemplateKey.BRANCH_NAME, gitCommand.getBranch(), data);
        TemplateMessageDTO.put(TemplateMessageDTO.TemplateKey.COMMIT_MESSAGE, gitCommand.getMessage(), data);
        TemplateMessageDTO.put(TemplateMessageDTO.TemplateKey.COMMIT_AUTHOR, gitCommand.getAuthor(), data);
        weiXin.sendTemplateMessage(logUrl, data);
    }
}
