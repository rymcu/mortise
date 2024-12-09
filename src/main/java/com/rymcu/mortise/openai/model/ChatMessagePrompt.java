package com.rymcu.mortise.openai.model;

import lombok.Data;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionMessage;

import java.util.List;

/**
 * Created on 2024/12/7 20:07.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.openai.model
 */
@Data
public class ChatMessagePrompt {

    private String model;

    private List<ChatCompletionMessage> messages;

}
