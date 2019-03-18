package co.work.fukouka.happ.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import co.work.fukouka.happ.R;


public class NotificationHolder extends RecyclerView.ViewHolder {
    public ImageView ivUserPhoto;
    public TextView tvNotifDetail;
    public TextView tvDate;

    public NotificationHolder(View itemView) {
        super(itemView);

        ivUserPhoto = (ImageView) itemView.findViewById(R.id.iv_user_photo);
        tvNotifDetail = (TextView) itemView.findViewById(R.id.tv_notif_detail);
        tvDate = (TextView) itemView.findViewById(R.id.tv_date);

    }

}
