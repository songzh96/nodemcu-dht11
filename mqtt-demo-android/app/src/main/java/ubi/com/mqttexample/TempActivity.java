package ubi.com.mqttexample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Timer;
import java.util.TimerTask;

public class TempActivity extends AppCompatActivity implements Button.OnClickListener{

    private TextView mTextMessage;

    String host;
    String tempurature;

    private final static int CONNECTED=1;
    private final static int LOST=2;
    private final static int FAIL=3;
    private final static int RECEIVE=4;

    private EditText subTopic;
    private TextView subMsg,a;
    private Button subButton,nowButton,pastButton;
    private MqttAsyncClient mqttClient;


    private ImageView needleView;  //指针图片
    private Timer timer;  //时间
    private float degree = 0.0f,b;  //记录指针旋转


  private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    Intent intent6to7=new Intent(TempActivity.this,HumActivity.class);
                    intent6to7.putExtra("HOST",host);
                    startActivity(intent6to7);
                    //mTextMessage.setText(R.string.title_dashboard);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tempterature);
        //subTopic=(EditText)findViewById(R.id.subTopic);
        subMsg=(EditText)findViewById(R.id.submessage);
        //subButton=(Button)findViewById(R.id.subButton);
        nowButton=(Button)findViewById(R.id.nowButton);
        pastButton=(Button)findViewById(R.id.pastButton);
       //subButton.setOnClickListener(this);
        nowButton.setOnClickListener(this);
        pastButton.setOnClickListener(this);
        connectBroker();
        needleView = (ImageView) findViewById(R.id.needle);


        // 开始转动
        timer = new Timer();
        // 设置每一秒转动一下
        timer.schedule(new TempActivity.NeedleTask(), 50, 1000);
        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    public void onClick(View v) {
      /*  if(v==subButton){
            try {
                mqttClient.subscribe(subTopic.getText().toString(),2);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }else*/

        if(v==nowButton){
            Intent intent6to2=new Intent(TempActivity.this,ControlActivity.class);
            intent6to2.putExtra("HOST",host);
            startActivity(intent6to2);
            subMsg.setText("");

        }
        else if(v==pastButton){
            Intent intent3to5=new Intent(TempActivity.this,HistoryActivity.class);
            intent3to5.putExtra("Tem",tempurature);
            startActivity(intent3to5);
        }
    }


    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg){

            if(msg.what==CONNECTED){
              //  Toast.makeText(Main6Activity.this,"nice",Toast.LENGTH_SHORT).show();
            }else if(msg.what==LOST){
               // Toast.makeText(Main6Activity.this,"连接丢失，进行重连",Toast.LENGTH_SHORT).show();
            }else if(msg.what==FAIL){
               // Toast.makeText(Main6Activity.this,"连接失败",Toast.LENGTH_SHORT).show();
            }else if(msg.what==RECEIVE){
                String message=(String) msg.obj;
                tempurature = message.substring(0,2);
                subMsg.setText(tempurature);
                //subMsg.append(tempurature);
                b=Float.parseFloat(tempurature);
                degree=b*36/10;


            }  if (degree >= 180) {
                //timer.cancel();
                degree=180;
                RotateAnimation animation = new RotateAnimation(degree, degree, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setDuration(1000);
                animation.setFillAfter(true);
                needleView.startAnimation(animation);

            }else {
                RotateAnimation animation = new RotateAnimation(degree, degree, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setDuration(1000);
                animation.setFillAfter(true);
                needleView.startAnimation(animation);}
            super.handleMessage(msg);
        }
    };

    private IMqttActionListener mqttActionListener=new IMqttActionListener() {
        @Override

        public void onSuccess(IMqttToken asyncActionToken) {
            //连接成功处理
            Message msg=new Message();
            msg.what=CONNECTED;
            handler.sendMessage(msg);
            try {
                mqttClient.subscribe("ywy/dht11",2);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            exception.printStackTrace();
            //连接失败处理
            Message msg=new Message();
            msg.what=FAIL;
            handler.sendMessage(msg);
        }
    };

    private MqttCallback callback=new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            //连接断开
            Message msg=new Message();
            msg.what=LOST;
            handler.sendMessage(msg);

        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            //消息到达
//            subMsg.append(new String(message.getPayload())+"\n"); //不能直接修改,需要在UI线程中操作
            Message msg=new Message();
            msg.what=RECEIVE;
            msg.obj=new String(message.getPayload())+"\n";
            handler.sendMessage(msg);

        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            //消息发送完成

        }

    };


    private void connectBroker(){
        Intent intent3=getIntent();
        String hos1=intent3.getStringExtra("HOST");
        String port1=intent3.getStringExtra("PORT");
        host=hos1;
        try {
            mqttClient=new MqttAsyncClient("tcp://"+host,"ClientID"+Math.random(),new MemoryPersistence());
//            mqttClient.connect(getOptions());
            mqttClient.connect(getOptions(),null,mqttActionListener);
            mqttClient.setCallback(callback);
            //mqttClient.subscribe("1",2);
        }

        catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private MqttConnectOptions getOptions(){

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);//重连不保持状态
        options.setConnectionTimeout(10);//设置连接超时时间
        options.setKeepAliveInterval(30);//设置保持活动时间，超过时间没有消息收发将会触发ping消息确认
        return options;

    }


    private class NeedleTask extends TimerTask {
        @Override
        public void run() {
            handler.sendEmptyMessage(1);
        }
    }




}
