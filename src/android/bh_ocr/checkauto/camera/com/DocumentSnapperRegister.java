package bh_ocr.checkauto.camera.com;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.ToneGenerator;
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
import android.widget.Button;
import android.widget.ImageButton;
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

public class DocumentSnapperRegister extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private String overlayText;
    private int width, height, WIDTH, HEIGHT;
    public int srcWidth, srcHeight;
    public int surfaceWidth, surfaceHeight;
    private int preWidth = 0;
    private int preHeight = 0;
    private boolean isFatty;
    protected boolean isStopAutoFocus;
    private boolean isShowBorder = false;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Button back;
    private ImageButton flash;
    private ImageButton imbtn_takepic;
    private ViewfinderView viewfinder_view;
    private RelativeLayout bg_camera_doctype;
    private TextView tv_camera_doctype;
    private TextView title_navi;

    private Camera camera;
    private TimerTask timer;
    private ToneGenerator tone;
    private Bitmap bitmap;
    private Timer time;
    private Message msg;

    RelativeLayout.LayoutParams layoutParams;
    Camera.Parameters parameters;
    List<Camera.Size> list;

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

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        int activity_capture_camera = getResources().getIdentifier("activity_capture_camera", "layout", this.getPackageName());

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        setContentView(activity_capture_camera);
        setScreenSize(this);
        width = srcWidth;
        height = srcHeight;
    }

    public int px2dip(float pxValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    @Override
    protected void onStart(){
        super.onStart();

        System.out.println("On start!");

        int surfaceViews = getResources().getIdentifier("surfaceView", "id", this.getPackageName());
        surfaceView = (SurfaceView) findViewById(surfaceViews);

        int back_camera = getResources().getIdentifier("back_camera", "id", this.getPackageName());
        back = (Button) findViewById(back_camera);

        Drawable img = getResources().getDrawable(R.drawable.btn_back);
        img.setBounds(0, 0, (int) (img.getIntrinsicWidth() * 0.4), (int) (img.getIntrinsicHeight() * 0.4));
        back.setCompoundDrawables(img, null, null, null);
        back.setTextColor(Color.rgb(255,255,255));
        back.setText(getResources().getIdentifier("back", "string", this.getPackageName()));

        int flash_camera = getResources().getIdentifier("flash_camera", "id", this.getPackageName());
        flash = (ImageButton) findViewById(flash_camera);

        int btn_takepic = getResources().getIdentifier("imbtn_takepic", "id", this.getPackageName());
        imbtn_takepic = (ImageButton) findViewById(btn_takepic);

        int viewfinder_views = getResources().getIdentifier("viewfinder_view", "id", this.getPackageName());
        viewfinder_view = (ViewfinderView) findViewById(viewfinder_views);
        viewfinder_view.setRegister(Boolean.TRUE);

        int bg_camera_doctypes = getResources().getIdentifier("bg_camera_doctype", "id", this.getPackageName());
        bg_camera_doctype = (RelativeLayout) findViewById(bg_camera_doctypes);

        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/ZawgyiOne2008.ttf");

        int tv_camera_doctypes = getResources().getIdentifier("tv_camera_doctypexx", "id", this.getPackageName());
        tv_camera_doctype = (TextView) findViewById(tv_camera_doctypes);
        tv_camera_doctype.setTypeface(custom_font);
        tv_camera_doctype.setTextColor(Color.parseColor("#FFFFFF"));
        tv_camera_doctype.setText(overlayText);

        int title_navigation = getResources().getIdentifier("title_navi", "id", this.getPackageName());
        title_navi = (TextView) findViewById(title_navigation);
        title_navi.setTypeface(custom_font);
        title_navi.setTextColor(Color.rgb(255, 255, 255));
        title_navi.setText(getResources().getIdentifier("title_activity_capture_camera_myKad", "string", this.getPackageName()));
        title_navi.setTextSize(18);
        title_navi.setMinHeight(56);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(DocumentSnapperRegister.this);
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

        bg_camera_doctype.setVisibility(View.VISIBLE);

        if (width * 3 == height * 4) {
            isFatty = true;
        }

        System.out.println("isFatty >>> " + isFatty);

        if (width == surfaceView.getWidth() || surfaceView.getWidth() == 0) {
            System.out.println("yes takepic >>> ");
            layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, height);
            surfaceView.setLayoutParams(layoutParams);

            layoutParams = new RelativeLayout.LayoutParams((int) (width * 0.20), (int) (width * 0.20));
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT); //here parent or vertical
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.bottomMargin = (int) (width * 0.05);

            imbtn_takepic.setLayoutParams(layoutParams);

        } else if (width > surfaceView.getWidth()){

            System.out.println("no takepic >>> ");
            int surfaceViewHeight = (surfaceView.getWidth() * height) / width;
            layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, surfaceViewHeight);
            layoutParams.topMargin = (height - surfaceViewHeight) / 2;
            surfaceView.setLayoutParams(layoutParams);

            layoutParams = new RelativeLayout.LayoutParams((int) (width * 0.20), (int) (width * 0.20));
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.bottomMargin = (int) (width * 0.05);

            imbtn_takepic.setLayoutParams(layoutParams);
        }

        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        back.setLayoutParams(layoutParams);

        int flash_w = (int) (width * 0.08);
        int flash_h = (int) (flash_w * 1);
        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        layoutParams.rightMargin = (int) flash_h / 2;
        flash.setLayoutParams(layoutParams);

//         if (surfaceWidth < width || surfaceHeight < height) {
//             layoutParams = new RelativeLayout.LayoutParams(surfaceWidth, surfaceHeight);
//             layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//             surfaceView.setLayoutParams(layoutParams);
//             layoutParams = new RelativeLayout.LayoutParams((int) (width * 0.1), (int) (width * 0.1));
//             layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
//             if (width == 1280 && height == 800)
//                 layoutParams.leftMargin = (int) (width * 0.85);
//             else
//                 layoutParams.leftMargin = (int) (width * 0.885);
// //			imbtn_takepic.setLayoutParams(layoutParams);
//         }

        back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        flash.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    Toast.makeText(DocumentSnapperRegister.this,
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
                            flash.setBackgroundResource(R.drawable.mb_ic_flash_off_24dp);
                        } else {
                            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);// ����Ƴ���
                            parameters.setExposureCompensation(-1);
                            flash.setBackgroundResource(R.drawable.mb_ic_flash_on_24dp);
                        }
                        try {
                            camera.setParameters(parameters);
                        } catch (Exception e) {
                            Toast.makeText(DocumentSnapperRegister.this,
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
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
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
            camera.setPreviewCallback(DocumentSnapperRegister.this);
            camera.setParameters(parameters);
            camera.startPreview();
            msg = new Message();
            handler.sendMessage(msg);

        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
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
        System.out.println("onKeyDown >>> ");
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

        parameters.setPictureFormat(PixelFormat.JPEG);
        System.out.println(" preWidth >>> " + preWidth + " preHeight >>> " + preHeight);
        parameters.setPreviewSize(preWidth, preHeight);

        Camera.Size supportedPictureSize = this.getPictureSize();

        System.out.println(" picture Width >>> " + supportedPictureSize.width + " picture Height >>> " + supportedPictureSize.height);
        parameters.setPictureSize(supportedPictureSize.width, supportedPictureSize.height);

        if (parameters.getSupportedFocusModes().contains(parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);// 1�����Խ�
        }

        // for portrait orientation
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
        {
            parameters.set("orientation", "landscape");
            camera.setDisplayOrientation(90);
        }

        camera.setPreviewCallback(this);
        camera.setParameters(parameters);
        camera.startPreview();
    }

    private Camera.Size getPictureSize() {
        List<Camera.Size> supportedPictureSizeList = parameters.getSupportedPictureSizes();
        for (Camera.Size supportedSize : supportedPictureSizeList) {
            System.out.println("getPictureSize >>> width " + srcWidth + " height " + srcHeight );
            System.out.println("supported >>> width " + supportedSize.width + " height " + supportedSize.height );
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
        File mediaStorageDir = new File(DocumentSnapperRegister.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "allianceonline");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        System.out.println("output path >>> " + mediaStorageDir.getPath());
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg";
    }

    byte[] resizeImage(byte[] input) {
        System.out.println("resizeImage >>> " + getRequestedOrientation());
        Bitmap original = BitmapFactory.decodeByteArray(input, 0, input.length);
        Bitmap resized = Bitmap.createScaledBitmap(original, preWidth, preHeight, true);
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 20, blob);

        System.out.println("resizeImage >>> " + original + " resize >>> " + resized);
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
    public void onPreviewFrame(byte[] bytes, Camera camera) {
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
            camera.autoFocus(new Camera.AutoFocusCallback() {
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        camera.stopPreview();
                        camera.startPreview();
                        camera.takePicture(shutterCallback, null, picturecallback);
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
        System.out.print("getCameraPreParameters -> srcWidth >>> " + srcWidth + " srcHeight >>> " + srcHeight);
        isShowBorder = false;

        preWidth = 0;
        preHeight = 0;
        parameters = camera.getParameters();
        list = parameters.getSupportedPreviewSizes();
        float ratioScreen = (float) srcWidth / srcHeight;
        for (int i = 0; i < list.size(); i++) {
            System.out.print("list size -> " + list.size() + " listwidth >>> " + list.get(i).width + " srcHeight >>> " + list.get(i).height);
            float ratioPreview = (float) list.get(i).width / list.get(i).height;
            System.out.println(" ratio preview >>> " + ratioPreview);
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

        System.out.println(" preWidth2: " + preWidth + " --preHeight2: " + preHeight);
        if (preWidth == 0 || preHeight == 0) {
            isShowBorder = true;
            preWidth = list.get(0).width;
            preHeight = list.get(0).height;
            for (int i = 0; i < list.size(); i++) {
                System.out.println("list size 2 -> " + list.size() + " list [0] >>> " + list.get(0).width + " list [?] >>> " + list.get(list.size() - 1).width);
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
        System.out.println(" isShowBorder >>> " + isShowBorder);
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

        System.out.println(" surfaceWidth1: " + surfaceWidth + " --surfaceHeight1: " + surfaceHeight);

        System.out.println(" preWidth: " + preWidth + " --preHeight: " + preHeight);
    }

    @SuppressLint("NewApi")
    private void setScreenSize(Context context) {
        int x,y;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point screenSize = new Point();
        display.getRealSize(screenSize);
        x = screenSize.x;
        y = screenSize.y;

        srcWidth = x;
        srcHeight = y;
    }
}

