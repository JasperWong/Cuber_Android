package com.jasper.cuber;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

//implements Runnable
public class JoystickView extends View  {
	// Constants
//	private OnUpActionListener mUp = null;
	private final double RAD = 57.2957795;
	public final static long DEFAULT_LOOP_INTERVAL = 100; // 100 ms
	public final static int FRONT = 3;
	public final static int FRONT_RIGHT = 4;
	public final static int RIGHT = 5;
	public final static int RIGHT_BOTTOM = 6;
	public final static int BOTTOM = 7;
	public final static int BOTTOM_LEFT = 8;
	public final static int LEFT = 1;
	public final static int LEFT_FRONT = 2;
	public final static int joy_up=1;
	public final static int joy_down=2;
	public final static int joy_left=3;
	public final static int joy_right=4;

	// Variables
//	private OnJoystickMoveListener onJoystickMoveListener; // Listener
//	private Thread thread = new Thread(this);
	private long loopInterval = DEFAULT_LOOP_INTERVAL;
	private int xPosition = 0; // Touch x position
	private int yPosition = 0; // Touch y position
	private double centerX = 0; // Center view x position
	private double centerY = 0; // Center view y position
	private Paint mainCircle;
	private Paint secondaryCircle;
	private Paint button;
	private Paint horizontalLine;
	private Paint verticalLine;
	private int joystickRadius;
	private int buttonRadius;
	private int BottomXFix=0;
	private int BottomYFix=0;
	private int TopXFix=0;
	private int TopYFix=0;
	private int lastAngle = 0;
	private int lastPower = 0;
	private int postion=0;
	private int SituationX =0;
	private int SituationY =0;

	Bitmap JoystickBottom=null;
	Bitmap JoystickTop=null;

	public JoystickView(Context context) {
		super(context);
	}

	public JoystickView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initJoystickView();
	}

	public JoystickView(Context context, AttributeSet attrs, int defaultStyle) {
		super(context, attrs, defaultStyle);
		initJoystickView();
	}

//    public void Setup()
//    {
//        // 1.we only need to setup for one time
////        if (bHasSetup)
////        {
////            return;
////        }
//
////        m_ViewWidth = RockerView.this.getWidth();
////        m_ViewHeight = RockerView.this.getHeight();
////        m_ViewOriginX = m_ViewWidth / 2;
////        m_ViewOriginY = m_ViewHeight / 2;
//
////        // 3. !!!:we should use square/circle bitmap
////        BitmapDrawable drawable = (BitmapDrawable) getContext().getResources().getDrawable(R.drawable.table);
////        Bitmap bmp = drawable.getBitmap();
//////        int density = bmp.getDensity();
////        int height = bmp.getHeight();
//////        float Dpi = ((float) density) / 240;
////        // suppose height == width
////        float WidthScaleRate = (float) m_ViewWidth / height;
////        float HeightScaleRate = (float) m_ViewHeight / height;
////        float MinScaleRate = WidthScaleRate < HeightScaleRate ? WidthScaleRate : HeightScaleRate;
////        if (MinScaleRate > (float) 2.0)
////        {
////            MinScaleRate = (float) 2.0;
////        }
////        Matrix matrix = new Matrix();
////        matrix.postScale(MinScaleRate, MinScaleRate);
////        bmpTouchNothing = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
////
////        BitmapWidth = bmpTouchNothing.getWidth();
////        BitmapHeight = bmpTouchNothing.getHeight();
////
////        // 4
////        drawable = (BitmapDrawable) getContext().getResources().getDrawable(R.drawable.ic_rocker_click);
////        bmp = drawable.getBitmap();
////        bmpTouching = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
////
////        // 5
////        bHasSetup = true;
//    }



    protected void initJoystickView() {
		mainCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
		mainCircle.setColor(Color.LTGRAY);
		mainCircle.setStyle(Paint.Style.FILL_AND_STROKE);


		secondaryCircle = new Paint();
		secondaryCircle.setColor(Color.BLACK);
		secondaryCircle.setStyle(Paint.Style.STROKE);


		verticalLine = new Paint();
		verticalLine.setStrokeWidth(5);
		verticalLine.setColor(Color.RED);

		horizontalLine = new Paint();
		horizontalLine.setStrokeWidth(2);
		horizontalLine.setColor(Color.BLACK);

		button = new Paint(Paint.ANTI_ALIAS_FLAG);
		button.setColor(Color.BLACK);
		button.setStyle(Paint.Style.FILL);


	}

	@Override
	protected void onFinishInflate() {
	}

	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
		super.onSizeChanged(xNew, yNew, xOld, yOld);
		// before measure, get the center of view
		xPosition = (int) getWidth() / 2;						//initial x,yPosition
		yPosition = (int) getWidth() / 2;

//		xPosition=240;
//		yPosition=240;
		int d = Math.min(xNew, yNew);
		buttonRadius = (int) (d / 2 * 0.25);
		joystickRadius = (int) (d / 2 * 0.45);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// setting the measured values to resize the view to a certain width and
		// height
		int d = Math.min(measure(widthMeasureSpec), measure(heightMeasureSpec));

		setMeasuredDimension(d, d);

	}

	private int measure(int measureSpec) {
		int result = 0;

		// Decode the measurement specifications.
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.UNSPECIFIED) {
			// Return a default size of 200 if no bounds are specified.
			result = 150;
		} else {
			// As you want to fill the available space
			// always return the full available bounds.
			result = specSize;
		}
		return result;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		centerX = ((getWidth()) /2);
		centerY = ((getHeight()) /2);

		Log.d("pos","PostionX:"+xPosition);
		Log.d("pos","PostionY:"+yPosition);
		Log.d("pos", "SituitionX:" + GetSituationX());
		Log.d("pos", "SituitionY:" + GetSituationY());



		JoystickBottom=((BitmapDrawable)getResources().getDrawable(R.drawable.joystick_bottom)).getBitmap();
		JoystickTop=((BitmapDrawable)getResources().getDrawable(R.drawable.joystick_top)).getBitmap();
		BottomXFix=JoystickBottom.getWidth()/2;
		BottomYFix=JoystickBottom.getHeight()/2;
		TopXFix=JoystickTop.getWidth()/2;
		TopYFix=JoystickTop.getHeight()/2;
		canvas.drawBitmap(JoystickBottom, (int) centerX - BottomXFix, (int) centerY - BottomXFix, null);        //绘制图像
		canvas.drawBitmap(JoystickTop, xPosition - TopXFix, yPosition - TopXFix, null);
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {


		xPosition = (int) event.getX();
		yPosition = (int) event.getY();

		double abs = Math.sqrt((xPosition - centerX) * (xPosition - centerX)
                + (yPosition - centerY) * (yPosition - centerY));
		if (abs > joystickRadius) {
			xPosition = (int) ((xPosition - centerX) * joystickRadius / abs + centerX);
			yPosition = (int) ((yPosition - centerY) * joystickRadius / abs + centerY);
		}
		invalidate();
		if (event.getAction() == MotionEvent.ACTION_UP) {
			xPosition = (int) centerX;
			yPosition = (int) centerY;
			Control_vertical.mUp.setImageResource(R.drawable.up_0);
			Control_vertical.mDown.setImageResource(R.drawable.down_0);
			Control_vertical.mRight.setImageResource(R.drawable.right_0);
			Control_vertical.mLeft.setImageResource(R.drawable.left_0);

//			thread.interrupt();
//			if (onJoystickMoveListener != null)
//				onJoystickMoveListener.onValueChanged(getAngle(), getPower(),
//						getDirection());

		}
//		if (onJoystickMoveListener != null
//				&& event.getAction() == MotionEvent.ACTION_DOWN) {
//			if (thread != null && thread.isAlive()) {
//				thread.interrupt();
//			}
//			thread = new Thread(this);
//			thread.start();
//			if (onJoystickMoveListener != null)
//				onJoystickMoveListener.onValueChanged(getAngle(), getPower(),
//						getDirection());
//		}
		return true;
	}

//	public void setOnUpActionListener(OnUpActionListener up){

//	}

	public interface onUpActionListener{
		public void OnUp();
	}
//	private int getAngle() {
//		if (xPosition > centerX) {
//			if (yPosition < centerY) {
//				return lastAngle = (int) (Math.atan((yPosition - centerY)
//                        / (xPosition - centerX))
//						* RAD + 90);
//			} else if (yPosition > centerY) {
//				return lastAngle = (int) (Math.atan((yPosition - centerY)
//                        / (xPosition - centerX)) * RAD) + 90;
//			} else {
//				return lastAngle = 90;
//			}
//		} else if (xPosition < centerX) {
//			if (yPosition < centerY) {
//				return lastAngle = (int) (Math.atan((yPosition - centerY)
//                        / (xPosition - centerX))
//						* RAD - 90);
//			} else if (yPosition > centerY) {
//				return lastAngle = (int) (Math.atan((yPosition - centerY)
//                        / (xPosition - centerX)) * RAD) - 90;
//			} else {
//				return lastAngle = -90;
//			}
//		} else {
//			if (yPosition <= centerY) {
//				return lastAngle = 0;
//			} else {
//				if (lastAngle < 0) {
//					return lastAngle = -180;
//				} else {
//					return lastAngle = 180;
//				}
//			}
//		}
//	}

    public double getRoll()
    {
        return 20.0 * (xPosition - centerX) / joystickRadius;
    }

    public double getPitch()
    {
        return 20.0 *  (centerY - yPosition ) / joystickRadius;
    }


	public int GetSituationX()
	{
		int Temp=(int)(xPosition-centerX);


		if(Temp>0){
				return joy_right;
			}
		if(Temp<0){
			return joy_left;
		}
		else return 0;
	}

	public int GetSituationY()
	{
		int Temp=(int)(yPosition-centerY);

		if(Temp>0){
			return joy_down;
		}
		if(Temp<0){
			return joy_up;
		}
		else return 0;
	}

    private int getPower() {
		return (int) (100 * Math.sqrt((xPosition - centerX)
                * (xPosition - centerX) + (yPosition - centerY)
                * (yPosition - centerY)) / joystickRadius);
	}

	private int getDirection() {
		if (lastPower == 0 && lastAngle == 0) {
			return 0;
		}
		int a = 0;
		if (lastAngle <= 0) {
			a = (lastAngle * -1) + 90;
		} else if (lastAngle > 0) {
			if (lastAngle <= 90) {
				a = 90 - lastAngle;
			} else {
				a = 360 - (lastAngle - 90);
			}
		}

		int direction = (int) (((a + 22) / 45) + 1);

		if (direction > 8) {
			direction = 1;
		}
		return direction;
	}

//	public void setOnJoystickMoveListener(OnJoystickMoveListener listener,
//			long repeatInterval) {
//		this.onJoystickMoveListener = listener;
//		this.loopInterval = repeatInterval;
//	}

//	public static interface OnJoystickMoveListener {
//		public void onValueChanged(int angle, int power, int direction);
//	}





//	@Override
//	public void run() {
//		while (!Thread.interrupted()) {
//			post(new Runnable() {
//				public void run() {
//					if (onJoystickMoveListener != null)
//						onJoystickMoveListener.onValueChanged(getAngle(),
//								getPower(), getDirection());
//				}
//			});
//			try {
//				Thread.sleep(loopInterval);
//			} catch (InterruptedException e) {
//				break;
//			}
//		}
//	}
}