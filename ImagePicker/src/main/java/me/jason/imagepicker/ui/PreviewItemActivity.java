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

import java.util.List;

import me.jason.imagepicker.R;
import me.jason.imagepicker.internal.entity.Album;
import me.jason.imagepicker.internal.entity.Item;
import me.jason.imagepicker.internal.model.AlbumMediaCollection;
import me.jason.imagepicker.internal.model.SelectedItemCollection;
import me.jason.imagepicker.ui.adapter.SelectedItemAdapter;
import me.jason.imagepicker.utils.CursorUtils;
import me.jason.imagepicker.utils.ThreadUtils;

public class PreviewItemActivity extends AppCompatActivity {

    public static final String EXTRA_ALBUM = "extra_album";
    public static final String EXTRA_ITEM = "extra_item";

    private ViewPager viewPager;
    //top
    private View previewTopLayout;
    private ImageView previewClose;
    private TextView previewChoose;
    private TextView previewVideoTips;
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
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAlbumMediaCollection.onDestroy();
    }

    private void parseIntent() {
        if (getIntent() == null) return;
        album = getIntent().getParcelableExtra(EXTRA_ALBUM);
        item = getIntent().getParcelableExtra(EXTRA_ITEM);
    }

    private void initView() {
        viewPager = findViewById(R.id.viewPager);
        //top
        previewTopLayout = findViewById(R.id.previewTopLayout);
        previewClose = findViewById(R.id.previewClose);
        previewClose.setOnClickListener(v -> onBackPressed());
        previewChoose = findViewById(R.id.previewChoose);
        previewChoose.setOnClickListener(v -> {
            //TODO:
        });
        previewVideoTips = findViewById(R.id.previewVideoTips);
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

    private void initData() {
        //处理顶部显示逻辑
        processTopLayout(item);
        //处理底部显示逻辑
        processBottomLayout(item);
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

    private void updateUIByInit(List<Item> itemList) {
        // TODO:

        // 用完就释放掉
        mAlbumMediaCollection.onDestroy();
    }

    private void updateUIByReset() {
        // TODO:
    }

    private void processTopLayout(Item item) {
        if (item.isImage()) {
            previewChoose.setVisibility(View.VISIBLE);
            previewVideoTips.setVisibility(View.GONE);
        } else {
            previewChoose.setVisibility(View.GONE);
            int count = SelectedItemCollection.getInstance().count();
            if (count > 0) {
                previewVideoTips.setVisibility(View.VISIBLE);
                previewVideoTips.setText(R.string.preview_item_video_tips1);
            } else {
                int minSecond = 3;
                int curSecond = (int) (item.duration / 1000);
                previewVideoTips.setVisibility(curSecond > minSecond ? View.GONE : View.VISIBLE);
                previewVideoTips.setText(curSecond > minSecond ? "" : getString(R.string.preview_item_video_tips2, curSecond));
            }
        }
    }

    private void processBottomLayout(Item item) {
        if (item.isImage()) {
            recyclerView.setVisibility(View.VISIBLE);
            previewImageCompleted.setVisibility(View.VISIBLE);
            previewVideoLayout.setVisibility(View.GONE);
            previewVideoCompleted.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            previewImageCompleted.setVisibility(View.GONE);
            previewVideoLayout.setVisibility(View.VISIBLE);
            previewVideoCompleted.setVisibility(View.VISIBLE);
        }
    }
}
