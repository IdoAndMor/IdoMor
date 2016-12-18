package com.rec.photoeditor.editoractivity;

import static com.rec.photoeditor.editoractivity.EditorSaveConstants.RESTORE_PREVIEW_BITMAP;
import static com.rec.photoeditor.editoractivity.EditorSaveConstants.RESTORE_SAVED_BITMAP;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

//import com.rec.photoeditor.R;
import com.mycons_web.mycons.PopupDelete;
import com.mycons_web.mycons.R;

import com.rec.photoeditor.graphics.ImageProcessor;
import com.rec.photoeditor.graphics.ImageProcessorListener;
import com.rec.photoeditor.graphics.commands.CropCommand;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SuppressWarnings("ALL")
public class CropActivity extends Activity implements ImageProcessorListener {
    private ImageProcessor imageProcessor;

    private CropImageView imageView;
    //private ImageView imageView;

    private ImageButton okButton;
    private ImageButton cancelButton;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop_editor);
        initializeComponents();
    }

    private void initializeComponents() {
        imageProcessor = ImageProcessor.getInstance();
        imageView = (CropImageView) findViewById(R.id.image_view);
        //imageView = (ImageView) findViewById(R.id.image_view);
        okButton = (ImageButton) findViewById(R.id.ok_button);
        okButton.setOnClickListener(okButtonListener);
        cancelButton = (ImageButton) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(cancelButtonListener);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        initializeValues();
    }

    private void initializeValues() {
        final Object data = getLastCustomerNonConfigurationInstance(); //$$
        //final Object data = getLastNonConfigurationInstance();
        if (data == null) {
            imageView.setImageBitmap(imageProcessor.getBitmap());
        } else {
            restoreSavedValues(data);
        }
    }

    private void restoreSavedValues(Object data) {
        Bundle savedValues = (Bundle) data;
        int bitmapToRead = savedValues.getInt("BITMAP");
        boolean isRunning = savedValues.getBoolean("IS_RUNNING");
        Rect roi = Rect.unflattenFromString(savedValues.getString("FLATTEN_ROI"));

        if (bitmapToRead == RESTORE_PREVIEW_BITMAP) {
           imageView.setImageBitmap(imageProcessor.getLastResultBitmap());
        } else {
            imageView.setImageBitmap(imageProcessor.getBitmap());
        }
        imageView.setRegionOfInterest(roi);

        if (isRunning) {
            onProcessStart();
            imageProcessor.setProcessListener(this);
        }

    }


    @Override
    public Object onRetainNonConfigurationInstance() {
        Bundle saveObject = new Bundle();
        if (imageProcessor.getLastResultBitmap() == null) {
            saveObject.putInt("BITMAP", RESTORE_SAVED_BITMAP);
        } else {
            saveObject.putInt("BITMAP", RESTORE_PREVIEW_BITMAP);
        }

        saveObject.putString("FLATTEN_ROI", imageView.getRegionOfInterest()
                .flattenToString());
        saveObject.putBoolean("IS_RUNNING", isProgressBarVisible());
        return saveObject;
    }


    @Override
    public Object getLastCustomerNonConfigurationInstance() {
        Bundle saveObject = new Bundle();
        if (imageProcessor.getLastResultBitmap() == null) {
            saveObject.putInt("BITMAP", RESTORE_SAVED_BITMAP);
        } else {
            saveObject.putInt("BITMAP", RESTORE_PREVIEW_BITMAP);
        }

        saveObject.putString("FLATTEN_ROI", imageView.getRegionOfInterest()
                .flattenToString());
        saveObject.putBoolean("IS_RUNNING", isProgressBarVisible());
        return saveObject;
    }

    private boolean isProgressBarVisible() {
        return progressBar.getVisibility() == View.VISIBLE ? true : false;
    }

    private OnClickListener okButtonListener = new OnClickListener() {
        public void onClick(View v) {
            runImageProcessor();
           Intent endCropWorkaroundIntent = new Intent(CropActivity.this, RotateActivity.class);
            endCropWorkaroundIntent.putExtra("AfterCrop", 'K');
            startActivity(endCropWorkaroundIntent);
            setResult(RESULT_OK);
            imageProcessor.save();
            finish();
        }
    };

    private void runImageProcessor() {
        CropCommand command = new CropCommand(imageView.getRegionOfInterest());
        imageProcessor.setProcessListener(this);
        imageProcessor.runCommand(command);
    }

    private OnClickListener cancelButtonListener = new OnClickListener() {
        public void onClick(View v) {
            setResult(RESULT_CANCELED);
            finish();
        }
    };

    public void onProcessStart() {
        // turn off buttons and show "processing" animation
        Log.i("Crop", "Start Processing");
        okButton.setEnabled(false);
        cancelButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void onProcessEnd(Bitmap result) {
        Log.i("Crop", "Start Processing");
        okButton.setEnabled(false);
        cancelButton.setEnabled(false);
        progressBar.setVisibility(View.INVISIBLE);
        imageView.setImageBitmap(result);
        imageView.invalidate();
        imageProcessor.save();

        imageView.setImageBitmap(imageProcessor.getBitmap()); //$$

        finish();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        initializeValues();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 8888) { //ACTIVITY_AFTER_FILTER_ACTIVITY_COMBINA
                    finish();
                }
            }
        }

}
