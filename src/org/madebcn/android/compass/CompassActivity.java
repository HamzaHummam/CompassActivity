/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.madebcn.android.compass;


import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

/**
 * This is an example of using the accelerometer to integrate the device's
 * acceleration to a position using the Verlet method. This is illustrated with
 * a very simple particle system comprised of a few iron balls freely moving on
 * an inclined wooden table. The inclination of the virtual table is controlled
 * by the device's accelerometer.
 * 
 * @see SensorManager
 * @see SensorEvent
 * @see Sensor
 */

public class CompassActivity extends Activity {
  
    private SimulationView mSimulationView;
    private SensorManager mSensorManager;
    private PowerManager mPowerManager;
    private WakeLock mWakeLock;
    
    //UI elements
    protected TextView angleField;
    protected EditText serverField; //editText1
    protected Button testButton;//button1
    protected Switch switchButton; //Switch1
    protected Button setangleButton;//button2
    protected TextView statusField; //textview3
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
  

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        
        // Get an instance of the SensorManager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        
        
        // Get an instance of the PowerManager
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);

        // Get an instance of the WindowManager

        // Create a bright wake lock
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass()
                .getName());

        // instantiate our simulation view and set it as the activity's content
        mSimulationView = new SimulationView(this);
        setContentView(R.layout.main);
        
        angleField = (TextView) findViewById(R.id.textView2);
        serverField = (EditText) findViewById(R.id.editText1); //editText1
	    testButton = (Button) findViewById(R.id.button1);//button1
	    switchButton =  (Switch) findViewById(R.id.switch1); //Switch1
	    setangleButton=  (Button) findViewById(R.id.button2);//button2
	    statusField=  (TextView) findViewById(R.id.textView3); //textview3
	    
	  

        //setContentView(mSimulationView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
         * when the activity is resumed, we acquire a wake-lock so that the
         * screen stays on, since the user will likely not be fiddling with the
         * screen or buttons.
         */
        mWakeLock.acquire();

        // Start the simulation
        mSimulationView.startSimulation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*
         * When the activity is paused, we make sure to stop the simulation,
         * release our sensor resources and wake locks
         */

        // Stop the simulation

        // and release our wake-lock
        mWakeLock.release();
    }

    class SimulationView extends View implements OnTouchListener {
        // diameter of the balls in meters
      
        private double lastAngle = 10;
        private String lastUrl ="";
        
   


        public void updateAnlge(double newAngle)
        {
        	this.lastAngle = newAngle; 
        	angleField.setText(""+newAngle);
        	
        }
        
        public void updateUrl(String newUrl)
        {
        	this.lastUrl = newUrl;
        }
        public void startSimulation() {
            /*
             * It is not necessary to get accelerometer events at a very high
             * rate, by using a slower rate (SENSOR_DELAY_UI), we get an
             * automatic low-pass filter, which "extracts" the gravity component
             * of the acceleration. As an added benefit, we use less power and
             * CPU resources.
             */
           // mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
            //Made addded-------------------------
            MadeListener ml = new MadeListener(mSimulationView);
             // Register this class as a listener for the accelerometer sensor
            mSensorManager.registerListener(ml, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                             SensorManager.SENSOR_DELAY_NORMAL);
            // ...and the orientation sensor
            mSensorManager.registerListener(ml, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                             SensorManager.SENSOR_DELAY_NORMAL);
        }

    

        public SimulationView(Context context) {
            super(context);

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
       


            Options opts = new Options();
            opts.inDither = true;
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            // compute the origin of the screen relative to the origin of
            // the bitmap
         
        }

      

        @Override
        protected void onDraw(Canvas canvas) {

            /*
             * draw the background
             */
        	  /*
            String rotatedAngle = +lastAngle+"�";
            String rotatedURL = "HTTP GET "+lastUrl;

            //Display the angle
           
            Paint paint = new Paint(); 
            paint.setColor(Color.BLUE); 
            paint.setStyle(Style.FILL); 
            canvas.drawPaint(paint); 


            paint.setColor(Color.WHITE); 
            paint.setTextSize(20); 
            canvas.drawText(rotatedURL, 10, 25, paint); 
            
          
            int xPos = (canvas.getWidth() / 2);
            int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ; 
            
            
            canvas.drawText(rotatedtext, xPos, xPos, paint); 
           
            
            int xx = 180;
            int yy = 790;
            paint.setColor(Color.GRAY);
            paint.setTextSize(150);

            Rect rect = new Rect();
            paint.getTextBounds(rotatedAngle, 0, 1, rect);
            canvas.translate(xx, yy);
            paint.setStyle(Paint.Style.FILL);

            canvas.translate(-xx, -yy);

            paint.setColor(Color.YELLOW);
            canvas.rotate(-90, xx + rect.exactCenterX(),yy + rect.exactCenterY());
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(rotatedAngle, xx, yy, paint);
            

            invalidate();
             */
        }

 

		@Override
		public boolean onTouch(View arg0, MotionEvent event) {
		
			return true;			
		}
    }
    
    
    public class MadeListener implements SensorEventListener {
    	  
    	  private static final String TAG = "MadeListener";
    	  protected int angleOffset =0;
    	  //Settings
    	  public final int INTERVAL = (int) (0.5* 1000);
    	  public String serverPath = "";
    	  protected boolean isOn = false;
    	  
    	  public long lastSentInfoTime;
    	  private int lastSentAngle ;
    	  private int lastReadAngle; 
    	  
    	 
    	  float[] inR = new float[16];
    	  float[] I = new float[16];
    	  float[] gravity = new float[3];
    	  float[] geomag = new float[3];
    	  float[] orientVals = new float[3];
    	  
    	  int absoluteAzimuth;
    	  double azimuth = 0;
    	  double pitch = 0;
    	  double roll = 0;
    	  
    	  private SimulationView view;
    	  
    	  String ip;
    	  
    	  public MadeListener(SimulationView view)
    	  {  
    		this.view=view;
    	    lastSentInfoTime = System.currentTimeMillis();
    	    lastSentAngle = 183;
    	    
    	    testButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    serverPath=serverField.getText().toString();
                    Log.v(TAG,serverPath); //Todo coment
                }
            });
    	    
    	    setangleButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                	angleOffset = lastReadAngle;
                    Log.v(TAG,"new offset set to "+ angleOffset); //Todo comment
                }
            });
    	    
    	    switchButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					isOn=isChecked;
					if(!isChecked)
						changeStatus("idle");
				}
			});
    	    
    	  }
    	  
    	  protected int getCalibratedAngle(int originalAngle)
    	  {
    		  int newAngle = 0; //initialization, will be overwritten
    		  
    		   if ((originalAngle-angleOffset)<0)  
    			   newAngle=(originalAngle-angleOffset)+360;
    		   else 
    			   newAngle = (originalAngle-angleOffset); 
    		  
    		  return newAngle;
    	  }
    	  
    	  
    	  private String generateServerPath(double angle)
    	  {
    	    return "http://"+serverPath+"/"+(int)angle ;
    	  }
    	  
    	  @Override
    	  public void onAccuracyChanged(Sensor arg0, int arg1) {
    	    // TODO Auto-generated method stub
    	  }
    	 

    	  @Override
    	  public void onSensorChanged(SensorEvent sensorEvent) {

    	    // If the sensor  is unreliable return
    	    if (sensorEvent.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
    	    {    
    	      //System.out.println("Status unreliable");
    	      //return;
    	    }

    	    // Gets the value of the sensor that has been changed
    	    switch (sensorEvent.sensor.getType()) {  
    	        case Sensor.TYPE_ACCELEROMETER:
    	            gravity = sensorEvent.values.clone();
    	            //System.out.println("Accelerometer changed");

    	            break;
    	        case Sensor.TYPE_MAGNETIC_FIELD:
    	            geomag = sensorEvent.values.clone();
    	            //System.out.println("geomag : "+  geomag);
    	            break;
    	    }

    	    // If gravity and geomag have values then find rotation matrix
    	    if (gravity != null && geomag != null) {
    	     /* System.out.println("gravity0="+  gravity[0]+ " geomag0="+geomag[0]);
    	      System.out.println("gravity1="+  gravity[1]+ " geomag1="+geomag[1]);
    	      System.out.println("gravity2="+  gravity[2]+ " geomag2="+geomag[2]);
    	      */

    	        // checks that the rotation matrix is found
    	        boolean success = SensorManager.getRotationMatrix(inR, I, gravity, geomag);

    	        if (success) {
    	            SensorManager.getOrientation(inR, orientVals);
    	            azimuth = Math.toDegrees(orientVals[0]);
    	            pitch = Math.toDegrees(orientVals[1]);
    	            roll = Math.toDegrees(orientVals[2]);
    	            
    	            absoluteAzimuth = (int) normalize((Math.round(azimuth)));
    	            lastReadAngle = absoluteAzimuth; //save the value 
    	            absoluteAzimuth = getCalibratedAngle(absoluteAzimuth); //add remove offset
    	            
    	            //Log.v(TAG, "azimuth="+Math.round(azimuth)+" pitch="+pitch+" roll="+roll);

    	            //Log.v(TAG, "Angle = "+absoluteAzimuth);
    	            long elapseTime = System.currentTimeMillis() - lastSentInfoTime;

    	            //Log.v(TAG,"elapseTime = "+ elapseTime);
    	          
    	            if (elapseTime >= INTERVAL)
    	            {
    	              lastSentInfoTime=System.currentTimeMillis(); 
    	              int differenceWithOld = Math.abs((int) (absoluteAzimuth - lastSentAngle));
    	              //Log.v(TAG, "old="+lastSentAngle+" now="+absoluteAzimuth+" difference="+differenceWithOld);
    	              view.updateAnlge((int)absoluteAzimuth);
    	              if(differenceWithOld>=4)
    	              {
    	                lastSentAngle = (int)(absoluteAzimuth);
    	                String url = generateServerPath(lastSentAngle);
    	                
    	                if(isOn)
    	                {
	    	                changeStatus("GET" +url);
	    	                openHttpConn(url);
    	                }
    	              }
    	            }
    	        }
    	    }
    	  }
    	  
    	  public void changeStatus(String newStatus)
    	  {
              statusField.setText("Status : "+newStatus);

    	  }
    	  
    	  private double normalize(double angle) {
    		  /*int offset=131+90; //change it with the room

    		  Log.v(TAG,"original angle="+angle);
    		  
    		  if(angle < -120 && angle >-180)
    		  {
    			  angle = angle+360;
    		  }
    		  angle=angle-offset; //Adjust to the room

    		  Log.v(TAG,"angle="+angle);
    		  angle=Math.abs(angle); //Remove negative values
    		  if (angle>180 && angle < 270)
    			angle=180; //Remove angles greater than 180 and show 180�
    		  if (angle>=270)
    				angle=0; //Remove angles greater than 180 and show 0�
    		  else if (angle<90)
    			angle=angle*1.2; //Adjust compression

    		  if(angle > 90 && angle < 180)
    			  angle = angle *1.3;
    			  */
    		  //if angle=angleOffset return 90 ;
    		  
    		  return (angle+180);
    	  }

    	  
    	  public void openHttpConn(String url) {
    	    Log.d(TAG, "Calling "+url);
    	    HttpClient httpClient = new DefaultHttpClient();  

    	    HttpGet httpGet = new HttpGet(url);
    	    try {
    	        HttpResponse response = httpClient.execute(httpGet);
    	        StatusLine statusLine = response.getStatusLine();
    	        if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
    	            HttpEntity entity = response.getEntity();
    	            ByteArrayOutputStream out = new ByteArrayOutputStream();
    	            entity.writeTo(out);
    	            out.close();
    	            String responseStr = out.toString();
    	            // do something with response 
    	        } else {
    	            // handle bad response
    	        }
    	    } catch (ClientProtocolException e) {
    	        // handle exception
    	    } catch (IOException e) {
    	        // handle exception
    	    }
    	  }
    	  

    	  
    	}
}
