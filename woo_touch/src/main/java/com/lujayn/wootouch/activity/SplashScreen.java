package com.lujayn.wootouch.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.messaging.FirebaseMessaging;
import com.lujayn.wootouch.R;
import com.lujayn.wootouch.bean.SettingOption;
import com.lujayn.wootouch.common.AppConstant;
import com.lujayn.wootouch.common.AppConstant.HTTPResponseCode;
import com.lujayn.wootouch.common.AppConstant.HttpRequestType;
import com.lujayn.wootouch.common.ApplicationData;

import com.lujayn.wootouch.common.SessionManager;
import com.lujayn.wootouch.common.SharedPrefManager;
import com.lujayn.wootouch.common.Webservice;
import com.lujayn.wootouch.custom.AppDebugLog;
import com.lujayn.wootouch.database.DataWrite;
import com.lujayn.wootouch.network.RequestTask;
import com.lujayn.wootouch.network.RequestTaskDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import static com.lujayn.wootouch.Utility.checkConnectivity;


/**
 * Created by Shailesh on 25/07/16.
 */
public class SplashScreen extends AppCompatActivity implements RequestTaskDelegate {
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSIONS_REQUEST = 0;
    SessionManager manager;
    DataWrite dataWrite;
    ImageView ivSplashLogo;
    String color_back;
    LinearLayout linearLayout;
    private ApplicationData appData;
    CountDownTimer lTimer;
    private ProgressDialog pdialog;
    ProgressBar progressBar;
    public static final String TOPIC_GLOBAL = "global";
    Activity activity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        activity = this;
        setStatusBarTranslucent(true);
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_GLOBAL);
        initialize();
    }

    private void initialize() {
        appData = ApplicationData.getSharedInstance();
        ivSplashLogo = (ImageView) findViewById(R.id.ivSplashLogo);
        linearLayout = (LinearLayout) findViewById(R.id.llSplash);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        dataWrite = new DataWrite(this);
        dataWrite.open();
        manager = new SessionManager();
        Webservice.BASE_URL = manager.getPreferences(SplashScreen.this, AppConstant.TRIAL_URL);

        dataWrite.clearDatabase();
        getPermissionToWriteStorage();
        if (checkConnectivity()) {
            sendTokenToServer();
        } else {
            Toast.makeText(activity, "There is no network connection at the moment.Try again later", Toast.LENGTH_SHORT).show();
        }

    }


    public void getPermissionToWriteStorage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_EXTERNAL_STORAGE_PERMISSIONS_REQUEST);
            }
        } else {
            //setLogo();
            if (checkConnectivity()) {
                settingOption("option");
            } else {
                Toast.makeText(activity, "There is no network connection at the moment.Try again later", Toast.LENGTH_SHORT).show();
            }

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == WRITE_EXTERNAL_STORAGE_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //dataWrite.showToast("Permission granted");

                //setLogo();
                settingOption("option");

            } else {
                SplashScreen.this.finish();
                // dataWrite.showToast("Permission denied");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    private void settingOption(String option) {
        progressBar.setVisibility(View.VISIBLE);

        String requestURL = Webservice.BASE_URL + "" + Webservice.URL_SETTING_OPTION;
        AppDebugLog.println("requestURL in sendNewDataRequest : " + requestURL);
        RequestTask requestTask = new RequestTask(requestURL, AppConstant.HttpRequestType.DataUpdateRequest);
        requestTask.delegate = this;
        requestTask.execute(requestURL, option);
    }

    private void setLogo() {
        SettingOption settingOption = new SettingOption();
        settingOption = manager.getPreferencesOption(SplashScreen.this, AppConstant.SETTING_OPTION);
        String image = settingOption.getData().getOptions().getSplash_screen();
        Log.d("image", image);
        Glide.with(this).load(image)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivSplashLogo);
        boolean isInternet = appData.getConnectionDetector().isConnectingToInternet();
        if (isInternet) {

            //getCountry("country");

            Thread background = new Thread() {
                public void run() {
                    try {
                        String status = manager.getPreferences(SplashScreen.this, AppConstant.STATUS);
                        //String status_login = manager.getPreferences(SplashScreen.this,AppConstant.STATUS_LOGIN);
                        AppDebugLog.println("status" + status);
                        AppDebugLog.println("Splash end time ------------------: " + System.currentTimeMillis() / 1000);
                        if (status.equals("1")) {
                            Intent i = new Intent(SplashScreen.this, MainActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            dataWrite.deletedData();
                            Intent i = new Intent(SplashScreen.this, MainActivity.class);
                            startActivity(i);
                            finish();
                        }
                        progressBar.setVisibility(View.GONE);

                        //Remove activity
                    } catch (Exception e) {
                        progressBar.setVisibility(View.GONE);
                    }


                }

            };

            // start thread
            background.start();

        } else {
            startTimer();
        }

    }

    private void startTimer() {
        lTimer = new CountDownTimer(AppConstant.SPLASH_SCREEN_TIMEOUT, 100) {
            public void onFinish() {
                //closeScreen();
            }

            @Override
            public void onTick(long millisUntilFinished) {
                // TODO Auto-generated method stub
            }
        }.start();
    }


    private void getCountry(String country) {
        String requestURL = Webservice.BASE_URL + "" + Webservice.URL_COUNTRY;
        AppDebugLog.println("requestURL in sendNewDataRequest : " + requestURL);
        RequestTask requestTask = new RequestTask(requestURL, AppConstant.HttpRequestType.DataUpdateRequest);
        requestTask.delegate = this;
        requestTask.execute(requestURL, country);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
        startActivity(intent);
        finish();
        System.exit(0);
    }

    protected void setStatusBarTranslucent(boolean makeTranslucent) {
        if (makeTranslucent) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        //registerReceiver(receive, new IntentFilter(Intent_Class_Data.NOTIFICATION));

    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        // unregisterReceiver(receive);

    }

    @Override
    public void backgroundActivityComp(String response, HttpRequestType completedRequestType) {
        HTTPResponseCode responseCode = appData.getResponseCode();
        AppDebugLog.println("responseCode : " + responseCode);
        switch (responseCode) {
            case Country:
                Thread background = new Thread() {
                    public void run() {
                        try {

                            String status = manager.getPreferences(SplashScreen.this, AppConstant.STATUS);
                            //String status_login = manager.getPreferences(SplashScreen.this,AppConstant.STATUS_LOGIN);
                            AppDebugLog.println("status" + status);
                            AppDebugLog.println("Splash end time ------------------: " + System.currentTimeMillis() / 1000);
                            if (status.equals("1")) {
                                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                                startActivity(i);
                                finish();
                            } else {
                                dataWrite.deletedData();
                                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                                startActivity(i);
                                finish();
                            }
                            progressBar.setVisibility(View.GONE);

                            //Remove activity
                        } catch (Exception e) {
                            progressBar.setVisibility(View.GONE);
                        }


                    }

                };

                // start thread
                background.start();

                break;

            case Success:
                SettingOption settingOption = new SettingOption();
                settingOption = manager.getPreferencesOption(SplashScreen.this, AppConstant.SETTING_OPTION);
                color_back = settingOption.getData().getOptions().getSplash_screen_color();

                AppDebugLog.println("CatView= : " + settingOption.getData().getOptions().getCategory_view());
                linearLayout.setBackgroundColor(Color.parseColor(color_back));
                setLogo();

                break;

            case NetworkError:
                showUserAlert(this, getString(R.string.alert_title_message),
                        getString(R.string.alert_body_network_error), null);
                break;

            case ServerError:
                showUserAlert(this, getString(R.string.alert_title_message),
                        getString(R.string.alert_body_something_was_wrong), null);
                break;

            case NotiSuccess:

                break;
        }
    }

    public void showUserAlert(Context context, String title, String message, DialogInterface.OnClickListener listner) {
        new AlertDialog.Builder(context).setTitle(title).setMessage(message)
                .setPositiveButton(context.getText(R.string.btn_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        manager.setPreferences(SplashScreen.this, "finstall", "0");
                    }
                }).show();

    }

    @Override
    public void timeOut() {

    }

    @Override
    public void codeError(int code) {

    }

    @Override
    public void percentageDownloadCompleted(int percentage, Object record) {

    }

    private void showProgressDialog(String title, String msg) {
        if (pdialog == null) {
            pdialog = ProgressDialog.show(this, null, null);
            pdialog.getWindow().setGravity(Gravity.BOTTOM);
            pdialog.setCancelable(false);
        }
    }

    /**
     * Stop progress dialog
     */
    private void cancelProgressDialog() {
        if (pdialog != null) {
            pdialog.dismiss();
            pdialog = null;
        }
    }

    private void sendTokenToServer() {
        final String token = SharedPrefManager.getInstance(this).getDeviceToken();

        // Toast.makeText(this, token, Toast.LENGTH_LONG).show();

        JSONObject jsonObject = new JSONObject();

        try {

            jsonObject.put("token", token);
            jsonObject.put("type", "1");
            jsonObject.put("userid", "");


        } catch (JSONException e) {
            e.printStackTrace();
        }

        String requestURL = Webservice.BASE_URL + "" + Webservice.URL_NOTIFICATION;
        AppDebugLog.println("requestURL in sendNewDataRequest : " + requestURL);
        RequestTask requestTask = new RequestTask(requestURL, AppConstant.HttpRequestType.DataUpdateRequest);
        requestTask.delegate = this;
        requestTask.execute(requestURL, "noti", jsonObject.toString());
    }
}
