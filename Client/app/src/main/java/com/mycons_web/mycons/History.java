package com.mycons_web.mycons;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;


import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class History extends Activity {

    private class ImageButtonWithName {
        private ImageButton imageButton;
        private String name;
        public String getImageButtonName() {
            return name;
        }
        public void setImageButtonName(String name) {
            this.name = name;
        }
        public ImageButton getImageButton() {
            return imageButton;
        }
        public void setImageButton(ImageButton imageButton) {
            this.imageButton = imageButton;
        }
    }

    private static final int NUM_ROWS = 25;
    private static final int NUM_COLS = 2;

    TableLayout table;
    ImageButtonWithName currentImageButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        table = (TableLayout) findViewById(R.id.historyTableLayout);

        Button deleteAllHistoryButton = (Button) findViewById(R.id.deleteAllHistoryButton);
        deleteAllHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent(History.this, PopupDelete.class);
                startActivityForResult(returnIntent, 1920);  //DELETE_ALL_HISTORY

            }
        });

        currentImageButton = new ImageButtonWithName();

        UpdateHistory();

    }

    public void deleteAllHistory() {
        String path = String.format(getExternalCacheDir() + "//Msgs");
        File dir = createDirIfNotExists(path);
        final File[] childFile = dir.listFiles();
        for (int i = 0; i < childFile.length; i++)
            childFile[i].delete();

        Toast.makeText(getBaseContext(), "Deleted", Toast.LENGTH_SHORT).show();
        startActivity(History.this.getIntent());
        finish();
    }

    public void UpdateHistory() {
        System.gc();
        String pathMsgs = String.format(getExternalCacheDir() + "//Msgs");
        File dir = createDirIfNotExists(pathMsgs);
        File[] files = dir.listFiles();

        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            }
        });

        final File[] childFile = files;

        int count = 0;
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
                imageButtonWithName.getImageButton().setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            currentImageButton.setImageButton((ImageButton) v);
                            currentImageButton.setImageButtonName(imageButtonWithName.getImageButtonName());
                            ImageButton key = (ImageButton) v;
                            Bitmap ourImage = ((BitmapDrawable) key.getDrawable()).getBitmap();
                            ImageButtonClicked(ourImage);
                        } catch (Exception e) {
                            //TODO;
                        }
                    }});

            imageButtonWithName.getImageButton().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    currentImageButton.setImageButton((ImageButton) v);
                    currentImageButton.setImageButtonName(imageButtonWithName.getImageButtonName());
                    Intent returnIntent = new Intent(History.this, PopupDelete.class);
                    startActivityForResult(returnIntent, 1919);
                    return true;
                }
            });
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
    public void onBackPressed() {
        finish();
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

    private void ImageButtonClicked(Bitmap buttonImageUri) {
        Uri uri = null;
        String pathMsgs = String.format(getExternalCacheDir() + "//Msgs");
        File dir = createDirIfNotExists(pathMsgs);
        final File[] childFile = dir.listFiles();
//        String imageToSand = currentImageButton.getTransitionName();
        String imageToSand = currentImageButton.getImageButtonName();
        String fileName = null;
        for (int i = 0; i < childFile.length; i++) {
            fileName = childFile[i].getName();
            if (fileName.equals(imageToSand)) {
                uri = Uri.fromFile(childFile[i]);
            }
        }

        Intent returnIntent = new Intent();
        returnIntent.putExtra("currentMsg", uri);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1919) { //ACTIVITY_AFTER_POPUP_DELETE
                String path = String.format(getExternalCacheDir() + "//Msgs");
                File dir = createDirIfNotExists(path);
                final File[] childFile = dir.listFiles();

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
                    startActivity(History.this.getIntent());
                    finish();
                }
            }
            if (requestCode == 1920)  //DELETE_ALL_HISTORY
            {
                deleteAllHistory();
            }
        }
    }
}

