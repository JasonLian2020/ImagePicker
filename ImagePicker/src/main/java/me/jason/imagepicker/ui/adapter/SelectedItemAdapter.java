package me.jason.imagepicker.ui.adapter;

import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.blankj.utilcode.util.SizeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import me.jason.imagepicker.R;
import me.jason.imagepicker.internal.entity.Item;
import me.jason.imagepicker.internal.entity.SelectionSpec;

public class SelectedItemAdapter extends BaseQuickAdapter<Item, BaseViewHolder> {

    public SelectedItemAdapter(@Nullable List<Item> data) {
        super(R.layout.item_selected_image, data);
    }

    private Item selectedItem;

    public void setSelectedItem(Item selectedItem) {
        this.selectedItem = selectedItem;
    }

    @Override
    protected void convert(BaseViewHolder helper, Item item) {
        ImageView seletedImage = helper.getView(R.id.seletedImage);
        if (item.isGif()) {
            SelectionSpec.getInstance().imageEngine.loadGifThumbnail(mContext, getImageResize(), null, seletedImage, item.getContentUri());
        } else {
            SelectionSpec.getInstance().imageEngine.loadThumbnail(mContext, getImageResize(), null, seletedImage, item.getContentUri());
        }

        helper.setGone(R.id.selectMask, item.equals(selectedItem));
    }

    private int getImageResize() {
        return SizeUtils.dp2px(80);
    }
}
