package org.nwolfhub.notes.deprecated.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import org.nwolfhub.notes.R;

public class UpdateColors {
    public static void updateColors(Context context, View... views) {
        int mode = context.getResources().getConfiguration().uiMode;
        Log.d("views update", "Active mode: " + mode + ". Night mode is " + Configuration.UI_MODE_NIGHT_YES);
        for(View view:views) {
            if(mode == 33 || mode==32) {
                view.setBackgroundColor(Color.parseColor("#000000"));
                if(view instanceof Button) {
                    Log.d("Instanceof info",view.getId() + ": " + (view instanceof AppCompatButton));
                    if(view instanceof AppCompatButton) {
                        view.setBackground(ContextCompat.getDrawable(context, R.drawable.buttonshapeblack));
                    } else {
                        view.setBackgroundColor(Color.parseColor("#000000"));
                    }
                    ((Button) view).setTextColor(Color.parseColor("#FFFFFF"));
                }
                if(view instanceof TextView) {
                    ((TextView) view).setTextColor(Color.parseColor("#FFFFFF"));
                }
                if(view instanceof EditText) {
                    ((EditText) view).setHintTextColor(Color.parseColor("#FFFFFFB3"));
                    ((EditText) view).setTextColor(Color.parseColor("#FFFFFF"));
                }
            } else {
                view.setBackgroundColor(Color.parseColor("#FFFFFF"));
                if(view instanceof Button) {
                    if(view instanceof AppCompatButton) {
                        view.setBackground(ContextCompat.getDrawable(context, R.drawable.buttonshapewhite));
                    } else {
                        view.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    }
                    ((Button) view).setTextColor(Color.parseColor("#000000"));
                }
                if(view instanceof TextView) {
                    ((TextView) view).setTextColor(Color.parseColor("#000000"));
                }
                if(view instanceof EditText) {
                    ((EditText) view).setHintTextColor(Color.parseColor("#FFAFAA66"));
                    ((EditText) view).setTextColor(Color.parseColor("#000000"));
                }
            }
        }
    }

    public static void updateBars(AppCompatActivity activity) {
        activity.getSupportActionBar().hide();
        int mode = activity.getResources().getConfiguration().uiMode;
        if(mode == 33 || mode==32) {
            activity.getWindow().setStatusBarColor(Color.parseColor("#000000"));
        } else {
            activity.getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
        }
    }
}
