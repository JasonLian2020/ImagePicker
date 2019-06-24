package me.jason.imagepicker.internal.entity;

public class CaptureItem {
    public static final int IMAGE_CAPTURE = 1;
    public static final int VIDEO_CAPTURE = 2;

    private String title;
    private int captureType;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCaptureType() {
        return captureType;
    }

    public void setCaptureType(int captureType) {
        this.captureType = captureType;
    }

    public CaptureItem(String title, int captureType) {
        this.title = title;
        this.captureType = captureType;
    }
}
