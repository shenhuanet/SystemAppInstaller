package com.shenhua.systemappinstaller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * ListView GridView 适配器基类
 * Created by shenhua on 9/18/2016.
 */
public abstract class BaseListAdapter<T> extends BaseAdapter {

    protected Context mContext;
    private List<T> mDatas;

    protected BaseListAdapter(Context context, List<T> datas) {
        this.mContext = context;
        this.mDatas = datas;
    }

    @Override
    public int getCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BaseViewHolder holder = BaseViewHolder.getInstance(mContext, getItemViewId(), position, convertView, parent);
        onBindItemView(holder, mDatas.get(position), position);
        return holder.getConvertView();
    }

    public abstract void onBindItemView(BaseViewHolder holder, T t, int position);

    public abstract int getItemViewId();

    public static class BaseViewHolder {
        private int mPosition;
        //用于存储holder里面的各个view，此集合比map效率高,但key必须为Integer
        private SparseArray<View> mViews;
        //复用的view
        private View convertView;

        private BaseViewHolder(Context context, int position, int layoutId, ViewGroup parent) {
            this.mPosition = position;
            mViews = new SparseArray<>();
            convertView = LayoutInflater.from(context).inflate(layoutId, parent, false);
            convertView.setTag(this);
        }

        public static BaseViewHolder getInstance(Context context, int layoutId, int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                return new BaseViewHolder(context, position, layoutId, parent);
            } else {
                BaseViewHolder holder = (BaseViewHolder) convertView.getTag();
                holder.mPosition = position;
                return holder;
            }
        }

        /**
         * 可通过Id获取item里面的view
         *
         * @param resourceId 控件的id
         * @return view
         */
        @SuppressWarnings("unchecked")
        public <T extends View> T getView(int resourceId) {
            View view = mViews.get(resourceId);
            if (view == null) {
                view = convertView.findViewById(resourceId);
                mViews.put(resourceId, view);
            }
            return (T) view;
        }

        /**
         * 为textView填充内容
         *
         * @param resourceId 控件id
         * @param text       文本
         * @return BaseViewHolder
         */
        public BaseViewHolder setText(int resourceId, CharSequence text) {
            ((TextView) getView(resourceId)).setText(text);
            return this;
        }

        public BaseViewHolder setText(int resourceId, int resId) {
            ((TextView) getView(resourceId)).setText(resId);
            return this;
        }

        /**
         * 为ImageView设置Bitmap
         *
         * @param resourceId 控件id
         * @param bm         bitmap
         * @return BaseViewHolder
         */
        public BaseViewHolder setBitmap(int resourceId, Bitmap bm) {
            ((ImageView) getView(resourceId)).setImageBitmap(bm);
            return this;
        }

        public BaseViewHolder setImageDrawable(int resourceId, Drawable drawable) {
            ((ImageView) getView(resourceId)).setImageDrawable(drawable);
            return this;
        }

        public BaseViewHolder setImageResource(int resourceId, int resId) {
            ((ImageView) getView(resourceId)).setImageResource(resId);
            return this;
        }

        View getConvertView() {
            return convertView;
        }

        /**
         * 获取当前item的位置
         *
         * @return position
         */
        public int getPosition() {
            return mPosition;
        }
    }

}
