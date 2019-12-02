package com.example.loggertest;

import android.app.Application;

public class LoggerApplication extends Application {
    public LoggerApplication() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Z1Logger.configureLogbackDirectly(this);
    }
}
