package com.ens.servise;

import com.ens.models.UserData;
import com.ens.models.Users;
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

    private final GroupService groupService;
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final CacheService cacheService;

    @Transactional
    public void registerUser(Message message, Long groupId, Date dateOfBirth, String groupName) {
        log.info("Method registerUser called in UserService with groupId: {} and message: {}", groupId, message);

        var chatId = message.getChatId();
        var chat = message.getChat();

        Optional<Users> optionalUsers = userRepository.findById(chatId);

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

        if (!groupService.groupExists(groupId)) {
            groupService.registerGroup(groupId, groupName);
            log.info("Group registered: {}", groupName);
        }

        if (!groupService.isUserInGroup(chatId, groupId)) {
            groupService.addUserToGroup(users.getChatId(), groupId);
            log.info("Saved user in users_groups: {} with group: {}", users.getChatId(), groupId);
        }

        log.info("Method registerUser was performed in UserService with groupId: {} and chatId: {}", groupId, chatId);
        userWasRegistered(groupId, users);
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

    public void deleteUser(Long chatId) {
        groupService.removeUserFromAllGroups(chatId);
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

    public Optional<UserData> getCachedDateOfBirth(Long chatId) {
        log.info("Method getCachedDateOfBirth called in UserService with chatId: {}", chatId);

        return cacheService.getCachedUserData(chatId).or(() -> {
            var data = getDateOfBirth(chatId);
            data.ifPresent(userData -> cacheService.cacheUserData(chatId, userData));
            return data;
        });
    }

    private void userWasRegistered(Long groupId, Users users) {
        String groupMessage = "@" + users.getUserName() + " has registered their birthday for this group.";
        messageService.sendMessage(groupId, groupMessage);
    }
}
