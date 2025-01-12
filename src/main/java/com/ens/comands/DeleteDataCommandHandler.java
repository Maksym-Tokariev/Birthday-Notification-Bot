package com.ens.comands;

import com.ens.servise.BotState;
import com.ens.servise.MessageService;
import com.ens.servise.UserService;
import com.ens.servise.UserStateHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@AllArgsConstructor
public class DeleteDataCommandHandler implements CommandHandler {

    private final UserService userService;
    private final MessageService messageService;
    private final UserStateHandler userStateHandler;

    @Override
    public void handle(Update update) {
        Long chatId = update.getMessage().getChatId();
        if (userService.userExists(chatId)) {
            userService.deleteUser(chatId);
            messageService.sendMessage(chatId, "You've successfully deleted birthday");
        } else {
            messageService.sendMessage(chatId, "No data was found for deletion. Register using the command /start");
        }
        userStateHandler.setState(chatId, BotState.WAITING_FOR_RESPONSE);
    }
}
