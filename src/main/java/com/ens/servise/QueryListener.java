package com.ens.servise;

import com.ens.config.rabbitmq.RabbitMQConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@AllArgsConstructor
public class QueryListener {

    private final GroupService groupService;
    private final MessageService messageService;

    @RabbitListener(queues = RabbitMQConfig.QUERY_QUEUE)
    public void handleQuery(Update update) {
        try {
            log.info("QueryListener consume update: {}", update);
            if (update.hasCallbackQuery()) {
                String groupName = update.getCallbackQuery().getData();
                long messageId = update.getCallbackQuery().getMessage().getMessageId();
                long chatId = update.getCallbackQuery().getMessage().getChatId();

                log.info("Received query: {}, messageId: {}, chatId: {}", groupName, messageId, chatId);

                groupService.deleteUserGroup(groupName,chatId);
                messageService.sendMessage(chatId, "You have been successfully removed from the group " + groupName +
                        ". Your birthday messages will no longer be sent to this group");
            } else {
                log.warn("Received update without a valid callback query: {}", update);
            }
        } catch (Exception e) {
            log.error("Error handling query: {}",e.getMessage(), e);
        }

    }

}
