package com.example.yamagata.scheduler;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by yamagata on 西暦16/08/23.
 */
public class ScheduleEditActivity extends AppCompatActivity  implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,PlaceSelectionListener {

    EditText mDateEdit;
    EditText mTitleEdit;
    EditText mCompanyEdit;
    EditText mDetailEdit;
    Button mReturn;
    Button mMap;
    Button mDelete;
    PlaceAutocompleteFragment autocompleteFragment;

    private final static String TAG = "ScheduleEditActivity";
    private GoogleApiClient googleApiClient;
    private final static String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private final static int REQCODE_PERMISSIONS = 1111;
    private double longitude;
    private double latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_edit);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(this);
        mDateEdit= (EditText) findViewById(R.id.dateEdit);
        mTitleEdit= (EditText) findViewById(R.id.titleEdit);
        mCompanyEdit =(EditText)findViewById(R.id.companyEdit);
        mDetailEdit = (EditText) findViewById(R.id.detailEdit);
        mReturn = (Button) findViewById(R.id.returnbutton);
        mMap = (Button) findViewById(R.id.mapbutton);
        mDelete = (Button) findViewById(R.id.delete);
        long scheduleId = getIntent().getLongExtra("schedule_id", -1);
        if (scheduleId != -1) {
            Realm realm = Realm.getInstance(this);
            RealmResults<Schedule> results = realm.where(Schedule.class)
                    .equalTo("id", scheduleId).findAll();
            Schedule schedule = results.first();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            String date = sdf.format(schedule.getDate());
            mDateEdit.setText(date);
            mTitleEdit.setText(schedule.getTitle());
            mCompanyEdit.setText(schedule.getCompany());
            mDetailEdit.setText(schedule.getDetail());
            mDelete.setVisibility(View.VISIBLE);
        } else {
            mDelete.setVisibility(View.INVISIBLE);
        }
    }

    public void onSaveTapped(View view) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date();
        try {
            date = sdf.parse(mDateEdit.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long scheduleId = getIntent().getLongExtra("schedule_id", -1);
        if (scheduleId != -1) {
            Realm realm = Realm.getInstance(this);
            RealmResults<Schedule> results = realm.where(Schedule.class)
                    .equalTo("id", scheduleId).findAll();
            realm.beginTransaction();
            Schedule schedule = results.first();
            schedule.setDate(date);
            schedule.setTitle(mTitleEdit.getText().toString());
            schedule.setCompany(mCompanyEdit.getText().toString());
            schedule.setDetail(mDetailEdit.getText().toString());
            realm.commitTransaction();
            Snackbar.make(findViewById(android.R.id.content), "アップデートしました"
                    , Snackbar.LENGTH_SHORT)
                    .setAction("戻る", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    })
                    .setActionTextColor(Color.YELLOW)
                    .show();
        } else {
            Realm realm = Realm.getInstance(this);
            realm.beginTransaction();
            Number maxId = realm.where(Schedule.class).max("id");
            long nextId = 1;
            if (maxId != null) nextId = maxId.longValue() + 1;
            Schedule schedule = realm.createObject(Schedule.class);
            schedule.setId(nextId);
            schedule.setDate(date);
            schedule.setTitle(mTitleEdit.getText().toString());
            schedule.setCompany(mCompanyEdit.getText().toString());
            schedule.setDetail(mDetailEdit.getText().toString());
            realm.commitTransaction();
            Toast.makeText(this, "追加しました", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void onReturnTapped(View view){
        finish();
    }

    public void onDeleteTapped(View view) {
        long scheduleId = getIntent().getLongExtra("schedule_id", -1);
        if (scheduleId != -1) {
            Realm realm = Realm.getInstance(this);
            RealmResults<Schedule> results = realm.where(Schedule.class)
                    .equalTo("id", scheduleId).findAll();
            realm.beginTransaction();
            results.remove(0);
            realm.commitTransaction();
            Toast.makeText(this, "削除しました", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    public void onMapTapped(View view){
        System.out.println(latitude+"  "+longitude);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
        intent.setData(Uri.parse("http://maps.google.com/maps?saddr="+latitude+","+longitude+"&daddr="+mCompanyEdit.getText().toString()+"&dirflg=r"));
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");
        showLastLocation(true);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int reqCode,
                                           @NonNull String[] permissions, @NonNull int[] grants) {
        Log.d(TAG, "onRequestPermissionsResult");
        switch (reqCode) {
            case REQCODE_PERMISSIONS:
                showLastLocation(false);
                break;
        }
    }

    private void showLastLocation(boolean reqPermission) {
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                if (reqPermission) {
                    ActivityCompat.requestPermissions(this, PERMISSIONS, REQCODE_PERMISSIONS);
                }
                else {
                    Toast.makeText(this, getString(R.string.toast_requires_permission, permission),
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
        Location loc = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (loc != null) {
            latitude = loc.getLatitude();
            longitude = loc.getLongitude();
        }
    }

    @Override
    public void onPlaceSelected(Place place) {
        mCompanyEdit.setText(place.getAddress());
    }

    @Override
    public void onError(Status status) {

    }
}

