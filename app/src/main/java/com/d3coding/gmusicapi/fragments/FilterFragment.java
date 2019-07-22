package com.d3coding.gmusicapi.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.d3coding.gmusicapi.R;

public class FilterFragment extends DialogFragment {

    OnResultListener onResultListener;

    public FilterFragment() {
    }

    public static FilterFragment newInstance(Bundle args) {
        FilterFragment frag = new FilterFragment();
        frag.setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_AlertDialog);
        frag.setArguments(args);
        return frag;
    }

    public void setOnResultListener(OnResultListener mOnResultListener) {
        onResultListener = mOnResultListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().setTitle(getString(R.string.act_icon_filter));

        Spinner spinner_organize = view.findViewById(R.id.filter_organize);
        Spinner spinner_filter = view.findViewById(R.id.filter_filter);
        ArrayAdapter<CharSequence> adapter_organize = ArrayAdapter.createFromResource(getContext(), R.array.organize_by, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapter_filter = ArrayAdapter.createFromResource(getContext(), R.array.filter_by, android.R.layout.simple_spinner_item);
        adapter_organize.setDropDownViewResource(R.layout.spinner_simple_text_box);
        adapter_filter.setDropDownViewResource(R.layout.spinner_simple_text_box);
        spinner_organize.setAdapter(adapter_organize);
        spinner_filter.setAdapter(adapter_filter);

        spinner_organize.setSelection(getArguments().getInt("sort", 0));
        spinner_filter.setSelection(getArguments().getInt("sort_online", 0));

        CheckBox checkBox = view.findViewById(R.id.checkbox_ascend);
        checkBox.setChecked(getArguments().getBoolean("desc", false));

        Button button = view.findViewById(R.id.button_filter);
        button.setOnClickListener((vView) -> {
            Bundle args = new Bundle();
            args.putInt("sort", spinner_organize.getSelectedItemPosition());
            args.putInt("sort_online", spinner_filter.getSelectedItemPosition());
            args.putBoolean("desc", checkBox.isChecked());
            onResultListener.OnResult(args);
            this.dismiss();
        });

    }

    public interface OnResultListener {
        void OnResult(Bundle result);
    }
}