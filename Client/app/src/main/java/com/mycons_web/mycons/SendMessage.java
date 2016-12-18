package com.mycons_web.mycons;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class SendMessage extends Activity implements View.OnClickListener {

    private class ImageButtonWithName
    {
        private ImageButton imageButton;
        private String name;

        public String getImageButtonName(){return name;}
        public void setImageButtonName(String name){this.name=name;}

        public ImageButton getImageButton(){return imageButton;}
        public void setImageButton(ImageButton imageButton){this.imageButton=imageButton;}
    }


    private static final int MAX_CHARS = 72;
    EditText msgTxt;
    TextView scsTxt;
    ImageView iv;
    boolean firstTime = true;

    int counter;
    private static final int NUM_ROWS = 50;
    private static final int NUM_COLS = 6;

    TableLayout table;
    ImageButtonWithName currentImageButton;
    ImageButton temp;
    ImageButton myconBtn;
    ImageButton androidKeyboardButton;
    private GoogleApiClient client;
    InputMethodManager imm;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_message);

        currentImageButton=new ImageButtonWithName();

        counter = 0;
        msgTxt = (EditText) findViewById(R.id.msgTextBox);
        scsTxt = (TextView) findViewById(R.id.textViewToChange);
        iv = (ImageView) findViewById(R.id.imageTxt);
        myconBtn = (ImageButton) findViewById(R.id.myconButton);
        androidKeyboardButton = (ImageButton) findViewById(R.id.androidKeyboardButton);
        temp = myconBtn;
        Button sndBtn = (Button) findViewById(R.id.forgetPasswordButton);
        Button createMyconsButton = (Button) findViewById(R.id.createButton);
        Button historyButton = (Button) findViewById(R.id.historyButton);
        Button clrButton = (Button) findViewById(R.id.clrButton);

        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(MAX_CHARS);
        msgTxt.setFilters(FilterArray);

        scsTxt.setTextSize(30);
        msgTxt.setTextSize(30);
        scsTxt.setTextColor(Color.WHITE);
        msgTxt.setMaxLines(5);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);;

        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboardDown();
                Intent historyIntent = new Intent(SendMessage.this, History.class);
                startActivityForResult(historyIntent, 1026);  //MSG_FROM_HISTORY_PUSHED
            }
        });

        createMyconsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboardDown();
                Intent createMyconsIntent = new Intent(SendMessage.this, NewMyconMenu.class);
                startActivityForResult(createMyconsIntent, 1880);
            }
        });

        myconBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboardDown();
                androidKeyboardButton.setVisibility(View.VISIBLE);
                myconBtn.setVisibility(View.INVISIBLE);
                if (msgTxt.length() <= MAX_CHARS - 3) {
                    keyboardDown();
                } else {
                    Toast.makeText(getBaseContext(), "Msg is to long", Toast.LENGTH_SHORT).show();
                }
            }
        });

        androidKeyboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msgTxt.requestFocus();
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                androidKeyboardButton.setVisibility(View.INVISIBLE);
                androidKeyboardButton.setClickable(false);
                myconBtn.setVisibility(View.VISIBLE);
                myconBtn.setClickable(true);
            }
        });
        msgTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msgTxt.requestFocus();
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                androidKeyboardButton.setVisibility(View.INVISIBLE);
                androidKeyboardButton.setClickable(false);
                myconBtn.setVisibility(View.VISIBLE);
                myconBtn.setClickable(true);
            }
        });

        sndBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboardDown();
                scsTxt.setDrawingCacheEnabled(true); // Enable drawing cache before calling the getDrawingCache() method
                scsTxt.setTextColor(Color.BLACK);
                scsTxt.setBackgroundColor(Color.TRANSPARENT);
                scsTxt.setTextSize(18f);

                scsTxt.setText(msgTxt.getText());
                scsTxt.layout(10, 10, 700, 700);

                Bitmap msgImage = Bitmap.createBitmap(700, 700, Bitmap.Config.ARGB_8888);
                msgImage.eraseColor(Color.WHITE);
                iv.setBackgroundColor(Color.WHITE);
                iv.setImageBitmap(msgImage);
                Canvas c = new Canvas(msgImage);
                iv.setMaxWidth(700);
                iv.setMaxHeight(700);

                try {
                    scsTxt.draw(c);
                    String pathMsgs = String.format(getExternalCacheDir() + "//Msgs");
                    boolean scsMsgDir = createDirIfNotExists(pathMsgs);

                    File file = new File(pathMsgs, String.format(System.currentTimeMillis() + ".png"));
                    FileOutputStream ostream1 = new FileOutputStream(file);
                    boolean scs = msgImage.compress(Bitmap.CompressFormat.JPEG, 99, ostream1);
                    ostream1.close();

                    Uri uri = Uri.fromFile(file);
                    shareImage(uri);

                    scsTxt.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    msgTxt.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                    scsTxt.setTextColor(Color.WHITE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        clrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msgTxt.setText("");
                scsTxt.setText("");
            }
        });

        msgTxt.addTextChangedListener(new TextWatcher() {
            int rowCounter = 0;
            String strEdit3 = "";
            int lastValidPosition = 0;
            boolean overLines =false;


            @Override
            public void afterTextChanged(Editable s) {
                strEdit3 = s.toString();
                rowCounter = msgTxt.getLineCount();
                if (lastValidPosition - msgTxt.getText().length() >=0){
                    overLines = false;
                }
                if (rowCounter <= 6 && !overLines){
                    lastValidPosition = msgTxt.getText().length();
                }
                if (rowCounter > 6){
                    overLines = true;
                    msgTxt.getText().delete(lastValidPosition, msgTxt.getText().length());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        initMyconKeyboard();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void initMyconKeyboard()
    {
        table = (TableLayout)findViewById(R.id.keyboardTableLayout);
        UpdateKeyborad();
    }
    private void shareImage(Uri uri) {

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);

        sendIntent.putExtra(Intent.EXTRA_TEXT, "Sent by MyCons");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Sent by MyCons");

        sendIntent.putExtra(sendIntent.EXTRA_STREAM, uri);
        sendIntent.setType("image/*");
        startActivity(Intent.createChooser(sendIntent, "Share image using"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1025) {
            if (resultCode == RESULT_OK) {
                Bitmap currentMycon = (Bitmap) data.getParcelableExtra("result");
                insertImageToCurrentSelection(currentMycon);
            }
        }
        if (requestCode == 1026) { //MSG_FROM_HISTORY_PUSHED
            if (resultCode == RESULT_OK) {

                Uri uri = (Uri) data.getParcelableExtra("currentMsg");
                shareImage(uri);
            }
        }
        if (resultCode == RESULT_OK) {
            if (requestCode == 1919) { //ACTIVITY_AFTER_POPUP_DELETE
                String path = String.format(getExternalCacheDir()+"//MyconsImge");
                File dir = createDirIfNotExists1(path);
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
                    initMyconKeyboard();
                }
            }
        }
        if (requestCode == 1880) { //RETURN_FROM_CREATE_MYCON
            initMyconKeyboard();
        }

    }

    public void insertImageToCurrentSelection(Bitmap Bitmap) {
        BitmapDrawable drawable = new BitmapDrawable(this.getResources(), Bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        int selectionCursor = msgTxt.getSelectionStart();
        msgTxt.getText().insert(selectionCursor, "~~");
        selectionCursor = msgTxt.getSelectionStart();

        SpannableStringBuilder builder = new SpannableStringBuilder(msgTxt.getText());
        builder.setSpan(new ImageSpan(drawable), selectionCursor - "~~".length(), selectionCursor,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        msgTxt.setText(builder);
        msgTxt.setSelection(selectionCursor);
    }

    @Override
    public void onStart() {
        super.onStart();
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW,
                "SendMessage Page",
                Uri.parse("http://host/path"),
                Uri.parse("android-app://com.mycons_web.mycons/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW,
                "SendMessage Page",
                Uri.parse("http://host/path"),
                Uri.parse("android-app://com.mycons_web.mycons/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
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
    public File createDirIfNotExists1(String path) {
        boolean ret = true;
        File file = new File(path);

        if (!file.exists()) {
            if (!file.mkdir()) {
                Log.e("TravellerLog :: ", "Problem creating Image folder");
                ret = false;
            }
        }
        return file;
    }
    public void UpdateKeyborad() {

        int count = table.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = table.getChildAt(i);
            if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
        }
        String path = String.format(getExternalCacheDir() + "//MyconsImge");
        File dir = createDirIfNotExists1(path);
        File[] files = dir.listFiles();

        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            }
        });

        final File[] childFile = files;
        count = 0;
        Bitmap mycon = null;

        for(int row=0; row < NUM_ROWS; row++) {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.MATCH_PARENT,
                    1.0f));

            table.addView(tableRow);

            for(int col=0; col < NUM_COLS && count < childFile.length; col++) {
                final ImageButtonWithName imageButtonWithName = new ImageButtonWithName();
                imageButtonWithName.setImageButton(new ImageButton(this));
                mycon = BitmapFactory.decodeFile(childFile[count].getPath());

                imageButtonWithName.setImageButtonName(childFile[count].getName());
                imageButtonWithName.getImageButton().setImageBitmap(mycon);
                imageButtonWithName.getImageButton().setOnClickListener((View.OnClickListener) this);
                imageButtonWithName.getImageButton().setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        currentImageButton.setImageButton((ImageButton) v);
                        currentImageButton.setImageButtonName(imageButtonWithName.getImageButtonName());
                        Intent returnIntent = new Intent(SendMessage.this, MyconButtonMenu.class);
                        returnIntent.putExtra("bitmapMyconImage", (Bitmap) ((BitmapDrawable) ((ImageButton) v).getDrawable()).getBitmap());
                        startActivityForResult(returnIntent, 1919);
                        return true;
                    }
                });

                imageButtonWithName.getImageButton().setBackgroundColor(Color.WHITE);
                imageButtonWithName.getImageButton().setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT,
                        1.0f));
                tableRow.addView(imageButtonWithName.getImageButton());
                count ++;
            }
        }
    }

    @Override
    public void onClick(final View v) {
    try {
            ImageButton key = (ImageButton) v;
            Bitmap ourImage = ((BitmapDrawable) key.getDrawable()).getBitmap();
            ImageButtonClicked(ourImage);
        } catch (Exception e) {
            //TODO;
        }
    }

    private void ImageButtonClicked(Bitmap buttonImageUri) {
        insertImageToCurrentSelection(buttonImageUri);

    }

    @Override
    protected void onPause() {
        super.onPause();
//        keyboardDown();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        keyboardDown();
    }

    public void keyboardDown()
    {
        if(myconBtn.getVisibility() == View.INVISIBLE && getCurrentFocus().getWindowToken()!=null) {
            androidKeyboardButton.setVisibility(View.VISIBLE);
            androidKeyboardButton.setClickable(true);
            myconBtn.setVisibility(View.INVISIBLE);
            myconBtn.setClickable(false);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        }
    }
}
