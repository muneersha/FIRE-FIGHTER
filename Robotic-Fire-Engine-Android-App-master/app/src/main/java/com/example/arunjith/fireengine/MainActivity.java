package com.example.arunjith.fireengine;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import com.github.nkzawa.emitter.Emitter;

import io.github.controlwear.virtual.joystick.android.JoystickView;


public class MainActivity extends AppCompatActivity {

    Button rightBtn, leftBtn;
    long lastDown;
    long lastDuration;
    WebView webView;
    TextView tempHumid;
    TextView waterLevel;
    Button motorPumbBtn;

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://192.168.43.111:3000");
        } catch (URISyntaxException e) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        mSocket.connect();
        mSocket.on("message", handleIncomingMessage);
        mSocket.on("sensors", handleSensorIncomingMessage);
        mSocket.on("water", handleWaterLevelIncomingMessage);
        mSocket.on("motorPump", handleMotorMessage);



        webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl("http://192.168.43.111/html/cam_pic_new.php?time=1554025846105&pDelay=40000");

        tempHumid = (TextView) findViewById(R.id.temp_humid);
        waterLevel = (TextView) findViewById(R.id.water_level);
        motorPumbBtn = (Button) findViewById(R.id.pump_button);

        JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                // do whatever you want

                if(angle >= 225 && angle < 315 ) {
                    Log.i("angleXX", "DOWN");
                    sendData("DOWN");
                } else if(angle >= 315 || angle < 45 ) {
                    Log.i("angleXX", "RIGHT");
                    sendData("RIGHT");
                } else if(angle >= 45 && angle < 135 ) {
                    Log.i("angleXX", "UP");
                    sendData("UP");
                } else if(angle >= 135 && angle < 225 ) {
                    Log.i("angleXX", "LEFT");
                    sendData("LEFT");
                }

                if(angle == 0) {
                    for(int i=0; i<20; i++) {
                        sendData("STOP");
                    }

                }
            }
        });

        motorPumbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Log.i("asdasdasdasd", "ookkkk");
                JSONObject postData = new JSONObject();
                try {
                    postData.put("state", true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSocket.emit("motorPump", postData);
            }
        });

    }


    // sensors
    private Emitter.Listener handleSensorIncomingMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable(){

                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String temp, humidity;
                    try {
                        temp = data.getString("temp");
                        humidity = data.getString("humidity");
                        tempHumid.setText("Temperature: " + temp + "°C   Humidity: " + humidity + "%");
                    } catch (JSONException e){
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener handleIncomingMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable(){

                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String message;
                    try {
                        message = data.getString("message");
                    } catch (JSONException e){
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener handleWaterLevelIncomingMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable(){

                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String message;
                    try {
                        message = data.getString("water");
                        waterLevel.setText("Water Level: " + message + "%");
                    } catch (JSONException e){
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener handleMotorMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable(){

                @Override
                public void run() {

                }
            });
        }
    };

    public void sendData(String message) {
        JSONObject postData = new JSONObject();
        try {
            postData.put("message", message);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("message", postData);
    }

}
