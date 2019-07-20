package com.yupa.stuffshare;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.yupa.stuffshare.entity.Stuff;
import com.yupa.stuffshare.utils.ShowMessage;
import com.yupa.stuffshare.db.DBController;
import com.yupa.stuffshare.fragments.AboutCASFragment;

import java.io.File;

public class EditStuffActivity extends AppCompatActivity {


    private static final String TAG = "EditStuff_MAP";
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private boolean mLocationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private GoogleMap mMap;
    private Location mCurrentLocation;
    private final float DEFAULT_ZOOM = 15f;
    Stuff mStuff;
    Button btnUpdate;
    //DB
    private DBController dbController;
    //UI
    EditText edtName, edtQuantitiy, edtTag, edtDescription;

    AboutCASFragment casFragment = new AboutCASFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_stuff);
        Toolbar myToolbar = findViewById(R.id.edit_toolbar);
        setSupportActionBar(myToolbar);
        mStuff = (Stuff) getIntent().getExtras().get("stuff");
        ImageView iv = findViewById(R.id.ivUPhoto);
        File mPic = new File(mStuff.get_picture());
        iv.setImageURI(Uri.fromFile(mPic));
        if (isServiceOK()) {
            getLocationPermission();
        }
        final String pPath = mPic.getAbsolutePath();
        btnUpdate = findViewById(R.id.btnUpdate);
        // UI
        edtName = findViewById(R.id.edtUName);
        edtQuantitiy = findViewById(R.id.edtUQuantity);
        edtTag = findViewById(R.id.edtUTxtTag);
        edtDescription = findViewById(R.id.edtUDescription);
        edtName.setText(mStuff.get_name());
        edtQuantitiy.setText("" + mStuff.get_quantity());
        edtDescription.setText(mStuff.get_description());
        edtTag.setText(mStuff.get_tag());
        //implement update photo later.
        setUpdateButton(pPath);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                if (getSupportFragmentManager().findFragmentById(R.id.g_about) == null) {
                    getSupportFragmentManager().beginTransaction().addToBackStack(null)
                            .add(R.id.e_about, casFragment, "About").commit();
                }
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void setUpdateButton(String pPath) {
        final String path = pPath;
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //get all needed values
                String name = edtName.getText().toString();
                int quantity = 1;

                String tag = edtTag.getText().toString();
                String description = edtDescription.getText().toString();

                if (TextUtils.isEmpty(name)) {
                    edtName.setError("Stuff Name field is empty/not valid");
                    return;
                }
                if (TextUtils.isEmpty(edtQuantitiy.getText())) {
                    edtQuantitiy.setError("Quantity field is empty/not valid");
                    return;
                }

                try {
                    quantity = Integer.parseInt(edtQuantitiy.getText().toString());
                } catch (NumberFormatException e) {
                    ShowMessage.showCenter(EditStuffActivity.this, "need a number");
                    return;
                }
                if (quantity <= 0) {
                    ShowMessage.showCenter(EditStuffActivity.this, "Quantity has to be bigger than 0!");
                    return;
                }
                if (TextUtils.isEmpty(tag)) {
                    edtTag.setError("tag field is empty/not valid");
                    return;
                }
                if (TextUtils.isEmpty(description)) {
                    edtDescription.setError("description field is empty/not valid");
                    return;
                }
                //init db controller
                dbController = new DBController(EditStuffActivity.this);

                Stuff stuff = new Stuff();
                if (mCurrentLocation != null) {
                    stuff.set_latitude(mCurrentLocation.getAltitude());
                    stuff.set_longitude(mCurrentLocation.getLongitude());
                } else {// give default
                    stuff.set_latitude(0.00);
                    stuff.set_longitude(0.00);
                }
                stuff.set_id(mStuff.get_id());
                stuff.set_description(description);
                stuff.set_picture(path);
                stuff.set_tag(tag);
                stuff.set_name(name);
                stuff.set_quantity(quantity);
                dbController.updateStuff(stuff);
                Intent intent = new Intent(EditStuffActivity.this, MainActivity.class);
                startActivity(intent);
                EditStuffActivity.this.finish();

            }
        });
    }

    /**
     * detect google service available or not
     */
    public boolean isServiceOK() {
        Log.d(TAG, "is ok?");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "is ok");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "is not good:but we can fix it.");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, -1);
            dialog.show();
        } else {
            Toast.makeText(this, "we can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * get device location
     */
    private void getDeviceLocation() {
        FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionGranted) {

                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "found location1");
                            mCurrentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), DEFAULT_ZOOM);
                            mMap.setMyLocationEnabled(true);

                        } else {
                            ShowMessage.showCenter(EditStuffActivity.this, "No Location information!!!");
                            Log.d(TAG, "failed location1");
                        }
                    }
                });
            }

        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation SecurityException" + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    /**
     * set up basic map
     */
    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                getDeviceLocation();

            }
        });
    }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            initMap();
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * request permission
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i : grantResults) {
                        if (i != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    getLocationPermission();
                }
            }
        }
    }

}
