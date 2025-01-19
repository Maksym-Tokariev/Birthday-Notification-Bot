package com.ens.servise;

import com.ens.models.UserGroups;
import com.ens.models.Users;
import com.ens.repository.UserGroupRepository;
import com.ens.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class BirthdayNotifierService {

    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final MessageService messageService;

    @Scheduled(cron = "0 0 10 * * ?")
    public void notifyBirthdays() {
        log.info("Starting birthday notification process");

        Date today = new Date();

        List<Users> users = userRepository.findAll();

        for (Users user : users) {
            List<UserGroups> groups = userGroupRepository.findGroupByChatId(user.getChatId());

            for (UserGroups group : groups) {
                Long groupId = group.getGroupId();
                if (isToday(user.getDateOfBirth(), today)) {
                    sendBirthdayMessages(user, group);
                } else {
                    log.info("No user has a birthday today");
                }
                if (isTomorrow(user.getDateOfBirth(), today)) {
                    notifyUsersAboutUpcomingBirthday(user, groupId);
                } else {
                    log.info("No user has a birthday tomorrow");
                }
            }
        }

        log.info("Birthday notification process completed.");
    }

    private void notifyUsersAboutUpcomingBirthday(Users user, Long groupId) {
        String message = "Tomorrow is " + user.getFirstName() + "'s birthday!";

        List<Long> classmates = userRepository.findUsersByGroupId(groupId);
        for (Long classmate : classmates) {
            if (!classmate.equals(user.getChatId())) {
                messageService.sendMessage(classmate, message);
                log.info("Upcoming birthday notification sent for user: {}", classmate);
            }
        }


        log.info("Upcoming birthday notification sent for user: {}", user.getChatId());
    }

    private void sendBirthdayMessages(Users user, UserGroups group) {
        String message = "\uD83C\uDF89 Happy Birthday, " + user.getFirstName() + "! \uD83C\uDF89\n" +
                "\n" +
                "\uD83C\uDF82 I wish you great happiness, health and success! May every day bring you joy and new achievements. " +
                "Enjoy your special day and may all your dreams come true! \uD83E\uDD73\n" +
                "\n" +
                "\uD83C\uDF81 May this year be filled with new opportunities and unforgettable moments. You deserve all the best! \uD83C\uDF1F\n" +
                "\n" +
                "Best wishes, \uD83D\uDC8C";

        messageService.sendMessage(group.getGroupId(), message);

        log.info("Birthday congratulation sent for user: {}, group: {}", user.getChatId(), group.getGroupId());
    }

    private boolean isTomorrow(Date dateOfBirth, Date today) {
        if (dateOfBirth == null) {
            return false;
        }

        Calendar calendarBirthday = Calendar.getInstance();
        calendarBirthday.setTime(dateOfBirth);

        Calendar calendarTomorrow = Calendar.getInstance();
        calendarTomorrow.setTime(today);
        calendarTomorrow.add(Calendar.DAY_OF_MONTH, 1);

        return calendarBirthday.get(Calendar.DAY_OF_MONTH) == calendarTomorrow.get(Calendar.DAY_OF_MONTH) &&
                calendarBirthday.get(Calendar.MONTH) == calendarTomorrow.get(Calendar.MONTH);
    }

    private boolean isToday(Date dateOfBirth, Date today) {
        if (dateOfBirth == null) {
            return false;
        }

        Calendar calendarBirthday = Calendar.getInstance();
        calendarBirthday.setTime(dateOfBirth);

        Calendar calendarToday = Calendar.getInstance();
        calendarToday.setTime(today);

        return calendarBirthday.get(Calendar.DAY_OF_MONTH) == calendarToday.get(Calendar.DAY_OF_MONTH) &&
                calendarBirthday.get(Calendar.MONTH) == calendarToday.get(Calendar.MONTH);
    }
}
