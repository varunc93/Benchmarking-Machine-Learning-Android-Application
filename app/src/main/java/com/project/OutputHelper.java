package com.project;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Center;

public class OutputHelper extends MainActivity {

    ScrollView scrollView;
    Button download_btn;
    Button viewlog_btn;
    TextView log;
    BufferedReader reader, reader_train, reader_test;

    private boolean flag_smo = false;
    private boolean flag_nb = false;
    private boolean flag_rf = false;
    private boolean flag_dt = false;
    private float trainpercent = 0;
    private int count_view = 0;

    private double tar_smo = 0;
    private double trr_smo = 0;
    private double far_smo = 0;
    private double frr_smo = 0;

    private double tar_nb = 0;
    private double trr_nb = 0;
    private double far_nb = 0;
    private double frr_nb = 0;

    private double tar_rf = 0;
    private double trr_rf = 0;
    private double far_rf = 0;
    private double frr_rf = 0;
    boolean download_smo = false;
    boolean download_dt = false;
    boolean download_rf = false;
    boolean download_nb = false;

    private double tar_dt = 0;
    private double trr_dt = 0;
    private double far_dt = 0;
    private double frr_dt = 0;
    private int files_downloaded = 0;
    private StringBuilder logtext = new StringBuilder();
    File file1;

    String android_id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output);


        android_id = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);

        scrollView = (ScrollView) findViewById(R.id.scrollViewoutput);
        download_btn = (Button) findViewById(R.id.button_download);
        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.outputmodels);
        viewlog_btn = (Button) findViewById(R.id.viewlog);

        SharedPreferences pref = getSharedPreferences("Flags_output", 0);
        flag_smo = pref.getBoolean("flag_smo", false);
        flag_nb = pref.getBoolean("flag_nb", false);
        flag_rf = pref.getBoolean("flag_rf", false);
        flag_dt = pref.getBoolean("flag_dt", false);
        trainpercent = pref.getFloat("trainpercent", 70.0f);


        if(flag_smo) {
            count_view++;
            download_smo = true;
        }
        if(flag_nb) {
            count_view++;
            download_nb = true;
        }
        if(flag_rf) {
            count_view++;
            download_rf = true;
        }
        if(flag_dt)
            count_view++;
            download_dt = true;

        download_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Async Task
                linearLayout.removeAllViews();
                try {
                    Toast.makeText(OutputHelper.this, "Downloading...",Toast.LENGTH_SHORT).show();

                        if(flag_smo && download_smo) {
                            file1 = new File(String.valueOf(Environment.getExternalStorageDirectory() + "/Android/Data/Project_Group10/" + android_id + "_SMO.model"));
                            if (!file1.exists()) {
                                AsyncDownloadFile downTask = new AsyncDownloadFile();
                                downTask.execute("_SMO.model");
                                download_smo = false;
                            }
                        }
                        if(flag_dt && download_dt) {
                            file1 = new File(String.valueOf(Environment.getExternalStorageDirectory() + "/Android/Data/Project_Group10/" + android_id + "_DT.model"));
                            if (!file1.exists()) {
                                AsyncDownloadFile downTask = new AsyncDownloadFile();
                                downTask.execute("_DT.model");

                                download_dt = false;
                            }
                        }
                        if(flag_nb && download_nb) {
                            file1 = new File(String.valueOf(Environment.getExternalStorageDirectory() + "/Android/Data/Project_Group10/" + android_id + "_NB.model"));
                            if (!file1.exists()) {
                                AsyncDownloadFile downTask = new AsyncDownloadFile();
                                downTask.execute("_NB.model");
                                download_nb = false;
                            }
                        }
                        if(flag_rf && download_rf) {
                            file1 = new File(String.valueOf(Environment.getExternalStorageDirectory() + "/Android/Data/Project_Group10/" + android_id + "_RF.model"));
                            if (!file1.exists()) {
                                AsyncDownloadFile downTask = new AsyncDownloadFile();
                                downTask.execute("_RF.model");
                                download_rf = false;
                            }
                        }

                    reader = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory() + "/Android/Data/Project_Group10/abalone.arff"));
                    Instances data = new Instances(reader);
                    data.setClassIndex(data.numAttributes() - 1);
                    data.randomize(new java.util.Random(0));

                    int trainSize = (int) Math.round(data.numInstances() * (trainpercent/100));
                    int testSize = data.numInstances() - trainSize;
                    Instances train = new Instances(data, 0, trainSize);
                    Instances test = new Instances(data, trainSize, testSize);

                    if (flag_smo) {
                        Classifier class_SMO = (Classifier) weka.core.SerializationHelper.read(Environment.getExternalStorageDirectory()
                                + "/Android/Data/Project_Group10/" + android_id + "_SMO.model");
                        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                        Evaluation eval = new Evaluation(train);
                        long startTime = System.currentTimeMillis();
                        eval.evaluateModel(class_SMO, test);
                        long endTime = System.currentTimeMillis();
                        long totalExecutionTime = endTime - startTime;

                        tar_smo = eval.weightedTruePositiveRate();
                        trr_smo = eval.weightedTrueNegativeRate();
                        far_smo = eval.weightedFalsePositiveRate();
                        frr_smo = eval.weightedFalseNegativeRate();

                        TextView smo_textView = new TextView(view.getContext());
                        smo_textView.setText("\nSMO: "
                                + "\n" + "  Train Percentage: " + trainpercent
                                + "\n" + "  Execution time: " + totalExecutionTime/1000 + " secs"
                                + "\n" + "  TAR: " +  tar_smo + " "
                                + "\n" + "  TRR: " +  trr_smo + " "
                                + "\n" + "  FAR: " +  far_smo + " "
                                + "\n" + "  FRR: " +  frr_smo + " "
                                + "\n" + "  HTER: " + (far_smo+frr_smo)/2);
                        LinearLayout.LayoutParams params_smo = new LinearLayout.LayoutParams
                                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        smo_textView.setLayoutParams(params_smo);
                        linearLayout.addView(smo_textView);
                        logtext.append("\n\n Timestamp:" + currentDateTimeString + " \n Classifier: SMO "
                                + "\n Train Percentage " + trainpercent
                                + "\n Execution Time " + totalExecutionTime/1000 + " secs");
                    }

                    if (flag_nb) {
                        Classifier class_NB = (Classifier) weka.core.SerializationHelper.read(Environment.getExternalStorageDirectory()
                                + "/Android/Data/Project_Group10/" + android_id + "_NB.model");
                        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                        Evaluation eval = new Evaluation(train);
                        long startTime = System.currentTimeMillis();
                        eval.evaluateModel(class_NB, test);
                        long endTime = System.currentTimeMillis();
                        long totalExecutionTime = endTime - startTime;

                        tar_nb = eval.weightedTruePositiveRate();
                        trr_nb = eval.weightedTrueNegativeRate();
                        far_nb = eval.weightedFalsePositiveRate();
                        frr_nb = eval.weightedFalseNegativeRate();


                        TextView nb_textView = new TextView(view.getContext());
                        nb_textView.setText("\nNaive Bayes: "
                                + "\n" + "  Train Percentage: " + trainpercent
                                + "\n" + "  Execution time: " + totalExecutionTime/1000 + " secs"
                                + "\n" + "  TAR: " + tar_nb + " "
                                + "\n" + "  TRR: " + trr_nb + " "
                                + "\n" + "  FAR: " + far_nb + " "
                                + "\n" + "  FRR: " + frr_nb + " "
                                + "\n" + "  HTER: " + (far_nb+frr_nb)/2);
                        LinearLayout.LayoutParams params_nb = new LinearLayout.LayoutParams
                                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        nb_textView.setLayoutParams(params_nb);
                        linearLayout.addView(nb_textView);
                        logtext.append("\n\n Timestamp:" + currentDateTimeString + "\n" + " Classifier: Naive Bayes "
                                + "\n Train Percentage " + trainpercent
                                + "\n Execution Time " + totalExecutionTime/1000 + " secs");
                    }

                    if (flag_rf) {

                        Classifier class_RF = (Classifier) weka.core.SerializationHelper.read(Environment.getExternalStorageDirectory()
                                + "/Android/Data/Project_Group10/" + android_id + "_RF.model");
                        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                        Evaluation eval = new Evaluation(train);
                        long startTime = System.currentTimeMillis();
                        eval.evaluateModel(class_RF, test);
                        long endTime = System.currentTimeMillis();
                        long totalExecutionTime = endTime - startTime;


                        tar_rf = eval.weightedTruePositiveRate();
                        trr_rf = eval.weightedTrueNegativeRate();
                        far_rf = eval.weightedFalsePositiveRate();
                        frr_rf = eval.weightedFalseNegativeRate();

                        TextView rf_textView = new TextView(view.getContext());
                        rf_textView.setText("\nRandom Forest: "
                                + "\n" + "  Train Percentage: " + trainpercent
                                + "\n" + "  Execution time: " + totalExecutionTime/1000 + " secs"
                                + "\n" + "  TAR: " + tar_rf + " "
                                + "\n" + "  TRR: " + trr_rf + " "
                                + "\n" + "  FAR: " + far_rf + " "
                                + "\n" + "  FRR: " + frr_rf + " "
                                + "\n" + "  HTER: " + (far_rf+frr_rf)/2);
                        LinearLayout.LayoutParams params_rf = new LinearLayout.LayoutParams
                                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        rf_textView.setLayoutParams(params_rf);
                        linearLayout.addView(rf_textView);
                        logtext.append("\n\n Timestamp:" + currentDateTimeString + "\n" + " Classifier: Random Forest "
                                + "\n Train Percentage " + trainpercent
                                + "\n Execution Time " + totalExecutionTime/1000 + " secs");
                    }

                    if (flag_dt) {
                        Classifier class_DT = (Classifier) weka.core.SerializationHelper.read(Environment.getExternalStorageDirectory()
                                + "/Android/Data/Project_Group10/" + android_id + "_DT.model");
                        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                        Evaluation eval = new Evaluation(train);
                        long startTime = System.currentTimeMillis();
                        eval.evaluateModel(class_DT, test);
                        long endTime = System.currentTimeMillis();
                        long totalExecutionTime = endTime - startTime;



                        tar_dt = eval.weightedTruePositiveRate();
                        trr_dt = eval.weightedTrueNegativeRate();
                        far_dt = eval.weightedFalsePositiveRate();
                        frr_dt = eval.weightedFalseNegativeRate();


                        TextView dt_textView = new TextView(view.getContext());
                        dt_textView.setText("\nDecision Tree: "
                                + "\n" + "  Train Percentage: " + trainpercent
                                + "\n" + "  Execution time: " + totalExecutionTime/1000 + " secs"
                                + "\n" + "  TAR: " +  tar_dt + " "
                                + "\n" + "  TRR: " +  trr_dt + " "
                                + "\n" + "  FAR: " +  far_dt + " "
                                + "\n" + "  FRR: " +  frr_dt + " "
                                + "\n" + "  HTER: " + (far_dt+frr_dt)/2);
                        LinearLayout.LayoutParams params_dt = new LinearLayout.LayoutParams
                                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        dt_textView.setLayoutParams(params_dt);
                        linearLayout.addView(dt_textView);
                        logtext.append("\n\n Timestamp:" + currentDateTimeString + "\n Classifier: Decision Tree "
                                + "\n Train Percentage " + trainpercent
                                + "\n Execution Time " + totalExecutionTime/1000 + " secs");

                    }

                } catch (Exception e) {

                } finally {
                }
            }
        });

        viewlog_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setContentView(R.layout.activity_log);
                log = (TextView) findViewById(R.id.viewlogtext);
                log.setText("\n" + logtext);
                generateNoteOnSD(OutputHelper.this,"log.txt", logtext.toString());
                Toast.makeText(OutputHelper.this,"Log file created and exported in /Android/Data/Project_Group10/",Toast.LENGTH_LONG).show();
            }
        });
    }

    public void generateNoteOnSD(Context context, String sFileName, String sBody) {
        try {
            System.getProperty("line.separator");
            File root = new File(Environment.getExternalStorageDirectory(), "/Android/Data/Project_Group10/");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);

            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();

            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Download
    private class AsyncDownloadFile extends AsyncTask<String, String, Void>  //returns exception strings to onProgressUpdate
    {
        boolean flag = false;   //flag to check for any exception
        ProgressDialog waitDialog;  //progress dialog to disable activity during download
        String phpPath = "http://10.0.2.2:8080/generateModel";

        String UploadfileName = android_id+"_SMO.model";

        String downloadDir = Environment.getExternalStorageDirectory() + "/Android/Data/Project_Group10/";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            waitDialog = ProgressDialog.show(OutputHelper.this,"","Wait until download finishes",true);
        }

        protected Void doInBackground(String... params) {
            String modelName = params[0];
            String serverPath = "http://10.0.2.2:8080/downloadFile?fileName="+android_id+modelName;
            String fileName = android_id+modelName;
            try {

                File downloadedFile1 = new File(downloadDir);   //create SD path based on device with /CSE535_ASSIGNMENT2_Extra/ folder
                if(!downloadedFile1.isDirectory())
                    downloadedFile1.mkdir();    //create directory if none exists
                File downloadedFile = new File(downloadedFile1+"/"+fileName);  //create complete path for file
                URL urlPath = new URL(serverPath);  //create URL object of server path
                HttpURLConnection urlConnect = (HttpURLConnection) urlPath.openConnection();    //create object to establish http connection
                int contentLength = urlConnect.getContentLength();  //get length of the file to be downloaded
                DataInputStream iStream = new DataInputStream(urlPath.openStream());    //new input stream to save buffer of downloaded file
                byte[] buffer = new byte[contentLength];    //buffer size is size of file


                int length;
                FileOutputStream fStream = new FileOutputStream(downloadedFile);    //output stream to write from buffer and save as file
                DataOutputStream oStream = new DataOutputStream(fStream);

                //transfer from input stream to output stream using buffer
                while ((length = iStream.read(buffer)) != -1) {
                    oStream.write(buffer, 0, length);
                }

                //close streams
                iStream.close();
                oStream.flush();
                oStream.close();
            }

            catch (FileNotFoundException e)
            {
                flag = true;    //flag true if there is exception
                System.out.println(e.getMessage());
                publishProgress(e.getMessage());
            }
            catch (MalformedURLException e)
            {
                flag = true;
                publishProgress(e.getMessage());
                e.printStackTrace();
            }
            catch (IOException e)
            {
                flag = true;
                publishProgress(e.getMessage());
            }
            return null;
        }

        protected void onProgressUpdate(String... value) {
            super.onProgressUpdate(value);
            //show message if there exception flag is set
            if(flag)
                Toast.makeText(OutputHelper.this, "ERROR:  "+value[0], Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            waitDialog.dismiss();   //remove dialog after download completes

            //show message if download is successful
            if(!flag)
                Toast.makeText(OutputHelper.this, "Download completed", Toast.LENGTH_SHORT).show();



        }
    }
}
