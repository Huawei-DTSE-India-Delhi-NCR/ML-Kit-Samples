package com.ml.location;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.huawei.hms.mlsdk.landmark.MLRemoteLandmark;
import com.ml.location.utils.GetGeoDetailsListener;
import com.ml.location.utils.LandmarkTransactorMl;
import com.ml.location.utils.MlAddPictureDialog;
import com.ml.location.utils.MlBitmapUtils;
import com.ml.location.utils.MlGraphicOverlay;
import com.ml.location.utils.MlImageTransactor;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    String TAG = "MainActivity";

    private String KEY_IMAGE_URI = "KEY_IMAGE_URI";
    private String KEY_IMAGE_MAX_WIDTH = "KEY_IMAGE_MAX_WIDTH";
    private String KEY_IMAGE_MAX_HEIGHT = "KEY_IMAGE_MAX_HEIGHT";

    private Integer REQUEST_TAKE_PHOTO = 1;
    private Integer REQUEST_SELECT_IMAGE = 2;

    private Button getImageButton= null;
    private ImageView preview = null;
    private TextView hintText = null;

    private MlGraphicOverlay mlGraphicOverlay = null;

    boolean isLandScape = false;

    private Uri imageUri= null;
    private Integer maxWidthOfImage = null;
    private Integer maxHeightOfImage = null;
    private MlImageTransactor mlImageTransactor = null;

    private Bitmap imageBitmap= null;

    private MlAddPictureDialog addPictureDialog = null;

    private FrameLayout progressLayout;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.hms_lank_mark_layout);
        getSupportActionBar().setTitle("Find Landmark Location");

        initView();

        checkPermissions();
    }


    /**
     *  Initialize the view
     */
    private void initView(){

        preview = findViewById(R.id.still_preview);
        mlGraphicOverlay = findViewById(R.id.still_overlay);
        getImageButton = findViewById(R.id.getImageButton);
        progressLayout= findViewById(R.id.progress_layout);

        hintText=findViewById(R.id.hint_text);
        getImageButton.setOnClickListener(this);
        isLandScape = this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        // This is mainly related to fetch the location details
        mlImageTransactor = new LandmarkTransactorMl(new GetGeoDetailsListener() {
            @Override
            public void onSuccess(List<MLRemoteLandmark> results) {
                // Final result
                Log.d(TAG,results.get(0).getLandmark());
                showProgress(false);
            }

            @Override
            public void onFailed(String message) {
                Log.d(TAG+"_ERROR",message);
                Toast.makeText(MainActivity.this,message,Toast.LENGTH_LONG).show();
                showProgress(false);
            }
        });

        createDialog();

    }

    /**
     *  Create Dialog
     */
    private void createDialog() {
        addPictureDialog = new MlAddPictureDialog(this);

        addPictureDialog.setClickListener(new MlAddPictureDialog.ClickListener() {
            @Override
            public void takePicture() {
                startCamera();
            }

            @Override
            public void selectImage() {
                selectLocalImage();
            }

            @Override
            public void doExtend() {

            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.getImageButton) {
            hintText.setVisibility(View.GONE);
            this.showDialog();
        }
    }

    /**
     *  Show the add Picture dialog
     */
    private void showDialog() {
        addPictureDialog.show();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_IMAGE_URI, imageUri);
        if (maxWidthOfImage != null) {
            outState.putInt(KEY_IMAGE_MAX_WIDTH, maxWidthOfImage);
        }
        if (maxHeightOfImage != null) {
            outState.putInt(KEY_IMAGE_MAX_HEIGHT, maxHeightOfImage);
        }
    }

    /**
     *  Redirect to Gallery
     */
    private void selectLocalImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        this.startActivityForResult(intent, REQUEST_SELECT_IMAGE);
    }

    /**
     *  Redirect to camera view
     */
    private void startCamera() {
        imageUri = null;
        preview.setImageBitmap(null);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "New Picture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
            imageUri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            this.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            showProgress(true);
            loadImageAndSetTransactor();
        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_CANCELED) {
           // finish();
        } else if (requestCode == REQUEST_SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                imageUri = data.getData();
            }
            showProgress(true);
            loadImageAndSetTransactor();

        } else if (requestCode == REQUEST_SELECT_IMAGE && resultCode == Activity.RESULT_CANCELED) {
          //  finish();
        }

    }


    /**
     *  Loading the bitmap image finding the location
     */
    private void loadImageAndSetTransactor() {
        if (imageUri == null) {
            return;
        }
        mlGraphicOverlay.clear();
      //  mHandler.postDelayed(myRunnable, TIMEOUT.longValue());
        imageBitmap = MlBitmapUtils.loadFromPath(this,imageUri,getMaxWidthOfImage(), getMaxHeightOfImage());
        preview.setImageBitmap(imageBitmap);
        if (imageBitmap != null) {
            mlImageTransactor.process(imageBitmap, mlGraphicOverlay);
        }
    }

    /**
     *  Maximum width of image
     * @return
     */
    private Integer getMaxWidthOfImage() {
        if (maxWidthOfImage == null || maxWidthOfImage == 0) {
            if (isLandScape) {
                maxWidthOfImage = ((View)(preview.getParent())).getHeight();
            } else {
                maxWidthOfImage = ((View)(preview.getParent())).getWidth();
            }
        }
        return maxWidthOfImage;
    }

    /**
     *  Maximum height of image
     * @return
     */
    private Integer getMaxHeightOfImage() {
        if (maxHeightOfImage == null || maxHeightOfImage == 0) {
            if (isLandScape) {
                maxHeightOfImage = ((View)(preview.getParent())).getWidth();
            } else {
                maxHeightOfImage = ((View)(preview.getParent())).getHeight();
            }
        }
        return maxHeightOfImage;
    }

    /**
     *  Permissions checking
     */
    private void checkPermissions() {
        //Check Permissions
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            Log.i("TAG", "sdk < 28 Q");
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            ) {
                String[] strings = new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE};

                ActivityCompat.requestPermissions(this, strings, 1);
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            )  != PackageManager.PERMISSION_GRANTED
            ) {
                String[] strings = new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                };
                ActivityCompat.requestPermissions(this, strings, 2);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                Log.i(TAG, "onRequestPermissionsResult: apply READ PERMISSION successful");
            } else {
                Log.i(TAG, "onRequestPermissionsResult: apply READ PERMISSSION  failed");
            }
        }
        if (requestCode == 2) {
            if (grantResults.length > 2 && grantResults[2] == PackageManager.PERMISSION_GRANTED && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                Log.i(
                        TAG,
                        "onRequestPermissionsResult: apply WRITE successful"
                );
            } else {
                Log.i(TAG, "onRequestPermissionsResult: apply WRITE  failed");
            }
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mlImageTransactor != null) {
            mlImageTransactor.stop();
            mlImageTransactor = null;
        }
        imageUri = null;


    }

    private void showProgress(boolean showProgress){
        if(showProgress)
            progressLayout.setVisibility(View.VISIBLE);
        else
            progressLayout.setVisibility(View.GONE);
    }



}
