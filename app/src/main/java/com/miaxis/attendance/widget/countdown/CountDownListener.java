package com.miaxis.attendance.widget.countdown;

/**
 * @author Tank
 * @date 2020/9/10 17:13
 * @des
 * @updateAuthor
 * @updateDes
 */
public interface CountDownListener {

    void onCountDownStart();

    void onCountDownProgress(int progress);

    void onCountDownDone();
}
