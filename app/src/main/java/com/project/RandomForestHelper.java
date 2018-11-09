package com.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RandomForestHelper extends MainActivity {
    Button defaulbtn;
    Button nextbtn;

    EditText bagsizepercenttxt_rf;
    EditText batchsizetxt_rf;
    EditText maxdepthtxt_rf;
    EditText numdecimalplacestxt_rf;
    EditText numexecutionslotstxt_rf;
    EditText numfeaturestxt_rf;
    EditText numiterationstxt_rf;
    EditText seedtxt_rf;

    private boolean flag_smo = false;
    private boolean flag_nb = false;
    private boolean flag_dt = false;
    String android_id;
    String RF;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_randomforest);

        android_id = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        final Intent smo = new Intent(this, SMOHelper.class);
        final Intent nb = new Intent(this, NaivebayesHelper.class);
        final Intent dt = new Intent(this, DecisionTreeHelper.class);

        SharedPreferences pref = getSharedPreferences("Flags", 0);
        SharedPreferences.Editor editor = pref.edit();
        flag_nb = pref.getBoolean("flag_nb", false);
        flag_dt = pref.getBoolean("flag_dt", false);
        flag_smo = pref.getBoolean("flag_smo", false);

        editor.putBoolean("flag_rf", false);
        editor.commit();

        SharedPreferences pref2 = getSharedPreferences("Final_Strings", 0);
        final SharedPreferences.Editor editor2 = pref2.edit();

        bagsizepercenttxt_rf = (EditText) findViewById(R.id.bagsizepercent_rf);
        batchsizetxt_rf = (EditText) findViewById(R.id.batchsize_rf);
        maxdepthtxt_rf = (EditText) findViewById(R.id.maxdepth_rf);
        numdecimalplacestxt_rf = (EditText) findViewById(R.id.numdecimalplaces_rf);
        numexecutionslotstxt_rf = (EditText) findViewById(R.id.numexecutionslots_rf);
        numfeaturestxt_rf = (EditText) findViewById(R.id.numfeatures_rf);
        numiterationstxt_rf = (EditText) findViewById(R.id.numiterations_rf);
        seedtxt_rf = (EditText) findViewById(R.id.seed_rf);

        defaulbtn = (Button) findViewById(R.id.DefaultRF);
        defaulbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                defaultvalues();
            }
        });

        nextbtn = (Button) findViewById(R.id.NextRF);
        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bagsizepercenttxt_rf.getText().toString().equals(""))
                    bagsizepercenttxt_rf.setText("100");
                if (batchsizetxt_rf.getText().toString().equals(""))
                    batchsizetxt_rf.setText("101");
                if (maxdepthtxt_rf.getText().toString().equals(""))
                    maxdepthtxt_rf.setText("0");
                if (numdecimalplacestxt_rf.getText().toString().equals(""))
                    numdecimalplacestxt_rf.setText("3");
                if (numexecutionslotstxt_rf.getText().toString().equals(""))
                    numexecutionslotstxt_rf.setText("1");
                if (numfeaturestxt_rf.getText().toString().equals(""))
                    numfeaturestxt_rf.setText("0");
                if (numiterationstxt_rf.getText().toString().equals(""))
                    numiterationstxt_rf.setText(("100"));
                if (seedtxt_rf.getText().toString().equals(""))
                    seedtxt_rf.setText(("1"));

                RF = "java#-cp#weka.jar#"
                        + "weka.classifiers.trees.RandomForest#-P#" + batchsizetxt_rf.getText()
                        + "#-I#" + numiterationstxt_rf.getText()
                        + "#-num-slots#" + numexecutionslotstxt_rf.getText()
                        + "#-K#" + numfeaturestxt_rf.getText()
                        + "#-M#1.0#-V#0.001"
                        + "#-S#" + seedtxt_rf.getText()
                        + "#-num-decimal-places#" + numdecimalplacestxt_rf.getText()
                        + "#-batch-size#" + batchsizetxt_rf.getText()
                        + "#-t#train.arff#-d#" + android_id + "_RF.model" ;

                editor2.putString("RF", RF);

                Toast.makeText(RandomForestHelper.this, RF, Toast.LENGTH_SHORT).show();

                if (flag_smo)
                    startActivity(smo);
                else if (flag_nb)
                    startActivity(nb);
                else if (flag_dt)
                    startActivity(dt);
                SharedPreferences pref = getSharedPreferences("Flags_output", 0);
                boolean flag_rf_upload = pref.getBoolean("flag_rf", false);
                if(flag_rf_upload)
                {
                    AsyncUploadFile upTask1 = new AsyncUploadFile();
                    upTask1.execute();
                    if(!flag_smo && !flag_dt && !flag_nb){
                        Intent intent_output = new Intent(RandomForestHelper.this, OutputHelper.class);
                        Toast.makeText(RandomForestHelper.this, "Uploading data...",Toast.LENGTH_LONG).show();
                        Toast.makeText(RandomForestHelper.this, "Upload Successful",Toast.LENGTH_SHORT).show();
                        startActivity(intent_output);
                    }
                }


            }
        });
    }

    private class AsyncUploadFile extends AsyncTask<Void, String, Void>
    {
        int flag = 0;
        //ProgressDialog waitDialog;
        //strings to create multipart format message
        String nl = "\r\n";
        String hyphen = "--";
        String boundary =  "*****";



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Void doInBackground(Void... params) {
            try
            {


                File file = new File(String.valueOf(Environment.getExternalStorageDirectory() + "/Android/Data/Project_Group10/train.arff"));

                URL upPath = new URL("http://10.0.2.2:8080/generateModel");



                HttpURLConnection urlConnect = (HttpURLConnection) upPath.openConnection(); //create http object
                urlConnect.setDoOutput(true);   //flag to set server to be written to using output stream
                urlConnect.setDoInput(true);
                urlConnect.setUseCaches(false);
                urlConnect.setRequestMethod("POST");    //send data using POST
                urlConnect.setRequestProperty("Connection", "Keep-Alive");  //keep connection without disconnecting
                urlConnect.setRequestProperty("ENCTYPE", "multipart/form-data");    //send multipart format data to server
                urlConnect.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                urlConnect.setRequestProperty("command",RF );   //To be sent to php code for setting file path
                urlConnect.setRequestProperty("outputFileName", android_id + "_RF.model");
                urlConnect.setRequestProperty("arffFile", String.valueOf(file));
                DataOutputStream upStream = new DataOutputStream(urlConnect.getOutputStream());

                upStream.writeBytes(hyphen + boundary + nl);
                upStream.writeBytes("Content-Disposition: form-data; name=\"outputFileName\""+nl+nl);
                upStream.writeBytes(android_id + "_RF.model");
                upStream.writeBytes(nl);

                upStream.writeBytes(hyphen + boundary + nl);
                upStream.writeBytes("Content-Disposition: form-data; name=\"command\""+nl+nl);
                upStream.writeBytes(RF);
                upStream.writeBytes(nl);

                //open output stream in server
                upStream.writeBytes(hyphen + boundary + nl);    //start the message with boundary
                upStream.writeBytes("Content-Disposition: form-data; name=\"arffFile\";filename=\"" + "train.arff" + "\""+nl);   //write the type of data with filename and path
                upStream.writeBytes(nl); //new line which is followed by the actual file contents

                FileInputStream fStream = new FileInputStream(file);
                DataInputStream readStream = new DataInputStream(fStream);

                int length;
                byte[] buffer = new byte[4096];
                while ((length = readStream.read(buffer)) != -1) {
                    upStream.write(buffer, 0, length);
                }

                //after file is written, finish the message with boundary
                upStream.writeBytes(nl);
                upStream.writeBytes(hyphen + boundary + hyphen + nl);


                //To check success response from server
                if(urlConnect.getResponseCode() == 200)
                {
                    flag = 1;
                }

                //close streams
                readStream.close();
                fStream.close();
                upStream.close();

            }

            catch (MalformedURLException ex)
            {
                flag = 2;
                publishProgress(ex.getMessage());
                Log.e("Debug", "error: " + ex.getMessage(), ex);
            } catch (FileNotFoundException nf)
            {
                flag = 2;
                publishProgress(nf.getMessage());
            } catch (IOException ioe)
            {
                Log.e("Debug", "error: " + ioe.getMessage(), ioe);
                flag = 2;
                publishProgress(ioe.getMessage());
            }


            return null;
        }


        protected void onProgressUpdate(String... value) {
            super.onProgressUpdate(value);
            //display message if there is any exception
            if(flag == 2)
                Toast.makeText(RandomForestHelper.this, "ERROR: "+ value[0], Toast.LENGTH_LONG).show();
        }


        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //display if upload is success
            if(flag == 1)
                Toast.makeText(RandomForestHelper.this, "Upload completed", Toast.LENGTH_SHORT).show();

        }
    }

    public void defaultvalues() {
        bagsizepercenttxt_rf.setText("100");
        batchsizetxt_rf.setText("100");
        maxdepthtxt_rf.setText("0");
        numdecimalplacestxt_rf.setText("2");
        numexecutionslotstxt_rf.setText("1");
        numfeaturestxt_rf.setText("0");
        numiterationstxt_rf.setText("100");
        seedtxt_rf.setText("1");
    }
}
