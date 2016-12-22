package com.mycons_web.mycons;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.rec.photoeditor.EditorActivity;

public class NewMyconMenu extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_mycon_menu);

        Button createMyconsButton =(Button) findViewById(R.id.createMyconButton);
        Button downloadMyconsButton =(Button) findViewById(R.id.downloadButton);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * .4), (int) (height * .2));

        createMyconsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createMyconsIntent = new Intent(NewMyconMenu.this, EditorActivity.class);
                startActivityForResult(createMyconsIntent, 1880);
            }
        });


        downloadMyconsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent downloadMyconsIntent = new Intent(NewMyconMenu.this, DownloadMycons.class);
                startActivityForResult(downloadMyconsIntent, 1880);
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1880) { //RETURN_FROM_CREATE_MYCON
            finish();
        }
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}