package ubi.com.mqttexample;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;

public class HistoryActivity extends ActionBarActivity {

    StringBuffer sb = new StringBuffer();
    private static JSONArray time;
    private static JSONArray temp;
    private static JSONArray hum;

    private Handler handler;


    Runnable runnable=new Runnable() {
        @Override
        public void run() {

            String sql = "SELECT `time`, `temp`, `hum` FROM `dht11` ORDER BY id DESC LIMIT 5";
            String url = "http://192.168.23.4:8888/getdata";//?data="+sql;
            //1.设置相关连接属性

            HttpURLConnection conn = null;   //打开http连接
            try {
                URL aUrl = new URL(url);  //新建url
                conn = (HttpURLConnection) aUrl.openConnection();
                conn.setRequestMethod("POST");  //设置发送方式为post
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);  //超时时间
                OutputStream os = conn.getOutputStream(); //获得发送数据的输出流对象
                String content; //存放需要发送的字符串信息

                //sql语句 查询得到数据
                content = "data="+sql;
                os.write(content.getBytes());   //将要发送的字符串转换为字节流并发送

                //3.创建读取响应信息的相关对象,并读取响应信息
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String str;
                //while (true) {
                sb.delete(0,sb.length());
                while ((str = reader.readLine()) != null)   //一行一行读取响应信息
                {
                    sb.append(str);
                }
                Message message = new Message();
                message.arg1 = 1;
                handler.sendMessage(message);
                //}
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        new Thread(runnable).start();

        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.arg1==1)
                {
                    System.out.println(sb);

                    //将传输过来的字符串转换成JSON格式
                    try {
                        JSONObject Dht11Data = new JSONObject(String.valueOf(sb));
                        //分割
                        time = Dht11Data.getJSONArray("time");
                        temp = Dht11Data.getJSONArray("temp");
                        hum = Dht11Data.getJSONArray("hum");
                        //打印测试
//                        System.out.println(time);
//                        System.out.println(temp);
//                        System.out.println(hum);
                        getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                //if (savedInstanceState == null) {

                // }
            }
        };
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_data_main);
    }

    /**
     * A fragment containing a line chart.
     */
    public static class PlaceholderFragment extends Fragment {

        private LineChartView chart_temp;
        private LineChartView chart_hum;
        private LineChartData data;

        private int numberOfLines = 1;
        private int maxNumberOfLines = 4;
        private int numberOfPoints = temp.length();

        float[][] randomNumbersTab = new float[maxNumberOfLines][numberOfPoints];

        private boolean hasAxes = true;
        private boolean hasAxesNames = true;
        private boolean hasLines = true;
        private boolean hasPoints = true;
        private ValueShape shape = ValueShape.CIRCLE;
        private boolean isFilled = false;
        private boolean hasLabels = false;
        private boolean isCubic = true;
        private boolean hasLabelForSelected = false;
        private boolean pointsHaveDifferentColor;
        private boolean hasGradientToTransparent = false;


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            View rootView = inflater.inflate(R.layout.history_data_fragment, container, false);

            //声明图表
            chart_temp = (LineChartView) rootView.findViewById(R.id.chart_temp);
            chart_temp.setOnValueTouchListener(new ValueTouchListener());
            generateValues(temp);
            try {
                generateData(chart_temp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Disable viewport recalculations, see toggleCubic() method for more info.
            chart_temp.setViewportCalculationEnabled(false);
            try {
                resetViewport(chart_temp,temp);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            chart_hum = (LineChartView) rootView.findViewById(R.id.chart_hum);
            chart_hum.setOnValueTouchListener(new ValueTouchListener());
            //为对应的表赋值
            generateValues(hum);
            // Generate some random values.
            try {

                generateData(chart_hum);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            chart_hum.setViewportCalculationEnabled(false);
            //设置图表样式
            try {
                resetViewport(chart_hum,hum);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return rootView;
        }


        //设置Y轴的数值
        private void generateValues(JSONArray data) {
            for (int j = 0; j < numberOfPoints; ++j) {
                try {
                    randomNumbersTab[0][j] = (float) data.getDouble(j);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        private void resetViewport(LineChartView chart_view,JSONArray json_array) throws JSONException {
            // Reset viewport height range to (0,100)
            final Viewport v = new Viewport(chart_view.getMaximumViewport());
            v.bottom = json_array.getInt(0)-5;
            v.top = json_array.getInt(0)+5;
            v.left = 0;
            v.right = numberOfPoints - 1;
            chart_view.setMaximumViewport(v);
            chart_view.setCurrentViewport(v);
        }

        private void generateData(LineChartView chart_view) throws JSONException {

            List<Line> lines = new ArrayList<Line>();
            for (int i = 0; i < numberOfLines; ++i) {

                List<PointValue> values = new ArrayList<PointValue>();
                for (int j = 0; j < numberOfPoints; ++j) {
                    values.add(new PointValue(j, randomNumbersTab[i][j]));
                }

                Line line = new Line(values);
                line.setColor(ChartUtils.COLORS[i]);
                line.setShape(shape);
                line.setCubic(isCubic);
                line.setFilled(isFilled);
                line.setHasLabels(hasLabels);
                line.setHasLabelsOnlyForSelected(hasLabelForSelected);
                line.setHasLines(hasLines);
                line.setHasPoints(hasPoints);
//                line.setHasGradientToTransparent(hasGradientToTransparent);
                if (pointsHaveDifferentColor){
                    line.setPointColor(ChartUtils.COLORS[(i + 1) % ChartUtils.COLORS.length]);
                }
                lines.add(line);
            }

            data = new LineChartData(lines);

            if (hasAxes) {
                Axis axisX = new Axis();
                Axis axisY = new Axis().setHasLines(true);


                if (hasAxesNames) {
                    axisX.setName("时间");
                    axisY.setName("数值");
                }
                data.setAxisXBottom(axisX);
                data.setAxisYLeft(axisY);
            } else {
                data.setAxisXBottom(null);
                data.setAxisYLeft(null);
            }
            data.setBaseValue(Float.NEGATIVE_INFINITY);
            chart_view.setLineChartData(data);
        }

        //        点击显示数值
        private class ValueTouchListener implements LineChartOnValueSelectListener {

            @Override
            public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
                Toast.makeText(getActivity(), "" + value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onValueDeselected() {
                // TODO Auto-generated method stub

            }
        }

    }

}
