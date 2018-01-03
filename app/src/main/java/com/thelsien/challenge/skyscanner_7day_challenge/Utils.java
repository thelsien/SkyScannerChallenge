package com.thelsien.challenge.skyscanner_7day_challenge;

import android.support.annotation.NonNull;

import java.util.Calendar;

/**
 * Created by frodo on 2018-01-03.
 */

public class Utils {
    @NonNull
    public static Calendar getNextMonday() {
        Calendar calendar = Calendar.getInstance();
        //task specified we need to get the NEXT monday's live pricing,
        //so even if today is monday, I skip to the next monday.
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            calendar.add(Calendar.DATE, 1);
        }

        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DATE, 1);
        }
        return calendar;
    }
}
