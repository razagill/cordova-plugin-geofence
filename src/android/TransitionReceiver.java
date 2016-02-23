package com.cowbell.cordova.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpResponse;
import android.os.AsyncTask;
import java.util.*;

public class TransitionReceiver extends BroadcastReceiver {

    private GeoNotificationManager geoNotificationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.setLogger(new Logger(GeofencePlugin.TAG, context, false));
        Logger logger = Logger.getLogger();

        String error = intent.getStringExtra("error");

        if (error != null) {
            //handle error
            logger.log(Log.DEBUG, error);
        } else {
            String geofencesJson = intent.getStringExtra("transitionData");
            PostLocationTask task = new TransitionReceiver.PostLocationTask();
            task.execute(geofencesJson);
        }
        geoNotificationManager = new GeoNotificationManager(context);
    }

    private class PostLocationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... geofencesJson) {
            try {

                Log.println(Log.DEBUG, GeofencePlugin.TAG, "Executing PostLocationTask#doInBackground");

                GeoNotification[] geoNotifications = Gson.get().fromJson(geofencesJson[0], GeoNotification[].class);

                for (int i=0; i < geoNotifications.length; i++){
                    GeoNotification geoNotification = geoNotifications[i];

                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    HttpPost request = new HttpPost(geoNotification.url);

                    Log.println(Log.DEBUG, GeofencePlugin.TAG, "URL:" + geoNotification.url);
                    StringEntity se = new StringEntity(geoNotification.toJson());
                    request.setEntity(se);
                    request.setHeader("Accept", "application/json");
                    request.setHeader("Content-type", "application/json");

                    for (Map.Entry<String, String> entry : geoNotification.headers.entrySet()) {
                        request.setHeader(entry.getKey(), entry.getValue());
                    }

                    HttpResponse response = httpClient.execute(request);
                    //Remove the geofence
                    geoNotificationManager.removeGeoNotification(geoNotification.id, null);

                    Log.println(Log.DEBUG, GeofencePlugin.TAG,  "Response received"+ response.getStatusLine());
                    if (response.getStatusLine().getStatusCode() == 200) {

                        Log.println(Log.DEBUG, GeofencePlugin.TAG,  "Reponse OK");
                    } else {
                        Log.println(Log.DEBUG, GeofencePlugin.TAG,  "Reponse KO");
                    }
                }
            } catch (Throwable e) {
                Log.println(Log.ERROR, GeofencePlugin.TAG, "Exception posting geofence: " + e);
            }

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }
}
