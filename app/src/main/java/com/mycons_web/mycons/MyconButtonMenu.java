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

public class MyconButtonMenu extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mycon_button_menu);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * .4), (int) (height * .4));

        Button shareMyconButton = (Button) findViewById(R.id.shareMyconButton);
        Button deleteMyconButton = (Button) findViewById(R.id.deletMyconButton);
        ImageView shareOrDeleteImageView = (ImageView)findViewById(R.id.shareOrDeleteImageView);
        Bitmap currentMycon = (Bitmap) getIntent().getParcelableExtra("bitmapMyconImage");
        shareOrDeleteImageView.setImageBitmap(currentMycon);

        shareMyconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkConnected()){
                    Toast.makeText(getBaseContext(), "No Internet connection!", Toast.LENGTH_LONG).show();
                    return;
                }

                Bitmap bitmap = (Bitmap) getIntent().getParcelableExtra("bitmapMyconImage");
                Intent returnIntent = new Intent(MyconButtonMenu.this, ShareMycon.class);
                returnIntent.putExtra("bitmapMyconImage",bitmap);
                startActivityForResult(returnIntent, 1921);
             //   finish();
            }
        });
        deleteMyconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_OK,getIntent());
                Intent returnIntent = new Intent(MyconButtonMenu.this, PopupDelete.class);
                startActivityForResult(returnIntent, 1919);
//                finish();
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       if (resultCode == RESULT_OK) {
            if (requestCode == 1919) { //ACTIVITY_AFTER_POPUP_DELETE
                finish();
            }
       }
        if(requestCode == 1921) {
            finish();
        }


    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}