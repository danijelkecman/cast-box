package com.cxromos.castbox.ui.main;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.cxromos.castbox.R;
import com.cxromos.castbox.data.model.Cast;
import com.cxromos.castbox.ui.base.BaseActivity;
import com.cxromos.castbox.util.DialogFactory;
import com.cxromos.castbox.util.LocalUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends BaseActivity implements MainMvpView {

    @BindView(R.id.progress_indicator) ProgressBar mProgressBar;
    @BindView(R.id.recycler_casts) RecyclerView mCastRecycler;
    @BindView(R.id.swipe_container) SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.toolbar) Toolbar mToolbar;

    @Inject CastAdapter mCastAdapter;
    @Inject MainPresenter mMainPresenter;

    private List<Cast> mCasts;
    private String mCountryCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        setContentView(R.layout.activity_main);

        ButterKnife.setDebug(true);
        ButterKnife.bind(this);

        mCasts = new ArrayList<>();

        setupToolbar();
        setupRecyclerView();

        mMainPresenter.attachView(this);
        mMainPresenter.loadCasts(0, 20, mCountryCode, new ArrayList<>());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMainPresenter.detachView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        setupSpinnerInActionBar(menu);

        return true;
    }

    /***** MVP View methods implementation *****/

    @Override
    public void showCasts(List<Cast> casts) {
        mProgressBar.setVisibility(View.GONE);
        mSwipeRefresh.setRefreshing(false);
        mCasts.addAll(casts);
        mCastAdapter.setCasts(mCasts);
        if (mCasts.size() == casts.size()) {
            mCastAdapter.notifyDataSetChanged();
        } else {
            mCastAdapter.notifyItemInserted(mCasts.size() - 1);
        }
    }

    @Override
    public void showCastsEmpty() {
        mCastAdapter.setCasts(Collections.<Cast>emptyList());
        mCastAdapter.notifyDataSetChanged();
    }

    @Override
    public void showError() {
        mProgressBar.setVisibility(View.GONE);
        mSwipeRefresh.setRefreshing(false);
        DialogFactory.createSimpleErrorDialog(MainActivity.this).show();
    }

    /***** Setup View methods *****/

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
    }

    private void setupSpinnerInActionBar(Menu menu) {
        final List<String> countries = Arrays.asList(getResources().getStringArray(R.array.spinner_list_item_array));

        MenuItem item = menu.findItem(R.id.spinner);
        Spinner spinner = (Spinner) item.getActionView();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCountryCode = LocalUtils.getCountryCodeFromCountry(countries.get(position));
                if(mCasts != null && mCasts.size() > 0) {
                    mCasts.clear();
                }
                mProgressBar.setVisibility(View.VISIBLE);
                mMainPresenter.loadCasts(0, 20, mCountryCode, mCasts);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_list_item_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
    }

    private void setupRecyclerView() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mCastRecycler.setLayoutManager(layoutManager);
        mCastRecycler.setAdapter(mCastAdapter);
        mCastRecycler.addOnScrollListener(new CastRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                mMainPresenter.loadCasts(totalItemsCount, 10, mCountryCode, mCasts);
                Timber.d(String.valueOf(totalItemsCount));
            }
        });

        mSwipeRefresh.setColorSchemeResources(R.color.primary);
        mSwipeRefresh.setOnRefreshListener(() -> mMainPresenter.loadCasts(0, 20, mCountryCode, new ArrayList<>()));
    }
}
