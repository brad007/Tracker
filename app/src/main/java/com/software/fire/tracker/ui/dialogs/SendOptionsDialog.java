package com.software.fire.tracker.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.google.android.gms.maps.model.LatLng;
import com.software.fire.tracker.R;
import com.software.fire.tracker.ui.activities.EmailActivity;
import com.software.fire.tracker.ui.activities.SMSActivity;
import com.software.fire.tracker.utils.Constants;

/**
 * Created by Brad on 12/31/2016.
 */

public class SendOptionsDialog extends DialogFragment {


    private static final int SMS_OPTION = 0;
    private static final int EMAIL_OPTION = 1;
    private SendOptionsManager mSendOptionsManager;

    public interface SendOptionsManager {
        LatLng getPosition();
    }

    public void setSendOptionsManger(SendOptionsManager sendOptionsManger) {
        mSendOptionsManager = sendOptionsManger;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.send_option_title);
        builder.setItems(R.array.send_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case SMS_OPTION:
                        Intent smsIntent = new Intent(getContext(), SMSActivity.class);
                        smsIntent.putExtra(Constants.LATITUDE, mSendOptionsManager.getPosition().latitude);
                        smsIntent.putExtra(Constants.LONGITUDE, mSendOptionsManager.getPosition().longitude);
                        startActivity(smsIntent);
                        break;
                    case EMAIL_OPTION:
                        Intent emailIntent = new Intent(getContext(), EmailActivity.class);
                        emailIntent.putExtra(Constants.LATITUDE, mSendOptionsManager.getPosition().latitude);
                        emailIntent.putExtra(Constants.LONGITUDE, mSendOptionsManager.getPosition().longitude);
                        startActivity(emailIntent);
                        break;
                }
            }
        });
        return builder.create();
    }
}

