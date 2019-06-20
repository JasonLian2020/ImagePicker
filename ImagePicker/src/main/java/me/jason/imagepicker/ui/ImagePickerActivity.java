package me.jason.imagepicker.ui;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;

import me.jason.imagepicker.R;
import me.jason.imagepicker.internal.entity.Album;
import me.jason.imagepicker.internal.entity.Item;
import me.jason.imagepicker.internal.entity.SelectionSpec;
import me.jason.imagepicker.internal.model.AlbumCollection;
import me.jason.imagepicker.internal.model.SelectedItemCollection;
import me.jason.imagepicker.ui.adapter.AlbumsAdapter;
import me.jason.imagepicker.ui.widget.AlbumsSpinner;
import me.jason.imagepicker.utils.CursorUtils;
import me.jason.imagepicker.utils.MediaStoreCompat;
import me.jason.imagepicker.utils.PathUtils;
import me.jason.imagepicker.utils.ThreadUtils;
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager;

import static me.jason.imagepicker.IntentHub.EXTRA_RESULT_FROM;
import static me.jason.imagepicker.IntentHub.EXTRA_RESULT_SELECTED_ITEM;
import static me.jason.imagepicker.IntentHub.EXTRA_RESULT_SELECTED_PATH;
import static me.jason.imagepicker.IntentHub.EXTRA_RESULT_SELECTED_URI;
import static me.jason.imagepicker.IntentHub.FROM_IMAGE;
import static me.jason.imagepicker.IntentHub.FROM_NONE;
import static me.jason.imagepicker.IntentHub.FROM_VDIEO;

public class ImagePickerActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_PREVIEW = 23;
    public static final int REQUEST_CODE_CAPTURE = 24;

    private final AlbumCollection mAlbumCollection = new AlbumCollection();
    private MediaStoreCompat mMediaStoreCompat;

    private AlbumsSpinner mAlbumsSpinner;
    private AlbumsAdapter mAlbumsAdapter;

    private ImageView titleClose;
    private TextView titleNext;
    private TextView titleNextNormal;

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

        //初始化
        if (SelectionSpec.getInstance().capture) {
            mMediaStoreCompat = new MediaStoreCompat(this);
            if (SelectionSpec.getInstance().captureStrategy == null)
                throw new RuntimeException("Don't forget to set CaptureStrategy.");
            mMediaStoreCompat.setCaptureStrategy(SelectionSpec.getInstance().captureStrategy);
        }

        //EXOPlayer内核，支持格式更多
        PlayerFactory.setPlayManager(Exo2PlayerManager.class);

        //view
        titleClose = findViewById(R.id.titleClose);
        titleClose.setOnClickListener(v -> onBackPressed());
        titleNext = findViewById(R.id.titleNext);
        titleNext.setOnClickListener(v -> clickNext());
        titleNext.setVisibility(View.VISIBLE);
        titleNextNormal = findViewById(R.id.titleNextNormal);
        titleNextNormal.setOnClickListener(v -> clickNext());
        titleNextNormal.setVisibility(View.GONE);

        //选中数据集合
        SelectedItemCollection.getInstance().onCreate(this, savedInstanceState);
        SelectedItemCollection.getInstance().setOnSelectChanageListener(items -> {
            if (items != null && items.size() > 0) {
                titleNext.setVisibility(View.GONE);
                titleNextNormal.setVisibility(View.VISIBLE);
                titleNextNormal.setText(getString(R.string.image_picker_next_btn_text2, items.size()));
            } else {
                titleNext.setVisibility(View.VISIBLE);
                titleNextNormal.setVisibility(View.GONE);
                titleNextNormal.setText("");
            }
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
                processResultForCapture(data);
                break;
            case REQUEST_CODE_PREVIEW:
                processResultForPreview(data);
                break;
        }
    }

    private void processResultForCapture(@Nullable Intent data) {
        Uri contentUri = mMediaStoreCompat.getCurrentPhotoUri();
        String path = mMediaStoreCompat.getCurrentPhotoPath();
        //通知图库更新
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
        //通知给外面
        ArrayList<Uri> selectedUris = new ArrayList<>();
        ArrayList<String> selectedPaths = new ArrayList<>();
        selectedPaths.add(path);
        selectedUris.add(contentUri);
        sendResult(selectedUris, selectedPaths, FROM_VDIEO);
    }

    private void processResultForPreview(@Nullable Intent data) {
        if (data == null) return;
        ArrayList<Item> selectedItems = data.getParcelableArrayListExtra(EXTRA_RESULT_SELECTED_ITEM);
        int from = data.getIntExtra(EXTRA_RESULT_FROM, FROM_NONE);
        ArrayList<Uri> selectedUris = new ArrayList<>();
        ArrayList<String> selectedPaths = new ArrayList<>();
        if (selectedItems != null) {
            for (Item selectedItem : selectedItems) {
                selectedUris.add(selectedItem.getContentUri());
                selectedPaths.add(PathUtils.getPath(this, selectedItem.getContentUri()));
            }
        }
        sendResult(selectedUris, selectedPaths, from);
    }

    private void sendResult(ArrayList<Uri> selectedUris, ArrayList<String> selectedPaths, int from) {
        Intent result = new Intent();
        result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTED_URI, selectedUris);
        result.putStringArrayListExtra(EXTRA_RESULT_SELECTED_PATH, selectedPaths);
        result.putExtra(EXTRA_RESULT_FROM, from);
        setResult(RESULT_OK, result);
        finish();
    }

    private void clickNext() {
        int count = SelectedItemCollection.getInstance().count();
        ArrayList<Uri> selectedUris = new ArrayList<>();
        ArrayList<String> selectedPaths = new ArrayList<>();
        int from = FROM_NONE;
        if (count > 0) {
            ArrayList<Item> selectedItems = SelectedItemCollection.getInstance().asList();
            if (selectedItems != null) {
                for (Item selectedItem : selectedItems) {
                    selectedUris.add(selectedItem.getContentUri());
                    selectedPaths.add(PathUtils.getPath(this, selectedItem.getContentUri()));
                }
            }
            from = FROM_IMAGE;
        }
        sendResult(selectedUris, selectedPaths, from);
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
        // 用完就释放掉
        mAlbumCollection.onDestroy();
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

    public void imageCapture() {
        new RxPermissions(this)
                .requestEach(Manifest.permission.CAMERA)
                .subscribe(permission -> {
                    if (permission.granted) {
                        //授权成功
                        if (mMediaStoreCompat != null) {
                            mMediaStoreCompat.dispatchCaptureIntent(this, REQUEST_CODE_CAPTURE);
                        }
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        //授权失败，可以再次询问
                        ToastUtils.showShort("拒绝授权，无法拍照");
                    } else {
                        //授权失败，不能再次询问
                        ToastUtils.showShort("拒绝授权，请到设置页面打开对应权限");
                    }
                });
    }
}
