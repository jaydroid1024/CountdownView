package cn.iwgang.countdownviewdemo;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

/**
 * 将倒计时安排到将来的某个时间，并在此期间定期进行通知。
 * 在文本字段中显示30秒倒计时的示例：
 * <pre class="prettyprint">
 * new CountDownTimer(30000, 1000) {
 *
 *     public void onTick(long millisUntilFinished) {
 *         mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
 *     }
 *
 *     public void onFinish() {
 *         mTextField.setText("done!");
 *     }
 *  }.start();
 * </pre>
 * <p>
 * 对onTick（long）的调用已同步到该对象，因此在上一个回调完成之前永远不会发生对onTick（long）的调用。
 * 仅当onTick（long）的实现花费的时间与倒计时间隔相比要小得多时，才有意义。
 */
public abstract class CountDownTimer {

    /**
     * 消息
     */
    private static final int MSG_WHAT = 1;
    /**
     * MainLooper
     */
    final Looper mLooper = Looper.getMainLooper();
    /**
     * 经过多长时间需要停止的时间间隔(单位为毫秒)
     */
    private final long mMillisInFuture;
    /**
     * 用户接收回调的时间间隔(单位为毫秒)
     */
    private final long mCountdownInterval;
    /**
     * 倒计时处理器
     */
    private final Handler mHandler = new CountDownTimerHandler(this, mLooper);
    /**
     * 停止倒计时的未来的时间点
     */
    private long mStopTimeInFuture;

    /**
     * 开始超时计时的过去的时间点
     */
    private long mStopTimeInPassed;
    /**
     * 表示计时器是否被取消的布尔值
     */
    private boolean mCancelled = false;

    /**
     * CountDownTimer 构造器
     *
     * @param millisInFuture    从调用 start() 方法到倒计时完成为止的将来的毫秒数，然后调用onFinish()。
     * @param countDownInterval 接收onTick（long）回调的时间间隔。
     */
    public CountDownTimer(long millisInFuture, long countDownInterval) {
        mMillisInFuture = millisInFuture;
        mCountdownInterval = countDownInterval;
    }

    /**
     * 取消倒数计时
     */
    public synchronized final void cancel() {
        mCancelled = true;
        mHandler.removeMessages(MSG_WHAT);
    }

    /**
     * 开始倒计时
     */
    public synchronized final CountDownTimer start() {
        mCancelled = false;
        //倒计时总时间差小于0直接停止
        if (mMillisInFuture <= 0) {
            mStopTimeInPassed = SystemClock.elapsedRealtime() - mMillisInFuture;

            //超时记时
//            onFinish();
//            return this;
        }
        // System.currentTimeMillis()  系统时间，也就是日期时间，可以被系统设置修改，然后值就会发生跳变。
        // SystemClock.uptimeMillis() 自开机后，经过的时间，不包括深度睡眠的时间
        // SystemClock.elapsedRealtime() 自开机后，经过的时间，包括深度睡眠的时间
        // mStopTimeInFuture: 停止倒计时的未来的时间点==当前时间+倒计时总时间差
        mStopTimeInFuture = SystemClock.elapsedRealtime() + mMillisInFuture;
        //通知 CountDownTimerHandler 开始处理倒计时逻辑
        mHandler.sendMessage(mHandler.obtainMessage(MSG_WHAT));
        return this;
    }

    /**
     * 定期触发回调
     *
     * @param millisUntilFinished 直到完成剩余的总时间 == mStopTimeInFuture - SystemClock.elapsedRealtime();
     */
    public abstract void onTick(long millisUntilFinished);

    /**
     * 时间结束时触发回调
     */
    public abstract void onFinish();

    /**
     * 倒计时处理器
     */
    class CountDownTimerHandler extends Handler {

        CountDownTimer countDownTimer;

        public CountDownTimerHandler(CountDownTimer countDownTimer, Looper mLooper) {
            this.countDownTimer = countDownTimer;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            synchronized (CountDownTimer.class) {
                if (countDownTimer == null) return;
                if (countDownTimer.mCancelled) return;

                // 直到倒计时完成还剩余的总时间=未来的时间点-当前时间
                final long millisLeft = countDownTimer.mStopTimeInFuture - SystemClock.elapsedRealtime();
                // 倒计时结束回调 onFinish
                long lastTickDuration = getLastTickDuration(countDownTimer, millisLeft);

                Log.d("Jay", "lastTickDuration: " + lastTickDuration);
                Log.d("Jay", "millisLeft: " + millisLeft);
                //计算延迟
                long delay = getDelay(countDownTimer, millisLeft, lastTickDuration);
                Log.d("Jay", "delay: " + delay);
                //开始下一次
                //发送延迟消息准备下一次的 onTick
                sendMessageDelayed(obtainMessage(MSG_WHAT), delay);
            }

        }

        /**
         * 计算满足下一次回调的延迟时间
         *
         * @param countDownTimer
         * @param millisLeft
         * @param lastTickDuration
         * @return
         */
        private long getDelay(CountDownTimer countDownTimer, long millisLeft, long lastTickDuration) {
            long delay;
            long millisLeftAbs = Math.abs(millisLeft);
            Log.d("Jay", "getDelay,millisLeftAbs: " + millisLeftAbs);
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
        private long getLastTickDuration(CountDownTimer countDownTimer, long millisLeft) {
            //上次onTick开始时间
            long lastTickStart = SystemClock.elapsedRealtime();
            countDownTimer.onTick(millisLeft);
            //上次onTick结束时间
            long lastTickEnd = SystemClock.elapsedRealtime();
            // 考虑用户的onTick花费的时间
            return lastTickEnd - lastTickStart;
        }
    }
}
