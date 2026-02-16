package com.example.harmonia.utils;



import android.app.Application;
import androidx.lifecycle.ProcessLifecycleOwner;

public class HarmoniaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize and register the observer
        AppLifecycleObserver appLifecycleObserver = new AppLifecycleObserver(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(appLifecycleObserver);
    }
}

