package pl.wizut_s2.test3;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class MainActivity extends FragmentActivity {

    private GoogleMap mMap;
//    private static final String URL =  "http://192.168.202.124:9000/AndroidWS/wsdl/ServiceImpl.wsdl";
    private static final String URL = "http://jgowor-001-site1.smarterasp.net/PBAI_WebService.svc?singleWsdl";
    public static final String NAMESPACE = "http://sample";
    public static final String SOAP_ACTION_PREFIX = "/";
    private static final String METHOD = "getName";

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

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute();
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

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        private String resp;

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Loading contents..."); // Calls onProgressUpdate()
            try {
                // SoapEnvelop.VER11 is SOAP Version 1.1 constant
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                SoapObject request = new SoapObject(NAMESPACE, METHOD);
                //bodyOut is the body object to be sent out with this envelope
                envelope.bodyOut = request;
                HttpTransportSE transport = new HttpTransportSE(URL);
                try {
                    transport.call(NAMESPACE + SOAP_ACTION_PREFIX + METHOD, envelope);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
                //bodyIn is the body object received with this envelope
                if (envelope.bodyIn != null) {
                    //getProperty() Returns a specific property at a certain index.
                    SoapPrimitive resultSOAP = (SoapPrimitive) ((SoapObject) envelope.bodyIn).getProperty(0);
                    resp = resultSOAP.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
                resp = e.getMessage();
            }
            return resp;
        }

        /**
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            // In this example it is the return value from the web service
//            textView.setText(result);
        }

        /**
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            // Things to be done before execution of long running operation. For
            // example showing ProgessDialog
        }
    }
}
