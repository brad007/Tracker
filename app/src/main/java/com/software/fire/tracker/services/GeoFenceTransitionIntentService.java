package com.software.fire.tracker.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.software.fire.tracker.ui.activities.MainActivity;
import com.software.fire.tracker.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brad on 12/30/2016.
 */

public class GeoFenceTransitionIntentService extends IntentService {
    public static final String TAG = GeoFenceTransitionIntentService.class.getSimpleName();


    public GeoFenceTransitionIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessage.getErrorString(this, geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        //Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        //Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            //Get the geofences that were triggered. A single event can trigger multiple geofences
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            //Get the transition details as a String.
            String geofenceTransitionDetails = getGeofencesTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            //Send notification and log the transition details.
            sendNotification(geofenceTransitionDetails);
            Log.i(TAG, geofenceTransitionDetails);
        }else{
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }
    }

    private String getGeofencesTransitionDetails(Context context, int geofenceTransition, List<Geofence> triggeringGeofences) {

        String geofencingTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

        return geofencingTransitionString + ": " + triggeringGeofencesIdsString;
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }

    private void sendNotification(String notificationDetails) {
        //Create an explicit content Intent that starts the main activity
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);

        //Construct a task stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        //Add the main activity to the task stack as the parent
        stackBuilder.addParentStack(MainActivity.class);

        //Push the content Intent onto the stack
        stackBuilder.addNextIntent(notificationIntent);

        //Get a PendingIntent containing the entire back stack.
        PendingIntent notifiationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        //Get a notification builder that's compatible with platform version >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        //Define the notificcation setting
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText("Click notification to return to app")
                .setContentIntent(notifiationPendingIntent);

        builder.setAutoCancel(true);

        //Get an instance of notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

}
