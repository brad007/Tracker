package com.software.fire.tracker.ui.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.software.fire.tracker.R;
import com.software.fire.tracker.utils.Constants;

public class EmailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView emailTextView = (TextView) findViewById(R.id.email_textview);
                String email = emailTextView.getText().toString();

                Intent intent = getIntent();
                double latitude = intent.getDoubleExtra(Constants.LATITUDE, 0);
                double longitude = intent.getDoubleExtra(Constants.LONGITUDE, 0);

                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("textt/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                emailIntent.putExtra(Intent.EXTRA_TEXT, latitude + "," + longitude);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "My location");

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send email... "));
                    finish();
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(EmailActivity.this, "There is no email client installed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
