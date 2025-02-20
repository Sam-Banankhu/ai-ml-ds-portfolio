package Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.weatherfocus.wf.DiseaseDetection;
import com.weatherfocus.wf.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Visual_Diseases#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Visual_Diseases extends Fragment {
    ConstraintLayout tomatoV, onionV, milletV, peanutV;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Visual_Diseases() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Visual_Diseases.
     */
    // TODO: Rename and change types and number of parameters
    public static Visual_Diseases newInstance(String param1, String param2) {
        Visual_Diseases fragment = new Visual_Diseases();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_visual__diseases, container, false);
        initViews(v);

        tomatoV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDiseaseDetection(R.id.tomato);
            }
        });

      /*  peanutV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDiseaseDetection(R.id.peanut);
            }
        });
        milletV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDiseaseDetection(R.id.millet);
            }
        });
        onionV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDiseaseDetection(R.id.onion);
            }
        }); */
        return v;
    }

    private void initViews(View v) {
        tomatoV = v.findViewById(R.id.tomato);
       /* onionV = v.findViewById(R.id.onion);
        milletV = v.findViewById(R.id.millet);
        peanutV = v.findViewById(R.id.peanut);*/
    }

    @SuppressLint("NonConstantResourceId")
    public void openDiseaseDetection(int view_ID) {
        Intent intent = new Intent(requireContext(), DiseaseDetection.class);
      switch (view_ID){
          case R.id.tomato:
              intent.putExtra("crop_name", getResources().getString(R.string.Tomato));
              intent.putExtra("crop_icon", R.drawable.tomato);
              break;
       /*   case R.id.onion:
              intent.putExtra("crop_name", "Onion");
              intent.putExtra("crop_icon", R.drawable.onion);

              break;
          case R.id.peanut:
              intent.putExtra("crop_name", "Peanut");
              intent.putExtra("crop_icon", R.drawable.peanut);
              break;
          case R.id.millet:
              intent.putExtra("crop_name", "Millet");
              intent.putExtra("crop_icon", R.drawable.millet);
              break; */
      }
      startActivity(intent);
    }
}