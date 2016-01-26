package com.lucas.freeshots;


import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

import timber.log.Timber;

public class App extends Application {

    //public static Resources resources;

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());
//        if (BuildConfig.DEBUG) {
//            Timber.plant(new Timber.DebugTree());
//        } else {
//            Timber.plant(new CrashReportingTree());
//        }

        Fresco.initialize(this);
    }
}
