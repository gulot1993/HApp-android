package co.work.fukouka.happ.helper;


import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

public class TimeStartPicker implements TimePickerDialog.OnTimeSetListener {

    TimeStartListener mListener;

    public interface TimeStartListener {
        void onStartTimeSet(int hour, int minutes);
    }

    public TimeStartPicker(TimeStartListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        mListener.onStartTimeSet(hourOfDay, minute);
    }
}
