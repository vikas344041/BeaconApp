package com.example.dvg_vk.beacons;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.dvg_vk.beacons.DAO.RequestHandler;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements BeaconConsumer,RangeNotifier{

    protected static final String TAG = "MainActivity";
    private BeaconManager mBeaconManager;
    private Context context=this;
    private Button btn_edit,btn_back;
    private EditText txtUsername,txtAge,txtHeight,txtDesignation,txtFloor;
    private Boolean savedState=false,isPlaying=true;
    String prevUsername,prevAge,prevDesignation,prevHeight,prevFloor;
    Intent mServiceIntent;
    private String[] content;
    private static final int MESSAGES_NOTIFICATION_ID = 1;
    String json_string;
    private String json_response;
    DecimalFormat df;
    MenuItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Config.list = new ArrayList<HashMap<String, String>>();
        Config.list = new ArrayList<HashMap<String, String>>();
        df = new DecimalFormat("#.0000");
        assert getSupportActionBar() != null;
        getSupportActionBar().setLogo(R.mipmap.ibks);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setTitle("BeaconApp");

        mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        // Detect the main Eddystone-UID frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        mBeaconManager.bind(this);
        mBeaconManager.setBackgroundBetweenScanPeriod(12000);

        mBeaconManager.setBackgroundScanPeriod(10000L);          // default is 10000L
        mBeaconManager.setForegroundBetweenScanPeriod(0L);      // default is 0L
        mBeaconManager.setForegroundScanPeriod(4000L);          // Default is 1100L

        mBeaconManager.addRangeNotifier(this);

        btn_back=(Button)findViewById(R.id.btn_back);
        btn_edit=(Button)findViewById(R.id.btn_edit);
        txtUsername=(EditText)findViewById(R.id.username);
        txtAge=(EditText)findViewById(R.id.age);
        txtDesignation=(EditText)findViewById(R.id.designation);
        txtHeight=(EditText)findViewById(R.id.height);
        txtFloor=(EditText)findViewById(R.id.work_floor);

        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if(savedState){
                    savedState=false;
                    btn_back.setEnabled(false);
                    btn_edit.setText("EDIT");
                    txtUsername.setEnabled(false);
                    txtAge.setEnabled(false);
                    txtHeight.setEnabled(false);
                    txtDesignation.setEnabled(false);
                    txtFloor.setEnabled(false);
                    btn_back.setAlpha(0.5f);
                    Config.USER=txtUsername.getText().toString();
                    displayNotification();
                }
                else {
                    btn_back.setEnabled(true);
                    btn_edit.setText("SAVE");
                    txtUsername.setEnabled(true);
                    txtAge.setEnabled(true);
                    txtHeight.setEnabled(true);
                    txtDesignation.setEnabled(true);
                    txtFloor.setEnabled(true);
                    txtUsername.setSelection(txtUsername.getText().length());
                    btn_back.setAlpha(1);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(txtUsername, InputMethodManager.SHOW_IMPLICIT);
                    savedState = true;
                    prevUsername=txtUsername.getText().toString();
                    prevAge=txtAge.getText().toString();
                    prevDesignation=txtDesignation.getText().toString();
                    prevHeight=txtHeight.getText().toString();
                    prevFloor=txtFloor.getText().toString();
                }
            }

        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                txtUsername.setText(prevUsername);
                Config.USER=txtUsername.getText().toString();
                txtAge.setText(prevAge);
                txtDesignation.setText(prevDesignation);
                txtHeight.setText(prevHeight);
                txtFloor.setText(prevFloor);

                btn_back.setEnabled(false);
                btn_edit.setText("EDIT");
                txtUsername.setEnabled(false);
                txtAge.setEnabled(false);
                txtHeight.setEnabled(false);
                txtDesignation.setEnabled(false);
                txtFloor.setEnabled(false);
                btn_back.setAlpha(0.5f);
                savedState=false;
                displayNotification();
            }

        });

        txtUsername.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    savedState=false;
                    btn_back.setEnabled(false);
                    btn_edit.setText("EDIT");
                    Config.USER=txtUsername.getText().toString();
                    txtUsername.setEnabled(false);
                    txtAge.setEnabled(false);
                    txtHeight.setEnabled(false);
                    txtDesignation.setEnabled(false);
                    txtFloor.setEnabled(false);
                    btn_back.setAlpha(0.5f);
                    displayNotification();
                    return true;
                }
                return false;
            }
        });

        /*
        * Creates a new Intent to start the RSSPullService
        * IntentService. Passes a URI in the
        * Intent's "data" field.
        */
    }
    @Override
    public void onResume() {
        super.onResume();
        mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        // Detect the main Eddystone-UID frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        mBeaconManager.bind(this);
        if(!isPlaying){
            stopBackgroundService();
            pauseScan();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mBeaconManager.unbind(this);
    }

    @Override
    public void onBackPressed(){
        moveTaskToBack(true);
        if(isPlaying){
            sendDataToBackgroundService();
        }
    }

    private void sendDataToBackgroundService(){
        mServiceIntent = new Intent(context, BackGroundService.class);
        mServiceIntent.putExtra("username",txtUsername.getText().toString());
        Config.USER=txtUsername.getText().toString();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        context.startService(mServiceIntent);
    }

    private void stopBackgroundService(){
        mServiceIntent = new Intent(context, BackGroundService.class);
        Config.USER=txtUsername.getText().toString();
        context.stopService(mServiceIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()){
            case R.id.menu_item://this item has your app icon
                if(isPlaying){
                    isPlaying=false;
                    item.setIcon(R.drawable.ic_action_playback_play);
                    item.setTitle("Paused");
                    pauseScan();
                }
                else{
                    isPlaying=true;
                    item.setIcon(R.drawable.ic_action_playback_pause);
                    item.setTitle("Running");
                    startScan();
                }
                return true;

            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        Config.list.clear();
        json_string="";
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("user", Config.USER);

            try {
                // In this case we need a json array to hold the java list
                JSONArray jsonArr = new JSONArray();

                for (Beacon beacon : beacons) {
                    if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x00) {
                        JSONObject jsonBeacon=new JSONObject();
                        HashMap<String, String> beaconList = new HashMap<String, String>();
                        // This is a Eddystone-UID frame
                        Identifier namespaceId = beacon.getId1();
                        Identifier instanceId = beacon.getId2();
                        Log.d(TAG, "I see a beacon transmitting namespace id: " + namespaceId +
                                " and instance id: " + instanceId +
                                " approximately " + beacon.getDistance() + " meters away." + beacon.getBluetoothName());

                        beaconList.put("Beacon_Name", beacon.getBluetoothName());
                        beaconList.put("Beacon_Distance", String.valueOf(df.format(beacon.getDistance())));
                        beaconList.put("Beacon_Uuid", String.valueOf(namespaceId).substring(2) + String.valueOf(instanceId).substring(2));
                        Config.list.add(beaconList);

                        jsonBeacon.put("uuid", String.valueOf(namespaceId).substring(2) + String.valueOf(instanceId).substring(2));
                        jsonBeacon.put("distance", df.format(beacon.getDistance()));
                        jsonArr.put(jsonBeacon);
                    }
                }
                jsonObj.put("beacons", jsonArr);
                json_string=jsonObj.toString();

            } catch (JSONException e) {
                Log.d(TAG, "Cant find beacon");
            }
        }
        catch (JSONException e) {
            Log.d(TAG, "Cant find beacon");
        }
        if(isPlaying){
            displayNotification();
            getJSON(Config.URL);
        }
        else{
            pauseScan();
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        Region region = new Region("all-beacons-region", null, null, null);
        try {
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mBeaconManager.addRangeNotifier(this);
    }

    public  void displayNotification(){
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent launchIntent = new Intent(getApplicationContext(), MainActivity.class);
        launchIntent.setAction(Intent.ACTION_MAIN);
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String contentTitle = txtUsername.getText().toString();
        String contentText="";
        if(!Config.list.isEmpty()){
            content=new String[Config.list.size()];
            for(int i=0;i<Config.list.size();i++){
                content[i]=Config.list.get(i).get("Beacon_Name")+" | "+Config.list.get(i).get("Beacon_Distance");
            }
            contentText = Config.list.get(0).get("Beacon_Name")+" | "+Config.list.get(0).get("Beacon_Distance");
        }
        else{
            contentText="No Beacon found";
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ibks)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setOngoing(true)
                .setContentIntent(pi);
        if(Config.list.size()>0){
            NotificationCompat.InboxStyle inboxStyle =
                    new NotificationCompat.InboxStyle();
            inboxStyle.setBigContentTitle(contentTitle);
            // Moves events into the expanded layout
            for (int i=0; i < content.length; i++) {

                inboxStyle.addLine(content[i]);
            }
            // Moves the expanded layout object into the notification object.
            notificationBuilder.setStyle(inboxStyle);
        }
            notificationManager.notify(MESSAGES_NOTIFICATION_ID, notificationBuilder.build());

    }

    private void getJSON(final String url){
        class AddBeacon extends AsyncTask<Void,Void,String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                json_response=s;
                //getBeaconResponse();
            }

            @Override
            protected String doInBackground(Void... v) {
                RequestHandler rh = new RequestHandler();
                String res = rh.sendPostRequestJSON(url, json_string);
                return res;

            }
        }

        AddBeacon anm = new AddBeacon();
        anm.execute();
    }

    private void getBeaconResponse(){
        Toast.makeText(getApplicationContext(), json_response, Toast.LENGTH_LONG).show();
    }

    private void pauseScan(){
        Region region = new Region("all-beacons-region", null, null, null);
        try {
            if (mBeaconManager.isBound(this)) {
                mBeaconManager.stopRangingBeaconsInRegion(region);
            }

        } catch (RemoteException e) {
            Log.d("Error", "Stop scan beacon problem", e);
        }
    }

    private void startScan(){
        Region region = new Region("all-beacons-region", null, null, null);
        try {
            if (mBeaconManager.isBound(this)) {
                mBeaconManager.startRangingBeaconsInRegion(region);
            }

        } catch (RemoteException e) {
            Log.d("Error", "Stop scan beacon problem", e);
        }
    }
}
