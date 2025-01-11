package com.ens.servise;
//
//import com.ens.models.User;
//import com.ens.models.UserRepository;
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
//public class EventCheck {
//
//    private final UserRepository repository;
//
//    private final MessageService messageService;
//
//    @Scheduled(cron = "0 0 10 * * ?")
//    private void birthdayCheck() {
//        Date today = new Date();
//
//        log.info("Today is {}", today);
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(today);
//        calendar.add(Calendar.DAY_OF_MONTH, 1);
//
//        Date tomorrow = calendar.getTime();
//
//        log.info("Tomorrow is {}", tomorrow);
//
//        List<User> users = repository.findAll();
//
//        for (User user : users) {
//            log.info("User is {}", user);
//
//            Date birthday = user.getDateOfBirth();
//            Calendar birthCalendar = Calendar.getInstance();
//            birthCalendar.setTime(birthday);
//            birthCalendar.add(Calendar.YEAR, calendar.get(Calendar.YEAR));
//
//            if (birthCalendar.getTime().compareTo(tomorrow) == 0) {
//                log.info("User is a birthday");
//                for (User targ : users) {
//                    if (!targ.getChatId().equals(user.getChatId())) {
//                        String notify = "Напоминание: завтра день рождения у " + user.getFirstName() + "!";
//                        messageService.sendMessage(user.getChatId(), notify);
//                    }
//                }
//            } else {
//                log.info("User is not a birthday");
//            }
//        }
//    }
//}
