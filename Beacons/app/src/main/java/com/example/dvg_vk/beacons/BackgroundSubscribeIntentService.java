package com.example.dvg_vk.beacons;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
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

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dvg-vk on 14.06.2017.
 */

public class BackgroundSubscribeIntentService extends IntentService implements BeaconConsumer,RangeNotifier{

    private static final int MESSAGES_NOTIFICATION_ID = 1;
    private BeaconManager mBeaconManager;
    protected static final String TAG = "BackgroundService";
    private static final int NUM_MESSAGES_IN_NOTIFICATION = 5;
    private  String username;
    private String[] content;
    String json_string;
    private String json_response;
    DecimalFormat df;
    private Integer[] distanceArray;
    HashMap<String, Float> hash = new HashMap<String, Float>();

    public BackgroundSubscribeIntentService() {
        super("BackgroundSubscribeIntentService");
        Config.list = new ArrayList<HashMap<String, String>>();
        df = new DecimalFormat("#.0000");
    }

    @Override
    public void onHandleIntent(Intent intent){
        // Gets data from the incoming Intent
        username= intent.getStringExtra("username");
        mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        // Detect the main Eddystone-UID frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        // Detect the telemetry Eddystone-TLM frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
        mBeaconManager.bind(this);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        Config.list.clear();
        json_string="";
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("user", Config.user);

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
                        hash.put(String.valueOf(namespaceId).substring(2) + String.valueOf(instanceId).substring(2),Float.valueOf(df.format(beacon.getDistance())));
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
        getJSON(Config.URL);
        displayNotification();
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

    private void displayNotification(){
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent launchIntent = new Intent(getApplicationContext(), MainActivity.class);
        launchIntent.setAction(Intent.ACTION_MAIN);
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String contentTitle = username;
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
        class AddBeacon extends AsyncTask<Void,Void,String>{

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
                //HashMap<String, String> params = new HashMap<>();
                // params.put(Config.TAG_JSON_DATA,json_string);
                //String JsonResponse = null;
                //String JsonDATA = params[0];

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

}
