package com.bth.running.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bth.running.R;
import com.bth.running.adapter.BaseAdapter;
import com.bth.running.adapter.RunAdapter;
import com.bth.running.core.RunningApp;
import com.bth.running.running.Run;
import com.bth.running.storage.StorageManager;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;


public class HistoryFragment extends Fragment implements BaseAdapter.OnItemClickListener<Run> {


    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    protected StorageManager storageManager;

    @Bind(R.id.fragment_history_rv)
    protected RecyclerView recyclerView;

    @Bind(R.id.fragment_history_empty)
    protected TextView txtEmpty;

    private RunAdapter adapter;

    public HistoryFragment() {
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
        View v = inflater.inflate(R.layout.fragment_history, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onItemClick(Run run, View v) {

        getFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.main_content, HistoryDetailFragment.newInstance(run))
                .addToBackStack(null)
                .commit();
    }

    private void setupRecyclerView() {

        adapter = new RunAdapter(getContext(), storageManager.getRuns());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);

        if (adapter.getItemCount() == 0) {
            txtEmpty.animate().alpha(1).start();
        }

    }

}
