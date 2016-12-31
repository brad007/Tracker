package com.software.fire.tracker.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.TextView;

import com.software.fire.tracker.R;
import com.software.fire.tracker.utils.Constants;

public class SMSActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                double longitude = intent.getDoubleExtra(Constants.LONGITUDE, 0);
                double latitude = intent.getDoubleExtra(Constants.LATITUDE, 0);

                TextView cellphoneNumbeTextView = (TextView) findViewById(R.id.cellnumber_textview);
                String cellphoneNumber = cellphoneNumbeTextView.getText().toString();

                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(cellphoneNumber, null, latitude + "," + longitude, null, null);
                finish();
            }
        });
    }
}
