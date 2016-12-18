package com.mycons_web.mycons;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DownloadMycons extends Activity {
    public static final int ACTIVITY_ADD_TO_KEYBOARD = 1026;
    public static final int DOWNLOAD_OR_REPORT = 1030;

    public class MyconsList {
        public int myconsTotalPages;
        public ArrayList<ListItem> myconsList;
    }

    int pageNum;
    int totalPageNum;
    String currentCategory;
    String currentSearchTag;
    boolean isCategorySearch;
    boolean isTagsSearch;

    OkHttpClient client;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.community_mycons);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        client = new OkHttpClient();
        if (!isNetworkConnected()) {
            Toast.makeText(getBaseContext(), "No Internet connection!", Toast.LENGTH_LONG).show();
            return;
        }
        String[] categories = getCategoriesList();

        final Button prevButton = (Button) findViewById(R.id.prevButton);
        final Button nextButton = (Button) findViewById(R.id.nextEmailSingupButton);
        final Button searchButton = (Button) findViewById(R.id.search);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Object item = arg0.getItemAtPosition(arg2);
                if (!isNetworkConnected()) {
                    Toast.makeText(getBaseContext(), "No Internet connection!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (item != null && !item.toString().equals("Choose Category")) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                    String categoryName = item.toString();
                    Toast.makeText(DownloadMycons.this, item.toString(), Toast.LENGTH_SHORT).show();

                    pageNum = 0;
                    prevButton.setVisibility(View.INVISIBLE);
                    nextButton.setVisibility(View.VISIBLE);
                    currentCategory = categoryName;
                    isCategorySearch = true;

                    Toast.makeText(DownloadMycons.this, "Selected", Toast.LENGTH_SHORT).show();
                    new DownloadingAsyncTask().execute("byCategory", String.valueOf(pageNum), categoryName);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                if (!isNetworkConnected()) {
                    Toast.makeText(getBaseContext(), "No Internet connection!", Toast.LENGTH_LONG).show();
                    return;
                }
                pageNum = 0;
                prevButton.setVisibility(View.INVISIBLE);
                nextButton.setVisibility(View.VISIBLE);

                currentCategory = null;
                isCategorySearch = false;
                isTagsSearch = true;

                new DownloadingAsyncTask().execute("bySearch", String.valueOf(pageNum));
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pageNum == 0) {
                    return;
                }

                pageNum--;
                if (pageNum == 0) {
                    prevButton.setVisibility(View.INVISIBLE);
                } else {
                    prevButton.setVisibility(View.VISIBLE);
                }
                nextButton.setVisibility(View.VISIBLE);

                if (isTagsSearch) {
                    new DownloadingAsyncTask().execute("bySearch", String.valueOf(pageNum));
                } else if (isCategorySearch) {
                    new DownloadingAsyncTask().execute("byCategory", String.valueOf(pageNum), currentCategory);
                }

            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pageNum == totalPageNum - 1) {
                    return;
                }
                pageNum++;
                if (pageNum == totalPageNum - 1) {
                    nextButton.setVisibility(View.INVISIBLE);
                } else {
                    nextButton.setVisibility(View.VISIBLE);
                }
                prevButton.setVisibility(View.VISIBLE);

                if (isTagsSearch) {
                    new DownloadingAsyncTask().execute("bySearch", String.valueOf(pageNum));
                } else if (isCategorySearch) {
                    new DownloadingAsyncTask().execute("byCategory", String.valueOf(pageNum), currentCategory);
                }
            }
        });
    }

    public class DownloadingAsyncTask extends AsyncTask<String, Void, MyconsList> {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.bingSerachProgressBar2);

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public MyconsList doInBackground(String... params) {
            int pageNum = Integer.parseInt(params[1]);
            MyconsList myconsList = new MyconsList();

            switch (params[0]) {
                case "byCategory":
                    String currentCategory = params[2];
                    myconsList = getListDataByCategory(currentCategory, pageNum);
                    break;
                case "bySearch":
                    myconsList = getListDataBySearch(pageNum);
                    break;
            }
            return myconsList;
        }

        @Override
        protected void onPostExecute(MyconsList myconsList) {
            final ListView listView = (ListView) findViewById(R.id.custom_list);
            Button resultNextButton = (Button) findViewById(R.id.nextEmailSingupButton);
            if (myconsList.myconsTotalPages == 1) {
                resultNextButton.setVisibility(View.INVISIBLE);
            }

            listView.setAdapter(new CustomListAdapter(getBaseContext(), myconsList.myconsList));
            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                    Toast.makeText(DownloadMycons.this, "SAVED in your MyCons Keyboard", Toast.LENGTH_LONG).show();
                    ListItem newsData = (ListItem) listView.getItemAtPosition(position);
                    Bitmap myconBitmap = getBitmapFromBase64String(newsData.getImage());
                    String pathMycons = String.format(getExternalCacheDir() + "//MyconsImge");
                    boolean scsMyconsDir = createDirIfNotExists(pathMycons);
                    try {
                        File file = new File(pathMycons, String.format(System.currentTimeMillis() + ".png"));

                        file.setReadable(true);
                        FileOutputStream ostream1 = null;
                        ostream1 = new FileOutputStream(file);
                        boolean scs = myconBitmap.compress(Bitmap.CompressFormat.JPEG, 99, ostream1);
                        ostream1.close();
                        finish();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    ListItem newsData = (ListItem) listView.getItemAtPosition(position);
                    Intent intent = new Intent(DownloadMycons.this, DownloadOption.class);
                    intent.putExtra("bitmapMyconImage", getBitmapFromBase64String(newsData.getImage()));
                    intent.putExtra("getId",newsData.getId());
                    startActivityForResult(intent, DOWNLOAD_OR_REPORT);
                    return true;
                }
            });

            progressBar.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == DOWNLOAD_OR_REPORT) {
            if (resultCode == RESULT_OK) {
                if (data.hasExtra("downloadMycon")) {
                    Bitmap myconBitmap = (Bitmap) data.getParcelableExtra("bitmapMyconImage");
                    String pathMycons = String.format(getExternalCacheDir() + "//MyconsImge");
                    boolean scsMyconsDir = createDirIfNotExists(pathMycons);
                    try {
                        File file = new File(pathMycons, String.format(System.currentTimeMillis() + ".png"));
                        file.setReadable(true);
                        FileOutputStream ostream1 = null;
                        ostream1 = new FileOutputStream(file);
                        boolean scs = myconBitmap.compress(Bitmap.CompressFormat.JPEG, 99, ostream1);
                        ostream1.close();
                        finish();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public boolean createDirIfNotExists(String path) {
        boolean ret = true;
        File file = new File(path);

        if (!file.exists()) {
            if (!file.mkdir()) {
                Log.e("TravellerLog :: ", "Problem creating Image folder");
                ret = false;
            }
        }
        return ret;
    }

    private Bitmap getBitmapFromBase64String(String base64str) {

        byte[] decodedString = Base64.decode(base64str, Base64.DEFAULT);
        Bitmap myconImage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        return myconImage;

    }

    public MyconsList getListDataBySearch(final int page) {
        final ExecutorService es = Executors.newSingleThreadExecutor();
        Future<MyconsList> result = es.submit(new Callable<MyconsList>() {
            @Override
            public MyconsList call() throws Exception {
                final EditText myconSearchEditText = (EditText) findViewById(R.id.myconSearchEditText);
                currentSearchTag = myconSearchEditText.toString();
                return getMyconsByPage(null, myconSearchEditText.getText().toString(), page);
            }
        });

        MyconsList res = null;
        try {
            res = result.get();
        } catch (Exception e) {

        }
        es.shutdown();
        return res;

    }

    public MyconsList getListDataByCategory(final String categoryName, final int page) {
        final ExecutorService es = Executors.newSingleThreadExecutor();
        Future<MyconsList> result = es.submit(new Callable<MyconsList>() {
            @Override
            public MyconsList call() throws Exception {
                return getMyconsByPage(categoryName, null, page);
            }
        });

        MyconsList res = null;
        try {
            res = result.get();
        } catch (Exception e) {

        }
        es.shutdown();
        return res;
    }

    public MyconsList getMyconsByPage(String category, String searchTag, int Page) throws JSONException {
        ArrayList<ListItem> listData = null;
        listData = new ArrayList<ListItem>();
        JSONObject json = new JSONObject();
        String resultStr = null;
        JSONObject jsonPages = new JSONObject();
        String jsonPagesStr;
        MyconsList myconsList = new MyconsList();

        try {
            if (category != null) {
                if (Page == 0) {
                    jsonPages.put("category", category);
                    jsonPagesStr = jsonPages.toString();
                    String pagesStr = post("http://ec2-52-43-208-125.us-west-2.compute.amazonaws.com:3000/get/pagesNumByCategory", jsonPagesStr);
                    totalPageNum = Integer.parseInt(pagesStr);
                }

                json.put("category", category);
                json.put("page", Page);
                String jsonString = json.toString();
                resultStr = post("http://ec2-52-43-208-125.us-west-2.compute.amazonaws.com:3000/get/categoryMycons", jsonString);
            } else if (searchTag != null) {
                if (Page == 0) {
                    jsonPages.put("searchTags", searchTag);
                    jsonPagesStr = jsonPages.toString();
                    String pagesStr = post("http://ec2-52-43-208-125.us-west-2.compute.amazonaws.com:3000/get/pagesNumByTag", jsonPagesStr);
                    totalPageNum = Integer.parseInt(pagesStr);
                }

                json.put("searchTags", searchTag);
                json.put("page", Page);
                String jsonString = json.toString();
                resultStr = post("http://ec2-52-43-208-125.us-west-2.compute.amazonaws.com:3000/get/searchByTag", jsonString);
            }

            JSONArray jsonArray = new JSONArray(resultStr);
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    ListItem mycon = new ListItem();
                    mycon.setCategory(jsonObject.getString("category"));
                    mycon.setImage(jsonObject.getString("image"));
                    mycon.setName(jsonObject.getString("name"));
                    mycon.setTags(jsonObject.getString("tags"));
                    mycon.setId(jsonObject.getString("_id"));

                    listData.add(mycon);
                }
            }
        } catch (JSONException e) {
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        myconsList.myconsList = listData;
        myconsList.myconsTotalPages = totalPageNum;

        return myconsList;
    }

    public String[] getCategoriesList() {
        final ExecutorService es = Executors.newSingleThreadExecutor();
        Future<String[]> result = es.submit(new Callable<String[]>() {
            @Override
            public String[] call() throws Exception {

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
        });

        String[] res = null;
        try {
            res = result.get();
        } catch (Exception e) {

        }

        es.shutdown();
        return res;
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

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

}