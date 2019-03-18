package co.work.fukouka.happ.helper;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.EditText;


public class GenericTextWatcher implements TextWatcher {

    private EditText mEditText;
    public GenericTextWatcher(EditText editText) {
        this.mEditText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (charSequence.length() > 0) {
            mEditText.setError(null);
            //mEditText.setGravity(Gravity.CENTER_HORIZONTAL);
            mEditText.setPadding(0, 0 , 16, 0);
        } else {
            mEditText.setGravity(Gravity.RIGHT);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
