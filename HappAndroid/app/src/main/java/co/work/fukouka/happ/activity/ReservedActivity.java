package co.work.fukouka.happ.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.helper.AlertDialogHelper;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.model.Reservation;
import co.work.fukouka.happ.presenter.ReservationNewPresenter;
import co.work.fukouka.happ.utils.JsonObjectRequest;
import co.work.fukouka.happ.view.ResView;
import io.fabric.sdk.android.Fabric;

public class ReservedActivity extends AppCompatActivity implements  ResView {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tb_title) TextView tbTitle;
    @BindView(R.id.btn_create) Button btnCreate;
    @BindView(R.id.main_layout) LinearLayout mainLayout;

    private HappHelper mHelper;
    private GetSystemValue mSystem;
    private ReservationNewPresenter mPres;

    private List<Integer> patternList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserved);
        ButterKnife.bind(this);
        Fabric.with(this, new Crashlytics());
        getSystemValues();

        mHelper = new HappHelper(this);
        mSystem = new GetSystemValue(this);
        mPres = new ReservationNewPresenter(this, this);

        mHelper.setUpToolbar(toolbar);

        boolean fromFragment = getIntent().getBooleanExtra("from_fragment", false);
        String sender = getIntent().getStringExtra("sender");
        if (fromFragment) {
            btnCreate.setVisibility(View.GONE);
            mPres.getOffice("LIST", "", "");
        } else {
            if (sender != null && sender.equals("notification")) {
                btnCreate.setVisibility(View.GONE);
                mPres.getOffice("LIST", "", "");
            } else {
                btnCreate.setVisibility(View.VISIBLE);

                Intent in = getIntent();
                int year = in.getIntExtra("year", 0);
                int month = in.getIntExtra("month", 0);
                int day = in.getIntExtra("day", 0);

                String mSelectedDate = String.valueOf(year) + "-" + String.valueOf(month) + "-" +String.valueOf(day);

                String startDate = mSelectedDate + " 00:00:00";
                String endDate = mSelectedDate + " 23:59:00";

                mPres.getOffice("DATE", startDate, endDate);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.happ.check_user_session");
        this.registerReceiver(this.logOutUser, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.unregisterReceiver(this.logOutUser);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        String sender =  getIntent().getStringExtra("sender");
        if (sender != null && sender.equals("notification")) {
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        } else {
            overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_top);
        }
    }

    @OnClick(R.id.btn_create)
    public void onClicks(View view) {
        Intent in = getIntent();
        int year = in.getIntExtra("year", 0);
        int month = in.getIntExtra("month", 0);
        int day = in.getIntExtra("day", 0);

        Intent intent = new Intent(this, MakeReservationActivity.class);
        intent.putExtra("year", year);
        intent.putExtra("month", month);
        intent.putExtra("day", day);
        startActivity(intent);
    }

    private void generateLayout(Reservation reservation) {
        GetSystemValue systemValue = new GetSystemValue(this);
        String roomTxt = systemValue.getValue("label_room");
        String facilityTxt = systemValue.getValue("lbl_facility");
        String delete = systemValue.getValue("button_delete");

        String officeName;
        String roomName;

        // Reservatin date
        String reservationDate = reservation.getDate();
        if (reservationDate != null && mPres.getLanguage().equals("jp")) {
            reservationDate = mHelper.getJapaneseDate(reservationDate);
        }

        String lang = mPres.getLanguage();
        if (lang.equals("jp")) {
            officeName = reservation.getOffice().getOfficeNameJp();
            roomName = reservation.getRoom().getRoomNameJp();
        } else {
            officeName = reservation.getOffice().getOfficeNameEn();
            roomName = reservation.getRoom().getRoomNameEn();
        }

        String officeId = String.valueOf(reservation.getOffice().getOfficeId());
        String roomId = String.valueOf(reservation.getRoom().getRoomId());

        final int tag = Integer.parseInt(officeId + roomId);

        int listSize = reservation.getReservationList().size();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        LinearLayout.LayoutParams wrapParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        RelativeLayout.LayoutParams myParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        if (!patternList.contains(tag)) {
            patternList.add(tag);

            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setId(tag);
            linearLayout.setPadding(0, 0 , 0 , 10);
            linearLayout.setLayoutParams(params);
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            final LinearLayout roomLayout = new LinearLayout(this);
            roomLayout.setLayoutParams(params);
            roomLayout.setOrientation(LinearLayout.VERTICAL);
            roomLayout.setGravity(Gravity.CENTER);
            roomLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.alpha));

            TextView tvRoomTitle = new TextView(this);
            tvRoomTitle.setLayoutParams(wrapParams);
            tvRoomTitle.setText(roomTxt);
            tvRoomTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            tvRoomTitle.setTextColor(ContextCompat.getColor(this, R.color.jet));
            tvRoomTitle.setPadding(0, 8, 0, 8);

            roomLayout.addView(tvRoomTitle);

            final RelativeLayout fLayout = new RelativeLayout(this);
            fLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.layout_bottom_border));
            fLayout.setLayoutParams(params);

            TextView tvFacility = new TextView(this);
            tvFacility.setLayoutParams(wrapParams);
            tvFacility.setText(facilityTxt);
            tvFacility.setPadding(26, 26, 0, 26);

            final TextView facility = new TextView(this);
            facility.setLayoutParams(params);
            facility.setText(officeName);
            facility.setGravity(Gravity.RIGHT);
            facility.setPadding(0, 26, 26, 26);

            fLayout.addView(tvFacility);
            fLayout.addView(facility);

            final RelativeLayout rLayout = new RelativeLayout(this);
            rLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.layout_bottom_border));
            rLayout.setLayoutParams(params);

            TextView tvRoom = new TextView(this);
            tvRoom.setLayoutParams(myParams);
            tvRoom.setText(roomTxt);
            tvRoom.setPadding(26, 26, 0, 26);

            final TextView room = new TextView(this);
            room.setLayoutParams(params);
            room.setText(roomName);
            room.setGravity(Gravity.RIGHT);
            room.setPadding(0, 26, 26, 26);

            rLayout.addView(tvRoom);
            rLayout.addView(room);

            linearLayout.addView(roomLayout);
            linearLayout.addView(fLayout);
            linearLayout.addView(rLayout);

            mainLayout.addView(linearLayout);
        }

        final LinearLayout resLayout = new LinearLayout(this);
        resLayout.setLayoutParams(params);
        resLayout.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < listSize; i++) {
            //create list
            final int resId = reservation.getReservationList().get(i).getResId();

            // Reservation time
            String reservedTime = reservation.getReservationList().get(i).getTime();

            RelativeLayout.LayoutParams dParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            dParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            // Delete button
            ImageButton ibDelete = new ImageButton(this);
            ibDelete.setId(Integer.parseInt("6060"));
            ibDelete.setLayoutParams(dParams);
            ibDelete.setPadding(26, 26, 26, 26);
            ibDelete.setBackground(null);
            ibDelete.setImageResource(R.drawable.ic_delete);

            RelativeLayout.LayoutParams rParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            rParams.addRule(RelativeLayout.ALIGN_BASELINE, ibDelete.getId());
            rParams.addRule(RelativeLayout.START_OF, ibDelete.getId());

            // Reservation time
            TextView tvTime = new TextView(this);
            tvTime.setId(Integer.parseInt("5050"));
            tvTime.setLayoutParams(rParams);
            tvTime.setPadding(26, 26, 26, 26);
            tvTime.setText(reservedTime);

            final RelativeLayout relativeLayout = new RelativeLayout(this);
            relativeLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.layout_bottom_border));
            relativeLayout.setLayoutParams(params);

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.START_OF, tvTime.getId());

            // Reservation date
            TextView tvResDate = new TextView(this);
            tvResDate.setId(Integer.parseInt("8080"));
            tvResDate.setLayoutParams(lp);
            tvResDate.setText(reservationDate);
            tvResDate.setPadding(26, 26, 0, 26);

            ibDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LinearLayout linearLayout = findViewById(tag);
                    deleteReservation(resId, linearLayout, resLayout, relativeLayout);
                }
            });

            relativeLayout.addView(tvResDate);
            relativeLayout.addView(ibDelete);
            relativeLayout.addView(tvTime);

            resLayout.addView(relativeLayout);
        }

        LinearLayout layout = findViewById(tag);
        layout.addView(resLayout);

    }

    private void deleteReservation(final int resId, final LinearLayout linearLayout,
                                   final LinearLayout resLayout, final RelativeLayout relativeLayout) {

        String cancel = mSystem.getValue("btn_cancel") != null ?
                mSystem.getValue("btn_cancel"): getString(R.string.cancel);
        String message = mSystem.getValue("mess_delete_res") != null ?
                mSystem.getValue("mess_delete_res") : getString(R.string.delete_reservation);
        String delete = mSystem.getValue("button_delete") != null ?
                mSystem.getValue("button_delete") : getString(R.string.delete);

        AlertDialogHelper.showAlert(this, message, delete, cancel,
                new AlertDialogHelper.Callback() {
                    @Override
                    public void onPositiveButtonClick() {
                        Map<String,String> params = new HashMap<>();
                        params.put("sercret","jo8nefamehisd");
                        params.put("action","api");
                        params.put("ac","delete_resavation");
                        params.put("d","0");
                        params.put("lang", "en");
                        params.put("pid", String.valueOf(resId));

                        RequestQueue requestQueue = Volley.newRequestQueue(ReservedActivity.this);
                        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            boolean success = response.getBoolean("success");
                                            if (success) {
                                                int childCount = linearLayout.getChildCount();

                                                int viewCount = resLayout.getChildCount();
                                                if (viewCount <= 1) {
                                                    linearLayout.removeView(resLayout);
                                                    if (childCount <= 4) {
                                                        mainLayout.removeView(linearLayout);
                                                    }
                                                } else {
                                                    resLayout.removeView(relativeLayout);
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                            }
                        });

                        requestQueue.add(jsObjRequest);
                    }
                });
    }

    @Override
    public void onLoadReservationNew(Reservation reservation) {
        generateLayout(reservation);
    }

    private BroadcastReceiver logOutUser = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mHelper.showDeletedUserDialog();
        }
    };

    private void getSystemValues() {
        HappHelper helper = new HappHelper(this);
        GetSystemValue systemValue = new GetSystemValue(this);

        String reserved = systemValue.getValue("title_reserved");
        String create = systemValue.getValue("button_create");

        helper.setText(tbTitle, reserved);
        helper.setButtonText(btnCreate, create);
    }

}
