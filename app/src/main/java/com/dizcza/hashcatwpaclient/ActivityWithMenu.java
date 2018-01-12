package com.dizcza.hashcatwpaclient;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public abstract class ActivityWithMenu extends AppCompatActivity {


    class ResponseNotifierCallback implements Callback {

        @Override
        public void onFailure(Call call, final IOException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toastShowException(e);
                }
            });
        }

        @Override
        public void onResponse(Call call, final Response response) throws IOException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        toastShowResponse(response);
                    }
                    catch (IOException e) {
                        toastShowException(e);
                    }
                }
            });
        }
    }


    protected void toastShowResponse(Response response) throws IOException {
        String message = String.format(Locale.getDefault(), "%d %s", response.code(), response.body().string());
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.d(MainActivity.TAG, response.toString());
    }

    protected void toastShowException(Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
