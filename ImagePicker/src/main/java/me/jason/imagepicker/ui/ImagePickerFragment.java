package me.jason.imagepicker.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.util.ToastUtils;

import java.util.List;

import me.jason.imagepicker.R;
import me.jason.imagepicker.internal.entity.Album;
import me.jason.imagepicker.internal.entity.Item;
import me.jason.imagepicker.internal.entity.SelectionSpec;
import me.jason.imagepicker.internal.model.AlbumMediaCollection;
import me.jason.imagepicker.internal.model.SelectedItemCollection;
import me.jason.imagepicker.ui.adapter.AlbumMeidaAdapter;
import me.jason.imagepicker.ui.widget.MediaGridInset;
import me.jason.imagepicker.utils.CursorUtils;
import me.jason.imagepicker.utils.ThreadUtils;

public class ImagePickerFragment extends Fragment implements SelectedItemCollection.OnItemChanageListener {
    public static final String EXTRA_ALBUM = "extra_album";

    private Album album;

    private RecyclerView recyclerview;
    private AlbumMeidaAdapter mAdapter;

    private final AlbumMediaCollection mAlbumMediaCollection = new AlbumMediaCollection();

    public static ImagePickerFragment newInstance(Album album) {
        ImagePickerFragment fragment = new ImagePickerFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_ALBUM, album);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) return;
        album = getArguments().getParcelable(EXTRA_ALBUM);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image_picker, container, false);
        initRecyclerView(rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SelectedItemCollection.getInstance().addOnItemChanageListener(this);
        mAlbumMediaCollection.onCreate(getActivity(), new AlbumMediaCollection.AlbumMediaCallbacks() {
            @Override
            public void onAlbumMediaLoad(Cursor cursor) {
                Log.d("jason", ImagePickerFragment.class.getSimpleName() + ": onAlbumMediaLoad");
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
                Log.d("jason", ImagePickerFragment.class.getSimpleName() + ": onAlbumMediaLoad");
                if (ThreadUtils.isMainThread()) {
                    updateUIByReset();
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> updateUIByReset());
                }
            }
        });
        mAlbumMediaCollection.load(album, SelectionSpec.getInstance().capture);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SelectedItemCollection.getInstance().removeOnItemChanageListener(this);
        mAlbumMediaCollection.onDestroy();
    }

    @Override
    public void onAdd(Item item) {
        // 选中集合，数据发生增加
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRemove(Item item) {
        // 选中集合，数据发生删除
        mAdapter.notifyDataSetChanged();
    }

    private void initRecyclerView(View rootView) {
        int spanCount = 3;
        int spacing = getResources().getDimensionPixelSize(R.dimen.media_grid_spacing);
        recyclerview = rootView.findViewById(R.id.recyclerView);
        recyclerview.setHasFixedSize(true);
        recyclerview.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        recyclerview.addItemDecoration(new MediaGridInset(spanCount, spacing, false));
        mAdapter = new AlbumMeidaAdapter(null);
        mAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            Item item = (Item) adapter.getItem(position);
            if (item == null) return;
            if (view.getId() == R.id.mediaChoose) {
                // 处理选中和取消选中
                if (view.isSelected()) {
                    SelectedItemCollection.getInstance().remove(item);
                } else {
                    int count = SelectedItemCollection.getInstance().count();
                    int maxSelectable = SelectionSpec.getInstance().maxSelectable;
                    if (count >= maxSelectable) {
                        //不能再选，已经达到限制了
                        ToastUtils.showShort(R.string.error_over_count, count);
                    } else {
                        SelectedItemCollection.getInstance().add(item);
                    }
                }
            }
        });
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            Item item = (Item) adapter.getItem(position);
            if (item == null) return;
            if (item.isCapture()) {
                //拍照
                int count = SelectedItemCollection.getInstance().count();
                if (count > 0) {
                    ToastUtils.showShort(R.string.image_picker_camera_tips1);
                } else {
                    //TODO:
                    ToastUtils.showShort("拍照功能开发中...");
                }
            } else if (item.isImage()) {
                //预览照片
                PreviewItemActivity.startForResult(getActivity(), album, item, ImagePickerActivity.REQUEST_CODE_PREVIEW);
            } else if (item.isVideo()) {
                //预览视频
                PreviewItemActivity.startForResult(getActivity(), album, item, ImagePickerActivity.REQUEST_CODE_PREVIEW);
            }
        });
        mAdapter.bindToRecyclerView(recyclerview);
    }

    private void updateUIByInit(List<Item> itemList) {
        Log.d("jason", "updateUIByInit");
        mAdapter.setNewData(itemList);
        // 用完就释放掉
        mAlbumMediaCollection.onDestroy();
    }

    private void updateUIByReset() {
        Log.d("jason", "updateUIByReset");
        mAdapter.setNewData(null);
    }
}
