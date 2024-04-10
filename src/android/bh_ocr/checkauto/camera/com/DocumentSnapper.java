package bh_ocr.checkauto.camera.com;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alliance.AOPMobileApp.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import bh_ocr.checkauto.camera.com.utill.ViewfinderView;

public class DocumentSnapper extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback {
	public String PATH = Environment.getExternalStorageDirectory().toString() + "/wtimage/";
	private Camera camera;
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private ImageButton back;
	private ImageButton flash;
	private ImageButton imbtn_eject;
	private ImageButton imbtn_takepic;
	private ImageView help_word;
	private ViewfinderView viewfinder_view;

	private Bitmap bitmap;
	private int preWidth = 0;
	private int preHeight = 0;
	//	private boolean isROI = false;
	private int width, height, WIDTH, HEIGHT;
	private TimerTask timer;
	private ToneGenerator tone;

	private boolean isFatty;
	private Timer time;
	Camera.Parameters parameters;
	public int srcWidth, srcHeight;
	public int surfaceWidth, surfaceHeight;
	List<Camera.Size> list;
	private boolean isShowBorder = false;
	RelativeLayout.LayoutParams layoutParams;
	private RelativeLayout bg_camera_doctype;
	private Message msg;
	private TextView tv_camera_doctype;

	private String overlayText;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			findView();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overlayText = getIntent().getStringExtra("overlayText");

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		int activity_scan_camera = getResources().getIdentifier("activity_scan_camera", "layout",
				this.getPackageName());

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		setContentView(activity_scan_camera);
		setScreenSize(this);
		width = srcWidth;
		height = srcHeight;
	}

	public int px2dip(float pxValue) {
		final float scale = getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	@Override
	protected void onStart() {
		super.onStart();

		System.out.println("On start!");

		int surfaceViwe = getResources().getIdentifier("surfaceViwe", "id", this.getPackageName());
		surfaceView = (SurfaceView) findViewById(surfaceViwe);
		int help_word_ = getResources().getIdentifier("help_word", "id", this.getPackageName());
		help_word = (ImageView) findViewById(help_word_);
		int back_camera_ = getResources().getIdentifier("back_camera", "id", this.getPackageName());
		back = (ImageButton) findViewById(back_camera_);
		int flash_camera_ = getResources().getIdentifier("flash_camera", "id", this.getPackageName());
		flash = (ImageButton) findViewById(flash_camera_);
		int imbtn_takepic_ = getResources().getIdentifier("imbtn_takepic", "id", this.getPackageName());
		imbtn_takepic = (ImageButton) findViewById(imbtn_takepic_);
		int imbtn_eject_ = getResources().getIdentifier("imbtn_eject", "id", this.getPackageName());
		imbtn_eject = (ImageButton) findViewById(imbtn_eject_);
		int viewfinder_view_ = getResources().getIdentifier("viewfinder_view", "id", this.getPackageName());
		viewfinder_view = (ViewfinderView) findViewById(viewfinder_view_);
		int bg_camera_doctype_ = getResources().getIdentifier("bg_camera_doctype", "id", this.getPackageName());
		bg_camera_doctype = (RelativeLayout) findViewById(bg_camera_doctype_);
		int tv_camera_doctype_ = getResources().getIdentifier("tv_camera_doctypexx", "id", this.getPackageName());
		tv_camera_doctype = (TextView) findViewById(tv_camera_doctype_);
		Typeface custom_font = Typeface.createFromAsset(getAssets(),"fonts/ZawgyiOne2008.ttf");
		tv_camera_doctype.setTypeface(custom_font);
		tv_camera_doctype.setTextColor(Color.rgb(0, 0, 102));
		// tv_camera_doctype.setText(getString(R.string.workPermit));
		tv_camera_doctype.setText(overlayText);

		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(DocumentSnapper.this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	protected void onRestart() {
		if (bitmap != null) {
			bitmap.recycle();
			bitmap = null;
		}
		super.onRestart();
		tv_camera_doctype.setText(overlayText);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (null != tv_camera_doctype) {
			tv_camera_doctype.setText(overlayText);
		}
	}

	private void findView() {

		imbtn_takepic.setVisibility(View.VISIBLE);
		imbtn_eject.setVisibility(View.VISIBLE);
		help_word.setVisibility(View.GONE);
		bg_camera_doctype.setVisibility(View.VISIBLE);

		if (width * 3 == height * 4) {
			isFatty = true;
		}
		if (width == surfaceView.getWidth() || surfaceView.getWidth() == 0) {
			layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, height);
			surfaceView.setLayoutParams(layoutParams);

			layoutParams = new RelativeLayout.LayoutParams((int) (width * 0.1), (int) (width * 0.1));
			layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
			layoutParams.leftMargin = (int) (width * 0.89);
			imbtn_takepic.setLayoutParams(layoutParams);
		}

		else if (width > surfaceView.getWidth()) {
			int surfaceViewHeight = (surfaceView.getWidth() * height) / width;
			layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, surfaceViewHeight);
			layoutParams.topMargin = (height - surfaceViewHeight) / 2;
			surfaceView.setLayoutParams(layoutParams);

			layoutParams = new RelativeLayout.LayoutParams((int) (width * 0.1), (int) (width * 0.1));
			layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
			layoutParams.leftMargin = (int) (width * 0.83);

			imbtn_takepic.setLayoutParams(layoutParams);
		}

		int back_w = (int) (width * 0.06);
		int back_h = (int) (back_w * 1);
		layoutParams = new RelativeLayout.LayoutParams(back_w, back_h);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		int Fheight = height;
		if (isFatty)
			Fheight = (int) (height * 0.75);
		layoutParams.leftMargin = (int) (((width - Fheight * 0.8 * 1.585) / 2 - back_h) / 2);
		layoutParams.bottomMargin = (int) (height * 0.07);
		back.setLayoutParams(layoutParams);

		int flash_w = (int) (width * 0.06);
		int flash_h = (int) (flash_w * 1);
		layoutParams = new RelativeLayout.LayoutParams(flash_w, flash_h);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		if (isFatty)
			Fheight = (int) (height * 0.75);
		layoutParams.leftMargin = (int) (((width - Fheight * 0.8 * 1.585) / 2 - back_h) / 2);
		layoutParams.topMargin = (int) (height * 0.07);
		flash.setLayoutParams(layoutParams);

		int help_word_w = (int) (width * 0.474609375);
		int help_word_h = (int) (help_word_w * 0.05185185185185185185185185185185);
		layoutParams = new RelativeLayout.LayoutParams(help_word_w, help_word_h);
		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		help_word.setLayoutParams(layoutParams);

		int eject_btn_w = (int) (width * 0.024453125);
		int eject_btn_h = (int) (eject_btn_w * 7.8108108108108108108108108108108);
		layoutParams = new RelativeLayout.LayoutParams(eject_btn_w, eject_btn_h);
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		imbtn_eject.setLayoutParams(layoutParams);

		tv_camera_doctype = (TextView) this.findViewById(R.id.tv_camera_doctypexx);
		Typeface custom_font = Typeface.createFromAsset(getAssets(),"fonts/ZawgyiOne2008.ttf");
		tv_camera_doctype.setTypeface(custom_font);
		tv_camera_doctype.setTextColor(Color.rgb(0, 0, 102));
		tv_camera_doctype.setText(overlayText);

		if (surfaceWidth < width || surfaceHeight < height) {
			layoutParams = new RelativeLayout.LayoutParams(surfaceWidth, surfaceHeight);
			layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
			surfaceView.setLayoutParams(layoutParams);
			layoutParams = new RelativeLayout.LayoutParams((int) (width * 0.1), (int) (width * 0.1));
			layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
			if (width == 1280 && height == 800)
				layoutParams.leftMargin = (int) (width * 0.85);
			else
				layoutParams.leftMargin = (int) (width * 0.885);
			imbtn_takepic.setLayoutParams(layoutParams);
		}

		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		imbtn_eject.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				imbtn_takepic.setVisibility(View.VISIBLE);
				imbtn_eject.setVisibility(View.GONE);
			}
		});
		flash.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
					Toast.makeText(DocumentSnapper.this,
							getResources()
									.getString(getResources().getIdentifier("no_flash", "string", getPackageName())),
							Toast.LENGTH_LONG).show();
				} else {
					if (camera != null) {
						Camera.Parameters parameters = camera.getParameters();
						String flashMode = parameters.getFlashMode();
						if (flashMode.equals(Camera.Parameters.FLASH_MODE_TORCH)) {
							parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
							parameters.setExposureCompensation(0);
						} else {
							parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);// ����Ƴ���
							parameters.setExposureCompensation(-1);
						}
						try {
							camera.setParameters(parameters);
						} catch (Exception e) {
							Toast.makeText(DocumentSnapper.this,
									getResources().getString(
											getResources().getIdentifier("no_flash", "string", getPackageName())),
									Toast.LENGTH_LONG).show();
						}
						camera.startPreview();
					}
				}
			}
		});
		imbtn_takepic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (timer != null) {
					timer.cancel();
					timer = null;
				}
				if (camera != null) {
					try {
						isFocusTakePicture(camera);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	private ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			try {
				if (tone == null) {
					tone = new ToneGenerator(1, ToneGenerator.MIN_VOLUME);
				}
				tone.startTone(ToneGenerator.TONE_PROP_BEEP);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	};
	protected boolean isStopAutoFocus;

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		if (camera == null) {
			try {
				camera = Camera.open();
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
		try {
			camera.setPreviewDisplay(holder);
			time = new Timer();
			if (timer == null) {
				timer = new TimerTask() {
					public void run() {
						if (isStopAutoFocus) {
							return;
						}
						if (camera != null) {
							try {
								camera.autoFocus(new AutoFocusCallback() {
									public void onAutoFocus(boolean success, Camera camera) {

									}
								});
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					};
				};
			}
			time.schedule(timer, 500, 2500);
			initCamera();
			msg = new Message();
			handler.sendMessage(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

		if (camera != null) {

			getCameraPreParameters(camera);
			WIDTH = preWidth;
			HEIGHT = preHeight;

			System.out.println("WIDTH:" + WIDTH + "---" + "HEIGHT:" + HEIGHT);
			parameters = camera.getParameters();

			if (parameters.getSupportedFocusModes().contains(parameters.FOCUS_MODE_AUTO)) {
				parameters.setFocusMode(parameters.FOCUS_MODE_AUTO);
			}
			parameters.setPictureFormat(PixelFormat.JPEG);
			parameters.setExposureCompensation(0);
			parameters.setPreviewSize(WIDTH/* 1920 */, HEIGHT/* 1080 */);

			try {
				camera.setPreviewDisplay(surfaceHolder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			camera.setPreviewCallback(DocumentSnapper.this);
			camera.setParameters(parameters);
			camera.startPreview();
			msg = new Message();
			handler.sendMessage(msg);

		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		try {
			if (camera != null) {
				if (time != null) {
					time.cancel();
					time = null;
				}
				if (timer != null) {
					timer.cancel();
					timer = null;
				}
				camera.setPreviewCallback(null);
				camera.stopPreview();
				camera.release();
				camera = null;
			}
		} catch (Exception e) {
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			try {
				if (camera != null) {
					camera.setPreviewCallback(null);
					camera.stopPreview();
					camera.release();
					camera = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void initCamera() {

		Camera.Parameters parameters = camera.getParameters();
		getCameraPreParameters(camera);
		/*if (!isROI) {

			int l = (int) (preWidth * 0.16);
			int t = (int) (preHeight - 0.41004673 * preWidth) / 2;
			int r = (int) (preWidth * 0.85);
			int b = (int) (preHeight + 0.41004673 * preWidth) / 2;

			if (isFatty) {
				t = height / 5;
				b = height - t;
				l = (int) (width - ((b - t) * 1.585)) / 2;
				r = width - l;
				double proportion = (double) width / (double) preWidth;
				l = (int) (l / proportion);
				t = (int) (t / proportion);
				r = (int) (r / proportion);
				b = (int) (b / proportion);
			}
			isROI = true;
		}*/
		parameters.setPictureFormat(PixelFormat.JPEG);
		parameters.setPreviewSize(preWidth, preHeight);

		Camera.Size supportedPictureSize = this.getPictureSize();

		parameters.setPictureSize(supportedPictureSize.width, supportedPictureSize.height);

		if (parameters.getSupportedFocusModes().contains(parameters.FOCUS_MODE_AUTO)) {
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);// 1�����Խ�
		}
		camera.setPreviewCallback(this);
		camera.setParameters(parameters);
		camera.startPreview();
	}

	/*public String savePicture(Bitmap bitmap) {
		String strCaptureFilePath = PATH + "passport_" + pictureName() + ".jpg";
		savePictureByPath(strCaptureFilePath, bitmap);
		return strCaptureFilePath;
	}

	private String savePictureByPath(String path, Bitmap bitmap) {
		File dir = new File(PATH);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			bitmap.compress(Bitmap.CompressFormat.JPEG, 20, bos);
			bos.flush();
			bos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return path;
	}*/

	/*public String pictureName() {
		String str = "";
		Time t = new Time();
		t.setToNow();
		int year = t.year;
		int month = t.month + 1;
		int date = t.monthDay;
		int hour = t.hour;
		int minute = t.minute;
		int second = t.second;
		if (month < 10)
			str = String.valueOf(year) + "0" + String.valueOf(month);
		else {
			str = String.valueOf(year) + String.valueOf(month);
		}
		if (date < 10)
			str = str + "0" + String.valueOf(date + "_");
		else {
			str = str + String.valueOf(date + "_");
		}
		if (hour < 10)
			str = str + "0" + String.valueOf(hour);
		else {
			str = str + String.valueOf(hour);
		}
		if (minute < 10)
			str = str + "0" + String.valueOf(minute);
		else {
			str = str + String.valueOf(minute);
		}
		if (second < 10)
			str = str + "0" + String.valueOf(second);
		else {
			str = str + String.valueOf(second);
		}
		return str;
	}*/

	private Camera.Size getPictureSize() {
		List<Camera.Size> supportedPictureSizeList = parameters.getSupportedPictureSizes();
		for (Camera.Size supportedSize : supportedPictureSizeList) {
			float srcRatio = (float)srcWidth / (float)srcHeight;
			float picRatio = (float)supportedSize.width / (float)supportedSize.height;
			if (srcRatio == picRatio) {
				System.out.println("Confirmed picture size -- Height :: " + supportedSize.height + " Width :: " + supportedSize.width);
				return supportedSize;
			}
		}
		return parameters.getSupportedPictureSizes().get(0);
	}

	private String getOutputMediaFile() {
		File mediaStorageDir = new File(DocumentSnapper.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "allianceonline");
		
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				return null;
			}
		}
		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		return mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg";
	}

	byte[] resizeImage(byte[] input) {
		Bitmap original = BitmapFactory.decodeByteArray(input, 0, input.length);
		Bitmap resized = Bitmap.createScaledBitmap(original, srcWidth, srcHeight, true);
		ByteArrayOutputStream blob = new ByteArrayOutputStream();
		resized.compress(Bitmap.CompressFormat.JPEG, 20, blob);

		return blob.toByteArray();
	}

	private PictureCallback picturecallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			byte[] resized = resizeImage(data);
			String pictureFile = getOutputMediaFile();
			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(resized);
				fos.close();

				Intent intent = new Intent();
				intent.putExtra("Success", 3);
				intent.putExtra("HeadJpgPath", pictureFile);

				setResult(Activity.RESULT_OK, intent);
				finish();

			} catch (FileNotFoundException e) {
				System.out.println("File not found exception :: " + e.toString());
			} catch (IOException e) {
				System.out.println("IOException :: " + e.toString());
			}

		}
	};

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// Camera.Parameters parameters = camera.getParameters();
		int isBorders[] = { 0, 0, 0, 0 };

		int bRotated[] = { 0 };
		int pLineWarp[] = new int[32000];
		if (isBorders[0] == 1) {
			if (viewfinder_view != null) {
				viewfinder_view.setLeftLine(1);
			}
		} else {
			if (viewfinder_view != null) {
				viewfinder_view.setLeftLine(0);
			}
		}
		if (isBorders[1] == 1) {
			if (viewfinder_view != null) {
				viewfinder_view.setTopLine(1);
			}
		} else {
			if (viewfinder_view != null) {
				viewfinder_view.setTopLine(0);
			}
		}
		if (isBorders[2] == 1) {
			if (viewfinder_view != null) {
				viewfinder_view.setRightLine(1);
			}
		} else {
			if (viewfinder_view != null) {
				viewfinder_view.setRightLine(0);
			}
		}
		if (isBorders[3] == 1) {
			if (viewfinder_view != null) {
				viewfinder_view.setBottomLine(1);
			}
		} else {
			if (viewfinder_view != null) {
				viewfinder_view.setBottomLine(0);
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (time != null) {
			time.cancel();
			time = null;
		}
		if (bitmap != null) {
			bitmap.recycle();
			bitmap = null;
		}
		try {
			if (camera != null) {
				camera.setPreviewCallback(null);
				camera.stopPreview();
				camera.release();
				camera = null;
			}
		} catch (Exception e) {
			System.out.println("Exception from onStop " + e.toString());
		}
	}

	private void isFocusTakePicture(Camera camera) {
		final Camera.Parameters parameters = camera.getParameters();
		final boolean flashEnable = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
		if (flashEnable) {
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
		}

		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)) {
			camera.stopPreview();
			camera.startPreview();
			camera.takePicture(shutterCallback, null, picturecallback);
		} else {
			camera.autoFocus(new AutoFocusCallback() {
				public void onAutoFocus(boolean success, Camera camera) {
					if (success) {
						camera.stopPreview();
						camera.startPreview();
						camera.takePicture(shutterCallback, null, picturecallback);
						/* if (flashEnable) {
							try {
								camera.setParameters(parameters);
							} catch (Exception e) {
								e.printStackTrace();
							}
						} */
					}
				}
			});
		}
	}

	/**
	 * 
	 * @param camera
	 */
	public void getCameraPreParameters(Camera camera) {
		isShowBorder = false;

		preWidth = 0;
		preHeight = 0;
		parameters = camera.getParameters();
		list = parameters.getSupportedPreviewSizes();
		float ratioScreen = (float) srcWidth / srcHeight;
		for (int i = 0; i < list.size(); i++) {
			float ratioPreview = (float) list.get(i).width / list.get(i).height;
			if (ratioScreen == ratioPreview) {
				if (list.get(i).width >= 1280 || list.get(i).height >= 720) {
					if (preWidth == 0 && preHeight == 0) {
						preWidth = list.get(i).width;
						preHeight = list.get(i).height;
					}
					if (list.get(0).width > list.get(list.size() - 1).width) {
						if (preWidth > list.get(i).width || preHeight > list.get(i).height) {
							preWidth = list.get(i).width;
							preHeight = list.get(i).height;
						}
					} else {
						if (preWidth < list.get(i).width || preHeight < list.get(i).height) {
							if (preWidth >= 1280 || preHeight >= 720) {

							} else {
								preWidth = list.get(i).width;
								preHeight = list.get(i).height;
							}
						}
					}
				}
			}
		}
		if (preWidth == 0 || preHeight == 0) {
			isShowBorder = true;
			preWidth = list.get(0).width;
			preHeight = list.get(0).height;
			for (int i = 0; i < list.size(); i++) {

				if (list.get(0).width > list.get(list.size() - 1).width) {
					if (preWidth >= list.get(i).width || preHeight >= list.get(i).height) {
						if (list.get(i).width >= 1280) {
							preWidth = list.get(i).width;
							preHeight = list.get(i).height;

						}
					}
				} else {
					if (preWidth <= list.get(i).width || preHeight <= list.get(i).height) {
						if (preWidth >= 1280 || preHeight >= 720) {
							System.out.println("Nothing? Why??");
						} else {
							if (list.get(i).width >= 1280) {
								preWidth = list.get(i).width;
								preHeight = list.get(i).height;

							}
						}

					}
				}
			}
		}

		if (preWidth <= 640 || preHeight <= 480) {
			isShowBorder = true;
			if (list.get(0).width > list.get(list.size() - 1).width) {
				preWidth = list.get(0).width;
				preHeight = list.get(0).height;
			} else {
				preWidth = list.get(list.size() - 1).width;
				preHeight = list.get(list.size() - 1).height;
			}
		}
		if (isShowBorder) {
			if (ratioScreen > (float) preWidth / preHeight) {
				surfaceWidth = (int) (((float) preWidth / preHeight) * srcHeight);
				surfaceHeight = srcHeight;
			} else {
				surfaceWidth = srcWidth;
				surfaceHeight = (int) (((float) preHeight / preWidth) * srcWidth);
			}
		} else {
			surfaceWidth = srcWidth;
			surfaceHeight = srcHeight;
		}

		System.out.println("surfaceWidth1:" + surfaceWidth + "--surfaceHeight1:" + surfaceHeight);
	}

	@SuppressLint("NewApi")
	private void setScreenSize(Context context) {
		int x, y;
		WindowManager wm = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
		Display display = wm.getDefaultDisplay();
		/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			Point screenSize = new Point();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				display.getRealSize(screenSize);
				x = screenSize.x;
				y = screenSize.y;
			} else {
				display.getSize(screenSize);
				x = screenSize.x;
				y = screenSize.y;
			}
		} else {
			x = display.getWidth();
			y = display.getHeight();
		}*/

		Point screenSize = new Point();
		display.getRealSize(screenSize);
		x = screenSize.x;
		y = screenSize.y;

		srcWidth = x;
		srcHeight = y;
	}

	/**
	 * @param mDecorView{tags}
	 * @throws @Title:
	 */
	@TargetApi(19)
	public void hiddenVirtualButtons(View mDecorView) {
		if (Build.VERSION.SDK_INT >= 19) {
			mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_IMMERSIVE);
		}
	}

}
