package com.ens.models;

import lombok.Data;

@Data
public class UserContext {
    private Long chatId;
    private Long groupId;
    private String groupName;
    private String[] date = new String[3];

    public UserContext(Long chatId) {
    }
}
