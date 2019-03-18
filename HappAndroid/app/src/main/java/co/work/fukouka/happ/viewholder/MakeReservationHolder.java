package co.work.fukouka.happ.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import co.work.fukouka.happ.R;

public class MakeReservationHolder extends RecyclerView.ViewHolder{
    public TextView tvReservedTime;

    public MakeReservationHolder(View itemView) {
        super(itemView);

        tvReservedTime = itemView.findViewById(R.id.tv_reserved_time);
    }
}
