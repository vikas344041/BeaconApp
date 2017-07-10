# BeaconApp
An android app for scanning nearby beacons and calculating their distance.
This app is designed for Fraunhofer IPA department.

The main tasks that we are doing here are -
1)Find the nearby beacons and check their properties like(rSSI value,txPowerLevel,UUID, Frame type,etc.).
2)Display the beacons available nearby in the notification bar with the beacon name and its corresponding distance.
3)Sending the information on the cloud and get a response back from the server.



Beacons can be configured using Google provided android application called Beacon Tools or the manufacturer provided app.
In our case, we use the Accent systems provided configuration app called iBKS Config tool. With the help of this app we can rename the beacon, set its uuid.

![image](https://user-images.githubusercontent.com/17603441/28023628-b3f35c4c-658e-11e7-9522-00304523414a.png)

![image](https://user-images.githubusercontent.com/17603441/28023655-c0caab82-658e-11e7-9f1c-5c444d5fed16.png)

We are calculating the approximate distance every 5 seonds and send it to the server with the beacon name.
The distance is an approximate value that is calculated by saving a number of values and calculating ist average.

Implementation

![image](https://user-images.githubusercontent.com/17603441/28023676-cdfe358a-658e-11e7-8645-198df4bcbae1.png)

![image](https://user-images.githubusercontent.com/17603441/28023680-d2aa0a28-658e-11e7-87fc-d569e9e01999.png)

We are using the library version of altbeacon that supports android 5.0 and above. Further testing on other devices has to be checked.

