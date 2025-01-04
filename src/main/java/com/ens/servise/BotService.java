package com.ens.servise;

import com.ens.models.User;
import com.ens.models.UserData;
import com.ens.models.UserRepository;
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
public class BotService {

    private final UserRepository repository;

    @Transactional
    protected void registerUser(Message message, Date dateOfBirth) {
        log.info("Entering registerUser method for chatId: {}", message.getChatId());

        if (repository.findById(message.getChatId()).isEmpty()) {
            var chatId = message.getChatId();
            var chat = message.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setDateOfBirth(dateOfBirth);
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            log.info("Saving user: {}", user);

            repository.save(user);

            log.info("user saved: {}", user);
        }
    }

    public boolean userExists(long chatId) {
        log.info("Entering userExists method for chatId: {}", chatId);

        return repository.findById(chatId).isPresent();
    }

    public Optional<UserData> getDateOfBirth(long chatId) {
        return repository.findById(chatId).map(user -> {
            UserData userData = new UserData();
            userData.setDateOfBirth(String.valueOf(user.getDateOfBirth()));
            userData.setFirstName(user.getFirstName());

            log.info("user found in getDateOfBirth: {}", userData);
            return userData;
        });
    }

    public void deleteUser(long chatId) {
        repository.deleteById(chatId);

        log.info("user deleted: {}", chatId);
    }
}
