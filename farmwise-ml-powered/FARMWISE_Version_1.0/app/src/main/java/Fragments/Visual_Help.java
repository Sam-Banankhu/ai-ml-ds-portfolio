package Fragments;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.weatherfocus.wf.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Visual_Help#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Visual_Help extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CardView card1, card2;
    public LinearLayout topic_1_content, topic_2_content;

    public Visual_Help() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Visual_Help.
     */
    // TODO: Rename and change types and number of parameters
    public static Visual_Help newInstance(String param1, String param2) {
        Visual_Help fragment = new Visual_Help();
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
        View view = inflater.inflate(R.layout.fragment_visual__help, container, false);

        initViews(view);
        card1.setOnClickListener(this);
        card2.setOnClickListener(this);

        topic_1_content.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        topic_2_content.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        return view;
    }

    public void initViews(View view) {
        card1 = view.findViewById(R.id.card1);
        card2 = view.findViewById(R.id.card2);

        topic_1_content = view.findViewById(R.id.topic_1_content);
        topic_2_content = view.findViewById(R.id.topic_2_content);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.card1:
                TransitionManager.beginDelayedTransition(topic_1_content, new AutoTransition());
                topic_1_content.setVisibility(topic_1_content.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                break;
            case R.id.card2:
                TransitionManager.beginDelayedTransition(topic_2_content, new AutoTransition());
                topic_2_content.setVisibility(topic_2_content.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                break;
        }
    }
}