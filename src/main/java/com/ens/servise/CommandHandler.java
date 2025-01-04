package com.ens.servise;

import com.ens.models.UserData;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class CommandHandler {

    private final Map<Long, BotState> userStates = new HashMap<>();

    private final BotService botService;

    private final BotUtils utils;

    private final MessageService messageService;

    private final String[] date = new String[5];

    private static final String HELP_TEXT = "This bot sends birthday reminders\n\n"
            + "Commands:\n\n"
            + "The command /start adds you to the notification system\n\n"
            + "The command /mydate shows your information about you\n\n"
            + "The command /deletedata delete information about you\n\n";

    public void handleUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleTextMessage(update);
        } else if (update.hasMyChatMember()) {
            handleChatMessageUpdate(update);
        }
    }

    private void handleChatMessageUpdate(Update update) {
        ChatMemberUpdated myChatMember = update.getMyChatMember();
        if (myChatMember.getNewChatMember().getStatus().equals("member")) {
            long chatId = myChatMember.getChat().getId();
            String answer = "Hi, I'll create a birthday calendar for this group and remind you to congratulate the birthday people. " +
                    "Please visit the bot privately to register: https://t.me/ENSystembot";

            messageService.sendMessage(chatId, answer);
        }
    }

    private void handleTextMessage(Update update) {
        String message = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        BotState state = userStates.getOrDefault(chatId, BotState.START);

        switch (state) {
            case START:
                handleStartCommand(chatId, message, update);
                break;

            case WAITING_FOR_RESPONSE:
                handleWaitingForResponse(update);
                break;

            case GET_YEAR:
                yearCommandReceived(chatId, message);
                break;

            case GET_MONTH:
                monthCommandReceived(chatId, message);
                break;

            case GET_DAY:
                dayCommandReceived(chatId, message, update);
                break;

            case COMPLETED:
                break;

            case HELP:
                handleHelpCommand(chatId, message);
                break;

            default:
                messageService.sendMessage(chatId, "Sorry, command not found");
                userStates.put(chatId, BotState.WAITING_FOR_RESPONSE);
        }
    }

    private void handleWaitingForResponse(Update update) {

        String message = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        if (message.equals("/start")) {
            handleStartCommand(chatId, message, update);
        } else if (message.equals("/mydata")) {
            handleMyDataCommand(chatId);
        } else if (message.equals("/help")) {
            helpCommandReceived(chatId);
        } else if (message.equals("/deletedata")) {
            handleDeleteDataCommand(chatId);
        } else {
            messageService.sendMessage(chatId, "Sorry, command not found *");
            userStates.put(chatId, BotState.WAITING_FOR_RESPONSE);
        }

    }

    private void handleDeleteDataCommand(long chatId) {
        if (botService.userExists(chatId)) {
            botService.deleteUser(chatId);
            messageService.sendMessage(chatId, "You've successfully deleted birthday");
            userStates.put(chatId, BotState.WAITING_FOR_RESPONSE);
        } else {
            messageService.sendMessage(chatId, "No data was found for deletion. Register using the command /start");
        }
    }

    private void handleMyDataCommand(long chatId) {
        Optional<UserData> optionalUserData = botService.getDateOfBirth(chatId);

        if (optionalUserData.isPresent()) {
            UserData userData = optionalUserData.get();
            messageService.sendMessage(chatId, userData.getFirstName() + ", your birthday is" + userData.getDateOfBirth());
        } else {
            messageService.sendMessage(chatId, "No date of birth was found for your account. " +
                    "You may not have entered it. Use the command /start");
        }

        userStates.put(chatId, BotState.WAITING_FOR_RESPONSE);
    }

    private void handleStartCommand(long chatId, String message, Update update) {
        if ("/start".equals(message) && !botService.userExists(chatId)) {
            startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
            userStates.put(chatId, BotState.GET_YEAR);
        } else {
            messageService.sendMessage(chatId, "Command /start not supported. You may have already entered your birthday");
        }
    }

    private void handleHelpCommand(long chatId, String message) {
        if ("/help".equals(message)) {
            helpCommandReceived(chatId);
            userStates.put(chatId, BotState.WAITING_FOR_RESPONSE);
        } else {
            messageService.sendMessage(chatId, "Unrecognized command");
        }
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = "hello, " + name + ", enter your year of birth";

        log.info("Received command: {}", name);

        messageService.sendMessage(chatId, answer);
    }

    private void yearCommandReceived(long chatId, String message) {
        if (utils.checkYearMessage(message)) {
            messageService.sendMessage(chatId, "Good, now enter the month", utils.createKeyboardMonth());
            userStates.put(chatId, BotState.GET_MONTH);

            date[0] = message;

            log.info("Received year: {}", message);
        } else {
            messageService.sendMessage(chatId, "The year is incorrect, please try again");

            log.info("Received incorrect year {}", message);
        }
    }

    private void monthCommandReceived(long chatId, String message) {
        if (utils.checkMonthMessage(message)) {
            messageService.sendMessage(chatId, "Good, and lastly, enter the day");
            userStates.put(chatId, BotState.GET_DAY);

            date[1] = utils.monthToNum(message);

            log.info("Received month {}", message);
        } else {
            messageService.sendMessage(chatId, "The month is incorrect, please try again");

            log.info("Received incorrect month {}", message);
        }
    }

    private void dayCommandReceived(long chatId, String message, Update update) {
        if (utils.checkDayMessage(message)) {
            messageService.sendMessage(chatId, "Thank you! The process is completed.");
            userStates.put(chatId, BotState.WAITING_FOR_RESPONSE);

            date[2] = message;

            String stringDate = String.join("-", date);
            Date dateOfBirth = utils.stringToDate(stringDate);
            botService.registerUser(update.getMessage(), dateOfBirth);

            log.info("Received day {}", message);

        } else {
            messageService.sendMessage(chatId, "The day is incorrect, try again");

            log.info("Received incorrect day {}", message);
        }
    }

    private void helpCommandReceived(long chatId) {
        messageService.sendMessage(chatId, HELP_TEXT);

        log.info("Received command help");
    }

}
