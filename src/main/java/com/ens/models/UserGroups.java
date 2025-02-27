package com.ens.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserGroups {
    private String userName;
    private String groupName;
    private Long groupId;
}
