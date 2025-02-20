package com.weatherfocus.wf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import DBHelpers.WeatherDataDBHelper;
import Fragments.Visual_About;
import Fragments.Visual_Diseases;
import Fragments.Visual_Help;
import Fragments.Visual_Home;
import Fragments.Visual_Recommendation;
import ViewHolders.AdapterViewPager;

public class Visualizations extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private ViewPager2 frameLayout;
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private WeatherDataDBHelper Weather_DB;
    SharedPreferences settings;
    SharedPreferences.Editor settings_editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizations);
        settings = getBaseContext().getSharedPreferences("SETTINGS", 0);
              settings_editor  = settings.edit();
        Weather_DB = new WeatherDataDBHelper(Visualizations.this);

        initializeViews(); //initialize views before using
        initializeToolbar(); //adding a toolbar
        initViewPager();
       frameLayout.setOffscreenPageLimit(4);

        //CHECKING THE TOPIC THAT WAS CLICKED ON
        int topic_number = getIntent().getIntExtra("topic_number", 1);
        if (topic_number != 1) {
            SharedPreferences station_prefs = getBaseContext().getSharedPreferences("SELECTED_STATION", 0);
            SharedPreferences.Editor pref_editor = station_prefs.edit();
            pref_editor.putInt("St_ID", 0);
            pref_editor.apply();
        }
        if (topic_number == 2) {//Recommendations
            loadFragment(1); //loading default fragment
            bottomNavigationView.setSelectedItemId(R.id.recommend);
        } else if (topic_number == 3) {//Disease Detection
            loadFragment(2); //loading default fragment
            bottomNavigationView.setSelectedItemId(R.id.diseases);
        } else {
            loadFragment(0); //loading default fragment
        }


        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        return loadFragment(0);
                    case R.id.recommend:
                        return loadFragment(1);
                    case R.id.diseases:
                        return loadFragment(2);
                    case R.id.help:
                        return loadFragment(3);
                    case R.id.about:
                        return loadFragment(4);
                   /* case R.id.settings:
                        return loadFragment(new Visual_Settings());*/
                }
                return false;
            }
        });
    }

    private void initViewPager() {
        fragments.add(new Visual_Home());
        fragments.add(new Visual_Recommendation());
        fragments.add(new Visual_Diseases());
        fragments.add(new Visual_Help());
        fragments.add(new Visual_About());

        AdapterViewPager adapterViewPager = new AdapterViewPager(this, fragments);
        frameLayout.setAdapter(adapterViewPager);
        frameLayout.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.home);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.recommend);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.diseases);
                        break;
                    case 3:
                        bottomNavigationView.setSelectedItemId(R.id.help);
                        break;
                    default:
                        bottomNavigationView.setSelectedItemId(R.id.about);
                }
                super.onPageSelected(position);
            }
        });
    }


    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottom_nav);
        toolbar = findViewById(R.id.toolbar);
        frameLayout = findViewById(R.id.fragment);
    }

    private void initializeToolbar() {
        this.setSupportActionBar(toolbar);
        Objects.requireNonNull(this.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    private boolean loadFragment(int position) {
        frameLayout.setCurrentItem(position);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.parent_home_menu, menu);
        MenuItem item=menu.findItem(R.id.choose_language);
        View customView= LayoutInflater.from(this).inflate(R.layout.change_language_layout, null);
        ImageView  languageIcon= customView.findViewById(R.id.language_icon);
        TextView languageText = customView.findViewById(R.id.language_text);

        languageIcon.setOnClickListener(view -> chooseLanguage());
        languageText.setOnClickListener(view -> chooseLanguage());

        item.setActionView(customView);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
     /*   if (item.getItemId() == R.id.exit) {

        }*/

        switch (item.getItemId()) {
            case R.id.exit:
                showExitPrompt();
                break;
            case R.id.home:
                goBackToHome();
                break;
            case R.id.choose_language:
                chooseLanguage();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void chooseLanguage() {
        final String[] languages={"English","French", "Wolof"};
        String defaultLanguage= settings.getString("lang", "en");

        int defaultLanguageIndex = 0;
       if (Objects.equals(defaultLanguage, "fr")) {
           defaultLanguageIndex =1;
        } else if (Objects.equals(defaultLanguage, "wo")) {
           defaultLanguageIndex =2;
       }

        AlertDialog.Builder languageDialog =new AlertDialog.Builder(Visualizations.this);
        languageDialog.setCancelable(true)
                .setTitle(R.string.change_language)
                .setSingleChoiceItems(languages,defaultLanguageIndex , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                      if(i==0){
                          //English
                          setLocale("en");
                          recreate();
                      }  else if(i==1){
                          //French
                          setLocale("fr");
                          recreate();
                      } else if (i==2) {
                          //Wolof
                          setLocale("wo");
                          recreate();
                      }
                        dialogInterface.dismiss();
                    }

                });
        AlertDialog alertDialog = languageDialog.create();
        alertDialog.show();
    }

    private void setLocale(String language_id) {
        settings_editor.putString("lang", language_id); //saving default language
        settings_editor.apply();

        Locale locale=new Locale(language_id);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
    }

    public void showExitPrompt() {
        AlertDialog.Builder adb = new AlertDialog.Builder(Visualizations.this);
        adb.setCancelable(false);
        View confirmationView = getLayoutInflater().inflate(R.layout.confirm_exit, null);
        Button noBtn = confirmationView.findViewById(R.id.noBtn);
        Button yesBtn = confirmationView.findViewById(R.id.yesBtn);
        adb.setView(confirmationView);
        AlertDialog alertDialog = adb.create();
        noBtn.setOnClickListener(v -> {
            alertDialog.dismiss();
        });
        yesBtn.setOnClickListener(v -> {
            alertDialog.dismiss();
            finishAffinity();
        });
        alertDialog.show();
    }

    public void goBackToHome() {
        startActivity(new Intent(Visualizations.this, MainActivity.class));
    }
}