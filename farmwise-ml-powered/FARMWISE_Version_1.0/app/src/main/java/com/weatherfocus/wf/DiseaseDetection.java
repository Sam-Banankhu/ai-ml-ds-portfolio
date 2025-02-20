package com.weatherfocus.wf;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.weatherfocus.wf.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

public class DiseaseDetection extends AppCompatActivity {
    Toolbar toolbar;


    Button camera, gallery;
    ImageView imageView;
    TextView result;
    TextView reco;
    TextView confi;
    Resources resources;
    int imageSize = 224;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease_detection);
        resources = getResources();
        Intent intent = getIntent();
        String cropName = intent.getStringExtra("crop_name");
        Integer cropIcon = intent.getIntExtra("crop_icon", R.drawable.crop);
        initViews();
        toolbar.setTitle(resources.getString(R.string.disease_detection));
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setSubtitle(cropName);
        //this.getSupportActionBar().setHomeAsUpIndicator(cropIcon);
        //this.getSupportActionBar().setIcon(R.drawable.virus);
        //this.getSupportActionBar().setIcon(cropIcon);

        camera = findViewById(R.id.button);
        gallery = findViewById(R.id.button2);

        result = findViewById(R.id.result);
        reco = findViewById(R.id.reco);
        confi = findViewById(R.id.confidence);
        imageView = findViewById(R.id.imageView);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, 3);
                    } else {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                    }
                }
            }
        });
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(cameraIntent, 1);
            }
        });

    }

    public void showExitPrompt() {
        AlertDialog.Builder adb = new AlertDialog.Builder(DiseaseDetection.this);
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

 /*   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.parent_home_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }*/

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
    }

    @SuppressLint("NonConstantResourceId")
    //@Override
    /*public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exit:
                showExitPrompt();
                return true;
            case R.id.home:
                startActivity(new Intent(DiseaseDetection.this, MainActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void classifyImage(Bitmap image) {
        try {
            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 1));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            // find the index of the class with the biggest confidence.
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
         /*   String[] classes = {"Alternaria_Mild","Alternaria_Moderate","Alternaria_Severe","Curvularia_Mild","Curvularia_Moderate",
                    "Curvularia_Severe", "Health", "Helminthosporium_Mild", "Helminthosporium_Moderate",
                    "Helminthosporium_Severe", "Lasiodiplodia_Mild", "Lasiodiplodia_Moderate","Lasiodiplodia_Severe"};*/

            /*
            Treatments/recommended treatments have deliberately duplicated so that it is easy to change treatment for one specific disease
             */


            String[] classes = resources.getStringArray(R.array.classes); //Accessed from strings.xml file

            if (maxPos == 0 && confidences[maxPos] > 0.5) {
                // result.setText("\nAlternaria Mild\n");
                result.setText("\n" + classes[0] + "\n");
                //confi.setText("With confidence of: " + String.format("%.1f",(confidences[maxPos]* 100)) + "%\n");
                confi.setText(resources.getString(R.string.results_confidence, String.format("%.1f", (confidences[maxPos] * 100))));
                //reco.setText("You can apply chlorothalonil or copper fungicides\n");
                reco.setText(resources.getString(R.string.d0_treat));
            } else if (maxPos == 1 && confidences[maxPos] > 0.5) {
                //result.setText("\nAlternaria Moderate\n");
                result.setText("\n" + classes[1] + "\n");
                //confi.setText("With confidence of: " + String.format("%.1f",(confidences[maxPos]* 100)) + "%\n");
                confi.setText(resources.getString(R.string.results_confidence, String.format("%.1f", (confidences[maxPos] * 100))));
                reco.setText(resources.getString(R.string.d1_treat));

            } else if (maxPos == 2 && confidences[maxPos] > 0.5) {
                //result.setText("\nAlternaria Severe\n");
                result.setText("\n" + classes[2] + "\n");
                //confi.setText("With confidence of: " + String.format("%.1f",(confidences[maxPos]* 100)) + "%\n");
                confi.setText(resources.getString(R.string.results_confidence, String.format("%.1f", (confidences[maxPos] * 100))));
                reco.setText(resources.getString(R.string.d2_treat));

            } else if (maxPos == 3 && confidences[maxPos] > 0.5) {
                //result.setText("\nCurvularia Mild\n");
                result.setText("\n" + classes[3] + "\n");
                //confi.setText("With confidence of: " + String.format("%.1f",(confidences[maxPos]* 100)) + "%\n");
                confi.setText(resources.getString(R.string.results_confidence, String.format("%.1f", (confidences[maxPos] * 100))));
                reco.setText(resources.getString(R.string.d3_treat));


            } else if (maxPos == 4 && confidences[maxPos] > 0.5) {
                //result.setText("\nCurvularia Moderate\n");
                result.setText("\n" + classes[4] + "\n");
                //confi.setText("With confidence of: " + String.format("%.1f",(confidences[maxPos]* 100)) + "%\n")
                confi.setText(resources.getString(R.string.results_confidence, String.format("%.1f", (confidences[maxPos] * 100))));
                reco.setText(resources.getString(R.string.d4_treat));

            } else if (maxPos == 5 && confidences[maxPos] > 0.5) {
                // result.setText("\nCurvularia Severe\n");
                result.setText("\n" + classes[5] + "\n");
                // confi.setText("With confidence of: " + String.format("%.1f",(confidences[maxPos]* 100)) + "%\n");
                confi.setText(resources.getString(R.string.results_confidence, String.format("%.1f", (confidences[maxPos] * 100))));
                reco.setText(resources.getString(R.string.d5_treat));

            } else if (maxPos == 6 && confidences[maxPos] > 0.5) {
                result.setText("\n" + resources.getString(R.string.no_disease_detected) + "\n");

            } else if (maxPos == 7 && confidences[maxPos] > 0.5) {
                //result.setText("\nHelminthosporium Mild\n");
                result.setText("\n" + classes[7] + "\n");
                //confi.setText("With confidence of: " + String.format("%.1f",(confidences[maxPos]* 100)) + "%\n");
                confi.setText(resources.getString(R.string.results_confidence, String.format("%.1f", (confidences[maxPos] * 100))));
                reco.setText(resources.getString(R.string.d7_treat));

            } else if (maxPos == 8 && confidences[maxPos] > 0.5) {
                // result.setText("\nHelminthosporium Moderate\n");
                result.setText("\n" + classes[8] + "\n");
                // confi.setText("With confidence of: " + String.format("%.1f",(confidences[maxPos]* 100)) + "%\n");
                confi.setText(resources.getString(R.string.results_confidence, String.format("%.1f", (confidences[maxPos] * 100))));
                reco.setText(resources.getString(R.string.d8_treat));

            } else if (maxPos == 9 && confidences[maxPos] > 0.5) {
                //result.setText("\nHelminthosporium Severe\n");
                result.setText("\n" + classes[9] + "\n");
                //confi.setText("With confidence of: " + String.format("%.1f",(confidences[maxPos]* 100)) + "%\n");
                confi.setText(resources.getString(R.string.results_confidence, String.format("%.1f", (confidences[maxPos] * 100))));
                reco.setText(resources.getString(R.string.d9_treat));

            } else if (maxPos == 10 && confidences[maxPos] > 0.5) {
                //result.setText("\nLasiodiplodia Mild\n");
                result.setText("\n" + classes[10] + "\n");
                // confi.setText("With confidence of: " + String.format("%.1f",(confidences[maxPos]* 100)) + "%\n");
                confi.setText(resources.getString(R.string.results_confidence, String.format("%.1f", (confidences[maxPos] * 100))));
                reco.setText(resources.getString(R.string.d10_treat));

            } else if (maxPos == 11 && confidences[maxPos] > 0.5) {
                //result.setText("\nLasiodiplodia Moderate\n");
                result.setText("\n" + classes[11] + "\n");
                //confi.setText("With confidence of: " + String.format("%.1f",(confidences[maxPos]* 100)) + "%\n");
                confi.setText(resources.getString(R.string.results_confidence, String.format("%.1f", (confidences[maxPos] * 100))));
                reco.setText(resources.getString(R.string.d11_treat));

            } else if (maxPos == 12 && confidences[maxPos] > 0.5) {
                //result.setText("\nLasiodiplodia Severe\n");
                result.setText("\n" + classes[12] + "\n");
                //confi.setText("With confidence of: " + String.format("%.1f",(confidences[maxPos]* 100)) + "%\n");
                confi.setText(resources.getString(R.string.results_confidence, String.format("%.1f", (confidences[maxPos] * 100))));
                reco.setText(resources.getString(R.string.d12_treat));
            } else {
                result.setText("\n" + resources.getString(R.string.disease_not_known) + "\n");
            }

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 3) {
                Bitmap image = (Bitmap) data.getExtras().get("data");
                int dimension = Math.min(image.getWidth(), image.getHeight());
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                imageView.setImageBitmap(image);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
            } else {
                Uri dat = data.getData();
                Bitmap image = null;
                try {
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dat);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageView.setImageBitmap(image);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}