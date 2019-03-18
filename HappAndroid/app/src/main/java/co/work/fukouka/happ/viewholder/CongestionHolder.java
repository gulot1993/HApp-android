package co.work.fukouka.happ.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import co.work.fukouka.happ.R;


public class CongestionHolder extends RecyclerView.ViewHolder {
    public ImageView ivUserPhoto;
    public TextView tvName;

    public CongestionHolder(View itemView) {
        super(itemView);
        ivUserPhoto = itemView.findViewById(R.id.iv_user_photo);
        tvName = itemView.findViewById(R.id.tv_name);
    }
}
