package com.example.smsgateway;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.telephony.SmsManager;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);

        final Activity activity = this;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                // Read SMS
                try {
                    List<String> listPermissionsNeeded = new ArrayList<>();
                    final boolean send = ContextCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED;
                    final boolean receive = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED;
                    final boolean internet = ContextCompat.checkSelfPermission(activity, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED;
                    if (send || receive || internet) {
                        if (receive) {
                            listPermissionsNeeded.add(Manifest.permission.READ_SMS);
                        }
                        if (send) {
                            listPermissionsNeeded.add(Manifest.permission.SEND_SMS);
                        }
                        if (internet){
                            listPermissionsNeeded.add(Manifest.permission.INTERNET);
                        }
                        Snackbar.make(view, "Request SMS Permision", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray(new
                                String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
                    } else {
                        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
                        if (cursor.moveToFirst()) { // must check the result to prevent exception
                            String msgData = "";
                            do {

                                String body = "";
                                String id = "";
                                String address = "";
                                for (int idx = 0; idx < cursor.getColumnCount(); idx++) {
                                    if (cursor.getColumnName(idx).equalsIgnoreCase("body")) {
                                        body = cursor.getString(idx).replaceAll("\\s+", "");
                                    } else if (cursor.getColumnName(idx).equalsIgnoreCase("_id")) {
                                        id = cursor.getString(idx);
                                    } else if (cursor.getColumnName(idx).equalsIgnoreCase("address")) {
                                        address = cursor.getString(idx);
                                    }
                                }
                                if (body.toLowerCase().indexOf("ayuda") >= 0) {
                                    msgData += address + " " + body + ";";
                                }
                                // TODO Execute Http and Get SMS Response Message
                                String response = request("http://ayuda.net:8080/ayuda.do");
                                SmsManager smsManager = SmsManager.getDefault();
                                smsManager.sendTextMessage(address, null, "En breve recibirá un instructivo [" + response + "]", null, null);
                                Toast.makeText(getApplicationContext(), "SMS sent. to [" + address + "][" + response + "]",
                                        Toast.LENGTH_LONG).show();
                                // use msgData
                            } while (cursor.moveToNext());
                            Snackbar.make(view, "SMS: " + msgData, Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();


                        } else {
                            Snackbar.make(view, "SMS: " + "Empty", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            // empty box, no SMS

                        }
                    }

                } catch (Exception e) {
                    Snackbar.make(view, "SMS Error: " + e.getMessage(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }


            }
        });
    }

    public String request(String to) throws IOException {
        URL url = new URL(to);
        URLConnection uc = url.openConnection();
        //String j = (String) uc.getContent();
        uc.setDoInput(true);
        BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String inputLine;
        StringBuilder a = new StringBuilder();
        while ((inputLine = in.readLine()) != null)
            a.append(inputLine);
        in.close();
        return a.toString();
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
