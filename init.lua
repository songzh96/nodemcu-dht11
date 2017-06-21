-- -- 定义全局变量
DHT11_PIN =4 --dht11引脚 
SSID = "Awwz-PC" --wifi名称
PWD = "1234567890" --wifi密码
broker = "120.25.161.167" --代理ip地址
port = 1883 --端口号
LED_R = 1 --红灯灯脚
LED_G = 2 --绿灯灯脚
LED_B = 3 --蓝灯灯脚
-- -- 初始化
gpio.mode(LED_R,gpio.OUTPUT)
gpio.mode(LED_G,gpio.OUTPUT)
gpio.mode(LED_B,gpio.OUTPUT)
gpio.write(LED_R,gpio.LOW)
gpio.write(LED_B,gpio.LOW)
gpio.write(LED_G,gpio.LOW)

-- 获取dht11的数据
function getDht11(pin)
  status, temp, humi, temp_dec, humi_dec = dht.read11(pin)

  if status == dht.OK then
      temp_str = tostring(temp)
      hum_str = tostring(humi)
      return temp_str..hum_str

  elseif status == dht.ERROR_CHECKSUM then
      print( "DHT Checksum error." )
  elseif status == dht.ERROR_TIMEOUT then
      print( "DHT timed out." )
  end
end

-- 将表转为json
function encode(data)
  data_json = sjson.encode(data)
  return data_json
end

-- 链接wifi
function connectWifi(SSID,PWD)
  -- 设置wifi模式
  wifi.setmode(wifi.STATION)

  -- 设置主机名
  wifi.sta.sethostname("Node-MCU")

  --设置接入路由的信息 (不将此保存至flash)
  station_cfg={}
  station_cfg.ssid=SSID
  station_cfg.pwd=PWD
  wifi.sta.config(station_cfg)

  --注册wifi事件监听器
  wifi.eventmon.register(wifi.eventmon.STA_CONNECTED, function(T)
  print("\n\tSTA - CONNECTED".."\n\tSSID: "..T.SSID)
  end)
  wifi.eventmon.register(wifi.eventmon.STA_GOT_IP, function(T)
  print("\n\tSTA - GOT IP".."\n\tStation IP: "..T.IP.."\n\tSubnet mask: "..
  T.netmask.."\n\tGateway IP: "..T.gateway)
  end)

end
-- 开关灯组
function switchLed(message)

    if(message == "Greenon") then
        gpio.write(LED_G,gpio.HIGH)
    end
    if(message == "Blueon") then
        gpio.write(LED_B,gpio.HIGH)
    end
    if(message == "Redon") then
        gpio.write(LED_R,gpio.HIGH)
    end
    if(message == "Greenoff") then
        gpio.write(LED_G,gpio.LOW)
    end
    if(message == "Redoff") then
        gpio.write(LED_R,gpio.LOW)
    end
    if(message == "Blueoff") then
        gpio.write(LED_B,gpio.LOW)
    end
end




-- -- 链接WiFi
connectWifi(SSID,PWD)

-- -- 主函数
function main()
    data = getDht11(DHT11_PIN)
    print(data)
    m = mqtt.Client("nodeMCU", 120)
    m:connect(broker,port, 
        function(client)
            print("connected")
            -- 发布数据
            m:publish("ywy/dht11", data, 0, 0)
            m:subscribe("feeds/Led", 0, function(client) print("subscribe success") end)
        end, 
        function(client, reason)
            print("fail reason: " .. reason)
        end
    )
    m:on("message", function(client, topic, data)
        if data ~= nil then
            switchLed(data)
            print(data)
        end
    end)
    -- m:subscribe("test", 0, function(client) print("subscribe success") end)
    m:close()
end

tmr.alarm(0, 2000, tmr.ALARM_AUTO, main)

