/*
package com.lujayn.wootouch_V2.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import R;
import com.lujayn.wootouch_V2.activity.MainActivity;
import com.lujayn.wootouch_V2.activity.ProductActivity;
import com.lujayn.wootouch_V2.activity.SubCategoryActivity;
import com.lujayn.wootouch_V2.bean.BeanCart;
import com.lujayn.wootouch_V2.bean.BeanCategory;
import com.lujayn.wootouch_V2.common.AppConstant;

import com.lujayn.wootouch_V2.custom.AppDebugLog;
import com.lujayn.wootouch_V2.database.DataWrite;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import dmax.dialog.SpotsDialog;

*/
/**
 * Created by Shailesh on 27/07/16.
 *//*


public class HomeFragment extends Fragment implements MainActivity.OnBackPressedListener {
    private Boolean exit = false;
    GridView gridView;
    ArrayList<BeanCart> cartlist;
    ArrayList<BeanCategory> categorieslist;
    DataWrite dataWrite;
    private AlertDialog progressDialog;
    private static final String LOG_TAG = "CheckNetworkStatus";
    //private NetworkChangeReceiver receiver;
    private boolean isConnected = false;
    // Keep all Images in array
 */
/*   public int[] mThumbIds = {
            R.drawable.shirt, R.drawable.shoes,
            R.drawable.t_shirt, R.drawable.jewellery,
            R.drawable.accesorries, R.drawable.ballpen,
            R.drawable.i_watch, R.drawable.home};*//*


    //String[] name = {"SHIRT","SHOES","T-SHIRS","JEWELLERY","ACCESORIES","VIEW ALL CATEGORY","MOST POPULAR","RECENT POST"};
    View rootView;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        */
/*receiver = new NetworkChangeReceiver();
        getActivity().registerReceiver(receiver, filter);*//*

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        ((MainActivity) getActivity()).setOnBackPressedListener(this);
        progressDialog = new SpotsDialog(getActivity(), R.style.Custom);
        gridView = (GridView) rootView.findViewById(R.id.gv);


        dataWrite = new DataWrite(getActivity());
        dataWrite.open();
        AppConstant.counter = "" + dataWrite.getCartCount();

        MainActivity.setCounter(AppConstant.counter);

        homecategory("home");

        final CategoryFragment fragment = new CategoryFragment();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            String title;

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                switch (i) {
                    case 0:
                        categoryhome(i);
                        break;
                    case 1:
                        categoryhome(i);
                        break;
                    case 2:
                        categoryhome(i);
                        break;
                    case 3:
                        categoryhome(i);
                        break;
                    case 4:
                        categoryhome(i);
                        break;
                    case 5:
                        FragmentManager fragmentManager = getFragmentManager();
                        title = getString(R.string.title_category);
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container_body, fragment);
                        fragmentTransaction.commit();
                        ((MainActivity) getActivity()).Title(title);
                        break;
                    case 6:

                        Intent intent = new Intent(getActivity(), ProductActivity.class);
                        // intent.putExtra("beanSearchProducts", beanSearchProducts);
                        getActivity().startActivity(intent);
*/
/*                        Fragment fr=new ProductFragment();
                        title = getString(R.string.title_popular);
                        FragmentManager fm=getFragmentManager();
                        FragmentTransaction ft=fm.beginTransaction();
                        Bundle args = new Bundle();
                        args.putString("catName","popular");
                        fr.setArguments(args);
                        ft.replace(R.id.container_body, fr);
                        ft.commit();

                        ((MainActivity) getActivity()).Title(title);*//*


                        break;
                    case 7:

                        Intent intent2 = new Intent(getActivity(), ProductActivity.class);
                        // intent.putExtra("beanSearchProducts", beanSearchProducts);
                        getActivity().startActivity(intent2);
*/
/*
                        Fragment fr1=new ProductFragment();
                        title = getString(R.string.title_recent);
                        FragmentManager fm1=getFragmentManager();
                        FragmentTransaction ft1=fm1.beginTransaction();
                        Bundle args1 = new Bundle();
                        args1.putString("catName","recent");
                        fr1.setArguments(args1);
                        ft1.replace(R.id.container_body, fr1);
                        ft1.commit();

                        ((MainActivity) getActivity()).Title(title);*//*

                        break;
                }

            }

            private void categoryhome(int i) {
                if (categorieslist.get(i).getChild().equals("1")) {


                    Intent intent = new Intent(getActivity(), SubCategoryActivity.class);
                    intent.putExtra("beanSearchProducts", beanSearchProducts);
                    startActivity(intent);


                    startActivity(new Intent(getActivity(), SubCategoryActivity.class));

    */
/*                Fragment fr = new SubCategoryFragment();
                    String title = categorieslist.get(i).getCategory_name();
                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    Bundle args = new Bundle();
                    args.putString("catID", categorieslist.get(i).getCategory_id());
                    args.putString("catname", categorieslist.get(i).getCategory_name());
                    fr.setArguments(args);
                    ft.replace(R.id.container_body, fr);
                    ft.commit();

                    ((MainActivity) getActivity()).Title(title);*//*


                } else {


                    Intent intent = new Intent(getActivity(), ProductActivity.class);
                    // intent.putExtra("beanSearchProducts", beanSearchProducts);
                    getActivity().startActivity(intent);
     */
/*               Fragment fr=new ProductFragment();
                    String title = categorieslist.get(i).getCategory_name();
                    FragmentManager fm=getFragmentManager();
                    FragmentTransaction ft=fm.beginTransaction();
                    Bundle args = new Bundle();
                    args.putString("catslug", categorieslist.get(i).getSlug());
                    args.putString("catName",categorieslist.get(i).getCategory_name());
                    fr.setArguments(args);
                    ft.replace(R.id.container_body, fr);
                    ft.commit();

                    ((MainActivity) getActivity()).Title(title);*//*

                }
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    private void homecategory(String home) {
        progressDialog.show();
    */
/*    Intent intent=new Intent(getActivity(), Intent_Class_Data.class);
        intent.putExtra("url", Webservice.BASE_URL+""+Webservice.URL_HOME_CATEGORY);
        intent.putExtra("type", home);
        getActivity().startService(intent);*//*

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void doBack() {
        if (exit) {
            getActivity().finish(); // finish activity
        } else {
            Toast.makeText(getActivity(), R.string.press_back_again_to_exit,
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }
    }


    BroadcastReceiver receive = new BroadcastReceiver() {
        JSONObject object;

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            Bundle bundle = intent.getExtras();
            String data = bundle.getString("");
            categorieslist = new ArrayList<BeanCategory>();
            if (bundle != null) {

                if (bundle.getString("type").equalsIgnoreCase("home")) {

                    AppDebugLog.println("home category is== " + data);

                    try {
                        object = new JSONObject(data);

                        if (object.getInt("success") == 1) {
                            JSONObject jsonObject = object.optJSONObject("data");
                            JSONArray jsonArray = jsonObject.optJSONArray("product_categories");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object1 = jsonArray.optJSONObject(i);

                                BeanCategory bean = new BeanCategory();
                                bean.setCategory_id(object1.getString("category_id"));
                                bean.setCategory_name(object1.getString("category_name"));
                                bean.setDescription(object1.getString("description"));
                                bean.setImage(object1.getString("image"));
                                bean.setCount(object1.getString("count"));
                                bean.setSlug(object1.getString("slug"));
                                bean.setChild(object1.getString("child"));

                                categorieslist.add(bean);

                            }

                            BeanCategory beanCategory = new BeanCategory();
                            beanCategory.setCategory_id("viewall");
                            beanCategory.setCategory_name("VIEW ALL CATEGORY");
                            beanCategory.setImage("http://phpjoomlacoders.com/wp/wp-content/uploads/2013/06/mont-blanc-web.jpg");
                            categorieslist.add(beanCategory);

                            BeanCategory beanCategory1 = new BeanCategory();
                            beanCategory1.setCategory_id("popular");
                            beanCategory1.setCategory_name("MOST POPULAR");
                            beanCategory1.setImage("http://phpjoomlacoders.com/wp/wp-content/uploads/2013/06/Attractive-Prefab-Home-Toby-Long_3.jpg");
                            categorieslist.add(beanCategory1);

                            BeanCategory beanCategory2 = new BeanCategory();
                            beanCategory2.setCategory_id("recent");
                            beanCategory2.setCategory_name("RECENT POST");
                            beanCategory2.setImage("http://phpjoomlacoders.com/wp/wp-content/uploads/2016/07/cropped-computer-accessories-sydney1.png");
                            categorieslist.add(beanCategory2);

                            HomeAdapter productCategoryAdapter = new HomeAdapter(categorieslist);
                            gridView.setAdapter(productCategoryAdapter);

                            progressDialog.dismiss();

                        } else {
                            Toast.makeText(getActivity(), "Unknown error",
                                    Toast.LENGTH_LONG).show();
                        }


                    } catch (Exception e) {
                        // TODO: handle exception

                        AppDebugLog.println("error =" + e);
                    }


                }
            }


        }


    };


    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        // getActivity().registerReceiver(receive, new IntentFilter(Intent_Class_Data.NOTIFICATION));

    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        getActivity().unregisterReceiver(receive);

    }

    public class HomeAdapter extends BaseAdapter {

        LayoutInflater inflater;
        ArrayList<BeanCategory> categoryArrayList;

        public HomeAdapter(ArrayList<BeanCategory> categorieslist) {

            this.categoryArrayList = categorieslist;
            inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public int getCount() {
            return categoryArrayList.size();
        }

        @Override
        public Object getItem(int i) {
            return categoryArrayList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            final ViewHolder vh;

            if (view == null) {
                vh = new ViewHolder();
                view = inflater.inflate(R.layout.inflater_category, null);
                vh.iv_categoryImage = (ImageView) view.findViewById(R.id.ivCategoryImage);
                vh.tv_categoryName = (TextView) view.findViewById(R.id.tvCategoryname);


                view.setTag(vh);
            } else {

                vh = (ViewHolder) view.getTag();
            }

            if (i == 5) {
                vh.tv_categoryName.setTextSize(12);
            }
            vh.tv_categoryName.setText(categoryArrayList.get(i).getCategory_name());


*/
/*
            Picasso.with(getActivity())
                    .load(categoryArrayList.get(i).getImage())
                    .into(vh.iv_categoryImage);*//*



            return view;
        }

        public class ViewHolder {
            TextView tv_categoryName;
            ImageView iv_categoryImage;

        }
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {


            AppDebugLog.println("Receieved notification about network status");
            isNetworkAvailable(context);

        }


        private boolean isNetworkAvailable(Context context) {
            ConnectivityManager connectivity = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null) {
                    for (int i = 0; i < info.length; i++) {
                        if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                            if (!isConnected) {

                                AppDebugLog.println("Now you are connected to Internet!");
                                //networkStatus.setText("Now you are connected to Internet!");
                                isConnected = true;
                                //do your processing here ---
                                //if you need to post any data to the server or get status
                                //update from the server

                                Toast.makeText(getActivity(), "Now you are connected to Internet!", Toast.LENGTH_LONG).show();
                                final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                                alertDialog.setTitle("Connection Alert");
                                alertDialog.setMessage("Now you are connected to Internet!");
                                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        alertDialog.dismiss();
                                    }
                                });
                                // Showing Alert Message
                                alertDialog.show();
                            }
                            return true;
                        }
                    }
                }
            }

            AppDebugLog.println("You are not connected to Internet!");
            //networkStatus.setText("You are not connected to Internet!");
            isConnected = false;

            final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle("Connection Alert");
            alertDialog.setMessage("You are not connected to Internet!");
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                }
            });
            // Showing Alert Message
            alertDialog.show();


            return false;
        }
    }

}
*/
