package me.jason.imagepicker.ui.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.util.MultiTypeDelegate;

import java.util.List;

import me.jason.imagepicker.R;
import me.jason.imagepicker.internal.entity.Item;
import me.jason.imagepicker.internal.entity.SelectionSpec;

public class AlbumMeidaAdapter extends BaseQuickAdapter<Item, BaseViewHolder> {
    private static final int ITEM_MEDIA = 0;
    private static final int ITEM_CAPTURE = 1;

    private int mImageResize;

    public AlbumMeidaAdapter(@Nullable List<Item> data) {
        super(data);
        setMultiTypeDelegate(new MultiTypeDelegate<Item>() {
            @Override
            protected int getItemType(Item item) {
                if (item.isCapture()) return ITEM_CAPTURE;
                else return ITEM_MEDIA;
            }
        });
        getMultiTypeDelegate()
                .registerItemType(ITEM_MEDIA, R.layout.item_media_grid)
                .registerItemType(ITEM_CAPTURE, R.layout.item_photo_capture);
    }

    @Override
    protected void convert(BaseViewHolder helper, Item item) {
        switch (helper.getItemViewType()) {
            case ITEM_MEDIA:
                handleMedia(helper, item);
                break;
            case ITEM_CAPTURE:
                handleCapture(helper, item);
                break;
        }
    }

    private void handleMedia(BaseViewHolder helper, Item item) {
        // cover
        ImageView mediaCover = helper.getView(R.id.mediaCover);
        if (item.isGif()) {
            SelectionSpec.getInstance().imageEngine.loadGifThumbnail(mContext, getImageResize(), null, mediaCover, item.getContentUri());
        } else {
            SelectionSpec.getInstance().imageEngine.loadThumbnail(mContext, getImageResize(), null, mediaCover, item.getContentUri());
        }
    }

    private void handleCapture(BaseViewHolder helper, Item item) {

    }

    private int getImageResize() {
        if (mImageResize == 0) {
            RecyclerView.LayoutManager lm = getRecyclerView().getLayoutManager();
            int spanCount = ((GridLayoutManager) lm).getSpanCount();
            int screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
            int availableWidth = screenWidth - mContext.getResources().getDimensionPixelSize(
                    R.dimen.media_grid_spacing) * (spanCount - 1);
            mImageResize = availableWidth / spanCount;
            mImageResize = (int) (mImageResize * SelectionSpec.getInstance().thumbnailScale);
        }
        return mImageResize;
    }
}
