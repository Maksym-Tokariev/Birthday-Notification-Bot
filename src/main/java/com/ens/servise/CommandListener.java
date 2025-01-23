package com.ens.servise;

import com.ens.comands.CommandHandler;
import com.ens.config.rabbitmq.RabbitMQConfig;
import com.ens.utils.BotState;
import com.ens.utils.UserStateHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class CommandListener {

    private final UserStateHandler userStateHandler;
    private final Map<String, CommandHandler> commandHandlers;
    private final MainCommandHandler mainCommandHandler;

    @RabbitListener(queues = RabbitMQConfig.COMMAND_QUEUE)
    public void handleCommand(Update update) {
        try {
            log.info("CommandListener consume update: {}", update);
            if (update.hasMessage() && update.getMessage().hasText()) {
                String command = update.getMessage().getText();
                long chatId = update.getMessage().getChatId();

                BotState state = userStateHandler.getState(chatId);

                log.info("Method handleCommand, state: {}, message: {}", state, command);
                CommandHandler handler = commandHandlers.get(command);

                if (handler != null) {
                    handler.handle(update);
                    log.info("Method handleCommand, handler: {}", handler);
                } else {
                    mainCommandHandler.handleCommand(chatId, command, state, update);
                    log.warn("No handler found for message: {}", command);
                }
            } else {
                log.warn("Received update without a valid message: {}", update);
            }
        } catch (Exception e) {
            log.error("Error handling update: {}", e.getMessage(), e);
        }
    }

}
