package edu.asu.artag.Data.CameraPaintBoard;


import android.app.Activity;
import android.content.Context;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.io.OutputStream;


public class PaintBoard extends View {

    private Paint mPaint = null;
    private Bitmap mBitmap = null;
    public Canvas mBitmapCanvas = null;

    private float startX;
    private float startY ;


    public PaintBoard(Context context, AttributeSet attrs) {
        super(context, attrs);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        mBitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        //mBitmap = Bitmap.createBitmap((int)Math.floor(0.5*width),(int)Math.floor(0.5*height), Bitmap.Config.ARGB_8888);
        mBitmapCanvas = new Canvas(mBitmap);
        //mBitmapCanvas.drawColor(Color.argb(255,255,255,255));
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(18);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float stopX = event.getX();
                float stopY = event.getY();
//                Log.e(TAG,"onTouchEvent-ACTION_MOVE\nstartX is "+startX+
//                        " startY is "+startY+" stopX is "+stopX+ " stopY is "+stopY);
                mBitmapCanvas.drawLine(startX, startY, stopX, stopY, mPaint);
                startX = event.getX();
                startY = event.getY();
                invalidate();//call onDraw()
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        }
    }

    public void saveBitmap(OutputStream stream) {
        if (mBitmap != null) {
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        }
        //screenShot(activity.getWindow().getDecorView()).compress(Bitmap.CompressFormat.JPEG, 0, stream);
    }

    public Bitmap screenShot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }


}
