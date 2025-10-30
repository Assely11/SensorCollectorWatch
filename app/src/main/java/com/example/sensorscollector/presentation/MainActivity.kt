/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.sensorscollector.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.setMargins
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.sensorscollector.R
import com.example.sensorscollector.presentation.theme.SensorsCollectorTheme
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    private lateinit var startButton: TextView;
//    private lateinit var stopButton: TextView;
    private lateinit var scrollView: ScrollView;
    private lateinit var textView: TextView;
    private lateinit var linearLayout: LinearLayout;
    private lateinit var editorView: EditText;

    private var button_margin=10;
    private var text_margin=5;

    private lateinit var sensorManager: SensorManager;
    private var accData= FloatArray(4);
    private var gyrData= FloatArray(4);
    private var magData= FloatArray(4);
    private var pressData= FloatArray(4);

    private lateinit var handler: Handler;

    private lateinit var file: File;
    private var isRecord=false;

    private var curret_Time= System.currentTimeMillis().toLong();



//    private lateinit var layoutView: ListView;

    // private lateinit var linerView: View
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

//        setContent {
//            WearApp("Android")
//        }

        initView();
        initService();
        initHandler();
    }

    fun initView(){
        linearLayout= LinearLayout(this);
        scrollView= ScrollView(this);
        textView= TextView(this);

        startButton= TextView(this);
//        stopButton= TextView(this);
        editorView= EditText(this);

        startButton.gravity= Gravity.CENTER;
//        stopButton.gravity= Gravity.CENTER;
        startButton.setTextColor(this.resources.getColor(R.color.text_color));
//        stopButton.setTextColor(this.resources.getColor(R.color.text_color));
        startButton.setBackgroundDrawable(this.resources.getDrawable(R.drawable.button_temp));
//        stopButton.setBackgroundDrawable(this.resources.getDrawable(R.drawable.button_temp));


        linearLayout.orientation= LinearLayout.VERTICAL;

        startButton.setText(this.resources.getString(R.string.start_button));
        editorView.setHint("fileName");
//        stopButton.setText(this.resources.getString(R.string.stop_button));

        startButton.setOnClickListener { onClickStartRecord() };
//        stopButton.setOnClickListener { onClickStopRecord() };

        // textView.setText("111");
        scrollView.addView(textView);

//        var scrollLP= ViewGroup.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.MATCH_PARENT);
//        var buttonLP= ViewGroup.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        );
        var scrollLP= LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT);
        var buttonLP= LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );

        buttonLP.setMargins(button_margin);
        scrollLP.setMargins(text_margin);

        linearLayout.addView(editorView,buttonLP);
        linearLayout.addView(startButton,buttonLP);
//        linearLayout.addView(stopButton,buttonLP);
        linearLayout.addView(scrollView,scrollLP);

        this.addContentView(linearLayout,scrollLP);


        this.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @SuppressLint("DefaultLocale")
    fun onClickStartRecord(){
        if(!isRecord){
            var str=editorView.text;
            var fileName:String="";
            if(str.length>0){
                fileName= String.format("%s.log", str);
            }
            else{
                fileName= String.format("%d.log", System.currentTimeMillis());
            }

            file= File(filesDir,fileName);
            if(!file.exists()){
                file.createNewFile();
            }
            isRecord=true;
            Toast.makeText(this@MainActivity,"start record", Toast.LENGTH_SHORT).show();
            startButton.setText(this.resources.getString(R.string.stop_button));
        }else{
            Toast.makeText(this@MainActivity,"stop record", Toast.LENGTH_SHORT).show();
            isRecord=false;
            startButton.setText(this.resources.getString(R.string.start_button));
        }

    }
//    fun onClickStopRecord(){
//        Toast.makeText(this@MainActivity,"stop record", Toast.LENGTH_SHORT).show();
//        isRecord=false;
//    }


    fun initService(){
        sensorManager= this.getSystemService(Context.SENSOR_SERVICE) as SensorManager;

        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(
            Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(
            Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED), SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(sensorListener,sensorManager.getDefaultSensor(
            Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(sensorListener,sensorManager.getDefaultSensor(
            Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(sensorListener,sensorManager.getDefaultSensor(
            Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(sensorListener,sensorManager.getDefaultSensor(
            Sensor.TYPE_ALL), SensorManager.SENSOR_DELAY_GAME);
//        sensorManager.registerListener(sensorListener,sensorManager.getDefaultSensor(
//            Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_GAME);
    }

    private var sensorListener: SensorEventListener = object : SensorEventListener{

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // TODO("Not yet implemented")
        }

        @SuppressLint("DefaultLocale")
        override fun onSensorChanged(event: SensorEvent?) {
            // TODO("Not yet implemented")
            var values=event?.values;

//            var currentDate= LocalDate.now();
//            var formatter= DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss.SSS");
//            var formatted=currentDate.format(formatter);
//            var formatted=currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss.SSS"));
            var dataTime= System.currentTimeMillis().toLong();

            if(event?.sensor==sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)){
                accData[0]= System.currentTimeMillis().toFloat();
                var d1=values?.get(0);
                var d2=values?.get(1);
                var d3=values?.get(2);

                accData[1]= d1?.toFloat()!!;
                accData[2]= d2?.toFloat()!!;
                accData[3]= d3?.toFloat()!!;

                if(isRecord){
                    var str= String.format("ACC,%f,%f,%f,%f\n",
                        dataTime*1e-3,accData[1],accData[2],accData[3]);
                    // var fileOutputStream: FileOutputStream= FileOutputStream(file,true);
                    // fileOutputStream.writer(str.toByteArray(Charsets.US_ASCII));
                    file.appendText(str);
                }
            }else if(event?.sensor==sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)){
                gyrData[0]= System.currentTimeMillis().toFloat();
                var d1=values?.get(0);
                var d2=values?.get(1);
                var d3=values?.get(2);

                gyrData[1]= d1?.toFloat()!!;
                gyrData[2]= d2?.toFloat()!!;
                gyrData[3]= d3?.toFloat()!!;

                if(isRecord){
                    var str= String.format("GYR,%f,%f,%f,%f\n",
                        dataTime*1e-3,gyrData[1],gyrData[2],gyrData[3]);
                    // var fileOutputStream: FileOutputStream= FileOutputStream(file,true);
                    // fileOutputStream.writer(str.toByteArray(Charsets.US_ASCII));
                    file.appendText(str);
                }

//                Log.d("DEBUG", String.format("GYR,%f,%f,%f,%f\n",
//                        gyrData[0]*1e-3,gyrData[1],gyrData[2],gyrData[3]));
            }else if(event?.sensor==sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)){
                magData[0]= System.currentTimeMillis().toFloat();
                var d1=values?.get(0);
                var d2=values?.get(1);
                var d3=values?.get(2);

                magData[1]= d1?.toFloat()!!;
                magData[2]= d2?.toFloat()!!;
                magData[3]= d3?.toFloat()!!;


                if(isRecord){
                    var str= String.format("MAG,%f,%f,%f,%f\n",
                        dataTime*1e-3,magData[1],magData[2],magData[3]);
                    // var fileOutputStream: FileOutputStream= FileOutputStream(file,true);
                    // fileOutputStream.writer(str.toByteArray(Charsets.US_ASCII));
                    file.appendText(str);
                }
//                Log.d("DEBUG",String.format("MAG,%f,%f,%f,%f\n",
//                    magData[0]*1e-3,magData[1],magData[2],magData[3]));
            }else if(event?.sensor==sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)){
                pressData[0]= System.currentTimeMillis().toFloat();
                var d1=values?.get(0);
                pressData[1]= d1?.toFloat()!!;

                if(isRecord){
                    var str= String.format("PRESS,%f,%f\n",
                        dataTime*1e-3,pressData[1]);
                    // var fileOutputStream: FileOutputStream= FileOutputStream(file,true);
                    // fileOutputStream.writer(str.toByteArray(Charsets.US_ASCII));
                    file.appendText(str);
                }
            }
        }
    };

    fun initHandler(){
        handler= Handler();
        handler.post(runnable);
    }

    private var runnable: Runnable=object : Runnable{
        @SuppressLint("DefaultLocale", "SetTextI18n")
        override fun run() {
            //TODO("Not yet implemented")
            curret_Time= System.currentTimeMillis().toLong();
            if(isRecord){
                var str= String.format("[Time] %.3f\n [ACC] %.3f,%.3f,%.3f\n [GYR] %.3f,%.3f,%.3f\n [MAG] %.1f,%.1f,%.1f\n [PRES] %.3f",
                    curret_Time*1e-3,
                    accData[1],accData[2],accData[3],
                    gyrData[1],gyrData[2],gyrData[3],
                    magData[1],magData[2],magData[3],
                    pressData[1]);
                textView.text = str;
            }else{
                textView.text = String.format("[Time] %.3f\nNo Recording ....",curret_Time*1e-3);
            }


            handler.postDelayed(this,10);
        }

    };




}

@Composable
fun WearApp(greetingName: String) {
    SensorsCollectorTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            Greeting(greetingName = greetingName)
        }
    }
}

@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.hello_world, greetingName)
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}