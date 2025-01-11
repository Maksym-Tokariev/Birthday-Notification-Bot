package com.ens;

import com.ens.config.BotConfig;
//import com.ens.servise.BotService;
import com.ens.servise.BotUtils;
import com.ens.servise.TelegramBot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.mockito.Mockito.*;

@SpringBootTest
class EnsApplicationTests {

    @Mock
    private BotUtils utils;

    @Mock
    private BotConfig config;

//    @Mock
//    private BotService service;

    @InjectMocks
    private TelegramBot bot;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void contextLoads() throws TelegramApiException {
        Update update = mock(Update.class);
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage().hasText()).thenReturn(true);
        when(update.getMessage().getText()).thenReturn("/start");
        when(update.getMessage().getChatId()).thenReturn(1L);
        when(update.getMessage().getChat().getFirstName()).thenReturn("User");

        when(config.getBotName()).thenReturn("TestBot");
        when(config.getBotToken()).thenReturn("TestToken");

        bot.onUpdateReceived(update);

//        verify(bot).sendMessage(any(SendMessage.class));

    }

}
