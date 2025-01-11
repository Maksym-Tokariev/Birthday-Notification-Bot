package com.ens.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class UserGroup {
    private Long chatId;
    private Long groupId;

    public UserGroup(Long chatId, Long groupId) {
    }
}
