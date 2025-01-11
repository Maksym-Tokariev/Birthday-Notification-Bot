package com.ens.servise;

import com.ens.models.UserData;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
@AllArgsConstructor
public class CommandHandler {

    private final Map<Long, BotState> userStates = new HashMap<>();

    private final UserService userService;

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
            Long groupId = myChatMember.getChat().getId();
            String groupName = myChatMember.getChat().getTitle();

            String encodedGroupId = URLEncoder.encode("groupId_" + groupId, StandardCharsets.UTF_8);
            String encodedGroupName = URLEncoder.encode("groupName_" + groupName, StandardCharsets.UTF_8);
            String answer = "Hi, I'll create a birthday calendar for this group and remind you to congratulate the birthday people. " +
                    "Please visit the bot privately to register: https://t.me/ENSystembot?start=" + encodedGroupId + "_" + encodedGroupName;

            messageService.sendMessage(groupId, answer);
        }
    }

    private void handleTextMessage(Update update) {
        String message = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        BotState state = userStates.getOrDefault(chatId, BotState.WAITING_FOR_RESPONSE);

        switch (state) {
            case START:
                log.info("12345---------");
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
                userStates.put(chatId, BotState.WAITING_FOR_RESPONSE);
        }
    }

    private void handleWaitingForResponse(Update update) {

        String message = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        String[] parts = message.split(" ");

        log.info("--handleWaitingForResponse--");

        if ("/start".equals(parts[0])) {
            handleStartCommand(chatId, message, update);
        } else if (message.equals("/mydata")) {
            handleMyDataCommand(chatId);
        } else if (message.equals("/help")) {
            helpCommandReceived(chatId);
        } else if (message.equals("/deletedata")) {
            handleDeleteDataCommand(chatId);
        } else if (message.equals("/mygroups")) {
            listOfMyGroups(chatId);
        } else {
            messageService.sendMessage(chatId, "Sorry, command not found");
            userStates.put(chatId, BotState.WAITING_FOR_RESPONSE);
        }

    }

    private void handleDeleteDataCommand(Long chatId) {
        if (userService.userExists(chatId)) {
            userService.deleteUser(chatId);
            messageService.sendMessage(chatId, "You've successfully deleted birthday");
            userStates.put(chatId, BotState.WAITING_FOR_RESPONSE);
        } else {
            messageService.sendMessage(chatId, "No data was found for deletion. Register using the command /start");
            userStates.put(chatId, BotState.WAITING_FOR_RESPONSE);
        }
    }

    private void handleMyDataCommand(Long chatId) {
        Optional<UserData> optionalUserData = userService.getDateOfBirth(chatId);

        if (optionalUserData.isPresent()) {
            UserData userData = optionalUserData.get();
            messageService.sendMessage(chatId, userData.getFirstName() + ", your birthday is " + userData.getDateOfBirth());
        } else {
            messageService.sendMessage(chatId, "No date of birth was found for your account. " +
                    "You may not have entered it. Use the command /start");
        }

        userStates.put(chatId, BotState.WAITING_FOR_RESPONSE);
    }

    private void listOfMyGroups(Long chatId) {
        // TODO
    }

    private void handleStartCommand(Long chatId, String message, Update update) {
        String[] parts = message.split(" ");

        if (parts.length > 1) {
            try {
                String[] groupIdParts = parts[1].split("_");
                Long groupId = Long.parseLong(groupIdParts[1]);
                String groupName = groupIdParts[3];
                date[4] = groupName;
                if (!userService.userExists(chatId)) {
                    log.info("In handleStartCommand if-3");
                    startCommandReceived(chatId, groupId, update.getMessage().getChat().getFirstName());
                } else {
                    messageService.sendMessage(chatId, "You're already registered, I've added you to a new group: " + date[4] +
                            ". If the group is not missing from the database, I will add it.");
                    date[3] = String.valueOf(groupId);
                    registration(update);
                    userStates.put(chatId, BotState.WAITING_FOR_RESPONSE);
                }
            } catch (NumberFormatException e) {
                messageService.sendMessage(chatId, "Invalid group ID");
                userStates.put(chatId, BotState.WAITING_FOR_RESPONSE);
            }
        } else {
            messageService.sendMessage(chatId, "Group ID is missing. Try again or change link");
            userStates.put(chatId, BotState.WAITING_FOR_RESPONSE);
        }
    }

    private void handleHelpCommand(Long chatId, String message) {
        if ("/help".equals(message)) {
            helpCommandReceived(chatId);
            userStates.put(chatId, BotState.WAITING_FOR_RESPONSE);
        } else {
            messageService.sendMessage(chatId, "Unrecognized command");
        }
    }

    private void startCommandReceived(Long chatId, Long groupId, String name) {
        String answer = "hello, " + name + ", enter your year of birth";

        date[3] = String.valueOf(groupId);

        log.info("Received command name: {}, groupId: {}", name, groupId);

        userStates.put(chatId, BotState.GET_YEAR);

        messageService.sendMessage(chatId, answer);
    }

    private void yearCommandReceived(Long chatId, String message) {
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

    private void monthCommandReceived(Long chatId, String message) {
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

    private void dayCommandReceived(Long chatId, String message, Update update) {
        if (utils.checkDayMessage(message)) {
            messageService.sendMessage(chatId, "Thank you! The process is completed.");
            userStates.put(chatId, BotState.WAITING_FOR_RESPONSE);

            date[2] = message;

            log.info("Received day {}", message);

            registration(update);

        } else {
            messageService.sendMessage(chatId, "The day is incorrect, try again");

            log.info("Received incorrect day {}", message);
        }
    }

    private void helpCommandReceived(Long chatId) {
        messageService.sendMessage(chatId, HELP_TEXT);

        log.info("Received command help");
    }

    private void registration(Update update) {
        Long groupId = Long.parseLong(date[3]);
        String stringDate = String.join("-", date);
        Date dateOfBirth = utils.stringToDate(stringDate);
        String groupName = date[4];

        log.info("Received date: {}, groupId: {}", dateOfBirth, groupId);

        userService.registerUser(update.getMessage(), groupId, dateOfBirth, groupName);
    }
}
