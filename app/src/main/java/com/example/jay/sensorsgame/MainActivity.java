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
import android.view.Display;
import android.view.View;

import static android.R.attr.x;
import static android.R.attr.y;

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
    private Paint infoPaint;
    private Bitmap ball;
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
    // (5) Instantiate SensorManger by referencing the system's sensormanager
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

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);                   // (5)



    }

    @Override
    protected void onStart(){
        super.onStart();
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);

    }

    private void updateBall(){
        float frameTime = 0.666f;
        xVel += (xAccel * frameTime);
        yVel += (yAccel * frameTime);

        float xS = (xVel / 2) * frameTime;
        float yS = (yVel / 2) * frameTime;

        xPos -= xS;
        yPos -= yS;

        if (xPos > xMax) {
            xPos = xMax;
        } else if (xPos < 0) {
            xPos = 0;
        }

        if (yPos > yMax) {
            yPos = yMax;
        } else if (yPos < 0) {
            yPos = 0;
        }

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
            infoPaint = new Paint();
            infoPaint.setTextAlign(Paint.Align.LEFT);
            infoPaint.setColor(Color.BLACK);
            infoPaint.setTextSize(48);
            Bitmap ballSrc = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
            final int dstWidth = 100;
            final int dstHeight = 100;
            ball = Bitmap.createScaledBitmap(ballSrc, dstWidth, dstHeight, true);

        }

        @Override
        protected void onDraw(Canvas canvas){
            canvas.drawText("Level", 100, 50, infoPaint);
            canvas.drawBitmap((ball), xPos, yPos, null);
            invalidate();
        }
    }
}
