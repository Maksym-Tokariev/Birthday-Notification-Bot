package com.ens.servise;

import com.ens.config.BotConfig;
import com.ens.models.User;
import com.ens.models.UserRepository;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@ToString
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository repository;

    private final BotConfig config;

    private final Map<Long, BotState> userStates = new HashMap<>();

    static final String HELP_TEXT = "This bot sends birthday reminders\n\n"
            + "Commands:\n\n"
            + "The command /start adds you to the notification system\n\n"
            + "The command /mydate shows your information about you\n\n"
            + "The command /deletedata delete information about you\n\n";

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start","Greetings"));
        listOfCommands.add(new BotCommand("/mydata","Get info about yourself"));
        listOfCommands.add(new BotCommand("/deletedata","Delete your my data"));
        listOfCommands.add(new BotCommand("/help","Show this help"));
        listOfCommands.add(new BotCommand("/settings","Set your settings"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error with initializing bot commands: {}", e.getMessage());
        }
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

        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            BotState state = userStates.getOrDefault(chatId, BotState.START);

            switch (state) {
                case START:
                    if ("/start".equals(message)) {
                        startCommandReceived(chatId, update.getMessage().getChat().getFirstName(), state);
                        userStates.put(chatId, BotState.GET_YEAR);
                    } else {
                        sendMessage(chatId, "Unrecognized command");
                    }
                    break;

                case WAITING_FOR_RESPONSE:
                    sendMessage(chatId, "Choose command");
                    break;

                case GET_YEAR:
                    yearCommandReceived(chatId, message, state);
                    break;

                case GET_MONTH:
                    monthCommandReceived(chatId, message, state);
                    break;

                case GET_DAY:
                    dayCommandReceived(chatId, message, state);
                    break;

                case HELP:
                    if ("/help".equals(message)) {
                        helpCommandReceived(chatId, state);
                        userStates.put(chatId, BotState.WAITING_FOR_RESPONSE);
                    }
                    break;

                default:
                    sendMessage(chatId, "Sorry, command not found");
            }
        }

    }

    private void startCommandReceived(long chatId, String name, BotState state) {
        String answer = "hello, " + name + ", enter your year of birth";

        log.info("Received command: {}", name);

        sendMessage(chatId, answer);
    }

    private void yearCommandReceived(long chatId, String message, BotState state) {
        if (checkYearMessage(message)) {
            sendMessage(chatId, "Good, now enter the month", createKeyboardMonth());
            userStates.put(chatId, BotState.GET_MONTH);
        } else {
            sendMessage(chatId, "The year is incorrect, please try again");
        }
    }

    private void monthCommandReceived(long chatId, String message, BotState state) {
        if (checkMonthMessage(message)) {
            sendMessage(chatId, "Good, and lastly, enter the day");
            userStates.put(chatId, BotState.GET_DAY);
        } else {
            sendMessage(chatId, "The month is incorrect, please try again");
        }
    }

    private void dayCommandReceived(long chatId, String message, BotState state) {
        if (checkDayMessage(message)) {
            sendMessage(chatId, "Thank you! The process is completed.");
            userStates.put(chatId, BotState.COMPLETED);
        } else {
            sendMessage(chatId, "The day is incorrect, try again");
        }
    }

    private void helpCommandReceived(long chatId, BotState state) {
        sendMessage(chatId, HELP_TEXT);
    }

    private void sendMessage(long chatId, String message) {
        sendMessage(chatId, message, null);

    }

    private void sendMessage(long chatId, String message, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(message);

        if (replyKeyboardMarkup != null) {
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
        }

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error while sending message: {}", e.getMessage());
        }

    }

    private boolean checkYearMessage(String message) {
        int year = Integer.parseInt(message);
        int currYear = LocalDate.now().getYear();

        return (year >= 1900 && year <= currYear);
    }

    private boolean checkMonthMessage(String message) {
        String[] months = {"January", "February", "March", "April", "May", "June", "July",
                "August", "September", "October", "November", "December"};
        for (String month : months) {
            if (message.contains(month)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkDayMessage(String message) {
        int day = Integer.parseInt(message);
        for (int i = 0; i < message.length(); i++) {
            if (day >= 1 && day <= 31) {
                return true;
            }
        }
        return false;
    }

    private void registerUser(Message message) {

        if (repository.findById(message.getChatId()).isPresent()) {
            var chatId = message.getChatId();
            var chat = message.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            repository.save(user);

            log.info("user saved: {}", user);
        }
    }


    private ReplyKeyboardMarkup createKeyboardMonth() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> rows = new ArrayList<>();
        String[] months = {"January", "February", "March", "April", "May", "June", "July",
                "August", "September", "October", "November", "December"};

        KeyboardRow row = new KeyboardRow();

        for (int i = 0; i < months.length; i++) {
            row.add(months[i]);
            if (i == 2 || i == 5 || i == 8) {
                rows.add(row);
                row = new KeyboardRow();
            }
        }

        keyboardMarkup.setKeyboard(rows);

        return keyboardMarkup;
    }

}
