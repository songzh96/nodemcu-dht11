package ubi.com.mqttexample;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class MainActivity extends Activity  implements Button.OnClickListener{

    private final static String host="10.66.15.36:1883";
    private final static String username="";
    private final static String password="";


    private final static int CONNECTED=1;
    private final static int LOST=2;
    private final static int FAIL=3;
    private final static int RECEIVE=4;

    private EditText pubTopic,pubMsg,subTopic;
    private TextView subMsg;
    private Button pubButton,subButton,clearButton;
    private MqttAsyncClient mqttClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pubTopic=(EditText)findViewById(R.id.pubTopic);
        pubMsg=(EditText)findViewById(R.id.pubMessage);
        subTopic=(EditText)findViewById(R.id.subTopic);
        subMsg=(TextView)findViewById(R.id.submessage);
        pubButton=(Button)findViewById(R.id.pubButton);
        subButton=(Button)findViewById(R.id.subButton);
        clearButton=(Button)findViewById(R.id.clearButton);
        pubButton.setOnClickListener(this);
        subButton.setOnClickListener(this);
        clearButton.setOnClickListener(this);
        connectBroker();
    }

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what==CONNECTED){
                Toast.makeText(MainActivity.this,"连接成功",Toast.LENGTH_SHORT).show();
            }else if(msg.what==LOST){
                Toast.makeText(MainActivity.this,"连接丢失，进行重连",Toast.LENGTH_SHORT).show();
            }else if(msg.what==FAIL){
                Toast.makeText(MainActivity.this,"连接失败",Toast.LENGTH_SHORT).show();
            }else if(msg.what==RECEIVE){
                subMsg.append((String)msg.obj);
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
        if(username!=null&&username.length()>0&&password!=null&&password.length()>0){
            options.setUserName(username);//设置服务器账号密码
            options.setPassword(password.toCharArray());
        }
        options.setConnectionTimeout(10);//设置连接超时时间
        options.setKeepAliveInterval(30);//设置保持活动时间，超过时间没有消息收发将会触发ping消息确认
        return options;
    }

    @Override
    public void onClick(View v) {
        if(v==pubButton){
            try {
                mqttClient.publish(pubTopic.getText().toString(),pubMsg.getText().toString().getBytes(),1,false);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }else if(v==subButton){
            try {
                mqttClient.subscribe(subTopic.getText().toString(),2);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }else if(v==clearButton){
            subMsg.setText("");
        }
    }
}
