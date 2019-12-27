package com.androidcourse.pam.projet.v1;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.lstContacts);
        listView.setAdapter(dataAdapter);
        requestContactPermission();
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

    public void confirmAppointment(View v){
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
        // enregistrer rdv dans bdd
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

        //System.out.println(msgRDV+latitude+longitude+" avec "+numeroTest);

        public void requestContactPermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            android.Manifest.permission.READ_CONTACTS)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Read contacts access needed");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setMessage("Please enable access to contacts.");
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @TargetApi(Build.VERSION_CODES.M)
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                requestPermissions(
                                        new String[]
                                                {android.Manifest.permission.READ_CONTACTS}
                                        , PERMISSIONS_REQUEST_READ_CONTACTS);
                            }
                        });
                        builder.show();
                    } else {
                        ActivityCompat.requestPermissions(this,
                                new String[]{android.Manifest.permission.READ_CONTACTS},
                                PERMISSIONS_REQUEST_READ_CONTACTS);
                    }
                } else {
                    getContacts();
                }
            } else {
                getContacts();
            }
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
            ContentResolver contentResolver = getContentResolver();
            String contactId = null;
            String displayName = null;
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
        /* TODO gerer si liste nulle */
            setSelectedSublistFromListView(listView);
            if(!subListOfCheckedNames.isEmpty()){
                getCoordinates();
                confirmAppointment(v);
            }
            // ci-dessous test pour sous liste de noms selectionnés
            /*
            String testArray="Do you want to message to:";
            for (int i = 0; i < subListOfCheckedNames.size(); i++) {
                testArray+=" "+subListOfCheckedNames.get(i);
            }
            System.out.println(testArray);*/
        }

        public void setSelectedSublistFromListView(ListView listView){
            View v;
            CheckBox checkBox;
            TextView displayNameView;
            TextView displayNumberView;
            subListOfCheckedNames=new ArrayList<String>();;
            subListOfCheckedNames.addAll(subListOfSelectedNames);
            subListOfCheckedNumber=new ArrayList<String>();
            subListOfCheckedNumber.addAll(subListOfSelectedNumber);
            for (int i = 0; i < listView.getCount(); i++) {
                v = listView.getChildAt(i);
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
        /* TODO fonction pour ouvrir popup et saisir numero a ajouter qui n'est pas dans les contacts */
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
                        //listView.add
                        updateUIWithNewNumber(phoneNumber);
                        //subListOfSelectedNames.add(phoneNumber);
                        //subListOfSelectedNumber.add(phoneNumber);
                        //System.out.println("phoneNumber value: "+phoneNumber);
                        //System.out.println("sublist value: "+subListOfSelectedNames.get(0));
                    }
                    else {
                        //show wrong input number error message
                        //text.setText("Veuillez entrer un numero valide");
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

    /*
    public void resetAllLists(View v){
        subListOfCheckedNames.clear();
        subListOfCheckedNumber.clear();
        subListOfSelectedNames.clear();
        subListOfSelectedNumber.clear();
    }*/

    public void updateUIWithNewNumber(String phoneNumber){
        ContactsInfo contactsInfo = new ContactsInfo();
        contactsInfo.setDisplayName(phoneNumber);
        contactsInfo.setPhoneNumber(phoneNumber);
        contactsInfo.setIsAddedNumber(true);
        contactsInfoList.add(0,contactsInfo);
        dataAdapter = new MyCustomAdapter(MainActivity.this, R.layout.contact_info, contactsInfoList);
        listView.setAdapter(dataAdapter);
        //View v = listView.getChildAt(0);
        View v = listView.getAdapter().getView(0, null, listView);
        //v.findViewById(R.id.phoneNumber);
        CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkBox);
        checkBox.setChecked(true);
        dataAdapter.notifyDataSetChanged();
    }

}
