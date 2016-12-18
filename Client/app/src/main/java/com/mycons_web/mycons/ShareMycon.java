package com.mycons_web.mycons;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ShareMycon extends Activity {

    EditText nameEditText;
    EditText descEditText;
    EditText tagsEditText;
    ImageView myconToShareImageView;
    OkHttpClient client;
    String categoryName;

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share);
        nameEditText = (EditText) findViewById(R.id.nameEditText);
        myconToShareImageView = (ImageView) findViewById(R.id.myconToShareImageView);
        tagsEditText = (EditText) findViewById(R.id.tagsEditText);

        Button shareButton = (Button) findViewById(R.id.shareButton);
        final Button cancleButton = (Button) findViewById(R.id.cancleButton);
        myconToShareImageView.setImageBitmap((Bitmap) getIntent().getParcelableExtra("bitmapMyconImage"));

        client = new OkHttpClient();
        new getCategoriesListTaskAsync().execute();

        shareButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String url = "http://ec2-52-43-208-125.us-west-2.compute.amazonaws.com:3000/share";
                ByteArrayOutputStream boas = new ByteArrayOutputStream();
                Bitmap bitmap = ((BitmapDrawable) myconToShareImageView.getDrawable()).getBitmap();
                bitmap.compress(Bitmap.CompressFormat.PNG, 99, boas);
                byte[] byteArray = boas.toByteArray();
                String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

                JSONObject json = new JSONObject();
                try {
                    String name = nameEditText.getText().toString();
                    if (name == "" || name == null) {
                        name = "unknown";
                    }
                    json.put("name", name);
                    String category = categoryName;
                    if (category == "" || category == null) {
                        category = "general";
                    }
                    String tags = getTags();
                    json.put("tags", tags);
                    json.put("category", category);
                    json.put("image", encoded);
                    String jsonString = json.toString();
                    new ShareMyconAsyncTask().execute(url, jsonString);
                } catch (JSONException e) {
                }
            }
        });
        cancleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public class ShareMyconAsyncTask extends AsyncTask<String, Void, String> {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar3);

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public String doInBackground(String... params) {
            String result = null;
            try {
                result = post(params[0], params[1]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.INVISIBLE);

            if (result != null) {
                Toast.makeText(getBaseContext(), "Mycon Saved", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(getBaseContext(), "Sorry, Problem Occured", Toast.LENGTH_LONG).show();
            }
        }
    }

    String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private String getTags() {
        String tags = "";
        int length = tagsEditText.length();
        CharSequence fromUser = tagsEditText.getText();

        for (int i = 0; i < length; i++) {
            if (fromUser.charAt(i) >= 'a' && fromUser.charAt(i) <= 'z' ||
                    fromUser.charAt(i) >= 'A' && fromUser.charAt(i) <= 'Z' ||
                    fromUser.charAt(i) >= '0' && fromUser.charAt(i) <= '9') {
                tags += fromUser.charAt(i);
            } else {
                if (i != 0 && tags.charAt(tags.length() - 1) != ',') {
                    tags += ',';
                }
            }
        }

        return tags;
    }

    public class getCategoriesListTaskAsync extends AsyncTask<String, Void, String[]> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        public String[] doInBackground(String... params) {

            JSONObject jsonCategoriesList = new JSONObject();
            String[] mArray = null;
            mArray = new String[]{};

            try {
                jsonCategoriesList.put("category", "all");
                String jsonCategoriesListStr = jsonCategoriesList.toString();
                String categoryList = post("http://ec2-52-43-208-125.us-west-2.compute.amazonaws.com:3000/get/categoriesNames", jsonCategoriesListStr);

                mArray = categoryList.substring(1, categoryList.length() - 1).split(",");
                if (mArray != null) {
                    for (int i = 0; i < mArray.length; i++) {
                        mArray[i] = mArray[i].substring(1, mArray[i].length() - 1);
                    }
                }

            } catch (JSONException e) {
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            return mArray;
        }

        @Override
        protected void onPostExecute(String[] categories) {
            final Spinner categorySpinner = (Spinner) findViewById(R.id.categorySpinner);
            categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    Object item = arg0.getItemAtPosition(arg2);
                    if (item != null) {
                        categoryName = item.toString();
                        Toast.makeText(ShareMycon.this, item.toString(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    categoryName = "General";
                }
            });

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, categories);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(dataAdapter);

        }
    }
   }
