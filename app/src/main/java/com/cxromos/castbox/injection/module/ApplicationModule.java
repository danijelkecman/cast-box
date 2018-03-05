package com.cxromos.castbox.injection.module;

import android.app.Application;
import android.content.Context;

import com.cxromos.castbox.data.remote.CastBoxService;
import com.cxromos.castbox.injection.ApplicationContext;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Provide application-level dependencies. Mainly singleton object that can be injected from
 * anywhere in the app.
 */
@Module
public class ApplicationModule {
    protected final Application mApplication;

    public ApplicationModule(Application mApplication) {
        this.mApplication = mApplication;
    }

    @Provides
    Application provideApplication() { return mApplication; }

    @Provides
    @ApplicationContext
    Context provideContext() { return mApplication; }

    @Provides
    @Singleton
    CastBoxService provideCastBoxService() {
        return CastBoxService.Factory.makeChatBoxService(mApplication);
    }
}
