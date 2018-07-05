package com.dizcza.hashcatwpaclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import okhttp3.Authenticator;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

/**
 * Created by dizcza on 11/5/17.
 */

public class TokenAuthenticator implements Authenticator {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final Context mContext;
    private final OkHttpClient mTokenClient;

    TokenAuthenticator(Context context) {
        mContext = context;
        mTokenClient = new OkHttpClient();
    }

    public String getCredential() {
        try {
            String token = refreshToken();
            return String.format("JWT %s", token);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String refreshToken() throws IOException, JSONException {
        Request requestAuth = createAuthRequest(mContext);
        Response responseAuth = mTokenClient.newCall(requestAuth).execute();
        JSONObject jsonObject = new JSONObject(responseAuth.body().string());
        String token = jsonObject.getString("access_token");
        return token;
    }

    public static Request createAuthRequest(Context context) {
        SharedPreferences sharedPref = Utils.getSharedPref(context);
        String urlAuth = Utils.buildUrl(context, "auth");
        String username = sharedPref.getString(Constants.USERNAME_SHARED_KEY, "");
        String password = sharedPref.getString(Constants.PASSWORD_SHARED_KEY, "");
        String cred = username + ':' + password;
        RequestBody authBody = RequestBody.create(JSON, cred);
        Request requestAuth = new Request.Builder()
                .url(urlAuth)
                .post(authBody)
                .build();
        return requestAuth;
    }

    @Nullable
    @Override
    public Request authenticate(@NonNull Route route, @NonNull Response response) throws IOException {
        Log.d(MainActivity.TAG, "401 authenticate " + route.toString());
        String mCredential = "";
        try {
            String token = refreshToken();
            mCredential = String.format("JWT %s", token);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        Request requestAuthorized = response.request().newBuilder()
                .header("Authorization", mCredential)
                .build();
        return requestAuthorized;
    }
}