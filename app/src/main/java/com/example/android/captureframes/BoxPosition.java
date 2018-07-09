package com.example.android.captureframes;

/**
 * Created by shafy on 09/07/2018.
 */

public class BoxPosition {
    private Double left;
    private Double top;
    private Double right;
    private Double bottom;
    private Double width;
    private Double height;

    public BoxPosition(Double left, Double top, Double width, Double height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;

        init();
    }

    public BoxPosition(BoxPosition boxPosition) {
        this.left = boxPosition.left;
        this.top = boxPosition.top;
        this.width = boxPosition.width;
        this.height = boxPosition.height;

        init();
    }

    public void init() {
        Double tmpLeft = this.left;
        Double tmpTop = this.top;
        Double tmpRight = this.left + this.width;
        Double tmpBottom = this.top + this.height;

        this.left = Math.min(tmpLeft, tmpRight); // left should have lower value as right
        this.top = Math.min(tmpTop, tmpBottom);  // top should have lower value as bottom
        this.right = Math.max(tmpLeft, tmpRight);
        this.bottom = Math.max(tmpTop, tmpBottom);
    }

    public Double getLeft() {
        return left;
    }

    public Double getTop() {
        return top;
    }

    public Double getWidth() {
        return width;
    }

    public Double getHeight() {
        return height;
    }

    public Double getRight() {
        return right;
    }

    public Double getBottom() {
        return bottom;
    }
}
