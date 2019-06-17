package me.jason.imagepicker.ui.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import me.jason.imagepicker.R;
import me.jason.imagepicker.internal.entity.Item;

public class SelectedItemAdapter extends BaseQuickAdapter<Item, BaseViewHolder> {
    public SelectedItemAdapter(@Nullable List<Item> data) {
        super(R.layout.item_selected_image, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Item item) {

    }
}
