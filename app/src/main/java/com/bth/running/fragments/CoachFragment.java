package com.bth.running.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.bth.running.R;
import com.bth.running.coaching.Coach;
import com.bth.running.core.RunningApp;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CoachFragment extends Fragment {

    public static CoachFragment newInstance() {
        CoachFragment fragment = new CoachFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    protected Coach coach;

    @Bind(R.id.fragment_coach_body_edit_height)
    protected EditText editHeight;

    @Bind(R.id.fragment_coach_body_edit_weight)
    protected EditText editWeight;

    public CoachFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((RunningApp) getActivity().getApplication()).getAppComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_coach, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.fragment_coach_body_btn_update)
    protected void onClickBodyUpdate() {

        String textWeight = editWeight.getText().toString();
        String textHeight = editHeight.getText().toString();

        if (textHeight.isEmpty() || textWeight.isEmpty()) {
            Snackbar.make(getView(), "Body information can't be empty", Snackbar.LENGTH_SHORT).show();
            return;
        }
        coach.setUserBodyInformation(Integer.parseInt(textHeight), Double.parseDouble(textWeight));

        Snackbar.make(getView(), "Body information updated!", Snackbar.LENGTH_SHORT).show();
    }

    private void setupViews() {
        editWeight.setText(String.valueOf(coach.getUserWeight()));
        editHeight.setText(String.valueOf(coach.getUserHeight()));
    }

}
