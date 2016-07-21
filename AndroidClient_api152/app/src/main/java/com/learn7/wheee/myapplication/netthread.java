package com.learn7.wheee.myapplication;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

public class netthread extends Thread {
    private String threadInfo;
    private String urlstring;
    static Message msg = new Message();

    private Handler ntthrd_handler;//未指定activity的handler

    static String SUCCEED_FLAG_WRT = "WOK";//操作成功后服务器的返回值 write
    static String SUCCEED_FLAG_RD = "ROK";// read
//    static boolean issucceed = false;


    netthread(String url,Handler h) {
        this.urlstring = url;
        this.ntthrd_handler = h;//指定activity
    }

    @Override
    public void run() {
        Looper.prepare();
        super.run();

        threadInfo = "";
        msg.obj = null;
        msg.what = 0;//初始化 //0-ERROR 表示android端与php传输数据流是出错 而不是mysql与php之间的错误
        try {

            //GET方式发送数据
            URL url = new URL(urlstring);
            //接收服务器反馈
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStreamReader isr = new InputStreamReader(urlConnection.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            String strbr="",Allstrbr="";//中间变量

            while ((strbr = br.readLine()) != null)
                Allstrbr += strbr + "\n";
            threadInfo = Allstrbr;//赋值，去除threadInfo的初始值

            isr.close();//流 关闭
            urlConnection.disconnect();//关闭http连接
            msg.what = 1;//可能出错的语句 全部执行完毕
        } catch (UnknownHostException e) {
            e.printStackTrace();
            threadInfo += e.toString() + "AND";
            msg.what = 0;//ERROR
        } catch (IOException e) {
            e.printStackTrace();
            threadInfo += e.toString() + "AND";
            msg.what = 0;//ERROR 与php传输数据流是出错 而不是mysql与php之间的错误
        } finally {
            //简单处理 去掉首位空格换行
            threadInfo = trimInnerSpaceStr(threadInfo);
            Log.i("我是线程：netthread", "android-php 网络传输过程的信息threadInfo=" + threadInfo);
            msg.obj = new String(threadInfo);
            //临时 Message,防止主线程同时访问 msg导致出错
            Message msg_copy =new Message();
            msg_copy.obj = new String(msg.obj.toString());//新的类，不是引用
            msg_copy.what = msg.what;
            //feedback.mhandler.sendMessage(msg_copy);//发送消息 msg以引用形式被发送至 主线程
            this.ntthrd_handler.sendMessage(msg_copy);
            Looper.loop();
        }
    }

    //处理串 去掉String 首位所有 换行 空格
    static String trimInnerSpaceStr(String str) {
        str = str.trim();
        while (str.startsWith(" ") || str.startsWith("\n")) {
            str = str.substring(1, str.length()).trim();
        }
        while (str.endsWith(" ") || str.endsWith("\n")) {
            str = str.substring(0, str.length() - 1).trim();
        }
        return str;
    }
}