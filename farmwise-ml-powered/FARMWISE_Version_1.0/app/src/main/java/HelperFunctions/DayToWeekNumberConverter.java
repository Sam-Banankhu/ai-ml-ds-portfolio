package HelperFunctions;

public class DayToWeekNumberConverter {
    public int getWeekNumber(int dayOfTheWeek) {
        if (dayOfTheWeek > 0 && dayOfTheWeek <= 7) {
            return 1;
        } else if (dayOfTheWeek > 7 && dayOfTheWeek <= 14) {
            return 2;
        } else if (dayOfTheWeek > 14 && dayOfTheWeek <= 21) {
            return 3;
        } else if (dayOfTheWeek > 21 && dayOfTheWeek <= 28) {
            return 4;
        } else {
            return 5;
        }
    }
}
