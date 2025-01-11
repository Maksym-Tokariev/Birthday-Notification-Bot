package com.ens.models;

import lombok.Data;

import java.util.List;

@Data
public class Group {
    private Long groupId;
    private String groupName;
    private List<Users> users;
}
