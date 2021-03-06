package pl.wizut_s2.test3;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetVersionService extends WebServiceConnection {

    // todo
    // update address
    private String HTTP_GET_URL = "https://projekt-pbai.pl/PBAI_WebApp/Account/GetVersion";

    public GetVersionService(PBAIClientInterface client) {
        super(client);

        AsyncTask<String, String, String> runner;
        runner = new HttpGetAsyncTaskRunner();

        runner.execute();

    }


    @Override
    public ServiceType getType() {
        return ServiceType.HTTP_GET;
    }

    private class HttpGetAsyncTaskRunner extends AsyncTask<String, String, String> {

        public String getStringFromInputStream(InputStream stream, String charsetName) throws IOException {
            int n = 0;
            char[] buffer = new char[1024 * 4];
            InputStreamReader reader = new InputStreamReader(stream, charsetName);
            StringWriter writer = new StringWriter();
            while (-1 != (n = reader.read(buffer))) writer.write(buffer, 0, n);
            return writer.toString();
        }

        @Override
        protected String doInBackground(String... strings) {
            String resp = "";
            try {
                java.net.URL url = new URL(HTTP_GET_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(conn.getInputStream());

                resp = getStringFromInputStream(in, "UTF-8");

                conn.disconnect();
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
            //publishProgress(result);
            onListOfScannersUpdate("Version:"+result);
        }
    }
}
