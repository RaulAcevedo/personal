package com.example.raulacevedo.exp_rxapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.raulacevedo.exp_rxapp.R;

import java.util.ArrayList;
import java.util.List;

public class SimpleStringAdapter extends RecyclerView.Adapter<SimpleStringAdapter.ViewHolder> {

    private final Context mContext;
    private final List<String> mStrings = new ArrayList<>();

    public SimpleStringAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(
                viewGroup.getContext()).inflate(R.layout.string_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        viewHolder.colorTextView.setText(mStrings.get(i));
        viewHolder.itemView.setOnClickListener(view -> {
            Toast.makeText(mContext, mStrings.get(i), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return mStrings.size();
    }


    public void setStrings(List<String> newStrings) {
        mStrings.clear();
        mStrings.addAll(newStrings);
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView colorTextView;

        public ViewHolder(View view) {
            super(view);
            colorTextView = view.findViewById(R.id.color_display);
        }
    }
}
