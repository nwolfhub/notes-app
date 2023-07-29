package org.nwolfhub;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.nwolfhub.model.Server;

import java.util.ArrayList;
import java.util.List;

public class PublicShared {
    public static SharedPreferences preferences;
    public static Notes activity;
    public static SharedPreferences web;

    public static WebLogin webActivity;

    public static ServerManageActivity serverManageActivity;

    public static int counter = 0;

    public static void restart() {
        activity.runOnUiThread(() -> {
            Notes timedCopy = activity;
            timedCopy.startActivity(new Intent(activity, Notes.class));
            timedCopy.finish();
        });
    }

    public static List<Server> buildServersList(SharedPreferences preferences) {
        String rawData = preferences.getString("servers", "");
        List<Server> servers = new ArrayList<>();
        for(String server:preferences.getAll().keySet()) {
            if(!server.equals("servers")) servers.add(new Server(server, preferences.getString(server, "")));
        }
        return servers;
    }

    public static AlertDialog buildServerDialog(String prevName, String prevUrl, SharedPreferences preferences) {
        LayoutInflater inflater = serverManageActivity.getLayoutInflater();
        View view = inflater.inflate(R.layout.serversettingsdialog, null);
        EditText nameEdit = view.findViewById(R.id.servernameedit);
        EditText urlEdit = view.findViewById(R.id.serveraddressedit);
        nameEdit.setText(prevName);
        urlEdit.setText(prevUrl);
        nameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(nameEdit.getText().toString().equals("servers")) nameEdit.setText("server");
                if(nameEdit.getText().toString().equals("Add new")) nameEdit.setText("smart fella fart smella");
                while(nameEdit.getText().toString().contains(";")) nameEdit.setText(nameEdit.getText().toString().substring(0, nameEdit.length()-1));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        return new AlertDialog.Builder(serverManageActivity).setTitle("Edit server").setView(view).setPositiveButton("Submit", (dialogInterface, i) -> {
            String name = nameEdit.getText().toString();
            String url = urlEdit.getText().toString();
            if(name.equals("") || url.equals("")) {
                Toast.makeText(serverManageActivity, "Either url or name is empty!", Toast.LENGTH_SHORT).show();
            } else {
                if(prevName.equals("Nwolfhub (official)") || name.equals("Nwolfhub (official)")) {
                    Toast.makeText(serverManageActivity, "Editing official server is not allowed", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("server save", "Saving server " + name + " with url " + url);
                    if (!prevName.equals("")) preferences.edit().remove(prevName).apply();
                    preferences.edit().putString(name, url).putString("servers", preferences.getString("servers", "") + ";" + name).apply();
                    serverManageActivity.startActivity(new Intent(serverManageActivity, WebLogin.class));
                    serverManageActivity.finish();
                }
            }
        }).create();
    }
}
