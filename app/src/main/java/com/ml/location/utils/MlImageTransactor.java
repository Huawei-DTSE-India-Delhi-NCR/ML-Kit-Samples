package com.ml.location.utils;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;

public interface MlImageTransactor {
    /**
     * Start detection
     *
     * @param data ByteBuffer object
     * @param mlFrameMetadata FrameMetadata object
     * @param mlGraphicOverlay GraphicOverlay object
     */
    void process(ByteBuffer data, MlFrameMetadata mlFrameMetadata, MlGraphicOverlay mlGraphicOverlay);

    /**
     * Start detection
     *
     * @param bitmap Bitmap object
     * @param mlGraphicOverlay GraphicOverlay object
     */
    void process(Bitmap bitmap, MlGraphicOverlay mlGraphicOverlay);

    /**
     * Stop detection
     */
    void stop();



}
