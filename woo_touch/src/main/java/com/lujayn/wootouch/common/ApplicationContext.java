package com.lujayn.wootouch.common;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.lujayn.wootouch.utils.TypefaceUtil;

/**
 * Created by ExT-Emp-005 on 16-03-2017.
 */

public class ApplicationContext extends MultiDexApplication {

    public static final String TAG = ApplicationContext.class.getSimpleName();
    public static Context context;
    public static ApplicationContext rest;
    private RequestQueue mRequestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        rest = this;
        context = getApplicationContext();
        TypefaceUtil.overrideFont(getApplicationContext(), "monospace", "assets/fonts/arial.ttf"); // font from assets: "assets/fonts/Roboto-Regular.ttf
    }

    public static synchronized ApplicationContext getInstance() {
        return rest;
    }

    public static Context getAppContext() {
        return ApplicationContext.context;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
/*
import android.app.Application;
import android.content.Context;


*//**
 * Created by Shailesh on 03/05/16.
 *//*
public class ApplicationContext extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        // AppDebugLog.println("Context Reference Assigned"
        // + getApplicationContext());
        ApplicationContext.context = getApplicationContext();

    }

    public static Context getAppContext() {

        return ApplicationContext.context;
    }
 *//*   @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        //MultiDex.install(this);
    }
*//*

  *//*  @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    }*//*
}*/
