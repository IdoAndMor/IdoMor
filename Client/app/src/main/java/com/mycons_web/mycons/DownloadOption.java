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

public class DownloadOption extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_option);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * .4), (int) (height * .4));

        Button downloadMyconButton = (Button) findViewById(R.id.downloadMycons2Button);
        Button reportMyconButton = (Button) findViewById(R.id.reportMyconButton);
        ImageView shareOrDeleteImageView = (ImageView)findViewById(R.id.downloadOrReportImageView);
        final Bitmap currentMycon = (Bitmap) getIntent().getParcelableExtra("bitmapMyconImage");
        shareOrDeleteImageView.setImageBitmap(currentMycon);

        downloadMyconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkConnected()){
                    Toast.makeText(getBaseContext(), "No Internet connection!", Toast.LENGTH_LONG).show();
                    return;
                }
                getIntent().putExtra("downloadMycon",true);
                getIntent().putExtra("bitmapMyconImage", currentMycon);
                setResult(RESULT_OK,getIntent());
                finish();
            }
        });
        reportMyconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = getIntent().getStringExtra("getId");
                Intent intent = new Intent(DownloadOption.this, ReportMycon.class);
                intent.putExtra("getId",id);
                startActivity(intent);
                finish();
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
