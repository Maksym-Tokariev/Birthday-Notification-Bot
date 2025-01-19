package com.ens.utils;

import com.ens.models.UserGroups;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;


@Slf4j
@Component
@AllArgsConstructor
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
            if (message == null || message.isEmpty()) {
                log.debug("Provided message is null or empty");
            }
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

    public ReplyKeyboardMarkup createKeyboardMonth() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> rows = new ArrayList<>();
        String[] months = {"January", "February", "March", "April", "May", "June", "July",
                "August", "September", "October", "November", "December"};

        KeyboardRow row = new KeyboardRow();

        for (int i = 0; i < months.length; i++) {
            row.add(months[i]);
            if (i == 2 || i == 5 || i == 8) {
                rows.add(row);
                row = new KeyboardRow();
            }
        }

        keyboardMarkup.setKeyboard(rows);

        return keyboardMarkup;
    }

    public InlineKeyboardMarkup createKeyboardCommand(List<UserGroups> groupsList) {

        List<String> groupNames = new ArrayList<>();

        for (UserGroups group : groupsList) {
            groupNames.add(group.getGroupName());
        }

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (String groupName : groupNames) {
            var button = new InlineKeyboardButton();

            button.setText(groupName);
            button.setCallbackData(groupName);

            List<InlineKeyboardButton> rows = new ArrayList<>();
            rows.add(button);
            keyboard.add(rows);
        }

        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }
}
