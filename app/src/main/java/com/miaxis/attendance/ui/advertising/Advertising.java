package com.miaxis.attendance.ui.advertising;

/**
 * @author Tank
 * @date 2021/8/25 2:33 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class Advertising {

    public int imageId;
    public int logoId;
    public MxRect rect;


    public Advertising(int imageId, int logoId, MxRect rect) {
        this.imageId = imageId;
        this.logoId = logoId;
        this.rect = rect;
    }


    public static class MxRect {
        public int left;
        public int top;
        public int right;
        public int bottom;

        public MxRect(int left, int top, int width, int height) {
            this.left = left;
            this.top = top;
            this.right = left + width;
            this.bottom = top + height;
        }

        public int width() {
            return right - left;
        }

        public int height() {
            return bottom - top;
        }
    }
}
