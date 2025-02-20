package Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.snackbar.Snackbar;
import com.weatherfocus.wf.R;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import DBHelpers.WeatherDataDBHelper;
import HelperFunctions.PeriodTextToNumberConverter;
import HelperFunctions.ToMonthNumberConverter;
import HelperFunctions.graphMarkerView;
import HelperFunctions.WaitProgressBar;
import ViewModelClasses.PasseableValues;
import ViewModelClasses.SelectedParameters;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Visual_Home#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Visual_Home extends Fragment {

    private BarChart rain_barChart;
    private LineChart temp_lineGraph, humidity_lineGraph;
    private Button show_stations;
    private AppCompatSpinner periodSpinner;
    private ArrayAdapter<CharSequence> periodAdaptor;
    private EditText date_range;
    private Resources resources;
    private TextView date1, date2, date3, stations_text;
    private LinearLayout station_list_container;
    private TextView miniTemp, maxiTemp, miniRain, maxiRain, miniHum, maxiHum, selectedStationName;
    private SelectedParameters sp_vm;
    private TextView ST_name1, ST_name2, ST_name3;
    private ImageView add_to_default;
    WeatherDataDBHelper weatherDataDBHelper;
    PeriodTextToNumberConverter ptn;
    WaitProgressBar waitProgressBar;
    ToMonthNumberConverter tmnc;
    String[] allMonths;
    private ScrollView scrollViewContainer;
    private SharedPreferences stationSharedPreferences;
    private SharedPreferences.Editor stationVisualization_default_editor;
    private TextView period_l1, period_l2, period_l3;

    public Visual_Home() {
        // Required empty public constructor
    }


    public static Visual_Home newInstance() {
        Visual_Home fragment = new Visual_Home();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_visual__home, container, false);

        stationSharedPreferences = getActivity().getSharedPreferences("DEFAULT_VISUALIZATION_DETAILS", Context.MODE_PRIVATE);
        stationVisualization_default_editor = stationSharedPreferences.edit();
        resources=getResources();
        weatherDataDBHelper = new WeatherDataDBHelper(getContext());
        ptn = new PeriodTextToNumberConverter();
        allMonths = getResources().getStringArray(R.array.months_abbrev);
        waitProgressBar = new WaitProgressBar(getContext());
        tmnc = new ToMonthNumberConverter();

        initializeViews(view);

        sp_vm = new ViewModelProvider(getActivity()).get(SelectedParameters.class);
        //sp_vm.St_ID = 3;
        // sp_vm.period_interval = "Monthly";
              /*

                stationVisualization_default_editor.putInt("St_ID", St_ID);
                stationVisualization_default_editor.putString("periodInterval", p_interval);
                stationVisualization_default_editor.putInt("startMonth", startMonth);
                stationVisualization_default_editor.putInt("endMonth", endMonth);
                stationVisualization_default_editor.putInt("startYear", startYear);
                stationVisualization_default_editor.putInt("endYear", endYear);
                stationVisualization_default_editor.putInt("startDay", sp_vm.startDay);
                stationVisualization_default_editor.putInt("endDay", sp_vm.endDay);
                stationVisualization_default_editor.putBoolean("defaultIsSet", true);
         */
        initializePeriodSpinner();
        //LOAD STATS BASED ON DEFAULT PREFS
        if (stationSharedPreferences.getBoolean("defaultIsSet", false)) {
            loadDefaultStationData();
        }

        rain_barChart.animateY(1000);

        periodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
               // sp_vm.period_interval = getResources().getStringArray(R.array.period)[i];
                sp_vm.period_interval = getResources().getIntArray(R.array.period_for_selection)[i];
                //date_range.setText("[Date] - [Range]");
                //loadMinMaxVals();
                setPeriodAxisTitles();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                 //TODO
            }
        });

        show_stations.setOnClickListener(v -> {
            showStationsPopup();
        });
        stations_text.setOnClickListener(v -> {
            showStationsPopup();
        });
        selectedStationName.setOnClickListener(v -> {
            showStationsPopup();
        });

        date_range.setOnClickListener(v -> {
            showDateRangeDialog();
            //Toast.makeText(getContext(), "DatePicker", Toast.LENGTH_SHORT).show();
        });
        add_to_default.setOnClickListener(v -> addToDefaultDialog());


        // loadMaxiLineChart(40, 90);
        temp_lineGraph.animateX(1000);

        return view;
    }


    private void initializePeriodSpinner() {
        periodAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.period));
        periodSpinner.setAdapter(periodAdaptor);
    }

    private void initializeViews(@NonNull View view) {
        rain_barChart = view.findViewById(R.id.rainGraph);
        show_stations = view.findViewById(R.id.show_stations);
        periodSpinner = view.findViewById(R.id.crops_dropdown);
        date_range = view.findViewById(R.id.date_range);
        temp_lineGraph = view.findViewById(R.id.temperatureGraph);
        humidity_lineGraph = view.findViewById(R.id.humidityGraph);

        miniTemp = view.findViewById(R.id.miniTemp);
        maxiTemp = view.findViewById(R.id.maxiTemp);

        miniRain = view.findViewById(R.id.miniRain);
        maxiRain = view.findViewById(R.id.maxiRain);

        miniHum = view.findViewById(R.id.miniHum);
        maxiHum = view.findViewById(R.id.maxiHum);

        date1 = view.findViewById(R.id.date1);
        date2 = view.findViewById(R.id.date2);
        date3 = view.findViewById(R.id.date3);

        selectedStationName = view.findViewById(R.id.selectedStationName);
        ST_name1 = view.findViewById(R.id.ST_name1);
        ST_name2 = view.findViewById(R.id.ST_name2);
        ST_name3 = view.findViewById(R.id.ST_name3);
        add_to_default = view.findViewById(R.id.add_default);
        scrollViewContainer = view.findViewById(R.id.scroll_v_container);
        stations_text = view.findViewById(R.id.stations_text);
        period_l1 = view.findViewById(R.id.period1);
        period_l2 = view.findViewById(R.id.period2);
        period_l3 = view.findViewById(R.id.period3);
    }

    @SuppressLint("SetTextI18n")
    private void showDateRangeDialog() {
        int chosen_period_interval = sp_vm.period_interval;
        if (chosen_period_interval == 3) {
            annuallyDateRangePicker();
        } else if (chosen_period_interval == 2) {
            monthlyDateRangePicker();
        } else if (chosen_period_interval == 1) {
            weeklyDateRangePicker();
        } else {
            dailyDateRangePicker();
        }

    }

    @SuppressLint("SetTextI18n")
    public void loadMinMaxVals() {

        // waitProgressBar.showDialog();
        // Toast.makeText(getContext(), sp_vm.period_interval, Toast.LENGTH_SHORT).show();
        if (sp_vm.period_interval == 2) {
            //Toast.makeText(getContext(), "called", Toast.LENGTH_SHORT).show();
            List<Float> stats = weatherDataDBHelper.loadMinMaxStats(sp_vm.startMonth, sp_vm.startYear, sp_vm.endYear, sp_vm.endMonth, sp_vm.St_ID);
            miniTemp.setText("" + stats.get(0));
            maxiTemp.setText("" + stats.get(1));
            miniRain.setText("" + stats.get(2));
            maxiRain.setText("" + stats.get(3));
            miniHum.setText("" + stats.get(4));
            maxiHum.setText("" + stats.get(5));
        }

        List<List<Float>> ChartValues = new ArrayList<>();
        if (sp_vm.period_interval == 3) {//annually
            ChartValues = weatherDataDBHelper.loadAnnuallyGraphs(sp_vm.startYear, sp_vm.endYear, sp_vm.St_ID);
        } else if (sp_vm.period_interval == 2) {//monthly
            ChartValues = weatherDataDBHelper.loadMonthlyGraphs(sp_vm.startMonth, sp_vm.startYear, sp_vm.endYear, sp_vm.endMonth, sp_vm.St_ID);
        } else if (sp_vm.period_interval == 1) {//weekly
            ChartValues = weatherDataDBHelper.loadWeeklyGraphs(sp_vm.startWeek, sp_vm.startMonth, sp_vm.startYear, sp_vm.endWeek, sp_vm.endMonth, sp_vm.endYear, sp_vm.St_ID);
        } else {//daily
            ChartValues = weatherDataDBHelper.loadDailyGraphs(sp_vm.startDay, sp_vm.startMonth, sp_vm.startYear, sp_vm.endDay, sp_vm.endMonth, sp_vm.endYear, sp_vm.St_ID);
        }
        // List<List<Float>> lineChartValues = weatherDataDBHelper.loadMonthlyLineGraph(sp_vm.startMonth, sp_vm.startYear, sp_vm.endYear, sp_vm.endMonth, sp_vm.St_ID);

        //ORDER: miniTempAvg, maxiTempAvg, tempAvg, humidityAvg
        List<Float> miniTempAvg = ChartValues.get(0);
        List<Float> maxiTempAvg = ChartValues.get(1);
        List<Float> tempAvg = ChartValues.get(2);
        List<Float> humidityAvg = ChartValues.get(3);
        List<Float> rainAccumulative = ChartValues.get(4);
        List<Float> x_values = ChartValues.get(5);//months
        List<Float> x_values_years = ChartValues.get(6);//years
        List<Float> x_days = new ArrayList<>();//days
        List<Float> x_weeks = new ArrayList<>();//weeks

        if (ChartValues.size() > 7) {
            if (sp_vm.period_interval == 0) {
                x_days = ChartValues.get(7);
            } else if (sp_vm.period_interval == 1) {
                x_weeks = ChartValues.get(7);
            }
        }


        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.UP);
        StringBuilder stringBuilder = new StringBuilder();

        // Toast.makeText(getContext(), "months:"+x_values.size() + " :years: " + x_values_years.size() + " :days: " + x_days.size(), Toast.LENGTH_SHORT).show();
        /*for (int i = 0; i < miniTempAvg.size(); i++) {
            stringBuilder.append(" ," + (int) Math.round(x_values.get(i)));
            // stringBuilder.append(" ," + (int) Math.round(x_values_years.get(i)));

        }*/
        // Toast.makeText(getContext(), "" + stringBuilder, Toast.LENGTH_LONG).show();


       /* for(String l:xLabels){
            Toast.makeText(getContext(), l, Toast.LENGTH_SHORT).show();
        }*/

        ArrayList<Entry> miniTempEnt = new ArrayList<>();
        for (int i = 0; i < miniTempAvg.size(); i++) {
            if (Float.parseFloat(df.format(miniTempAvg.get(i))) == 0.0f)
                continue; //skipping zero values using temp since temp is always non-zer0
            miniTempEnt.add(new Entry(i, Float.parseFloat(df.format(miniTempAvg.get(i)))));
        }

        ArrayList<Entry> maxiTempEnt = new ArrayList<>();
        for (int i = 0; i < maxiTempAvg.size(); i++) {
            if (Float.parseFloat(df.format(miniTempAvg.get(i))) == 0.0f) continue;
            maxiTempEnt.add(new Entry(i, Float.parseFloat(df.format(maxiTempAvg.get(i)))));
        }

        ArrayList<Entry> avgTempEnt = new ArrayList<>();
        for (int i = 0; i < tempAvg.size(); i++) {
            if (Float.parseFloat(df.format(miniTempAvg.get(i))) == 0.0f)
                continue; //skipping zero values
            avgTempEnt.add(new Entry(i, Float.parseFloat(df.format(tempAvg.get(i)))));
        }
        ArrayList<Entry> humidityEnt = new ArrayList<>();
        for (int i = 0; i < humidityAvg.size(); i++) {
            if (Float.parseFloat(df.format(miniTempAvg.get(i))) == 0.0f)
                continue; //skipping zero values
            humidityEnt.add(new Entry(i, Float.parseFloat(df.format(humidityAvg.get(i)))));
        }
        //RAIN_BAR_CHART VALUES
        ArrayList<BarEntry> barEntries = new ArrayList<>();

        for (int i = 0; i < x_values.size(); i++) {
            if (Float.parseFloat(df.format(miniTempAvg.get(i))) == 0.0f)
                continue; //skipping zero values
            barEntries.add(new BarEntry(i, Float.parseFloat(df.format(rainAccumulative.get((i))))));
        }


        List<String> xLabels = new ArrayList<>();

        if (sp_vm.period_interval == 2) {
            for (int i = 0; i < x_values.size(); i++) {
                if (sp_vm.startYear != sp_vm.endYear) {
                    xLabels.add(getResources().getStringArray(R.array.months_abbrev)[tmnc.toMonthNumber((int) Math.round(x_values.get(i))) - 1] + "-" + (int) Math.round(x_values_years.get(i)));
                } else {
                    xLabels.add(getResources().getStringArray(R.array.months_abbrev)[tmnc.toMonthNumber((int) Math.round(x_values.get(i))) - 1]);
                }
            }
        } else if (sp_vm.period_interval == 3) {
            for (int i = 0; i < x_values.size(); i++) {
                xLabels.add("" + Math.round(x_values_years.get(i)));
            }
        } else if (sp_vm.period_interval == 0) {//DAILY CAPTIONS
            for (int i = 0; i < x_values.size(); i++) {
                if (sp_vm.startYear != sp_vm.endYear) {//d-m-y
                    xLabels.add((int) Math.round(x_days.get(i)) + "-" + getResources().getStringArray(R.array.months_abbrev)[tmnc.toMonthNumber((int) Math.round(x_values.get(i))) - 1] + "-" + (int) Math.round(x_values_years.get(i)));
                } else {
                    if (sp_vm.startMonth != sp_vm.endMonth) {//d-m
                        xLabels.add((int) Math.round(x_days.get(i)) + "-" + getResources().getStringArray(R.array.months_abbrev)[tmnc.toMonthNumber((int) Math.round(x_values.get(i))) - 1]);
                    } else {//d
                        xLabels.add((int) Math.round(x_days.get(i)) + "");
                    }
                }
            }
        } else if (sp_vm.period_interval == 1) {//WEEKLY CAPTIONS
            for (int i = 0; i < x_values.size(); i++) {
                if (Float.parseFloat(df.format(miniTempAvg.get(i))) == 0.0f)
                    continue; //skipping zero values using temperature
                if (sp_vm.startYear != sp_vm.endYear) {//w-m-y
                    xLabels.add(resources.getString(R.string.week_first_letter) + (int) Math.round(x_weeks.get(i)) + "-" + getResources().getStringArray(R.array.months_abbrev)[tmnc.toMonthNumber((int) Math.round(x_values.get(i))) - 1] + "-" + (int) Math.round(x_values_years.get(i)));
                } else {
                    if (sp_vm.startMonth != sp_vm.endMonth) {//w-m
                        xLabels.add(resources.getString(R.string.week_first_letter)  + (int) Math.round(x_weeks.get(i)) + "-" + getResources().getStringArray(R.array.months_abbrev)[tmnc.toMonthNumber((int) Math.round(x_values.get(i))) - 1]);
                    } else {//w
                        xLabels.add(resources.getString(R.string.week_first_letter)  + (int) Math.round(x_weeks.get(i)) + "");
                    }
                }
            }
        }

       /* if (sp_vm.period_interval.equals("Monthly")) {
            for (int i = 0; i < rainAccumulative.size(); i++) {
                barEntries.add(new BarEntry(i, Float.parseFloat(df.format(rainAccumulative.get(i)))));
            }
        } else if (sp_vm.period_interval.equals("Annually")) {
            for (int i = 0; i < x_values.size(); i++) {
                barEntries.add(new BarEntry(i, Float.parseFloat(df.format(rainAccumulative.get((i))))));
            }
        }else if (sp_vm.period_interval.equals("Weekly")) {
            for (int i = 0; i < x_values.size(); i++) {
                barEntries.add(new BarEntry(i, Float.parseFloat(df.format(rainAccumulative.get((i))))));
            }
        }*/
        //end()
        //Labels


        /*for (int i= sp_vm.startMonth; i <sp_vm.endMonth; i++) {
            if (sp_vm.period_interval.equals("Monthly")) {
                ToMonthNumberConverter tmnc = new ToMonthNumberConverter();
                if (sp_vm.startYear != sp_vm.endYear) {
                    xLabels.add("" + getResources().getStringArray(R.array.months_abbrev)[tmnc.toMonthNumber(i-1)] + "-" + x_values_years.get(i));
                } else {
                    xLabels.add("" + getResources().getStringArray(R.array.months_abbrev)[tmnc.toMonthNumber(i-1)]);
                }
            }else if(sp_vm.period_interval.equals("Annually")){
                ToMonthNumberConverter tmnc = new ToMonthNumberConverter();
                xLabels.add("" + getResources().getStringArray(R.array.months_abbrev)[tmnc.toMonthNumber(i)]);
            }
        }*/

        graphMarkerView gmv = new graphMarkerView(getContext(), R.layout.graph_marker_view);
        ArrayList<ILineDataSet> tData = new ArrayList<>();

        LineDataSet s1, s2, s3, s4;
        s1 = new LineDataSet(miniTempEnt, resources.getString(R.string.minimum_average_text) );
        //s1.setColor(Color.parseColor("#ffff00"));
        s1.setColor(getResources().getColor(R.color.weak_yellow));
        s1.setLineWidth(1.5f);
        s1.setFillAlpha(15);
        s1.setFillColor(getResources().getColor(R.color.weak_yellow));
        s1.setDrawFilled(true);
        s2 = new LineDataSet(maxiTempEnt, resources.getString(R.string.maximum_average_text) );
        s2.setLineWidth(1.5f);
        s2.setColor(getResources().getColor(R.color.purple_700));
        s2.setFillAlpha(15);
        s2.setFillColor(getResources().getColor(R.color.purple_700));
        s2.setDrawFilled(true);
        s3 = new LineDataSet(avgTempEnt, resources.getString(R.string.average_text) );
        s3.setColor(getResources().getColor(R.color.teal_700));
        s3.setLineWidth(1.5f);
        s3.setFillAlpha(15);
        s3.setFillColor(getResources().getColor(R.color.teal_700));
        s3.setDrawFilled(true);

        tData.add(s1);
        tData.add(s2);
        tData.add(s3);
        //XAxis xAxisTemp= temp_lineGraph.getXAxis();
        //\xAxisTemp.setValueFormatter(new IndexAxisValueFormatter(xLabels));

        LineData tempData = new LineData(tData);
        tempData.setDrawValues(false);
        temp_lineGraph.setData(tempData);

        Legend l = temp_lineGraph.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        temp_lineGraph.invalidate();
        temp_lineGraph.animateY(1000);
        XAxis tXaxis = temp_lineGraph.getXAxis();
        if (xLabels.size() > 0) {
            tXaxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        }
        tXaxis.setGranularity(1f);
        tXaxis.setGranularityEnabled(true);
        temp_lineGraph.getData().setDrawValues(false);
        temp_lineGraph.getDescription().setText("");
        temp_lineGraph.setExtraBottomOffset(15f);
        tXaxis.setLabelCount(humidityEnt.size());
        tXaxis.setLabelRotationAngle(270);
        tXaxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        temp_lineGraph.setMarker(gmv);

        //HUMIDITY GRAPH
        ArrayList<ILineDataSet> hData = new ArrayList<>();
        s4 = new LineDataSet(humidityEnt, resources.getString(R.string.average_humidity_text) );
        s4.setColor(getResources().getColor(R.color.humidity_color));
        s4.setLineWidth(1.5f);
        s4.setFillAlpha(15);
        s4.setFillColor(getResources().getColor(R.color.humidity_color));
        s4.setDrawFilled(true);
        hData.add(s4);
        LineData humidityData = new LineData(hData);
        humidityData.setDrawValues(false);
        XAxis hXaxis = humidity_lineGraph.getXAxis();
        if (xLabels.size() > 0) {
            hXaxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        }
        hXaxis.setGranularity(1f);
        hXaxis.setGranularityEnabled(true);
        humidity_lineGraph.setData(humidityData);
        humidity_lineGraph.invalidate();
        humidity_lineGraph.animateY(1000);
        humidity_lineGraph.getDescription().setText("");
        humidity_lineGraph.setExtraBottomOffset(40f);
        humidity_lineGraph.setMarker(gmv);
        hXaxis.setLabelCount(humidityEnt.size());
        hXaxis.setLabelRotationAngle(270);
        hXaxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        Legend l2 = humidity_lineGraph.getLegend();
        l2.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l2.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l2.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l2.setDrawInside(false);

        //RAIN_GRAPH

        BarDataSet barDataSet = new BarDataSet(barEntries, resources.getString(R.string.rainfall) );
        barDataSet.setColor(getResources().getColor(R.color.rain_graph_color));

        BarData rainBarData = new BarData(barDataSet);
        rain_barChart.setFitBars(true);
        rain_barChart.setExtraBottomOffset(40f);
        rain_barChart.setData(rainBarData);
        rain_barChart.getDescription().setText("");
        rain_barChart.getLegend().setEnabled(false);
        rain_barChart.setDrawGridBackground(true);
        //rain_barChart.setMarker(gmv);


        XAxis xAxis = rain_barChart.getXAxis();
        if (xLabels.size() > 0) {
            xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        }
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(xLabels.size());
        xAxis.setLabelRotationAngle(270);


        rain_barChart.animateY(1000);
        rain_barChart.invalidate();

        //waitProgressBar.dismissDialog();

    }

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
        if (sp_vm.St_ID > 0) {
            radioButtons.get(sp_vm.St_ID - 1).setChecked(true);
        }

        s1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    sp_vm.St_ID = 1;
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
                    sp_vm.St_ID = 2;
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
                    sp_vm.St_ID = 3;
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
                    sp_vm.St_ID = 4;
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
                    sp_vm.St_ID = 5;
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
                    sp_vm.St_ID = 6;
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
                    sp_vm.St_ID = 7;
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
                    sp_vm.St_ID = 8;
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
                    sp_vm.St_ID = 9;
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
                    sp_vm.St_ID = 10;
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
            sp_vm.St_ID = 0;
            alertDialog.dismiss();
        });
        okayBtn.setOnClickListener(vw -> {
            if (sp_vm.St_ID != 0) {
                setStationName();
            } else {
                selectedStationName.setText(resources.getString(R.string.select_station_info) );
            }
            if (sp_vm.dateIsSet) {
                loadMinMaxVals();
            }

            alertDialog.dismiss();
        });
        alertDialog.show();
    }

    private void monthlyDateRangePicker() {
        AlertDialog.Builder date_dialog = new AlertDialog.Builder(getContext());
        date_dialog.setCancelable(false);
        View date_layout = LayoutInflater.from(getContext()).inflate(R.layout.custom_monthly_date_picker, null);
        AutoCompleteTextView yr_dropdown = date_layout.findViewById(R.id.year_dropdown);
        AutoCompleteTextView yr_dropdown2 = date_layout.findViewById(R.id.year_dropdown2);
        AutoCompleteTextView month_dropdown = date_layout.findViewById(R.id.month_dropdown);
        AutoCompleteTextView month_dropdown2 = date_layout.findViewById(R.id.month_dropdown2);
        Button okayBtn, cancelBtn;
        okayBtn = date_layout.findViewById(R.id.okBtn);
        cancelBtn = date_layout.findViewById(R.id.cancelBtn);
        ProgressBar queryDataProg = date_layout.findViewById(R.id.queryDataProg);
        //initializing yr dropdown
        ArrayList<Integer> allYears = new ArrayList<>();
        /*for (int y = 2023; y <= 2030; y++) {
            allYears.add(y);
        }*/
        allYears.add(2023);
        ArrayAdapter<Integer> y_adapter = new ArrayAdapter<>(getContext(), R.layout.exposed_dropdown_view, allYears);
        yr_dropdown.setAdapter(y_adapter);
        yr_dropdown2.setAdapter(y_adapter);
        yr_dropdown.setText(resources.getString(R.string._2023) , false);//selected by default
        yr_dropdown2.setText(resources.getString(R.string._2023), false);
        /// month dropdown
        String[] allMonths = getResources().getStringArray(R.array.months_abbrev);
        ArrayAdapter<CharSequence> m_adapter = new ArrayAdapter<>(getContext(), R.layout.exposed_dropdown_view, allMonths);
        month_dropdown.setAdapter(m_adapter);
        month_dropdown2.setAdapter(m_adapter);

        date_dialog.setView(date_layout);
        AlertDialog dialog = date_dialog.create();

        cancelBtn.setOnClickListener(v -> {
            dialog.dismiss();
        });

        okayBtn.setOnClickListener(v -> {
            if (String.valueOf(yr_dropdown.getText()).equals("") || String.valueOf(yr_dropdown2.getText()).equals("") || String.valueOf(month_dropdown.getText()).equals("") || String.valueOf(month_dropdown2.getText()).equals("")) {
                Toast.makeText(getContext(), resources.getString(R.string.select_all_fields_info), Toast.LENGTH_SHORT).show();
            } else {
                if (Integer.parseInt(String.valueOf(yr_dropdown2.getText())) < Integer.parseInt(String.valueOf(yr_dropdown.getText()))) {
                    Toast.makeText(getContext(), resources.getString(R.string.validation_year_info), Toast.LENGTH_SHORT).show();
                    return;
                }
                PeriodTextToNumberConverter ptn = new PeriodTextToNumberConverter();
                if ((Integer.parseInt(String.valueOf(yr_dropdown2.getText())) == Integer.parseInt(String.valueOf(yr_dropdown.getText()))) && (ptn.monthAbbrevToNumber(allMonths, String.valueOf(month_dropdown2.getText())) < ptn.monthAbbrevToNumber(allMonths, String.valueOf(month_dropdown.getText())))) {
                    Toast.makeText(getContext(), resources.getString(R.string.validation_month_info), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Toast.makeText(getContext(),""+ ptn.monthAbbrevToNumber(allMonths, String.valueOf(month_dropdown.getText())), Toast.LENGTH_SHORT).show();

                String startDateFull = "[ " + month_dropdown.getText() + " / " + yr_dropdown.getText() + " ]";
                String endDateFull = "[ " + month_dropdown2.getText() + " / " + yr_dropdown2.getText() + " ]";
                //Toast.makeText(getContext(), startDateFull, Toast.LENGTH_SHORT).show();

                sp_vm.startYear = Integer.parseInt(String.valueOf(yr_dropdown.getText()));
                sp_vm.endYear = Integer.parseInt(String.valueOf(yr_dropdown2.getText()));

                sp_vm.startMonth = ptn.monthAbbrevToNumber(allMonths, String.valueOf(month_dropdown.getText()));
                sp_vm.endMonth = ptn.monthAbbrevToNumber(allMonths, String.valueOf(month_dropdown2.getText()));
                sp_vm.dateIsSet = true;
                date_range.setText(startDateFull + " - " + endDateFull);
                final String dateString = startDateFull.replace("[", "").replace("]", "") + " - " + endDateFull.replace("[", "").replace("]", "");
                setDate(dateString);

                queryDataProg.setVisibility(View.VISIBLE);
                waitProgressBar.showDialog();
                loadMinMaxVals();
                waitProgressBar.dismissDialog();
                dialog.dismiss();
            }


        });
        dialog.show();
    }

    private void annuallyDateRangePicker() {
        AlertDialog.Builder date_dialog = new AlertDialog.Builder(getContext());
        date_dialog.setCancelable(false);
        View date_layout = LayoutInflater.from(getContext()).inflate(R.layout.custom_annually_date_picker, null);
        AutoCompleteTextView yr_dropdown = date_layout.findViewById(R.id.year_dropdown);
        AutoCompleteTextView yr_dropdown2 = date_layout.findViewById(R.id.year_dropdown2);
        Button okayBtn, cancelBtn;
        okayBtn = date_layout.findViewById(R.id.okBtn);
        cancelBtn = date_layout.findViewById(R.id.cancelBtn);
        ProgressBar queryDataProg = date_layout.findViewById(R.id.queryDataProg);
        //initializing yr dropdown
        ArrayList<Integer> allYears = new ArrayList<>();
        for (int y = 2023; y <= 2030; y++) {
            allYears.add(y);
        }
        ArrayAdapter<Integer> y_adapter = new ArrayAdapter<>(getContext(), R.layout.exposed_dropdown_view, allYears);
        yr_dropdown.setAdapter(y_adapter);
        yr_dropdown2.setAdapter(y_adapter);

        date_dialog.setView(date_layout);
        AlertDialog dialog = date_dialog.create();

        cancelBtn.setOnClickListener(v -> {
            dialog.dismiss();
        });

        okayBtn.setOnClickListener(v -> {
            if (String.valueOf(yr_dropdown.getText()).equals("") || String.valueOf(yr_dropdown2.getText()).equals("")) {
                Toast.makeText(getContext(), resources.getString(R.string.select_all_fields_info), Toast.LENGTH_SHORT).show();
            } else {
                if (Integer.parseInt(String.valueOf(yr_dropdown2.getText())) < Integer.parseInt(String.valueOf(yr_dropdown.getText()))) {
                    Toast.makeText(getContext(), resources.getString(R.string.validation_year_info), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Toast.makeText(getContext(),""+ ptn.monthAbbrevToNumber(allMonths, String.valueOf(month_dropdown.getText())), Toast.LENGTH_SHORT).show();

                String startDateFull = "[ " + yr_dropdown.getText() + " ]";
                String endDateFull = "[ " + yr_dropdown2.getText() + " ]";
                //Toast.makeText(getContext(), startDateFull, Toast.LENGTH_SHORT).show();

                sp_vm.startYear = Integer.parseInt(String.valueOf(yr_dropdown.getText()));
                sp_vm.endYear = Integer.parseInt(String.valueOf(yr_dropdown2.getText()));
                sp_vm.dateIsSet = true;
                date_range.setText(startDateFull + " - " + endDateFull);
                final String dateString = startDateFull.replace("[", "").replace("]", "") + " - " + endDateFull.replace("[", "").replace("]", "");
                setDate(dateString);
                queryDataProg.setVisibility(View.VISIBLE);
                waitProgressBar.showDialog();
                loadMinMaxVals();
                waitProgressBar.dismissDialog();
                dialog.dismiss();
            }


        });
        dialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void dailyDateRangePicker() {
        AlertDialog.Builder date_dialog = new AlertDialog.Builder(getContext());
        date_dialog.setCancelable(false);
        View date_layout = LayoutInflater.from(getContext()).inflate(R.layout.custom_daily_date_picker, null);
        AutoCompleteTextView yr_dropdown = date_layout.findViewById(R.id.year_dropdown);
        AutoCompleteTextView yr_dropdown2 = date_layout.findViewById(R.id.year_dropdown2);
        AutoCompleteTextView month_dropdown = date_layout.findViewById(R.id.month_dropdown);
        AutoCompleteTextView month_dropdown2 = date_layout.findViewById(R.id.month_dropdown2);
        AutoCompleteTextView day1_dropdown = date_layout.findViewById(R.id.week1_dropdown);
        AutoCompleteTextView day2_dropdown = date_layout.findViewById(R.id.week2_dropdown);
        Button okayBtn, cancelBtn;
        okayBtn = date_layout.findViewById(R.id.okBtn);
        cancelBtn = date_layout.findViewById(R.id.cancelBtn);
        ProgressBar queryDataProg = date_layout.findViewById(R.id.queryDataProg);
        //initializing yr dropdown
        ArrayList<Integer> allYears = new ArrayList<>();
        /*for (int y = 2023; y <= 2030; y++) {
            allYears.add(y);
        }*/
        allYears.add(2023);
        ArrayAdapter<Integer> y_adapter = new ArrayAdapter<>(getContext(), R.layout.exposed_dropdown_view, allYears);
        yr_dropdown.setAdapter(y_adapter);
        yr_dropdown2.setAdapter(y_adapter);
        yr_dropdown.setText(resources.getString(R.string._2023), true);
        yr_dropdown2.setText(resources.getString(R.string._2023), true);
        /// month dropdown

        ArrayAdapter<CharSequence> m_adapter = new ArrayAdapter<>(getContext(), R.layout.exposed_dropdown_view, allMonths);
        month_dropdown.setAdapter(m_adapter);
        month_dropdown2.setAdapter(m_adapter);

        month_dropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                List<Integer> days = weatherDataDBHelper.getDaysOfAMonth(Integer.parseInt(String.valueOf(yr_dropdown.getText())), ptn.monthAbbrevToNumber(allMonths, String.valueOf(month_dropdown.getText())));
                populateDropDown(day1_dropdown, days);
            }
        });
        month_dropdown2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                List<Integer> days = weatherDataDBHelper.getDaysOfAMonth(Integer.parseInt(String.valueOf(yr_dropdown2.getText())), ptn.monthAbbrevToNumber(allMonths, String.valueOf(month_dropdown2.getText())));
                populateDropDown(day2_dropdown, days);
            }
        });
        date_dialog.setView(date_layout);
        AlertDialog dialog = date_dialog.create();

        cancelBtn.setOnClickListener(v -> {
            dialog.dismiss();
        });

        okayBtn.setOnClickListener(v -> {
            if (String.valueOf(yr_dropdown.getText()).equals("") || String.valueOf(yr_dropdown2.getText()).equals("") || String.valueOf(month_dropdown.getText()).equals("") || String.valueOf(month_dropdown2.getText()).equals("")) {
                Toast.makeText(getContext(), resources.getString(R.string.select_all_fields_info), Toast.LENGTH_SHORT).show();
            } else {
                if (Integer.parseInt(String.valueOf(yr_dropdown2.getText())) < Integer.parseInt(String.valueOf(yr_dropdown.getText()))) {
                    Toast.makeText(getContext(), resources.getString(R.string.validation_year_info), Toast.LENGTH_SHORT).show();
                    return;
                }
                if ((Integer.parseInt(String.valueOf(yr_dropdown2.getText())) == Integer.parseInt(String.valueOf(yr_dropdown.getText()))) && (ptn.monthAbbrevToNumber(allMonths, String.valueOf(month_dropdown2.getText())) < ptn.monthAbbrevToNumber(allMonths, String.valueOf(month_dropdown.getText())))) {
                    Toast.makeText(getContext(), resources.getString(R.string.validation_month_info), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Toast.makeText(getContext(),""+ ptn.monthAbbrevToNumber(allMonths, String.valueOf(month_dropdown.getText())), Toast.LENGTH_SHORT).show();

                String startDateFull = "[" + day1_dropdown.getText() + "/" + month_dropdown.getText() + " / " + yr_dropdown.getText() + " ]";
                String endDateFull = "[" + day2_dropdown.getText() + "/" + month_dropdown2.getText() + " / " + yr_dropdown2.getText() + " ]";
                //Toast.makeText(getContext(), startDateFull, Toast.LENGTH_SHORT).show();

                sp_vm.startYear = Integer.parseInt(String.valueOf(yr_dropdown.getText()));
                sp_vm.endYear = Integer.parseInt(String.valueOf(yr_dropdown2.getText()));

                sp_vm.startMonth = ptn.monthAbbrevToNumber(allMonths, String.valueOf(month_dropdown.getText()));
                sp_vm.endMonth = ptn.monthAbbrevToNumber(allMonths, String.valueOf(month_dropdown2.getText()));

                sp_vm.startDay = Integer.parseInt(String.valueOf(day1_dropdown.getText()));
                sp_vm.endDay = Integer.parseInt(String.valueOf(day2_dropdown.getText()));
                sp_vm.dateIsSet = true;

                date_range.setText(startDateFull + " - " + endDateFull);
                final String dateString = startDateFull.replace("[", "").replace("]", "") + " - " + endDateFull.replace("[", "").replace("]", "");
                setDate(dateString);

                queryDataProg.setVisibility(View.VISIBLE);
                waitProgressBar.showDialog();
                loadMinMaxVals();
                waitProgressBar.dismissDialog();
                dialog.dismiss();
            }


        });
        dialog.show();
    }

    private void weeklyDateRangePicker() {
        AlertDialog.Builder date_dialog = new AlertDialog.Builder(getContext());
        date_dialog.setCancelable(false);
        View date_layout = LayoutInflater.from(getContext()).inflate(R.layout.custom_weekly_date_picker, null);
        AutoCompleteTextView yr_dropdown = date_layout.findViewById(R.id.year_dropdown);
        AutoCompleteTextView yr_dropdown2 = date_layout.findViewById(R.id.year_dropdown2);
        AutoCompleteTextView month_dropdown = date_layout.findViewById(R.id.month_dropdown);
        AutoCompleteTextView month_dropdown2 = date_layout.findViewById(R.id.month_dropdown2);
        AutoCompleteTextView week1_dropdown = date_layout.findViewById(R.id.week1_dropdown);
        AutoCompleteTextView week2_dropdown = date_layout.findViewById(R.id.week2_dropdown);
        Button okayBtn, cancelBtn;
        okayBtn = date_layout.findViewById(R.id.okBtn);
        cancelBtn = date_layout.findViewById(R.id.cancelBtn);
        ProgressBar queryDataProg = date_layout.findViewById(R.id.queryDataProg);
        //initializing yr dropdown
        ArrayList<Integer> allYears = new ArrayList<>();
       /* for (int y = 2023; y <= 2030; y++) {
            allYears.add(y);
        }*/
        allYears.add(2023);
        ArrayAdapter<Integer> y_adapter = new ArrayAdapter<>(getContext(), R.layout.exposed_dropdown_view, allYears);
        yr_dropdown.setAdapter(y_adapter);
        yr_dropdown2.setAdapter(y_adapter);
        yr_dropdown.setText(resources.getString(R.string._2023), true);
        yr_dropdown2.setText(resources.getString(R.string._2023), true);
        /// month dropdown

        ArrayAdapter<CharSequence> m_adapter = new ArrayAdapter<>(getContext(), R.layout.exposed_dropdown_view, allMonths);
        month_dropdown.setAdapter(m_adapter);
        month_dropdown2.setAdapter(m_adapter);


        month_dropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int num_days = weatherDataDBHelper.getDaysOfAMonth(Integer.parseInt(String.valueOf(yr_dropdown.getText())), ptn.monthAbbrevToNumber(allMonths, String.valueOf(month_dropdown.getText()))).size();
                int n_weeks = num_days > 28 ? 5 : 4;
                List<String> weeks = new ArrayList<>();
                for (int n = 1; n <= n_weeks; n++) {
                    weeks.add(resources.getString(R.string.week)+" " + n);
                }
                ArrayAdapter<String> weeks_adapter = new ArrayAdapter<>(getContext(), R.layout.exposed_dropdown_view, weeks);
                week1_dropdown.setAdapter(weeks_adapter);
            }
        });
        month_dropdown2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int num_days = weatherDataDBHelper.getDaysOfAMonth(Integer.parseInt(String.valueOf(yr_dropdown2.getText())), ptn.monthAbbrevToNumber(allMonths, String.valueOf(month_dropdown2.getText()))).size();
                int n_weeks = num_days > 28 ? 5 : 4;
                List<String> weeks = new ArrayList<>();
                for (int n = 1; n <= n_weeks; n++) {
                    weeks.add(resources.getString(R.string.week)+" " + n);
                }

                ArrayAdapter<String> weeks_adapter2 = new ArrayAdapter<>(getContext(), R.layout.exposed_dropdown_view, weeks);
                week2_dropdown.setAdapter(weeks_adapter2);
            }
        });


        date_dialog.setView(date_layout);
        AlertDialog dialog = date_dialog.create();

        cancelBtn.setOnClickListener(v -> {
            dialog.dismiss();
        });

        okayBtn.setOnClickListener(v -> {
            if (String.valueOf(yr_dropdown.getText()).equals("") || String.valueOf(yr_dropdown2.getText()).equals("") || String.valueOf(month_dropdown.getText()).equals("") || String.valueOf(month_dropdown2.getText()).equals("")) {
                Toast.makeText(getContext(), resources.getString(R.string.select_all_fields_info), Toast.LENGTH_SHORT).show();
            } else {
                if (Integer.parseInt(String.valueOf(yr_dropdown2.getText())) < Integer.parseInt(String.valueOf(yr_dropdown.getText()))) {
                    Toast.makeText(getContext(), resources.getString(R.string.validation_year_info), Toast.LENGTH_SHORT).show();
                    return;
                }
                if ((Integer.parseInt(String.valueOf(yr_dropdown2.getText())) == Integer.parseInt(String.valueOf(yr_dropdown.getText()))) && (ptn.monthAbbrevToNumber(allMonths, String.valueOf(month_dropdown2.getText())) < ptn.monthAbbrevToNumber(allMonths, String.valueOf(month_dropdown.getText())))) {
                    Toast.makeText(getContext(), resources.getString(R.string.validation_month_info), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Toast.makeText(getContext(),""+ ptn.monthAbbrevToNumber(allMonths, String.valueOf(month_dropdown.getText())), Toast.LENGTH_SHORT).show();

                String startDateFull = "[" + week1_dropdown.getText() + "/" + month_dropdown.getText() + " / " + yr_dropdown.getText() + " ]";
                String endDateFull = "[" + week2_dropdown.getText() + "/" + month_dropdown2.getText() + " / " + yr_dropdown2.getText() + " ]";
                //Toast.makeText(getContext(), startDateFull, Toast.LENGTH_SHORT).show();

                sp_vm.startYear = Integer.parseInt(String.valueOf(yr_dropdown.getText()));
                sp_vm.endYear = Integer.parseInt(String.valueOf(yr_dropdown2.getText()));

                sp_vm.startMonth = ptn.monthAbbrevToNumber(allMonths, String.valueOf(month_dropdown.getText()));
                sp_vm.endMonth = ptn.monthAbbrevToNumber(allMonths, String.valueOf(month_dropdown2.getText()));

               // sp_vm.startWeek = Integer.parseInt(String.valueOf(week1_dropdown.getText().subSequence(5, 6)));
                //sp_vm.endWeek = Integer.parseInt(String.valueOf(week2_dropdown.getText().subSequence(5, 6)));

                sp_vm.startWeek = Integer.parseInt(String.valueOf(week1_dropdown.getText().toString().substring(week1_dropdown.getText().length() - 1)));
                sp_vm.endWeek = Integer.parseInt(String.valueOf(week2_dropdown.getText().toString().substring(week2_dropdown.getText().length() - 1)));

                sp_vm.dateIsSet = true;

                date_range.setText(startDateFull + " - " + endDateFull);
                final String dateString = startDateFull.replace("[", "").replace("]", "") + " - " + endDateFull.replace("[", "").replace("]", "");
                setDate(dateString);

                queryDataProg.setVisibility(View.VISIBLE);
                waitProgressBar.showDialog();
                loadMinMaxVals();
                waitProgressBar.dismissDialog();
                dialog.dismiss();
            }


        });
        dialog.show();
    }

    private void populateDropDown(@NonNull AutoCompleteTextView dropdown, List<Integer> days) {
        //Toast.makeText(getContext(), "Called", Toast.LENGTH_SHORT).show();
        ArrayAdapter<Integer> m_adapter = new ArrayAdapter<>(getContext(), R.layout.exposed_dropdown_view, days);
        dropdown.setAdapter(m_adapter);
    }

    @SuppressLint("SetTextI18n")
    private void setStationName() {
        String st_name = getResources().getStringArray(R.array.stations)[sp_vm.St_ID - 1];
        ST_name1.setText(" - " + st_name);
        ST_name2.setText("- " + st_name);
        ST_name3.setText("- " + st_name);
///Starting marquee if the station name is too long
        ST_name1.setActivated(true);
        ST_name2.setActivated(true);
        ST_name3.setActivated(true);
        selectedStationName.setText(st_name);
    }

    private void setDate(String date) {
        date1.setText(date);
        date2.setText(date);
        date3.setText(date);
    }

    @SuppressLint("SetTextI18n")
    private void addToDefaultDialog() {
        int p_interval = sp_vm.period_interval;
        int St_ID, startYear, startMonth, endYear, endMonth, startDay, endDay, startWeek, endWeek;
        St_ID = sp_vm.St_ID;
        startMonth = sp_vm.startMonth;
        endMonth = sp_vm.endMonth;
        startYear = sp_vm.startYear;
        endYear = sp_vm.endYear;
        startDay = sp_vm.startDay;
        endDay = sp_vm.endDay;
        startWeek = sp_vm.startWeek;
        endWeek = sp_vm.endWeek;

        AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.add_to_default, null);

        TextView station_name, period_interval, date_range;
        Button cancelBtn, okBtn;

        station_name = view.findViewById(R.id.station_name);
        period_interval = view.findViewById(R.id.period_interval);
        date_range = view.findViewById(R.id.date_range);
        cancelBtn = view.findViewById(R.id.cancelBtn);
        okBtn = view.findViewById(R.id.okBtn);

        station_name.setText(getResources().getStringArray(R.array.stations)[St_ID - 1]);

        String[] months_abbrev = getResources().getStringArray(R.array.months_abbrev);

        if (p_interval == 2) {
            period_interval.setText(resources.getString(R.string.monthly));
            String monthly_range_text = "[" + months_abbrev[startMonth - 1] + "/" + startYear + "] - [" + months_abbrev[endMonth - 1] + "/" + endYear + "]";
            date_range.setText(monthly_range_text);
        } else if (p_interval == 3) {
            //startDay = sp_vm.startDay;
            // endDay = sp_vm.endDay;
            period_interval.setText(resources.getString(R.string.annually));
            String annually_range_text = "[" + startYear + "] - [" + endYear + "]";
            date_range.setText(annually_range_text);
        } else if (p_interval == 0) {
            period_interval.setText(resources.getString(R.string.daily));
            String daily_range_text = "[" + startDay + "/" + months_abbrev[startMonth - 1] + "/" + startYear + "] - [" + endDay + "/" + months_abbrev[endMonth - 1] + "/" + endYear + "]";
            date_range.setText(daily_range_text);
        } else if (p_interval == 1) {
            period_interval.setText(resources.getString(R.string.weekly));
            String weekly_range_text = "["+resources.getString(R.string.week_first_letter)+" " + startWeek + "/" + months_abbrev[startMonth - 1] + "/" + startYear + "] - ["+resources.getString(R.string.week_first_letter)+" " + endWeek + "/" + months_abbrev[endMonth - 1] + "/" + endYear + "]";
            date_range.setText(weekly_range_text);
        }
        adb.setView(view);
        AlertDialog alertDialog = adb.create();
        cancelBtn.setOnClickListener(v -> {
            alertDialog.dismiss();
        });
        okBtn.setOnClickListener(v -> {
            if (p_interval == 2) {
                stationVisualization_default_editor.putInt("St_ID", St_ID);
                //stationVisualization_default_editor.putString("periodInterval", p_interval);
                stationVisualization_default_editor.putInt("periodInterval", 2);
                stationVisualization_default_editor.putInt("startMonth", startMonth);
                stationVisualization_default_editor.putInt("endMonth", endMonth);
                stationVisualization_default_editor.putInt("startYear", startYear);
                stationVisualization_default_editor.putInt("endYear", endYear);
                stationVisualization_default_editor.putBoolean("defaultIsSet", true);
                stationVisualization_default_editor.commit();
                Snackbar.make(scrollViewContainer, resources.getString(R.string.default_parameters_set_info), Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        stationVisualization_default_editor.putBoolean("defaultIsSet", false);
                    }
                }).show();
            } else if (p_interval == 3) {
                stationVisualization_default_editor.putInt("St_ID", St_ID);
                stationVisualization_default_editor.putInt("periodInterval", 3);
                stationVisualization_default_editor.putInt("startMonth", startMonth);
                stationVisualization_default_editor.putInt("endMonth", endMonth);
                stationVisualization_default_editor.putInt("startYear", startYear);
                stationVisualization_default_editor.putInt("endYear", endYear);
                //stationVisualization_default_editor.putInt("startDay", sp_vm.startDay);
                //stationVisualization_default_editor.putInt("endDay", sp_vm.endDay);
                stationVisualization_default_editor.putBoolean("defaultIsSet", true);
                stationVisualization_default_editor.commit();
                Snackbar.make(scrollViewContainer, resources.getString(R.string.default_parameters_set_info), Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        stationVisualization_default_editor.putBoolean("defaultIsSet", false).commit();
                    }
                }).show();
            } else if (p_interval == 1) {
                stationVisualization_default_editor.putInt("St_ID", St_ID);
                stationVisualization_default_editor.putInt("periodInterval", 1);
                stationVisualization_default_editor.putInt("startMonth", startMonth);
                stationVisualization_default_editor.putInt("endMonth", endMonth);
                stationVisualization_default_editor.putInt("startYear", startYear);
                stationVisualization_default_editor.putInt("endYear", endYear);
                stationVisualization_default_editor.putInt("startWeek", sp_vm.startWeek);
                stationVisualization_default_editor.putInt("endWeek", sp_vm.endWeek);
                stationVisualization_default_editor.putBoolean("defaultIsSet", true);
                stationVisualization_default_editor.commit();
                Snackbar.make(scrollViewContainer, resources.getString(R.string.default_parameters_set_info), Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        stationVisualization_default_editor.putBoolean("defaultIsSet", false).commit();
                    }
                }).show();
            } else if (p_interval == 0) {
                stationVisualization_default_editor.putInt("St_ID", St_ID);
                stationVisualization_default_editor.putInt("periodInterval", 0);
                stationVisualization_default_editor.putInt("startMonth", startMonth);
                stationVisualization_default_editor.putInt("endMonth", endMonth);
                stationVisualization_default_editor.putInt("startYear", startYear);
                stationVisualization_default_editor.putInt("endYear", endYear);
                stationVisualization_default_editor.putInt("startDay", sp_vm.startDay);
                stationVisualization_default_editor.putInt("endDay", sp_vm.endDay);
                stationVisualization_default_editor.putBoolean("defaultIsSet", true);
                stationVisualization_default_editor.commit();
                Snackbar.make(scrollViewContainer, resources.getString(R.string.daily), Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        stationVisualization_default_editor.putBoolean("defaultIsSet", false).commit();
                    }
                }).show();
            }

            alertDialog.dismiss();
        });
        alertDialog.show();
    }

    private void loadDefaultStationData() {

        int p_interval = stationSharedPreferences.getInt("periodInterval", 0);
        //int St_ID, startYear, startMonth, endYear, endMonth, startDay, endDay;
        sp_vm.St_ID = stationSharedPreferences.getInt("St_ID", 0);
        sp_vm.startMonth = stationSharedPreferences.getInt("startMonth", 0);
        sp_vm.endMonth = stationSharedPreferences.getInt("endMonth", 0);
        sp_vm.startYear = stationSharedPreferences.getInt("startYear", 0);
        sp_vm.endYear = stationSharedPreferences.getInt("endYear", 0);
        sp_vm.dateIsSet = true;
        if (p_interval == 2) {
            periodSpinner.setSelection(2);
            sp_vm.period_interval = p_interval;
            String monthly_range_text = "[" + allMonths[sp_vm.startMonth - 1] + "/" + sp_vm.startYear + "] - [" + allMonths[sp_vm.endMonth - 1] + "/" + sp_vm.endYear + "]";
            setDate(monthly_range_text);
            date_range.setText(monthly_range_text);
        } else if (p_interval == 3) {
            periodSpinner.setSelection(3);
            sp_vm.period_interval = p_interval;
            String annually_range_text = "[" + sp_vm.startYear + "] - [" + sp_vm.endYear + "]";
            setDate(annually_range_text);
            date_range.setText(annually_range_text);
        } else if (p_interval == 1) {
            periodSpinner.setSelection(1);
            sp_vm.period_interval = p_interval;
            sp_vm.startWeek = stationSharedPreferences.getInt("startWeek", 0);
            sp_vm.endWeek = stationSharedPreferences.getInt("endWeek", 0);
            String weekly_range_text = "["+resources.getString(R.string.week_first_letter)+"" + sp_vm.startWeek + "/" + allMonths[sp_vm.startMonth - 1] + "/" + sp_vm.startYear + "] - ["+resources.getString(R.string.week_first_letter)+"" + sp_vm.endWeek + "/" + allMonths[sp_vm.endMonth - 1] + "/" + sp_vm.endYear + "]";
            setDate(weekly_range_text);
            date_range.setText(weekly_range_text);
        } else if (p_interval == 0) {
            periodSpinner.setSelection(0);
            sp_vm.period_interval = p_interval;
            sp_vm.startDay = stationSharedPreferences.getInt("startDay", 0);
            sp_vm.endDay = stationSharedPreferences.getInt("endDay", 0);
            String daily_range_text = "[" + sp_vm.startDay + "/" + allMonths[sp_vm.startMonth - 1] + "/" + sp_vm.startYear + "] - [" + sp_vm.endDay + "/" + allMonths[sp_vm.endMonth - 1] + "/" + sp_vm.endYear + "]";
            setDate(daily_range_text);
            date_range.setText(daily_range_text);
        }
        setStationName();
        setPeriodAxisTitles();
        loadMinMaxVals();

    }

    @SuppressLint("SetTextI18n")
    private void setPeriodAxisTitles() {
        int interval = sp_vm.period_interval;
        if (interval == 0) {
            period_l1.setText(resources.getString(R.string.period_days_text));
            period_l2.setText(resources.getString(R.string.period_days_text));
            period_l3.setText(resources.getString(R.string.period_days_text));
        } else if (interval == 1) {
            period_l1.setText(resources.getString(R.string.period_weeks_text));
            period_l2.setText(resources.getString(R.string.period_weeks_text));
            period_l3.setText(resources.getString(R.string.period_weeks_text));
        } else if (interval == 2) {
            period_l1.setText(resources.getString(R.string.period_months_text));
            period_l2.setText(resources.getString(R.string.period_months_text));
            period_l3.setText(resources.getString(R.string.period_months_text));
        } else {
            period_l1.setText(resources.getString(R.string.period_years_text));
            period_l2.setText(resources.getString(R.string.period_years_text));
            period_l3.setText(resources.getString(R.string.period_years_text));
        }
    }

   @Override
    public void onDetach() {
        super.onDetach();
        //Shared preference to pass selected station and year
        SharedPreferences preferences = requireContext().getSharedPreferences("SELECTED_STATION", 0);
        SharedPreferences.Editor pref_editor = preferences.edit();
        if(sp_vm.St_ID!=0){
            pref_editor.putInt("St_ID", sp_vm.St_ID);
        }else{
            pref_editor.putInt("St_ID", 0);
        }
        pref_editor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        //Toast.makeText(requireContext(), "Resumed", Toast.LENGTH_SHORT).show();
    }
}