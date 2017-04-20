package com.reactnativecomponent.barcode;


import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.common.SystemClock;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.google.zxing.BarcodeFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RCTCaptureManager extends ViewGroupManager<CaptureView> {
    private static final String REACT_CLASS = "CaptureView";//要与类名一致
    public static final int CHANGE_SHOW = 0;//用来标记方法的下标
    Activity activity;
    CaptureView cap;
    private float density;

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public CaptureView createViewInstance(ThemedReactContext context) {
        Activity activity = context.getCurrentActivity();
        density = activity.getResources().getDisplayMetrics().density;
        cap = new CaptureView(activity, context);

        return cap;
    }
    @ReactProp(name = "barCodeTypes")
    public void setbarCodeTypes(CaptureView view, ReadableArray barCodeTypes) {

        if (barCodeTypes == null) {
            return;
        }
        List<String> result = new ArrayList<String>(barCodeTypes.size());
        for (int i = 0; i < barCodeTypes.size(); i++) {
            result.add(barCodeTypes.getString(i));
        }
        view.setDecodeFormats(result);

    }
    @ReactProp(name = "scannerRectLeft", defaultInt = 0)
    public void setCX(CaptureView view, int cX) {
        view.setcX((int) (cX* density + 0.5f));
    }

    @ReactProp(name = "scannerRectTop", defaultInt = 0)
    public void setCY(CaptureView view, int cY) {
        view.setcY((int)(cY* density + 0.5f));
    }

    @ReactProp(name = "scannerRectWidth", defaultInt = 255)
    public void setMAX_FRAME_WIDTH(CaptureView view, int FRAME_WIDTH) {
        view.setMAX_FRAME_WIDTH((int) (FRAME_WIDTH * density + 0.5f));
    }

    @ReactProp(name = "scannerRectHeight", defaultInt = 255)
    public void setMAX_FRAME_HEIGHT(CaptureView view, int FRAME_HEIGHT) {
        view.setMAX_FRAME_HEIGHT((int) (FRAME_HEIGHT * density + 0.5f));
    }

    //扫描线移动一圈时间
    @ReactProp(name = "scannerLineInterval", defaultInt = 1000)
    public void setTime(CaptureView view, int time) {
        view.setScanTime(time);
    }

    @ReactProp(name = "scannerRectCornerColor")
    public void setCORNER_COLOR(CaptureView view, String color) {
        if (color != null && !color.isEmpty()) {
            view.setCORNER_COLOR(Color.parseColor(color));//转换成16进制
        }
    }

    @Override
    public
    @Nullable
    Map<String, Integer> getCommandsMap() {
        return MapBuilder.of(
                "change",
                CHANGE_SHOW);//js处发送的方法名字
    }

    @Override
    public void receiveCommand(CaptureView root, int commandId, @Nullable ReadableArray config) {
        if (commandId == CHANGE_SHOW) {
            this.changeWidthHeight(config.getMap(0));
        }
    }


    @ReactMethod
    public void changeWidthHeight(final ReadableMap config) {
        if (cap != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    int width = config.getInt("FRAME_WIDTH");
                    int height = config.getInt("FRAME_HEIGHT");
                    cap.setCHANGE_WIDTH((int)(width* density + 0.5f), (int)(height* density + 0.5f));
                }
            });
        }
    }

    @Override
    protected void addEventEmitters(
            final ThemedReactContext reactContext,
            final CaptureView view) {
        view.setOnEvChangeListener(
                new CaptureView.OnEvChangeListener() {
                    @Override
                    public void getQRCodeResult(String result,BarcodeFormat format) {
                        reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher()
                                .dispatchEvent(new QRCodeResultEvent(view.getId(), SystemClock.nanoTime(), result,format));
                    }

                });
    }

    @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.<String, Object>builder()
                .put("QRCodeResult", MapBuilder.of("registrationName", "onBarCodeRead"))//registrationName 后的名字,RN中方法也要是这个名字否则不执行
                .build();
    }


}