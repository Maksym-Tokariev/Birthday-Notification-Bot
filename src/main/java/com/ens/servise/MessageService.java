package com.ens.servise;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
public class MessageService {

    private TelegramBot bot;

    public void setBot(TelegramBot bot) {
        this.bot = bot;
    }

    public void sendMessage(long chatId, String message) {
        sendMessage(chatId, message, null);
    }

    public void sendMessage(long chatId, String message, ReplyKeyboardMarkup monthsKeyboard) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(message);

        if (monthsKeyboard != null) {
            sendMessage.setReplyMarkup(monthsKeyboard);
        }

        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error while sending message: {}", e.getMessage(), e);
        }
    }

    public void sendMessage(Long chatId, String message, InlineKeyboardMarkup groupsKeyboard) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(message);

        if (groupsKeyboard != null) {
            sendMessage.setReplyMarkup(groupsKeyboard);
        }

        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error while sending message: {}", e.getMessage(), e);
        }
    }
}
