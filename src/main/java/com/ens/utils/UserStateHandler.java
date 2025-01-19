package com.ens.utils;

import com.ens.models.UserContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@AllArgsConstructor
public class UserStateHandler {

    private final Map<Long, BotState> userStates = new HashMap<>();
    private final Map<Long, UserContext> userContexts = new HashMap<>();

    public BotState getState(Long chatId) {
        return userStates.getOrDefault(chatId, BotState.WAITING_FOR_RESPONSE);
    }

    public void setState(Long chatId, BotState state) {
        userStates.put(chatId, state);
    }

    public UserContext getContext(Long chatId) {
        return userContexts.computeIfAbsent(chatId, UserContext::new);
    }

    public void setContext(Long chatId, UserContext context) {
        userContexts.put(chatId, context);
    }

}
