package com.ml.location.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.util.Log;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.ml.common.utils.NV21ToBitmapConverter;
import com.huawei.hms.mlsdk.common.MLFrame;

import java.nio.ByteBuffer;

public abstract class MlBaseTransactorMl<T> implements MlImageTransactor {
    private static final String TAG = "BaseTransactor";
    // To keep the latest images and its metadata.
    private ByteBuffer latestImage;

    private MlFrameMetadata latestImageMetaData;

    // To keep the images and metadata in process.
    private ByteBuffer transactingImage;

    private MlFrameMetadata transactingMetaData;

    private Context mContext;

    private NV21ToBitmapConverter converter = null;

    public MlBaseTransactorMl() {
    }

    public MlBaseTransactorMl(Context context) {
        this.mContext = context;
        this.converter = new NV21ToBitmapConverter(this.mContext);
    }

    @Override
    public synchronized void process(ByteBuffer data, final MlFrameMetadata mlFrameMetadata, MlGraphicOverlay mlGraphicOverlay) {
        this.latestImage = data;
        this.latestImageMetaData = mlFrameMetadata;
        if (this.transactingImage == null && this.transactingMetaData == null) {
            this.processLatestImage(mlGraphicOverlay);
        }
    }

    @Override
    public void process(Bitmap bitmap, MlGraphicOverlay mlGraphicOverlay) {
        MLFrame frame = new MLFrame.Creator().setBitmap(bitmap).create();
        this.detectInVisionImage(bitmap, frame, null, mlGraphicOverlay);
    }

    private synchronized void processLatestImage(MlGraphicOverlay mlGraphicOverlay) {
        this.transactingImage = this.latestImage;
        this.transactingMetaData = this.latestImageMetaData;
        this.latestImage = null;
        this.latestImageMetaData = null;
        Bitmap bitmap = null;
        if (this.transactingImage != null && this.transactingMetaData != null) {
            int width;
            int height;
            width = this.transactingMetaData.getWidth();
            height = this.transactingMetaData.getHeight();
            MLFrame.Property metadata = new MLFrame.Property.Creator().setFormatType(ImageFormat.NV21)
                    .setWidth(width)
                    .setHeight(height)
                    .setQuadrant(this.transactingMetaData.getRotation())
                    .create();


                bitmap = MlBitmapUtils.getBitmap(this.transactingImage, this.transactingMetaData);
                this.detectInVisionImage(bitmap, MLFrame.fromByteBuffer(this.transactingImage, metadata),
                        this.transactingMetaData, mlGraphicOverlay);
        }
    }

    private void detectInVisionImage(final Bitmap bitmap, MLFrame image, final MlFrameMetadata metadata,
                                     final MlGraphicOverlay mlGraphicOverlay) {
        this.detectInImage(image).addOnSuccessListener(new OnSuccessListener<T>() {
            @Override
            public void onSuccess(T results) {
                if (metadata == null || metadata.getCameraFacing() == MlCameraConfiguration.getCameraFacing()) {
                    MlBaseTransactorMl.this.onSuccess(bitmap, results, metadata, mlGraphicOverlay);
                }
                MlBaseTransactorMl.this.processLatestImage(mlGraphicOverlay);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                MlBaseTransactorMl.this.onFailure(e);
            }
        });
    }

    @Override
    public void stop() {
    }

    /**
     * Detect image
     *
     * @param image MLFrame object
     * @return Task object
     */
    protected abstract Task<T> detectInImage(MLFrame image);

    /**
     * Callback that executes with a successful detection result.
     *
     * @param originalCameraImage hold the original image from camera, used to draw the background image.
     * @param results             T object
     * @param mlFrameMetadata     FrameMetadata object
     * @param mlGraphicOverlay    GraphicOverlay object
     */

    protected abstract void onSuccess(
            Bitmap originalCameraImage,
            T results,
            MlFrameMetadata mlFrameMetadata, MlGraphicOverlay mlGraphicOverlay);

    /**
     * Callback that executes with failure detection result.
     *
     * @param exception Exception object
     */
    protected abstract void onFailure(Exception exception);

}
