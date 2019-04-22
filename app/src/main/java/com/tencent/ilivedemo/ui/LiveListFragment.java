package com.tencent.ilivedemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tencent.ilivedemo.R;
import com.tencent.ilivedemo.demos.DemoGuest;

public class LiveListFragment extends Fragment{

    private Button button;
    private EditText roomId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.liveframent_layout, null);

        intiView(view);
        setListener();

        return view;
    }

    private void setListener() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "............." , Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), DemoGuest.class).putExtra("room_ID", roomId.getText().toString()));
            }
        });
    }

    private void intiView(View view) {
        button = (Button) view.findViewById(R.id.test);
        roomId = (EditText) view.findViewById(R.id.input_room_id);
    }
}