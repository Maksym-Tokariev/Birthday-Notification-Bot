package com.ens.service;

import com.ens.models.UserContext;
import com.ens.servise.MainCommandHandler;
import com.ens.servise.MessageService;
import com.ens.servise.UserService;
import com.ens.utils.BotState;
import com.ens.utils.BotUtils;
import com.ens.utils.UserStateHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Date;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class MainCommandHandlerTest {

    @Mock
    private BotUtils utils;

    @Mock
    private MessageService messageService;

    @Mock
    private UserStateHandler userStateHandler;

    @Mock
    private UserService userService;

    @InjectMocks
    private MainCommandHandler mainCommandHandler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testYearCommandReceivedValid() {
        long chatId = 1234335L;
        String message = "2003";

        when(utils.checkYearMessage(message)).thenReturn(true);
        UserContext context = new UserContext();
        when(userStateHandler.getContext(anyLong())).thenReturn(context);

        mainCommandHandler.yearCommandReceived(chatId, message);

        verify(messageService).sendMessage(chatId, "Good, now enter the month", utils.createKeyboardMonth());
        verify(userStateHandler).setState(chatId, BotState.GET_MONTH);
        verify(userStateHandler).setContext(eq(chatId), eq(context));

        Assertions.assertEquals("2003", context.getDate()[0]);
    }

    @Test
    void testYearCommandReceivedInvalid() {
        long chatId = 1234335L;
        String message = "1777";

        when(utils.checkYearMessage(message)).thenReturn(false);
        mainCommandHandler.yearCommandReceived(chatId, message);
        verify(messageService).sendMessage(chatId, "The year is incorrect, please try again");
    }

    @Test
    void testMonthCommandReceivedValid() {
        long chatId = 1234335L;
        String message = "January";

        when(utils.checkMonthMessage(message)).thenReturn(true);
        when(utils.monthToNum(message)).thenReturn("01");
        UserContext context = new UserContext();
        when(userStateHandler.getContext(anyLong())).thenReturn(context);

        mainCommandHandler.monthCommandReceived(chatId, message);

        verify(messageService).sendMessage(chatId, "Good, and lastly, enter the day");
        verify(userStateHandler).setState(chatId, BotState.GET_DAY);
        verify(userStateHandler).setContext(eq(chatId), eq(context));

        Assertions.assertEquals("01", context.getDate()[1]);
    }

    @Test
    void testMonthCommandReceivedInvalid() {
        long chatId = 1234335L;
        String message = "dfdfs";


        when(utils.checkMonthMessage(message)).thenReturn(false);
        mainCommandHandler.monthCommandReceived(chatId, message);
        verify(messageService).sendMessage(chatId, "The month is incorrect, please try again");
    }

    @Test
    void testDayCommandReceivedValid() {
        long chatId = 1234335L;
        String message = "05";
        Update update = mock(Update.class);
        Message telegramMessage = mock(Message.class);

        when(update.getMessage()).thenReturn(telegramMessage);
        when(telegramMessage.getChatId()).thenReturn(chatId);

        when(utils.checkDayMessage(message)).thenReturn(true);
        UserContext context = new UserContext();
        when(userStateHandler.getContext(anyLong())).thenReturn(context);

        doNothing().when(userService).registerUser(any(Message.class), any(Long.class), any(Date.class), any(String.class));

        mainCommandHandler.dayCommandReceived(chatId, message, update);

        verify(messageService).sendMessage(chatId, "Thank you! The process is completed.");
        verify(userStateHandler).setState(chatId, BotState.WAITING_FOR_RESPONSE);
        verify(userStateHandler).setContext(eq(chatId), eq(context));

        Assertions.assertEquals("05", context.getDate()[2]);
    }

    @Test
    void testDayCommandReceivedInvalid() {
        long chatId = 1234335L;
        String message = "d";
        Update update = new Update();

        when(utils.checkDayMessage(message)).thenReturn(false);
        mainCommandHandler.dayCommandReceived(chatId, message, update);
        verify(messageService).sendMessage(chatId, "The day is incorrect, try again");
    }

    @Test
    void testRegistration() {
        long chatId = 1234335L;
        long groupId = 1234336L;

        Update update = mock(Update.class);
        Message telegramMessage = mock(Message.class);

        when(update.getMessage()).thenReturn(telegramMessage);
        when(telegramMessage.getChatId()).thenReturn(chatId);

        UserContext context = new UserContext();
        context.setGroupId(groupId);
        context.setGroupName("Test Group");
        context.setDate(new String[] {"2003", "04", "23"});

        when(userStateHandler.getContext(chatId)).thenReturn(context);
        when(utils.stringToDate("2003-04-23")).thenReturn(new Date());
        doNothing().when(userService).registerUser(any(Message.class), any(Long.class), any(Date.class), any(String.class));

        mainCommandHandler.registration(update);

        verify(userStateHandler).getContext(chatId);
        verify(utils).stringToDate("2003-04-23");
        verify(userService).registerUser(eq(telegramMessage), eq(groupId), any(Date.class), eq("Test Group"));
    }
}
