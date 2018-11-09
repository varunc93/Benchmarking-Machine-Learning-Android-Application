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

public class DecisionTreeHelper extends MainActivity {
    Button defaulbtn;
    Button nextbtn;
    EditText batchsizetxt_dt;
    EditText confidencefactor_dt;
    EditText minnumobj_dt;
    EditText numdecimalplacestxt_dt;
    EditText numfoldstxt_dt;
    EditText seed_dt;

    private boolean flag_smo = false;
    private boolean flag_nb = false;
    private boolean flag_rf = false;
    String android_id;
    String DT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decisiontree);

        android_id = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        final Intent smo = new Intent(this, SMOHelper.class);
        final Intent nb = new Intent(this, NaivebayesHelper.class);
        final Intent rf = new Intent(this, RandomForestHelper.class);

        SharedPreferences pref = getSharedPreferences("Flags", 0);
        SharedPreferences.Editor editor = pref.edit();
        flag_nb = pref.getBoolean("flag_nb", false);
        flag_rf = pref.getBoolean("flag_rf", false);
        flag_smo = pref.getBoolean("flag_smo", false);

        editor.putBoolean("flag_dt", false);
        editor.commit();


        SharedPreferences pref2 = getSharedPreferences("Final_Strings", 0);
        final SharedPreferences.Editor editor2 = pref2.edit();

        batchsizetxt_dt = (EditText) findViewById(R.id.batchsize_dt);
        confidencefactor_dt = (EditText) findViewById(R.id.confidencefactor_dt);
        minnumobj_dt = (EditText) findViewById(R.id.minnumobj_dt);
        numdecimalplacestxt_dt = (EditText) findViewById(R.id.numdecimalplaces_dt);
        numfoldstxt_dt = (EditText) findViewById(R.id.numfolds_dt);
        seed_dt = (EditText) findViewById(R.id.seed_dt);

        defaulbtn = (Button) findViewById(R.id.DefaultDT);
        defaulbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                defaultvalues();
            }
        });

        nextbtn = (Button) findViewById(R.id.NextDT);
        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (batchsizetxt_dt.getText().toString().equals(""))
                    batchsizetxt_dt.setText("100");
                if (confidencefactor_dt.getText().toString().equals(""))
                    confidencefactor_dt.setText("0.25");
                if (minnumobj_dt.getText().toString().equals(""))
                    minnumobj_dt.setText("2");
                if (numdecimalplacestxt_dt.getText().toString().equals(""))
                    numdecimalplacestxt_dt.setText("2");
                if (numfoldstxt_dt.getText().toString().equals(""))
                    numfoldstxt_dt.setText("3");
                if (seed_dt.getText().toString().equals(""))
                    seed_dt.setText("11");

                DT = "java#-cp#weka.jar#"
                        + "weka.classifiers.trees.J48"
                        + "#-C#" + confidencefactor_dt.getText()
                        + "#-M#" + minnumobj_dt.getText()
                        + "#-num-decimal-places#" + numdecimalplacestxt_dt.getText()
                        + "#-batch-size#" + batchsizetxt_dt.getText()
                        + "#-t#train.arff#-d#" + android_id + "_DT.model";

                editor2.putString("DT", DT);

                Toast.makeText(DecisionTreeHelper.this, DT, Toast.LENGTH_SHORT).show();

                if (flag_smo)
                    startActivity(smo);
                else if (flag_nb)
                    startActivity(nb);
                else if (flag_rf)
                    startActivity(rf);
                SharedPreferences pref = getSharedPreferences("Flags_output", 0);
                boolean flag_dt_upload = pref.getBoolean("flag_dt", false);
                if(flag_dt_upload)
                {
                    AsyncUploadFile upTask1 = new AsyncUploadFile();
                    upTask1.execute();
                    if(!flag_smo && !flag_nb && !flag_rf){
                        Intent intent_output = new Intent(DecisionTreeHelper.this, OutputHelper.class);
                        Toast.makeText(DecisionTreeHelper.this, "Uploading data...",Toast.LENGTH_LONG).show();
                        Toast.makeText(DecisionTreeHelper.this, "Upload Successful",Toast.LENGTH_SHORT).show();
                        startActivity(intent_output);
                    }
                }
            }
        });
    }

    private class AsyncUploadFile extends AsyncTask<Void, String, Void>
    {
        int flag = 0;

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
                urlConnect.setRequestProperty("command",DT );   //To be sent to php code for setting file path
                urlConnect.setRequestProperty("outputFileName", android_id + "_DT.model");
                urlConnect.setRequestProperty("arffFile", String.valueOf(file));
                DataOutputStream upStream = new DataOutputStream(urlConnect.getOutputStream());

                upStream.writeBytes(hyphen + boundary + nl);
                upStream.writeBytes("Content-Disposition: form-data; name=\"outputFileName\""+nl+nl);
                upStream.writeBytes(android_id + "_DT.model");
                upStream.writeBytes(nl);

                upStream.writeBytes(hyphen + boundary + nl);
                upStream.writeBytes("Content-Disposition: form-data; name=\"command\""+nl+nl);
                upStream.writeBytes(DT);
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
                Toast.makeText(DecisionTreeHelper.this, "ERROR: "+ value[0], Toast.LENGTH_LONG).show();
        }


        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //display if upload is success
            if(flag == 1)
                Toast.makeText(DecisionTreeHelper.this, "Upload completed", Toast.LENGTH_SHORT).show();

        }
    }

    public void defaultvalues() {
        batchsizetxt_dt.setText("100");
        confidencefactor_dt.setText("0.25");
        minnumobj_dt.setText("2");
        numdecimalplacestxt_dt.setText("2");
        numfoldstxt_dt.setText("3");
        seed_dt.setText("11");
    }

}
