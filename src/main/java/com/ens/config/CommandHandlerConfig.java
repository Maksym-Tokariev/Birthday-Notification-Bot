package com.ens.config;

import com.ens.comands.*;
import com.ens.comands.impl.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CommandHandlerConfig {

    @Bean
    public Map<String, CommandHandler> commandHandlers(StartCommandHandler startCommandHandler,
                                                       HelpCommandHandler helpCommandHandler,
                                                       MyDataCommandHandler myDataCommandHandler,
                                                       DeleteDataCommandHandler deleteDataCommandHandler,
                                                       ListOfMyGroupsCommandHandler list,
                                                       RemoveGroupCommandHandler removeGroupCommandHandler) {
        Map<String, CommandHandler> commandHandlers = new HashMap<>();
        commandHandlers.put("/start", startCommandHandler);
        commandHandlers.put("/help", helpCommandHandler);
        commandHandlers.put("/mydata", myDataCommandHandler);
        commandHandlers.put("/deletedata", deleteDataCommandHandler);
        commandHandlers.put("/mygroups", list);
        commandHandlers.put("/removegroup", removeGroupCommandHandler);
        return commandHandlers;
    }

}
