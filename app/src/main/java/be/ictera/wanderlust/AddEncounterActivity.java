package be.ictera.wanderlust;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.AndroidCharacter;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import Entity.Encounter;
import Entity.EncounterPicture;
import ShowEncounters.ScreenSlidePageFragment;

public class AddEncounterActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int SHOW_PICTURE_REQUEST = 2;
    static int currentPicture = 0;
    public Encounter encounter;

    ImageView[] pics = new ImageView[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_encounter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        mImageView.getLayoutParams().height = 520;
        mImageView.getLayoutParams().width = 520;
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

        mImageView.setRotation(90);
    }

}
