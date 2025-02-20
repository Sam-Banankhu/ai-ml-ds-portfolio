package HelperFunctions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.weatherfocus.wf.R;

public class WaitProgressBar {
    Context context;
   public AlertDialog alertDialog;

    public WaitProgressBar(Context context) {
        this.context = context;
    }

    public void showDialog(){
        AlertDialog.Builder adb= new AlertDialog.Builder(context);
        adb.setCancelable(false);
        View progress_view= LayoutInflater.from(context).inflate(R.layout.progress_view, null);
        adb.setView(progress_view);

        alertDialog = adb.create();
        alertDialog.show();
    }

    public void dismissDialog(){
        alertDialog.dismiss();
    }
}
