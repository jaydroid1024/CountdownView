package cn.iwgang.countdownviewdemo;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.iwgang.countdownview.CountdownView;


public class MainActivity extends AppCompatActivity implements CountdownView.OnCountdownEndListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CountdownView mCvCountdownViewTest1 = (CountdownView) findViewById(R.id.cv_countdownViewTest1);
        mCvCountdownViewTest1.setTag("test1");
        long time1 = (long) 5 * 60 * 60 * 1000;
        mCvCountdownViewTest1.start(time1);

        CountdownView mCvCountdownViewTest2 = (CountdownView) findViewById(R.id.cv_countdownViewTest2);
        mCvCountdownViewTest2.setTag("test2");
        long time2 = (long) 30 * 60 * 1000;
        mCvCountdownViewTest2.start(time2);

        CountdownView cvCountdownViewTest211 = (CountdownView) findViewById(R.id.cv_countdownViewTest211);
        cvCountdownViewTest211.setTag("test21");
        long time211 = (long)30 * 60 * 1000;
        cvCountdownViewTest211.start(time211);

        CountdownView mCvCountdownViewTest21 = (CountdownView)findViewById(R.id.cv_countdownViewTest21);
        mCvCountdownViewTest21.setTag("test21");
        long time21 = (long)24 * 60 * 60 * 1000;
        mCvCountdownViewTest21.start(time21);

        CountdownView mCvCountdownViewTest22 = (CountdownView)findViewById(R.id.cv_countdownViewTest22);
        mCvCountdownViewTest22.setTag("test22");
        long time22 = (long)30 * 60 * 1000;
        mCvCountdownViewTest22.start(time22);

        CountdownView mCvCountdownViewTest3 = (CountdownView)findViewById(R.id.cv_countdownViewTest3);
        long time3 = (long)9 * 60 * 60 * 1000;
        mCvCountdownViewTest3.start(time3);

        CountdownView mCvCountdownViewTest4 = (CountdownView)findViewById(R.id.cv_countdownViewTest4);
        long time4 = (long)150 * 24 * 60 * 60 * 1000;
        mCvCountdownViewTest4.start(time4);

        CountdownView cv_convertDaysToHours = (CountdownView) findViewById(R.id.cv_convertDaysToHours);
        // long timeConvertDaysToHours = (long) 150 * 24 * 60 * 60 * 1000;
        cv_convertDaysToHours.start(time4);

        final CountdownView mCvCountdownViewTest5 = (CountdownView)findViewById(R.id.cv_countdownViewTest5);
        new AsyncTask<Void, Long, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                long time = 0;
                while (true) {
                    try {
                        Thread.sleep(1000);
                        time += 1000;
                        publishProgress(time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            protected void onProgressUpdate(Long... values) {
                super.onProgressUpdate(values);
                mCvCountdownViewTest5.updateShow(values[0]);
            }
        }.execute();

        CountdownView mCvCountdownViewTest6 = (CountdownView) findViewById(R.id.cv_countdownViewTest6);
        long time6 = (long) 5 * 1000;
        mCvCountdownViewTest6.start(time6);
        mCvCountdownViewTest6.setOnCountUpListener(new CountdownView.OnCountUpListener() {
            @Override
            public void onUp(CountdownView cv) {
                TextView mCountdown = findViewById(R.id.tv_time_label);
                mCountdown.setText("已超时");
                mCountdown.setTextColor(Color.RED);

            }
        });


        findViewById(R.id.btn_toDynamicShowActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DynamicShowActivity.class));
            }
        });

        findViewById(R.id.btn_toListViewActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ListViewActivity.class));
            }
        });

        findViewById(R.id.btn_toRecyclerViewActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RecyclerViewActivity.class));
            }
        });

        findViewById(R.id.btn_toTimerVTestiewActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                long time = 3 * 1000;
                long time2 = 0;
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                try {
                    Date date = df.parse("2021-2-27 21:23:36");// new Date()为获取当前系统时间，也可使用当前时间戳
                    time2 = date.getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                long currentTimeMillis = System.currentTimeMillis();
                long uptimeMillis = SystemClock.uptimeMillis();
                long elapsedRealtime = SystemClock.elapsedRealtime();
                long dura = time2 - currentTimeMillis;

                dura = 1000 * 5;
                CountDownTimer countDownTimer = new CountDownTimer(dura, 1) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        reSetTime(millisUntilFinished);
                    }

                    @Override
                    public void onFinish() {
                        Toast.makeText(MainActivity.this, "onFinish", Toast.LENGTH_SHORT).show();
                    }
                };

                countDownTimer.start();

            }
        });
    }


    private void reSetTime(long ms) {
        String prefix = "倒计时";
        if (ms < 0) {
            prefix = "超时时";
            ms = Math.abs(ms);
        }
        int day = 0;
        int hour;
        day = (int) (ms / (1000 * 60 * 60 * 24));
        hour = (int) (ms / (1000 * 60 * 60));
        int minute = (int) ((ms % (1000 * 60 * 60)) / (1000 * 60));
        int second = (int) ((ms % (1000 * 60)) / 1000);
        int millisecond = (int) (ms % 1000);
        TextView mCountdown = findViewById(R.id.tv_info);
        mCountdown.setText("prefix:+" + prefix + "+day:" + day + ",hour:" + hour + ",minute:" + minute + ",second:" + second + ",millisecond:" + millisecond);
    }


    @Override
    public void onEnd(CountdownView cv) {
        Object tag = cv.getTag();
        if (null != tag) {
            Log.i("wg", "tag = " + tag.toString());
        }
    }
}


