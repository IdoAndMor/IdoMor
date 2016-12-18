package com.rec.photoeditor;

import static android.Manifest.permission.CAMERA;
import static com.rec.photoeditor.editoractivity.EditorSaveConstants.RESTORE_SAVED_BITMAP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.mycons_web.mycons.R;


import com.mycons_web.mycons.SendMessage;
import com.rec.photoeditor.editoractivity.BrightnessActivity;
import com.rec.photoeditor.editoractivity.CropActivity;
import com.rec.photoeditor.editoractivity.RotateActivity;
import com.rec.photoeditor.graphics.ImageProcessor;
import com.rec.photoeditor.utils.BitmapScalingUtil;
import com.search.BingSearch;
@SuppressWarnings("ALL")
public class EditorActivity extends Activity
{
	private static final int EDITOR_FUNCTION = 1;
	private static final int AUTHORIZE_FACEBOOK = 2;
	private static final int CAMERA_CAPTURE = 3;
	public static final int ACTIVITY_SELECT_IMAGE = 1234;
    public static final int BING_SEARCH = 1333;
    private static final int PERMISSION_REQUEST_CODE = 200;

	private ImageView imageView;

	// Top bar buttons
	private ImageButton brightnessButton;
	private ImageButton cropButton;
	private ImageButton rotateButton;
    private  ImageButton circleCropButton;

	// Bottom bar buttons
    private ImageButton backButton;
	private ImageButton choosePicButton;
	private ImageButton saveButton;
	private ImageButton cameraButton;
    private ImageButton MyconsKeyboardButton;
    private  ImageButton googleSearchImageButton;
	private String savedImagePath;

    final private Bitmap[] bitmapsForUndo = new Bitmap[30];
    private int countEdites;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editor);
        imageView = (ImageView) findViewById(R.id.image_view);
		initComponents();
	}

	private void initComponents() {
		brightnessButton = (ImageButton) findViewById(R.id.brightness_button);
		brightnessButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				brightnessButtonClicked();
			}
		});
        MyconsKeyboardButton = (ImageButton) findViewById(R.id.MyconsKeyboardButton);
        MyconsKeyboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent MyconsKeyboardIntent = new Intent(EditorActivity.this, SendMessage.class);
                startActivity(MyconsKeyboardIntent);
            }
        });
		cropButton = (ImageButton) findViewById(R.id.crop_button);
		cropButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cropButtonClicked();
			}
		});
		rotateButton = (ImageButton) findViewById(R.id.rotate_button);
		rotateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				rotateButtonClicked();
			}
		});
		choosePicButton = (ImageButton) findViewById(R.id.choosePic_button);
		choosePicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicButtonClicked();
            }
        });
        backButton = (ImageButton) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backButtonClicked();
            }
        });
        saveButton = (ImageButton) findViewById(R.id.save_button);
		saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButtonClicked();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraButton = (ImageButton) findViewById(R.id.camera_button);
            cameraButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cameraButtonClicked();
                }
            });
        }
        else
        {
            cameraButton = (ImageButton) findViewById(R.id.camera_button);
            cameraButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cameraButtonClicked();
                }
            });
            cameraButton.setVisibility(View.INVISIBLE);
            cameraButton.setClickable(false);
        }
        circleCropButton = (ImageButton) findViewById(R.id.circle_crop_button);
        circleCropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getclip();
            }
        });

        googleSearchImageButton =(ImageButton) findViewById(R.id.googleSearchImageButton);
        googleSearchImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                bingImageSearchClicked();
            }
        });

        countEdites = 0;
        bitmapsForUndo[countEdites] = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
	}

    public void beforEditChange()
    {
        if(countEdites<30) {
            countEdites++;
            if(bitmapsForUndo[countEdites] != null && !bitmapsForUndo[countEdites].isRecycled())
            {
                bitmapsForUndo[countEdites].recycle();
                bitmapsForUndo[countEdites]=null;
                System.gc();
            }
            bitmapsForUndo[countEdites] = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        }
    }
    public void afterEditChange(Bitmap bitmap)
    {
        if(countEdites<30) {
            countEdites++;
            if(bitmapsForUndo[countEdites] != null && !bitmapsForUndo[countEdites].isRecycled())
            {
                bitmapsForUndo[countEdites].recycle();
                bitmapsForUndo[countEdites]=null;
                System.gc();
            }
            bitmapsForUndo[countEdites] = bitmap;
        }
    }
    public void backButtonClicked()
    {
        finish();
    }

    public void bingImageSearchClicked()
    {
        Intent searchIntent = new Intent(EditorActivity.this, BingSearch.class);
        startActivityForResult(searchIntent,BING_SEARCH);
    }

    public void getclip() {

        Bitmap bitmap  = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        Bitmap output =  Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(),bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);

        canvas.drawCircle(bitmap.getWidth() / 2,
                bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        imageView.setImageBitmap(output);
       afterEditChange(output);
    }
    private void initImageView(String imageUri) {
        Log.i("REC Photo Editor", "Image URI = " + imageUri);
        final Object data = getLastNonConfigurationInstance();
        if (data == null) {
            openBitmap(imageUri);
        } else {
            restoreBitmap();
        }
    }


    private void restoreBitmap() {
		Log.i("Photo Editor", "Restore bitmap");
		Bitmap b = ImageProcessor.getInstance().getBitmap();
		if (b != null) {
			imageView.setImageBitmap(b);
            afterEditChange(b);
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		Bundle saveObject = new Bundle();
		saveObject.putInt("Bitmap", RESTORE_SAVED_BITMAP);
		return saveObject;
	}

	private void openBitmap(String imageUri) {
		Log.i("Photo Editor", "Open Bitmap");
		Bitmap b;
		try {
			b = BitmapScalingUtil.bitmapFromUri(this, Uri.parse(imageUri));
			if (b != null) {
				Log.i("REC Photo Editor", "Opened Bitmap Size: " + b.getWidth()
                        + " " + b.getHeight());
			}
			ImageProcessor.getInstance().setBitmap(b);
			imageView.setImageBitmap(b);
            afterEditChange(b);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void choosePicButtonClicked()
    {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,
				MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(photoPickerIntent, ACTIVITY_SELECT_IMAGE);
    }

	private boolean imageIsAlreadySaved() {
		return savedImagePath != null && !savedImagePath.equals("");
	}
	private void saveButtonClicked() {
        if (!imageView.isDrawingCacheEnabled()) {
            imageView.setDrawingCacheEnabled(true);
        }
        imageView.buildDrawingCache(true);
        Bitmap bitmapMycon = Bitmap.createScaledBitmap(replaceColor(Color.TRANSPARENT, Color.WHITE), 110, 110, true);
        String pathMycons = String.format(getExternalCacheDir() + "//MyconsImge");
        boolean scsMyconsDir = createDirIfNotExists(pathMycons);
        try {
            File file = new File(pathMycons, String.format(System.currentTimeMillis() + ".png"));

            file.setReadable(true);
            FileOutputStream ostream1 = null;
            ostream1 = new FileOutputStream(file);
            boolean scs = bitmapMycon.compress(Bitmap.CompressFormat.JPEG, 99, ostream1);
            ostream1.close();
            imageView.setDrawingCacheEnabled(false);
            finish();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

    private boolean checkPermission() {
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        return result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void cameraButtonClicked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_CAPTURE);
            } else {
                Intent cameraPermissionIntent = new Intent(EditorActivity.this, CameraPermission.class);
                startActivityForResult(cameraPermissionIntent, PERMISSION_REQUEST_CODE);
            }
        }
        else
        {
            cameraButton.setVisibility(View.INVISIBLE);
            cameraButton.setClickable(false);
        }
    }

    public Bitmap replaceColor(int fromColor, int targetColor) {
        Bitmap mImage = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        if(mImage == null) {
            return null;
        }

        int width = mImage.getWidth();
        int height = mImage.getHeight();
        int[] pixels = new int[width * height];
        mImage.getPixels(pixels, 0, width, 0, 0, width, height);

        for(int x = 0; x < pixels.length; ++x) {
            pixels[x] = (pixels[x] == fromColor) ? targetColor : pixels[x];
        }

        Bitmap newImage = Bitmap.createBitmap(width, height, mImage.getConfig());
        newImage.setPixels(pixels, 0, width, 0, 0, width, height);

        return newImage;
    }

    private int getTheDivToNewMycons(int width, int height) {
        int num = 30;
        int w = width;
        int h = height;

        return 0;
    }

    public String setImageView() {
        if (!imageView.isDrawingCacheEnabled()) {
            imageView.setDrawingCacheEnabled(true);
        }

        imageView.buildDrawingCache(true);
        Bitmap bitmapMycon = Bitmap.createScaledBitmap(imageView.getDrawingCache(), 800, 800, true);

        String pathMycons = String.format(getExternalCacheDir() + "//tempImge");
        boolean scsMyconsDir = createDirIfNotExists(pathMycons);
        String ret_temp_uri = String.format(pathMycons + "//" + System.currentTimeMillis() + ".png");
        try {
            File file = new File(ret_temp_uri);

            file.setReadable(true);
            FileOutputStream ostream1 = null;
            ostream1 = new FileOutputStream(file);
            boolean scs = bitmapMycon.compress(Bitmap.CompressFormat.JPEG, 99, ostream1);
            ostream1.close();
            imageView.setImageBitmap(bitmapMycon);
            afterEditChange(bitmapMycon);
            imageView.setDrawingCacheEnabled(false);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret_temp_uri;
    }

	private void cropButtonClicked() {
		runEditorActivity(CropActivity.class);
    }

	private void brightnessButtonClicked() {
		runEditorActivity(BrightnessActivity.class);
	}


	private void rotateButtonClicked() {
		runEditorActivity(RotateActivity.class);
	}

	private void brightnessButtonLongClick() {
		Toast.makeText(this, "Brightness long click", Toast.LENGTH_SHORT)
				.show();
	}

	private void runEditorActivity(Class<?> activityClass) {
        beforEditChange();
		Intent i = new Intent(EditorActivity.this, activityClass);
        startActivityForResult(i, EDITOR_FUNCTION);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
           case BING_SEARCH:
                if(resultCode == RESULT_OK) {
                    if (data != null) {
                        Bitmap currentImage = (Bitmap) data.getParcelableExtra("searchImage");
                        imageView.setMaxHeight(currentImage.getHeight());
                        imageView.setMaxWidth(currentImage.getWidth());
                        imageView.setImageBitmap(currentImage);
                        afterEditChange(currentImage);
                        ImageProcessor.getInstance().setBitmap(currentImage);
                    }
                }
                break;
            case AUTHORIZE_FACEBOOK:
			break;
		case EDITOR_FUNCTION:
			if (resultCode == RESULT_OK) {
				imageView.setImageBitmap(ImageProcessor.getInstance()
                        .getBitmap());
                afterEditChange(ImageProcessor.getInstance().getBitmap());
            }
			break;
        case CAMERA_CAPTURE:
        case ACTIVITY_SELECT_IMAGE:
            if(data !=null)
            {
                Uri imageUri = data.getData();

                InputStream inputStream;
                try {

                    inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap imageToMycon = BitmapFactory.decodeStream(inputStream);
                    imageView.setMaxHeight(imageToMycon.getHeight());
                    imageView.setMaxWidth(imageToMycon.getWidth());
                    imageView.setImageBitmap(imageToMycon);
                    afterEditChange(imageToMycon);
                    openBitmap(imageUri.toString());

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            break;
            case PERMISSION_REQUEST_CODE:
                if(checkPermission()) {
                    cameraButtonClicked();
                }
                break;
            default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}


	private Runnable createPostRotateAction() {
		final Runnable postRotateAction = new Runnable() {
			public void run() {
				imageView.setImageBitmap(ImageProcessor.getInstance()
                        .getBitmap());
				imageView.invalidate();
			}
		};
		return postRotateAction;
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
}
