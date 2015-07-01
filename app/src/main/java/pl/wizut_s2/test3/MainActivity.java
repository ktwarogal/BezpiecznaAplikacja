package pl.wizut_s2.test3;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends FragmentActivity implements PBAIClientInterface {
    MainActivity MainContext = this;
    //TODO
    // - migotanie policji
    // - poruszanie się w lewo, aż trafi w policję
    // - sprawdzanie, czy jest w obrębiue policji (dodać flagę)


    private GoogleMap mMap;
    private ArrayList<Address> mPolicePoints;
    private double mPoliceRadius =150;
    Location mCurrentLocation = null;
    private boolean mIsInPoliceRadius = false;


    public GetWebService webServiceConnection;


    public MainActivity() {
        mIsInPoliceRadius = false;


        switch (WebServiceConnection.ServiceType.HTTP_GET) {
            case HTTP_GET:
                webServiceConnection = new GetWebService(this);
                break;
            case SOAP:
                webServiceConnection = new GetWebService(this);
                break;
        }

        final Handler handler = new Handler();
        Timer timer = new Timer(false);

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        webServiceConnection = new GetWebService(MainContext);
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(timerTask, 15000,15000); // 1000 = 1 second.
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPolicePoints = new ArrayList<Address>();
        //mPolicePoints = GetPointsFromWeb();



        mMap = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map)).getMap();
        mMap.setMyLocationEnabled(true);


        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(53.368633, 14.671369));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

        mMap.moveCamera(center);
        mMap.animateCamera(zoom);

        mMap.clear();

//        for(Address _Location : mPolicePoints) {
//            mMap.addCircle(new CircleOptions()
//                    .center(new LatLng(_Location.getLatitude(), _Location.getLongitude()))
//                    .radius(mPoliceRadius)
//                    .strokeColor(Color.RED)
//                    .fillColor(Color.BLUE));
//        }
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

// Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location pLocation) {
                // Called when a new location is found by the network location provider.
                //Toast.makeText(getBaseContext(), "Got new location!", Toast.LENGTH_LONG).show();
                mCurrentLocation = pLocation;
                mCurrentLocation.setLongitude(mCurrentLocation.getLongitude());

                MakeUseOfNewLocation();
                TriggerPoliceActionIfNeeded();

            }

            public void onStatusChanged(String pProvider, int pStatus, Bundle pExtras) {}
            public void onProviderEnabled(String pProvider) {}
            public void onProviderDisabled(String pProvider) {}
        };

// Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener);
    }

    void AddPolicePointsOnMap() {
        mMap.clear();
        for(Address _Location : mPolicePoints) {
            mMap.addCircle(new CircleOptions()
                    .center(new LatLng(_Location.getLatitude(), _Location.getLongitude()))
                    .radius(mPoliceRadius)
                    .strokeColor(Color.RED)
                    .fillColor(Color.BLUE));
        }
    }

    private ArrayList<Address> GetPointsFromWeb() {
        ArrayList<Address> _RetValue = new ArrayList<Address>();

        // pobieranie koordynatów:  http://mondeca.com/index.php/en/any-place-en

        Address _Address = new Address(Locale.GERMAN);
        _Address.setLatitude(53.42214);
        _Address.setLongitude(14.53787);
        _RetValue.add(_Address);

        return _RetValue;
    }


    private void MakeUseOfNewLocation() {
        Context _Context = getApplicationContext();
        CharSequence _Text = "Zmieniono lokalizację";
        int _Duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(_Context, _Text, _Duration);
        //toast.show();


        //53.368633, 14.671369
        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

        mMap.moveCamera(center);
        mMap.animateCamera(zoom);



    }

    private void TriggerPoliceActionIfNeeded() {
        //Toast.makeText(getBaseContext(), (mIsInPoliceRadius ? "T": "F"), Toast.LENGTH_LONG).show();
        if(IsInPoliceRadius()){ // if in police radius

            if(!mIsInPoliceRadius) {  // first time in police radius, play sound
                // weszlismy w pole policji, a wczesniej nie bylismy. Odpalamy alarm i zmieniamy flagę
                Toast.makeText(getBaseContext(), "IN POLICE AREA!", Toast.LENGTH_LONG).show();
                PlayPoliceAlarm();

            }
            else{  // have already been in police radius, don't play sound
                Toast.makeText(getBaseContext(), "still in police area", Toast.LENGTH_LONG).show();
            }

            mIsInPoliceRadius = true;
        }
        else{ // not in police radius
            if(mIsInPoliceRadius)  // just left the radius, play the sound
            {
                PlayPoliceAwayNotification();
                Toast.makeText(getBaseContext(), "LEFT AREA!", Toast.LENGTH_LONG).show();
                PlayPoliceAlarm();
            }
            else{  // still in police radius in this session
                Toast.makeText(getBaseContext(), "still NOT in police area", Toast.LENGTH_LONG).show();
            }

            mIsInPoliceRadius = false;
        }
    }

    private void PlayPoliceAwayNotification() {
        // TODO
    }

    private boolean IsInPoliceRadius() {
        for(Address _Location : mPolicePoints){
            if(IsCurrentLocationInRadius(_Location)) {
                return true;
            }
        }
        return false;
    }

    private boolean IsCurrentLocationInRadius(Address pAddress) {

        double _Distance;
        //_Distance = distance(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude(), pAddress.getLatitude(),pAddress.getLongitude(),'K');
        float[] results = new float[1];
        Location.distanceBetween(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                pAddress.getLatitude(), pAddress.getLongitude(), results);
        _Distance = results[0];

        //Toast.makeText(getBaseContext(), "DISTANCE: "+String.valueOf(_Distance), Toast.LENGTH_LONG).show();
        return _Distance <= mPoliceRadius;
    }




    private void PlayPoliceAlarm() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == 'K') {
            dist = dist * 1.609344;
        } else if (unit == 'N') {
            dist = dist * 0.8684;
        }
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
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


    public void displayNotification(String text, int duration) {
        Toast toast = Toast.makeText(getApplicationContext(), text, duration);
        toast.show();
    }

    // default values, java style T_T
    public void displayNotification(String text) {
        displayNotification(text, Toast.LENGTH_SHORT);
    }

    public void onListOfScannersUpdate(String s) {
        Toast.makeText(getBaseContext(), "Refreshed points list!", Toast.LENGTH_LONG).show();
        PreparePolicePoints(s);
        AddPolicePointsOnMap();
    }

    private void PreparePolicePoints(String pPoints) {
        mPolicePoints.clear();
        String [] _Points = pPoints.split("\\|",-1);
        for(String _Point : _Points){
            String [] _Coordinates = _Point.split("\\,");
            Address _TempAddress = new Address(Locale.GERMAN);
            _TempAddress.setLatitude(Double.parseDouble(_Coordinates[0]));
            _TempAddress.setLongitude(Double.parseDouble(_Coordinates[1]));
            mPolicePoints.add(_TempAddress);

        }
    }


}
