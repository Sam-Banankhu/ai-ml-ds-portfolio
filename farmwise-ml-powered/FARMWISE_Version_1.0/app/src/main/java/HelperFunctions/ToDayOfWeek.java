package HelperFunctions;

public class ToDayOfWeek {
    public int getDayOfWeek(int dayNumber){
        if(dayNumber>7){
            return getDayOfWeek(dayNumber-7);
        }else{
           return dayNumber;
        }
    }
}
