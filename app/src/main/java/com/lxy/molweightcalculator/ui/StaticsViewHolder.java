package com.lxy.molweightcalculator.ui;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StaticsViewHolder extends RecyclerView.ViewHolder {
    public StaticsViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public TextView getTextView() {
        return (TextView) itemView;
    }
}
