// 代码功能:代码主要的功能是服务器脚本，另外为了编码方便增加了数据库脚本和mqtt客户端脚本
// 引入express框架
var express = require('express');
var moment = require('moment');
// 引入socket文件
var app = require('express')();
var server = require('http').Server(app);
var io = require('socket.io')(server);
var bodyParser = require('body-parser')

app.use(bodyParser.urlencoded({ extended: false }))
// ************************初始化数据库***************************
var mysql      = require('mysql');
var connection = mysql.createConnection({
  host     : 'localhost',
  user     : 'root',
  password : '',
  database : 'nodemcu',
 
});

//  链接数据库
connection.connect();
console.log('mysql connected');

// ***********************服务器监听端口*********************
server.listen(8888);

//实例一个时间对象；
var oDate = new Date(); 

// 调用静态文件
app.use(express.static('public'));

// url解析
app.get('/', function (req, res) {
   res.sendFile( __dirname + "/" + "client.html" );
});
//接收Android发送的信息
app.post('/getdata',function(req,res){
	// 根据Android发送来的sql语句进行查询数据
	console.log(req.body.data);
	var sql = req.body.data;
	connection.query(sql,function (err, rows, fields) {
          if(err){
            console.log('[SELECT ERROR] - ',err.message);
            return;
          }
          var time = new Array();
          var temp = new Array();
          var hum = new Array();          
          for(var i = rows.length-1; i > -1; i--)
          {	

              time[i] = moment(rows[i].time).format('MMMM Do YYYY, h:mm:ss').substring(18,24);
              temp[i] = rows[i].temp;
              hum[i] = rows[i].hum;
          }
          // 定义一个对象进行发送
  		  
  		  var dataObj = {"time":time,"temp":temp,"hum":hum};
  		  res.send(dataObj);

	});
});

// *****************利用mqtt接收温湿度数据  mqtt脚本*************
var mqtt = require('mqtt');
var client = mqtt.connect('mqtt://120.25.161.167');
var topic = 'test';

client.on('connect',function(){
	console.log('Client connect to mqtt server succeed');
	client.subscribe('ywy/dht11');
});

// 处理订阅数据
client.on('message',(topic, message) =>{
	// console.log(topic,message.toString());
  // 接收数据并处理
  data = message.toString();
  temp = data.substring(0,2);
  hum = data.substring(2,4);
  temp = parseInt(temp);
  hum = parseInt(hum);
  var time = new Date().toLocaleString(); 
  // sql语句 添加
  var  addSql = 'INSERT INTO dht11(time,temp,hum) VALUES(?,?,?)';
  var  addSqlParams = [time, temp, hum];
  connection.query(addSql,addSqlParams,function (err, result) {
          if(err){
          console.log('[INSERT ERROR] - ',err.message);
          return;
          }        
        console.log('INSERT SUCCESSED!');                   
  });
});

// 重新连接事件
client.on('reconnect',() => {
	console.log('event:reconnect');
});

// 连接关闭事件
client.on('close',() => {
	console.log('event:close');
});

// 错误信息事件
client.on('error',(err) => {
	console.log('event:error',err);
});

// 断线事件
client.on('offline',() => {
	console.log('event:offline');
});

// 实现实时查询数据
io.on('connection', function (socket) {
  
  var selectSql = 'SELECT `time`, `temp`, `hum` FROM `dht11` ORDER BY id DESC LIMIT 10';
  // 执行sql语句
  connection.query(selectSql,function (err, rows, fields) {
          if(err){
            console.log('[SELECT ERROR] - ',err.message);
            return;
          }
          var time = new Array();
          var temp = new Array();
          var hum = new Array();          
          for(var i = rows.length-1; i > -1; i--)
          {
              time[i] = moment(rows[i].time).format('MMMM Do YYYY, h:mm:ss').substring(18,24);
              temp[i] = rows[i].temp;
              hum[i] = rows[i].hum;
          }
        
          socket.emit('chart_data', { time: time,temp:temp,hum:hum });
                  socket.on('res', function (data) {
                  console.log(data);
                  });
    });
});

