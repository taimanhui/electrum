package org.haobtc.wallet.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.entries.DummyContent;
import org.haobtc.wallet.fragment.ItemFragmentTransaction;

import java.util.List;

public class MyItemRecyclerViewAdapterTransaction extends RecyclerView.Adapter<MyItemRecyclerViewAdapterTransaction.ViewHolder> {

    private final List<DummyContent.DummyItem> mValues;
    private final ItemFragmentTransaction.OnListFragmentInteractionListener mListener;

    public MyItemRecyclerViewAdapterTransaction(List<DummyContent.DummyItem> items, ItemFragmentTransaction.OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item_trans, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).id);
        holder.mContentView.setText(mValues.get(position).content);

        holder.mView.setOnClickListener(v ->{
            if (null != mListener) {
                // Notify the activate callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
//                mListener.onListFragmentInteraction(holder.mItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public DummyContent.DummyItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = view.findViewById(R.id.id_2);
            mContentView = view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
