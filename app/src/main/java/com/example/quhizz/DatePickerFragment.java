package com.example.pickerfordate;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.quhizz.R;
import com.example.quhizz.Register;
import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    public DatePickerFragment() {
    }

    public static DatePickerFragment newInstance() {
        return new DatePickerFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, dayOfMonth);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(year, month, day);

        Calendar currentDate = Calendar.getInstance();

        if (selectedDate.after(currentDate)) {

            Toast.makeText(getActivity(), "Please select a date before today.", Toast.LENGTH_SHORT).show();
        } else {
            Register activity = (Register) getActivity();
            if (activity != null) {
                activity.processDatePickerResult(year, month, day);

                EditText editText = activity.findViewById(R.id.set_Birth);
                if (editText != null) {
                    String selectedDateText = String.valueOf((month + 1)) + "/" + day + "/" + year;
                    editText.setText(selectedDateText);
                }
            }
        }
    }
}
