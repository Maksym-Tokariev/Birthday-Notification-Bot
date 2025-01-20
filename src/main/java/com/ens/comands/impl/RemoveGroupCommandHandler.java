package com.ens.comands.impl;

import com.ens.comands.CommandHandler;
import com.ens.models.UserGroups;
import com.ens.servise.GroupService;
import com.ens.utils.BotUtils;
import com.ens.servise.MessageService;
import com.ens.servise.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class RemoveGroupCommandHandler implements CommandHandler {

    private final UserService userService;
    private final GroupService groupService;
    private final MessageService messageService;
    private final BotUtils botUtils;

    @Override
    public void handle(Update update) {
        log.info("The command is processed in RemoveGroupCommandHandler");
        Long chatId = update.getMessage().getChatId();

        List<UserGroups> groupsList = groupService.getUserGroups(chatId);

        if (!groupsList.isEmpty() && userService.userExists(chatId)) {
            InlineKeyboardMarkup groups = botUtils.createKeyboardCommand(groupsList);
            messageService.sendMessage(chatId, "Select the group you want to remove", groups);

            log.info("The command executed and deleted group");
        } else {
            messageService.sendMessage(chatId, "You do not have any groups to remove");
        }
    }
}
