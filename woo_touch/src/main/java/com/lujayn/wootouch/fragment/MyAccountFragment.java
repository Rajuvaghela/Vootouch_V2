package com.lujayn.wootouch.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lujayn.wootouch.R;
import com.lujayn.wootouch.activity.ChangePasswordActivity;
import com.lujayn.wootouch.activity.EditProfileActivity;
import com.lujayn.wootouch.activity.LoginActivity;
import com.lujayn.wootouch.activity.MainActivity;
import com.lujayn.wootouch.activity.PastOrderActivity;
import com.lujayn.wootouch.bean.BeanSearchProduct;
import com.lujayn.wootouch.bean.SettingOption;
import com.lujayn.wootouch.common.AppConstant;
import com.lujayn.wootouch.common.ApplicationContext;
import com.lujayn.wootouch.common.SessionManager;
import com.lujayn.wootouch.database.DataWrite;

import java.util.ArrayList;

/**
 * Created by Shailesh on 10/08/16.
 */
public class MyAccountFragment extends Fragment implements MainActivity.OnBackPressedListener {
    View rootView;
    //RelativeLayout rl_profile,rl_changepass,rl_logout,rl_orders, rl_myaccount;
    LinearLayout rl_profile, rl_changepass, rl_logout, rl_orders, rl_myaccount;
    SessionManager manager;
    DataWrite dataWrite;
    TextView tv_logout;
    ArrayList<BeanSearchProduct> beanSearchProducts = new ArrayList<>(); //change

    public MyAccountFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = this.getArguments(); //change
        beanSearchProducts = bundle.getParcelableArrayList("beanSearchProducts"); //change
        rootView = inflater.inflate(R.layout.inflater_myaccount, container, false);
        ((MainActivity) getActivity()).setOnBackPressedListener(this);
        manager = new SessionManager();
        dataWrite = new DataWrite(getActivity());
        dataWrite.open();

      /*  rl_changepass = (RelativeLayout)rootView.findViewById(R.id.rl_changepass);
        rl_profile = (RelativeLayout)rootView.findViewById(R.id.rl_profile);
        rl_logout = (RelativeLayout)rootView.findViewById(R.id.rl_logout);
        rl_orders = (RelativeLayout)rootView.findViewById(R.id.rl_orders);
        rl_myaccount = (RelativeLayout)rootView.findViewById(R.id.rl_myAccount);
        tv_logout = (TextView)rootView.findViewById(R.id.tvLogout);


        String status=manager.getPreferences(getActivity(), AppConstant.STATUS);

        if (status.equals("1")){
            tv_logout.setText("Logout");
        }else{
            tv_logout.setText("Login");
        }

        SettingOption settingOption = new SettingOption();
        settingOption = manager.getPreferencesOption(getActivity(),AppConstant.SETTING_OPTION);
        String color_back = settingOption.getData().getOptions().getBack_color();

        rl_myaccount.setBackgroundColor(Color.parseColor(color_back));

        rl_changepass.setOnClickListener(onClickMethod);
        rl_profile.setOnClickListener(onClickMethod);
        rl_logout.setOnClickListener(onClickMethod);
        rl_orders.setOnClickListener(onClickMethod);*/


        rl_changepass = (LinearLayout) rootView.findViewById(R.id.ll_changepass);
        rl_profile = (LinearLayout) rootView.findViewById(R.id.ll_profile);
        rl_logout = (LinearLayout) rootView.findViewById(R.id.ll_logout);
        rl_orders = (LinearLayout) rootView.findViewById(R.id.ll_orders);
        rl_myaccount = (LinearLayout) rootView.findViewById(R.id.ll_myAccount);
        tv_logout = (TextView) rootView.findViewById(R.id.tvLogout1);


        String status = manager.getPreferences(getActivity(), AppConstant.STATUS);

        if (status.equals("1")) {
            tv_logout.setText(getString(R.string.logout));
        } else {
            tv_logout.setText(getString(R.string.login));
        }

        SettingOption settingOption = new SettingOption();
        settingOption = manager.getPreferencesOption(getActivity(), AppConstant.SETTING_OPTION);
        String color_back = settingOption.getData().getOptions().getStatus_bar_color();

        // rl_myaccount.setBackgroundColor(Color.parseColor(color_back));

        rl_changepass.setOnClickListener(onClickMethod);
        rl_profile.setOnClickListener(onClickMethod);
        rl_logout.setOnClickListener(onClickMethod);
        rl_orders.setOnClickListener(onClickMethod);
        return rootView;
    }


    private View.OnClickListener onClickMethod = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Fragment fragment = null;
            String title = getString(R.string.app_name);
            switch (view.getId()) {
                case R.id.ll_profile:
                    startActivity(new Intent(getActivity(), EditProfileActivity.class));
                    break;
                case R.id.ll_changepass:

                    startActivity(new Intent(getActivity(), ChangePasswordActivity.class));

                    break;
                case R.id.ll_orders:
                    startActivity(new Intent(getActivity(), PastOrderActivity.class));

                    break;
                case R.id.ll_logout:

                    if (tv_logout.getText().toString().equals("Login")) {

                        Intent loginInt = new Intent(ApplicationContext.getAppContext(), LoginActivity.class);
                        startActivity(loginInt);
                        getActivity().finish();

                    } else {

                        manager.setPreferences(getActivity(), "status", "0");
                        dataWrite.deletedData();
                        Intent in = new Intent(ApplicationContext.getAppContext(), MainActivity.class);
                        startActivity(in);
                        getActivity().finish();

                    }

                    break;
            }

            if (fragment != null) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container_body, fragment);
                fragmentTransaction.commit();

                // set the toolbar title
                ((MainActivity) getActivity()).Title(title);
            }

        }
    };

    @Override
    public void doBack() {
        Bundle bundle = new Bundle();
        Fragment fr = new CategoryFragment();
        String title = getString(R.string.title_category);
        bundle.putParcelableArrayList("beanSearchProducts", beanSearchProducts);
        fr.setArguments(bundle);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container_body, fr);
        ft.commit();
        ((MainActivity) getActivity()).Title(title);
    }
}
