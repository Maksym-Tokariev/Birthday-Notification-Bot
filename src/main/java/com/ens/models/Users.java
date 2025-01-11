package com.ens.models;

import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Data
public class Users {

    private Long chatId;
    private String firstName;
    private String lastName;
    private String userName;
//    private String photoUrl;
    private Date dateOfBirth;
    private Timestamp registeredAt;
    private List<Group> groups;
}
