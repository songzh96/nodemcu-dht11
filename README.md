### 简介
本项目主要功能是读取温度和湿度然后将数值显示在客户端，并将此进行存储。<br>
通过客户端控制nodemcu的RGB模块，读取数据库中的数据并将此以图表形式显示出来<br>
本项目包含了三个模块，分别是硬件模块，Web模块，Android模块<br>
代码冗余率比较高，希望参考的同学能重构一下。<br>
代码仅供参考，希望大家能根据自己的想法自己制作这样的一个小系统<br>
认真阅读下面参考资料，你会发现其实并不难，<br>
**本项目的难点** *如何实时显示图表数据，Android端的历史数据实现。*<br>
**个人建议** *系统很简单，但是要学的不少。基础好的同学可直接看API。
Android端的同学要记得使用镜像地址,
Web端的同学还要了解数据库的使用，
硬件端的同学要注意定制模块的时候邮箱要留Gmail，反正qq邮箱不能用，
一般3分钟左右你定制的模块会发送过来，下面有资料参考。*<br>
注：NodeMcu也称ESP8266。在测试时，所有设备最好在同一局域网下。
### 需要掌握的语言
* Java（Android）
* NodeJs（Web）
* HTML（Web）
* CSS（Web）
* XML（Android）
* Lua（NodeMcu）
### 需要了解的基本知识
* NodeMcu的基础知识，引脚功能，模块功能
* Mqtt服务器机制
* 如何利用NodeJs搭建Web服务器和Mqtt服务器
* Web前端的框架使用
* Android基础知识，尤其是Activity生命周期
### 需要安装的软件
* AndroidStudio（这个比较折腾人，大家自己琢磨）[Android Studio 中文社区](http://www.android-studio.org/)
* NodeJs [NodeJs官网下载页面](https://nodejs.org/en/download/)
* ESP8266Flasher(这个是烧写固件的) [下载地址](https://github.com/nodemcu/nodemcu-flasher)
* Esplorer(为硬件烧写你写的代码) [下载地址](https://esp8266.ru/esplorer/)
* VSCode（比较好用的编辑器，用于快速编写代码）[下载地址](https://code.visualstudio.com/)
### 参考资料
* [NodeMcu官方Api](http://nodemcu.readthedocs.io/en/dev/) 
* [NodeMcu各模块简单介绍](http://www.jianshu.com/nb/7000517)
* [Lua基本语法](http://www.runoob.com/lua/lua-basic-syntax.html)
* [NodeJs基本语法（选看）](http://www.runoob.com/nodejs/nodejs-tutorial.html)
* [NodeJs搭建Mqtt服务器（21-24）](http://www.maiziedu.com/course/803/)
* [如何为NodeMcu烧写固件](http://www.cnblogs.com/wangzexi/p/5696925.html)
* [Android基础知识（有点过于基础，可快进）](https://cn.udacity.com/course/android-development-for-beginners--ud837)
* [Web端图表Echart](http://echarts.baidu.com/tutorial.html)
* [Androdi端图表HelloChart](http://blog.csdn.net/androidtalent/article/details/52290051)
* [Mqtt服务器简单介绍](http://blog.csdn.net/jiesa/article/details/50635222)
* [Socket.io web前后台实时传值 聊天室搭建](https://socket.io/docs/)


