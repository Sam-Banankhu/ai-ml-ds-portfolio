package ViewHolders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.weatherfocus.wf.R;

import java.util.ArrayList;
import java.util.List;

import DataModels.RecommendedCroppingDataModel;

public class AllRecommendationsViewAdapter extends RecyclerView.Adapter<AllRecommendationsViewAdapter.ViewHolder> {
    public Context context;
    public List<RecommendedCroppingDataModel> allRecommendations;

    public AllRecommendationsViewAdapter(Context context, List<RecommendedCroppingDataModel> allRecommendations) {
        this.context = context;
        this.allRecommendations = allRecommendations;
    }


    @NonNull
    @Override
    public AllRecommendationsViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.all_crops_single_row_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllRecommendationsViewAdapter.ViewHolder holder, int position) {
        Picasso.get().load(allRecommendations.get(position).getPlant_image()).into(holder.crop_image);
        holder.plant_name.setText(allRecommendations.get(position).getPlant_name());
        holder.planting_week.setText(allRecommendations.get(position).getPlanting_week());
        holder.planting_date.setText(allRecommendations.get(position).getPlanting_date());
        holder.harvesting_week.setText(allRecommendations.get(position).getHarvesting_week());
        holder.harvesting_date.setText(allRecommendations.get(position).getHarvesting_date());
    }

    @Override
    public int getItemCount() {
        return allRecommendations.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView crop_image;
        public TextView plant_name, planting_week, planting_date, harvesting_week, harvesting_date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            crop_image = itemView.findViewById(R.id.plant_image);
            plant_name = itemView.findViewById(R.id.plant_name);
            planting_week = itemView.findViewById(R.id.planting_week);
            planting_date = itemView.findViewById(R.id.planting_date);
            harvesting_week = itemView.findViewById(R.id.harvesting_week);
            harvesting_date = itemView.findViewById(R.id.harvesting_date);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterCropList(ArrayList<RecommendedCroppingDataModel> filteredList){
        allRecommendations = filteredList;
        notifyDataSetChanged();
    }
}
