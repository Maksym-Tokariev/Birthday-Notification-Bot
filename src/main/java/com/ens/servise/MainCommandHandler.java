package com.ens.servise;

import com.ens.comands.CommandHandler;
import com.ens.models.UserContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class MainCommandHandler {

    private final UserService userService;
    private final BotUtils utils;
    private final MessageService messageService;
    private final UserStateHandler userStateHandler;
    private final Map<String, CommandHandler> commandHandlers;

    public void handleUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleTextMessage(update);
        } else if (update.hasMyChatMember()) {
            handleChatMessageUpdate(update);
        } else if (update.hasCallbackQuery()) {
            handleQuery(update);
        }
    }

    private void handleQuery(Update update) {
        String callbackQuery = update.getCallbackQuery().getData();
        long messageId = update.getCallbackQuery().getMessage().getMessageId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        log.info("Received query: {}, messageId: {}, chatId: {}", callbackQuery, messageId, chatId);

        userService.deleteUserGroup(callbackQuery,chatId);
        messageService.sendMessage(chatId, "You have been successfully removed from the group " + callbackQuery +
                ". Your birthday messages will no longer be sent to this group");
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
            log.info("The bot has been added to the group");
        }
    }

    private void handleTextMessage(Update update) {
        String message = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        BotState state = userStateHandler.getState(chatId);

        log.info("Method handleTextMessage, state: {}, message: {}", state, message);
        CommandHandler handler = commandHandlers.get(message);

        if (handler != null) {
            handler.handle(update);
        } else {
            switch (state) {

                case WAITING_FOR_RESPONSE:
                    log.info("handleTextMessage, waiting for response {}, {}", message, state);
                    handleWaitingForResponse(chatId, message, update);
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

                default:
                    log.error("Unknown state: {}", state);
            }
        }

    }

    private void handleWaitingForResponse(Long chatId, String message, Update update) {
        String[] parts = message.split(" ");

        log.info("handleWaitingForResponse, chatId: {}, message: {}", chatId, message);
        switch (parts[0]) {
            case "/start":
                CommandHandler startHandler = commandHandlers.get("/start");
                if (startHandler != null) {
                    startHandler.handle(update);
                }
                break;
            case "/mydata":
                CommandHandler myDataHandler = commandHandlers.get("/mydata");
                if (myDataHandler != null) {
                    myDataHandler.handle(update);
                }
                break;
            case "/help":
                CommandHandler helpHandler = commandHandlers.get("/help");
                if (helpHandler != null) {
                    helpHandler.handle(update);
                }
                break;
            case "/deletedata":
                CommandHandler deleteDataHandler = commandHandlers.get("/deletedata");
                if (deleteDataHandler != null) {
                    deleteDataHandler.handle(update);
                }
                break;
            default:
                messageService.sendMessage(chatId, "Sorry, command not found");
        }
    }

    private void yearCommandReceived(Long chatId, String message) {
        if (utils.checkYearMessage(message)) {
            messageService.sendMessage(chatId, "Good, now enter the month", utils.createKeyboardMonth());
            userStateHandler.setState(chatId, BotState.GET_MONTH);

            UserContext context = userStateHandler.getContext(chatId);
            context.getDate()[0] = message;
            userStateHandler.setContext(chatId, context);

            log.info("Received year: {}", message);
        } else {
            messageService.sendMessage(chatId, "The year is incorrect, please try again");
            log.info("Received incorrect year {}", message);
        }
    }

    private void monthCommandReceived(Long chatId, String message) {
        if (utils.checkMonthMessage(message)) {
            messageService.sendMessage(chatId, "Good, and lastly, enter the day");
            userStateHandler.setState(chatId, BotState.GET_DAY);

            UserContext context = userStateHandler.getContext(chatId);
            context.getDate()[1] = utils.monthToNum(message);
            userStateHandler.setContext(chatId, context);

            log.info("Received month {}", message);
        } else {
            messageService.sendMessage(chatId, "The month is incorrect, please try again");

            log.info("Received incorrect month {}", message);
        }
    }

    private void dayCommandReceived(Long chatId, String message, Update update) {
        if (utils.checkDayMessage(message)) {
            messageService.sendMessage(chatId, "Thank you! The process is completed.");
            userStateHandler.setState(chatId, BotState.WAITING_FOR_RESPONSE);

            UserContext context = userStateHandler.getContext(chatId);
            context.getDate()[2] = message;
            userStateHandler.setContext(chatId, context);

            log.info("Received day {}", message);

            registration(update);

        } else {
            messageService.sendMessage(chatId, "The day is incorrect, try again");

            log.info("Received incorrect day {}", message);
        }
    }

    private void registration(Update update) {
        UserContext context = userStateHandler.getContext(update.getMessage().getChatId());
        Long groupId = context.getGroupId();

        String stringDate = String.join("-", context.getDate());
        Date dateOfBirth = utils.stringToDate(stringDate);
        String groupName = context.getGroupName();

        log.info("Received date: {}, groupId: {}", dateOfBirth, groupId);

        userService.registerUser(update.getMessage(), groupId, dateOfBirth, groupName);
    }
}
