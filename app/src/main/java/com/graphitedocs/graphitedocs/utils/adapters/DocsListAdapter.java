package com.graphitedocs.graphitedocs.utils.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.graphitedocs.graphitedocs.R;
import com.graphitedocs.graphitedocs.docs.DocsActivity;
import com.graphitedocs.graphitedocs.utils.models.DocsListItem;

import java.util.ArrayList;
import java.util.List;

public class DocsListAdapter extends RecyclerView.Adapter<DocsListAdapter.Holder> {

    Context mContext;
    ArrayList<DocsListItem> mArrayList;

    public DocsListAdapter(Context mContext, ArrayList<DocsListItem> mArrayList) {
        this.mContext = mContext;
        this.mArrayList = mArrayList;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.docs_list_item, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        final DocsListItem item = mArrayList.get(position);

        holder.titleTextView.setText(item.getTitle());

        if (item.getSharedWith() != null) {
            String collaborators = item.getAuthor() + join(item.getSharedWith());
            holder.collaboratorsTextView.setText(collaborators);
        }


        if (item.getTags() != null) {
            holder.tagsTextView.setText(join(item.getTags()));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(DocsActivity.Companion.newIntent(mContext, item.getTitle(), item.getId(), item.getDate()));
            }
        });

    }

    private String join(List<String> list) {
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            text.append(", ");
            text.append(list.get(i));
        }
        return text.toString();
    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        private TextView titleTextView;
        private TextView collaboratorsTextView;
        private TextView tagsTextView;

        public Holder(View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.docs_list_title);
            collaboratorsTextView = itemView.findViewById(R.id.docs_list_collaborators);
            tagsTextView = itemView.findViewById(R.id.docs_list_tags);
        }
    }
}