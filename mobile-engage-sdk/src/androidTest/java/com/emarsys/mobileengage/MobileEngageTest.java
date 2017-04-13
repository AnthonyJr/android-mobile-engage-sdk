package com.emarsys.mobileengage;

import android.app.Application;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class MobileEngageTest {
    private static final String MESSAGE = "{\"google.sent_time\":1490799707291,\"onStart\":true,\"pw_msg\":\"1\",\"p\":\"<fI\",\"userdata\":{\"sid\":\"e6c_QAbjN4NMEio4\"},\"u\":\"{\\\"sid\\\":\\\"e6c_QAbjN4NMEio4\\\"}\",\"title\":\"aaaa\",\"google.message_id\":\"0:1490799707327870%2a5f08eef9fd7ecd\",\"foreground\":false}";

    private MobileEngageInternal mobileEngageInternal;
    private Application application;
    private MobileEngageConfig baseConfig;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Before
    public void init() {
        String appID = "56789876";
        String appSecret = "secret";

        application = mock(Application.class);
        when(application.getApplicationContext()).thenReturn(InstrumentationRegistry.getTargetContext());
        mobileEngageInternal = mock(MobileEngageInternal.class);
        baseConfig = new MobileEngageConfig.Builder()
                .credentials(appID, appSecret)
                .build();
    }

    @Test
    public void testSetup_initializesInstance() {
        MobileEngage.instance = null;
        MobileEngage.setup(application, baseConfig);

        assertNotNull(MobileEngage.instance);
    }

    @Test
    public void testSetPushToken_callsInternal() {
        String pushtoken = "pushtoken";
        MobileEngage.instance = mobileEngageInternal;
        MobileEngage.setPushToken(pushtoken);
        verify(mobileEngageInternal).setPushToken(pushtoken);
    }

    @Test
    public void testSetStatusListener_callsInternal() {
        MobileEngageStatusListener listener = mock(MobileEngageStatusListener.class);
        MobileEngage.instance = mobileEngageInternal;
        MobileEngage.setStatusListener(listener);
        verify(mobileEngageInternal).setStatusListener(listener);
    }

    @Test
    public void testAppLogin_anonymous_callsInternal() {
        MobileEngage.instance = mobileEngageInternal;
        MobileEngage.appLogin();
        verify(mobileEngageInternal).appLogin();
    }

    @Test
    public void testAppLogin_withUser_callsInternal() {
        MobileEngage.instance = mobileEngageInternal;
        MobileEngage.appLogin(4, "CONTACT_FIELD_VALUE");
        verify(mobileEngageInternal).appLogin(4, "CONTACT_FIELD_VALUE");
    }

    @Test
    public void testAppLogout_callsInternal() {
        MobileEngage.instance = mobileEngageInternal;
        MobileEngage.appLogout();
        verify(mobileEngageInternal).appLogout();
    }

    @Test
    public void testTrackCustomEvent_callsInternal() throws Exception {
        MobileEngage.instance = mobileEngageInternal;
        Map<String, String> attributes = mock(Map.class);
        MobileEngage.trackCustomEvent("event", attributes);
        verify(mobileEngageInternal).trackCustomEvent("event", attributes);
    }

    @Test
    public void testTrackMessageOpen_intent_callsInternal() {
        MobileEngage.instance = mobileEngageInternal;
        Intent intent = mock(Intent.class);
        MobileEngage.trackMessageOpen(intent);
        verify(mobileEngageInternal).trackMessageOpen(intent);
    }

    @Test
    public void testTrackMessageOpen_string_callsInternal() {
        MobileEngage.instance = mobileEngageInternal;
        MobileEngage.trackMessageOpen(MESSAGE);
        verify(mobileEngageInternal).trackMessageOpen(MESSAGE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetup_whenApplicationIsNull() {
        MobileEngage.setup(null, baseConfig);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetup_whenConfigIsNull() {
        MobileEngage.setup(application, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppLogin_whenContactFieldValueIsNull() {
        MobileEngage.appLogin(0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackCustomEvent_whenEventNameIsNull() {
        MobileEngage.trackCustomEvent(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackMessageOpen_intent_whenIntentIsNull() {
        MobileEngage.trackMessageOpen((Intent) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackMessageOpen_string_whenIntentIsNull() {
        MobileEngage.trackMessageOpen((String) null);
    }
}