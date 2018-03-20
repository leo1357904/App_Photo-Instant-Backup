package lincyu.chapter15_basiccamera;

import java.io.File;
import java.io.FileInputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.Toast;

public class ViewPicture extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_viewpicture);
		
		ImageView iv = (ImageView)findViewById(R.id.iv);
		
		try {
			File sdroot = Environment.getExternalStorageDirectory();
			File file = new File(sdroot, "picture.jpg");
			FileInputStream fis = new FileInputStream(file);
			Bitmap bitmap = BitmapFactory.decodeStream(fis);
			fis.close();
			iv.setImageBitmap(bitmap);
		} catch (Exception e) {
			Toast.makeText(this, "無法讀取照片",
					Toast.LENGTH_SHORT).show();
			finish();
		}
	}
}
