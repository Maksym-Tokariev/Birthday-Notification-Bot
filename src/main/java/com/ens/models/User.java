package com.ens.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.sql.Timestamp;
import java.util.Date;

@Entity(name = "usersData")
//@AllArgsConstructor
//@NoArgsConstructor
//@Getter
//@Setter
@Data
public class User {

    @Id
    private Long chatId;

    private String firstName;

    private String lastName;

    private String userName;

    private Date dateOfBirth;

    private Timestamp registeredAt;

}
