package com.lujayn.wootouch.activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lujayn.wootouch.R;
import com.lujayn.wootouch.adapter.AdapterSearch;
import com.lujayn.wootouch.bean.BeanCart;
import com.lujayn.wootouch.bean.BeanSearchProduct;
import com.lujayn.wootouch.bean.SettingOption;
import com.lujayn.wootouch.common.AppConstant;
import com.lujayn.wootouch.common.ApplicationContext;
import com.lujayn.wootouch.common.SessionManager;
import com.lujayn.wootouch.common.Webservice;
import com.lujayn.wootouch.database.DataWrite;
import com.lujayn.wootouch.fragment.CategoryFragment;
import com.lujayn.wootouch.fragment.ContactUsFragment;
import com.lujayn.wootouch.fragment.FragmentDrawer;
import com.lujayn.wootouch.fragment.MyAccountFragment;
import com.lujayn.wootouch.fragment.TermAndConditionFragment;
import com.lujayn.wootouch.fragment.WishlistFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener {
    private static TextView tv_Title, tv_counter, tv_counter_wish;
    private Boolean exit = false;
    private static String TAG = MainActivity.class.getSimpleName();
    TextView tv_logout;
    private Toolbar mToolbar;
    private FragmentDrawer drawerFragment;
    SessionManager manager;
    DataWrite dataWrite;
    String status;
    ArrayList<BeanCart> cartlist;
    static RelativeLayout rl_counterCart, rl_counterWish;
    static ImageView iv_search, iv_cart, iv_wish;
    String page_view;
    String color_statusbar, color_tool_bg, color_tool_title, color_tool_icon;
    Dialog search_dialog;
    EditText et_search;
    AdapterSearch adapterSearch;
    RequestQueue queue;
    Activity activity;
    ArrayList<BeanSearchProduct> beanSearchProducts = new ArrayList<>();
    RecyclerView rv_product_list;
    RecyclerView.LayoutManager layoutManager;
    private AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        progressDialog = new SpotsDialog(activity, R.style.Custom);
        progressDialog.setCancelable(false);
        queue = Volley.newRequestQueue(this);
        manager = new SessionManager();
        status = manager.getPreferences(MainActivity.this, AppConstant.STATUS);

        SettingOption settingOption = new SettingOption();
        settingOption = manager.getPreferencesOption(MainActivity.this, AppConstant.SETTING_OPTION);
        color_statusbar = settingOption.getData().getOptions().getStatus_bar_color();
        color_tool_bg = settingOption.getData().getOptions().getToolbar_back_color();
        color_tool_title = settingOption.getData().getOptions().getToolbar_title_color();
        color_tool_icon = settingOption.getData().getOptions().getToolbar_icon_color();

        page_view = settingOption.getData().getOptions().getPage_view();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(color_statusbar));
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar1);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(color_tool_bg)));
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        tv_Title = (TextView) mToolbar.findViewById(R.id.tvTitle1);
        tv_counter = (TextView) mToolbar.findViewById(R.id.tvCounter);
        tv_counter_wish = (TextView) mToolbar.findViewById(R.id.tvCounter_wish);

        rl_counterCart = (RelativeLayout) mToolbar.findViewById(R.id.rlCounter_toolbar);
        rl_counterWish = (RelativeLayout) mToolbar.findViewById(R.id.rlwish_toolbar);
        iv_search = (ImageView) mToolbar.findViewById(R.id.iv_right_search);
        iv_cart = (ImageView) mToolbar.findViewById(R.id.iv_cart_toolbar);
        iv_wish = (ImageView) mToolbar.findViewById(R.id.iv_wish_toolbar);

        tv_Title.setTextColor(Color.parseColor(color_tool_title));

        Drawable myIcon = getResources().getDrawable(R.drawable.ic_search);
        myIcon.setColorFilter(Color.parseColor(color_tool_icon), PorterDuff.Mode.SRC_ATOP);

        Drawable myIcon1 = getResources().getDrawable(R.drawable.ic_cart_white);
        myIcon1.setColorFilter(Color.parseColor(color_tool_icon), PorterDuff.Mode.SRC_ATOP);

        Drawable myIcon2 = getResources().getDrawable(R.drawable.ic_heart_white);
        myIcon2.setColorFilter(Color.parseColor(color_tool_icon), PorterDuff.Mode.SRC_ATOP);


        dataWrite = new DataWrite(this);
        dataWrite.open();

        drawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);
        tv_logout = (TextView) findViewById(R.id.tvLogout);


        tv_logout.setOnClickListener(onClickMethod);
        rl_counterCart.setOnClickListener(onClickMethod);
        //rl_counterWish.setOnClickListener(onClickMethod);
        iv_search.setOnClickListener(onClickMethod);

        // display the first navigation drawer view on app launch
        if (page_view.equals("shop")) {
            displayView(1);
        } else {
            displayView(0);
        }
        getSearchData();
    }

    protected OnBackPressedListener onBackPressedListener;

    public interface OnBackPressedListener {
        void doBack();
    }

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }


    private View.OnClickListener onClickMethod = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                case R.id.tvLogout:

                    if (tv_logout.getText().toString().equals("Login")) {

                        Intent loginInt = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(loginInt);
                        finish();

                    } else {

                        manager.setPreferences(MainActivity.this, "status", "0");
                        dataWrite.deletedData();

                        Intent in = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(in);
                        finish();
                    }

                    break;
                case R.id.rlCounter_toolbar:
   /*                 Fragment fragment = new ShoppingCartFragment();
                    String title = getString(R.string.title_shoppingcart);
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    Fragment current = fragmentManager.findFragmentByTag(title);
                    if (current!=null){
                        fragmentTransaction.remove(current);
                        fragmentTransaction.addToBackStack(title);
                    }else {
                        fragmentTransaction.addToBackStack(title);
                    }

                    fragmentTransaction.replace(R.id.container_body, fragment);
                    fragmentTransaction.commit();
                    // set the toolbar title
                    Title(title);*/
                    startActivity(new Intent(MainActivity.this, CartActivity.class));
                    iv_search.setVisibility(View.VISIBLE);
                    CartVisibility(View.VISIBLE);
                    WishVisibility(View.VISIBLE);

                    break;

                case R.id.iv_right_search:

                    SearchDialogOpen();
   /*                 Fragment fragment1 = new SearchFragment();
                    String title1 = getString(R.string.title_search);
                    FragmentManager fragmentManager1 = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction1 = fragmentManager1.beginTransaction();
                    fragmentTransaction1.replace(R.id.container_body, fragment1);
                    fragmentTransaction1.commit();
                    // set the toolbar title
                    Title(title1);
                    iv_search.setVisibility(View.INVISIBLE);
                    CartVisibility(View.VISIBLE);
                    WishVisibility(View.VISIBLE);*/
                    break;
                case R.id.rlwish_toolbar:
                    Fragment fragment2 = new WishlistFragment();
                    String title2 = getString(R.string.title_wishlist);
                    FragmentManager fragmentManager2 = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                    Fragment current2 = fragmentManager2.findFragmentByTag(title2);
                    if (current2 != null) {
                        fragmentTransaction2.remove(current2);
                        fragmentTransaction2.addToBackStack(title2);
                    } else {
                        fragmentTransaction2.addToBackStack(title2);
                    }

                    fragmentTransaction2.replace(R.id.container_body, fragment2);
                    fragmentTransaction2.commit();
                    // set the toolbar title
                    Title(title2);
                    iv_search.setVisibility(View.VISIBLE);
                    CartVisibility(View.VISIBLE);
                    break;

            }

        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);
    }

    private void displayView(int position) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);

        switch (position) {
          /*  case 0:
                fragment = new HomeFragment();
                title = getString(R.string.title_home);
                break;*/

            case 0:
                Bundle bundle=new Bundle();
                bundle.putParcelableArrayList("beanSearchProducts",beanSearchProducts);
                fragment = new CategoryFragment();
                fragment.setArguments(bundle);
                title = getString(R.string.title_category);

                manager.setPreferences(ApplicationContext.getAppContext(), AppConstant.LAST_FRAG, 0);
                AppConstant.page_view = "category";
                break;

            case 1:

              //  fragment = new ProductFragment();
                Intent intent = new Intent(activity, ProductActivity.class);
                 intent.putExtra("beanSearchProducts", beanSearchProducts);
                startActivity(intent);
                title = getString(R.string.title_shop);
                AppConstant.page_view = "shop";
                AppConstant._Activty = "shop";

                break;

            case 2:

             /*   FragmentManager fm = getSupportFragmentManager();
                if (fm!=null){
                    FragmentManager.BackStackEntry first = fm.getBackStackEntryAt(0);
                    fm.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }*/
           /*     fragment = new ShoppingCartFragment();
                title = getString(R.string.title_shoppingcart);*/
                startActivity(new Intent(MainActivity.this, CartActivity.class));
                break;

            /*case 3:
                fragment = new WishlistFragment();
                title = getString(R.string.title_wishlist);
                break;*/
            case 3:

                Bundle bundle2 = new Bundle(); //change
                bundle2.putParcelableArrayList("beanSearchProducts", beanSearchProducts); //change

                fragment = new ContactUsFragment();
                fragment.setArguments(bundle2);
                title = getString(R.string.title_contactus);
                manager.setPreferences(ApplicationContext.getAppContext(), AppConstant.LAST_FRAG, 3);

                break;
            case 4:
                Bundle bundle3 = new Bundle(); //change
                bundle3.putParcelableArrayList("beanSearchProducts", beanSearchProducts); //change

                fragment = new TermAndConditionFragment();
                fragment.setArguments(bundle3);
                title = getString(R.string.title_terms_condition);
                manager.setPreferences(ApplicationContext.getAppContext(), AppConstant.LAST_FRAG, 4);

                //Bundle args = new Bundle();
                //args.putString("url", "file:///android_asset/termscondition.html");
                //fragment.setArguments(args);
                break;

            case 5:

                status = manager.getPreferences(MainActivity.this, AppConstant.STATUS);
                if (status.equals("1")) {
                    Bundle bundle4 = new Bundle();
                    bundle4.putParcelableArrayList("beanSearchProducts", beanSearchProducts);
                    fragment = new MyAccountFragment();
                    fragment.setArguments(bundle4);
                    manager.setPreferences(ApplicationContext.getAppContext(), AppConstant.LAST_FRAG, 5);

                    title = getString(R.string.title_account);
                } else {
                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    i.putExtra("LOGIN", "login");
                    startActivity(i);
                    finish();
                }

                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            Fragment current = fragmentManager.findFragmentByTag(title);
            if (current != null) {
                //Log.i("TEST", "Found fragment: " + current);
                fragmentTransaction.remove(current);
                fragmentTransaction.addToBackStack(title);
            } else {
                fragmentTransaction.addToBackStack(title);
            }

            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();
            // set the toolbar title
            Title(title);

            imageviewVisibility(View.VISIBLE);
            CartVisibility(View.VISIBLE);
            WishVisibility(View.VISIBLE);
        }
    }

    public static void imageviewVisibility(int visible) {
        iv_search.setVisibility(visible);
    }

    public static void CartVisibility(int visible) {
        rl_counterCart.setVisibility(visible);
    }

    public static void WishVisibility(int visible) {
        //rl_counterWish.setVisibility(visible);
    }

    public static void Title(String title) {
        tv_Title.setText(title);
    }

    public static void setCounter(String counter) {
        if (counter.equals("0")) {
            tv_counter.setVisibility(View.GONE);
        } else {
            tv_counter.setVisibility(View.VISIBLE);
            tv_counter.setText(counter);
        }

    }

    public static void setWishCounter(String counter) {

        /*if (counter.equals("0")){
         tv_counter_wish.setVisibility(View.GONE);
        }else {
            tv_counter_wish.setVisibility(View.VISIBLE);
            tv_counter_wish.setText(counter);
        }*/
    }

    @Override
    public void onBackPressed() {
        if (onBackPressedListener != null) {
            onBackPressedListener.doBack();
        } else {
        }

    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    private void getSearchData() {
        progressDialog.show();
        Map<String, String> postParam = new HashMap<String, String>();
        //postParam.put("email", "test@gmail.com");
        // postParam.put("p", "somepasswordhere");
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, Webservice.BASE_URL+Webservice.get_search_products
                , new JSONObject(postParam),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("res2", "" + response.toString());
                        try {
                            JSONObject object1 = new JSONObject(response.toString());
                            JSONObject object = object1.getJSONObject("data");
                            if (object.has("products")) {
                                JSONArray jsonArray = object.getJSONArray("products");
                                if (jsonArray.length() != 0) {
                                    for (int j = 0; j < jsonArray.length(); j++) {
                                        beanSearchProducts.clear();
                                        beanSearchProducts.addAll((Collection<? extends BeanSearchProduct>) new Gson().fromJson(jsonArray.toString(), new TypeToken<ArrayList<BeanSearchProduct>>() {
                                        }.getType()));

                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        progressDialog.dismiss();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error2", "" + error.toString());
                progressDialog.dismiss();
            }
        }) {
            /**
             * Passing some request headers
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        jsonObjReq.setTag(MainActivity.this);
        queue.add(jsonObjReq);

    }

    private void SearchDialogOpen() {
        search_dialog = new Dialog(activity);
        search_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        search_dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
        search_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        search_dialog.setContentView(R.layout.search_layout);

        Window window = search_dialog.getWindow();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.gravity = Gravity.CENTER;
        window.setAttributes(lp);

        LinearLayout ll_back = (LinearLayout) search_dialog.findViewById(R.id.ll_back);
        et_search = (EditText) search_dialog.findViewById(R.id.et_search);
        rv_product_list = (RecyclerView) search_dialog.findViewById(R.id.rv_product_list);
        LinearLayoutManager gridLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv_product_list.setLayoutManager(gridLayoutManager);
        adapterSearch = new AdapterSearch(beanSearchProducts,activity);
        rv_product_list.setAdapter(adapterSearch);

        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapterSearch.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        ll_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_dialog.dismiss();
            }
        });

        search_dialog.show();

    }

}