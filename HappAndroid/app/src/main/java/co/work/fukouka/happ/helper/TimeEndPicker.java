package co.work.fukouka.happ.helper;


import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

public class TimeEndPicker implements TimePickerDialog.OnTimeSetListener {

    private TimeEndListener mListener;

    public interface TimeEndListener {
        void onEndTimeSet(int hour, int minutes);
    }

    public TimeEndPicker(TimeEndListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        mListener.onEndTimeSet(hourOfDay, minute);
    }
}
