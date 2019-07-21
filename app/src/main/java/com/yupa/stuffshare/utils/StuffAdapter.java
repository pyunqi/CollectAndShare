package com.yupa.stuffshare.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yupa.stuffshare.R;
import com.yupa.stuffshare.entity.Stuff;
import com.yupa.stuffshare.utils.ImageHelper;

import java.util.List;

public class StuffAdapter extends BaseAdapter {
    private final Context mContext;
    private List<Stuff> mStuffs;

    public StuffAdapter(Context mContext, List<Stuff> mStuffs) {
        this.mContext = mContext;
        this.mStuffs = mStuffs;
    }

    @Override
    public int getCount() {
        if (mStuffs == null || mStuffs.size() == 0) {
            return -1;
        }
        return mStuffs.size();
    }

    @Override
    public Object getItem(int position) {
        if (mStuffs == null || mStuffs.size() == 0) {
            return null;
        }
        return mStuffs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater myInflater = LayoutInflater.from(mContext);
            view = myInflater.inflate(R.layout.activity_stuff_adapter, parent, false);
        }
        ImageView imageView = view.findViewById(R.id.ivImage);
        TextView txtName = view.findViewById(R.id.txtName);
        TextView txtQuantity = view.findViewById(R.id.txtQuantity);
        TextView txtDescription = view.findViewById(R.id.txtDescription);
        txtQuantity.setText("TAG: " + mStuffs.get(position).get_tag() + "    Quantity:  " + mStuffs.get(position).get_quantity());
        txtDescription.setText("Description:\n" + mStuffs.get(position).get_description());
        txtName.setText("Name:  " + mStuffs.get(position).get_name());
        imageView.setImageBitmap(ImageHelper.decodeSampledBitmapFromFile(mStuffs.get(position).get_picture(), 270, 480));

        return view;
    }

    public Stuff getStuff(int position) {
        return mStuffs.get(position);
    }


}
