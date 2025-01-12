package com.ens.servise;

import com.ens.models.Group;
import com.ens.models.UserData;
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
import java.util.Date;
import java.util.Optional;

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

        if (optionalGroup.isEmpty()) {
            registerGroup(groupId, groupName);
            log.info("Group registered: {}", groupName);
        }

        if (groupUserGroupRepository.findUserAndGroup(chatId, groupId).isEmpty()) {
            groupRepository.saveUserGroup(users.getChatId(), groupId);
            log.info("Saved user in users_groups: {} with group: {}", users.getChatId(), groupId);
        }

        userWasRegistered(groupId, users);

    }

    @Transactional
    public void registerGroup(Long groupId, String groupName) {
        Group newGroup = new Group();

        newGroup.setGroupId(groupId);
        newGroup.setGroupName(groupName);
//            newGroup.setUsers(userRepository.findAllWithGroupId(groupId)); TODO

        groupRepository.save(newGroup);
        log.info("Group has been registered: {}", newGroup);
    }

    public boolean userExists(Long chatId) {
        log.info("userExists chatId: {}", chatId);
        try {
            return userRepository.findById(chatId).isPresent();
        } catch (Exception e) {
            log.error("userExists error: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean groupExists(Long groupId) {
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
        return userRepository.findById(chatId).map(users -> {
            UserData userData = new UserData();
            userData.setDateOfBirth(String.valueOf(users.getDateOfBirth()));
            userData.setFirstName(users.getFirstName());

            log.info("user found in getDateOfBirth: {}", userData);

            return userData;
        });
    }

    private void userWasRegistered(Long groupId, Users users) {
        String groupMessage = "@" + users.getUserName() + " has registered their birthday fo this group.";
        messageService.sendMessage(groupId, groupMessage);
    }
}
