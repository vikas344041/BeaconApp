package com.example.dvg_vk.beacons;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity{

    protected static final String TAG = "MainActivity";
    private BeaconManager mBeaconManager;
    private Context context=this;
    private Button btn_edit,btn_back;
    private EditText txtUsername,txtAge,txtHeight,txtDesignation,txtFloor;
    private Boolean savedState=false;
    String prevUsername,prevAge,prevDesignation,prevHeight,prevFloor;
    Intent mServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Config.list = new ArrayList<HashMap<String, String>>();
        assert getSupportActionBar() != null;
        getSupportActionBar().setLogo(R.mipmap.ibks);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setTitle("Beacon Manager");

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
                    sendDataToBackgroundService();
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
                sendDataToBackgroundService();
            }

        });

        /*
        * Creates a new Intent to start the RSSPullService
        * IntentService. Passes a URI in the
        * Intent's "data" field.
        */
        sendDataToBackgroundService();
    }
    @Override
    public void onResume() {
        super.onResume();
        /*mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        // Detect the main Eddystone-UID frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        mBeaconManager.bind(this);*/
    }

    public void didEnterRegion(Region region) {

    }

    public void didExitRegion(Region region) {
    }

    public void didDetermineStateForRegion(int state, Region region) {
    }

    @Override
    public void onPause() {
        super.onPause();
        //mBeaconManager.unbind(this);
    }

    /*@Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        Config.list.clear();
        for (Beacon beacon: beacons) {
            if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x00) {
                HashMap<String, String> beaconList = new HashMap<String, String>();
                // This is a Eddystone-UID frame
                Identifier namespaceId = beacon.getId1();
                Identifier instanceId = beacon.getId2();
                Log.d(TAG, "I see a beacon transmitting namespace id: "+namespaceId+
                        " and instance id: "+instanceId+
                        " approximately "+beacon.getDistance()+" meters away."+beacon.getBluetoothName());

                beaconList.put("Beacon_Name",beacon.getBluetoothName());
                beaconList.put("Beacon_Distance",String.valueOf(beacon.getDistance()));
                beaconList.put("Beacon_Uuid",String.valueOf(namespaceId).substring(2)+String.valueOf(instanceId).substring(2));
                Config.list.add(beaconList);
            }
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        // Set the two identifiers below to null to detect any beacon regardless of identifiers
        Region region = new Region("my-beacon-region", null, null, null);
        mBeaconManager.addMonitorNotifier(this);
        try {
            mBeaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mBeaconManager.addRangeNotifier(this);
    }*/

    @Override
    public void onBackPressed(){
        moveTaskToBack(true);
        sendDataToBackgroundService();
    }

    private void sendDataToBackgroundService(){
        mServiceIntent = new Intent(context, BackgroundSubscribeIntentService.class);
        mServiceIntent.putExtra("username","User = "+txtUsername.getText().toString());
        Config.user=txtUsername.getText().toString();
        context.startService(mServiceIntent);
    }
}
