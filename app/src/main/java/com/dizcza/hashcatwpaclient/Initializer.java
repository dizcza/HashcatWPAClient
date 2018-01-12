package com.dizcza.hashcatwpaclient;

import android.app.Application;

import net.gotev.uploadservice.Logger;
import net.gotev.uploadservice.UploadService;

public class Initializer extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // setup the broadcast action namespace string which will
        // be used to notify upload status.
        // Gradle automatically generates proper variable as below.
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;

        // Set upload service debug log messages level
        Logger.setLogLevel(Logger.LogLevel.DEBUG);
    }
}