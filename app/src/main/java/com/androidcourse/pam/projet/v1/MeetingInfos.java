package com.androidcourse.pam.projet.v1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;

public class MeetingInfos extends AppCompatActivity {

    String senderNumber="";
    String lat = "";
    String lon = "";
    String address = "";
    Boolean isAnAddressInput =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_infos);
        confirmAppointment();
    }

    public void confirmAppointment(){
        Intent appLinkIntent;
        appLinkIntent = getIntent();
        Uri appLinkData = appLinkIntent.getData();
        if(appLinkData.getQueryParameter("address")!=null)
            isAnAddressInput =true;
        if(isAnAddressInput){
            address = appLinkData.getQueryParameter("address");
        }else {
            lat = appLinkData.getQueryParameter("lat");
            lon = appLinkData.getQueryParameter("lon");
        }
        senderNumber = appLinkData.getQueryParameter("senderNumber");
        // ouvrir pop up pour confirmer rdv
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Confirmer RDV ?");
        if(isAnAddressInput) {
            String tmpAddress = address.replaceAll("\\+", " ");
            builder.setMessage("à\n" + tmpAddress);
        } else
            builder.setMessage("à\nlatitude:"+lat+"\nlongitude:"+lon);
        builder.setPositiveButton("Confirmer",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendConfirmationSMS(senderNumber,"yes");
                        openMapsView();
                        finish();
                    }
                }
        );
        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendConfirmationSMS(senderNumber,"no");
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void sendConfirmationSMS(String senderNumber, String response){
        Context context = getApplicationContext();
            String message="";
            if(response.equals("yes")) {
                message = "C'est avec plaisir que je vous retrouverais aux coordonées " +
                        "que vous m'avez indiqué";
                sendSMSFunction(senderNumber,message);
                CharSequence text = "Rendez-vous accepté";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration); toast.show();
            }
            else{
                message = "Désolé, je ne peux pas me rendre à ce rendez-vous";
                sendSMSFunction(senderNumber,message);
                CharSequence text = "Rendez-vous refusé";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration); toast.show();
            }
    }

    public void sendSMSFunction(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
        } catch (Exception ex) {
            System.out.println("can't send sms");
        }
    }

    public void openMapsView(){
        String mTitle = "Votre RDV";
        String uri = "";
        if(isAnAddressInput)
            uri = "http://maps.google.co.in/maps?q=" + address;
        else
            uri = "http://maps.google.com/maps?q=loc:" + lat + "," + lon + " (" + mTitle + ")";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);
    }
}
