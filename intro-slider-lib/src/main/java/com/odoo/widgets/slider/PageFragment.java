package com.odoo.widgets.slider;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class PageFragment extends Fragment {

    private SliderPagerAdapter adapter;

    public void setAdapter(SliderPagerAdapter adapter) {
        this.adapter = adapter;
    }

    public SliderItem getItem(int pos) {
        return adapter.getSliderItem(pos);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout;
        SliderItem item = getItem(getPosition());
        if (item.getSliderCustomViewListener() != null) {
            layout = item.getSliderCustomViewListener().getCustomView(
                    getContext(), item, container);
        } else {
            layout = inflater.inflate(R.layout.default_ui, container, false);
        }
        return layout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        SliderItem item = getItem(getPosition());
        if (item.getSliderCustomViewListener() == null) {
            ImageView imgPic = (ImageView) view.findViewById(R.id.view_image);
            TextView txvTitle, txvContent;
            txvTitle = (TextView) view.findViewById(R.id.view_title);
            txvContent = (TextView) view.findViewById(R.id.view_content);
            imgPic.setImageResource(item.getImagePath());
            txvTitle.setText(item.getTitle());
            txvContent.setText(item.getContent());
        }
    }

    private int getPosition() {
        return getArguments().getInt(SliderPagerAdapter.KEY_POSITION);
    }

}