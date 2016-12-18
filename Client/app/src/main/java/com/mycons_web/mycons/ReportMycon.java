package com.mycons_web.mycons;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReportMycon extends Activity {
    static boolean delete = false;

    OkHttpClient client;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_mycon);

        delete = false;
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * .7), (int) (height * .7));
        client = new OkHttpClient();
        final EditText reportEditText = (EditText) findViewById(R.id.reportEditText);
        Button reportButton = (Button) findViewById(R.id.reportButton);
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = getIntent().getStringExtra("getId");
                String body = "Report about mycon id:" + id + ". Complain:" + reportEditText.getText().toString();

                new ReporyMyconTaskAsync().execute(id,body);

                Toast.makeText(getBaseContext(), "thank you!", Toast.LENGTH_LONG).show();
                finish();

            }
        });
    }


    public class ReporyMyconTaskAsync extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        public Void doInBackground(String... params) {

            String id = params[0];
            String body = params[1];
            JSONObject json = new JSONObject();
            String resultStr = null;
            try {
                json.put("logType", "myconAbuse");
                json.put("myconId", id);
                json.put("message", body);
                String jsonString = json.toString();
                resultStr = post("http://ec2-52-43-208-125.us-west-2.compute.amazonaws.com:3000/share/addLogForReport", jsonString);

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
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

    }
    }


