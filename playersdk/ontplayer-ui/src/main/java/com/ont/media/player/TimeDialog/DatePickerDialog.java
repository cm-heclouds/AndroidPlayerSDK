package com.ont.media.player.TimeDialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ont.media.player.R;

import java.util.Calendar;

public class DatePickerDialog extends BaseDialogFragment {

    private Calendar startDate;
    private Calendar endDate;
    private Calendar selectedDate;
    private DateTimePickerView datePicker;

    public static DatePickerDialog newInstance(int type, ActionListener actionListener) {
        return BaseDialogFragment.newInstance(DatePickerDialog.class, type, actionListener);
    }

    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
        if (startDate != null && datePicker != null) {
            datePicker.setStartDate(startDate);
        }
    }

    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
        if (endDate != null && datePicker != null) {
            datePicker.setEndDate(endDate);
        }
    }

    public void setSelectedDate(Calendar selectedDate) {
        this.selectedDate = selectedDate;
        if (selectedDate != null && datePicker != null) {
            datePicker.setSelectedDate(selectedDate);
        }
    }

    @Override
    protected Dialog createDialog(Bundle savedInstanceState) {

        PickerViewDialog dialog = new PickerViewDialog(getActivity());
        dialog.setContentView(R.layout.date_picker_dialog);
        datePicker = dialog.findViewById(R.id.datePicker);
        attachActions(dialog.findViewById(R.id.done), dialog.findViewById(R.id.cancel));
        if (startDate != null) {
            datePicker.setStartDate(startDate);
        }
        if (endDate != null) {
            datePicker.setEndDate(endDate);
        }
        if (selectedDate != null) {
            datePicker.setSelectedDate(selectedDate);
        }
        return dialog;
    }

    @Override
    protected View createView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.date_picker_dialog, container, false);
        datePicker = view.findViewById(R.id.datePicker);
        attachActions(view.findViewById(R.id.done), view.findViewById(R.id.cancel));
        if (startDate != null) {
            datePicker.setStartDate(startDate);
        }
        if (endDate != null) {
            datePicker.setEndDate(endDate);
        }
        return view;
    }

    public Calendar getSelectedDate() {

        return datePicker.getSelectedDate();
    }
}
