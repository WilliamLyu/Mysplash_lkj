package com.wangdaye.mysplash.me.view.activity;

import android.annotation.SuppressLint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.wangdaye.mysplash.R;
import com.wangdaye.mysplash.common.data.entity.unsplash.User;
import com.wangdaye.mysplash.common.i.model.PagerManageModel;
import com.wangdaye.mysplash.common.i.model.UserModel;
import com.wangdaye.mysplash.common.i.presenter.PagerManagePresenter;
import com.wangdaye.mysplash.common.i.presenter.SwipeBackManagePresenter;
import com.wangdaye.mysplash.common.i.presenter.ToolbarPresenter;
import com.wangdaye.mysplash.common.i.presenter.UserPresenter;
import com.wangdaye.mysplash.common.i.view.PagerManageView;
import com.wangdaye.mysplash.common.i.view.PagerView;
import com.wangdaye.mysplash.common.i.view.SwipeBackManageView;
import com.wangdaye.mysplash.common.i.view.UserView;
import com.wangdaye.mysplash.common.basic.activity.MysplashActivity;
import com.wangdaye.mysplash.common.ui.adapter.MyPagerAdapter;
import com.wangdaye.mysplash.common.ui.widget.SwipeBackCoordinatorLayout;
import com.wangdaye.mysplash.common.ui.widget.coordinatorView.StatusBarView;
import com.wangdaye.mysplash.common.ui.widget.nestedScrollView.NestedScrollAppBarLayout;
import com.wangdaye.mysplash.common.utils.AnimUtils;
import com.wangdaye.mysplash.common.utils.BackToTopUtils;
import com.wangdaye.mysplash.common.utils.manager.AuthManager;
import com.wangdaye.mysplash.common.utils.manager.ThemeManager;
import com.wangdaye.mysplash.me.model.activity.PagerManageObject;
import com.wangdaye.mysplash.me.model.activity.UserObject;
import com.wangdaye.mysplash.me.model.widget.MyFollowObject;
import com.wangdaye.mysplash.me.presenter.activity.PagerManageImplementor;
import com.wangdaye.mysplash.me.presenter.activity.SwipeBackManageImplementor;
import com.wangdaye.mysplash.me.presenter.activity.ToolbarImplementor;
import com.wangdaye.mysplash.me.presenter.activity.UserImplementor;
import com.wangdaye.mysplash.me.view.widget.MyFollowUserView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * My follow activity.
 *
 * This activity is used to show followers for application user.
 *
 * */

public class MyFollowActivity extends MysplashActivity
        implements UserView, PagerManageView, SwipeBackManageView,
        View.OnClickListener, ViewPager.OnPageChangeListener,
        SwipeBackCoordinatorLayout.OnSwipeListener {

    @BindView(R.id.activity_my_follow_container)
    CoordinatorLayout container;

    @BindView(R.id.activity_my_follow_background)
    View background;

    @BindView(R.id.activity_my_follow_shadow)
    View shadow;

    @BindView(R.id.activity_my_follow_statusBar)
    StatusBarView statusBar;

    @BindView(R.id.activity_my_follow_appBar)
    NestedScrollAppBarLayout appBar;

    @BindView(R.id.activity_my_follow_viewPager)
    ViewPager viewPager;

    private PagerView[] pagers = new PagerView[2];

    private UserModel userModel;
    private UserPresenter userPresenter;

    private ToolbarPresenter toolbarPresenter;

    private PagerManageModel pagerManageModel;
    private PagerManagePresenter pagerManagePresenter;

    private SwipeBackManagePresenter swipeBackManagePresenter;

    public static final String KEY_MY_FOLLOW_ACTIVITY_PAGE_POSITION = "my_follow_activity_page_position";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_follow);
        initModel(savedInstanceState);
        initPresenter();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isStarted()) {
            setStarted();
            ButterKnife.bind(this);
            initView();
            userPresenter.requestUser();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userPresenter.getUser() != null) {
            User user = userPresenter.getUser();
            user.followers_count += ((MyFollowUserView) pagers[0]).getDeltaValue();
            user.following_count += ((MyFollowUserView) pagers[1]).getDeltaValue();
            if (AuthManager.getInstance().getUser() == null) {
                AuthManager.getInstance().updateUser(user);
            } else {
                User oldUser = AuthManager.getInstance().getUser();
                oldUser.followers_count = user.followers_count;
                oldUser.following_count = user.following_count;
                AuthManager.getInstance().updateUser(oldUser);
            }
        }
        for (PagerView p : pagers) {
            p.cancelRequest();
        }
    }

    @Override
    protected void setTheme() {
        if (ThemeManager.getInstance(this).isLightTheme()) {
            setTheme(R.style.MysplashTheme_light_Common);
        } else {
            setTheme(R.style.MysplashTheme_dark_Common);
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_MY_FOLLOW_ACTIVITY_PAGE_POSITION, pagerManagePresenter.getPagerPosition());
    }

    @Override
    public void handleBackPressed() {
        if (pagerManagePresenter.needPagerBackToTop()
                && BackToTopUtils.isSetBackToTop(false)) {
            backToTop();
        } else {
            finishSelf(true);
        }
    }

    @Override
    protected void backToTop() {
        BackToTopUtils.showTopBar(appBar, viewPager);
        pagerManagePresenter.pagerScrollToTop();
    }

    @Override
    public void finishSelf(boolean backPressed) {
        finish();
        if (backPressed) {
            overridePendingTransition(R.anim.none, R.anim.activity_slide_out);
        } else {
            overridePendingTransition(R.anim.none, R.anim.activity_fade_out);
        }
    }

    @Override
    public CoordinatorLayout getSnackbarContainer() {
        return container;
    }

    // init.

    private void initModel(Bundle savedInstanceState) {
        int page = 0;
        if (savedInstanceState != null) {
            page = savedInstanceState.getInt(KEY_MY_FOLLOW_ACTIVITY_PAGE_POSITION, page);
        } else {
            page = getIntent().getIntExtra(KEY_MY_FOLLOW_ACTIVITY_PAGE_POSITION, page);
        }
        this.userModel = new UserObject();
        this.pagerManageModel = new PagerManageObject(page);
    }

    private void initPresenter() {
        this.userPresenter = new UserImplementor(userModel, this);
        this.toolbarPresenter = new ToolbarImplementor();
        this.pagerManagePresenter = new PagerManageImplementor(pagerManageModel, this);
        this.swipeBackManagePresenter = new SwipeBackManageImplementor(this);
    }

    private void initView() {
        if (getBackground() != null) {
            background.setBackground(new BitmapDrawable(getResources(), getBackground()));
        }

        SwipeBackCoordinatorLayout swipeBackView = ButterKnife.findById(
                this, R.id.activity_my_follow_swipeBackView);
        swipeBackView.setOnSwipeListener(this);

        Toolbar toolbar = ButterKnife.findById(this, R.id.activity_my_follow_toolbar);
        toolbar.setTitle(getString(R.string.my_follow));
        ThemeManager.setNavigationIcon(
                toolbar, R.drawable.ic_toolbar_back_light, R.drawable.ic_toolbar_back_dark);
        toolbar.setNavigationOnClickListener(this);

        initPages();

        AnimUtils.translationYInitShow(
                (View) pagers[pagerManagePresenter.getPagerPosition()],
                400);
        for (PagerView p : pagers) {
            p.refreshPager();
        }
    }

    private void initPages() {
        List<View> pageList = new ArrayList<>();
        pageList.add(
                new MyFollowUserView(
                        this,
                        MyFollowObject.FOLLOW_TYPE_FOLLOWERS,
                        0, pagerManagePresenter.getPagerPosition() == 0));
        pageList.add(
                new MyFollowUserView(
                        this,
                        MyFollowObject.FOLLOW_TYPE_FOLLOWING,
                        1, pagerManagePresenter.getPagerPosition() == 1));
        for (int i = 0; i < pageList.size(); i ++) {
            pagers[i] = (PagerView) pageList.get(i);
        }

        String[] myFollowTabs = getResources().getStringArray(R.array.my_follow_tabs);

        List<String> tabList = new ArrayList<>();
        Collections.addAll(tabList, myFollowTabs);
        MyPagerAdapter adapter = new MyPagerAdapter(pageList, tabList);

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);
        viewPager.setCurrentItem(pagerManagePresenter.getPagerPosition(), false);

        TabLayout tabLayout = ButterKnife.findById(this, R.id.activity_my_follow_tabLayout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);
    }

    // interface.

    // on click listener.

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case -1:
                toolbarPresenter.touchNavigatorIcon(this);
                break;
        }
    }

    // on page change listener.

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // do nothing.
    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < pagers.length; i ++) {
            pagers[i].setSelected(i == position);
        }
        pagerManagePresenter.setPagerPosition(position);
        if (AuthManager.getInstance().getState() != AuthManager.LOADING_ME_STATE) {
            pagerManagePresenter.checkToRefresh(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // do nothing.
    }

    // on swipe listener.(swipe back listener)

    @Override
    public boolean canSwipeBack(int dir) {
        return swipeBackManagePresenter.checkCanSwipeBack(dir);
    }

    @Override
    public void onSwipeProcess(float percent) {
        statusBar.setAlpha(1 - percent);
        shadow.setAlpha(SwipeBackCoordinatorLayout.getBackgroundAlpha(percent));
    }

    @Override
    public void onSwipeFinish(int dir) {
        swipeBackManagePresenter.swipeBackFinish(this, dir);
    }

    // view.

    // user view.

    @Override
    public void drawUserInfo(User user) {
        String[] myFollowTabs = getResources().getStringArray(R.array.my_follow_tabs);
        MyPagerAdapter adapter = (MyPagerAdapter) viewPager.getAdapter();
        if (adapter != null) {
            adapter.titleList.set(0, user.followers_count + " " + myFollowTabs[0]);
            adapter.titleList.set(1, user.following_count + " " + myFollowTabs[1]);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void initRefreshStart() {
        // do nothing.
    }

    @Override
    public void followRequestSuccess(boolean follow) {
        // do nothing.
    }

    @Override
    public void followRequestFailed(boolean follow) {
        // do nothing.
    }

    // pager manage view.

    @Override
    public PagerView getPagerView(int position) {
        return pagers[position];
    }

    @Override
    public boolean canPagerSwipeBack(int position, int dir) {
        return pagers[position].canSwipeBack(dir);
    }

    @Override
    public int getPagerItemCount(int position) {
        return pagers[position].getItemCount();
    }

    // swipe back manage view.

    @Override
    public boolean checkCanSwipeBack(int dir) {
        if (dir == SwipeBackCoordinatorLayout.UP_DIR) {
            return pagerManagePresenter.canPagerSwipeBack(dir)
                    && appBar.getY() <= -appBar.getMeasuredHeight() + getResources().getDimensionPixelSize(R.dimen.tab_layout_height);
        } else {
            return pagerManagePresenter.canPagerSwipeBack(dir)
                    && appBar.getY() >= 0;
        }
    }
}
