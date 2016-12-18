package com.mycons_web.mycons;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.rec.photoeditor.EditorActivity;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class MyconsKeyboard extends Activity implements OnClickListener {

    //int j = 2;
    public class ImageButtonWithName
    {
        private ImageButton imageButton;
        private String name;

        public String getImageButtonName(){return name;}
        public void setImageButtonName(String name){this.name=name;}

        public ImageButton getImageButton(){return imageButton;}
        public void setImageButton(ImageButton imageButton){this.imageButton=imageButton;}
    }

    private static final int NUM_ROWS = 50;
    private static final int NUM_COLS = 6;

    TableLayout table;
    ImageButtonWithName currentImageButton;
    private GoogleApiClient client;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mycons_keyboard);
        currentImageButton = new ImageButtonWithName();
        table = (TableLayout) findViewById(R.id.keyboardTableLayout);
        UpdateKeyborad();
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    public File createDirIfNotExists(String path) {
        File file = new File(path);

        if (!file.exists()) {
            if (!file.mkdir()) {
                Log.e("TravellerLog :: ", "Problem creating Image folder");
            }
        }
        return file;
    }

    public void UpdateKeyborad() {
        int count = table.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = table.getChildAt(i);
            if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
        }
        String path = String.format(getExternalCacheDir() + "//MyconsImge");
        File dir = createDirIfNotExists(path);
        File[] files = dir.listFiles();

        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            }
        });

        final File[] childFile = files;
        count = 0;
        Bitmap mycon = null;
        for (int row = 0; row < NUM_ROWS; row++) {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.MATCH_PARENT,
                    1.0f));

            table.addView(tableRow);

            for (int col = 0; col < NUM_COLS && count < childFile.length; col++) {
                final ImageButtonWithName imageButtonWithName = new ImageButtonWithName();
                imageButtonWithName.setImageButton(new ImageButton(this));
                mycon = BitmapFactory.decodeFile(childFile[count].getPath());

                imageButtonWithName.setImageButtonName(childFile[count].getName());
                imageButtonWithName.getImageButton().setImageBitmap(mycon);
                imageButtonWithName.getImageButton().setOnClickListener((View.OnClickListener) this);
                imageButtonWithName.getImageButton().setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        currentImageButton.setImageButton((ImageButton) v);
                        currentImageButton.setImageButtonName(imageButtonWithName.getImageButtonName());
                        Intent returnIntent = new Intent(MyconsKeyboard.this, MyconButtonMenu.class);
                        returnIntent.putExtra("bitmapMyconImage", (Bitmap)((BitmapDrawable)((ImageButton) v).getDrawable()).getBitmap());
                        startActivityForResult(returnIntent, 1919);
                        return true;
                    }
                });
                imageButtonWithName.getImageButton().setBackgroundColor(Color.WHITE);
                imageButtonWithName.getImageButton().setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT,
                        1.0f));

                tableRow.addView(imageButtonWithName.getImageButton());
                count++;
            }
        }
    }

    @Override
    public void onClick(final View v) {
        try {
            ImageButton key = (ImageButton) v;
            Bitmap ourImage = ((BitmapDrawable) key.getDrawable()).getBitmap();
            ImageButtonClicked(ourImage);
        } catch (Exception e) {
            //TODO;
        }
    }
    private void ImageButtonClicked(Bitmap buttonImageUri) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", buttonImageUri);

        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW,
                "MyconsKeyboard Page",
                Uri.parse("http://host/path"),
                Uri.parse("android-app://com.mycons_web.mycons/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW,
                "MyconsKeyboard Page",
                Uri.parse("http://host/path"),
                Uri.parse("android-app://com.mycons_web.mycons/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1919) { //ACTIVITY_AFTER_POPUP_DELETE
                String path = String.format(getExternalCacheDir()+"//MyconsImge");
                File dir = createDirIfNotExists(path);
                final File[] childFile =dir.listFiles();

                boolean deleteIt = PopupDelete.delete;
                if (deleteIt) {
                    String imageToDelete = currentImageButton.getImageButtonName();
                    for (int i = 0; i < childFile.length; i++) {
                        String fileName = childFile[i].getName();

                        if (fileName.equals(imageToDelete)) {
                            childFile[i].delete();
                            PopupDelete.delete = false;
                        }
                    }
                    Toast.makeText(getBaseContext(), "Deleted", Toast.LENGTH_SHORT).show();
                    startActivity(MyconsKeyboard.this.getIntent());
                    finish();
                }
            }
        }
    }
}