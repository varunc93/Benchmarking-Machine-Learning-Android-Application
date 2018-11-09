package com.project;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private boolean flag_smo = false;
    private boolean flag_nb = false;
    private boolean flag_rf = false;
    private boolean flag_dt = false;
    private float Trainpercent = 0;
    BufferedReader reader;

    String[] classifierList = {"Select Model(s)", "SVM", "Naive Bayes", "Decision Tree", "Random Forest"};
    ArrayList<Integer> classifiersAlreadyAdded_index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Requesting permission for accessing the files in storage
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        try{
            createfolder();
        }
        catch (Exception e){

        }

        final EditText trainpercentage;
        trainpercentage = (EditText) findViewById(R.id.editText_TrainPercentage);
        trainpercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    String trainpercent = trainpercentage.getText().toString();
                    float train = Float.parseFloat(trainpercent);
                    if (train < 1 || train >= 100)
                        Toast.makeText(MainActivity.this, "Please enter correct percentage value", Toast.LENGTH_SHORT).show();
                    else
                        Trainpercent = train;
                }
                catch (Exception e){

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        classifiersAlreadyAdded_index = new ArrayList<>();
        classifiersAlreadyAdded_index.add(0);

        SharedPreferences pref = getSharedPreferences("Flags", 0);
        final SharedPreferences.Editor editor = pref.edit();

        SharedPreferences pref_output = getSharedPreferences("Flags_output", 0);
        final SharedPreferences.Editor editor_output = pref_output.edit();

        final Spinner classifier = (Spinner) findViewById(R.id.spinner_ModelSelection);
        ArrayAdapter<String> C_list = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, classifierList);
        classifier.setAdapter(C_list);
        classifier.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                boolean availabilityFlag = false;
                for (int loopVar = 0; loopVar < classifiersAlreadyAdded_index.size(); loopVar++) {
                    if (position == classifiersAlreadyAdded_index.get(loopVar)) {
                        availabilityFlag = true;
                    }
                }
                if (availabilityFlag) {

                } else {
                    TextView temp = (TextView) findViewById(R.id.textView_SelectedClassifier);
                    if (classifiersAlreadyAdded_index.size() == 1) {
                        temp.append(classifier.getSelectedItem().toString() + " ");
                        if (classifier.getSelectedItem().toString().equals("SVM"))
                            flag_smo = true;
                        else if (classifier.getSelectedItem().toString().equals("Random Forest"))
                            flag_rf = true;
                        else if (classifier.getSelectedItem().toString().equals("Naive Bayes"))
                            flag_nb = true;
                        else if (classifier.getSelectedItem().toString().equals("Decision Tree"))
                            flag_dt = true;
                    } else {
                        temp.append(", " + classifier.getSelectedItem().toString() + " ");
                        if (classifier.getSelectedItem().toString().equals("SVM"))
                            flag_smo = true;
                        else if (classifier.getSelectedItem().toString().equals("Random Forest"))
                            flag_rf = true;
                        else if (classifier.getSelectedItem().toString().equals("Naive Bayes"))
                            flag_nb = true;
                        else if (classifier.getSelectedItem().toString().equals("Decision Tree"))
                            flag_dt = true;
                    }
                    classifiersAlreadyAdded_index.add(position);
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button reset_btn = (Button) findViewById(R.id.button_reset);
        reset_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView temp = (TextView) findViewById(R.id.textView_SelectedClassifier);
                temp.setText("Selected Classifiers: ");
                classifiersAlreadyAdded_index.clear();
                classifiersAlreadyAdded_index.add(0);
                flag_dt = false;
                flag_nb = false;
                flag_rf = false;
                flag_smo = false;
            }
        });
        final Intent smo = new Intent(this, SMOHelper.class);
        final Intent nb = new Intent(this, NaivebayesHelper.class);
        final Intent rf = new Intent(this, RandomForestHelper.class);
        final Intent dt = new Intent(this, DecisionTreeHelper.class);

        Button next_btn = (Button) findViewById(R.id.button_next);
        next_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    reader = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory() + "/Android/Data/Project_Group10/abalone.arff"));
                    Instances data = new Instances(reader);
                    data.setClassIndex(data.numAttributes() - 1);
                    data.randomize(new java.util.Random(0));
                    if(trainpercentage.getText().toString().equals(""))
                        Trainpercent = 70.0f;
                    int trainSize = (int) Math.round(data.numInstances() * (Trainpercent/100));
                    int testSize = data.numInstances() - trainSize;
                    Instances train = new Instances(data, 0, trainSize);
                    Instances test = new Instances(data, trainSize, testSize);

                    ArffSaver saver = new ArffSaver();
                    saver.setInstances(train);
                    saver.setFile(new File(Environment.getExternalStorageDirectory() + "/Android/Data/Project_Group10/train.arff"));
                    saver.setDestination(new File(Environment.getExternalStorageDirectory() + "/Android/Data/Project_Group10/train.arff"));   // **not** necessary in 3.5.4 and later
                    saver.writeBatch();

                    saver.setInstances(test);
                    saver.setFile(new File(Environment.getExternalStorageDirectory() + "/Android/Data/Project_Group10/test.arff"));
                    saver.setDestination(new File(Environment.getExternalStorageDirectory() + "/Android/Data/Project_Group10/test.arff"));   // **not** necessary in 3.5.4 and later
                    saver.writeBatch();
                }
                catch (Exception e){

                }
                editor.putBoolean("flag_smo", flag_smo);
                editor.putBoolean("flag_nb", flag_nb);
                editor.putBoolean("flag_rf", flag_rf);
                editor.putBoolean("flag_dt", flag_dt);
                editor.commit();

                editor_output.putBoolean("flag_smo", flag_smo);
                editor_output.putBoolean("flag_nb", flag_nb);
                editor_output.putBoolean("flag_rf", flag_rf);
                editor_output.putBoolean("flag_dt", flag_dt);
                editor_output.putFloat("trainpercent",Trainpercent);
                editor_output.commit();

                if (flag_smo)
                    startActivity(smo);
                else if (flag_nb)
                    startActivity(nb);
                else if (flag_rf)
                    startActivity(rf);
                else if (flag_dt)
                    startActivity(dt);
                else
                    Toast.makeText(MainActivity.this, "Please select atleast one classifier", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case 1:
            {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {

                }
                else
                {


                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    public void createfolder(){ //creating folder if it does not exist
        try {
            File folder = new File(Environment.getExternalStorageDirectory() + "/Android/Data/Project_Group10");
            boolean success = true;
            if (!folder.exists())
            {
                success = folder.mkdirs();
            }
            if (success)
            {

            }
            else
            {

            }
        }
        catch(Exception e){
            Toast.makeText(MainActivity.this,"Cannot create folder", Toast.LENGTH_SHORT).show();
        }
    }

}
