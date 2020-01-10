package com.androidcourse.pam.projet.v1;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;
    //private LocationCallback mLocationCallback;
    int MY_PERMISSION_ACCESS_COARSE_LOCATION = 0;
    private String latitude="";
    private String longitude="";
    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    MyCustomAdapter dataAdapter = null;
    ListView listView;
    List<ContactsInfo> contactsInfoList;
    List<String> subListOfCheckedNames=new ArrayList<String>();
    List<String> subListOfCheckedNumber=new ArrayList<String>();
    List<String> subListOfSelectedNames=new ArrayList<String>();
    List<String> subListOfSelectedNumber=new ArrayList<String>();
    boolean isAnAddressInput = false;
    String address ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.lstContacts);
        listView.setAdapter(dataAdapter);
        requestPermission();
        getCoordinates();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getCoordinates();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getCoordinates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCoordinates();
    }

    public void confirmAppointment(){
        // ouvrir pop up pour confirmer rdv
        String nameOfChosenContact="";
        for (int i = 0; i < subListOfCheckedNames.size(); i++) {
            if(i==0)
                nameOfChosenContact=subListOfCheckedNames.get(i);
            else
                nameOfChosenContact+=", "+subListOfCheckedNames.get(i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Confirmer RDV ?");
        if(isAnAddressInput)
            builder.setMessage("Avec "+nameOfChosenContact+"\nà\n"+ address);
        else
            builder.setMessage("Avec "+nameOfChosenContact+"\nà\nlatitude:"+latitude+"\nlongitude:"+longitude);
        builder.setPositiveButton("Confirmer",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setAppointment();
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

    public void setAppointment(){
        sendSMS();
    }

    public void getCoordinates() {

        /****** Location Services ********/

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_ACCESS_COARSE_LOCATION);
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                try {
                    latitude = Double.toString(location.getLatitude());
                    longitude = Double.toString(location.getLongitude());
                }
                catch (NullPointerException locationIsNull){
                    System.out.println("Impossible to get location");
                }
            }
        });
    }

    public void requestPermission(){
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_SMS,},
                PERMISSIONS_REQUEST_READ_CONTACTS);
    }

        @Override
        public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
            switch (requestCode) {
                case PERMISSIONS_REQUEST_READ_CONTACTS: {
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        getContacts();
                    } else {
                        Toast.makeText(this, "You have disabled a contacts permission", Toast.LENGTH_LONG).show();
                    }
                    return;
                }
            }
        }

        private void getContacts() {
            String contactId;
            String displayName;
            contactsInfoList = new ArrayList<ContactsInfo>();
            Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                    if (hasPhoneNumber > 0) {

                        ContactsInfo contactsInfo = new ContactsInfo();
                        contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                        displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                        contactsInfo.setContactId(contactId);
                        contactsInfo.setDisplayName(displayName);
                        contactsInfo.setIsAddedNumber(false);

                        Cursor phoneCursor = getContentResolver().query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{contactId},
                                null);

                        if (phoneCursor.moveToNext()) {
                            String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                            contactsInfo.setPhoneNumber(phoneNumber);
                        }

                        phoneCursor.close();

                        contactsInfoList.add(contactsInfo);
                    }
                }
            }
            cursor.close();

            dataAdapter = new MyCustomAdapter(MainActivity.this, R.layout.contact_info, contactsInfoList);
            listView.setAdapter(dataAdapter);
        }

        public void runSetSelectedSublistFromListView(View v){
            Context context = getApplicationContext();
            checkRadioToSetAddress();
            setSelectedSublistFromListView(listView);
            if(!subListOfCheckedNames.isEmpty()){
                if(isAnAddressInput){
                    getAddressInput();
                } else {
                    getCoordinates();
                    confirmAppointment();
                }
            } else {
                CharSequence text = "Veuillez selectionner un contact ou un numero";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration); toast.show();
            }
        }

        public void setSelectedSublistFromListView(ListView listView){
            CheckBox checkBox;
            TextView displayNameView;
            TextView displayNumberView;
            subListOfCheckedNames=new ArrayList<String>();;
            subListOfCheckedNames.addAll(subListOfSelectedNames);
            subListOfCheckedNumber=new ArrayList<String>();
            subListOfCheckedNumber.addAll(subListOfSelectedNumber);
            for (int i = 0; i < listView.getChildCount(); i++) {
                View v = listView.getChildAt(i);
                checkBox = (CheckBox) v.findViewById(R.id.checkBox);
                if(checkBox.isChecked()) {
                    displayNameView = (TextView) v.findViewById(R.id.displayName);
                    subListOfCheckedNames.add(displayNameView.getText().toString());
                    displayNumberView = (TextView) v.findViewById(R.id.phoneNumber);
                    subListOfCheckedNumber.add(displayNumberView.getText().toString());
                }
            }
        }

        public void openPopUpToAddNumber(View v){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Saisir numero");

            // Set up the input
            final EditText input = new EditText(this);
            final TextView text = new TextView(this);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String phoneNumber = input.getText().toString();
                    if(isValidMobile(phoneNumber)) {
                        updateUIWithNewNumber(phoneNumber);
                    }
                    else {
                        CharSequence text = "Veuillez entrer un numero valide";
                        int duration = Toast.LENGTH_SHORT;
                        Context context = getApplicationContext();
                        Toast toast = Toast.makeText(context, text, duration); toast.show();
                    }
                }
            });
            builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }

    private boolean isValidMobile(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

    public void updateUIWithNewNumber(String phoneNumber){
        ContactsInfo contactsInfo = new ContactsInfo();
        contactsInfo.setDisplayName(phoneNumber);
        contactsInfo.setPhoneNumber(phoneNumber);
        contactsInfo.setIsAddedNumber(true);
        contactsInfoList.add(0,contactsInfo);
        dataAdapter = new MyCustomAdapter(MainActivity.this, R.layout.contact_info, contactsInfoList);
        listView.setAdapter(dataAdapter);
        View v = listView.getAdapter().getView(0, null, listView);
        CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkBox);
        checkBox.setChecked(true);
        dataAdapter.notifyDataSetChanged();
    }

    public void sendSMS(){
        TelephonyManager tMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String senderNumber="";
        try{
            senderNumber = tMgr.getLine1Number();
            //remplacer les + du numero pour le formatage URL
            senderNumber = senderNumber.replace("+","%2B");
        }catch(SecurityException e){
            System.out.println("can't get sender number");
        }
        if(isAnAddressInput){
            address = address.replaceAll(" ","+");
            for(int i=0;i<subListOfCheckedNumber.size();i++){
                String message = "Bonjour "+subListOfCheckedNames.get(i)+"\n" +
                        "Retrouvez moi ici pour notre rendez-vous : \n" +
                        "http://projetpam.com/meetinginfos?address="+ address
                        +"&senderNumber="+senderNumber;
                sendSMSFunction(subListOfCheckedNumber.get(i),message);
            }
        } else {
            for (int i = 0; i < subListOfCheckedNumber.size(); i++) {
                /*String message = "Bonjour " + subListOfCheckedNames.get(i) + "\n" +
                        "Retrouvez moi ici pour notre rendez-vous : \n" +
                        "http://projetpam.com/meetinginfos?lat=" + latitude + "&lon=" + longitude
                        + "&senderNumber=" + senderNumber;*/
                String message = "Bonjour \n" +
                        "Retrouvez moi ici pour notre rendez-vous : \n" +
                        "http://projetpam.com/meetinginfos?lat=" + latitude + "&lon=" + longitude
                        + "&senderNumber=" + senderNumber;
                sendSMSFunction(subListOfCheckedNumber.get(i),message);
            }
        }
    }

    public void sendSMSFunction(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(getApplicationContext(), "Rendez-vous partagé",
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            System.out.println("can't send sms");
        }
    }

    public void getAddressInput(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Saisir adresse");

        // Set up the input
        final EditText input = new EditText(this);
        final TextView text = new TextView(this);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                address = input.getText().toString();
                confirmAppointment();
            }
        });
        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void checkRadioToSetAddress(){
        RadioButton addressButton = (RadioButton) findViewById(R.id.radioButton2);
        isAnAddressInput = addressButton.isChecked();
    }

}
