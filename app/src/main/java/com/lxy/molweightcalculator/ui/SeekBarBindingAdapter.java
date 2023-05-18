package com.lxy.molweightcalculator.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.BindingMethod;
import androidx.databinding.BindingMethods;
import androidx.databinding.InverseBindingListener;
import androidx.databinding.InverseBindingMethod;
import androidx.databinding.InverseBindingMethods;

import com.warkiz.tickseekbar.OnSeekChangeListener;
import com.warkiz.tickseekbar.SeekParams;
import com.warkiz.tickseekbar.TickSeekBar;

@SuppressWarnings("unused")
@BindingMethods({
        @BindingMethod(type = TickSeekBar.class, attribute = "tsb_min", method = "setMin"),
        @BindingMethod(type = TickSeekBar.class, attribute = "tsb_max", method = "setMax"),
        @BindingMethod(type = TickSeekBar.class, attribute = "tsb_progress", method = "setProgress"),
        @BindingMethod(type = TickSeekBar.class, attribute = "tsb_ticks_count", method = "setTickCount"),
})
@InverseBindingMethods({
        @InverseBindingMethod(type = TickSeekBar.class, attribute = "tsb_progress", method = "getProgress")
})
public class SeekBarBindingAdapter {
    @SuppressWarnings("ConstantConditions")
    @BindingAdapter(value = "tsb_progressAttrChanged", requireAll = false)
    public static void setOnProgressChangeListener(@NonNull TickSeekBar view,
                                                   @Nullable InverseBindingListener attrChanged) {
        if (attrChanged == null) {
            // Although this parameter is annotated with @NonNull, null check is performed
            view.setOnSeekChangeListener(null);
        } else {
            view.setOnSeekChangeListener(new OnSeekChangeListener() {
                @Override
                public void onSeeking(SeekParams seekParams) {
                    if (attrChanged != null) {
                        attrChanged.onChange();
                    }
                }

                @Override
                public void onStartTrackingTouch(TickSeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(TickSeekBar seekBar) {

                }
            });
        }
    }
}
