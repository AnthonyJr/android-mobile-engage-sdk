package com.emarsys.mobileengage.inbox;

import android.app.Application;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.DeviceInfo;
import com.emarsys.core.request.RequestMethod;
import com.emarsys.core.request.RequestModel;
import com.emarsys.core.request.RestClient;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.HeaderUtils;
import com.emarsys.mobileengage.AppLoginParameters;
import com.emarsys.mobileengage.MobileEngageConfig;
import com.emarsys.mobileengage.MobileEngageException;
import com.emarsys.mobileengage.fake.FakeInboxResultListener;
import com.emarsys.mobileengage.fake.FakeRestClient;
import com.emarsys.mobileengage.inbox.model.Notification;
import com.emarsys.mobileengage.inbox.model.NotificationInboxStatus;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.emarsys.mobileengage.fake.FakeInboxResultListener.Mode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class InboxInternalTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    private static List<Notification> notificationList;

    private InboxInternal inbox;
    private CountDownLatch latch;
    private InboxResultListener<NotificationInboxStatus> resultListenerMock;
    private MobileEngageConfig config;

    private AppLoginParameters appLoginParameters_withCredentials;
    private AppLoginParameters appLoginParameters_noCredentials;
    private AppLoginParameters appLoginParameters_missing;

    @Before
    @SuppressWarnings("unchecked")
    public void init() throws JSONException {
        latch = new CountDownLatch(1);

        notificationList = createNotificationList();
        config = new MobileEngageConfig.Builder()
                .application((Application) InstrumentationRegistry.getTargetContext().getApplicationContext())
                .credentials("applicationCode", "applicationPassword")
                .build();
        inbox = new InboxInternal(config);

        resultListenerMock = mock(InboxResultListener.class);
        appLoginParameters_withCredentials = new AppLoginParameters(30, "value");
        appLoginParameters_noCredentials = new AppLoginParameters();
        appLoginParameters_missing = null;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_mobileEngageInternal_shouldNotBeNull() {
        inbox = new InboxInternal(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFetchNotifications_listenerShouldNotBeNull() {
        inbox.fetchNotifications(null);
    }

    @Test
    public void testFetchNotifications_shouldMakeRequest_viaRestClient() {
        DeviceInfo deviceInfo = new DeviceInfo(InstrumentationRegistry.getContext());

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", HeaderUtils.createBasicAuth(config.getApplicationCode(), config.getApplicationPassword()));
        headers.put("x-ems-me-hardware-id", deviceInfo.getHwid());
        headers.put("x-ems-me-application-code", config.getApplicationCode());
        headers.put("x-ems-me-contactfield-id", String.valueOf(appLoginParameters_withCredentials.getContactFieldId()));
        headers.put("x-ems-me-contactfield-value", appLoginParameters_withCredentials.getContactFieldValue());

        RequestModel expected = new RequestModel.Builder()
                .url("https://me-inbox.eservice.emarsys.net/api/notifications")
                .headers(headers)
                .method(RequestMethod.GET)
                .build();

        RestClient mockRestClient = mock(RestClient.class);
        inbox.client = mockRestClient;

        inbox.setAppLoginParameters(appLoginParameters_withCredentials);
        inbox.fetchNotifications(resultListenerMock);

        ArgumentCaptor<RequestModel> requestCaptor = ArgumentCaptor.forClass(RequestModel.class);
        verify(mockRestClient).execute(requestCaptor.capture(), any(CoreCompletionHandler.class));

        RequestModel requestModel = requestCaptor.getValue();
        Assert.assertNotNull(requestModel.getId());
        Assert.assertNotNull(requestModel.getTimestamp());
        Assert.assertEquals(expected.getUrl(), requestModel.getUrl());
        Assert.assertEquals(expected.getHeaders(), requestModel.getHeaders());
        Assert.assertEquals(expected.getMethod(), requestModel.getMethod());
    }

    @Test
    public void testFetchNotifications_listener_success() throws InterruptedException {
        inbox.setAppLoginParameters(appLoginParameters_withCredentials);

        inbox.client = new FakeRestClient(createSuccessResponse(), FakeRestClient.Mode.SUCCESS);

        FakeInboxResultListener listener = new FakeInboxResultListener(latch);
        inbox.fetchNotifications(listener);

        latch.await();

        Assert.assertEquals(new NotificationInboxStatus(notificationList, 300), listener.resultStatus);
        Assert.assertEquals(1, listener.successCount);
    }

    @Test
    public void testFetchNotifications_listener_success_shouldBeCalledOnMainThread() throws InterruptedException {
        inbox.setAppLoginParameters(appLoginParameters_withCredentials);

        inbox.client = new FakeRestClient(createSuccessResponse(), FakeRestClient.Mode.SUCCESS);

        FakeInboxResultListener listener = new FakeInboxResultListener(latch, Mode.MAIN_THREAD);
        inbox.fetchNotifications(listener);

        latch.await();

        Assert.assertEquals(1, listener.successCount);
    }

    @Test
    public void testFetchNotifications_listener_failureWithException() throws InterruptedException {
        inbox.setAppLoginParameters(appLoginParameters_withCredentials);

        Exception expectedException = new Exception("FakeRestClientException");
        inbox.client = new FakeRestClient(expectedException);

        FakeInboxResultListener listener = new FakeInboxResultListener(latch);
        inbox.fetchNotifications(listener);

        latch.await();

        Assert.assertEquals(expectedException, listener.errorCause);
        Assert.assertEquals(1, listener.errorCount);
    }

    @Test
    public void testFetchNotifications_listener_failureWithException_shouldBeCalledOnMainThread() throws InterruptedException {
        inbox.setAppLoginParameters(appLoginParameters_withCredentials);

        inbox.client = new FakeRestClient(new Exception());

        FakeInboxResultListener listener = new FakeInboxResultListener(latch, Mode.MAIN_THREAD);
        inbox.fetchNotifications(listener);

        latch.await();

        Assert.assertEquals(1, listener.errorCount);
    }

    @Test
    public void testFetchNotification_listener_failureWithResponseModel() throws InterruptedException {
        inbox.setAppLoginParameters(appLoginParameters_withCredentials);

        ResponseModel responseModel = new ResponseModel.Builder().statusCode(400).message("Bad request").build();
        inbox.client = new FakeRestClient(responseModel, FakeRestClient.Mode.ERROR_RESPONSE_MODEL);

        FakeInboxResultListener listener = new FakeInboxResultListener(latch);
        inbox.fetchNotifications(listener);

        latch.await();

        MobileEngageException expectedException = new MobileEngageException(
                responseModel.getStatusCode(),
                responseModel.getMessage(),
                responseModel.getBody());

        MobileEngageException resultException = (MobileEngageException) listener.errorCause;
        Assert.assertEquals(expectedException.getStatusCode(), resultException.getStatusCode());
        Assert.assertEquals(expectedException.getMessage(), resultException.getMessage());
        Assert.assertEquals(expectedException.getBody(), resultException.getBody());
        Assert.assertEquals(1, listener.errorCount);
    }

    @Test
    public void testFetchNotification_listener_failureWithResponseModel_shouldBeCalledOnMainThread() throws InterruptedException {
        inbox.setAppLoginParameters(appLoginParameters_withCredentials);

        ResponseModel responseModel = new ResponseModel.Builder().statusCode(400).message("Bad request").build();
        inbox.client = new FakeRestClient(responseModel, FakeRestClient.Mode.ERROR_RESPONSE_MODEL);

        FakeInboxResultListener listener = new FakeInboxResultListener(latch, Mode.MAIN_THREAD);
        inbox.fetchNotifications(listener);

        latch.await();

        Assert.assertEquals(1, listener.errorCount);
    }

    @Test
    public void testFetchNotification_listener_failureWithParametersNotSet() throws InterruptedException {
        inbox.setAppLoginParameters(appLoginParameters_missing);

        FakeInboxResultListener listener = new FakeInboxResultListener(latch);
        inbox.fetchNotifications(listener);

        latch.await();

        Assert.assertEquals(NotificationInboxException.class, listener.errorCause.getClass());
        Assert.assertEquals(1, listener.errorCount);
    }

    @Test
    public void testFetchNotification_listener_failureWithParametersNotSet_shouldBeCalledOnMainThread() throws InterruptedException {
        inbox.setAppLoginParameters(appLoginParameters_missing);

        FakeInboxResultListener listener = new FakeInboxResultListener(latch, Mode.MAIN_THREAD);
        inbox.fetchNotifications(listener);

        latch.await();

        Assert.assertEquals(1, listener.errorCount);
    }

    @Test
    public void testFetchNotification_listener_failureWithParametersSet_butLacksCredentials() throws InterruptedException {
        inbox.setAppLoginParameters(appLoginParameters_noCredentials);

        FakeInboxResultListener listener = new FakeInboxResultListener(latch);
        inbox.fetchNotifications(listener);

        latch.await();

        Assert.assertEquals(NotificationInboxException.class, listener.errorCause.getClass());
        Assert.assertEquals(1, listener.errorCount);
    }

    @Test
    public void testFetchNotification_listener_failureWithParametersSet_butLacksCredentials_shouldBeCalledOnMainThread() throws InterruptedException {
        inbox.setAppLoginParameters(appLoginParameters_noCredentials);

        FakeInboxResultListener listener = new FakeInboxResultListener(latch, Mode.MAIN_THREAD);
        inbox.fetchNotifications(listener);

        latch.await();

        Assert.assertEquals(1, listener.errorCount);
    }

    private List<Notification> createNotificationList() throws JSONException {
        Map<String, String> customData1 = new HashMap<>();
        customData1.put("data1", "dataValue1");
        customData1.put("data2", "dataValue2");

        JSONObject rootParams1 = new JSONObject();
        rootParams1.put("param1", "paramValue1");
        rootParams1.put("param2", "paramValue2");

        Map<String, String> customData2 = new HashMap<>();
        customData2.put("data3", "dataValue3");
        customData2.put("data4", "dataValue4");

        JSONObject rootParams2 = new JSONObject();
        rootParams2.put("param3", "paramValue3");
        rootParams2.put("param4", "paramValue4");


        Map<String, String> customData3 = new HashMap<>();
        customData3.put("data5", "dataValue5");
        customData3.put("data6", "dataValue6");

        JSONObject rootParams3 = new JSONObject();
        rootParams3.put("param5", "paramValue5");
        rootParams3.put("param6", "paramValue6");

        return Arrays.asList(
                new Notification("id1", "title1", customData1, rootParams1, 300, new Date(10000000)),
                new Notification("id2", "title2", customData2, rootParams2, 200, new Date(30000000)),
                new Notification("id3", "title3", customData3, rootParams3, 100, new Date(25000000))

        );
    }

    private ResponseModel createSuccessResponse() {
        String notificationString1 = "{" +
                "\"id\":\"id1\", " +
                "\"title\":\"title1\", " +
                "\"custom_data\": {" +
                "\"data1\":\"dataValue1\"," +
                "\"data2\":\"dataValue2\"" +
                "}," +
                "\"root_params\": {" +
                "\"param1\":\"paramValue1\"," +
                "\"param2\":\"paramValue2\"" +
                "}," +
                "\"expiration_time\": 300, " +
                "\"received_at\":10000000" +
                "}";

        String notificationString2 = "{" +
                "\"id\":\"id2\", " +
                "\"title\":\"title2\", " +
                "\"custom_data\": {" +
                "\"data3\":\"dataValue3\"," +
                "\"data4\":\"dataValue4\"" +
                "}," +
                "\"root_params\": {" +
                "\"param3\":\"paramValue3\"," +
                "\"param4\":\"paramValue4\"" +
                "}," +
                "\"expiration_time\": 200, " +
                "\"received_at\":30000000" +
                "}";

        String notificationString3 = "{" +
                "\"id\":\"id3\", " +
                "\"title\":\"title3\", " +
                "\"custom_data\": {" +
                "\"data5\":\"dataValue5\"," +
                "\"data6\":\"dataValue6\"" +
                "}," +
                "\"root_params\": {" +
                "\"param5\":\"paramValue5\"," +
                "\"param6\":\"paramValue6\"" +
                "}," +
                "\"expiration_time\": 100, " +
                "\"received_at\":25000000" +
                "}";

        String json = "{\"badge_count\": 300, \"notifications\": [" + notificationString1 + "," + notificationString2 + "," + notificationString3 + "]}";

        return new ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body(json)
                .build();
    }
}