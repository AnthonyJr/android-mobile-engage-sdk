package com.emarsys.mobileengage.config;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.MobileEngageStatusListener;

public class MobileEngageConfig {

    private final Application application;
    private final String applicationCode;
    private final String applicationPassword;
    private final MobileEngageStatusListener statusListener;
    private final boolean isDebugMode;
    private final boolean idlingResourceEnabled;
    private final OreoConfig oreoConfig;

    MobileEngageConfig(Application application,
                       String applicationCode,
                       String applicationPassword,
                       MobileEngageStatusListener statusListener,
                       boolean isDebugMode,
                       boolean idlingResourceEnabled,
                       OreoConfig oreoConfig) {
        Assert.notNull(application, "Application must not be null");
        Assert.notNull(applicationCode, "ApplicationCode must not be null");
        Assert.notNull(applicationPassword, "ApplicationPassword must not be null");
        Assert.notNull(oreoConfig, "OreoConfig must not be null");
        validate(oreoConfig);
        this.application = application;
        this.applicationCode = applicationCode;
        this.applicationPassword = applicationPassword;
        this.statusListener = statusListener;
        this.isDebugMode = isDebugMode;
        this.idlingResourceEnabled = idlingResourceEnabled;
        this.oreoConfig = oreoConfig;
    }

    public Application getApplication() {
        return application;
    }

    public String getApplicationCode() {
        return applicationCode;
    }

    public String getApplicationPassword() {
        return applicationPassword;
    }

    public MobileEngageStatusListener getStatusListener() {
        return statusListener;
    }

    public boolean isIdlingResourceEnabled() {
        return idlingResourceEnabled;
    }

    public boolean isDebugMode() {
        return isDebugMode;
    }

    public OreoConfig getOreoConfig() {
        return oreoConfig;
    }

    private void validate(OreoConfig oreoConfig) {
        if (oreoConfig.isDefaultChannelEnabled()) {
            Assert.notNull(oreoConfig.getDefaultChannelName(), "DefaultChannelName must not be null");
            Assert.notNull(oreoConfig.getDefaultChannelDescription(), "DefaultChannelDescription must not be null");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MobileEngageConfig that = (MobileEngageConfig) o;

        if (isDebugMode != that.isDebugMode) return false;
        if (idlingResourceEnabled != that.idlingResourceEnabled) return false;
        if (application != null ? !application.equals(that.application) : that.application != null)
            return false;
        if (applicationCode != null ? !applicationCode.equals(that.applicationCode) : that.applicationCode != null)
            return false;
        if (applicationPassword != null ? !applicationPassword.equals(that.applicationPassword) : that.applicationPassword != null)
            return false;
        if (statusListener != null ? !statusListener.equals(that.statusListener) : that.statusListener != null)
            return false;
        return oreoConfig != null ? oreoConfig.equals(that.oreoConfig) : that.oreoConfig == null;

    }

    @Override
    public int hashCode() {
        int result = application != null ? application.hashCode() : 0;
        result = 31 * result + (applicationCode != null ? applicationCode.hashCode() : 0);
        result = 31 * result + (applicationPassword != null ? applicationPassword.hashCode() : 0);
        result = 31 * result + (statusListener != null ? statusListener.hashCode() : 0);
        result = 31 * result + (isDebugMode ? 1 : 0);
        result = 31 * result + (idlingResourceEnabled ? 1 : 0);
        result = 31 * result + (oreoConfig != null ? oreoConfig.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MobileEngageConfig{" +
                "application=" + application +
                ", applicationCode='" + applicationCode + '\'' +
                ", applicationPassword='" + applicationPassword + '\'' +
                ", statusListener=" + statusListener +
                ", isDebugMode=" + isDebugMode +
                ", idlingResourceEnabled=" + idlingResourceEnabled +
                ", oreoConfig=" + oreoConfig +
                '}';
    }

    public static class Builder {
        private Application application;
        private String applicationCode;
        private String applicationPassword;
        private MobileEngageStatusListener statusListener;
        private boolean idlingResourceEnabled;
        private OreoConfig oreoConfig;

        public Builder from(MobileEngageConfig baseConfig) {
            Assert.notNull(baseConfig, "BaseConfig must not be null");
            application = baseConfig.getApplication();
            applicationCode = baseConfig.getApplicationCode();
            applicationPassword = baseConfig.getApplicationPassword();
            statusListener = baseConfig.getStatusListener();
            idlingResourceEnabled = baseConfig.isIdlingResourceEnabled();
            oreoConfig = baseConfig.getOreoConfig();
            return this;
        }

        public Builder application(@NonNull Application application) {
            this.application = application;
            return this;
        }

        public Builder credentials(@NonNull String applicationCode,
                                   @NonNull String applicationPassword) {
            this.applicationCode = applicationCode;
            this.applicationPassword = applicationPassword;
            return this;
        }

        public Builder statusListener(@NonNull MobileEngageStatusListener statusListener) {
            this.statusListener = statusListener;
            return this;
        }

        public Builder enableIdlingResource(boolean enabled) {
            idlingResourceEnabled = enabled;
            return this;
        }

        public Builder enableDefaultChannel(String name, String description) {
            this.oreoConfig = new OreoConfig(true, name, description);
            return this;
        }

        public Builder disableDefaultChannel() {
            this.oreoConfig = new OreoConfig(false);
            return this;
        }

        public MobileEngageConfig build() {
            boolean isDebuggable = (0 != (application.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));

            return new MobileEngageConfig(
                    application,
                    applicationCode,
                    applicationPassword,
                    statusListener,
                    isDebuggable,
                    idlingResourceEnabled,
                    oreoConfig);
        }
    }
}