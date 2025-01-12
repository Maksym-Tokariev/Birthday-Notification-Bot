package com.ens.servise;

import com.ens.models.Group;
import com.ens.models.UserData;
import com.ens.models.UserGroups;
import com.ens.models.Users;
import com.ens.repository.GroupRepository;
import com.ens.repository.UserGroupRepository;
import com.ens.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final UserGroupRepository groupUserGroupRepository;
    private final MessageService messageService;

    @Transactional
    public void registerUser(Message message, Long groupId, Date dateOfBirth, String groupName) {
        log.info("Method registerUser called in UserService with groupId: {} and message: {}", groupId, message);

        var chatId = message.getChatId();
        var chat = message.getChat();

        Optional<Users> optionalUsers = userRepository.findById(chatId);
        Optional<Group> optionalGroup = groupRepository.findById(groupId);

        Users users = optionalUsers.orElseGet(() -> {
            Users newUser = new Users();
            newUser.setChatId(chatId);
            newUser.setFirstName(chat.getFirstName());
            newUser.setLastName(chat.getLastName());
            newUser.setDateOfBirth(dateOfBirth);
            newUser.setUserName(chat.getUserName());
            newUser.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            log.info("User created: {}", newUser);
            userRepository.save(newUser);
            log.info("Saved user with chatId: {}", newUser.getChatId());

            return newUser;
        });

        log.info("User {} with chatId: {}", users, chatId);

        if (optionalGroup.isEmpty()) {
            registerGroup(groupId, groupName);
            log.info("Group registered: {}", groupName);
        }

        if (groupUserGroupRepository.findUserAndGroup(chatId, groupId).isEmpty()) {
            groupRepository.saveUserGroup(users.getChatId(), groupId);
            log.info("Saved user in users_groups: {} with group: {}", users.getChatId(), groupId);
        }

        log.info("Method registerUser was performed in UserService with groupId: {} and chatId: {}", groupId, chatId);

        userWasRegistered(groupId, users);

    }

    @Transactional
    public void registerGroup(Long groupId, String groupName) {
        log.info("Method registerGroup called in UserService with groupId: {}", groupId);
        Group newGroup = new Group();

        newGroup.setGroupId(groupId);
        newGroup.setGroupName(groupName);
//            newGroup.setUsers(userRepository.findAllWithGroupId(groupId)); TODO

        groupRepository.save(newGroup);
        log.info("Group has been registered: {}", newGroup);
    }

    public boolean userExists(Long chatId) {
        log.info("Method userExists called in UserService with chatId: {}", chatId);
        try {
            return userRepository.findById(chatId).isPresent();
        } catch (Exception e) {
            log.error("userExists error: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean groupExists(Long groupId) {
        log.info("Method groupExists called in UserService with groupId: {}", groupId);
        try {
            return groupRepository.findById(groupId).isPresent();
        } catch (Exception e) {
            log.error("groupExists error: {}", e.getMessage(), e);
            return false;
        }
    }

    public void deleteUser(Long chatId) {
        groupUserGroupRepository.deleteUserAndGroup(chatId);
        userRepository.deleteUser(chatId);
    }

    public Optional<UserData> getDateOfBirth(Long chatId) {
        log.info("Method getDateOfBirth called in UserService with chatId: {}", chatId);

        return userRepository.findById(chatId).map(users -> {
            UserData userData = new UserData();
            userData.setDateOfBirth(String.valueOf(users.getDateOfBirth()));
            userData.setFirstName(users.getFirstName());

            log.info("user found in getDateOfBirth: {}", userData);

            return userData;
        });
    }

    public List<UserGroups> listOfGroups(Long chatId) {
        log.info("Method listOfGroups called in UserService with chatId: {}", chatId);

        List<UserGroups> groupsList = groupUserGroupRepository.findGroupByChatId(chatId);

        for (UserGroups userGroups : groupsList) {
            if (userGroups.getGroupName() == null) {
                log.warn("One of the groups is null: {}", userGroups);
                break;
            }
        }

        if (groupsList.isEmpty()) {
            log.info("No groups found for chatId: {}", chatId);
        } else {
            log.info("Found {} groups", groupsList.size());
        }

        return groupsList;
    }

    private void userWasRegistered(Long groupId, Users users) {
        String groupMessage = "@" + users.getUserName() + " has registered their birthday for this group.";
        messageService.sendMessage(groupId, groupMessage);
    }
}
