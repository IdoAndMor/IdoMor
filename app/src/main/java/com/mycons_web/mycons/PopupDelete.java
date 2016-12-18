package com.mycons_web.mycons;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

public class PopupDelete extends Activity
{
    static boolean delete = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popupdeletewindow);

        delete = false;
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.45),(int)(height*.45));

        Button yesButton = (Button)findViewById(R.id.yesButton);
        Button noButton = (Button)findViewById(R.id.noButton);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete = true;
                setResult(Activity.RESULT_OK,getIntent());
                finish();
            }
        });
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete = false;
                setResult(Activity.RESULT_OK,getIntent());
                finish();
            }
        });
    }
}
