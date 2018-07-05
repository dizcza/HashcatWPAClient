package com.dizcza.hashcatwpaclient;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by dizcza on 11/4/17.
 */

public class Utils {
    private static final List<String> endpointsNoAuth = Arrays.asList("/auth", "//");

    public static String getEditTextValue(Activity activity, int resId) {
        EditText editText = activity.findViewById(resId);
        return editText.getText().toString();
    }

    public static String getRadioGroupCheckedText(Activity activity, int resId) {
        RadioGroup radioGroup = activity.findViewById(resId);
        RadioButton checkedButton = activity.findViewById(radioGroup.getCheckedRadioButtonId());
        return checkedButton.getText().toString();
    }

    public static String buildUrl(Context context, String... endPoints) {
        SharedPreferences sharedPref = getSharedPref(context);
        String serverUrl = sharedPref.getString(Constants.SERVER_URL_SHARED_KEY, "");
        String port = sharedPref.getString(Constants.PORT_SHARED_KEY, "");
        return String.format(Locale.getDefault(), "%s:%s/%s", serverUrl, port, TextUtils.join("/", endPoints));
    }

    public static SharedPreferences getSharedPref(Context context) {
        return context.getSharedPreferences(Constants.SHARED_KEY_FILE, Context.MODE_PRIVATE);
    }

    public static OkHttpClient buildClient(Context context) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        final TokenAuthenticator authenticator = new TokenAuthenticator(context);
        clientBuilder.authenticator(authenticator);
        clientBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(@NonNull Chain chain) throws IOException {
                Request request = chain.request();
                Log.d(MainActivity.TAG, "intercept " + request.url());
                if (!endpointsNoAuth.contains(request.url().encodedPath())) {
                    request = request.newBuilder()
                            .header("Authorization", authenticator.getCredential())
                            .build();
                }
                Response response = chain.proceed(request);
                return response;
            }
        });
        return clientBuilder.build();
    }

}
