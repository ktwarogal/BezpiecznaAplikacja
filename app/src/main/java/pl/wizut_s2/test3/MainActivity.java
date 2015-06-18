package pl.wizut_s2.test3;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends FragmentActivity {

    private GoogleMap mMap;

    WebServiceConnectionManager webServiceConnectionManager;

    public MainActivity() {
        webServiceConnectionManager = new WebServiceConnectionManager(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        
        mMap = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map)).getMap();
        mMap.setMyLocationEnabled(true);


        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(53.368633, 14.671369));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

        mMap.moveCamera(center);
        mMap.animateCamera(zoom);


        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

// Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location pLocation) {
                // Called when a new location is found by the network location provider.
                MakeUseOfNewLocation(pLocation);

            }

            public void onStatusChanged(String pProvider, int pStatus, Bundle pExtras) {}
            public void onProviderEnabled(String pProvider) {}
            public void onProviderDisabled(String pProvider) {}
        };

// Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener);

    }

    private void MakeUseOfNewLocation(Location pLocation) {
        displayNotification("Zmieniono lokalizację");

        //53.368633, 14.671369
        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(pLocation.getLatitude(), pLocation.getLongitude()));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

        mMap.moveCamera(center);
        mMap.animateCamera(zoom);

    }

    public void displayNotification(String text, int duration){
        Toast toast = Toast.makeText(getApplicationContext(), text, duration);
        toast.show();
    }

    // default values, java style T_T
    public void displayNotification(String text){
        displayNotification(text, Toast.LENGTH_SHORT);
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

    public void onListOfEnemiesUpdate(String s) {
        Log.v("WebServiceResult", "s = " + s);
    }
}
