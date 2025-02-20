package com.weatherfocus.wf;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Objects;

import HelperFunctions.WaitProgressBar;

public class MainActivity extends AppCompatActivity {
    private ConstraintLayout w_visualization, w_recommendation, w_disease_detection;
    private ProgressBar visualization_loader;
    private WaitProgressBar waitProgressBar;
    SharedPreferences settings;
    SharedPreferences.Editor settings_editor;
    ImageView language_icon;
    TextView language_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settings = getBaseContext().getSharedPreferences("SETTINGS", 0);
        settings_editor = settings.edit();

        loadLocale();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //recreate();

        waitProgressBar = new WaitProgressBar(MainActivity.this);
        initializeViews();//initializing all the views before using them

        // hideAllLoaders();
        w_visualization.setOnClickListener(v -> {
            waitProgressBar.showDialog();
            Intent intent = new Intent(MainActivity.this, Visualizations.class);
            intent.putExtra("topic_number", 1); //visualization
            startActivity(intent);
        });
        w_recommendation.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Visualizations.class);
            intent.putExtra("topic_number", 2); //recommendation
            startActivity(intent);
        });
        w_disease_detection.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Visualizations.class);
            intent.putExtra("topic_number", 3); //diseases
            startActivity(intent);
        });


        language_icon.setOnClickListener(view -> chooseLanguage());
        language_text.setOnClickListener(view -> chooseLanguage());
    }

    private void loadLocale() {
        String languageDefault = settings.getString("lang", "en");
        // Toast.makeText(this, languageDefault, Toast.LENGTH_SHORT).show();
        setLocale(languageDefault);

        Locale currentLocale = Locale.getDefault();
        String languageCode = currentLocale.getLanguage();

        if (!languageCode.equals(languageDefault)) {
            recreate();
        }
    }

    private void initializeViews() {
        w_visualization = findViewById(R.id.w_visualization);
        w_recommendation = findViewById(R.id.w_recommendation);
        visualization_loader = findViewById(R.id.visualization_loader);
        w_disease_detection = findViewById(R.id.w_disease_detection);
        language_text = findViewById(R.id.language_text);
        language_icon = findViewById(R.id.language_icon);
    }

    private void hideAllLoaders() {
        visualization_loader.setVisibility(View.GONE);
        waitProgressBar.dismissDialog();
    }

    private void setLocale(String language_id) {
        settings_editor.putString("lang", language_id); //saving default language
        settings_editor.apply();

        Locale locale = new Locale(language_id);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
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

        AlertDialog.Builder languageDialog =new AlertDialog.Builder(MainActivity.this);
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

    @Override
    protected void onRestart() {
        super.onRestart();
        // hideAllLoaders();
        if (waitProgressBar.alertDialog.isShowing()) {
            waitProgressBar.dismissDialog();
        }
    }

}