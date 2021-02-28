package cn.iwgang.countdownview;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

/**
 * 使用android.os.CountDownTimer的源码
 * 1. 对回调onTick做了细小调整，已解决最后1秒不会倒计时到0，要等待2秒才回调onFinish
 * 2. 添加了一些自定义方法
 * Created by iWgang on 15/10/18.
 * https://github.com/iwgang/CountdownView
 * <p>
 * <p>
 * System.currentTimeMillis()  系统时间，也就是日期时间，可以被系统设置修改，然后值就会发生跳变。
 * <p>
 * SystemClock.uptimeMillis() 自开机后，经过的时间，不包括深度睡眠的时间
 * <p>
 * SystemClock.elapsedRealtime() 自开机后，经过的时间，包括深度睡眠的时间
 */
public abstract class CustomCountDownTimer {
    private static final int MSG = 1;
    private final long mMillisInFuture;
    private final long mCountdownInterval;
    private long mStopTimeInFuture;
    private long mPauseTimeInFuture;
    private boolean isStop = false;
    private boolean isPause = false;

    /**
     * @param millisInFuture    总倒计时时间
     * @param countDownInterval 倒计时间隔时间
     */
    public CustomCountDownTimer(long millisInFuture, long countDownInterval) {
        // 解决秒数有时会一开始就减去了2秒问题（如10秒总数的，刚开始就8999，然后没有不会显示9秒，直接到8秒）
        if (countDownInterval > 1000) millisInFuture += 15;
        mMillisInFuture = millisInFuture;
        mCountdownInterval = countDownInterval;
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            synchronized (CustomCountDownTimer.this) {
                if (isStop || isPause) {
                    return;
                }
                // 直到倒计时完成还剩余的总时间=未来的时间点-当前时间
                final long millisLeft = mStopTimeInFuture - SystemClock.elapsedRealtime();
                // 倒计时结束回调 onFinish
                long lastTickDuration = getLastTickDuration(CustomCountDownTimer.this, millisLeft);
                //计算延迟
                long delay = getDelay(CustomCountDownTimer.this, millisLeft, lastTickDuration);
                //开始下一次
                //发送延迟消息准备下一次的 onTick
                sendMessageDelayed(obtainMessage(MSG), delay);
            }


        }
    };

    /**
     * 开始倒计时
     */
    public synchronized final void start() {
        start(mMillisInFuture);
    }

    /**
     * 停止倒计时
     */
    public synchronized final void stop() {
        isStop = true;
        mHandler.removeMessages(MSG);
    }

    /**
     * 暂时倒计时
     * 调用{@link #restart()}方法重新开始
     */
    public synchronized final void pause() {
        if (isStop) return ;

        isPause = true;
        mPauseTimeInFuture = mStopTimeInFuture - SystemClock.elapsedRealtime();
        mHandler.removeMessages(MSG);
    }

    /**
     * 重新开始
     */
    public synchronized final void restart() {
        if (isStop || !isPause) return ;

        isPause = false;
        start(mPauseTimeInFuture);
    }

    /**
     * 倒计时间隔回调
     * @param millisUntilFinished 剩余毫秒数
     */
    public abstract void onTick(long millisUntilFinished);

    /**
     * 倒计时结束回调
     */
    public abstract void onFinish();

    private synchronized CustomCountDownTimer start(long millisInFuture) {
        isStop = false;
//        if (millisInFuture <= 0) {
//            onFinish();
//            return this;
//        }

        mStopTimeInFuture = SystemClock.elapsedRealtime() + millisInFuture;
        mHandler.sendMessage(mHandler.obtainMessage(MSG));
        return this;
    }

    /**
     * 计算满足下一次回调的延迟时间
     *
     * @param countDownTimer
     * @param millisLeft
     * @param lastTickDuration
     * @return
     */
    private long getDelay(CustomCountDownTimer countDownTimer, long millisLeft, long lastTickDuration) {
        long delay;
        long millisLeftAbs = Math.abs(millisLeft);
        if (millisLeftAbs < countDownTimer.mCountdownInterval) {
            // 如果剩余的时间不够一次的 mCountdownInterval
            // 下一次的延迟时间 = 总的剩余时间 - 用户的onTick花费的时间
            delay = millisLeftAbs - lastTickDuration;
            // 特殊情况：用户的 onTic花费了多个 mCountdownInterval 才能完成，立即触发onFinish
            if (delay < 0) delay = 0;
        } else {
            // 如果剩余的时间多余 mCountdownInterval
            //  下一次的延迟时间 =  mCountdownInterval - 用户的onTick花费的时间
            delay = countDownTimer.mCountdownInterval - lastTickDuration;
            // 特殊情况：用户的 onTick 花费了多个 mCountdownInterval 才能完成，直接跳至下一个有效的间隔
            while (delay < 0) {
                // 循加 mCountdownInterval 直到 delay >= 0
                delay = delay + countDownTimer.mCountdownInterval;
            }
        }
        return delay;
    }

    /**
     * 回调 onTick() 并返回onTick方法的耗时时间
     *
     * @param countDownTimer
     * @param millisLeft
     * @return
     */
    private long getLastTickDuration(CustomCountDownTimer countDownTimer, long millisLeft) {
        //上次onTick开始时间
        long lastTickStart = SystemClock.elapsedRealtime();
        countDownTimer.onTick(millisLeft);
        //上次onTick结束时间
        long lastTickEnd = SystemClock.elapsedRealtime();
        // 考虑用户的onTick花费的时间
        return lastTickEnd - lastTickStart;
    }
}
