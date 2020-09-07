package com.ml.location.utils;

import android.graphics.Canvas;

import com.huawei.hms.mlsdk.common.LensEngine;

public abstract class MlBaseGraphic {
    private MlGraphicOverlay mlGraphicOverlay;

    public MlBaseGraphic(MlGraphicOverlay overlay) {
        this.mlGraphicOverlay = overlay;
    }

    /**
     * Draw of view
     *
     * @param canvas Canvas object
     */
    public abstract void draw(Canvas canvas);

    public float scaleX(float x) {
        return x * this.mlGraphicOverlay.getWidthScaleValue();
    }

    public float scaleY(float y) {
        return y * this.mlGraphicOverlay.getHeightScaleValue();
    }

    public float translateX(float x) {
        if (this.mlGraphicOverlay.getCameraFacing() == LensEngine.FRONT_LENS) {
            return this.mlGraphicOverlay.getWidth() - this.scaleX(x);
        } else {
            return this.scaleX(x);
        }
    }

    public float translateY(float y) {
        return this.scaleY(y);
    }
}
