package com.search;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.mycons_web.mycons.R;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BingSearch extends AppCompatActivity implements View.OnClickListener {

    private EditText bingSearchEditText;
    private Button bingSearchButton;

    private static final int NUM_ROWS = 7;
    private static final int NUM_COLS = 3;

    TableLayout table;
    ImageButton currentImageButton;
    TextView loadingTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_bing);
        bingSearchEditText = (EditText) findViewById(R.id.BingSearchEditText);
        loadingTextView = (TextView) findViewById(R.id.loadingTextView);

        table = (TableLayout) findViewById(R.id.searchTableLayout);
        String pathMycons = String.format(getExternalCacheDir() + "//BingSeachTemp");
        File dir = createDirIfNotExists(pathMycons);
        if (dir.exists()) {
            final File[] childFile = dir.listFiles();
            Bitmap[] bitmaps = new Bitmap[NUM_ROWS * NUM_COLS];
            for (int i = 0; i < bitmaps.length && i < childFile.length; i++) {
                bitmaps[i] = BitmapFactory.decodeFile(childFile[i].getPath());
            }
            UpdatePicsResultsView(bitmaps);
        }
        Button deleteSearchButton = (Button) findViewById(R.id.deleteSearchButton);
        deleteSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pathMycons = String.format(getExternalCacheDir() + "//BingSeachTemp");
                File dir = createDirIfNotExists(pathMycons);
                if (dir.exists()) {
                    final File[] childFile = dir.listFiles();
                    for (int i = 0; i < childFile.length; i++) {
                        childFile[i].delete();
                    }
                    dir.delete();
                }
                int count = table.getChildCount();
                for (int i = 0; i < count; i++) {
                    View child = table.getChildAt(i);
                    if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
                }
            }
        });
        bingSearchButton = (Button) findViewById(R.id.BingSearchButton);
        bingSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                if (!isNetworkConnected()){
                    Toast.makeText(getBaseContext(), "No Internet connection!", Toast.LENGTH_LONG).show();
                    return;
                }
                String pathMycons = String.format(getExternalCacheDir() + "//BingSeachTemp");
                File dir = createDirIfNotExists(pathMycons);
                if (dir.exists()) {
                    final File[] childFile = dir.listFiles();
                    for (int i = 0; i < childFile.length; i++) {
                        childFile[i].delete();
                    }
                    dir.delete();
                }
                String wordToSearch = bingSearchEditText.getText().toString();

                wordToSearch = wordToSearch.replace(' ', '+');
                new BingAsyncTask().execute(wordToSearch);
            }
        });

    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED, getIntent());
        finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class BingAsyncTask extends AsyncTask<String, Void, Bitmap[]> {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.bingSerachProgressBar);
        private String APILink;
        private String API_KEY = "4S+6DFRM4KonYFoYAOqxHIrAfM6iMRI9YeU5GUwktrs";
        private String[] SECTION = {"image"};

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap[] doInBackground(String... params) {
            String result = "";
            String wordToSearch = params[0];

            APILink = "https://api.datamarket.azure.com/Bing/Search/v1/Image?Query=%27" + wordToSearch + "%27&Market=%27en-US%27&Adult=%27Moderate%27&ImageFilters=%27Size%3ASmall%27&$format=json&$top=" + NUM_COLS * NUM_ROWS;
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(APILink);

            String auth = API_KEY + ":" + API_KEY;
            String encodedAuth = Base64.encodeToString(auth.getBytes(), Base64.NO_WRAP);
            Log.e("", encodedAuth);
            httpget.addHeader("Authorization", "Basic " + encodedAuth);
            HttpResponse response = null;
            try {
                response = httpClient.execute(httpget);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    inputStream = entity.getContent();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                try {
                    while ((line = bufferedReader.readLine()) != null) {
                        result += line;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //Extract link from JSON
            //String to Json
            JSONObject jsonObject = null;
            if (JSONValue.isValidJson(result)) {
                jsonObject = (JSONObject) JSONValue.parse(result);
            }
            Bitmap[] bitmaps = new Bitmap[NUM_COLS * NUM_ROWS];

            Bitmap bitmap = null;
            String pathMycons = String.format(getExternalCacheDir() + "//BingSeachTemp");
            File dirfile = createDirIfNotExists(pathMycons);
            jsonObject = (JSONObject) jsonObject.get("d");
            JSONArray jsonArray = (JSONArray) jsonObject.get("results");
            for (int i = 0; i < NUM_COLS * NUM_ROWS; i++) {
                jsonObject = (net.minidev.json.JSONObject) jsonArray.get(i);
                jsonObject = (JSONObject) jsonObject.get("Thumbnail");
                Log.e(". ", jsonObject.toString() + " . ");
                String url = (String) jsonObject.get("MediaUrl");

                Log.e(". ", url + " . ");
                try {
                    bitmap = downloadBitmap(url);
                    bitmaps[i] = bitmap;
//                    bitmaps.add(bitmap);
                    File file = new File(pathMycons, String.format(System.currentTimeMillis() + ".png"));
                    file.setReadable(true);
                    FileOutputStream ostream1 = null;
                    ostream1 = new FileOutputStream(file);
                    boolean scs = bitmap.compress(Bitmap.CompressFormat.JPEG, 99, ostream1);
                    ostream1.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return bitmaps;
        }

        private Bitmap downloadBitmap(String url) throws IOException {
            HttpUriRequest request = new HttpGet(url.toString());
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(request);

            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                byte[] bytes = EntityUtils.toByteArray(entity);

                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,
                        bytes.length);
                return bitmap;
            } else {
                throw new IOException("Download failed, HTTP response code "
                        + statusCode + " - " + statusLine.getReasonPhrase());
            }


        }

        @Override
        protected void onPostExecute(Bitmap[] bitmaps) {
            super.onPostExecute(bitmaps);
            UpdatePicsResultsView(bitmaps);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private boolean checkEnglish(CharSequence fromUser) {
        int length = fromUser.length();
        boolean englishFlag = true;

        for (int i = 0; i < length && englishFlag; i++) {
            if (fromUser.charAt(i) >= 'a' && fromUser.charAt(i) <= 'z' ||
                    fromUser.charAt(i) >= 'A' && fromUser.charAt(i) <= 'Z' ||
                    fromUser.charAt(i) >= '0' && fromUser.charAt(i) <= '9' ||
                    fromUser.charAt(i) != ' '
                    ) {
                englishFlag = true;
            } else {
                englishFlag = false;
            }
        }
        return englishFlag;
    }

    public void UpdatePicsResultsView(Bitmap[] bitmaps) {
        int count = table.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = table.getChildAt(i);
            if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
        }


        count = 0;
        Bitmap mycon = null;
        for (int row = 0; row < NUM_ROWS && count < bitmaps.length; row++) {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.MATCH_PARENT,
                    3.0f));

            table.addView(tableRow);

            for (int col = 0; col < NUM_COLS && count < bitmaps.length; col++) {
                final ImageButton imageButton = new ImageButton(this);
                mycon = bitmaps[count];
                imageButton.setImageBitmap(mycon);
                imageButton.setOnClickListener((View.OnClickListener) this);
                imageButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        currentImageButton = (ImageButton) v;
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("searchImage", (Bitmap) ((BitmapDrawable) ((ImageButton) v).getDrawable()).getBitmap());
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                        return true;
                    }
                });

                imageButton.setBackgroundColor(Color.WHITE);
                imageButton.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT,
                        3.0f));

                tableRow.addView(imageButton);

                count++;
            }
        }
    }

    @Override
    public void onClick(final View v) {
        currentImageButton = (ImageButton) v;
        Intent returnIntent = new Intent();
        returnIntent.putExtra("searchImage", (Bitmap) ((BitmapDrawable) ((ImageButton) v).getDrawable()).getBitmap());
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    protected void onPause() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        super.onDestroy();
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

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}