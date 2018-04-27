package com.dizcza.hashcatwpaclient;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends PermissionManagerActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private OkHttpClient httpClient;

    private AlertDialog createSingInDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_signin, null);
        builder.setView(dialogView)
                .setPositiveButton(R.string.signin, new DialogInterface.OnClickListener() {
                    @SuppressLint("ApplySharedPref")
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences sharedPref = Utils.getSharedPref(MainActivity.this);
                        SharedPreferences.Editor editor = sharedPref.edit();

                        EditText serverUrlView = dialogView.findViewById(R.id.server_url);
                        String serverUrl = serverUrlView.getText().toString();
                        if (!serverUrl.equals(getString(R.string.http)) && serverUrl.endsWith("/")) {
                            serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
                        }
                        editor.putString(Constants.SERVER_URL_SHARED_KEY, serverUrl);

                        EditText portView = dialogView.findViewById(R.id.port);
                        String port = portView.getText().toString();
                        editor.putString(Constants.PORT_SHARED_KEY, port);

                        EditText usernameView = dialogView.findViewById(R.id.username);
                        String username = usernameView.getText().toString();
                        editor.putString(Constants.USERNAME_SHARED_KEY, username);

                        EditText passwordView = dialogView.findViewById(R.id.password);
                        String password = passwordView.getText().toString();
                        editor.putString(Constants.PASSWORD_SHARED_KEY, password);

                        editor.commit();

                        checkSingIn();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }

    private void checkSingIn() {
        Request authRequest = TokenAuthenticator.createAuthRequest(MainActivity.this);
        httpClient.newCall(authRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String url = call.request().url().toString();
                final String message = String.format(Locale.getDefault(), "Failed to sign in %s. Try again", url);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    onFailure(call, new IOException("Unexpected code " + response));
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Successfully signed in", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();

        // fixme: make async
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        initMenuButtons();
        httpClient = new OkHttpClient();
        UploadService.HTTP_STACK = new OkHttpStack(httpClient);
    }

    private void initMenuButtons() {
        final AlertDialog singInDialog = createSingInDialog();

        Button signInButton = findViewById(R.id.signin);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                singInDialog.show();
                // todo encrypt password shared pref
                SharedPreferences sharedPref = Utils.getSharedPref(MainActivity.this);

                EditText serverUrlView = singInDialog.findViewById(R.id.server_url);
                String serverUrl = sharedPref.getString(Constants.SERVER_URL_SHARED_KEY, getString(R.string.http));
                serverUrlView.setText(serverUrl);

                EditText portView = singInDialog.findViewById(R.id.port);
                String port = sharedPref.getString(Constants.PORT_SHARED_KEY, getString(R.string.port));
                portView.setText(port);

                EditText usernameView = singInDialog.findViewById(R.id.username);
                String username = sharedPref.getString(Constants.USERNAME_SHARED_KEY, null);
                if (username != null) {
                    usernameView.setText(username);
                }

                EditText passwordView = singInDialog.findViewById(R.id.password);
                String password = sharedPref.getString(Constants.PASSWORD_SHARED_KEY, null);
                if (password != null) {
                    passwordView.setText(password);
                }
            }
        });

        Button uploadButton = findViewById(R.id.upload_activity);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, UploadActivity.class), Constants.UPLOAD_REQUEST_CODE);
            }
        });

        Button benchmarkButton = findViewById(R.id.benchmark_button);
        benchmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runBenchmark();
            }
        });

        Button indexButton = findViewById(R.id.index_button);
        indexButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                indexPing();
            }
        });

        Button terminateButton = findViewById(R.id.terminate);
        terminateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                terminateWorkers();
            }
        });

        Button listKeysButton = findViewById(R.id.list_keys);
        listKeysButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listKeys();
            }
        });
    }

    private void indexPing() {
        String urlPing = Utils.buildUrl(this, "/");
        Request request = new Request.Builder()
                .url(urlPing)
                .build();
        httpClient.newCall(request).enqueue(new ResponseNotifierCallback());
    }

    private void runBenchmark() {
        String urlBenchmark = Utils.buildUrl(this, "benchmark");
        Request request = new Request.Builder()
                .url(urlBenchmark)
                .build();
        httpClient.newCall(request).enqueue(new ResponseNotifierCallback());
    }

    private void terminateWorkers() {
        String urlBenchmark = Utils.buildUrl(this, "terminate");
        Request request = new Request.Builder()
                .url(urlBenchmark)
                .build();
        httpClient.newCall(request).enqueue(new ResponseNotifierCallback());
    }

    private void listKeys() {
        String urlBenchmark = Utils.buildUrl(this, "list");
        Request request = new Request.Builder()
                .url(urlBenchmark)
                .build();
        httpClient.newCall(request).enqueue(new ResponseNotifierCallback());
    }

}
