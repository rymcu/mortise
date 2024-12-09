package com.rymcu.mortise.web;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.core.result.GlobalResultGenerator;
import com.rymcu.mortise.entity.User;
import com.rymcu.mortise.openai.model.ChatMessagePrompt;
import com.rymcu.mortise.openai.service.SseService;
import com.rymcu.mortise.util.UserUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionMessage;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Created on 2023/2/15 10:04.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.openai
 */
@RestController
@RequestMapping("/api/v1/openai")
public class OpenAiController {
    @Resource
    private SseService sseService;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @PostMapping("/chat")
    public GlobalResult chat(@RequestBody ChatMessagePrompt chatMessagePrompt) {
        List<ChatCompletionMessage> messages = chatMessagePrompt.getMessages();
        if (messages.isEmpty()) {
            throw new IllegalArgumentException("参数异常！");
        }
        User user = UserUtils.getCurrentUserByToken();
        Collections.reverse(messages);
        return sendMessageStream(chatMessagePrompt.getModel(), user.getIdUser(), messages);
    }

    private GlobalResult sendMessageStream(String model, Long idUser, List<ChatCompletionMessage> list) {
        OpenAiApi openAiApi = new OpenAiApi(baseUrl, apiKey);
        openAiApi.chatCompletionStream(
                        new ChatCompletionRequest(list, model, 0.8, true))
                .doOnNext(chunk -> {
                    if (chunk.choices().isEmpty() || Objects.isNull(chunk.choices().getFirst().delta().content())) {
                        return;
                    }
                    String text = chunk.choices().getFirst().delta().content();
                    if (StringUtils.isBlank(text)) {
                        return;
                    }
                    sseService.send(idUser, text);
                })
                .doOnError(throwable -> System.out.println(throwable.getMessage()))
                .doOnComplete(() -> System.out.println("\ncomplete"))
                .subscribe();
        return GlobalResultGenerator.genSuccessResult();
    }
}
