package lincyu.chapter15_basiccamera;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
//
import android.app.Activity;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;
//
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
////////
import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
//////////////////
import java.text.SimpleDateFormat;






public class MainActivity extends Activity {

	public class WWW {

		private String charset = "UTF-8";
		private String CRLF = "\r\n"; // Line separator required by multipart/form-data.

		private String url;
		private ArrayList<String[]> fileList = new ArrayList<>();
		private ArrayList<String[]> valueList = new ArrayList<>();

		private String text = "";
		private boolean haveFile = false;

		private OutputStream output;
		private OutputStreamWriter outputW;
		private PrintWriter writer;
		private HttpURLConnection httpConn;
		private InputStreamReader in;
		private BufferedReader br;

		public WWW(String url) {
			this.url = url;
		}

		public WWW(String url, String charset) { // URL , 編碼方式 (預設為 UTF-8)
			this.url = url;
			this.charset = charset;
		}

		public WWW(String url, String charset, String CRLF) { //URL , 編碼方式 (預設為 UTF-8) , 自訂換行字元
			this.url = url;
			this.charset = charset;
			this.CRLF = CRLF;
		}

		public void addFile(String key, String path) {
// [0] = key
// [1] = path
			fileList.add(new String[]{key, path});
			haveFile = true;
		}

		public void addValue(String key, String value) {
// [0] = key
// [1] = value
			valueList.add(new String[]{key, value});
		}

		public String getText() {
			return text;
		}

		public void upload() throws MalformedURLException, IOException {

// --- HTTP ---
			URLConnection conn = new URL(url).openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);

			String boundary = null; // Just generate some unique random value.

			if (haveFile) {
				boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
				conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
			} else {
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			}

			try (
					OutputStream output = conn.getOutputStream();
					OutputStreamWriter outputW = new OutputStreamWriter(output, charset);
					PrintWriter writer = new PrintWriter(outputW, true);) {

				this.output = output;
				this.outputW = outputW;
				this.writer = writer;

				if (haveFile) {
// 上傳每個參數 -------------------------------------------------
// kv [0] = key
// kv [1] = path
					for (String[] kv : valueList) {
						writer.append("--" + boundary).append(CRLF);
						writer.append("Content-Disposition: form-data; name=\"" + kv[0] + "\"").append(CRLF);
						writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
						writer.append(CRLF).append(kv[1]).append(CRLF).flush();
					}
// 上傳 每個檔案 ------------------------------------------------
// fileInfo [0] = key
// fileInfo [1] = path
					for (String[] fileInfo : fileList) {
						File binaryFile = new File(fileInfo[1]);
						writer.append("--" + boundary).append(CRLF);
						writer.append("Content-Disposition: form-data; name=\"" + fileInfo[0] + "\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
						writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
						writer.append("Content-Transfer-Encoding: binary").append(CRLF);
						writer.append(CRLF).flush();

// Upload File
						try (InputStream is = new FileInputStream(binaryFile)) {
							byte[] buf = new byte[8192];
							int c = 0;
							while ((c = is.read(buf, 0, buf.length)) > 0) {
								output.write(buf, 0, c);
								output.flush();
							}
							is.close();
							buf = null;
						}
						writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
					}
// End of multipart/form-data.
					writer.append("--" + boundary + "--").append(CRLF).flush();

				} else { // Haven't File , 如果沒有夾帶檔案
// 上傳每個參數 -------------------------------------------------
					for (int i = 0; i < valueList.size(); i++) {
						String[] kv = valueList.get(i);
						String data
								= URLEncoder.encode(kv[0], charset)
								+ "="
								+ URLEncoder.encode(kv[1], charset);
						if (i < valueList.size() - 1) {
							data += "&";
						}
						writer.append(data);
					}
					writer.append(CRLF).flush();
				}

// 回傳訊息 ---------------------------------------------------------
				HttpURLConnection httpConn = (HttpURLConnection) conn; // 回傳使用
				this.httpConn = httpConn;
				try (InputStreamReader in = new InputStreamReader(httpConn.getInputStream());
					 BufferedReader br = new BufferedReader(in);) {
					this.in = in;
					this.br = br;
					text = "";
					String line = "";
					while ((line = br.readLine()) != null) {
						text += line;
					}
					close();
				}
			}
		}

		public void close() {
			try {
				if (output != null) {
					output.close();
				}
			} catch (IOException e) {
			}
			try {
				if (outputW != null) {
					outputW.close();
				}
			} catch (IOException e) {
			}
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (Exception e) {
			}
			try {
				if (httpConn != null) {
					httpConn.disconnect();
				}
			} catch (Exception e) {
			}
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
			}
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
			}
		}

	}
	SurfaceView sv;
	SurfaceHolder sh;
	Camera camera;
	int facing;

	TextView tv;
	Button takepicture;
	LinearLayout ll;

	MySensorListener sensorlistener;
	SensorManager smgr;
	Sensor sensor;
	
	Display display;


	//
	private MyLocationListener mll;
	private LocationManager mgr;
	//private TextView tvv;
	private String best;
	//
	String date;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		display = getWindowManager().getDefaultDisplay();

		Intent intent = getIntent();
		facing = intent.getIntExtra("FACING",
				CameraInfo.CAMERA_FACING_BACK);
		
		setContentView(R.layout.activity_main);

		sv = (SurfaceView)findViewById(R.id.sv);
		sh = sv.getHolder();
		sh.addCallback(new MySHCallback());

		smgr = (SensorManager)getSystemService(SENSOR_SERVICE);
		sensorlistener = new MySensorListener();

		sensor = smgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		LayoutInflater inflater = LayoutInflater.from(this);
		ll = (LinearLayout)inflater.inflate(
				R.layout.addviews, null);
		tv = (TextView)ll.findViewById(R.id.tv_camerastatus);


		LayoutParams lp = new LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		addContentView(ll, lp);

		///
		//tvv = (TextView)findViewById(R.id.tv_camerastatus);

		mgr = (LocationManager)getSystemService(LOCATION_SERVICE);
		mll = new MyLocationListener();

		Location location = mgr.getLastKnownLocation(
				LocationManager.GPS_PROVIDER);
		if (location == null) {
			location = mgr.getLastKnownLocation(
					LocationManager.NETWORK_PROVIDER);
		}
		if (location != null) {
			tv.setText(showLocation(location));
		} else {
			tv.setText("Cannot get location!");
		}

		///
		takepicture = (Button)ll.findViewById(R.id.btn_tp);
		takepicture.setOnClickListener(LOL);
		/*switchcamera = (Button)ll.findViewById(R.id.btn_sc);
		switchcamera.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (facing == CameraInfo.CAMERA_FACING_BACK)
					facing = CameraInfo.CAMERA_FACING_FRONT;
				else
					facing = CameraInfo.CAMERA_FACING_BACK;
				camera.stopPreview();
				camera.release();
				camera = null;
				Intent intent = new Intent();
				intent.setClass(MainActivity.this,
						MainActivity.class);
				intent.putExtra("FACING", facing);
				startActivity(intent);
				finish();
			}
		});*/
		/*viewpicture = (Button)ll.findViewById(R.id.btn_vp);
		viewpicture.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this,
						ViewPicture.class);
				startActivity(intent);
			}
		});*/


	}
	
	PictureCallback jpeg = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Bitmap bm = BitmapFactory.decodeByteArray(data,
					0, data.length);
			FileOutputStream fos = null;
			try {
				File sdroot =
						Environment.getExternalStorageDirectory();

				SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
				date = sDateFormat.format(new java.util.Date());

				File file = new File(sdroot, date+".jpg");
				fos = new FileOutputStream(file);
				BufferedOutputStream bos =
						new BufferedOutputStream(fos);
				bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
				bos.flush();
				bos.close();
				Toast.makeText(MainActivity.this, "儲存成功",
						Toast.LENGTH_SHORT).show();

				Thread thread = new Thread(mutiThread);
				thread.start();
				camera.startPreview();
			}catch (Exception e) {
				Toast.makeText(MainActivity.this, "儲存失敗",
						Toast.LENGTH_SHORT).show();
			}
		}
	};


	//MULTITHREAD DEF
	private Runnable mutiThread = new Runnable() {
		public void run() {
			// 運行網路連線的程式
			File  sd=Environment.getExternalStorageDirectory();
			String path=sd.getPath()+"/"+date+".jpg";

			try {
				Location location = mgr.getLastKnownLocation(
						LocationManager.GPS_PROVIDER);
				if(location == null) {
					location = mgr.getLastKnownLocation(
							LocationManager.NETWORK_PROVIDER);
				}
				String longtitude = Double.toString(location.getLongitude());
				String latitude = Double.toString(location.getLatitude());

				upload(longtitude,latitude, path );
			}catch(IOException ex) {
				//Do something witht the exception
			}
		}
	};
	//TAKEPICTURE BUTTON DEF
	public OnClickListener LOL = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (camera != null) {
				camera.takePicture(null, null, jpeg);
			}
		}
	};




	@Override
	protected void onResume() {
		super.onResume();
		if (sensor != null)
			smgr.registerListener(sensorlistener, sensor,
					SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (sensor != null)
			smgr.unregisterListener(sensorlistener, sensor);
	}

	class MySHCallback implements SurfaceHolder.Callback {

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			CameraInfo info = new CameraInfo();
			int ncamera = Camera.getNumberOfCameras();
			for (int i = 0; i < ncamera; i++) {
				Camera.getCameraInfo(i, info);
				if (info.facing == facing) {
					camera = Camera.open(i);
					break;
				}
			}
			if (camera == null) camera = Camera.open();
			
			if (camera == null) {
				Toast.makeText(MainActivity.this,
						"無法開啟相機", Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
			
			int degrees = 0;
			int rotation = display.getRotation();
			switch (rotation) {
			case Surface.ROTATION_0: degrees = 0; break;
			case Surface.ROTATION_90: degrees = 90; break;
			case Surface.ROTATION_180: degrees = 180; break;
			case Surface.ROTATION_270: degrees = 270; break;
			}

			int result;
			if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
				result = (info.orientation + degrees) % 360;
				result = (360 - result) % 360;  // compensate the mirror
			} else {  // back-facing
				result = (info.orientation - degrees + 360) % 360;
			}
			camera.setDisplayOrientation(result);

			Camera.Parameters params = camera.getParameters();
			params.setPictureFormat(ImageFormat.JPEG);
			params.setPictureSize(480, 320);
			camera.setParameters(params);

			try  {
				camera.setPreviewDisplay(sh);
			} catch (Exception e) {
				finish();
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder surfaceholder) {
			if (camera != null) {
				camera.stopPreview();
				camera.release();
			}
		}

		@Override
		public void surfaceChanged(SurfaceHolder surfaceholder,
				int format , int w, int h) {
			if (camera != null) camera.startPreview();
		}
	}


	class MySensorListener implements SensorEventListener {
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			/*if (event.values[0] > 9.3 || event.values[1] > 9.3) {
				tv.setText("相機是正的");
			} else {
				tv.setText("相機拿歪了喔");
			}*/
		}
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	}

    /////

    class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                tv.setText(showLocation(location));
            } else {
                tv.setText("Cannot get location!");
            }
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
        @Override
        public void onStatusChanged(String provider,
                                    int status, Bundle extras) {
        }
    }

    public String showLocation(Location location) {
        StringBuffer msg = new StringBuffer();
        msg.append("\nLongitude: ");
        msg.append(Double.toString(location.getLongitude()));
		msg.append("\nLatitude: ");
		msg.append(Double.toString(location.getLatitude()));
        return msg.toString();
    }
    ////

	public void upload(String longtitude,String latitude,String SDDIR) throws IOException {
		WWW w = new WWW("http://140.113.195.27/api/Fetch/AddPhoto");
		w.addValue("ACCOUNT", "0116013"); // 加入參數 Key, Value
		w.addValue("ACCESSCODE", "8CEA473670"); // 可以加很多參數
		w.addValue("LONGITUDE", longtitude); // Key, Value 也可以是中文
		w.addValue("LATITUDE", latitude);
		w.addFile("MYPHOTO", SDDIR); // 加入檔案 Key, 檔案路徑

		w.upload(); // 上傳
		w.close();
		System.out.println(w.getText()); // 取得伺服器回傳訊息
	}



}
