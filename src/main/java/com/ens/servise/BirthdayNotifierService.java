package com.ens.servise;

//import com.ens.models.Groups;
//import com.ens.models.GroupsRepository;
//import com.ens.models.User;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//
//@Slf4j
//@Service
//@AllArgsConstructor
//public class BirthdayNotifierService {
//
//    private final UserReposetory userRepository;
//
//    private final GroupsRepository groupsRepository;
//
//    private final MessageService messageService;
//
//    @Scheduled(cron = "0 18 21 * * ?")
//    public void notifyBirthdays() {
//        log.info("Starting birthday notification process.");
//
//        List<Groups> groups = groupsRepository.findAll();
//        Date today = new Date();
//
//        for (Groups group : groups) {
//            List<User> users = group.getUser();
//            for (User user : users) {
//                if (isToday(user.getDateOfBirth(), today)) {
//                    notifyUsersAboutUpcomingBirthday(user, group);
//                }
//                if (isTomorrow(user.getDateOfBirth(), today)) {
//                    sendBirthdayMessages(user, group);
//                }
//            }
//        }
//        log.info("Birthday notification process completed.");
//    }
//
//    private void notifyUsersAboutUpcomingBirthday(User user, Groups group) {
//        String message = "Tomorrow is " + user.getFirstName() + "'s birthday!";
//
//        for (User u : group.getUser()) {
//            if (!u.getChatId().equals(user.getChatId())) {
//                messageService.sendMessage(user.getChatId(), message);
//            }
//        }
//        log.info("Upcoming birthday notification sent for user: {}, group: {}", user.getChatId(), group.getGroupId());
//    }
//
//    private void sendBirthdayMessages(User user, Groups group) {
//        String message = "Happy Birthday, " + user.getFirstName() + "!";
//
//        messageService.sendMessage(group.getGroupId(), message);
//
//        log.info("Birthday congratulation sent for user: {}, group: {}", user.getChatId(), group.getGroupId());
//    }
//
//    private boolean isTomorrow(Date dateOfBirth, Date today) {
//        if (dateOfBirth == null) {
//            return false;
//        }
//
//        Calendar calendarBirthday = Calendar.getInstance();
//        calendarBirthday.setTime(dateOfBirth);
//
//        Calendar calendarTomorrow = Calendar.getInstance();
//        calendarTomorrow.setTime(today);
//        calendarBirthday.add(Calendar.DAY_OF_MONTH, 1);
//
//        return calendarBirthday.get(Calendar.DAY_OF_MONTH) == calendarTomorrow.get(Calendar.DAY_OF_MONTH) &&
//                calendarBirthday.get(Calendar.MONTH) == calendarTomorrow.get(Calendar.MONTH);
//    }
//
//    private boolean isToday(Date dateOfBirth, Date today) {
//        if (dateOfBirth == null) {
//            return false;
//        }
//
//        Calendar calendarBirthday = Calendar.getInstance();
//        calendarBirthday.setTime(dateOfBirth);
//
//        Calendar calendarToday = Calendar.getInstance();
//        calendarToday.setTime(today);
//
//        return calendarBirthday.get(Calendar.DAY_OF_MONTH) == calendarToday.get(Calendar.DAY_OF_MONTH) &&
//                calendarBirthday.get(Calendar.MONTH) == calendarToday.get(Calendar.MONTH);
//    }
//}
