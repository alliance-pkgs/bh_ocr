package bh_ocr.checkauto.camera.com.utill;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

public final class ViewfinderView extends View {

	private static final int[] SCANNER_ALPHA = { 0, 64, 128, 192, 255, 192, 128, 64 };
	/**
	 * ˢ�½����ʱ��
	 */
	private static final long ANIMATION_DELAY = 10L;
	private static final int OPAQUE = 0xFF;
	/**
	 * �ж���Ļ����ת�Ķ�����Ӧ�ķ���ֵ�磺0,1,2,3
	 */
	private static int directtion = 1;

	public int getDirecttion() {
		return directtion;
	}

	public void setDirecttion(int directtion) {
		this.directtion = directtion;
	}

	private boolean isRegister = false;

	public void setRegister(boolean isRegister) { 
		this.isRegister = isRegister; 
	}

	public boolean isRegister() { 
		return isRegister; 
	}

	private final Paint paint;
	private final Paint paintLine;
	private Bitmap resultBitmap;

	// private final int resultColor;
	// private final int frameColor;
	// private final int laserColor;
	private int scannerAlpha;
	private int leftLine = 0;
	private int topLine = 0;
	private int rightLine = 0;
	private int bottomLine = 0;
	/**
	 * �м们���ߵ����λ��
	 */
	private int slideTop;
	private int slideTop1;

	/**
	 * �м们���ߵ���׶�λ��
	 */
	private int slideBottom;
	/**
	 * �м�������ÿ��ˢ���ƶ��ľ���
	 */
	private static final int SPEEN_DISTANCE = 10;
	/**
	 * ɨ����е��ߵĿ��
	 */
	private static final int MIDDLE_LINE_WIDTH = 0;
	private boolean isFirst = false;
	/**
	 * ���ܱ߿�Ŀ��
	 */
	private static final int FRAME_LINE_WIDTH = 0;
	private Rect frame;

	private int maskColor;
	private int frameColor;

	public int rt, rb, rl, rr;

	public ViewfinderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// this.w = w;
		// this.h = h;
		paint = new Paint();
		paintLine = new Paint();
		WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		scannerAlpha = 0;
		// frameColor = getResources().getColor(R.color.viewfinder_frame);// ��ɫ
		// maskColor = getResources().getColor(R.color.viewfinder_mask);

	}

	public void setLeftLine(int leftLine) {
		this.leftLine = leftLine;
	}

	public void setTopLine(int topLine) {
		this.topLine = topLine;
	}

	public void setRightLine(int rightLine) {
		this.rightLine = rightLine;
	}

	public void setBottomLine(int bottomLine) {
		this.bottomLine = bottomLine;
	}

	@Override
	public void onDraw(Canvas canvas) {
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		System.out.println("width >>> " + width + ", height >>> " + height);

		int t = 0;
		int b = 0;
		int l = 0;
		int r = 0;

		if (width > height && width * 3 != height * 4) {
			/**
			 * ������ξ����м���ʾ���Ǹ����
			 */
			int $t = height / 10;
			t = $t;
			b = height - t;
			int $l = (int) ((b - t) * 1.585);
			l = (width - $l) / 2;
			r = width - l;

			System.out.println("width > height");	
			System.out.println("l1 >>> " + l + " t1 >>> " + t + " r1 >>> " + r + " b1 >>> " + b);

			l = l + 30;
			t = t + 19;
			r = r - 30;
			b = b - 19;

			System.out.println("l2 >>> " + l + " t2 >>> " + t + " r2 >>> " + r + " b2 >>> " + b);
			frame = new Rect(l, t, r, b);
		} else {
			System.out.println("isRegister >>> " + this.isRegister());
			if (this.isRegister()){
				int $t = height / 7; // 7 = 274
				t = $t;
				b = height - t;	// 1642
				int $l = (int) ((b - t) * 1.585); //2168
				// l = (width - $l) / 2;
				l = ($l - width) / 8; // divide 8 = 136
				r = width - l; //808

				System.out.println("no width > height");
				System.out.println("l3 >>> " + l + " t3 >>> " + t + " r3 >>> " + r + " b3 >>> " + b);

				l = l - 30; //100;//l + 20;
				t = t + 10; //283;//t + 10;
				r = r + 30; //980;//r - 20;
				b = b - 10; //1633;//b - 10;

				System.out.println("l4 >>> " + l + " t4 >>> " + t + " r4 >>> " + r + " b4 >>> " + b);
				frame = new Rect(l, t, r, b);
			} else {
				int $t = height / 5;
				t = $t;
				b = height - t;
				int $l = (int) ((b - t) * 1.585);
				l = (width - $l) / 2;
				r = width - l;

				System.out.println("no width > height");
				System.out.println("l3 >>> " + l + " t3 >>> " + t + " r3 >>> " + r + " b3 >>> " + b);

				l = l + 30;
				t = t + 19;
				r = r - 30;
				b = b - 19;

				System.out.println("l4 >>> " + l + " t4 >>> " + t + " r4 >>> " + r + " b4 >>> " + b);
				frame = new Rect(l, t, r, b);
			}
		}

		// ����ɨ����������Ӱ���֣����ĸ����֣�ɨ�������浽��Ļ���棬ɨ�������浽��Ļ����
		// ɨ��������浽��Ļ��ߣ�ɨ�����ұߵ���Ļ�ұ�
		paint.setColor(Color.argb(128, 0, 0, 0));
		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
		canvas.drawRect(0, frame.bottom + 1, width, height, paint);

		if (width > height && width * 3 != height * 4) {

			if(this.isRegister()){
				paintLine.setColor(Color.WHITE);
				paintLine.setStrokeWidth(8);
				paintLine.setAntiAlias(true);
				paintLine.setStyle(Paint.Style.STROKE);
				canvas.drawRoundRect(new RectF(l, t, r, b), 15, 15, paintLine);
			}
			else{
				paintLine.setColor(Color.rgb(0, 255, 0));
				paintLine.setStrokeWidth(8);
				paintLine.setAntiAlias(true);
				int num = t - 40;
				canvas.drawLine(l - 4, t, l + num, t, paintLine);
				canvas.drawLine(l, t, l, t + num, paintLine);
	
				canvas.drawLine(r, t, r - num, t, paintLine);
				canvas.drawLine(r, t - 4, r, t + num, paintLine);
	
				canvas.drawLine(l - 4, b, l + num, b, paintLine);
				canvas.drawLine(l, b, l, b - num, paintLine);
	
				canvas.drawLine(r, b, r - num, b, paintLine);
				canvas.drawLine(r, b + 4, r, b - num, paintLine);
	
				if (leftLine == 1) {
					canvas.drawLine(l, t, l, b, paintLine);
				}
				if (rightLine == 1) {
					canvas.drawLine(r, t, r, b, paintLine);
				}
				if (topLine == 1) {
					canvas.drawLine(l, t, r, t, paintLine);
				}
				if (bottomLine == 1) {
					canvas.drawLine(l, b, r, b, paintLine);
				}
			}

		} else {
			if(this.isRegister()){
				paintLine.setColor(Color.WHITE);
				paintLine.setStrokeWidth(8);
				paintLine.setAntiAlias(true);
				paintLine.setStyle(Paint.Style.STROKE);
				canvas.drawRoundRect(new RectF(l, t, r, b), 15, 15, paintLine);
			}
			else{
				paintLine.setColor(Color.rgb(0, 255, 0));
				paintLine.setStrokeWidth(8);
				paintLine.setAntiAlias(true);

				canvas.drawLine(l, t, l + 100, t, paintLine);
				canvas.drawLine(l, t, l, t + 100, paintLine);
				canvas.drawLine(r, t, r - 100, t, paintLine);
				canvas.drawLine(r, t, r, t + 100, paintLine);
				canvas.drawLine(l, b, l + 100, b, paintLine);
				canvas.drawLine(l, b, l, b - 100, paintLine);
				canvas.drawLine(r, b, r - 100, b, paintLine);
				canvas.drawLine(r, b, r, b - 100, paintLine);
			}
			
		}

		if (frame == null) {
			return;
		}
		rt = t;
		rb = b;
		rl = l;
		rr = r;
		/**
		 * �����ǻ�ý����ʱ�����Ǹ���������Ļ������
		 */
		postInvalidateDelayed(ANIMATION_DELAY);

	}
}
