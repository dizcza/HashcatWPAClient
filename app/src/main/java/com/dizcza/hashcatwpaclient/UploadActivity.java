package com.dizcza.hashcatwpaclient;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.gotev.uploadservice.BinaryUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class UploadActivity extends ActivityWithMenu {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_layout);

        Button chooseFileButton = findViewById(R.id.choose_file);
        chooseFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performFileSearch();
            }
        });

        Button startUploadingButton = findViewById(R.id.start_upload);
        startUploadingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String capturePath = Utils.getEditTextValue(UploadActivity.this, R.id.capture_file_path);
                if (capturePath.length() == 0) {
                    Toast.makeText(UploadActivity.this, "No file selected", Toast.LENGTH_SHORT).show();
                    return;
                }
                Uri captureUri = Uri.parse(capturePath);
                String filename = new File(captureUri.getLastPathSegment()).getName();
                try {
                    uploadCapture(capturePath, filename);
                } catch (IOException e) {
                    toastShowException(e);
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        startActivityForResult(intent, Constants.READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        if (requestCode == Constants.READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            if (resultData != null) {
                Uri uri = resultData.getData();
                final int takeFlags = resultData.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(uri, takeFlags);
                EditText capturePathView = findViewById(R.id.capture_file_path);
                String uriStr = uri.toString();
                capturePathView.setText(uriStr);
                capturePathView.setSelection(uriStr.length());
            }
        }
    }


    void uploadCapture(String capPath, String filename) throws IOException {
        String wordlist = Utils.getRadioGroupCheckedText(this, R.id.wordlists);
        String rule = Utils.getRadioGroupCheckedText(this, R.id.rules);
        String url = Utils.buildUrl(this, "upload");
        String timeout = Utils.getEditTextValue(this, R.id.timeout);

        if (timeout.startsWith("0")) {
            throw new IOException("Invalid timeout");
        }

        try {
            String uploadId =
                    new BinaryUploadRequest(this, url)
                            .setFileToUpload(capPath)
                            .addHeader("filename", filename)
                            .addHeader("wordlist", wordlist)
                            .addHeader("rule", rule)
                            .addHeader("timeout", timeout)
                            .setNotificationConfig(new UploadNotificationConfig())
                            .setMaxRetries(2)
                            .setDelegate(new UploadCallback())
                            .startUpload();
        } catch (FileNotFoundException e) {
            toastShowException(e);
            e.printStackTrace();
        }
    }

}
