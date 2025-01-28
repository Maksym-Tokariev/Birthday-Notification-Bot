package com.ens.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Date;
import java.util.List;

public class BotUtilsTest {

    private BotUtils botUtils;

    @BeforeEach
    public void setUp() {
        botUtils = new BotUtils();
    }

    @Test
    void testStringToDate() {
        Date date1 = botUtils.stringToDate("2016-11-15");
        Assertions.assertNotNull(date1);
        Assertions.assertEquals(116, date1.getYear());
        Assertions.assertEquals(10, date1.getMonth());
        Assertions.assertEquals(15, date1.getDate());

        Date date2 = botUtils.stringToDate("2004-03-04");
        Assertions.assertNotNull(date2);
        Assertions.assertEquals(104, date2.getYear());
        Assertions.assertEquals(2, date2.getMonth());
        Assertions.assertEquals(4, date2.getDate());

        Date date3 = botUtils.stringToDate("wrong date");
        Assertions.assertNull(date3);

        Date date4 = botUtils.stringToDate(null);
        Assertions.assertNull(date4);

        Date date5 = botUtils.stringToDate("");
        Assertions.assertNull(date5);
    }

    @Test
    void testCheckYear() {
        boolean case1 = botUtils.checkYearMessage("2016");
        Assertions.assertTrue(case1);

        boolean case2 = botUtils.checkYearMessage("1945");
        Assertions.assertTrue(case2);

        boolean case3 = botUtils.checkYearMessage("2015");
        Assertions.assertTrue(case3);

        boolean case4 = botUtils.checkYearMessage("20");
        Assertions.assertFalse(case4);

        boolean case5 = botUtils.checkYearMessage("1700");
        Assertions.assertFalse(case5);

        boolean case6 = botUtils.checkYearMessage("2050");
        Assertions.assertFalse(case6);

        boolean case7 = botUtils.checkYearMessage("");
        Assertions.assertFalse(case7);

        boolean case8 = botUtils.checkYearMessage(null);
        Assertions.assertFalse(case8);
    }

    @Test
    void testCheckMonth() {
        boolean case1 = botUtils.checkMonthMessage("Jen");
        Assertions.assertFalse(case1);

        boolean case2 = botUtils.checkMonthMessage("January");
        Assertions.assertTrue(case2);

        boolean case3 = botUtils.checkMonthMessage("November");
        Assertions.assertTrue(case3);

        boolean case4 = botUtils.checkMonthMessage("D");
        Assertions.assertFalse(case4);

        boolean case5 = botUtils.checkMonthMessage("");
        Assertions.assertFalse(case5);

        boolean case6 = botUtils.checkMonthMessage(null);
        Assertions.assertFalse(case6);
    }

    @Test
    void testCheckDate() {
        boolean case1 = botUtils.checkDayMessage("31");
        Assertions.assertTrue(case1);
        boolean case2 = botUtils.checkDayMessage("1");
        Assertions.assertTrue(case2);
        boolean case3 = botUtils.checkDayMessage("");
        Assertions.assertFalse(case3);
        boolean case4 = botUtils.checkDayMessage("34");
        Assertions.assertFalse(case4);
        boolean case5 = botUtils.checkDayMessage("-1");
        Assertions.assertFalse(case5);
        boolean case6 = botUtils.checkDayMessage(null);
        Assertions.assertFalse(case6);
    }

    @Test
    void testMonthToNum() {
        String case1 = botUtils.monthToNum("January");
        Assertions.assertEquals("01", case1);
        String case2 = botUtils.monthToNum("February");
        Assertions.assertEquals("02", case2);
        String case3 = botUtils.monthToNum("March");
        Assertions.assertEquals("03", case3);
        String case4 = botUtils.monthToNum(" ");
        Assertions.assertEquals("Invalid month", case4);
        String case5 = botUtils.monthToNum(null);
        Assertions.assertEquals("Invalid month", case5);
    }

    @Test
    void testCreateKeyboardMonth() {
        ReplyKeyboardMarkup keyboardMarkup = botUtils.createKeyboardMonth();

        List<KeyboardRow> rows = keyboardMarkup.getKeyboard();

        Assertions.assertEquals(3, rows.size());

        String[] months = {"January", "February", "March", "April", "May", "June", "July",
                "August", "September", "October", "November", "December"};

        int monthIndex = 0;
        for (KeyboardRow row : rows) {
            for (KeyboardButton buttonText : row) {
                Assertions.assertEquals(months[monthIndex], buttonText.getText());
                monthIndex++;
            }
        }

    }
}
