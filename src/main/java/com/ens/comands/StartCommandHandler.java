package com.ens.comands;

import com.ens.models.UserContext;
import com.ens.servise.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Date;

@Slf4j
@Component
@AllArgsConstructor
public class StartCommandHandler implements CommandHandler {

    private final UserService userService;
    private final MessageService messageService;
    private final BotUtils botUtils;
    private final UserStateHandler userStateHandler;

    @Override
    public void handle(Update update) {
        String message = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        String[] parts = message.split(" ");

        if (parts[0].equals("/start")) {
            if (parts.length > 1) {
                try {
                    String[] groupIdParts = parts[1].split("_");
                    Long groupId = Long.parseLong(groupIdParts[1]);
                    String groupName = groupIdParts[3];

                    UserContext context = userStateHandler.getContext(chatId);
                    context.setGroupId(groupId);
                    context.setGroupName(groupName);
                    userStateHandler.setContext(chatId, context);

                    if (!userService.userExists(chatId)) {
                        startCommandReceived(chatId, groupId, update.getMessage().getChat().getFirstName());
                    } else {
                        messageService.sendMessage(chatId, "You're already registered, I've added you to a new group: "
                                + context.getGroupName() +
                                ". If the group is not missing from the database, I will add it.");
                        context.setGroupName(groupName);
                        registration(update);
                        userStateHandler.setState(chatId, BotState.WAITING_FOR_RESPONSE);
                    }
                } catch (NumberFormatException e) {
                    messageService.sendMessage(chatId, "Invalid group ID");
                    userStateHandler.setState(chatId, BotState.WAITING_FOR_RESPONSE);
                }
            } else {
                messageService.sendMessage(chatId, "Group ID is missing. " +
                        "You can only register using the link from the group. Try again or change link.");
                userStateHandler.setState(chatId, BotState.WAITING_FOR_RESPONSE);
            }
        }

    }

    private void startCommandReceived(Long chatId, Long groupId, String name) {
        String answer = "hello, " + name + ", enter your year of birth";

        UserContext context = userStateHandler.getContext(chatId);
        context.setGroupId(groupId);
        log.info("Received command name: {}, groupId: {}", name, groupId);

        userStateHandler.setState(chatId, BotState.GET_YEAR);

        messageService.sendMessage(chatId, answer);
    }

    private void registration(Update update) {
        UserContext context = userStateHandler.getContext(update.getMessage().getChatId());
        Long groupId = context.getGroupId();

        String stringDate = String.join("-", context.getDate());
        Date dateOfBirth = botUtils.stringToDate(stringDate);
        String groupName = context.getGroupName();

        log.info("Received date: {}, groupId: {}", dateOfBirth, groupId);

        userService.registerUser(update.getMessage(), groupId, dateOfBirth, groupName);
    }
}
