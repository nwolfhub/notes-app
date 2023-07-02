package org.nwolfhub;

import android.content.Intent;
import android.content.SharedPreferences;

public class PublicShared {
    public static SharedPreferences preferences;
    public static Notes activity;
    public static SharedPreferences web;

    public static void restart() {
        activity.runOnUiThread(() -> {
            Notes timedCopy = activity;
            timedCopy.startActivity(new Intent(activity, Notes.class));
            timedCopy.finish();
        });
    }
}
