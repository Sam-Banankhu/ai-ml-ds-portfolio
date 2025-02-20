package ViewHolders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.weatherfocus.wf.R;

import java.util.List;

import DataModels.StationDataModel;

public class StationsViewHolder extends RecyclerView.Adapter<StationsViewHolder.ViewHolder> {
    Context context;
    List<StationDataModel> stations;

    public StationsViewHolder(Context context, List<StationDataModel> stations) {
        this.context = context;
        this.stations = stations;
    }

    @NonNull
    @Override
    public StationsViewHolder.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.stations_popup_row,  null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StationsViewHolder.ViewHolder holder, int position) {
      holder.name.setText(stations.get(position).getName());
      holder.latitude.setText(stations.get(position).getLatitude());
      holder.longitude.setText(stations.get(position).getLongitude());
    }

    @Override
    public int getItemCount() {
        return stations.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
    TextView name, latitude, longitude;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.station_name);
            latitude = itemView.findViewById(R.id.latitude);
            longitude = itemView.findViewById(R.id.longitude);
        }
    }
}
