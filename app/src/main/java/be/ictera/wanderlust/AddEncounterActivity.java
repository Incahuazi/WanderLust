package be.ictera.wanderlust;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import Database.WanderLustDb;
import Database.WanderLustDbHelper;
import Entity.Encounter;
import Entity.EncounterPicture;
import Helper.helper;
import Sync.NetworkStateChecker;

import static Helper.helper.GetUTCdatetimeAsString;

public class AddEncounterActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int SHOW_PICTURE_REQUEST = 2;
    static int currentPicture = 0;
    public Encounter encounter;
    private static final int PERMISSION_REQUEST_CODE_LOCATION = 1;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private Location location;
    private NetworkStateChecker networkStateChecker = new NetworkStateChecker();

    ImageView[] pics = new ImageView[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_encounter);

        setupHideKeyboard(findViewById(R.id.tableLayout));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button buttonSave = (Button)findViewById(R.id.ButtonSaveEncounter);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onEncounterSave();
            }});

        this.pics[0] = (ImageView)findViewById(R.id.Pic1);
        this.pics[1] = (ImageView)findViewById(R.id.Pic2);
        this.pics[2] = (ImageView)findViewById(R.id.Pic3);

        for (int i = 0; i <3 ; i++) {
            final int item = i;
            pics[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener(item);
                }
            });
        }
        encounter = new Encounter();

        //start a client for location requests
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds
    }

    public static boolean checkPermission(String strPermission,Context _c,Activity _a){
        int result = ContextCompat.checkSelfPermission(_c, strPermission);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    private void getLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,getApplicationContext(),AddEncounterActivity.this)){

            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location != null) {
                Log.d("GetLocation", String.valueOf(location.getLatitude()));
                handleNewLocation(location);
            }
            else {
                Log.d("GetLocation", "No location detected, starting listener");
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "APP must have Location permissions",Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
    }

    private boolean validateInput(String strTextInputName, String errorMsg, int textInputLayoutId){

        if (strTextInputName ==null||strTextInputName.isEmpty()){
            TextInputLayout textInputLayout = (TextInputLayout)findViewById(textInputLayoutId);
            textInputLayout.setError(errorMsg);
            return false;
        }
        else return true;
    }

    private void onEncounterSave() {
        WanderLustDbHelper dbhelper = new WanderLustDbHelper(this);
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        ContentValues encounterValues = new ContentValues();

        EditText textInputName = (EditText)findViewById(R.id.TextInputName);
        EditText textInputMessage = (EditText)findViewById(R.id.TextInputMessage);
        EditText textInputLocationCity = (EditText)findViewById(R.id.TextInputLocationCity);
        EditText textInputLocationCountry = (EditText)findViewById(R.id.TextInputLocationCountry);

        String strTextInputName = textInputName.getText().toString();
        String strTextInputMessage = textInputMessage.getText().toString();
        String strTextInputLocationCity = textInputLocationCity.getText().toString();
        String strTextInputLocationCountry = textInputLocationCountry.getText().toString();

        boolean inputValid;
        inputValid = validateInput(strTextInputName, "Please enter your name", R.id.TextInputNameLayout);
        inputValid &= validateInput(strTextInputLocationCity, "Please enter your location", R.id.TextInputLocationCityLayout);
        inputValid &= validateInput(strTextInputLocationCountry, "Please enter your location", R.id.TextInputLocationCountryLayout);

        if (!inputValid){
            return;
        }

        encounterValues.put(WanderLustDb.EncounterTable.COLUMN_NAME_NAME, strTextInputName);
        encounterValues.put(WanderLustDb.EncounterTable.COLUMN_NAME_LOCATION_CITY, strTextInputLocationCity);
        encounterValues.put(WanderLustDb.EncounterTable.COLUMN_NAME_LOCATION_COUNTRY, strTextInputLocationCountry);
        if (this.location != null) {
            String locationLatLong = this.location.getLatitude() + ";" + this.location.getLongitude();
            encounterValues.put(WanderLustDb.EncounterTable.COLUMN_NAME_LOCATION_LATLONG, locationLatLong);
        }
        encounterValues.put(WanderLustDb.EncounterTable.COLUMN_NAME_MESSAGE, strTextInputMessage);
        encounterValues.put(WanderLustDb.EncounterTable.COLUMN_NAME_SYNCED, 0);
        encounterValues.put(WanderLustDb.EncounterTable.COLUMN_NAME_INSERTEDTIMESTAMP, GetUTCdatetimeAsString());

        db.beginTransaction();
        try {
            long newEncounterId = db.insert(WanderLustDb.EncounterTable.TABLE_NAME, null, encounterValues);
            for (EncounterPicture encounterPicture: this.encounter.encounterPicture
                 ) {
                if (encounterPicture != null && encounterPicture.imageFilePath !=""){
                    //picture exists
                    ContentValues encounterPictureValues = new ContentValues();
                    encounterPictureValues.put(WanderLustDb.EncounterPictureTable.COLUMN_NAME_ENCOUNTERID, newEncounterId);
                    encounterPictureValues.put(WanderLustDb.EncounterPictureTable.COLUMN_NAME_IMAGEFILEPATH, encounterPicture.imageFilePath);
                    encounterPictureValues.put(WanderLustDb.EncounterPictureTable.COLUMN_NAME_SYNCED, 0);

                    db.insert(WanderLustDb.EncounterPictureTable.TABLE_NAME, null, encounterPictureValues);
                }
            }
            db.setTransactionSuccessful();
        }
        catch (Exception e){
            Toast toast = Toast.makeText(this,"Error saving to db - " + e.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
        }
        finally{
            db.endTransaction();
        }
        networkStateChecker.tryToSync(this);
        this.finish();
    }

    protected void onClickListener(int item){
        if (encounter.encounterPicture[item] == null || encounter.encounterPicture[item].imageFilePath == ""){
            //no picture available yet
            dispatchTakePictureIntent(item);
        }
        else {
            //display current picture
            Intent intent = new Intent(this, DisplayPictureActivity.class);
            intent.putExtra("ImagePath", encounter.encounterPicture[item].imageFilePath);
            currentPicture = item;
            startActivityForResult(intent, SHOW_PICTURE_REQUEST);
        }
    }

    private void dispatchTakePictureIntent(int item) {

        encounter.encounterPicture[item] = new EncounterPicture();
        File imageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), UUID.randomUUID().toString());
        encounter.encounterPicture[item].imageFilePath = imageFile.getAbsolutePath();

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String providername = getApplicationContext().getPackageName() + ".provider";

        Uri photoURI = FileProvider.getUriForFile(this,
                "be.ictera.wanderlust.fileprovider",
                imageFile);

        takePictureIntent.putExtra(
                MediaStore.EXTRA_OUTPUT,
                photoURI
        );

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            currentPicture = item;
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK){
                setPic(pics[currentPicture], encounter.encounterPicture[currentPicture].imageFilePath);
            }
            else{
                File imageFile = new File(this.encounter.encounterPicture[currentPicture].imageFilePath);
                imageFile.delete();
                this.encounter.encounterPicture[currentPicture].imageFilePath = "";
            }
        }
        if (requestCode == SHOW_PICTURE_REQUEST && resultCode == RESULT_OK) {
            String buttonClicked = data.getStringExtra("ButtonClicked");

            switch (buttonClicked){
                case "SaveClicked":
                    //do nothing
                    break;
                case "DeleteClicked":
                    File imageFile = new File(this.encounter.encounterPicture[currentPicture].imageFilePath);
                    imageFile.delete();

                    ImageView imageView = pics[currentPicture];
                    imageView.setImageResource(android.R.drawable.ic_menu_camera);
                    imageView.setRotation(0);

                    Resources r = getResources();
                    imageView.getLayoutParams().height = r.getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
                    this.encounter.encounterPicture[currentPicture].imageFilePath = "";
                    break;
            }

        }
    }

    private void setPic(ImageView mImageView, String imageFilePath) {

        mImageView.getLayoutParams().height = 620;
        mImageView.getLayoutParams().width = 620;
        mImageView.requestLayout();
        File picture = new File(imageFilePath);
        Glide.with(this).load(picture).into(mImageView);

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imageFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

//        mImageView.setRotation(90);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("OnConnectionFailed", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        boolean foo = mGoogleApiClient.isConnected();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    private void handleNewLocation(Location location){
        Log.d("GetLocation", String.valueOf(location.getLatitude()));
        this.location = location;
    }

    public void setupHideKeyboard(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    helper.hideSoftKeyboard(AddEncounterActivity.this);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupHideKeyboard(innerView);
            }
        }
    }
}
