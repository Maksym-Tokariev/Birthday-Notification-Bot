package com.ens.comands.impl;

import com.ens.comands.CommandHandler;
import com.ens.models.UserData;
import com.ens.servise.MessageService;
import com.ens.servise.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class MyDataCommandHandler implements CommandHandler {

    private final UserService userService;
    private final MessageService messageService;

    @Override
    public void handle(Update update) {
        log.info("The command is processed in MyDataCommandHandler");

        Long chatId = update.getMessage().getChatId();
        Optional<UserData> optionalUserData = userService.getCachedDateOfBirth(chatId);

        if (optionalUserData.isPresent()) {
            UserData userData = optionalUserData.get();
            messageService.sendMessage(chatId, userData.getFirstName() + ", your birthday is " + userData.getDateOfBirth());
            log.info("The command has been executed, the user {} data was received", userData.getFirstName());
        } else {
            messageService.sendMessage(chatId, "No date of birth was found for your account. " +
                    "You may not have entered it.");
            log.info("The command was not executed for the user because the user is not logged in");
        }
    }

}
