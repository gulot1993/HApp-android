package co.work.fukouka.happ.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.adapter.MakeReservationAdapter;
import co.work.fukouka.happ.adapter.OfficeSpinAdapter;
import co.work.fukouka.happ.adapter.RoomSpinAdapter;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.helper.TimeEndPicker;
import co.work.fukouka.happ.helper.TimeStartPicker;
import co.work.fukouka.happ.model.Reservation;
import co.work.fukouka.happ.model.RoomOffice;
import co.work.fukouka.happ.presenter.ReservationPresenter;
import co.work.fukouka.happ.utils.SessionManager;
import co.work.fukouka.happ.view.ReservationView;
import io.fabric.sdk.android.Fabric;

public class MakeReservationActivity extends AppCompatActivity implements
        TimeStartPicker.TimeStartListener, TimeEndPicker.TimeEndListener, ReservationView {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.tv_title) TextView tvTitle;
    @BindView(R.id.tv_time_start) TextView tvTimeStart;
    @BindView(R.id.tv_time_end) TextView tvTimeEnd;
    @BindView(R.id.btn_save) Button btnSave;
    @BindView(R.id.tv_room) TextView tvRoom;
    @BindView(R.id.tv_make_res) TextView tvMakeRes;
    @BindView(R.id.tv_reserved) TextView tvReserved;
    @BindView(R.id.spnr_office) Spinner spnrOffice;
    @BindView(R.id.spnr_room) Spinner spnrRoom;
    @BindView(R.id.tv_facility) TextView tvFacility;
    @BindView(R.id.tv_room_sub) TextView tvRoomSub;
    @BindView(R.id.tv_start) TextView tvStart;
    @BindView(R.id.tv_end) TextView tvEnd;

    private ReservationPresenter mPresenter;
    private HappHelper mHelper;
    private MakeReservationAdapter mAdapter;
    private RoomSpinAdapter roomSpinAdapter;
    private OfficeSpinAdapter officeSpinAdapter;
    private SessionManager mSession;

    private String mSelectedDate;
    private String mStartTime;
    private String mEndTime;
    private int roomId;
    private int officeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_reservation);
        ButterKnife.bind(this);
        Fabric.with(this, new Crashlytics());
        getSystemValues();

        mPresenter = new ReservationPresenter(this, this);
        mHelper = new HappHelper(this);
        mAdapter = new MakeReservationAdapter(this);

        mHelper.setUpToolbar(toolbar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(mAdapter);

        Intent intent = getIntent();
        int year = intent.getIntExtra("year", 0);
        int month = intent.getIntExtra("month", 0);
        int day = intent.getIntExtra("day", 0);

        if (year != 0) {
            String language;
            String newDate;

            language = mPresenter.getLanguage();

            mSelectedDate = String.valueOf(year) + "-" + String.valueOf(month) + "-" +String.valueOf(day);

            newDate = mSelectedDate;

            if (!language.equals("en")) {
                newDate = String.valueOf(year)+"年"+String.valueOf(month)+"月"+String.valueOf(day)+"日";
            }

            tvTitle.setText(newDate);
        }

        setResDateToCurrentDate();
        getReservations();

        mPresenter.getMeetingOffice();

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
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_top);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.happ.check_user_session");
        this.registerReceiver(this.userSession, filter);

        IntentFilter filter1 = new IntentFilter();
        filter1.addAction("com.happ.check_user_session");
        this.registerReceiver(this.userSession, filter1);
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.unregisterReceiver(this.userSession);
    }


    @OnClick({R.id.tv_time_start, R.id.tv_time_end, R.id.btn_save})
    public void onClicks(View view) {
        switch (view.getId()) {
            case R.id.tv_time_start:
                TimeStartPicker startPicker = new TimeStartPicker(this);
                TimePickerDialog timeStartPicker = TimePickerDialog.newInstance(
                        startPicker, 8, 0, true
                );
                timeStartPicker.setTimeInterval(1, 30);
                timeStartPicker.show(getFragmentManager(), "Timepickerdialog");
                break;
            case R.id.tv_time_end:
                TimeEndPicker endPicker = new TimeEndPicker(this);
                TimePickerDialog timeEndPicker = TimePickerDialog.newInstance(
                        endPicker, 23, 0, true
                );
                timeEndPicker.setTimeInterval(1, 30);
                timeEndPicker.show(getFragmentManager(), "Timepickerdialog");
                break;
            case R.id.btn_save:
                makeReservation();
                break;
        }
    }

    @Override
    public void onStartTimeSet(int hour, int minutes) {
        String startHour = hour < 10 ? "0" +String.valueOf(hour) : String.valueOf(hour);
        String strMinutes = String.valueOf(minutes);

        if (minutes < 10) {
            strMinutes =  "0" +strMinutes;
        }

        mStartTime = startHour + ":" + strMinutes;
        tvTimeStart.setText(mStartTime);
    }

    @Override
    public void onEndTimeSet(int hour, int minutes) {
        String endHour = hour < 10 ? "0" +String.valueOf(hour) : String.valueOf(hour);

        String strMinutes = String.valueOf(minutes);

        if (minutes < 10) {
            strMinutes =  "0" +strMinutes;
        }

        mEndTime = endHour + ":" + strMinutes;
        tvTimeEnd.setText(mEndTime);
    }

    private void makeReservation() {
        mHelper.showProgressDialog("", false);
        String startDate = mSelectedDate + " " +mStartTime;
        String endDate = mSelectedDate + " " +mEndTime;

        mPresenter.makeReservation(String.valueOf(roomId), startDate, endDate);
    }

    private void getReservations() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(dateFormat.parse(mSelectedDate));
            calendar.add(Calendar.DATE, 1);

            String startDate = mSelectedDate + " 00:00:00";
            String endDate = mSelectedDate+ " 23:59:00";

            mPresenter.getDayReservations(startDate, endDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void setResDateToCurrentDate() {
        final Calendar c = Calendar.getInstance();
//        int hour = c.get(Calendar.HOUR_OF_DAY);
//        int minute = c.get(Calendar.MINUTE);

        String startHour = "08";
        String startMinute = "00";
        String endHour = "23";
        String endMinute = "00";

        String startDate = startHour + ":" +startMinute;
        String endDate = endHour + ":" +endMinute;

        tvTimeStart.setText(startDate);
        tvTimeEnd.setText(endDate);

        mStartTime = startDate;
        mEndTime = endDate;
    }

    @Override
    public void onLoadReservation(Reservation reservation) {
        mAdapter.addReservations(reservation);
    }

    @Override
    public void onLoadReservation(String officeName, String roomName, List<Reservation> reservations) {

    }


    @Override
    public void onLoadMeetingRoom(List<RoomOffice> room) {
        roomSpinAdapter = new RoomSpinAdapter(this, R.layout.layout_spinner_item, room);
        spnrRoom.setAdapter(roomSpinAdapter);

        spnrRoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                RoomOffice roomOffice = roomSpinAdapter.getItem(i);
                roomId = roomOffice.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onLoadMeetingRoom(RoomOffice room, RoomOffice office) {

    }

    @Override
    public void onLoadOffice(RoomOffice office) {
    }

    @Override
    public void onLoadOffice(List<RoomOffice> office) {
        officeSpinAdapter = new OfficeSpinAdapter(this, R.layout.layout_spinner_item, office);
        spnrOffice.setAdapter(officeSpinAdapter);

        spnrOffice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                RoomOffice roomOffice = officeSpinAdapter.getItem(i);
                officeId = roomOffice.getId();
                mPresenter.getMeetingRooms(officeId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onSuccess(String message) {
        mHelper.hideProgressDialog();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFailed(String message) {
        mHelper.hideProgressDialog();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private BroadcastReceiver userSession = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mHelper.showDeletedUserDialog();
        }
    };

    private BroadcastReceiver logoutUser = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mHelper.showDeletedUserDialog();
        }
    };

    private void getSystemValues() {
        HappHelper helper = new HappHelper(this);
        GetSystemValue systemValue = new GetSystemValue(this);

        String save = systemValue.getValue("button_save");
        String room = systemValue.getValue("label_room");
        String reserved = systemValue.getValue("title_reserved");
        String facility = systemValue.getValue("lbl_facility");
        String makeRes = systemValue.getValue("subtitle_make_reservation");
        String start = systemValue.getValue("label_start");
        String end = systemValue.getValue("label_send");

        helper.setButtonText(btnSave, save);
        helper.setText(tvRoom, room);
        helper.setText(tvReserved, reserved);
        helper.setText(tvMakeRes, makeRes);
        helper.setText(tvFacility, facility);
        helper.setText(tvRoomSub, room);
        helper.setText(tvStart, start);
        helper.setText(tvEnd, end);
    }

//    @Override
//    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
//        String hourString = hourOfDay < 10 ? "0"+hourOfDay : ""+hourOfDay;
//        String minuteString = minute < 10 ? "0"+minute : ""+minute;
//        String secondString = second < 10 ? "0"+second : ""+second;
//        String time = "You picked the following time: "+hourString+"h"+minuteString+"m"+secondString+"s";
//
//        System.out.println("Time " +time);
//    }
}
