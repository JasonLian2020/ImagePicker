package me.jason.imagepicker.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.blankj.utilcode.util.BarUtils;

import java.util.List;

import me.jason.imagepicker.R;
import me.jason.imagepicker.internal.entity.Album;
import me.jason.imagepicker.internal.entity.SelectionSpec;
import me.jason.imagepicker.internal.model.AlbumCollection;
import me.jason.imagepicker.internal.model.SelectedItemCollection;
import me.jason.imagepicker.ui.adapter.AlbumsAdapter;
import me.jason.imagepicker.ui.widget.AlbumsSpinner;
import me.jason.imagepicker.utils.CursorUtils;
import me.jason.imagepicker.utils.ThreadUtils;

public class ImagePickerActivity extends AppCompatActivity {
    private final AlbumCollection mAlbumCollection = new AlbumCollection();

    public static final int REQUEST_CODE_PREVIEW = 23;
    public static final int REQUEST_CODE_CAPTURE = 24;

    private AlbumsSpinner mAlbumsSpinner;
    private AlbumsAdapter mAlbumsAdapter;

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.popup_down_in, R.anim.popup_down_out);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);
        int statusBarColor;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            statusBarColor = getResources().getColor(R.color.image_picker_colorPrimary);
            BarUtils.setStatusBarLightMode(this, true);
        } else {
            statusBarColor = getResources().getColor(R.color.image_picker_colorPrimary_compatibility);
        }
        //状态栏颜色
        BarUtils.setStatusBarColor(this, statusBarColor);

        //view
        findViewById(R.id.titleClose).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.titleNext).setOnClickListener(v -> {

        });

        //选中数据集合
        SelectedItemCollection.getInstance().onCreate(this, savedInstanceState);
        SelectedItemCollection.getInstance().setOnSelectChanageListener(items -> {
            //TODO:
        });

        //选择弹窗初始化
        mAlbumsAdapter = new AlbumsAdapter(this, null);
        mAlbumsSpinner = new AlbumsSpinner(this);
        mAlbumsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("jason", "onItemSelected position = " + position);
                updateUIBySelect(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("jason", "onNothingSelected");
            }
        });
        mAlbumsSpinner.setSelectedTextView(findViewById(R.id.titleSelect));
        mAlbumsSpinner.setPopupAnchorView(findViewById(R.id.titleLayout));
        mAlbumsSpinner.setAdapter(mAlbumsAdapter);
        //获取相册列表
        mAlbumCollection.onCreate(this, new AlbumCollection.AlbumCallbacks() {
            @Override
            public void onAlbumLoad(Cursor cursor) {
                Log.d("jason", ImagePickerActivity.class.getSimpleName() + ": onAlbumLoad");
                List<Album> albumList = CursorUtils.getAllAlbum(cursor);
                // select default album.
                if (ThreadUtils.isMainThread()) {
                    updateUIByInit(albumList);
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> updateUIByInit(albumList));
                }
            }

            @Override
            public void onAlbumReset() {
                Log.d("jason", ImagePickerActivity.class.getSimpleName() + ": onAlbumReset");
                // Reset List is null
                if (ThreadUtils.isMainThread()) {
                    updateUIByReset();
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> updateUIByReset());
                }
            }
        });
        mAlbumCollection.onRestoreInstanceState(savedInstanceState);
        mAlbumCollection.loadAlbums();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        SelectedItemCollection.getInstance().onSaveInstanceState(outState);
        mAlbumCollection.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SelectedItemCollection.getInstance().onDestroy();
        mAlbumCollection.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch (requestCode) {
            case REQUEST_CODE_CAPTURE:
                //TODO:
                break;
            case REQUEST_CODE_PREVIEW:
                //TODO:
                break;
        }
    }

    private void updateUIByInit(List<Album> albumList) {
        // 获取当前选中位置
        int currentPosition = mAlbumCollection.getCurrentSelection();
        // 刷新列表UI
        mAlbumsAdapter.setAlbumList(albumList);
        // 更新弹窗选中位置
        mAlbumsSpinner.setSelection(this, currentPosition);
        // 获取当前选中的相册
        Album album = albumList.get(currentPosition);
        // 开启了相机功能，“全部”count需要+1
        if (album.isAll() && SelectionSpec.getInstance().capture) {
            album.addCaptureCount();
        }
        // 更新对应相册下的Fragment
        updateAlbumSelected(album);
    }

    private void updateUIBySelect(int position) {
        // 更新当前选中位置
        mAlbumCollection.setStateCurrentSelection(position);
        // 每次切换，需要重置选中数据
        SelectedItemCollection.getInstance().reset();
        // 获取列表集合
        List<Album> albumList = mAlbumsAdapter.getAlbumList();
        // 获取当前选中的相册
        Album album = albumList.get(position);
        // 更新对应相册下的Fragment
        updateAlbumSelected(album);
    }

    private void updateUIByReset() {
        mAlbumsAdapter.setAlbumList(null);
    }

    private void updateAlbumSelected(Album album) {
        Fragment fragment = ImagePickerFragment.newInstance(album);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment, ImagePickerFragment.class.getSimpleName())
                .commitAllowingStateLoss();
    }
}
