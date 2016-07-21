package com.learn7.wheee.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Random;

public class MainActivity extends Activity {
    private EditText et_username;
    private EditText et_userpsw;
    private TextView tvlog;
    private Button bn_login;//登陆
    private Button bn_register;//注册

    //static String URLLOGIN_PHP = "http://192.168.1.109/mytest/login.php?a_lgninout=";
    static String URLLOGIN_PHP ="http://182.254.152.20/login.php?a_lgninout=";//外网

    static String URLLOGIN_PHP_IN = "IN";//登陆
    static String URLLOGIN_PHP_RGST = "RGST";//注册


    static String sttc_name;//正确的 name ；全局变量 整个acitvity共用一个 name 用户名
    private String usernm;//临时 name
    private String passwd;//临时 psw
    public Handler mainHandler;
    private String line;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_username = (EditText) this.findViewById(R.id.et_name);
        et_userpsw = (EditText) this.findViewById(R.id.et_psw);
        bn_login = (Button) findViewById(R.id.bn_login);//登陆
        bn_register = (Button) findViewById(R.id.bn_register);//注册
        tvlog = (TextView) findViewById(R.id.tv_log);//log信息

        bn_login.setOnClickListener(new MainOnClickLstnr());
        bn_register.setOnClickListener(new MainOnClickLstnr());

        mainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == 0){
                    //与 php 传输数据流时出错
                    tvlog.setText("[!]错误:android-php网络出错 请稍后重试\n耗时:"+msg.getWhen());
                    Toast.makeText(MainActivity.this,"网络出错 请稍后重试",Toast.LENGTH_LONG).show();
                    Log.i("mainHandler","[!]错误 android-php 网络出错 请稍后重试");
                }else {
                    //String strmsg = new String(msg.obj.toString().replaceAll("[^0-9a-zA-Z]+", ""));
                    String strmsg = new String(netthread.trimInnerSpaceStr(msg.obj.toString()));
                    Log.i("mainHandler", "去掉首尾空格的strmsg=" + strmsg);
                    //先分组 后解码 （login.php文件返回值为【flag&info】)
                    //1-2【分组】
                    String[] strFlagInfo = strmsg.split("&");
                    if (strFlagInfo.length != 2) {
                        Log.i("mainHandler", "strFlagInfo.length!=2 [!]错误 strmsg=" + strmsg);

                    }
                    //理论上只分成 2 组
                    //2-2【解码】并去掉解码后的 首尾空格回车
                    try {
                        strFlagInfo[0] = netthread.trimInnerSpaceStr(URLDecoder.decode(strFlagInfo[0].toString(), "utf8"));
                        strFlagInfo[1] = netthread.trimInnerSpaceStr(URLDecoder.decode(strFlagInfo[1].toString(), "utf8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    Log.i("mainHandler", "分组 解码后[OK_or_ERROR]?:strFlagInfo[0]=[" + strFlagInfo[0] + "] [login_ok_or_MaxId]?:strFlagInfo[1]=[" + strFlagInfo[1]+"]");
                    //判断flag是否为 OK
                    strFlagInfo[0] = new String(strFlagInfo[0].toString().replaceAll("[^0-9a-zA-Z]+", ""));
                    strFlagInfo[1] = new String(strFlagInfo[1].toString().replaceAll("[^0-9a-zA-Z]+", ""));
                    if ("OK".equals(strFlagInfo[0])) {
                        //操作成功  RGST//返回OK和最大ID,更新用户名控件值 :【'OK&'+$MaxId;】
                        //IN //	echo 'OK&login_ok';
                        Log.i("mainHandler", "【操作成功】等待判断是什么操作strFlagInfo[0]=" + strFlagInfo[0]+"strFlagInfo[1]="+strFlagInfo[1]);
                        if ("LOGINOK".equals(strFlagInfo[1])) {
                            //【登陆成功】
                            sttc_name = et_username.getText().toString();//
                            tvlog.setText("登陆成功\n耗时:"+msg.getWhen());
                            Log.i("mainHandler", "【登陆成功】 sttc_name=" + sttc_name + "&跳转到feedback.java");
                            //跳转到 feedback.java
                            Toast.makeText(MainActivity.this, "【登陆成功】", Toast.LENGTH_SHORT).show();
                            Intent ntnt_feedback = new Intent(MainActivity.this, feedback.class);
                            startActivity(ntnt_feedback);
                        } else {
                            //【注册成功】
                            sttc_name = new String(strFlagInfo[1]);//末尾加上唯一id
                            tvlog.setText("注册成功\n耗时:"+msg.getWhen());
                            Log.i("mainHandler", "【注册成功】 sttc_name=" + sttc_name + "&设置EditText_name_psw的值，等待用户点击登陆按钮");
                            et_username.setText(sttc_name);
                            et_userpsw.setText(passwd);
                        }
                    } else if ("ERROR".equals(strFlagInfo[0].toString())) {
                        //操作失败
                        tvlog.setText("[!]错误:php-mysql 连接数据库出错\n"+strmsg+"\n耗时:"+msg.getWhen());//输出错误信息
                        Log.i("mainHandler","[!]错误:php-mysql 连接数据库出错");

                    }else{
                        Log.i("mainHandler","[!]错误:strFlagInfo[0] FLAG判断不等于OK或ERROR\n[["+strFlagInfo[0]+"]][["+"OK"+"]]");
                    }
                }//if-else msg.what == 0?   -end

//                tvlog.setText("等待服务器回应:\n" + "用户名:" + sttc_name + "    密码:" + passwd + "\n反馈信息:[" + strmsg + "]" + strmsg.length());
//                if ("OK".equals(strmsg)) {
//                    Toast.makeText(MainActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
//                    Intent ntnt_feedback = new Intent(MainActivity.this, feedback.class);
//                    startActivity(ntnt_feedback);
//                } else if ("ERROR".equals(strmsg)) {
//                    tvlog.setText(tvlog.getText() + "else [" + "OK" + "OK".length() + "]");
//                }
            }
        };//mainHandler -end

//        bn_login.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                sttc_name = et_username.getText().toString();
//                passwd = et_userpsw.getText().toString();
//
//
//                Log.i("MainActivity_bn_OnClick", "开始登陆-启动线程 连接服务器【" + sttc_name + "】【" + passwd + "】");
//                //new netthread().start();
//                Intent ntnt_feedback = new Intent(MainActivity.this, feedback.class);
//                startActivity(ntnt_feedback);
//
//            }
//        });

    }//onCreate -end
    class MainOnClickLstnr implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.bn_login:
                    Log.i("MainActivityOnClick", "你点击了 登陆按钮");

                    usernm = et_username.getText().toString();
                    passwd = et_userpsw.getText().toString();
                    if (usernm == null || passwd == null) {
                        //用户名 或 密码 为空
                        EditText boo = (usernm == null) ? et_username : et_userpsw;
                        boo.setHint("请输入" + ((usernm == null) ? "用户名" : "密码"));
                        break;
                    }

                    Log.i("case R.id.bn_login:", "开始登陆-启动线程 连接服务器【" + usernm + "】【" + passwd + "】");
                    String url_login = "";
                    try {
                        //编码
                        url_login = URLLOGIN_PHP + URLLOGIN_PHP_IN + "&a_name=" + URLEncoder.encode(usernm, "utf8") + "&a_psw=" + URLEncoder.encode(passwd, "utf8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    //启动线程 验证登陆信息
                    netthread ntthrd_login = new netthread(url_login, mainHandler);
                    ntthrd_login.start();
                    break;
                case R.id.bn_register:
                    Log.i("MainActivityOnClick", "你点击了 注册按钮");

                    String name_RGST = netthread.trimInnerSpaceStr("" + android.os.Build.MODEL).toString().replace(" ","");//替换空格
                    String psw_RGST = "psw" + (new Random().nextInt(8999) + 1000);//产生 1000 - 9999 的随机数
                    //赋值给 未检测是否匹配的全局变量
                    usernm = name_RGST;
                    passwd = psw_RGST;
                    //编码
                    String url_rgst = "";
                    try {
                        url_rgst = URLLOGIN_PHP + URLLOGIN_PHP_RGST + "&a_name=" + URLEncoder.encode(name_RGST, "utf8") + "&a_psw=" + URLEncoder.encode(psw_RGST, "UTF8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    Log.i("case R.id.bn_register","即将提交的注册信息 url_rgst="+url_rgst+"【name_RGST】="+name_RGST+"【psw_RGST】="+psw_RGST);
                    //启动线程 发送注册信息
                    netthread ntthrd_rgst = new netthread(url_rgst, mainHandler);
                    ntthrd_rgst.start();

                    break;
                default:
                    break;

            }

        }
    }//MainOnClickLstnr -end
//    public class netthread extends Thread {
//        @Override
//        public void run() {
//            Looper.prepare();
//            line = "";
//            super.run();
//
//            try {
//
//                URL url = new URL("http://192.168.1.109/mytest/index.php?name=" + sttc_name + "&psw=" + passwd);
//                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//                InputStreamReader isr = new InputStreamReader(urlConnection.getInputStream());
//                BufferedReader br = new BufferedReader(isr);
//                String readbr;
//                while ((readbr = br.readLine()) != null)
//                    line += readbr + "\n";
//
//                isr.close();
//                urlConnection.disconnect();//关闭http连接
//
//            } catch (UnknownHostException e) {
//                e.printStackTrace();
//                line += e.toString();
//            } catch (IOException e) {
//
//                e.printStackTrace();
//                line += e.toString();
//            } finally {
//
//                Log.i("Mactivity_Thread", "line = " + line);
//                Message msg = new Message();
//                //  msg.set
//                msg.obj = new String(line);
//                mHandler.sendMessage(msg);
//                Looper.loop();
//
//            }
//        }
//    }


}//ALL END