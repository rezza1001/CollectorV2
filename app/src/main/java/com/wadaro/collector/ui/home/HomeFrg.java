package com.wadaro.collector.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wadaro.collector.R;
import com.wadaro.collector.adapter.HomeMenuGridViewAdapter;
import com.wadaro.collector.api.Config;
import com.wadaro.collector.database.table.UserDB;
import com.wadaro.collector.model.ItemObject;
import com.wadaro.collector.util.MyPreference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFrg extends Fragment implements HomeMenuGridViewAdapter.OnRecyclerViewClickListener{

    private UserDB userDB;

    public HomeFrg() {
        // Required empty public constructor
    }


    public static HomeFrg newInstance() {
        HomeFrg fragment = new HomeFrg();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userDB = new UserDB();
        userDB.getData(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ui_home_fragment, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView gridView = view.findViewById(R.id.rvGrid);

        gridView.setHasFixedSize(true);

        //set layout manager and adapter for "GridView"
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        gridView.setLayoutManager(layoutManager);
        HomeMenuGridViewAdapter gridViewAdapter = new HomeMenuGridViewAdapter(getActivity(), getAllItemObject());
        gridViewAdapter.setOnItemClickListener(this);
        gridView.setAdapter(gridViewAdapter);



    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }



    @Override
    public void onItemClick(Integer position) {
        if(position == 0) {
            Objects.requireNonNull(getActivity()).sendBroadcast(new Intent("OPEN_ASSIGNMENT"));
        } else if(position == 1){
            Objects.requireNonNull(getActivity()).sendBroadcast(new Intent("DELIVERY"));
        } else if(position == 2){
            Objects.requireNonNull(getActivity()).sendBroadcast(new Intent("REJECT"));
        } else if(position == 3){
            Objects.requireNonNull(getActivity()).sendBroadcast(new Intent("OVER_CREDIT"));
        } else if(position == 4){
            Objects.requireNonNull(getActivity()).sendBroadcast(new Intent("OPEN_PROFILE"));
        }
        else if (position == 5){
            Intent intent = new Intent(getContext(), com.rentas.ppob.LauncherPPOBActivity.class);
            intent.putExtra("email",userDB.email);
            intent.putExtra("name",userDB.name);
            intent.putExtra("phone",userDB.phone);
            intent.putExtra("employee_id",userDB.employee_id);
            intent.putExtra("company_id",userDB.company_id);
            intent.putExtra("avatar", Config.PROFILE_PATH + userDB.photo);
            MyPreference.save(getContext(),"WADARO_NAME", userDB.name);
            startActivity(intent);
        }
    }

    private List<ItemObject> getAllItemObject(){
        List<ItemObject> items = new ArrayList<>();
        items.add(new ItemObject("Penugasan Penagihan", "ic_assignment"));
        items.add(new ItemObject("Data Penagihan", "ic_datasurvey"));
        items.add(new ItemObject("Rekap Hasil", "ic_recap"));
        items.add(new ItemObject("Over Kredit", "ic_over_credit"));
        items.add(new ItemObject("Profil", "ic_profile"));
        items.add(new ItemObject("PPOB", "ic_ppob"));
        return items;
    }

}
