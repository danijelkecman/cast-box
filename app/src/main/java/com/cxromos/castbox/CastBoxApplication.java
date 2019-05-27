package com.cxromos.castbox;

import android.app.Application;
import android.content.Context;

import com.cxromos.castbox.injection.qualifiers.ConfigPersistent;
import com.cxromos.castbox.injection.component.ApplicationComponent;
import com.cxromos.castbox.injection.component.DaggerApplicationComponent;
import com.cxromos.castbox.injection.module.ApplicationModule;

import timber.log.Timber;

@ConfigPersistent
public class CastBoxApplication extends Application {

    ApplicationComponent mApplicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    public static CastBoxApplication get(Context context) {
        return (CastBoxApplication) context.getApplicationContext();
    }

    public ApplicationComponent getComponent() {
        if (mApplicationComponent == null) {
            mApplicationComponent = DaggerApplicationComponent.builder()
                    .applicationModule(new ApplicationModule(this))
                    .build();
        }
        return mApplicationComponent;
    }

    // Needed to replace the component with a test specific one
    public void setComponent(ApplicationComponent applicationComponent) {
        mApplicationComponent = applicationComponent;
    }
}
