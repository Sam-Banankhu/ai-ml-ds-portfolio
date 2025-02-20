package HelperFunctions;

import android.content.Context;

import com.weatherfocus.wf.R;

import java.util.Objects;

public class PeriodTextToNumberConverter {

    public int monthAbbrevToNumber(String[] months_abbrevs, String month_abbrev){
        int n = 0;
        for(int i = 0; i<months_abbrevs.length; i++){
            if(Objects.equals(months_abbrevs[i], month_abbrev)){
                n = (i+1);
                break;
            }
        }
        return n;
    }
}
