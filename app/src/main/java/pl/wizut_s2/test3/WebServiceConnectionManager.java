package pl.wizut_s2.test3;

import android.os.AsyncTask;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class WebServiceConnectionManager {

    //    private static final String URL = "http://jgowor-001-site1.smarterasp.net/PBAI_WebService.svc?singleWsdl";
    private static final String URL = "https://52.26.121.34/PBAI_WebApp/PBAI_WebService.svc?wsdl";
    //    public static final String NAMESPACE = "http://jgowor-001-site1.smarterasp.net";
    public static final String NAMESPACE = "http://tempuri.org";
    public static final String SOAP_ACTION_PREFIX = "/IPBAI_WebService/";
    private static final String METHOD = "GetSpeedCameras";

    private MainActivity mainActivity;

    WebServiceConnectionManager(MainActivity ma) {
        mainActivity = ma;

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute();
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        private String resp;

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Loading contents..."); // Calls onProgressUpdate()
            try {
                // SoapEnvelop.VER11 is SOAP Version 1.1 constant
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

                //bodyOut is the body object to be sent out with this envelope
                envelope.bodyOut = new SoapObject(NAMESPACE, METHOD);
                HttpTransportSE transport = new HttpTransportSE(URL);
                try {
                    transport.call(NAMESPACE + SOAP_ACTION_PREFIX + METHOD, envelope);
                } catch (IOException | XmlPullParserException e) {
                    resp = e.getMessage();
                    Log.w("WebService", "FAIL " + resp);
                    e.printStackTrace();
                }

                if (envelope.bodyIn instanceof SoapFault) {
                    final SoapFault sf = (SoapFault) envelope.bodyIn;
                    resp = sf.getMessage();
                    Log.w("WebService", "FAIL " + resp);
                } else if (envelope.bodyIn != null) {
                    //bodyIn is the body object received with this envelope

                    //getProperty() Returns a specific property at a certain index.
                    SoapPrimitive resultSOAP = (SoapPrimitive) ((SoapObject) envelope.bodyIn).getProperty(0);
                    resp = resultSOAP.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
                resp = e.getMessage();
            }
            publishProgress(resp);
            Log.w("WebService", resp);
            return resp;
        }


        /**
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            // In this example it is the return value from the web service
            //publishProgress(result);
            mainActivity.onListOfEnemiesUpdate(result);
        }

        /**
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            // Things to be done before execution of long running operation. For
            // example showing ProgessDialog
        }

        @Override
        protected void onProgressUpdate(String... text) {
            Log.w("WebService", text[0]);
            mainActivity.displayNotification(text[0]);
        }
    }
}
