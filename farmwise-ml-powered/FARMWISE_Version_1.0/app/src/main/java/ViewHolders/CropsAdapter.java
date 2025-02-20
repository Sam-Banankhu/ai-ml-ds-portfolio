package ViewHolders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.weatherfocus.wf.R;

import java.util.ArrayList;
import java.util.List;

import DataModels.CropsDataModel;

public class CropsAdapter extends ArrayAdapter<CropsDataModel> {
    public CropsAdapter(@NonNull Context context, ArrayList<CropsDataModel> cropsList) {
        super(context, 0, cropsList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @SuppressLint("SetTextI18n")
    private View initView(int position, View convertView, ViewGroup parent){
        if(convertView==null){
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.crops_list_dropdown_view, parent, false);
        }
        ImageView cropImage= convertView.findViewById(R.id.cropImage);
        TextView cropName= convertView.findViewById(R.id.cropName);

        CropsDataModel currentCrop= getItem(position);
        if(currentCrop!=null) {
            cropImage.setImageResource(currentCrop.getImage());
            cropName.setText("- "+currentCrop.getName());
        }
        return convertView;
    }
}
