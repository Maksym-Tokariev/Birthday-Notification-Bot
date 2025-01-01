package com.ens.servise;

import com.ens.config.BotConfig;
import com.ens.models.User;
import com.ens.models.UserRepository;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.*;

@Slf4j
@Component
@ToString
public class TelegramBot extends TelegramLongPollingBot {

    private final UserRepository repository;

    private final BotUtils utils;

    private final BotConfig config;

    private final Map<Long, BotState> userStates = new HashMap<>();

    private final String[] date = new String[5];

    private static final String HELP_TEXT = "This bot sends birthday reminders\n\n"
            + "Commands:\n\n"
            + "The command /start adds you to the notification system\n\n"
            + "The command /mydate shows your information about you\n\n"
            + "The command /deletedata delete information about you\n\n";

    @Autowired
    public TelegramBot(BotConfig config, UserRepository repository, BotUtils utils) {
        this.config = config;
        this.utils = utils;
        this.repository = repository;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Greetings"));
        listOfCommands.add(new BotCommand("/mydata", "Get info about yourself"));
        listOfCommands.add(new BotCommand("/deletedata", "Delete your my data"));
        listOfCommands.add(new BotCommand("/help", "Show this help"));
        listOfCommands.add(new BotCommand("/settings", "Set your settings"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error with initializing bot commands: {}", e.getMessage(), e);
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
                    handleStartCommand(chatId, message, update);
                    break;

                case WAITING_FOR_RESPONSE:
                    sendMessage(chatId, "Choose command");
                    break;

                case GET_YEAR:
                    yearCommandReceived(chatId, message);
                    break;

                case GET_MONTH:
                    monthCommandReceived(chatId, message);
                    break;

                case GET_DAY:
                    dayCommandReceived(chatId, message);
                    String stringDate = String.join("-", date);
                    Date dateOfBirth = utils.stringToDate(stringDate);
                    registerUser(update.getMessage(), dateOfBirth);
                    break;

                case COMPLETED:
//                    String stringDate = String.join("-", date);
//                    Date dateOfBirth = utils.stringToDate(stringDate);
//                    registerUser(update.getMessage(), dateOfBirth);
                    break;

                case HELP:
                    handleHelpCommand(chatId, message);
                    break;

                default:
                    sendMessage(chatId, "Sorry, command not found");
            }

        } else if (update.hasMyChatMember()) {
            ChatMemberUpdated myChatMember = update.getMyChatMember();
            if (myChatMember.getNewChatMember().getStatus().equals("member")) {
                long chatId = myChatMember.getChat().getId();
                String answer = "Hi, I'll create a birthday calendar for this group and remind you to congratulate the birthday people. " +
                        "Please visit the bot privately to register: https://t.me/ENSystembot";

                sendMessage(chatId, answer);
            }
        }

    }

    private void handleStartCommand(long chatId, String message, Update update) {
        if ("/start".equals(message)) {
            startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
            userStates.put(chatId, BotState.GET_YEAR);
        } else {
            sendMessage(chatId, "Unrecognized command");
        }
    }

    private void handleHelpCommand(long chatId, String message) {
        if ("/help".equals(message)) {
            helpCommandReceived(chatId);
            userStates.put(chatId, BotState.WAITING_FOR_RESPONSE);
        } else {
            sendMessage(chatId, "Unrecognized command");
        }
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = "hello, " + name + ", enter your year of birth";

        log.info("Received command: {}", name);

        sendMessage(chatId, answer);
    }

    private void yearCommandReceived(long chatId, String message) {
        if (utils.checkYearMessage(message)) {
            sendMessage(chatId, "Good, now enter the month", createKeyboardMonth());
            userStates.put(chatId, BotState.GET_MONTH);

            date[0] = message;

            log.info("Received year: {}", message);
        } else {
            sendMessage(chatId, "The year is incorrect, please try again");

            log.info("Received incorrect year {}", message);
        }
    }

    private void monthCommandReceived(long chatId, String message) {
        if (utils.checkMonthMessage(message)) {
            sendMessage(chatId, "Good, and lastly, enter the day");
            userStates.put(chatId, BotState.GET_DAY);

            date[1] = utils.monthToNum(message);

            log.info("Received month {}", message);
        } else {
            sendMessage(chatId, "The month is incorrect, please try again");

            log.info("Received incorrect month {}", message);
        }
    }

    private void dayCommandReceived(long chatId, String message) {
        if (utils.checkDayMessage(message)) {
            sendMessage(chatId, "Thank you! The process is completed.");
            userStates.put(chatId, BotState.COMPLETED);

            date[2] = message;

            log.info("Received day {}", message);

        } else {
            sendMessage(chatId, "The day is incorrect, try again");

            log.info("Received incorrect day {}", message);
        }
    }

    private void helpCommandReceived(long chatId) {
        sendMessage(chatId, HELP_TEXT);

        log.info("Received command help");
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
            log.error("Error while sending message: {}", e.getMessage(), e);
        }

    }

    @Transactional
    protected void registerUser(Message message, Date dateOfBirth) {
        log.info("Entering registerUser method for chatId: {}", message.getChatId());

        if (repository.findById(message.getChatId()).isEmpty()) {
            var chatId = message.getChatId();
            var chat = message.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setDateOfBirth(dateOfBirth);
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            log.info("Saving user: {}", user);

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
