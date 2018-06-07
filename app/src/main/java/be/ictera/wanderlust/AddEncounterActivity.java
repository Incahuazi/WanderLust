package be.ictera.wanderlust;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import Database.WanderLustDb;
import Database.WanderLustDbHelper;
import Entity.Encounter;
import Entity.EncounterPicture;
import Helper.GlideApp;
import Helper.helper;
import sync.NetworkStateChecker;

import static Helper.helper.GetUTCdatetimeAsString;

public class AddEncounterActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int SHOW_PICTURE_REQUEST = 2;
    private static final String BUNDLE_DATA_ENCOUNTER = "EncounterJson";
    private static final String BUNDLE_DATA_CURRENTPIC = "CurrentPicture";

    static int currentPicture = 0;
    public Encounter encounter;
    private static final int PERMISSION_REQUEST_CODE_LOCATION = 1;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private Location location;
    private NetworkStateChecker networkStateChecker = new NetworkStateChecker();

    private WanderLustDbHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private String LanguageCode = "ENG";

    ImageView[] pics = new ImageView[4];
    private String ErrorValidationName = "Please enter your name";
    private String ErrorValidationLocation = "Please enter your location";
    private String ErrorValidationEmail = "Please enter a valid email address";
    private String ErrorTakeProfilePicture = "PLease take a profile picture";

    private EditText textInputName;
    private EditText textInputMessage;
    private EditText textInputLocationCity;
    private EditText textInputLocationCountry;
    private EditText textInputEmail;

    private boolean activityIsClosing = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_encounter);

        dbHelper = new WanderLustDbHelper(this);
        db = dbHelper.getReadableDatabase();

        LanguageCode= getIntent().getStringExtra("LanguageCode");
        ResolveLanguageResource(LanguageCode);

        setupHideKeyboard(findViewById(R.id.linearLayout));

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
        this.pics[3] = (ImageView)findViewById(R.id.Pic4);

        textInputName = (EditText)findViewById(R.id.TextInputName);
        textInputMessage = (EditText)findViewById(R.id.ADDETextInputMessage);
        textInputLocationCity = (EditText)findViewById(R.id.TextInputLocationCity);
        textInputLocationCountry = (EditText)findViewById(R.id.TextInputLocationCountry);
        textInputEmail = (EditText)findViewById(R.id.TextInputEmail);

        for (int i = 0; i <4 ; i++) {
            final int item = i;
            pics[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener(item);
                }
            });
        }

        //Check if there is an ongoing encounter being added
        SharedPreferences foo = getPreferences(MODE_PRIVATE);
        String savedEncounterString = foo.getString(BUNDLE_DATA_ENCOUNTER, "");
        if (savedEncounterString != null && !savedEncounterString.isEmpty()){
            //restore previous encounter
            Gson gson = new Gson();
            encounter = gson.fromJson(savedEncounterString, Encounter.class);
            for (int i=0;i<encounter.encounterPicture.length;i++){
                if (encounter.encounterPicture[i] !=null){
                    String imageFilePath = encounter.encounterPicture[i].imageFilePath;
                    if (imageFilePath !=null && !TextUtils.isEmpty(imageFilePath)){
                        setPic(pics[i], encounter.encounterPicture[i].imageFilePath, i==0);
                    }
                }
            }
            textInputName.setText(encounter.Name);
            textInputMessage.setText(encounter.Message);
            textInputLocationCity.setText(encounter.LocationCity);
            textInputLocationCountry.setText(encounter.LocationCountry);
            textInputEmail.setText(encounter.EmailAddress);
        }
        else {
            encounter = new Encounter();
        }
        int savedCurrentPicture = foo.getInt(BUNDLE_DATA_CURRENTPIC,  -1);
        if (savedCurrentPicture!=-1){
            currentPicture = savedCurrentPicture;
        }

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

    private boolean validateInputEmail(String strTextInputEmail, String errorMsg, int textInputLayoutId) {
        if (!(strTextInputEmail ==null||strTextInputEmail.isEmpty())){
            //email address filled in, check if it has valid format

            if (android.util.Patterns.EMAIL_ADDRESS.matcher(strTextInputEmail).matches()==false){
                TextInputLayout textInputLayout = (TextInputLayout)findViewById(textInputLayoutId);
                textInputLayout.setError(errorMsg);
                return false;
            }
            else return true;
        }
        else {
            //email address is optional
            return true;
        }
    }

    private void onEncounterSave() {
        WanderLustDbHelper dbhelper = new WanderLustDbHelper(this);
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        ContentValues encounterValues = new ContentValues();

        String strTextInputName = textInputName.getText().toString();
        String strTextInputMessage = textInputMessage.getText().toString();
        String strTextInputLocationCity = textInputLocationCity.getText().toString();
        String strTextInputLocationCountry = textInputLocationCountry.getText().toString();
        String strTextInputEmail = textInputEmail.getText().toString();

        boolean inputValid;
        inputValid = validateInput(strTextInputName, this.ErrorValidationName, R.id.TextInputNameLayout);
        inputValid &= validateInput(strTextInputLocationCity, this.ErrorValidationLocation, R.id.TextInputLocationCityLayout);
        inputValid &= validateInput(strTextInputLocationCountry, this.ErrorValidationLocation, R.id.TextInputLocationCountryLayout);
        inputValid &= validateInputEmail(strTextInputEmail, this.ErrorValidationEmail, R.id.TextInputEmailLayout);


        if (!inputValid){
            return;
        }
        String newEncounterId = UUID.randomUUID().toString();
        encounterValues.put(WanderLustDb.EncounterTable._ID, newEncounterId);
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
        encounterValues.put(WanderLustDb.EncounterTable.COLUMN_NAME_EMAILADDRESS, strTextInputEmail);

        //check if there is at least a profile picture
        //this is picture with id 0

        if (this.encounter.encounterPicture[0] == null || TextUtils.isEmpty(this.encounter.encounterPicture[0].imageFilePath)){
            Toast myToast = Toast.makeText(this, ErrorTakeProfilePicture, Toast.LENGTH_LONG);
            myToast.setGravity(Gravity.CENTER,0,0);
            myToast.show();
            return;
        }

        db.beginTransaction();
        try {
            db.insert(WanderLustDb.EncounterTable.TABLE_NAME, null, encounterValues);
            for (EncounterPicture encounterPicture: this.encounter.encounterPicture
                 ) {
                if (encounterPicture != null && !TextUtils.isEmpty(encounterPicture.imageFilePath)){
                    //picture exists
                    ContentValues encounterPictureValues = new ContentValues();
                    encounterPictureValues.put(WanderLustDb.EncounterPictureTable._ID, UUID.randomUUID().toString());
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

        //clear the current SharedPreferences
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
        activityIsClosing = true;

        networkStateChecker.tryToSync(this);
        Intent i = new Intent(getApplicationContext(), ThanksActivity.class);
        i.putExtra("LanguageCode", LanguageCode);
        this.finish();
        startActivity(i);
    }

    protected void onClickListener(int item){
        if (encounter.encounterPicture[item] == null || TextUtils.isEmpty(encounter.encounterPicture[item].imageFilePath)){
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
                setPic(pics[currentPicture], encounter.encounterPicture[currentPicture].imageFilePath, currentPicture==0);
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
//                    imageView.setRotation(0);

//                    Resources r = getResources();
//                    imageView.getLayoutParams().height = r.getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
                    this.encounter.encounterPicture[currentPicture].imageFilePath = "";
                    break;
            }

        }
    }

    private void setPic(ImageView mImageView, String imageFilePath, boolean circular) {
        try {
            File picture = new File(imageFilePath);
//        Glide.with(this).load(picture).into(mImageView);

            if (circular){
                GlideApp.with(this)
                        .load(picture)
                        .fitCenter()
                        .apply(RequestOptions.circleCropTransform())
                        //.centerCrop()
                        .into(mImageView);
            } else {
                GlideApp.with(this)
                        .load(picture)
                        //.centerCrop()
                        //.fitCenter()
                        .into(mImageView);
            }

            ExifInterface exif = null;
            try {
                exif = new ExifInterface(imageFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

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

        if (!activityIsClosing) {

            //add the current textinput to the encount
            String strTextInputName = textInputName.getText().toString();
            String strTextInputMessage = textInputMessage.getText().toString();
            String strTextInputLocationCity = textInputLocationCity.getText().toString();
            String strTextInputLocationCountry = textInputLocationCountry.getText().toString();
            String strTextInputEmail = textInputEmail.getText().toString();

            encounter.EmailAddress = strTextInputEmail;
            encounter.LocationCity = strTextInputLocationCity;
            encounter.LocationCountry = strTextInputLocationCountry;
            encounter.Message = strTextInputMessage;
            encounter.Name = strTextInputName;

            //save encounter to shared preferences
            SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            Gson gson = new Gson();
            String encounterJson = gson.toJson(encounter);
            editor.putString(BUNDLE_DATA_ENCOUNTER, encounterJson);

            //save currentPicture
            editor.putInt(BUNDLE_DATA_CURRENTPIC, currentPicture);
            editor.apply();
        }

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


    private void ResolveLanguageResource(String languageCode) {
        Map<String, String> resLang = dbHelper.GetAllTextResouceForActivityAndLanguage("AddEncounterActivity", languageCode, db);

        if (resLang.containsKey("ADDEErrorName")) {
            this.ErrorValidationName = resLang.get("ADDEErrorName");
        }

        if (resLang.containsKey("ADDEErrorLocation")) {
            this.ErrorValidationLocation = resLang.get("ADDEErrorLocation");
        }

        if (resLang.containsKey("ADDEErrorEmail")) {
            this.ErrorValidationEmail = resLang.get("ADDEErrorEmail");
        }

        if (resLang.containsKey("ADDEStep1")) {
            TextView textView = (TextView)findViewById(R.id.ADDEStep1);
            textView.setText(resLang.get("ADDEStep1"));
        }

        if (resLang.containsKey("ADDEStep1SubText")) {
            TextView textView = (TextView)findViewById(R.id.ADDEStep1SubText);
            textView.setText(resLang.get("ADDEStep1SubText"));
        }

        if (resLang.containsKey("ADDEStep1MorePics")) {
            TextView textView = (TextView)findViewById(R.id.ADDEStep1MorePics);
            textView.setText(resLang.get("ADDEStep1MorePics"));
        }

        if (resLang.containsKey("ADDEStep2")) {
            TextView textView = (TextView)findViewById(R.id.ADDEStep2);
            textView.setText(resLang.get("ADDEStep2"));
        }

        if (resLang.containsKey("ADDEMoreInfo")) {
            TextView textView = (TextView)findViewById(R.id.ADDEMoreInfo);
            textView.setText(resLang.get("ADDEMoreInfo"));
        }

        if (resLang.containsKey("ADDETitle")) {
            this.setTitle(resLang.get("ADDETitle"));
        }

        if (resLang.containsKey("ADDETextInputName")) {
            TextInputLayout textView = (TextInputLayout)findViewById(R.id.TextInputNameLayout);
            textView.setHint (resLang.get("ADDETextInputName"));
        }

        if (resLang.containsKey("ADDETextInputMessage")) {
            TextInputLayout textView = (TextInputLayout)findViewById(R.id.TextInputMessageLayout);
            textView.setHint (resLang.get("ADDETextInputMessage"));
        }

        if (resLang.containsKey("ADDETextInputEmail")) {
            TextInputLayout textView = (TextInputLayout)findViewById(R.id.TextInputEmailLayout);
            textView.setHint (resLang.get("ADDETextInputEmail"));
        }

        if (resLang.containsKey("ADDEInputLocationCity")) {
            TextInputLayout textView = (TextInputLayout)findViewById(R.id.TextInputLocationCityLayout);
            textView.setHint (resLang.get("ADDEInputLocationCity"));
        }

        if (resLang.containsKey("ADDEInputLocationCountry")) {
            TextInputLayout textView = (TextInputLayout)findViewById(R.id.TextInputLocationCountryLayout);
            textView.setHint (resLang.get("ADDEInputLocationCountry"));
        }

        if (resLang.containsKey("ADDEButtonSave")) {
            Button buttonSave = (Button)findViewById(R.id.ButtonSaveEncounter);
            buttonSave.setText (resLang.get("ADDEButtonSave"));
        }

        if (resLang.containsKey("ADDEErrorAddProfilePic")) {
            ErrorTakeProfilePicture = resLang.get("ADDEErrorAddProfilePic");
        }


//
//        if (resLang.containsKey("TIDStep3Header")) {
//            TextView textView = (TextView)findViewById(R.id.TIDStep3Header);
//            textView.setText(resLang.get("TIDStep3Header"));
//        }
//
//        if (resLang.containsKey("TIDTitle")) {
//            TextView textView = (TextView)findViewById(R.id.TIDTitle);
//            textView.setText(resLang.get("TIDTitle"));
//        }
    }
}
