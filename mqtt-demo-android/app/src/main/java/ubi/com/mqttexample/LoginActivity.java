package ubi.com.mqttexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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



public class LoginActivity extends Activity  implements Button.OnClickListener{
    String host;
    String port;
    private final static int CONNECTED=1;
    private final static int LOST=2;
    private final static int FAIL=3;
    private final static int RECEIVE=4;
    private EditText editText,editText2;
    private Button loginButton;
    private MqttAsyncClient mqttClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        loginButton=(Button)findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);

}

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what==CONNECTED){
                Toast.makeText(LoginActivity.this,"登陆成功",Toast.LENGTH_SHORT).show();
            }else if(msg.what==LOST){
                Toast.makeText(LoginActivity.this,"连接丢失，进行重连",Toast.LENGTH_SHORT).show();
            }else if(msg.what==FAIL){
                Toast.makeText(LoginActivity.this,"连接失败",Toast.LENGTH_SHORT).show();
            }else if(msg.what==RECEIVE){
                //subMsg.append((String)msg.obj);
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
            mqttClient=new MqttAsyncClient("tcp://"+host,port,new MemoryPersistence());
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
     if(v==loginButton){
            editText=(EditText)findViewById(R.id.editText1);
            host=editText.getText().toString();
            editText2=(EditText)findViewById(R.id.editText2);
            port=editText2.getText().toString();
            Intent intent1to6=new Intent(LoginActivity.this,TempActivity.class);
            intent1to6.putExtra("HOST",host);
            intent1to6.putExtra("PORT",port);
            startActivity(intent1to6);
            connectBroker();
        }
    }
}
