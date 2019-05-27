package com.cxromos.castbox.injection.component;

import android.app.Application;
import android.content.Context;

import com.cxromos.castbox.CastBoxApplication;
import com.cxromos.castbox.data.DataManager;
import com.cxromos.castbox.injection.qualifiers.ApplicationContext;
import com.cxromos.castbox.injection.module.ApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {
    void inject(CastBoxApplication castBoxApplication);

    @ApplicationContext Context context();
    Application application();
    DataManager dataManager();
}
