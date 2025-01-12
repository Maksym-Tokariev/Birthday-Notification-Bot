package com.ens.comands;

import com.ens.servise.MessageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@AllArgsConstructor
public class HelpCommandHandler implements CommandHandler {

    private final MessageService messageService;
    private static final String HELP_TEXT = "This bot sends birthday reminders\n\n"
            + "Commands:\n\n"
            + "The command /start adds you to the notification system\n\n"
            + "The command /mydate shows your information about you\n\n"
            + "The command /deletedata delete information about you\n\n";

    @Override
    public void handle(Update update) {
        messageService.sendMessage(update.getMessage().getChatId(), HELP_TEXT);
        log.info("Received command help");
    }
}
