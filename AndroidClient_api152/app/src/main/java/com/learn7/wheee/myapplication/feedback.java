package com.learn7.wheee.myapplication;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Wheee on 2016/7/2.
 */
public class feedback extends Activity {


    private String username;
    private String usercomment;

    //footerview的文字 与 圆形进度框
    private ProgressBar progressBar;
    private TextView textView;

    private EditText et_comment;
    private Button bn_send;
    static Handler mhandler;

    //static String URLBASE = "http://192.168.1.109/mytest/write.php?a_wORr=";
    static String URLBASE ="http://182.254.152.20/write.php?a_wORr=";//外网
    static String URLBASE_WRT = "write";
    static String URLBASE_RD = "read";
    static int LISTITEMNUM = 15;//默认显示 20 条listitem
    private int LISTITEMNOW=0;//当前共显示了多少条ListView
    private int LISTVISIBCOUNT = 0;//当前页面可以显示多少条数据

    List<HashMap<String, String>> mylist;//mylist每次获取 都更新里面所有值
    ListView lv;//ListView

    private LinearLayout loadingLayout;//listview的footerview布局
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback_layout);

        lv = (ListView) findViewById(R.id.lv_a);
        et_comment = (EditText) findViewById(R.id.et_comment);
        bn_send = (Button) findViewById(R.id.bn_send);

        //将MainActivity 的name 赋值 到这里
        username = MainActivity.sttc_name;

        //ListView lv设置 TranscriptMode取消‘alwaysScroll’->永远停在最顶端
        lv.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
        //设置监听器
        bn_send.setOnClickListener(new feedbackViewOnClikListener());

//初始化
        //添加FooterView 到ListView
        AddFooterViewtoListview();
        loadingLayout.setId(R.id.LL_listfooterview);//DIY_id.xml 自定义的id
        loadingLayout.setOnClickListener(new feedbackViewOnClikListener());

        mylist = new ArrayList<HashMap<String, String>>();//每次全部替换其值
        // et_comment.setText("http://192.168.1.109/mytest/write.php?a_user=lurenyi&a_comment=吐槽几句abc123&a_time=2016-07-03-11:00:01");
        //接收 netthread 线程反馈消息 msg.what 1:0
        mhandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //判断是否 为 write 返回值，不是则为 read
                //操作成功 时
                Log.i("handleMessage","【读】【写】 操作判断msg.what="+msg.what+"msg.obj="+msg.obj.toString());
                if (netthread.SUCCEED_FLAG_WRT.equals(msg.obj.toString())) {
                    //【写】操作成功
                    Toast.makeText(feedback.this,"【写】 操作成功",Toast.LENGTH_SHORT).show();
                    Log.i("handleMessage","【写】 操作成功msg.what="+msg.what);

                } else if (msg.what == 1) {
                    //【读】操作成功
                    //read  则msg.obj储存着read数据，已被处理去掉首位空格换行
//                    try {
//                        //解码 utf8  PHP中 【/】解码后为【/】
//                        msg.obj = URLDecoder.decode(msg.obj.toString(), "utf8");
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }
                    //先分组 再解码
                    //【分组】PHP中 【/】解码后为【/】,待处理串中只剩 【/】
                    String[] UserCommTime = msg.obj.toString().split("/");// 形式表示【\\\\】 可以正确划分 形式表示为【\\】的数组
                   //【解码】
                    String str="";//临时变量
                    for(int i=0;i<UserCommTime.length;i++) {
                        try {
                            str = URLDecoder.decode(UserCommTime[i], "utf8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        UserCommTime[i]=new String(str.toString());
                    }//解码完毕
                    //处理末尾的 【读】成功标志位
                    UserCommTime[UserCommTime.length - 1] = netthread.trimInnerSpaceStr(UserCommTime[UserCommTime.length - 1]);
                    Log.i("handleMessage", "处理末尾的 【读】成功标志位" + UserCommTime[UserCommTime.length - 1]);
                    if (netthread.SUCCEED_FLAG_RD.equals(UserCommTime[UserCommTime.length - 1]))//"ROK" == 数组最后的分组，理论上是ROK
                    {
                        //Toast.makeText(feedback.this, "确定为 【读】 操作" + UserCommTime[UserCommTime.length - 1], Toast.LENGTH_LONG).show();
                        Log.i("handlMessage", "" + "确定为 【读】 操作" + UserCommTime[UserCommTime.length - 1]);
                        //【读】成功
                        //确定不是【写】操作，且获取到【读】操作成功FLAG
                        //调用 ListSetAdapter(listview,拆分成数据整条的String数组)
                        ListSetAdapter(UserCommTime);//mylist每次全部替换其值
                        //【读】完成后 textview改变值， progressBar隐藏
                        textView.setText("【加载完成】点击加载更多...");
                        progressBar.setVisibility(View.GONE);
                        textView.setFocusableInTouchMode(true);
                    }
                }

                //操作 失败和成功都显示
                //Toast.makeText(feedback.this, msg.what == 1 ? "Done" : ("Failed" + msg.obj.toString()), Toast.LENGTH_LONG).show();
                Log.i("handlMessage", "" + (msg.what == 1 ? "Done" + msg.obj.toString() : ("Failed" + msg.obj.toString())));
            }
        };
        //第一次运行前 获取 listitem 显示
        //必须先 创建handler实例 ，第一次只更新 LISTITEMNUM 数量的ListItem
        final netthread ntthrd_fstread = new netthread(URLBASE + URLBASE_RD + ("&a_nowid=" + LISTITEMNUM),mhandler);
        ntthrd_fstread.start();


        //ListView 滑动监听
        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            private String url_read = URLBASE + URLBASE_RD;//读取mysql数据
            private int lastItemIndex;//是否滑到底了
            //private netthread ntForread = new netthread("test");

            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
               // Log.i("onScrollStateChanged", "scrollState = " + scrollState);
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Log.i("onScroll", "firstVisibleItem = " + firstVisibleItem + "  visibleItemCount=" + visibleItemCount + "  totalItemCount=" + totalItemCount);
                lastItemIndex = firstVisibleItem + visibleItemCount - 1;//第一个可见的 加上当前页面可见的listitem数目 = 总共-1

                //更新 【当前共显示多少条ListItem】
                LISTITEMNOW = totalItemCount-1;
                LISTVISIBCOUNT=visibleItemCount;//当前页面显示多少条
                Log.i("lastItemIndex", "滑动停止 最新数据条数：lastItemIndex = " + lastItemIndex+"\nLISTITEMNOW="+LISTITEMNOW+" LISTVISIBCOUNT="+LISTVISIBCOUNT);
                //lastItemIndex 索引等于 totalItemCount - 1 时，到达最下面的一个，可以加载刷新view了
//                if (lastItemIndex >= 0 && lastItemIndex == totalItemCount - 1 && !ntForread.isAlive()) {
//                   // Toast.makeText(feedback.this, "刷新中...", Toast.LENGTH_SHORT).show();
//                    //ntForread = new netthread(url_read + ("&a_nowid=" + lastItemIndex));
//                    // ntForread.start();
//                    Log.i("onScroll_ntForread", "ntForread.isAlive()线程不在活动中 启动线程开始读取listitem");
//                } else if (ntForread.isAlive()) {
//                    Toast.makeText(feedback.this, "正在刷新中 请稍后...", Toast.LENGTH_SHORT).show();
//
//                }
            }
        });




    }//Create 函数end


    //更新 ListView 数据 ；类作为函数参数传递时，传递的是引用
    public void ListSetAdapter(String[] StrData) {
        Log.i("进入ListSetAdapter", "进入ListSetAdapter函数" + StrData.length);

        //组织数据源
        mylist = new ArrayList<HashMap<String, String>>();//mylist每次全部替换其值

        //处理StrData ,urlencode($row["User"]).'+'.urlencode($row["Comment"]).'+'.urlencode($row["Time"]).'\\';
        for (int i = StrData.length-2; i>=0; i--) {
            HashMap<String, String> map = new HashMap<String, String>();//中间变量 Hashmap初始定义
            String[] singleData = StrData[i].split("&");//【&】分割符
            Log.i("", "singleData长度" + singleData.length);
            singleData[1] = "[" + singleData[2] + "]: " + singleData[1];//singleData[1] = Time + Comment 合并为一条数据

            map.put("itemuser", "" + singleData[0]);//USER name
            map.put("itemcomment", "" + singleData[1]);//USER comment
            mylist.add(map);//添加一条数据
        }

//        //配置适配器
        SimpleAdapter adapter = new SimpleAdapter(
                this, mylist, R.layout.listitem, new String[]{"itemuser", "itemcomment"},
                new int[]{R.id.tv_user, R.id.tv_comment}

        );
       // adapter.notifyDataSetChanged();//强制刷新 新的 ListView
        //添加进去
        lv.setAdapter(adapter);
        //判断是否第一次显示 而不是后序的 加载更多
        lv.setSelection((!(LISTITEMNOW==-1 &&LISTVISIBCOUNT == 0))?(LISTITEMNOW-LISTVISIBCOUNT+2):0);//总共显示了多少条数据 - 当前页面显示多少条
        Log.i("进入ListSetAdapter", "ListView数据添加结束LISTITEMNOW="+LISTITEMNOW+" LISTVISIBCOUNT="+LISTVISIBCOUNT);
    }

    private void AddFooterViewtoListview() {

        /**
         * 设置布局显示属性
         */
        LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        /**
         * 设置布局显示目标最大化属性
         */
        LinearLayout.LayoutParams FFlayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        //线性布局
        LinearLayout layout = new LinearLayout(this);
        //设置布局水平方向
        layout.setOrientation(LinearLayout.HORIZONTAL);

        //进度条
       // final ProgressBar progressBar = new ProgressBar(this);
        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.GONE);//不占用空间 不可见
        //把进度条加入到layout中
        layout.addView(progressBar, mLayoutParams);

        //文本内容
       // final TextView textView = new TextView(this);
        textView = new TextView(this);
        textView.setText("【加载完成】点击加载更多...");//
        textView.setTextSize(28);//setTextSize()默认的单位是sp
        textView.setGravity(Gravity.CENTER_VERTICAL);

//        textView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                textView.setText("加载中...");
//                progressBar.setVisibility(View.VISIBLE);//可见
//            }
//        });

        //把文本加入到layout中
        layout.addView(textView, FFlayoutParams);
        //设置layout的重力方向，即对齐方式是
        layout.setGravity(Gravity.CENTER);

        //设置ListView的页脚layout
        loadingLayout = new LinearLayout(this);
        loadingLayout.addView(layout, mLayoutParams);
        loadingLayout.setGravity(Gravity.CENTER);

        lv.addFooterView(loadingLayout);
    }

    //feedbackViewOnClikListener feedbackView事件监听器
    private class feedbackViewOnClikListener implements OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.LL_listfooterview:
                {
                    Log.i("feedbackView点击监听器","你点击了 ListView的footerview加载更多");
                    //显示 圆形进度条
                    progressBar.setVisibility(View.VISIBLE);//可见//感觉这是不安全的做法
                    textView.setText("加载中...");
                    //获取数据库 更新ListView;LISTITEMNUM + LISTITEMNOW条数据
                    netthread ntthrd_read = new netthread(URLBASE + URLBASE_RD + ("&a_nowid=" + (LISTITEMNUM+LISTITEMNOW)),mhandler);
                    ntthrd_read.start();
                    break;
                }
                case R.id.et_comment:
                {
                    Log.i("feedbackView点击监听器","你点击了 评论编辑框");
                }
                case R.id.bn_send:
                {
                    //判断是否为空
                    if(et_comment.getText().toString().equals(""))
                    {
                        et_comment.setFocusable(true);
                        et_comment.setFocusableInTouchMode(true);
                        break;}
                    Log.i("feedbackView点击监听器","你点击了 发送评论的按钮");
                    //发送数据
                    String url_write = URLBASE + URLBASE_WRT;
                    SimpleDateFormat myfmt = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss ");
                    Date dt = new Date();

                    //获取 View 数据
                 //   username = netthread.trimInnerSpaceStr(""+android.os.Build.MODEL);
                    usercomment = et_comment.getText().toString();
                    String timestr = new String(myfmt.format(dt));

                    try {
                        url_write = url_write + "&a_user=" +
                                (URLEncoder.encode(username, "utf8")) + "&a_comment=" +
                                URLEncoder.encode(usercomment, "utf8") + "&a_time=" +
                                URLEncoder.encode(timestr, "utf8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    Log.i("setOnClickListener_启动线程", url_write + "");
                    //启动线程
                    netthread nt_write = new netthread(url_write,mhandler);
                    nt_write.start();
                    et_comment.setText("");

                    //关闭输入法 如果输入法在窗口上已经显示，则隐藏，反之则显示
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

                    //创建线程更新 ListView
                    //获取数据库 更新ListView;LISTITEMNUM + LISTITEMNOW条数据
                    netthread ntthrd_read = new netthread(URLBASE + URLBASE_RD + ("&a_nowid=" + (LISTITEMNUM+LISTITEMNOW)),mhandler);
                    ntthrd_read.start();
                }

            }
        }
    }
}


//        //点击 提交按钮发送至服务器mysql存储
//        bn_send.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                String url_write = URLBASE + URLBASE_WRT;
////                SimpleDateFormat myfmt = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss ");
////                Date dt = new Date();
////
////
////                //获取 View 数据
////                username = netthread.trimInnerSpaceStr(""+android.os.Build.MODEL);
////                usercomment = et_comment.getText().toString();
////                String timestr = new String(myfmt.format(dt));
////
////                try {
////                    url_write = url_write + "&a_user=" +
////                            (URLEncoder.encode(username, "utf8")) + "&a_comment=" +
////                            URLEncoder.encode(usercomment, "utf8") + "&a_time=" +
////                            URLEncoder.encode(timestr, "utf8");
////                } catch (UnsupportedEncodingException e) {
////                    e.printStackTrace();
////                }
////                Log.i("setOnClickListener_启动线程", url_write + "");
////                //启动线程
////                netthread nt_write = new netthread(url_write);
////                nt_write.start();
////                et_comment.setText("");
//            }
//        });//setOnClickListener函数end