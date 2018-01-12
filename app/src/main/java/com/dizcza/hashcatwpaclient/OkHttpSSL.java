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
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Authenticator;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

/**
 * Created by dizcza on 10/2/17.
 */

public class OkHttpSSL {

    public static OkHttpClient getSSLSelfSignedClient(final Context context, boolean withAuthenticator)
            throws
            NoSuchAlgorithmException,
            KeyStoreException,
            KeyManagementException,
            CertificateException,
            IOException {
        // todo reuse trustManager and sslFactory for both clients
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(readKeyStore(context));
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:"
                    + Arrays.toString(trustManagers));
        }
        X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                // fixme: remove when get trusted certificate
                Log.d("hostname", hostname);
//                return hostname.equals(MainActivity.HOST);
                return true;
            }
        };

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustManager)
                .hostnameVerifier(hostnameVerifier);

        if (withAuthenticator) {
            clientBuilder.authenticator(new TokenAuthenticator(context));
        }

        OkHttpClient client = clientBuilder.build();
        return client;
    }

    /**
     * Get keys store. Key file should be encrypted with pkcs12 standard.
     *
     * @param context Activity or some other context.
     * @return Keys store.
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    private static KeyStore readKeyStore(Context context)
            throws
            KeyStoreException,
            NoSuchAlgorithmException,
            IOException,
            CertificateException {
        SharedPreferences sharedPref = Utils.getSharedPref(context);
        char[] keystorePass = sharedPref.getString(Constants.KEYSTORE_SHARED_KEY, "").toCharArray();
        ArrayList<InputStream> certificates = new ArrayList<>();
        certificates.add(context.getResources().openRawResource(R.raw.certificate_aws));
        KeyStore keyStoreP12 = KeyStore.getInstance("pkcs12");
        for (int certId = 0; certId < certificates.size(); certId++) {
            try (InputStream certificate = certificates.get(certId)) {
                keyStoreP12.load(certificate, keystorePass);
            }
        }
        return keyStoreP12;
    }

    public static boolean canReadKeyStore(Context context) {
        try {
            readKeyStore(context);
            return true;
        } catch ( KeyStoreException | NoSuchAlgorithmException | IOException | CertificateException e) {
            e.printStackTrace();
            return false;
        }
    }

}
