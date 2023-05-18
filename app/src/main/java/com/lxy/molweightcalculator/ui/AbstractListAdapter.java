package com.lxy.molweightcalculator.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lxy.molweightcalculator.util.Contract;

import java.util.List;

public abstract class AbstractListAdapter<Row> extends BaseAdapter {
    @NonNull
    private final List<Row> rows;
    @NonNull
    private final LayoutInflater layoutInflater;
    private final @LayoutRes int layout;
    public AbstractListAdapter(@NonNull Context context,
                               @LayoutRes int layout,
                               @NonNull List<Row> rows) {
        this.layout = layout;
        this.rows = Contract.requireNonNull(rows);
        this.layoutInflater = LayoutInflater.from(Contract.requireNonNull(context));
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return rows.size();
    }

    @Override
    @NonNull
    public Object getItem(int position) {
        return rows.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @Nullable ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(layout, parent, false);
        }
        bindView(convertView, position);
        return convertView;
    }

    @SuppressWarnings("unchecked")
    protected Row get(int position) {
        return (Row) getItem(position);
    }

    protected abstract void bindView(@NonNull View view, int position);
}
