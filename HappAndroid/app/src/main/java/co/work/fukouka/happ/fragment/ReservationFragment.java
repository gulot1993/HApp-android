package co.work.fukouka.happ.fragment;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.activity.ReservedActivity;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReservationFragment extends Fragment implements CalendarView.OnDateChangeListener {

    @BindView(R.id.calendar_view) CalendarView calendar;
    @BindView(R.id.tb_title) TextView tbTitle;
    @BindView(R.id.btn_reserved) Button btnReserved;

    private HappHelper helper;
    private GetSystemValue systemValue;

    public ReservationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservation, container, false);
        ButterKnife.bind(this, view);

        helper = new HappHelper(getActivity());
        systemValue = new GetSystemValue(getActivity());

        String language = helper.getLanguage();
        Locale locale;
        if (language.equals("jp")) {
            locale = new Locale("ja");
        } else {
            locale = new Locale("en");
        }

        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getActivity().getBaseContext().getResources().updateConfiguration(config,
                getActivity().getBaseContext().getResources().getDisplayMetrics());

        assignSystemValues();

        calendar.setOnDateChangeListener(this);
        calendar.setFocusedMonthDateColor(ContextCompat.getColor(getActivity(), R.color.alpha));

        return view;
    }

    @OnClick(R.id.btn_reserved)
    public void onClicks(View view) {
        Intent intent = new Intent(getActivity(), ReservedActivity.class);
        intent.putExtra("from_fragment", true);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
    }

    @Override
    public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
        final Calendar c = Calendar.getInstance();
        String currentYear = String.valueOf(c.get(Calendar.YEAR));
        String currentMonth = String.valueOf(c.get(Calendar.MONTH));
        String currentDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));

        String currentDate = currentYear + "-" + currentMonth + "-" + currentDay;
        String selectedDate = String.valueOf(year) + "-" +String.valueOf(month) + "-" +String.valueOf(day);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date cDate = sdf.parse(currentDate);
            Date sDate = sdf.parse(selectedDate);

            if (sDate.before(cDate)) {
                helper.throwToastMessage(systemValue.getValue("mess_start_date_less_than"),
                        getString(R.string.error_select_date));
            } else {
                Intent intent = new Intent(getActivity(), ReservedActivity.class);
                intent.putExtra("year", year);
                intent.putExtra("month", month + 1);
                intent.putExtra("day", day);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void assignSystemValues() {
        String reservation = systemValue.getValue("title_room_reservation");
        String reserved = systemValue.getValue("title_reserved");

        helper.setText(tbTitle, reservation);
        helper.setButtonText(btnReserved, reserved);
    }
}
