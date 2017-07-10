# BeaconApp
An android app for scanning nearby beacons and calculating their distance.
This app is designed for Fraunhofer IPA department.

The main tasks that we are doing here are -
1)Find the nearby beacons and check their properties like(rSSI value,txPowerLevel,UUID, Frame type,etc.).
2)Display the beacons available nearby in the notification bar with the beacon name and its corresponding distance.
3)Sending the information on the cloud and get a response back from the server.



Beacons can be configured using Google provided android application called Beacon Tools or the manufacturer provided app.
In our case, we use the Accent systems provided configuration app called iBKS Config tool. With the help of this app we can rename the beacon, set its uuid.

