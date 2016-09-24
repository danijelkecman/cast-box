package com.cxromos.castbox.injection.component;

import com.cxromos.castbox.injection.PerActivity;
import com.cxromos.castbox.injection.module.ActivityModule;
import com.cxromos.castbox.ui.base.BaseActivity;
import com.cxromos.castbox.ui.track.TrackActivity;
import com.cxromos.castbox.ui.main.MainActivity;

import dagger.Subcomponent;

/**
 * This component inject dependencies to all Activities across the application
 */
@PerActivity
@Subcomponent(modules = ActivityModule.class)
public interface ActivityComponent {

    void inject(BaseActivity baseActivity);
    void inject(MainActivity mainActivity);
    void inject(TrackActivity trackActivity);
}
