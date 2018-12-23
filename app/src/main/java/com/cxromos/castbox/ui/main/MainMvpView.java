package com.cxromos.castbox.ui.main;

import com.cxromos.castbox.data.model.Cast;
import com.cxromos.castbox.ui.base.MvpView;

import java.util.List;

public interface MainMvpView extends MvpView {
    void showCasts(List<Cast> casts);
    void showCastsEmpty();
    void showError();
}
