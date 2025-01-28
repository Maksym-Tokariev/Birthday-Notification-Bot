package com.ens.comands.impl;

import com.ens.servise.MessageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@AllArgsConstructor
public class ChatMessageUpdateHandler {

    private final MessageService messageService;

    public void handleChatMessageUpdate(Update update) {
        ChatMemberUpdated myChatMember = update.getMyChatMember();
        if (myChatMember.getNewChatMember().getStatus().equals("member")) {
            Long groupId = myChatMember.getChat().getId();
            String groupName = myChatMember.getChat().getTitle();

            String encodedGroupId = URLEncoder.encode("groupId_" + groupId, StandardCharsets.UTF_8);
            String encodedGroupName = URLEncoder.encode("groupName_" + groupName, StandardCharsets.UTF_8);
            String answer = "Hi, I'll create a birthday calendar for this group and remind you to congratulate the birthday people. " +
                    "Please visit the bot privately to register: https://t.me/ENSystembot?start=" + encodedGroupId + "_" + encodedGroupName;

            messageService.sendMessage(groupId, answer);
            log.info("The bot has been added to the group");
        }
    }
}
