package aenadon.viruscomplete;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

//   #############################################################
// #### https://www.virustotal.com/en/documentation/public-api/ ####
//   #############################################################

public class FetchJSON {

    public String jsonResponse = null; // contains the JSON response after fetching

    // We're doing the whole work in the constructor! Every time a new class instance is created, the
    // requested JSON is saved inside that reference. When the instance is not needed anymore, must assign NULL
    // so the garbage collector claims that instance.
    public FetchJSON(String passedUrl) {
        String LOG_TAG = FetchJSON.class.getSimpleName();

        HttpsURLConnection urlConnection = null; // These two need to be declared outside the try/catch
        BufferedReader reader = null;            // so that they can be closed in the finally block.

        try {
            URL callUrl = new URL(passedUrl);

            urlConnection = (HttpsURLConnection) callUrl.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream == null) {
                Log.e(LOG_TAG, "Input stream is null!");
                return; // Nothing to do here
            }

            StringBuffer buffer = new StringBuffer();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            // This makes debugging easier
            String line;
            while ((line = reader.readLine()) != null) buffer.append(line).append("\n");

            if (buffer.length() == 0) {
                Log.e(LOG_TAG, "Buffer is empty!");
                return; // Stream was empty!
            }
            jsonResponse = buffer.toString();

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error ", e);
            e.printStackTrace(); // Error means: no info provided.
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
            if (reader != null) {
                try {reader.close();}
                catch (Exception e) {
                    Log.e(LOG_TAG, "Error closing stream: ", e);
                    e.printStackTrace();
                }
            }
        }
    }


}
