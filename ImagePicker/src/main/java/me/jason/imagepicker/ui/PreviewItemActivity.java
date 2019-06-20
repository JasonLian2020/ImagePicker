package me.jason.imagepicker.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.shuyu.gsyvideoplayer.GSYVideoManager;

import java.util.ArrayList;
import java.util.List;

import me.jason.imagepicker.IntentHub;
import me.jason.imagepicker.R;
import me.jason.imagepicker.internal.entity.Album;
import me.jason.imagepicker.internal.entity.Item;
import me.jason.imagepicker.internal.entity.SelectionSpec;
import me.jason.imagepicker.internal.model.AlbumMediaCollection;
import me.jason.imagepicker.internal.model.SelectedItemCollection;
import me.jason.imagepicker.ui.adapter.PreviewPagerAdapter;
import me.jason.imagepicker.ui.adapter.SelectedItemAdapter;
import me.jason.imagepicker.utils.CursorUtils;
import me.jason.imagepicker.utils.ThreadUtils;

public class PreviewItemActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private static final String EXTRA_ALBUM = "extra_album";
    private static final String EXTRA_ITEM = "extra_item";

    private static final int UPDATE_TYPE_INIT = 1;
    private static final int UPDATE_TYPE_ADD = 2;
    private static final int UPDATE_TYPE_REMOVE = 3;
    private static final int UPDATE_TYPE_SELECT = 4;

    private ViewPager viewPager;
    //top
    private View previewTopLayout;
    private ImageView previewClose;
    private TextView previewChoose;
    private TextView previewTips;
    // bottom
    private View previewBottomLayout;
    private RecyclerView recyclerView;
    private Button previewImageCompleted;
    private LinearLayout previewVideoLayout;
    private TextView current;
    private TextView total;
    private SeekBar progress;
    private Button previewVideoCompleted;

    private Album album;
    private Item item;

    private final AlbumMediaCollection mAlbumMediaCollection = new AlbumMediaCollection();
    private SelectedItemAdapter adapter;
    private PreviewPagerAdapter pagerAdapter;
    /**
     * 记录上一次的位置
     */
    private int prePosition = -1;

    public static void startForResult(Activity activity, Album album, Item item, int RequestCode) {
        Intent intent = new Intent(activity, PreviewItemActivity.class);
        intent.putExtra(EXTRA_ALBUM, album);
        intent.putExtra(EXTRA_ITEM, item);
        activity.startActivityForResult(intent, RequestCode);
        activity.overridePendingTransition(R.anim.popup_up_in, R.anim.popup_up_out);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.popup_down_in, R.anim.popup_down_out);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_item);
        //解析Intent
        parseIntent();
        //初始化view
        initView();
        //初始化数据
        updateView(UPDATE_TYPE_INIT);
        //获取图片视频集合
        mAlbumMediaCollection.onCreate(this, new AlbumMediaCollection.AlbumMediaCallbacks() {
            @Override
            public void onAlbumMediaLoad(Cursor cursor) {
                Log.d("jason", PreviewItemActivity.class.getSimpleName() + ": onAlbumMediaLoad");
                List<Item> itemList = CursorUtils.getAllItem(cursor);
                if (ThreadUtils.isMainThread()) {
                    updateUIByInit(itemList);
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> updateUIByInit(itemList));
                }
            }

            @Override
            public void onAlbumMediaReset() {
                Log.d("jason", PreviewItemActivity.class.getSimpleName() + ": onAlbumMediaReset");
                if (ThreadUtils.isMainThread()) {
                    updateUIByReset();
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> updateUIByReset());
                }
            }
        });
        mAlbumMediaCollection.load(album);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();
        viewPager.removeOnPageChangeListener(this);
        mAlbumMediaCollection.onDestroy();
    }

    private void parseIntent() {
        if (getIntent() == null) return;
        album = getIntent().getParcelableExtra(EXTRA_ALBUM);
        item = getIntent().getParcelableExtra(EXTRA_ITEM);
    }

    private void initView() {
        viewPager = findViewById(R.id.viewPager);
        pagerAdapter = new PreviewPagerAdapter(getSupportFragmentManager(), null);
        viewPager.addOnPageChangeListener(this);
        viewPager.setAdapter(pagerAdapter);
        //top
        previewTopLayout = findViewById(R.id.previewTopLayout);
        previewClose = findViewById(R.id.previewClose);
        previewClose.setOnClickListener(v -> onBackPressed());
        previewChoose = findViewById(R.id.previewChoose);
        previewChoose.setOnClickListener(v -> clickPreviewChoose());
        previewTips = findViewById(R.id.previewTips);
        //bottom
        previewBottomLayout = findViewById(R.id.previewBottomLayout);
        initRecyclerView();
        previewImageCompleted = findViewById(R.id.previewImageCompleted);
        previewImageCompleted.setOnClickListener(v -> clickImageCompleted());
        previewVideoLayout = findViewById(R.id.previewVideoLayout);
        current = findViewById(R.id.current);
        total = findViewById(R.id.total);
        progress = findViewById(R.id.progress);
        previewVideoCompleted = findViewById(R.id.previewVideoCompleted);
        previewVideoCompleted.setOnClickListener(v -> clickVideoCompleted());
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayout.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SelectedItemAdapter(null);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            Item clickItem = (Item) adapter.getItem(position);
            if (clickItem == null || clickItem.equals(item)) return;
            // 切换页面
            List<Item> itemList = pagerAdapter.getItemList();
            int index = itemList.indexOf(clickItem);
            viewPager.setCurrentItem(index);
            // 切换选中项
            this.adapter.setSelectedItem(clickItem);
            this.adapter.notifyDataSetChanged();
        });
        recyclerView.setAdapter(adapter);
    }

    private void updateView(int updateType) {
        //处理顶部显示逻辑
        processTopLayout(updateType);
        //处理底部显示逻辑
        processBottomLayout(updateType);
    }

    private void updateUIByInit(List<Item> itemList) {
        // 刷新UI
        pagerAdapter.addAll(itemList);
        pagerAdapter.notifyDataSetChanged();
        int selectedIndex = itemList.indexOf(item);
        viewPager.setCurrentItem(selectedIndex, false);
        prePosition = selectedIndex;
        // 用完就释放掉
        mAlbumMediaCollection.onDestroy();
    }

    private void updateUIByReset() {
        // do nothing
    }

    private void updateUIBySelect(int position) {
        // 赋值
        PreviewItemFragment fragment = (PreviewItemFragment) pagerAdapter.instantiateItem(viewPager, position);
        item = fragment.getItem();
        fragment.restartInitView();
        // 更新UI
        updateView(UPDATE_TYPE_SELECT);
        // 如果遮罩隐藏状态，需要显示
        showLayout(true);
    }

    private void clickPreviewChoose() {
        if (previewChoose.isSelected()) {
            SelectedItemCollection.getInstance().remove(item);
            updateView(UPDATE_TYPE_REMOVE);
        } else {
            int count = SelectedItemCollection.getInstance().count();
            int maxSelectable = SelectionSpec.getInstance().maxSelectable;
            if (count >= maxSelectable) {
                //不能再选，已经达到限制了
                ToastUtils.showShort(R.string.error_over_count, count);
            } else {
                SelectedItemCollection.getInstance().add(item);
                updateView(UPDATE_TYPE_ADD);
            }
        }
    }

    private void clickImageCompleted() {
        int count = SelectedItemCollection.getInstance().count();
        ArrayList<Item> itemList;
        if (count > 0) {
            itemList = SelectedItemCollection.getInstance().asList();
        } else {
            itemList = new ArrayList<>();
            itemList.add(item);
        }
        sendResult(itemList, IntentHub.FROM_IMAGE);
    }

    private void clickVideoCompleted() {
        ArrayList<Item> itemList = new ArrayList<>();
        itemList.add(item);
        sendResult(itemList, IntentHub.FROM_VDIEO);
    }

    private void sendResult(ArrayList<Item> selectedItems, int from) {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(IntentHub.EXTRA_RESULT_SELECTED_ITEM, selectedItems);
        intent.putExtra(IntentHub.EXTRA_RESULT_FROM, from);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void processTopLayout(int updateType) {
        updateViewByChoose();
        updateViewByTips();
    }

    private void processBottomLayout(int updateType) {
        updateViewByImageLayout(updateType);
        updateViewByVideoLayout();
    }

    private void updateViewByChoose() {
        if (item.isImage()) {
            previewChoose.setVisibility(View.VISIBLE);
            int count = SelectedItemCollection.getInstance().count();
            int checkedNum = SelectedItemCollection.getInstance().checkedNumOf(item);
            if (count > 0 && checkedNum > 0) {
                previewChoose.setSelected(true);
                previewChoose.setText(String.valueOf(checkedNum));
            } else {
                previewChoose.setSelected(false);
                previewChoose.setText("");
            }
        } else {
            previewChoose.setVisibility(View.GONE);
        }
    }

    private void updateViewByTips() {
        if (item.isImage()) {
            previewTips.setVisibility(View.GONE);
        } else {
            int count = SelectedItemCollection.getInstance().count();
            if (count > 0) {
                previewTips.setVisibility(View.VISIBLE);
                previewTips.setText(R.string.preview_item_video_tips1);
            } else {
                int minSecond = 3;
                int curSecond = (int) (item.duration / 1000);
                previewTips.setVisibility(curSecond > minSecond ? View.GONE : View.VISIBLE);
                previewTips.setText(curSecond > minSecond ? "" : getString(R.string.preview_item_video_tips2, curSecond));
            }
        }
    }

    private void updateViewByImageLayout(int updateType) {
        if (item.isImage()) {
            // 内容列表
            updateSeletedList(updateType);
            // 完成按钮
            if (previewImageCompleted.getVisibility() != View.VISIBLE)
                previewImageCompleted.setVisibility(View.VISIBLE);
            int count = SelectedItemCollection.getInstance().count();
            if (count > 0) {
                previewImageCompleted.setText(getString(R.string.preview_item_completed_btn_text2, count));
            } else {
                previewImageCompleted.setText(R.string.preview_item_completed_btn_text1);
            }
        } else {
            // 内容列表
            if (recyclerView.getVisibility() != View.GONE)
                recyclerView.setVisibility(View.GONE);
            // 完成按钮
            if (previewImageCompleted.getVisibility() != View.GONE)
                previewImageCompleted.setVisibility(View.GONE);
        }
    }

    private void updateSeletedList(int updateType) {
        int count = SelectedItemCollection.getInstance().count();
        if (count > 0) {
            if (recyclerView.getVisibility() != View.VISIBLE)
                recyclerView.setVisibility(View.VISIBLE);
        } else {
            if (recyclerView.getVisibility() != View.GONE)
                recyclerView.setVisibility(View.GONE);
        }
        switch (updateType) {
            case UPDATE_TYPE_INIT:
                adapter.setSelectedItem(item);
                adapter.setNewData(SelectedItemCollection.getInstance().asList());
                break;
            case UPDATE_TYPE_ADD:
                adapter.addData(item);
                break;
            case UPDATE_TYPE_REMOVE:
                List<Item> items = adapter.getData();
                adapter.remove(items.indexOf(item));
                break;
            case UPDATE_TYPE_SELECT:
                adapter.setSelectedItem(item);
                adapter.notifyDataSetChanged();
                break;
        }
    }

    private void updateViewByVideoLayout() {
        if (item.isImage()) {
            if (previewVideoLayout.getVisibility() != View.GONE)
                previewVideoLayout.setVisibility(View.GONE);
            if (previewVideoCompleted.getVisibility() != View.GONE)
                previewVideoCompleted.setVisibility(View.GONE);
        } else {
            if (previewVideoLayout.getVisibility() != View.VISIBLE)
                previewVideoLayout.setVisibility(View.VISIBLE);
            if (previewVideoCompleted.getVisibility() != View.VISIBLE)
                previewVideoCompleted.setVisibility(View.VISIBLE);
            int count = SelectedItemCollection.getInstance().count();
            previewVideoCompleted.setEnabled(count <= 0);
        }
    }

    /**
     * 记录是否隐藏遮罩
     * <P>true表示显示状态，去执行隐藏；false表示隐藏状态，去执行显示</P>
     */
    private boolean isHideToolbar = true;

    /**
     * 自动隐藏遮罩
     */
    public void autoHideToolbar() {
        if (isHideToolbar) {
            //当前为显示状态，需要去执行隐藏动画
            previewTopLayout.animate()
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .translationYBy(-previewTopLayout.getMeasuredHeight())
                    .start();
            previewBottomLayout.animate()
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .translationYBy(previewBottomLayout.getMeasuredHeight())
                    .start();
        } else {
            //当前为隐藏状态，需要去执行显示动画
            previewTopLayout.animate()
                    .translationYBy(previewTopLayout.getMeasuredHeight())
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .start();
            previewBottomLayout.animate()
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .translationYBy(-previewBottomLayout.getMeasuredHeight())
                    .start();
        }
        isHideToolbar = !isHideToolbar;
    }

    public void showLayout() {
        showLayout(previewTopLayout.getVisibility() != View.VISIBLE);
    }

    public void showLayout(boolean isShow) {
        if (isShow) {
            if (previewTopLayout.getVisibility() != View.VISIBLE)
                previewTopLayout.setVisibility(View.VISIBLE);
            if (previewBottomLayout.getVisibility() != View.VISIBLE)
                previewBottomLayout.setVisibility(View.VISIBLE);
        } else {
            if (previewTopLayout.getVisibility() != View.GONE)
                previewTopLayout.setVisibility(View.GONE);
            if (previewBottomLayout.getVisibility() != View.GONE)
                previewBottomLayout.setVisibility(View.GONE);
        }
    }

    public TextView getCurrentTextView() {
        return current;
    }

    public TextView getTotalTextView() {
        return total;
    }

    public SeekBar getProgressBar() {
        return progress;
    }

    public ViewGroup getBottomContainer() {
        return (ViewGroup) previewBottomLayout;
    }

    public ViewGroup getTopContainer() {
        return (ViewGroup) previewTopLayout;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Log.d("jason", "onPageSelected: prePosition = " + prePosition + " ,position = " + position);
        if (prePosition != -1 && prePosition != position) {
            // 重置上一个页面
            PreviewItemFragment fragment = (PreviewItemFragment) pagerAdapter.instantiateItem(viewPager, prePosition);
            fragment.resetView();
            // 重置进度
            current.setText(R.string.preview_item_video_current_time_def);
            total.setText(R.string.preview_item_video_total_time_def);
            progress.setProgress(0);
            // 更新下一个页面
            updateUIBySelect(position);
        }
        prePosition = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
