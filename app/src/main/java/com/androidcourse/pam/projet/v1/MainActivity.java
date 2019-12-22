package com.androidcourse.pam.projet.v1;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    int MY_PERMISSION_ACCESS_COARSE_LOCATION = 0;
    private String lattitude;
    private String longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void confirmAppointment(View v){

        // ouvrir pop up pour confirmer rdv
    }

    public void setAppointment(View v){
        String numeroTest="0635213563";
        String msgRDV = "Vous avez rendez-vous à: ";

        /****** Location Services ********/

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_ACCESS_COARSE_LOCATION);
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location == null) {
                    System.out.println("Location error");
                } else {
                    lattitude = Double.toString(location.getLatitude());
                    longitude = Double.toString(location.getLongitude());
                }
            }
        });

        System.out.println(msgRDV+lattitude+longitude+" avec "+numeroTest);

        //confirmer rdv
    }
}
