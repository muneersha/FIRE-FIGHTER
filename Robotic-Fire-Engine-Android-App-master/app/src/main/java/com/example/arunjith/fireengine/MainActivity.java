package com.example.arunjith.fireengine;

import android.content.pm.ActivityInfo;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.eclipsesource.v8.V8;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import com.github.nkzawa.emitter.Emitter;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;


import io.github.controlwear.virtual.joystick.android.JoystickView;


public class MainActivity extends AppCompatActivity {

    Button rightBtn, leftBtn;
    long lastDown;
    long lastDuration;
    WebView webView;
    TextView tempHumid;
    TextView waterLevel;
    Button motorPumbBtn;

    ImageView servoLeft;
    ImageView servoRight;
    ImageView servoUp;
    ImageView servoDown;
    SeekBar seekBarBase;
    SeekBar seekBarLeft;
    SeekBar seekBarRight;
    SeekBar pumpSeed;

    Switch pumpSwitch;
    TextView percView;

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://192.168.43.111:3000");
        } catch (URISyntaxException e) {}
    }

    private Socket pSocket;
    {
        try {
            pSocket = IO.socket("http://192.168.1.7:81");
        } catch (URISyntaxException e) {}
    }

    public static void main(String[] args) {
        V8 runtime = V8.createV8Runtime();

        int result = runtime.executeIntegerScript("Socket = new WebSocket('ws://192.168.1.7:81/')");

        System.out.println(result);

        runtime.release();
    }
    WebSocket ws = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(5000);
        // Create a WebSocket. The timeout value set above is used.
        try {
            ws = factory.createSocket("ws://192.168.1.7:81/");

            ws.addListener(new WebSocketAdapter() {
                @Override
                public void onTextMessage(WebSocket websocket, String message) throws Exception {
                    Log.d("TAG", "onTextMessage: " + message);
                }
            });

            ws.connectAsynchronously();
        } catch (IOException e) {
            e.printStackTrace();
        }


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();


        mSocket.connect();
        mSocket.on("message", handleIncomingMessage);
        mSocket.on("sensors", handleSensorIncomingMessage);
        mSocket.on("water", handleWaterLevelIncomingMessage);

        pSocket.connect();

        webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl("http://192.168.1.6/html/cam_pic_new.php?time=1554025846105&pDelay=40000");

        tempHumid = (TextView) findViewById(R.id.temp_humid);
        waterLevel = (TextView) findViewById(R.id.water_level);

        seekBarBase = (SeekBar) findViewById(R.id.base_seek);
        seekBarLeft = (SeekBar) findViewById(R.id.left_seek);
        seekBarRight = (SeekBar) findViewById(R.id.right_seek);

        pumpSwitch = (Switch) findViewById(R.id.pump_switch);
        pumpSeed = (SeekBar) findViewById(R.id.pump_seed);
        percView = (TextView) findViewById(R.id.perc_view);


        seekBarBase.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //sendArmData(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                sendNodeMsg("base-", seekBar.getProgress());
            }
        });

        seekBarRight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //sendArmData(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                sendNodeMsg("right-", seekBar.getProgress());
            }
        });

        seekBarLeft.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //sendArmData(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                sendNodeMsg("left-", seekBar.getProgress());
            }
        });

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

        pumpSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    Log.i("pumpsseeek", String.valueOf(pumpSeed.getProgress()));
                    sendNodeMsg("pumpOn-", pumpSeed.getProgress());
                } else {
                    sendNodeMsg("pumpOff-",  pumpSeed.getProgress());
                }
            }
        });

        pumpSeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float perc = (i * 100) / 1023;
                percView.setText("Speed: " + perc + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendNodeMsg("speed-", seekBar.getProgress());
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

    public void sendData(String message) {
        JSONObject postData = new JSONObject();
        try {
            postData.put("message", message);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("message", postData);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (ws != null) {
            ws.disconnect();
            ws = null;
        }
    }

    public void sendNodeMsg(String s, int i) {
        if (ws.isOpen()) {
            ws.sendText(s + Integer.toString(i));
        }
    }

}
