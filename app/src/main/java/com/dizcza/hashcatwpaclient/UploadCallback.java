package com.dizcza.hashcatwpaclient;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadServiceBroadcastReceiver;

import java.util.Locale;

public class UploadCallback extends UploadServiceBroadcastReceiver {

    private static String getResponseStr(String callbackName, UploadInfo uploadInfo, ServerResponse serverResponse) {
        String message = callbackName;
        if (uploadInfo != null) {
            message += String.format(Locale.getDefault(),
                    "Uploaded %s files -- total %d kb\n",
                    uploadInfo.getSuccessfullyUploadedFiles().toString(),
                    uploadInfo.getUploadedBytes() / 1000);
        }
        if (serverResponse != null) {
            message += String.format(Locale.getDefault(),
                    "Response %d %s",
                    serverResponse.getHttpCode(),
                    serverResponse.getBodyAsString());
        }
        return message;
    }

    private static void toastShowResponse(Context context, ServerResponse serverResponse) {
        if (serverResponse != null) {
            String message = String.format(Locale.getDefault(), "%d %s", serverResponse.getHttpCode(), serverResponse.getBodyAsString());
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
        Log.e(MainActivity.TAG, getResponseStr("onError", uploadInfo, serverResponse), exception);
        toastShowResponse(context, serverResponse);
    }

    @Override
    public void onCompleted(final Context context, final UploadInfo uploadInfo, final ServerResponse serverResponse) {
        Log.d(MainActivity.TAG, getResponseStr("onCompleted", uploadInfo, serverResponse));
        toastShowResponse(context, serverResponse);
    }

    @Override
    public void onCancelled(final Context context, final UploadInfo uploadInfo) {
        Log.d(MainActivity.TAG, getResponseStr("onCanceled", uploadInfo, null));
        Toast.makeText(context, "onCanceled", Toast.LENGTH_SHORT).show();
    }

}