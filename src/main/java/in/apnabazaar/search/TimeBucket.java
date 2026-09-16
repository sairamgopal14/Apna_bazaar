package in.apnabazaar.search;

import java.time.LocalTime;

public enum TimeBucket {
    early_morning, // 00:00-06:00
    morning_6,     // 06:00-06:30
    morning_630,   // 06:30-07:00
    morning_7,     // 07:00-07:30
    morning_730,   // 07:30-08:00
    morning_8,     // 08:00-08:30
    morning_830,   // 08:30-09:00
    morning_9,     // 09:00-09:30
    late_morning,  // 09:30-12:00
    afternoon,     // 12:00-16:00
    evening,       // 16:00-19:00
    night,         // 19:00-22:00
    late_night;    // 22:00-24:00

    public static TimeBucket forTime(LocalTime time) {
        if (time.isBefore(LocalTime.of(6, 0))) return early_morning;
        if (time.isBefore(LocalTime.of(6, 30))) return morning_6;
        if (time.isBefore(LocalTime.of(7, 0))) return morning_630;
        if (time.isBefore(LocalTime.of(7, 30))) return morning_7;
        if (time.isBefore(LocalTime.of(8, 0))) return morning_730;
        if (time.isBefore(LocalTime.of(8, 30))) return morning_8;
        if (time.isBefore(LocalTime.of(9, 0))) return morning_830;
        if (time.isBefore(LocalTime.of(9, 30))) return morning_9;
        if (time.isBefore(LocalTime.of(12, 0))) return late_morning;
        if (time.isBefore(LocalTime.of(16, 0))) return afternoon;
        if (time.isBefore(LocalTime.of(19, 0))) return evening;
        if (time.isBefore(LocalTime.of(22, 0))) return night;
        return late_night;
    }
}
