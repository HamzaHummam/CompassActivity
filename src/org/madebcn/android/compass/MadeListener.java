package org.madebcn.android.compass;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.madebcn.android.compass.CompassActivity.SimulationView;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class MadeListener implements SensorEventListener {
  
  private static final String TAG = "MadeListener";

  //Settings
  public final int INTERVAL = (int) (0.5* 1000);
  public final String SERVER_IP_ADDRESS = "192.168.1.46";
  
  
  public long lastSentInfoTime;
  private int lastSentAngle ;
  
 
  float[] inR = new float[16];
  float[] I = new float[16];
  float[] gravity = new float[3];
  float[] geomag = new float[3];
  float[] orientVals = new float[3];
  
  double absoluteAzimuth;
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
  }
  
  private String generateServerPath(double angle)
  {
    return "http://"+SERVER_IP_ADDRESS+":8080/"+(int)angle ;
  }
  
  @Override
  public void onAccuracyChanged(Sensor arg0, int arg1) {
    // TODO Auto-generated method stub
  }
 

  @Override
  public void onSensorChanged(SensorEvent sensorEvent) {

    // If the sensor data is unreliable return
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

                view.updateUrl(url);
                openHttpConn(url);
              }
            }
        }
    }
  }
  
  private double normalize(double angle) {
	  angle=angle-130; //Adjust to the room
	  angle=Math.abs(angle); //Remove negative values
    if (angle>180)
    	angle=180; //Remove angles greater than 180
    else if (angle<90)
    	angle=angle*1.15; //Adjust compression
    return angle;
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


