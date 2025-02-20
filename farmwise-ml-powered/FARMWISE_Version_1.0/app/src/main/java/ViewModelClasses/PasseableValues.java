package ViewModelClasses;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PasseableValues extends ViewModel {
    private final MutableLiveData<CharSequence> name= new MutableLiveData<>();
    public void setName(CharSequence name){
        this.name.setValue(name);
    }

    public LiveData<CharSequence> getName(){
        return this.name;
    }
}
