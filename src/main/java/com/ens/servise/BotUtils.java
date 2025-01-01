package com.ens.servise;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Component
public class BotUtils {
    public Date stringToDate(String date) {

        if (date == null || date.isEmpty()) {
            log.error("Provided date string is null or empty");
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);

        try {
            Date birthday = sdf.parse(date);
            return birthday;
        } catch (ParseException e) {
            log.error("Error while parsing date: {}", e.getMessage(), e);
        }
        return null;
    }

    public boolean checkYearMessage(String message) {
        try {
            int year = Integer.parseInt(message);
            int currYear = LocalDate.now().getYear();

            return (year >= 1900 && year <= currYear);
        } catch (NumberFormatException e) {
            log.error("Invalid year format: {}", message);
            return false;
        }
    }

    public boolean checkMonthMessage(String message) {
        String[] months = {"January", "February", "March", "April", "May", "June", "July",
                "August", "September", "October", "November", "December"};
        for (String month : months) {
            if (message.contains(month)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkDayMessage(String message) {
        int day = Integer.parseInt(message);
        for (int i = 0; i < message.length(); i++) {
            if (day >= 1 && day <= 31) {
                return true;
            }
        }
        return false;
    }

    public String monthToNum(String month) {
        Map<String, String> map = new HashMap<>();
        map.put("January", "01");
        map.put("February", "02");
        map.put("March", "03");
        map.put("April", "04");
        map.put("May", "05");
        map.put("June", "06");
        map.put("July", "07");
        map.put("August", "08");
        map.put("September", "09");
        map.put("October", "10");
        map.put("November", "11");
        map.put("December", "12");

        for (String s : map.keySet()) {
            if (month.equals(s)) {
                month = map.get(s);
                return month;
            }
        }
        return month;
    }
}
