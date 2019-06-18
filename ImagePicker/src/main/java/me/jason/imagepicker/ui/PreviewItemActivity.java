package me.jason.imagepicker.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;

import java.util.List;

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

    public static final String EXTRA_ALBUM = "extra_album";
    public static final String EXTRA_ITEM = "extra_item";

    public static final int UPDATE_TYPE_INIT = 1;
    public static final int UPDATE_TYPE_ADD = 2;
    public static final int UPDATE_TYPE_REMOVE = 3;
    public static final int UPDATE_TYPE_SELECT = 4;

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
        pagerAdapter = new PreviewPagerAdapter(getSupportFragmentManager(), position -> {
            Log.d("jason", "onPrimaryItemSet position = " + position);
        });
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
        previewVideoLayout = findViewById(R.id.previewVideoLayout);
        previewVideoCompleted = findViewById(R.id.previewVideoCompleted);
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayout.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SelectedItemAdapter(null);
        adapter.setOnItemClickListener((adapter, view, position) -> {

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
        // TODO:
    }

    private void updateUIBySelect(int position) {
        // 赋值
        PreviewItemFragment fragment = (PreviewItemFragment) pagerAdapter.instantiateItem(viewPager, position);
        item = fragment.getItem();
        // 更新底部
        updateView(UPDATE_TYPE_SELECT);
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
            // 更新下一个页面
            updateUIBySelect(position);
        }
        prePosition = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
