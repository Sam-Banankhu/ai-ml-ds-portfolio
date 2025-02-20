package Fragments;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.weatherfocus.wf.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import DBHelpers.WeatherDataDBHelper;
import DataModels.CropsDataModel;
import DataModels.RecommendedCroppingDataModel;
import HelperFunctions.DayToWeekNumberConverter;
import HelperFunctions.ToDayOfWeek;
import HelperFunctions.WaitProgressBar;
import ViewHolders.AllRecommendationsViewAdapter;
import ViewHolders.CropsAdapter;
import ViewModelClasses.PasseableValues;
import ViewModelClasses.recommendation_parameters;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Visual_Recommendation#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Visual_Recommendation extends Fragment {
    private ArrayList<CropsDataModel> crops;
    private CropsAdapter cropsAdapter;
    private Spinner crops_dropdown, years_dropdown;
    private ImageView crop_image1;
    private WeatherDataDBHelper weatherDataDBHelper;
    private ToDayOfWeek dayOfWeekConverter;
    private DayToWeekNumberConverter dayToWeekNumberConverter;
    private TextView recommended_weekP, recommended_monthP, recommended_dayP, selectedStationName, stations_text, st_name1, selectedYear, approximatedP_date, approximated_harvest_period, harvestable_period_text, approximated_week_h, approximated_week_h2, approximated_month_h, approximated_month_h2, range_week_havest, rainfall_req, min_temp_req, max_temp_req;
    private Button show_stations;
    private String[] allStations;
    private String[] allMonths;
    private recommendation_parameters rpvm;
    private RecyclerView allCropsRecyclerView;
    private WaitProgressBar waitProgressBar;
    SharedPreferences selected_station_prefs;
    private ConstraintLayout singleCropRecommendationViewContainer, allCropsRecommendationViewContainer;
    private final int[] allCropImages = {R.drawable.peanut, R.drawable.millet, R.drawable.maize, R.drawable.rice, R.drawable.tomato, R.drawable.onion, R.drawable.eggplant}; // for all crop predictions
    AllRecommendationsViewAdapter allRecommendationsViewAdapter;
    private SearchView search_cropView;

    private Resources resources;
    String[] allCrops;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Visual_Recommendation() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Visual_Recommendation.
     */
    // TODO: Rename and change types and number of parameters
    public static Visual_Recommendation newInstance(String param1, String param2) {
        Visual_Recommendation fragment = new Visual_Recommendation();
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
        View view = inflater.inflate(R.layout.fragment_visual__recommendation, container, false);
        weatherDataDBHelper = new WeatherDataDBHelper(getContext());
        waitProgressBar = new WaitProgressBar(getContext());
        dayOfWeekConverter = new ToDayOfWeek();
        dayToWeekNumberConverter = new DayToWeekNumberConverter();
        allCrops = getResources().getStringArray(R.array.crops_for_queries);//crops for querying data
        rpvm = new ViewModelProvider(Visual_Recommendation.this).get(recommendation_parameters.class);
        allMonths = getResources().getStringArray(R.array.months_abbrev);
        allStations = getResources().getStringArray(R.array.stations);
        selected_station_prefs = requireContext().getSharedPreferences("SELECTED_STATION", 0);
        resources=getResources();

        int default_St_ID = selected_station_prefs.getInt("St_ID", 0);
       // Toast.makeText(getContext(), default_St_ID+"", Toast.LENGTH_SHORT).show();

        rpvm.St_ID = default_St_ID;
        //rpvm.crop_name = "All crops"; //default
        rpvm.crop_name = allCrops[0]; //default

        initViews(view);
        if(default_St_ID!=0) {
            selectedStationName.setText(allStations[default_St_ID - 1]);
        }
        // st_name1.setSelected(true); //starting marquee
        initCropsSpinner(view);
        crops_dropdown.setSelection(1);
        initYearsSpinner(view);
        if (rpvm.St_ID != 0) {
            if (rpvm.crop_name.equals(resources.getString(R.string.all_crops))) {
                loadAllRecommendationDates();
            } else {
                loadRecommendationDates();
            }
        }
        years_dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                rpvm.year = Integer.parseInt(years_dropdown.getSelectedItem().toString());
                selectedYear.setText("" + rpvm.year);
                if (rpvm.St_ID != 0) {
                    if (rpvm.crop_name.equals(resources.getString(R.string.all_crops))) {
                        loadAllRecommendationDates();
                    } else {
                        loadRecommendationDates();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        show_stations.setOnClickListener(v -> {
            showStationsPopup();
        });
        selectedStationName.setOnClickListener(v -> showStationsPopup());
        stations_text.setOnClickListener(v -> showStationsPopup());
        /* allCropsRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
             @Override
             public void onGlobalLayout() {
                 Toast.makeText(getContext(), "DONE", Toast.LENGTH_SHORT).show();
             }
         });*/

        return view;
    }

    private void filterCrops(String s, List<RecommendedCroppingDataModel> allCrop_recommends) {
        ArrayList<RecommendedCroppingDataModel> filteredCrops =new ArrayList<>();
        for(RecommendedCroppingDataModel crop: allCrop_recommends){
            if(crop.getPlant_name().toLowerCase().contains(s.toLowerCase())){
                filteredCrops.add(crop);
            }
        }
        allRecommendationsViewAdapter.filterCropList(filteredCrops);
    }

    @SuppressLint("SetTextI18n")
    private void loadRecommendationDates() {

        if (allCropsRecommendationViewContainer.getVisibility() == View.VISIBLE && singleCropRecommendationViewContainer.getVisibility() == View.GONE) {
            singleCropRecommendationViewContainer.setVisibility(View.VISIBLE);
            allCropsRecommendationViewContainer.setVisibility(View.GONE);
        }

        List<Integer> recommendation_dates = new ArrayList<>();
        recommendation_dates = weatherDataDBHelper.getRecommendedPeriod(rpvm.year, rpvm.St_ID, rpvm.sowing_rainfall);
        // recommendation_dates.add(1);
        // recommendation_dates.add(5);

        int recommended_week_number = recommendation_dates.get(0); //to be used when querying harvest time
        int recommended_month_number = recommendation_dates.get(1);

        recommended_weekP.setText("" + recommendation_dates.get(0));
        recommended_monthP.setText("" + allMonths[recommendation_dates.get(1) - 1]);

        String approximatedExactDate = weatherDataDBHelper.getExactSowingDate(recommendation_dates.get(0), recommendation_dates.get(1), rpvm.year, rpvm.St_ID);

        //FORMATTING THE DATE
        String[] dateToArray = approximatedExactDate.split("/");
        recommended_dayP.setText("" + dayOfWeekConverter.getDayOfWeek(Integer.parseInt(dateToArray[1]))); //Setting day
        approximatedP_date.setText(dateToArray[1] + "-" + allMonths[Integer.parseInt(dateToArray[0]) - 1] + "-" + dateToArray[2]);


        //HARVESTING TIME
        //getting min_max_days
        int min_crop_days = 0;
        int max_crop_days = 0;
        List<Integer> min_max_days = weatherDataDBHelper.getMinMaxDays(rpvm.crop_name);
        List<Integer> additional_sowing_recommendations = weatherDataDBHelper.getAdditionalSowingRequirements(rpvm.crop_name);


        min_crop_days = min_max_days.get(0);
        max_crop_days = min_max_days.get(1);


        rainfall_req.setText(additional_sowing_recommendations.get(0)+"mm");//rainfall for sowing
        min_temp_req.setText(""+additional_sowing_recommendations.get(1)+"℃");//Min Temp
        max_temp_req.setText(""+additional_sowing_recommendations.get(2)+"℃");//Max Temp

        if (min_crop_days == max_crop_days) {
            //harvestable_period_text.setText("Harvestable after " + min_crop_days + " days");
            harvestable_period_text.setText(resources.getString(R.string.harvestable_period, min_crop_days));
            harvestable_period_text.setActivated(true);
        } else {
            //harvestable_period_text.setText("Harvestable " + min_crop_days + " to " + max_crop_days + " days.");
            harvestable_period_text.setText(resources.getString(R.string.harvestable_period2, min_crop_days, max_crop_days));
            harvestable_period_text.setActivated(true);

        }
        List<String> harvestDates = weatherDataDBHelper.getExpectedHarvestTime(recommended_week_number, recommended_month_number, rpvm.year, min_crop_days, max_crop_days, rpvm.St_ID);
        String[] startHarvestPeriodArr = harvestDates.get(0).split("/");
        String[] endHarvestPeriodArr = harvestDates.get(1).split("/");

        String week_startText = "" + dayToWeekNumberConverter.getWeekNumber(Integer.parseInt(startHarvestPeriodArr[1]));
        approximated_week_h.setText(week_startText);
        String week_endText = "" + dayToWeekNumberConverter.getWeekNumber(Integer.parseInt(endHarvestPeriodArr[1]));
        approximated_week_h2.setText(week_endText);

       String approximated_monthText = "" + allMonths[Integer.parseInt(startHarvestPeriodArr[0]) - 1];
        approximated_month_h.setText(approximated_monthText);
       String approximated_month2Text = "" + allMonths[Integer.parseInt(endHarvestPeriodArr[0]) - 1];
        approximated_month_h2.setText(approximated_month2Text);


       // range_week_havest.setText("Week "+week_startText+" of "+approximated_monthText +" to week "+week_endText+" of "+approximated_month2Text);
        range_week_havest.setText(getResources().getString(R.string.harvestable_period_weeks, week_startText, approximated_monthText, week_endText, approximated_month2Text));



        String approximatedDateText1 = "" + startHarvestPeriodArr[1] + "-" + allMonths[Integer.parseInt(startHarvestPeriodArr[0]) - 1] + "";//+"-"+startHarvestPeriodArr[2]+"]";
        String approximatedDateText2 = "" + endHarvestPeriodArr[1] + "-" + allMonths[Integer.parseInt(endHarvestPeriodArr[0]) - 1] + "";//+"-"+endHarvestPeriodArr[2]+"]";
        //approximated_harvest_period.setText(approximatedDateText1 + " to " + approximatedDateText2);
        approximated_harvest_period.setText(approximatedDateText1 + " " +getResources().getString(R.string.to_text)+" "+ approximatedDateText2);

    }

    @SuppressLint("SetTextI18n")
    private void loadAllRecommendationDates() {
        if (allCropsRecommendationViewContainer.getVisibility() == View.GONE && singleCropRecommendationViewContainer.getVisibility() == View.VISIBLE) {
            singleCropRecommendationViewContainer.setVisibility(View.GONE);
            allCropsRecommendationViewContainer.setVisibility(View.VISIBLE);
        }
        //waitProgressBar.showDialog();
        List<RecommendedCroppingDataModel> allCrop_recommends = new ArrayList<>();
        //int i = 0; //index of crop image in an array
        // for (String crop_name : allCrops) {
        for (int i = 0; i < allCrops.length; i++) {
            //
            int crop_image = allCropImages[i];
            //i++;//advancing

            String plant_name, planting_week, planting_date, harvesting_week, harvesting_date;
            plant_name = allCrops[i];

            //Toast.makeText(getContext(), ""+plant_name, Toast.LENGTH_SHORT).show();
            //
            float sowing_rainfall = weatherDataDBHelper.getSowingRainFall(allCrops[i]);
            ///
            List<Integer> recommendation_dates;
            recommendation_dates = weatherDataDBHelper.getRecommendedPeriod(rpvm.year, rpvm.St_ID, sowing_rainfall);


            int recommended_week_number = recommendation_dates.get(0); //to be used when querying harvest time
            int recommended_month_number = recommendation_dates.get(1);


            String approximatedExactDate = weatherDataDBHelper.getExactSowingDate(recommended_week_number, recommended_month_number, rpvm.year, rpvm.St_ID);

            //FORMATTING THE DATE
            String[] dateToArray = approximatedExactDate.split("/");

            //planting_week = "Day " + dayOfWeekConverter.getDayOfWeek(Integer.parseInt(dateToArray[1])) + " of Week " + recommendation_dates.get(0) + " of " + allMonths[recommendation_dates.get(1) - 1];
            planting_week = resources.getString(R.string.planting_week_text,""+dayOfWeekConverter.getDayOfWeek(Integer.parseInt(dateToArray[1])), ""+recommendation_dates.get(0), allMonths[recommendation_dates.get(1) - 1]);
            //  Toast.makeText(getContext(), ""+planting_week, Toast.LENGTH_SHORT).show();
            planting_date = dateToArray[1] + "-" + allMonths[Integer.parseInt(dateToArray[0]) - 1] + "-" + dateToArray[2];


            //HARVESTING TIME
            //getting min_max_days
            int min_crop_days;
            int max_crop_days;
            List<Integer> min_max_days = weatherDataDBHelper.getMinMaxDays(allCrops[i]);
            min_crop_days = min_max_days.get(0);
            max_crop_days = min_max_days.get(1);
           /* if (min_crop_days == max_crop_days) {
                harvestable_period_text.setText("Harvestable after " + min_crop_days + " days");
                harvestable_period_text.setActivated(true);
            } else {
                harvestable_period_text.setText("Harvestable within " + min_crop_days + " and " + max_crop_days + " days after sowing");
                harvestable_period_text.setActivated(true);

            }*/
            List<String> harvestDates = weatherDataDBHelper.getExpectedHarvestTime(recommended_week_number, recommended_month_number, rpvm.year, min_crop_days, max_crop_days, rpvm.St_ID);
            String[] startHarvestPeriodArr = harvestDates.get(0).split("/");
            String[] endHarvestPeriodArr = harvestDates.get(1).split("/");

            //approximated_week_h.setText("" + dayToWeekNumberConverter.getWeekNumber(Integer.parseInt(startHarvestPeriodArr[1])));
            //approximated_week_h2.setText("" + dayToWeekNumberConverter.getWeekNumber(Integer.parseInt(endHarvestPeriodArr[1])));

           //harvesting_week = "Week " + dayToWeekNumberConverter.getWeekNumber(Integer.parseInt(startHarvestPeriodArr[1])) + " of " + allMonths[Integer.parseInt(startHarvestPeriodArr[0]) - 1] + " to week " + dayToWeekNumberConverter.getWeekNumber(Integer.parseInt(endHarvestPeriodArr[1])) + " of " + allMonths[Integer.parseInt(endHarvestPeriodArr[0]) - 1]+".";
            harvesting_week = getResources().getString(R.string.harvestable_period_weeks,""+dayToWeekNumberConverter.getWeekNumber(Integer.parseInt(startHarvestPeriodArr[1])), allMonths[Integer.parseInt(startHarvestPeriodArr[0]) - 1], ""+dayToWeekNumberConverter.getWeekNumber(Integer.parseInt(endHarvestPeriodArr[1])), allMonths[Integer.parseInt(endHarvestPeriodArr[0]) - 1]);
            //approximated_month_h.setText("" + allMonths[Integer.parseInt(startHarvestPeriodArr[0]) - 1]);
            // approximated_month_h2.setText("" + allMonths[Integer.parseInt(endHarvestPeriodArr[0]) - 1]);

            String approximatedDateText1 = "[" + startHarvestPeriodArr[1] + "-" + allMonths[Integer.parseInt(startHarvestPeriodArr[0]) - 1] + "]";//+"-"+startHarvestPeriodArr[2]+"]";
            String approximatedDateText2 = "[" + endHarvestPeriodArr[1] + "-" + allMonths[Integer.parseInt(endHarvestPeriodArr[0]) - 1] + "]";//+"-"+endHarvestPeriodArr[2]+"]";
            harvesting_date = approximatedDateText1 + " - " + approximatedDateText2;

            RecommendedCroppingDataModel croppingDataModel = new RecommendedCroppingDataModel(crop_image, plant_name, planting_week, planting_date, harvesting_week, harvesting_date);
            allCrop_recommends.add(croppingDataModel);
        }
        allRecommendationsViewAdapter = new AllRecommendationsViewAdapter(getContext(), allCrop_recommends);
        allCropsRecyclerView.setHasFixedSize(true);
        allCropsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        allCropsRecyclerView.setAdapter(allRecommendationsViewAdapter);


        search_cropView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                filterCrops(s, allCrop_recommends);
                return false;
            }
        });
    }

    private void initViews(View view) {
        crops_dropdown = view.findViewById(R.id.crops_dropdown);
        crop_image1 = view.findViewById(R.id.crop_image1);
        recommended_dayP = view.findViewById(R.id.recommended_day);
        recommended_weekP = view.findViewById(R.id.recommended_week);
        recommended_monthP = view.findViewById(R.id.recommended_month);
        show_stations = view.findViewById(R.id.show_stations);
        selectedStationName = view.findViewById(R.id.selectedStationName);
        stations_text = view.findViewById(R.id.stations_text);
        years_dropdown = view.findViewById(R.id.years_dropdown);
        st_name1 = view.findViewById(R.id.st_name1);
        selectedYear = view.findViewById(R.id.selectedYear);
        approximatedP_date = view.findViewById(R.id.approximated_date);
        approximated_harvest_period = view.findViewById(R.id.approximated_harvest_period);
        harvestable_period_text = view.findViewById(R.id.harvestable_period_text);
        approximated_week_h = view.findViewById(R.id.approximated_week_h);
        approximated_week_h2 = view.findViewById(R.id.approximated_week_h2);
        approximated_month_h = view.findViewById(R.id.approximated_month_h);
        approximated_month_h2 = view.findViewById(R.id.approximated_month_h2);
        allCropsRecyclerView = view.findViewById(R.id.allCropsRecyclerView);
        singleCropRecommendationViewContainer = view.findViewById(R.id.singleCropRecommendationViewContainer);
        allCropsRecommendationViewContainer = view.findViewById(R.id.allCropsRecommendationViewContainer);
        search_cropView = view.findViewById(R.id.search_crop);
        range_week_havest = view.findViewById(R.id.week_range_havest);
        rainfall_req= view.findViewById(R.id.rainfall_req);
        min_temp_req= view.findViewById(R.id.min_temp_req);
        max_temp_req= view.findViewById(R.id.max_temp_req);
    }

    private void initCropsSpinner(View view) {
        crops = new ArrayList<>();
        crops.add(new CropsDataModel(resources.getString(R.string.all_crops), R.drawable.crops_symbol));
        crops.add(new CropsDataModel(resources.getString(R.string.Peanut), R.drawable.peanut));
        crops.add(new CropsDataModel(resources.getString(R.string.Millet), R.drawable.millet));
        crops.add(new CropsDataModel(resources.getString(R.string.Maize), R.drawable.maize));
        crops.add(new CropsDataModel(resources.getString(R.string.Rice), R.drawable.rice));
        crops.add(new CropsDataModel(resources.getString(R.string.Tomato), R.drawable.tomato));
        crops.add(new CropsDataModel(resources.getString(R.string.Onion), R.drawable.onion));
        crops.add(new CropsDataModel(resources.getString(R.string.Egg_Plant), R.drawable.eggplant));

        CropsAdapter cropsAdapter = new CropsAdapter(view.getContext(), crops);
        crops_dropdown.setAdapter(cropsAdapter);

        crops_dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                CropsDataModel clickedItem = (CropsDataModel) adapterView.getItemAtPosition(i);
                String clickedCropName = clickedItem.getName();
                int clickedCropImage = clickedItem.getImage();
                if(i==0) {
                    rpvm.crop_name = clickedCropName; //taking the name from the spinner
                }else{
                    rpvm.crop_name = allCrops[i-1]; //Accessing crops for queries
                }
                //Toast.makeText(view.getContext(), clickedCropName, Toast.LENGTH_SHORT).show();

                if (!rpvm.crop_name.equals(resources.getString(R.string.all_crops))) {
                    if (rpvm.St_ID != 0) {
                       // st_name1.setText("Sowing recommendation for " + rpvm.crop_name + ", based on " + allStations[rpvm.St_ID - 1]);
                        st_name1.setText(resources.getString(R.string.recommendation_for_chosen_station_text,rpvm.crop_name,allStations[rpvm.St_ID - 1] ));
                    } else {
                        //st_name1.setText("Sowing time recommendation for " + rpvm.crop_name + " based on [--CHOOSE STATION--]");
                        st_name1.setText(resources.getString(R.string.recommendation_for_not_chosen_station_text, rpvm.crop_name));

                    }
                    crop_image1.setImageResource(clickedCropImage);
                    rpvm.sowing_rainfall = weatherDataDBHelper.getSowingRainFall(clickedCropName);
                }

                if (rpvm.St_ID != 0) {
                    if (rpvm.crop_name.equals(resources.getString(R.string.all_crops))) {
                        loadAllRecommendationDates();
                    } else {
                        loadRecommendationDates();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void showStationsPopup() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());
        alertBuilder.setCancelable(true);

        View stationsView = LayoutInflater.from(getContext()).inflate(R.layout.stations_popup, null);
        RadioButton s1, s2, s3, s4, s5, s6, s7, s8, s9, s10;
        int selectedST_ID;
        List<RadioButton> radioButtons = new ArrayList<>();
        s1 = stationsView.findViewById(R.id.st1);
        s2 = stationsView.findViewById(R.id.st2);
        s3 = stationsView.findViewById(R.id.st3);
        s4 = stationsView.findViewById(R.id.st4);
        s5 = stationsView.findViewById(R.id.st5);
        s6 = stationsView.findViewById(R.id.st6);
        s7 = stationsView.findViewById(R.id.st7);
        s8 = stationsView.findViewById(R.id.st8);
        s9 = stationsView.findViewById(R.id.st9);
        s10 = stationsView.findViewById(R.id.st10);
        radioButtons.add(s1);
        radioButtons.add(s2);
        radioButtons.add(s3);
        radioButtons.add(s4);
        radioButtons.add(s5);
        radioButtons.add(s6);
        radioButtons.add(s7);
        radioButtons.add(s8);
        radioButtons.add(s9);
        radioButtons.add(s10);
        if (rpvm.St_ID > 0) {
            radioButtons.get(rpvm.St_ID - 1).setChecked(true);
        }

        s1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    rpvm.St_ID = 1;
                    for (RadioButton rb : radioButtons) {
                        if (rb == s1) continue;
                        rb.setChecked(false);
                    }
                }
            }
        });
        s2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    rpvm.St_ID = 2;
                    for (RadioButton rb : radioButtons) {
                        if (rb == s2) continue;
                        rb.setChecked(false);
                    }
                }
            }
        });
        s3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    rpvm.St_ID = 3;
                    for (RadioButton rb : radioButtons) {
                        if (rb == s3) continue;
                        rb.setChecked(false);
                    }
                }
            }
        });
        s4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    rpvm.St_ID = 4;
                    for (RadioButton rb : radioButtons) {
                        if (rb == s4) continue;
                        rb.setChecked(false);
                    }
                }
            }
        });
        s5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    rpvm.St_ID = 5;
                    for (RadioButton rb : radioButtons) {
                        if (rb == s5) continue;
                        rb.setChecked(false);
                    }
                }
            }
        });
        s6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    rpvm.St_ID = 6;
                    for (RadioButton rb : radioButtons) {
                        if (rb == s6) continue;
                        rb.setChecked(false);
                    }
                }
            }
        });
        s7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    rpvm.St_ID = 7;
                    for (RadioButton rb : radioButtons) {
                        if (rb == s7) continue;
                        rb.setChecked(false);
                    }
                }
            }
        });
        s8.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    rpvm.St_ID = 8;
                    for (RadioButton rb : radioButtons) {
                        if (rb == s8) continue;
                        rb.setChecked(false);
                    }
                }
            }
        });
        s9.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    rpvm.St_ID = 9;
                    for (RadioButton rb : radioButtons) {
                        if (rb == s9) continue;
                        rb.setChecked(false);
                    }
                }
            }
        });
        s10.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    rpvm.St_ID = 10;
                    for (RadioButton rb : radioButtons) {
                        if (rb == s10) continue;
                        rb.setChecked(false);
                    }
                }
            }
        });
        Button cancelBtn = stationsView.findViewById(R.id.cancelBtn);
        Button okayBtn = stationsView.findViewById(R.id.okBtn);

        alertBuilder.setView(stationsView);
        AlertDialog alertDialog = alertBuilder.create();
        cancelBtn.setOnClickListener(vw -> {
            rpvm.St_ID = 0;
            alertDialog.dismiss();
        });
        okayBtn.setOnClickListener(vw -> {
            if (rpvm.St_ID != 0) {
                setStationName();
            } else {
                //selectedStationName.setText("[select station]");
                selectedStationName.setText(resources.getString(R.string.select_station_info));
            }


            alertDialog.dismiss();

            if (rpvm.crop_name.equals(resources.getString(R.string.all_crops))) {
                loadAllRecommendationDates();
            } else {
                loadRecommendationDates();
            }
        });
        alertDialog.show();
    }

    @SuppressLint("SetTextI18n")
    public void setStationName() {
        selectedStationName.setText(allStations[rpvm.St_ID - 1]);
        //st_name1.setText("Sowing recommendation for " + rpvm.crop_name + " based on " + allStations[rpvm.St_ID - 1]);
        st_name1.setText(resources.getString(R.string.recommendation_for_chosen_station_text,rpvm.crop_name,allStations[rpvm.St_ID - 1] ));

    }

    public void initYearsSpinner(View v) {
        List<Integer> years = new ArrayList<>();
        /*for (int y = 2023; y <= 2030; y++) {
            years.add(y);
        }*/
        years.add(2023);
        ArrayAdapter adapter = new ArrayAdapter(v.getContext(), android.R.layout.simple_spinner_dropdown_item, years);
        years_dropdown.setAdapter(adapter);

    }

}