package co.work.fukouka.happ.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import co.work.fukouka.happ.R;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.model.Reservation;
import co.work.fukouka.happ.utils.SessionManager;
import co.work.fukouka.happ.viewholder.MakeReservationHolder;

public class MakeReservationAdapter extends RecyclerView.Adapter<MakeReservationHolder> {
    private List<Reservation> reservations = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mInflater;
    private HappHelper mHelper;

    public MakeReservationAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        mHelper = new HappHelper(context);
    }

    public void addReservations(Reservation reservation) {
        reservations.add(reservation);
        notifyItemInserted(reservations.size() - 1);
    }

    @Override
    public MakeReservationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_reserved_item, parent, false);
        MakeReservationHolder holder = new MakeReservationHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(MakeReservationHolder holder, int position) {
        String authorId = reservations.get(position).getAuthorId();
        String reservedTime = reservations.get(position).getTime();

        if (authorId.equals(getUserId())) {
            holder.tvReservedTime.setTypeface(Typeface.DEFAULT_BOLD);
            holder.tvReservedTime.setTextColor(ContextCompat.getColor(mContext, R.color.beta));
            mHelper.setText(holder.tvReservedTime, reservedTime + " " + mContext.getString(R.string.you));
        } else {
            mHelper.setText(holder.tvReservedTime, reservedTime);
        }
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    private String getUserId() {
        String userId;

        HashMap<String, String> user = new SessionManager(mContext).getUserId();
        userId = user.get(SessionManager.KEY_USER_ID);

        return userId;
    }
}
