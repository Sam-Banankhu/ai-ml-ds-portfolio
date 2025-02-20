package HelperFunctions;

public class ToMonthNumberConverter {
    public int toMonthNumber(int month){
        if(month>12){
            return  toMonthNumber(month-12);
        }else{
            return month;
        }
    }
}
