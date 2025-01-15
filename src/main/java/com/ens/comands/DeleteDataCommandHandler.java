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
        log.info("The command is processed in DeleteDataCommandHandler");

        Long chatId = update.getMessage().getChatId();
        if (userService.userExists(chatId)) {
            userService.deleteUser(chatId);
            messageService.sendMessage(chatId, "You've successfully deleted birthday");
            log.info("The command executed and deleted user: {}", update.getMessage().getChat().getUserName());
        } else {
            messageService.sendMessage(chatId, "No data was found for deletion. Register using the command /start");
            log.info("The command was not executed for the user because the user is not logged in");
        }
        userStateHandler.setState(chatId, BotState.WAITING_FOR_RESPONSE);
    }
}
