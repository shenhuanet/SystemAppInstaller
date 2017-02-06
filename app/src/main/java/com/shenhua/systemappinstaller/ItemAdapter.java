package com.shenhua.systemappinstaller;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.List;

/**
 * Created by shenhua on 1/18/2017.
 * Email shenhuanet@126.com
 */
public class ItemAdapter extends BaseListAdapter {

    protected ItemAdapter(Context context, List datas) {
        super(context, datas);
    }

    @Override
    public void onBindItemView(final BaseViewHolder holder, Object o, int position) {
        holder.setText(R.id.tv_name, ((PackageInfo) o).getAppName());
        holder.setText(R.id.tv_package, ((PackageInfo) o).getFilePath());
        holder.getView(R.id.btn_install).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = (TextView) holder.getConvertView().findViewById(R.id.tv_package);
                ApkOperateManager manager = new ApkOperateManager();
                manager.openApk(mContext, new File(tv.getText().toString()));
            }
        });
    }

    @Override
    public int getItemViewId() {
        return R.layout.item_list;
    }
}
