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

public class SMOHelper extends MainActivity {

    Button defaulbtn;
    Button nextbtn;
    EditText batchsizetxt_smo;
    EditText ctxt_smo;
    EditText epsilontxt_smo;
    EditText kerneltxt_smo;
    EditText numdecimalplacestxt_smo;
    EditText numfoldstxt_smo;
    EditText randomseedtxt_smo;
    EditText toleranceparametertxt_smo;
    EditText filtertypetxt_smo;

    private boolean flag_nb = false;
    private boolean flag_rf = false;
    private boolean flag_dt = false;
    String android_id;
    String SMO;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smo);

        android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        final Intent nb = new Intent(this, NaivebayesHelper.class);
        final Intent rf = new Intent(this, RandomForestHelper.class);
        final Intent dt = new Intent(this, DecisionTreeHelper.class);

        SharedPreferences pref = getSharedPreferences("Flags", 0);
        SharedPreferences.Editor editor = pref.edit();
        flag_nb = pref.getBoolean("flag_nb", false);
        flag_rf = pref.getBoolean("flag_rf", false);
        flag_dt = pref.getBoolean("flag_dt", false);

        editor.putBoolean("flag_smo", false);
        editor.commit();

        SharedPreferences pref2 = getSharedPreferences("Final_Strings", 0);
        final SharedPreferences.Editor editor2 = pref2.edit();

        batchsizetxt_smo = (EditText) findViewById(R.id.batchsize_svm);
        ctxt_smo = (EditText) findViewById(R.id.C_svm);
        epsilontxt_smo = (EditText) findViewById(R.id.epsilon_svm);
        kerneltxt_smo = (EditText) findViewById(R.id.kernel_svm);
        numdecimalplacestxt_smo = (EditText) findViewById(R.id.numdecimalplaces_svm);
        numfoldstxt_smo = (EditText) findViewById(R.id.numfolds_svm);
        randomseedtxt_smo = (EditText) findViewById(R.id.randomseed_svm);
        toleranceparametertxt_smo = (EditText) findViewById(R.id.toleranceparameter_svm);
        filtertypetxt_smo = (EditText) findViewById(R.id.filtertype_svm);

        defaulbtn = (Button) findViewById(R.id.DefaultSMO);
        defaulbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                defaultvalues();
            }
        });

        nextbtn = (Button) findViewById(R.id.NextSMO);
        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (batchsizetxt_smo.getText().toString().equals(""))
                    batchsizetxt_smo.setText("101");
                if (ctxt_smo.getText().toString().equals(""))
                    ctxt_smo.setText("1.0");
                if (epsilontxt_smo.getText().toString().equals(""))
                    epsilontxt_smo.setText("1.0E-12");
                if (kerneltxt_smo.getText().toString().equals(""))
                    kerneltxt_smo.setText("0");
                if (numdecimalplacestxt_smo.getText().toString().equals(""))
                    numdecimalplacestxt_smo.setText("3");
                if (filtertypetxt_smo.getText().toString().equals(""))
                    numfoldstxt_smo.setText("-1");
                if (filtertypetxt_smo.getText().toString().equals(""))
                    randomseedtxt_smo.setText("1");
                if (filtertypetxt_smo.getText().toString().equals(""))
                    toleranceparametertxt_smo.setText(".001");
                if (filtertypetxt_smo.getText().toString().equals(""))
                    filtertypetxt_smo.setText("0");

                 SMO = "java#-cp#weka.jar"
                        + "#weka.classifiers.functions.SMO"
                        + "#-batch-size#" + batchsizetxt_smo.getText()
                        + "#-num-decimal-places#" + numdecimalplacestxt_smo.getText()
                        + "#-C#" + ctxt_smo.getText() + "#-L#"
                        + toleranceparametertxt_smo.getText() + "#-P#" + epsilontxt_smo.getText()
                        + "#-N#" + filtertypetxt_smo.getText() + "#-V#" + numfoldstxt_smo.getText()
                        + "#-W#" + randomseedtxt_smo.getText()
                        + "#-K#weka.classifiers.functions.supportVector.PolyKernel "
                        + "-E 1.0 -C 250007#-calibrator#weka.classifiers.functions.Logistic "
                        + "-R 1.0E-8 -M -1 -num-decimal-places 4"
                        + "#-t#abalone.arff#-d#" + android_id + "_SMO.model";

                editor2.putString("SMO", SMO);

                Toast.makeText(SMOHelper.this, SMO, Toast.LENGTH_SHORT).show();

                if (flag_nb)
                    startActivity(nb);
                else if (flag_rf)
                    startActivity(rf);
                else if (flag_dt)
                    startActivity(dt);

                SharedPreferences pref = getSharedPreferences("Flags_output", 0);
                boolean flag_smo_upload = pref.getBoolean("flag_smo", false);
                if(flag_smo_upload)
                {
                    AsyncUploadFile upTask1 = new AsyncUploadFile();
                    upTask1.execute();
                    if(!flag_nb && !flag_dt && !flag_rf){
                        Intent intent_output = new Intent(SMOHelper.this, OutputHelper.class);
                        Toast.makeText(SMOHelper.this, "Uploading data...",Toast.LENGTH_LONG).show();
                        Toast.makeText(SMOHelper.this, "Upload Successful",Toast.LENGTH_SHORT).show();
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
                urlConnect.setRequestProperty("command",SMO );   //To be sent to php code for setting file path
                urlConnect.setRequestProperty("outputFileName", android_id + "_SMO.model");
                urlConnect.setRequestProperty("arffFile", String.valueOf(file));
                DataOutputStream upStream = new DataOutputStream(urlConnect.getOutputStream());

                upStream.writeBytes(hyphen + boundary + nl);
                upStream.writeBytes("Content-Disposition: form-data; name=\"outputFileName\""+nl+nl);
                upStream.writeBytes(android_id + "_SMO.model");
                upStream.writeBytes(nl);

                upStream.writeBytes(hyphen + boundary + nl);
                upStream.writeBytes("Content-Disposition: form-data; name=\"command\""+nl+nl);
                upStream.writeBytes(SMO);
                upStream.writeBytes(nl);

                //open output stream in server
                upStream.writeBytes(hyphen + boundary + nl);    //start the message with boundary
                upStream.writeBytes("Content-Disposition: form-data; name=\"arffFile\";filename=\"" + "abalone.arff" + "\""+nl);   //write the type of data with filename and path
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
                Toast.makeText(SMOHelper.this, "ERROR: "+ value[0], Toast.LENGTH_LONG).show();
        }


        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //display if upload is success
            if(flag == 1)
                Toast.makeText(SMOHelper.this, "Upload completed", Toast.LENGTH_SHORT).show();

        }
    }

    public void defaultvalues() {
        batchsizetxt_smo.setText("100");
        ctxt_smo.setText("1.0");
        epsilontxt_smo.setText("1.0E-12");
        kerneltxt_smo.setText("0");
        numdecimalplacestxt_smo.setText("2");
        numfoldstxt_smo.setText("-1");
        randomseedtxt_smo.setText("1");
        toleranceparametertxt_smo.setText(".001");
        filtertypetxt_smo.setText("0");
    }
}
