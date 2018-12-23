package com.cxromos.castbox.ui.track;

import com.cxromos.castbox.data.DataManager;
import com.cxromos.castbox.data.model.Tracks;
import com.cxromos.castbox.injection.ConfigPersistent;
import com.cxromos.castbox.ui.base.BasePresenter;
import com.cxromos.castbox.util.RxUtil;

import javax.inject.Inject;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

@ConfigPersistent
public class TrackPresenter extends BasePresenter<TrackMvpView> {

    private final DataManager mDataManager;
    private Subscription mSubscription;

    @Inject
    public TrackPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    @Override
    public void attachView(TrackMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        if (mSubscription != null) mSubscription.unsubscribe();
    }

    void loadTracks(String key) {
        checkViewAttached();
        RxUtil.unsubscribe(mSubscription);
        mSubscription = mDataManager.getTracks(key)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Tracks>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "There was an error loading the tracks.");
                        getMvpView().showError();
                    }

                    @Override
                    public void onNext(Tracks tracks) {
                        if (tracks == null || tracks.list.isEmpty()) {
                            getMvpView().showTracksEmpty();
                        } else {
                            getMvpView().showTracks(tracks.list);
                        }
                    }
                });
    }
}
