package com.dizcza.hashcatwpaclient;

import android.app.Activity;
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
import okhttp3.Callback;
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

    private String mCredential;
    private final OkHttpClient mTokenClient;
    private final Context mContext;

    TokenAuthenticator(Context context)
            throws
            CertificateException,
            NoSuchAlgorithmException,
            KeyStoreException,
            IOException,
            KeyManagementException {
        mContext = context;
        mTokenClient = OkHttpSSL.getSSLSelfSignedClient(context, false);
    }

    public String getCredential() throws IOException {
        if (mCredential != null) {
            String urlPing = Utils.buildUrl(mContext, "ping");
            Request pingRequest = new Request.Builder()
                    .url(urlPing)
                    .header("Authorization", mCredential)
                    .build();
            Response pingResponse = mTokenClient.newCall(pingRequest).execute();
            if (pingResponse.isSuccessful()) {
                return mCredential;
            }
        }
        try {
            String token = refreshToken();
            mCredential = String.format("JWT %s", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mCredential;
    }

    private String refreshToken() throws IOException, JSONException {
        Request requestAuth = createAuthRequest();
        Response responseAuth = mTokenClient.newCall(requestAuth).execute();
        JSONObject jsonObject = new JSONObject(responseAuth.body().string());
        String token = jsonObject.getString("access_token");
        return token;
    }

    private Request createAuthRequest() {
        SharedPreferences sharedPref = Utils.getSharedPref(mContext);
        String urlAuth = Utils.buildUrl(mContext, "auth");
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

    public void authenticate(Callback authCallback) {
        mTokenClient.newCall(createAuthRequest()).enqueue(authCallback);
    }

    @Nullable
    @Override
    public Request authenticate(@NonNull Route route, @NonNull Response response) throws IOException {
        Log.d(MainActivity.TAG, "Trying to authenticate " + route.toString());
        String header = response.request().header("Authorization");
        if (mCredential == null || (header != null && header.equals(mCredential))) {
            try {
                String token = refreshToken();
                mCredential = String.format("JWT %s", token);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        Request requestAuthorized = response.request().newBuilder()
                .header("Authorization", mCredential)
                .build();
        return requestAuthorized;
    }
}