package ubi.com.mqttexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class ControlActivity extends Activity  implements Button.OnClickListener{

    String host;
    private final static int CONNECTED=1;
    private final static int LOST=2;
    private final static int FAIL=3;
    private final static int RECEIVE=4;

    private EditText pubTopic,pubMsg;
    private Button nextButton,redButton,blueButton,greenButton,redoffButton,blueoffButton,greenoffButton;
    private MqttAsyncClient mqttClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control);
       // pubTopic=(EditText)findViewById(R.id.pubTopic);
       // pubMsg=(EditText)findViewById(R.id.pubMessage);
        redButton=(Button) findViewById(R.id.redButton);
        blueButton=(Button)findViewById(R.id.blueButton);
        greenButton=(Button)findViewById(R.id.greenButton);
        redoffButton=(Button)findViewById(R.id.redoffButton);
        blueoffButton=(Button)findViewById(R.id.blueoffButton);
        greenoffButton=(Button)findViewById(R.id.greenoffButton);
        //pubButton=(Button)findViewById(R.id.pubButton);
        nextButton=(Button)findViewById(R.id.nextButton);
        //pubButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        redButton.setOnClickListener(this);
        blueButton.setOnClickListener(this);
        greenButton.setOnClickListener(this);
        redoffButton.setOnClickListener(this);
        blueoffButton.setOnClickListener(this);
        greenoffButton.setOnClickListener(this);
        connectBroker();
    }

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what==CONNECTED){
                //Toast.makeText(Main2Activity.this,"连接成功",Toast.LENGTH_SHORT).show();
            }else if(msg.what==LOST){
                //Toast.makeText(Main2Activity.this,"连接丢失，进行重连",Toast.LENGTH_SHORT).show();
            }else if(msg.what==FAIL){
                //Toast.makeText(Main2Activity.this,"连接失败",Toast.LENGTH_SHORT).show();
            }else if(msg.what==RECEIVE){

            }
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
        Intent  intent1to2=getIntent();
        String hos1=intent1to2.getStringExtra("HOST");
        host=hos1;
        try {
            mqttClient=new MqttAsyncClient("tcp://"+host,"ClientID"+Math.random(),new MemoryPersistence());
//            mqttClient.connect(getOptions());
            mqttClient.connect(getOptions(),null,mqttActionListener);
            mqttClient.setCallback(callback);
        } catch (MqttException e) {
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

    @Override
    public void onClick(View v) {
        if(v==redButton){
            try {
                mqttClient.publish("feeds/Led","Redon".getBytes(),0,false);
                System.out.println("RedOn");
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        if(v==greenButton){
            try {
                mqttClient.publish("feeds/Led","Greenon".getBytes(),0,false);
                System.out.println("GreenOn");
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        if(v==blueButton){
            try {
                mqttClient.publish("feeds/Led","Blueon".getBytes(),0,false);
                System.out.println("BlueOn");
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
         if(v==redoffButton){
             try {
                 mqttClient.publish("feeds/Led","Redoff".getBytes(),0,false);
                 System.out.println("Redoff");
             } catch (MqttException e) {
                 e.printStackTrace();
             }
         }
         if(v==greenoffButton) {
             try {
                 mqttClient.publish("feeds/Led", "Greenoff".getBytes(), 0, false);
                 System.out.println("Greenoff");
             } catch (MqttException e) {
                 e.printStackTrace();
             }
         }
          if(v==blueoffButton) {
              try {
                  mqttClient.publish("feeds/Led", "Blueoff".getBytes(), 0, false);
                  System.out.println("Blueoff");
              } catch (MqttException e) {
                  e.printStackTrace();
              }
          }
        if(v==nextButton){
            Intent intent2to3=new Intent(ControlActivity.this,TempActivity.class);
            intent2to3.putExtra("HOST",host);
            startActivity(intent2to3);
        }
    }
}
