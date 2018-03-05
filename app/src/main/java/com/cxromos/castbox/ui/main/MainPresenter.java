package com.cxromos.castbox.ui.main;

import com.cxromos.castbox.data.DataManager;
import com.cxromos.castbox.data.model.Cast;
import com.cxromos.castbox.data.model.Casts;
import com.cxromos.castbox.injection.ConfigPersistent;
import com.cxromos.castbox.ui.base.BasePresenter;
import com.cxromos.castbox.util.RxUtil;

import java.util.List;

import javax.inject.Inject;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

@ConfigPersistent
public class MainPresenter extends BasePresenter<MainMvpView> {

    private final DataManager mDataManager;
    private Subscription mSubscription;

    @Inject
    public MainPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    @Override
    public void attachView(MainMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        if (mSubscription != null) mSubscription.unsubscribe();
    }

    public void loadCasts(int skip, int take, String countryCode, final List<Cast> casts) {
        checkViewAttached();
        RxUtil.unsubscribe(mSubscription);
        if (countryCode == null || countryCode.isEmpty()) {
            countryCode = "us";
        }
        mSubscription = mDataManager.getMedia(countryCode, skip, take)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Casts>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "There was an error loading the casts.");
                        getMvpView().showError();
                    }

                    @Override
                    public void onNext(Casts retrievedCasts) {
                        if (retrievedCasts == null || retrievedCasts.list.isEmpty()) {
                            getMvpView().showCastsEmpty();
                        } else {
                            casts.addAll(retrievedCasts.list);
                            getMvpView().showCasts(casts);
                        }
                    }
                });
    }
}