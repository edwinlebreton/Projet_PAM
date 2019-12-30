package com.androidcourse.pam.projet.v1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Toast;

public class MeetingInfos extends AppCompatActivity {

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
        String lat = appLinkData.getQueryParameter("lat");
        String lon = appLinkData.getQueryParameter("lon");
        final String senderNumber="(555)521-554";
        final String response="yes";
        final String senderName = appLinkData.getQueryParameter("senderName");
        // ouvrir pop up pour confirmer rdv
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Confirmer RDV ?");
        builder.setMessage("Avec "+senderName+"\nà\nlatitude:"+lat+"\nlongitude:"+lon);
        builder.setPositiveButton("Confirmer",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendConfirmationSMS(senderName,senderNumber,response);
                    }
                }
        );
        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void sendConfirmationSMS(String senderName, String senderNumber, String response){
        Context context = getApplicationContext();
            String message="";
        /*TODO trouver un moyen de recuperer response -> le faire en fesant tout dans la meme fonction
        * au lieu de passer en parametres final*/
            //if(response.equals("yes")) {
                message = "C'est avec plaisir que je vous retrouverais aux coordonées " +
                        "que vous m'avez indiqué\n"
                        + senderName;
                SmsManager smsManager = SmsManager.getDefault();
        /*TODO trouver un moyen de recuperer senderNumber*/
        smsManager.sendTextMessage("(555-521-5554", null, message, null, null);
                //smsManager.sendTextMessage(senderNumber, null, message, null, null);
                CharSequence text = "Rendez-vous accepté";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration); toast.show();
            //}
            /*else if(response.equals("no")) {
                message = "Désolé, je ne peux pas me rendre à ce rendez-vous \n"
                        + senderName;
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(senderNumber, null, message, null, null);
                CharSequence text = "Rendez-vous refusé";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration); toast.show();
            }*/
    }
}
