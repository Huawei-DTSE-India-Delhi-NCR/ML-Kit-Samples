package com.ml.location.utils;

import com.huawei.hms.mlsdk.landmark.MLRemoteLandmark;

import java.util.List;

public interface GetGeoDetailsListener {

    public void onSuccess(List<MLRemoteLandmark> results);

    public void onFailed(String message);

}
