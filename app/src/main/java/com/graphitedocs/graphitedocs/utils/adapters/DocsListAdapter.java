package com.graphitedocs.graphitedocs.utils.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.graphitedocs.graphitedocs.R;
import com.graphitedocs.graphitedocs.docs.DocsActivity;
import com.graphitedocs.graphitedocs.utils.models.DocsListItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DocsListAdapter extends RecyclerView.Adapter<DocsListAdapter.Holder> {

    public interface OnItemCheckListener {
        void onItemCheck (DocsListItem item);
        void onItemUncheck (DocsListItem item);
    }

    private Context mContext;
    private ArrayList<DocsListItem> mArrayList;
    private SparseBooleanArray mItemStateArray = new SparseBooleanArray();
    @NonNull
    private OnItemCheckListener onItemCheckListener;

    public DocsListAdapter(Context mContext, ArrayList<DocsListItem> mArrayList, @NonNull OnItemCheckListener onItemCheckListener) {
        this.mContext = mContext;
        this.mArrayList = mArrayList;
        this.onItemCheckListener = onItemCheckListener;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.docs_list_item, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(final Holder holder, final int position) {
        final DocsListItem item = mArrayList.get(position);

        holder.titleTextView.setText(item.getTitle());

        String date;
        if (item.getCreated() == null) {
            date = new SimpleDateFormat("MMMM dd, yyyy").format(new Date(item.getUpdated()));
        } else {
            date = new SimpleDateFormat("MMMM dd, yyyy").format(new Date(item.getCreated()));
        }

        holder.dateCreatedTextView.setText(date);



        if (item.getTags() != null) {
            holder.tagsTextView.setText(join(item.getTags()));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(DocsActivity.Companion.newIntent(mContext, item.getTitle(), item.getId(), item.getUpdated()));
            }
        });

        holder.checkBox.setChecked(mItemStateArray.get(position, false));
        holder.checkBox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!mItemStateArray.get(position, false)) {
                    mItemStateArray.put(position, true);
                    onItemCheckListener.onItemCheck(item);
                } else {
                    mItemStateArray.put(position, false);
                    onItemCheckListener.onItemUncheck(item);
                }
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
        private TextView dateCreatedTextView;
        private TextView tagsTextView;
        private CheckBox checkBox;

        public Holder(View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.docs_list_title);
            dateCreatedTextView = itemView.findViewById(R.id.created_date);
            tagsTextView = itemView.findViewById(R.id.docs_list_tags);
            checkBox = itemView.findViewById(R.id.docs_list_checkbox);
        }
    }
}