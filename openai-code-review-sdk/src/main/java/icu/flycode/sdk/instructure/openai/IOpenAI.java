package icu.flycode.sdk.instructure.openai;

import icu.flycode.sdk.instructure.openai.dto.ChatCompletionDto;
import icu.flycode.sdk.instructure.openai.dto.ChatCompletionSyncDto;

import java.io.IOException;

public interface IOpenAI {
    ChatCompletionSyncDto getAiResponse(ChatCompletionDto chatCompletionDto) throws IOException;
}
