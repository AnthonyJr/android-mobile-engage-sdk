package com.emarsys.mobileengage;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.DeviceInfo;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.HeaderUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

class MobileEngageInternal {
    private static String ENDPOINT_BASE = "https://push.eservice.emarsys.net/api/mobileengage/v2/";
    private static String ENDPOINT_LOGIN = ENDPOINT_BASE + "users/login";

    private final Application application;
    private final String applicationId;
    private final String applicationSecret;
    private final MobileEngageStatusListener statusListener;
    private final RequestManager manager;
    private final DeviceInfo deviceInfo;
    private final CoreCompletionHandler completionHandler;

    public MobileEngageInternal(Application application, MobileEngageConfig config, RequestManager manager) {
        this.application = application;
        this.applicationId = config.getApplicationID();
        this.applicationSecret = config.getApplicationSecret();
        this.statusListener = config.getStatusListener();

        this.manager = manager;
        initializeRequestManager(config.getApplicationID(), config.getApplicationSecret());

        this.deviceInfo = new DeviceInfo(application.getApplicationContext());

        this.completionHandler = new CoreCompletionHandler() {
            @Override
            public void onSuccess(String s, ResponseModel responseModel) {

            }

            @Override
            public void onError(String s, Exception e) {

            }
        };
    }

    private void initializeRequestManager(String id, String secret) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", HeaderUtils.createBasicAuth(id, secret));
        this.manager.setDefaultHeaders(headers);
    }

    public MobileEngageStatusListener getStatusListener() {
        return statusListener;
    }

    public CoreCompletionHandler getCompletionHandler() {
        return completionHandler;
    }

    public RequestManager getManager() {
        return manager;
    }

    public void setPushToken(String pushToken){

    }

    public void appLogin() {
        JSONObject payload;
        try {
            payload = new JSONObject()
                    .put("application_id", applicationId)
                    .put("hardware_id", deviceInfo.getHwid())
                    .put("platform", deviceInfo.getPlatform())
                    .put("language", deviceInfo.getLanguage())
                    .put("timezone", deviceInfo.getTimezone())
                    .put("device_model", deviceInfo.getModel())
                    .put("application_version", deviceInfo.getApplicationVersion())
                    .put("os_version", deviceInfo.getOsVersion())
                    .put("push_token", false);
        } catch (JSONException je) {
            throw new IllegalStateException(je);
        }

        RequestModel model = new RequestModel.Builder()
                .url(ENDPOINT_LOGIN)
                .payload(payload)
                .build();

        manager.submit(model, completionHandler);
    }

    public void appLogin(int contactField,
                         @NonNull String contactFieldValue) {
    }

    public void appLogout() {
    }

    public void trackCustomEvent(@NonNull String eventName,
                                 @Nullable JSONObject eventAttributes) {
    }
}