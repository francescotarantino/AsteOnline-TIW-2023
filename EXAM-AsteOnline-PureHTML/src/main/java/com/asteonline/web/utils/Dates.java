package com.asteonline.web.utils;

import java.time.Duration;
import java.util.Date;

public abstract class Dates {
    /**
     * This method returns an italian string (in days and hours) representing the time between two dates.
     * @param date1 the first date
     * @param date2 the second date
     * @return the string to be displayed to the user
     */
    public static String timeBetween(Date date1, Date date2){
        Duration duration = Duration.between(date1.toInstant(), date2.toInstant());

        if(duration.isNegative()){
            return "Asta scaduta!";
        }

        if (duration.toHours() == 0) {
            return "Meno di 1 ora";
        }

        String hours;
        if(duration.toHoursPart() == 1){
            hours = "1 ora";
        } else {
            hours = String.format("%d ore", duration.toHoursPart());
        }

        if(duration.toDays() == 0){
            return hours;
        }

        String days;
        if(duration.toDays() == 1){
            days = "1 giorno";
        } else {
            days = String.format("%d giorni", duration.toDays());
        }

        if(duration.toHoursPart() == 0){
            return days;
        } else {
            return days + " e " + hours;
        }
    }
}
