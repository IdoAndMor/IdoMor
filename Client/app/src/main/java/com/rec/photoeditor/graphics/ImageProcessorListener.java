package com.rec.photoeditor.graphics;

import android.graphics.Bitmap;

public interface ImageProcessorListener {
	Object getLastCustomerNonConfigurationInstance();

	void onProcessStart();
	void onProcessEnd(Bitmap result);
}
