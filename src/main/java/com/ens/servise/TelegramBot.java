package com.ens.servise;

import com.ens.config.BotConfig;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@ToString
public class TelegramBot extends TelegramLongPollingBot {

    private MessageService messageService;

    private final MainCommandHandler mainCommandHandler;

    private final BotConfig config;

    @Autowired
    public TelegramBot(BotConfig config, MainCommandHandler mainCommandHandler) {
        this.config = config;
        this.mainCommandHandler = mainCommandHandler;
        initializeBotCommands();
    }

    @Autowired
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
        this.messageService.setBot(this);
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        mainCommandHandler.handleUpdate(update);
    }

    private void initializeBotCommands() {
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Greetings"));
        listOfCommands.add(new BotCommand("/mydata", "Get info about yourself"));
        listOfCommands.add(new BotCommand("/deletedata", "Delete my data"));
        listOfCommands.add(new BotCommand("/help", "Show this help"));
        listOfCommands.add(new BotCommand("/mygroups", "Shows a list of the groups you are a member of "));
        listOfCommands.add(new BotCommand("/removegroup", "Removes you from the selected group"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
            log.info("Bot commands initialized");
        } catch (TelegramApiException e) {
            log.error("Error with initializing bot commands: {}", e.getMessage(), e);
        }
    }

}
