package com.ens.comands;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@AllArgsConstructor
public class RemoveGroupCommandHandler implements CommandHandler {
    @Override
    public void handle(Update update) {

    }
}
