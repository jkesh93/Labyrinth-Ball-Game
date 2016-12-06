package com.example.jay.sensorsgame;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.Random;

import static android.R.attr.screenSize;
import static android.R.attr.value;
import static android.R.attr.x;
import static android.R.attr.y;
import static java.lang.System.out;

public class MainActivity extends AppCompatActivity {

    // FULL DISCLOSURE: Built this app with help from the tutorial found on
    // url(https://androidkennel.org/android-sensors-game-tutorial/). Any
    // resemblance with respect to fields, methods, and structure is due to this.
    // I tried to do somethings differently to reflect some original creativity
    // in the final outcome of this project and to provide a fuller learning experience
    // rather than just simply understand the tutorial as is, and to fulfill project requirements.

    // Fields -- these fields  hold the ball's position, accel, x + y direction velocities.
    // xMax and yMax represent screen's dimensions to make bounds and we have a bitmap object to hold the ball
    private float xPos, xAccel, xVel = 0.0f;
    private float yPos, yAccel, yVel = 0.0f;
    private float xMax, yMax;
    private int score = 0;
    private int level = 0;

    // debugging variables
    private int debugging = 0;
    private int atWall = 0;
    long timeNow;
    long timeTemp;
    int tapCount = 0;

    // Game Variables;
    private boolean spriteHitWall;
    private int[] screenSize;
    private int spriteLocX;
    private int spriteLocY;
    private int starsInARow = 0;
    private int bestStarsInARow = 0;

    private Random r = new Random();
    private Paint infoPaint;

    // Resource images
    private Bitmap ball;
    private Bitmap star;
    private Bitmap backgroundImage;


    // Sensor Manager and Listener
    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                xAccel = event.values[0];
                yAccel = -event.values[1];                                                      // don't know why we have to negate the y value...
                updateBall();
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


    // (1) Modified the onCreate method to ensure permanent portrait mode
    // (2) Built our new custom view using the (3) class constructor we built below.
    // (4) Obtain screen size to set xMax and yMax to it
    // (5) Instantiate SensorManger by referencing the system's sensor manager
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);                          // (1)
        BallView ballView = new BallView(this);                                                     // (2)
        setContentView(ballView);



        Point size = new Point();
        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(size);
        xMax = (float) size.x - 100;                                                                // (4)
        yMax = (float) size.y - 360;                                                                // (4)
        spriteHitWall = false;
        r = new Random();



        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);                   // (5)



    }

    @Override
    protected void onStart(){
        super.onStart();
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);

    }

    // update ball -- game engine runs from here mostly.
    private void updateBall(){

        float frameTime = 0.666f;
        if(atWall == 0) {
            xVel += (xAccel * frameTime);
            yVel += (yAccel * frameTime);
        }

        float xS = (xVel / 2) * frameTime;
        float yS = (yVel / 2) * frameTime;

        xPos -= xS;
        yPos -= yS;



        if (xPos > xMax) {
            xPos = xMax;
            xVel = 0;
            starsInARow = 0;

        } else if (xPos < 0) {
            xPos = 0;
            xVel = 0;
            starsInARow = 0;

        }

        if (yPos > yMax) {
            yPos = yMax;
            yVel = 0;
            starsInARow = 0;


        } else if (yPos <= 0) {
            yPos = 0;
            yVel = 0;
            starsInARow = 0;

        }

        if(starsInARow > bestStarsInARow){
            bestStarsInARow = starsInARow;
        }



        /*
        If the ball is close enough to the star, reposition the star and try again
         */
        int minDistance = 100;
        if(Math.abs(xPos - spriteLocX) < minDistance && Math.abs(yPos - spriteLocY) < minDistance){
            spriteLocX = r.nextInt(screenSize[0]);
            spriteLocY = r.nextInt(screenSize[1]);
            if(!spriteHitWall){
                starsInARow++;
                score++;
            }
            spriteHitWall = false;
        }


    }

    // function for getting screen size;
    public int[] getScreenSize(){
        int[] screenSizeArray = new int[2];
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE); // the results will be higher than using the activity context object or the getWindowManager() shortcut
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        screenSizeArray[0] = screenWidth - 100;
        screenSizeArray[1] = screenHeight - 360;

        return screenSizeArray;
    }





    @Override
    protected void onStop(){
        sensorManager.unregisterListener(sensorEventListener);
        super.onStop();
    }

    private class BallView extends View {


        // (3)
        public BallView(Context context){
            super(context);
            // get values for positioning;
            screenSize = getScreenSize();

            infoPaint = new Paint();
            infoPaint.setTextAlign(Paint.Align.LEFT);
            infoPaint.setColor(Color.BLACK);
            infoPaint.setTextSize(48);
            Bitmap ballSrc = BitmapFactory.decodeResource(getResources(), R.drawable.ball); // ball player
            Bitmap starSrc = BitmapFactory.decodeResource(getResources(), R.drawable.star); // star points
            Bitmap floorSrc = BitmapFactory.decodeResource(getResources(), R.drawable.floor); // background floor
            final int dstWidth = 100;
            final int dstHeight = 100;
            final int tall = 75;
            final int wide = 75;
            ball = Bitmap.createScaledBitmap(ballSrc, dstWidth, dstHeight, true);
            star = Bitmap.createScaledBitmap(starSrc, wide, tall, true);
            backgroundImage = Bitmap.createScaledBitmap(floorSrc, screenSize[0]+100, screenSize[1]+150, true);

            // get values for positioning;
            spriteLocX = r.nextInt(screenSize[0]);
            spriteLocY = r.nextInt(screenSize[1]);


        }

        @Override
        protected void onDraw(Canvas canvas){
            canvas.drawBitmap(backgroundImage,0,0,null);
            canvas.drawText("Score: " + score, 100, 100, infoPaint);
            canvas.drawText("Streak: " + starsInARow, 100, 150, infoPaint);
            canvas.drawText("Best: " + bestStarsInARow, 100, 200, infoPaint);
          //  canvas.drawText("Score: " + score, 100, 100, infoPaint);
            if(debugging == 1) {
                canvas.drawText("Position of ball: x: " + xPos, 100, 250, infoPaint);
                canvas.drawText("Position of ball: y: " + yPos, 100, 300, infoPaint);
                canvas.drawText("Accel : x: " + xAccel, 100, 350, infoPaint);
                canvas.drawText("Accel : y: " + yAccel, 100, 400, infoPaint);
                canvas.drawText("Vel : x: " + xVel, 100, 450, infoPaint);
                canvas.drawText("Vel : y: " + yVel, 100, 500, infoPaint);
            }
            canvas.drawBitmap(ball, xPos, yPos, null);
            canvas.drawBitmap(star, spriteLocX, spriteLocY, null);
            invalidate();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event){
            long diff = 1000;
            timeNow = System.currentTimeMillis();
            if(debugging == 0 && Math.abs(timeNow - timeTemp) > diff || tapCount == 0){
                debugging = 1;
                timeTemp = System.currentTimeMillis();
                Log.v("alert: ", "timeNOW: " + timeNow + " and timeTemp: " + timeTemp);
                tapCount++;
            }
            if(debugging == 1 && Math.abs(timeNow - timeTemp) > diff || tapCount == 0){
                debugging = 0;
                timeTemp = System.currentTimeMillis();
                Log.v("alert: ", "timeNOW: " + timeNow + " and timeTemp: " + timeTemp);
                tapCount++;
            }



            return true;
        }

    }

}
