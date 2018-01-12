package com.dizcza.hashcatwpaclient;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.Locale;

/**
 * Created by dizcza on 11/4/17.
 */

public class Utils {

    public static String getEditTextValue(Activity activity, int resId) {
        EditText editText = activity.findViewById(resId);
        return editText.getText().toString();
    }

    public static String getRadioGroupCheckedText(Activity activity, int resId) {
        RadioGroup radioGroup = activity.findViewById(resId);
        RadioButton checkedButton = activity.findViewById(radioGroup.getCheckedRadioButtonId());
        return checkedButton.getText().toString();
    }

    public static String buildUrl(Context context, String endPoint) {
        SharedPreferences sharedPref = getSharedPref(context);
        String serverUrl = sharedPref.getString(Constants.SERVER_URL_SHARED_KEY, "");
        String port = sharedPref.getString(Constants.PORT_SHARED_KEY, "");
        return String.format(Locale.getDefault(), "%s:%s/%s", serverUrl, port, endPoint);
    }

    public static SharedPreferences getSharedPref(Context context) {
        return context.getSharedPreferences(Constants.SHARED_KEY_FILE, Context.MODE_PRIVATE);
    }

}
