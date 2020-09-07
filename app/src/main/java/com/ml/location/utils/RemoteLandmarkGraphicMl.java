package com.ml.location.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.huawei.hms.mlsdk.common.MLCoordinate;
import com.huawei.hms.mlsdk.landmark.MLRemoteLandmark;

public class RemoteLandmarkGraphicMl extends MlBaseGraphic {

    private static final float TEXT_SIZE = 54.0f;
    private static final float STROKE_WIDTH = 4.0f;

    private final MLRemoteLandmark landmark;
    private final Paint boxPaint;
    private final Paint textPaint;
    private final MlGraphicOverlay overlay;

    public RemoteLandmarkGraphicMl(MlGraphicOverlay overlay, MLRemoteLandmark landmark) {
        super(overlay);
        this.overlay = overlay;
        this.landmark = landmark;
        this.boxPaint = new Paint();
        this.boxPaint.setColor(Color.WHITE);
        this.boxPaint.setStyle(Paint.Style.STROKE);
        this.boxPaint.setStrokeWidth(RemoteLandmarkGraphicMl.STROKE_WIDTH);
        this.textPaint = new Paint();
        this.textPaint.setColor(Color.WHITE);
        this.textPaint.setTextSize(RemoteLandmarkGraphicMl.TEXT_SIZE);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect boundingBox = this.landmark.getBorder();
        if (null != boundingBox) {
            RectF rect = new RectF(boundingBox);
            rect.left = this.translateX(rect.left);
            rect.top = this.translateY(rect.top);
            rect.right = this.translateX(rect.right);
            rect.bottom = this.translateY(rect.bottom);
            canvas.drawRect(rect, this.boxPaint);
        }

        int x = 0;
        int y = this.overlay.getHeight() / 2;

        canvas.drawText(this.landmark.getLandmark(), x, y, this.textPaint);
        if (this.landmark.getPositionInfos() == null || this.landmark.getPositionInfos().isEmpty()) {
            canvas.drawText("Unknown location", x, y + 50, this.textPaint);
        } else {
            for (MLCoordinate location : this.landmark.getPositionInfos()) {
                canvas.drawText("Lat: " + location.getLat() + " ,Lng:" + location.getLng(), x, y + 100, this.textPaint);
            }
            canvas.drawText("confidence: " + this.landmark.getPossibility(), x, y + 150, this.textPaint);
        }
    }

}
