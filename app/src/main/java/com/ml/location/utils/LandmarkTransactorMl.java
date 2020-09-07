/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.ml.location.utils;

import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.landmark.MLRemoteLandmark;
import com.huawei.hms.mlsdk.landmark.MLRemoteLandmarkAnalyzer;
import com.huawei.hms.mlsdk.landmark.MLRemoteLandmarkAnalyzerSetting;

import java.io.IOException;
import java.util.List;

public class LandmarkTransactorMl extends MlBaseTransactorMl<List<MLRemoteLandmark>> {

    private static final String TAG = "LandmarkTransactor";

    private final MLRemoteLandmarkAnalyzer detector;

    GetGeoDetailsListener getGeoDetailsListener;


    public LandmarkTransactorMl(GetGeoDetailsListener getGeoDetailsListener) {
        super();
        this.detector = MLAnalyzerFactory.getInstance().getRemoteLandmarkAnalyzer();
        this.getGeoDetailsListener=getGeoDetailsListener;
    }

    public LandmarkTransactorMl(MLRemoteLandmarkAnalyzerSetting options) {
        super();
        this.detector = MLAnalyzerFactory.getInstance().getRemoteLandmarkAnalyzer(options);
    }

    @Override
    public void stop() {
        super.stop();
        try {
            this.detector.close();
        } catch (IOException e) {
            Log.e(LandmarkTransactorMl.TAG, "Exception thrown while trying to close remote landmark transactor: " + e.getMessage());
            getGeoDetailsListener.onFailed("Exception thrown while trying to close remote landmark transactor: " + e.getMessage());
        }
    }

    @Override
    protected Task<List<MLRemoteLandmark>> detectInImage(MLFrame image) {
        return this.detector.asyncAnalyseFrame(image);
    }

    @Override
    protected void onSuccess(
            Bitmap originalCameraImage,
            List<MLRemoteLandmark> results,
            MlFrameMetadata mlFrameMetadata, MlGraphicOverlay mlGraphicOverlay) {


        if (results != null && !results.isEmpty()) {
            mlGraphicOverlay.clear();
            for (MLRemoteLandmark landmark : results) {
                RemoteLandmarkGraphicMl landmarkGraphic = new RemoteLandmarkGraphicMl(mlGraphicOverlay, landmark);
                mlGraphicOverlay.addGraphic(landmarkGraphic);
            }
            getGeoDetailsListener.onSuccess(results);
            mlGraphicOverlay.postInvalidate();
        }

    }

    @Override
    protected void onFailure(Exception e) {
        Log.e(LandmarkTransactorMl.TAG, "Remote landmark detection failed: " + e.getMessage());
       // this.handler.sendEmptyMessage(MlConstant.GET_DATA_FAILED);
        getGeoDetailsListener.onFailed("Remote landmark detection failed: " + e.getMessage());
    }
}
