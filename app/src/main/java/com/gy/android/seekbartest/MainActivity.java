package com.gy.android.seekbartest;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    private TextView now_time;
    private SeekBar audio_seekBar;
    private Button btn_start_audio;
    private Button btn_stop_audio;

    private MediaPlayer m;

    private Context context = MainActivity.this;

    private Thread thread;
    //记录播放位置
    private int time;
    //记录是否暂停
    private boolean flage = false, isChanging = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Media控件设置
        m = new MediaPlayer();
        init();
    }

    //Activity从后台重新回到前台时被调用
    @Override
    protected void onRestart() {
        super.onRestart();
        if (m != null) {
            if (m.isPlaying()) {
                m.start();
            }
        }
    }

    //Activity被覆盖到下面或者锁屏时被调用
    @Override
    protected void onPause() {
        super.onPause();
        if (m != null) {
            if (m.isPlaying()) {
                m.pause();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (m != null) {
            if (!m.isPlaying()) {
                m.start();
            }
        }
    }

    //Activity被销毁
    protected void onDestroy() {
        if (m.isPlaying()) {
            m.stop();//停止音频的播放
        }
        m.release();//释放资源
        super.onDestroy();
    }

    class ClickEvent implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.Button01:
                    if (m.isPlaying()) {
                        //m.getCurrentPosition();获取当前播放位置
                        time = m.getCurrentPosition();
                        // 如果正在播放，则暂停，并把按钮上的文字设置成“暂停”
                        m.pause();
                        btn_start_audio.setText("暂停");
                        flage = true;//flage 标记为 ture
                    } else if (flage) {
                        m.start();//先开始播放
                        m.seekTo(time);//设置从哪里开始播放
                        btn_start_audio.setText("播放");
                        flage = false;
                    } else {
                        m.reset();//恢复到未初始化的状态
                        m = MediaPlayer.create(context,Uri.parse("http://zhangmenshiting.qianqian.com/data2/music/ba88ac878c68c2891a9c117a30ec05b1/540320263/540320263.mp3?xcode=e4b4d6362083813c0e36f2067d58dcad"));//读取音频
                        audio_seekBar.setMax(m.getDuration());//设置SeekBar的长度
                        try {
                            m.prepare();    //准备
                        } catch (IllegalStateException | IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        m.start();  //播放
                        // 创建一个线程
                        btn_start_audio.setText("播放");
                    }
                    thread = new Thread(new SeekBarThread());
                    // 启动线程
                    thread.start();
                    break;
                case R.id.Button02:
                    m.stop();
                    audio_seekBar.setProgress(0);
                    break;
            }

        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        now_time.setText("当前播放时间" + ShowTime(progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //防止在拖动进度条进行进度设置时与Thread更新播放进度条冲突
        isChanging = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        now_time.setText("当前播放时间" + ShowTime(seekBar.getProgress()));
        //将media进度设置为当前seekbar的进度
        m.seekTo(seekBar.getProgress());
        isChanging = false;
        thread = new Thread(new SeekBarThread());
        // 启动线程
        thread.start();
    }

    // 自定义的线程
    class SeekBarThread implements Runnable {

        @Override
        public void run() {
            while (!isChanging && m.isPlaying()) {
                // 将SeekBar位置设置到当前播放位置
                audio_seekBar.setProgress(m.getCurrentPosition());
                try {
                    // 每100毫秒更新一次位置
                    Thread.sleep(100);
                    //播放进度
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //时间显示函数,我们获得音乐信息的是以毫秒为单位的，把把转换成我们熟悉的00:00格式
    public String ShowTime(int time) {
        time /= 1000;
        int minute = time / 60;
        int hour = minute / 60;
        int second = time % 60;
        minute %= 60;
        return String.format("%02d:%02d", minute, second);
    }

    private void init() {
        audio_seekBar = (SeekBar) findViewById(R.id.seekBar);
        btn_start_audio = (Button) findViewById(R.id.Button01);
        btn_stop_audio = (Button) findViewById(R.id.Button02);
        now_time = (TextView) findViewById(R.id.now_time);

        btn_start_audio.setOnClickListener(new ClickEvent());
        btn_stop_audio.setOnClickListener(new ClickEvent());
        audio_seekBar.setOnSeekBarChangeListener(this);
    }
}
