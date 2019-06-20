package me.jason.imagepicker.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseLazyFragment extends Fragment {
    /**
     * 缓存View，不需要每次都初始化
     */
    private View rootView;
    /**
     * Fragment当前状态是否可见
     */
    protected boolean isVisible;
    /**
     * Fragment是否初始化视图完毕
     */
    protected boolean isPrepared = false;
    /**
     * Fragment是否第一次加载数据
     */
    protected boolean isFirst = true;
    /**
     * Fragment是否初始化view配置
     */
    private boolean isInitView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            isInitView = true;
            rootView = initView(inflater, container, savedInstanceState);
        } else {
            isInitView = false;
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null) {
                parent.removeView(rootView);
            }
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isPrepared = true;
        onVisible();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInvisible();
        }
    }


    /**
     * 初始化视图
     */
    protected abstract View initView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    /**
     * 初始化控件的配置
     */
    protected abstract void initViewConfig(ViewGroup rootView);

    /**
     * 初始化数据
     */
    protected abstract void initData();

    /**
     * 视图不可见时候的回调
     */
    private void onInvisible() {
        //do nothing
    }

    /**
     * 视图可见时候的回调
     */
    private void onVisible() {
        if (!isPrepared || !isVisible || !isFirst) {
            return;
        }
        initViewConfig((ViewGroup) rootView);
        initData();
        isFirst = false;
    }
}
