package com.mycons_web.mycons;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by mor on 09/12/2016.
 */
public class Login extends Activity {

    TextView badPasswordTextView;
    EditText loginPhoneOrMail;
    EditText loginPassword;
    OkHttpClient client;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        badPasswordTextView = (TextView) findViewById(R.id.badPasswordTextView);
        loginPhoneOrMail = (EditText) findViewById(R.id.emailOrPhoneEditText);
        loginPassword = (EditText) findViewById(R.id.loginPasswordEditText);

        Button loginButton = (Button)findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send to server username and password for verification
                //if good - innerCode = 0;
                //else - error username or password
                JSONObject jsonLogin = new JSONObject();
                String username = loginPhoneOrMail.getText().toString();
                String password = loginPassword.getText().toString();
                String goodOrBadFromServer;
/*                 try {
                   jsonLogin.put("username",username);
                    jsonLogin.put("password",password);

                    String jsonString = jsonLogin.toString();
                    goodOrBadFromServer = post("ido",jsonString);
                    */
                    if(username.equals("mor") || username.equals("ido") )
                    {
                        goodOrBadFromServer="1";
                    }
                    else
                    {
                        goodOrBadFromServer="0";
                    }

                    if (goodOrBadFromServer.equals("0"))
                    {
                        badPasswordTextView.setText("User name or password is not correct, please try again");
                        loginPassword.setText("");
                    }
                    else
                    {
                        finish();
                        Intent sendMsgIntent = new Intent(Login.this, SendMessage.class);
                        startActivity(sendMsgIntent);
                    }

/*                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
*/

            }
        });

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
