package ViewModelClasses;

import androidx.lifecycle.ViewModel;

public class SelectedParameters extends ViewModel {
    public int St_ID, startYear, startMonth, endYear, endMonth, startDay, endDay, startWeek, endWeek;
    public boolean dateIsSet = false;
    public int period_interval;
}
