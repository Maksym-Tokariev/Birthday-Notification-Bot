package com.ens.comands;

import com.ens.models.UserGroups;
import com.ens.utils.BotState;
import com.ens.servise.MessageService;
import com.ens.servise.UserService;
import com.ens.utils.UserStateHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class ListOfMyGroupsCommandHandler implements CommandHandler {

    private final UserService userService;
    private final MessageService messageService;
    private final UserStateHandler userStateHandler;

    @Override
    public void handle(Update update) {
        log.info("The command is processed in ListOfMyGroupsCommandHandler");

        Long chatId = update.getMessage().getChatId();
        List<UserGroups> listOfGroups = userService.listOfGroups(chatId);

        if (!listOfGroups.isEmpty() && userService.userExists(chatId)) {
            StringBuilder groupsMessage = new StringBuilder("Groups you are a member of:\n");
            for (UserGroups group : listOfGroups) {
                groupsMessage.append("- ").append(group.getGroupName()).append("\n");
            }
            messageService.sendMessage(chatId, groupsMessage.toString());

            log.info("The command executed with the list of groups: {}", listOfGroups);
        } else {
            messageService.sendMessage(chatId, "You are not a member of any group");
            log.info("The command executed and returned a empty list of groups");
        }
    }
}
