package com.graphitedocs.graphitedocs.utils.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.graphitedocs.graphitedocs.R;

import java.util.ArrayList;

public class DocsListAdapter extends RecyclerView.Adapter<DocsListAdapter.Holder> {

    Context mContext;
    ArrayList<String> mArrayListString;

    public DocsListAdapter(Context mContext, ArrayList<String> mArrayListString) {
        this.mContext = mContext;
        this.mArrayListString = mArrayListString;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.activity_contacts_main, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
//        holder.mTextView.setText(mArrayListString.get(position));
    }

    @Override
    public int getItemCount() {
        return mArrayListString.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        public Holder(View itemView) {
            super(itemView);
        }
    }
}