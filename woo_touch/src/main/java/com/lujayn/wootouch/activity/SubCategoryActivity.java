package com.lujayn.wootouch.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.lujayn.wootouch.R;
import com.lujayn.wootouch.adapter.AdapterSearch;
import com.lujayn.wootouch.adapter.GridSpacingItemDecoration;
import com.lujayn.wootouch.bean.BeanSearchProduct;
import com.lujayn.wootouch.bean.BeanSubCategory;
import com.lujayn.wootouch.bean.SettingOption;
import com.lujayn.wootouch.common.AppConstant;
import com.lujayn.wootouch.common.ApplicationData;
import com.lujayn.wootouch.common.SessionManager;
import com.lujayn.wootouch.common.Webservice;
import com.lujayn.wootouch.custom.AppDebugLog;
import com.lujayn.wootouch.database.DataWrite;
import com.lujayn.wootouch.network.RequestTask;
import com.lujayn.wootouch.network.RequestTaskDelegate;

import java.util.ArrayList;

import dmax.dialog.SpotsDialog;

public class SubCategoryActivity extends AppCompatActivity implements RequestTaskDelegate, View.OnClickListener {

    GridView gridView;
    private Boolean exit = false;
    ArrayList<BeanSubCategory> subcategorylist;
    private RecyclerView recyclerView;
    private AlertDialog progressDialog;
    String color_cate_shape, color_cate_name;
    SessionManager manager;
    DataWrite dataWrite;
    int cate_viewtype = 0;
    int screen_width, screen_height;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView.LayoutManager mLayoutManager;
    private ApplicationData appData;
    Activity activity;
    LinearLayout ll_back;
    TextView tvCounter;
    String color_back;
    RelativeLayout rlCounter_toolbar;
    ArrayList<BeanSearchProduct> beanSearchProducts = new ArrayList<>();
    Dialog search_dialog;
    AdapterSearch adapterSearch;
    ImageView iv_right_search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_category);
        activity = this;
        beanSearchProducts = getIntent().getParcelableArrayListExtra("beanSearchProducts");
        progressDialog = new SpotsDialog(activity, R.style.Custom);
        manager = new SessionManager();
        appData = ApplicationData.getSharedInstance();
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        (activity).getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);
        screen_height = displayMetrics.heightPixels;
        screen_width = displayMetrics.widthPixels;

        SettingOption settingOption = new SettingOption();
        settingOption = manager.getPreferencesOption(activity, AppConstant.SETTING_OPTION);
        color_cate_shape = settingOption.getData().getOptions().getCate_shape_color();
        color_cate_name = settingOption.getData().getOptions().getCate_name_color();
        color_back = settingOption.getData().getOptions().getToolbar_back_color();
        cate_viewtype = Integer.parseInt(settingOption.getData().getOptions().getCategory_view());
        dataWrite = new DataWrite(activity);
        dataWrite.open();
        gridView = (GridView) findViewById(R.id.gvCategory);


        recyclerView = (RecyclerView) findViewById(R.id.recyclerCategory);
        TextView tv_title = (TextView) findViewById(R.id.tv_title);
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        tvCounter = (TextView) findViewById(R.id.tvCounter);
        RelativeLayout rl_header = (RelativeLayout) findViewById(R.id.rl_header);
        rlCounter_toolbar = (RelativeLayout) findViewById(R.id.rlCounter_toolbar);
        iv_right_search = (ImageView) findViewById(R.id.iv_right_search);
        rl_header.setBackgroundColor(Color.parseColor(color_back));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(color_back));
        }

        if (cate_viewtype == 0) {
            gridView.setVisibility(View.GONE);

            mLayoutManager = new GridLayoutManager(activity, 2);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(2), true));
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            recyclerView.setVisibility(View.VISIBLE);

        } else {
            gridView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(activity);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.addItemDecoration(new DividerItemDecoration(activity, LinearLayoutManager.VERTICAL) {
                @Override
                public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                    // Do not draw the divider
                }
            });
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        }


        //catid = getArguments().getString("catID");
        //catName = getArguments().getString("catname");
        AppConstant.sub_catName = AppConstant.catName;
        AppConstant.sub_catID = AppConstant.catID;
        tv_title.setText(AppConstant.sub_catName);

        displaysubcategory();
        // Inflate the layout for this fragment
        mSwipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
        mSwipeRefreshLayout.setSoundEffectsEnabled(true);
        // mSwipeRefreshLayout.setDistanceToTriggerSync(200);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                //  mSwipeRefreshLayout.setRefreshing(false);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dataWrite.deletedSubCategory();
                        refreshSubCategory("subcategory");

                    }
                }, 2500);
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


                if (subcategorylist.get(i).getChild().equals("1")) {

                    // AppConstant.catslug = subcategorylist.get(i).getSlug();
                    AppConstant.catID = subcategorylist.get(i).getSubcategory_id();
                    //AppConstant.catName = subcategorylist.get(i).getSubcategory_name();
                    //  AppConstant._Activty = "subcategory";
                    //Log.d("catslug","AppConstant.catID==== " +AppConstant.catID);
                    displaysubcategory(AppConstant.catID);
                    gridView.deferNotifyDataSetChanged();

                } else {

                    AppConstant.catslug = subcategorylist.get(i).getSlug();
                    AppConstant.catID = subcategorylist.get(i).getSubcategory_id();
                    AppConstant.catName = subcategorylist.get(i).getSubcategory_name();
                    AppConstant._Activty = "subcategory";
                    AppConstant.page_view = "category";
                    //AppConstant.sub_catID = AppConstant.catID;
                    //AppConstant.sub_catName = AppConstant.catName;

                    String title = getString(R.string.app_name);
                    Intent intent = new Intent(activity, ProductActivity.class);
                     intent.putExtra("beanSearchProducts", beanSearchProducts);
                    startActivity(intent);
                  /*  Fragment fr=new ProductFragment();
                    title = subcategorylist.get(i).getSubcategory_name();
                    FragmentManager fm=getFragmentManager();
                    FragmentTransaction ft=fm.beginTransaction();
                    Fragment current = fm.findFragmentByTag(title);
                    if (current!=null){
                        ft.remove(current);

                    }
                    ft.addToBackStack(title);
                    //Bundle args = new Bundle();
                    //args.putString("catslug", subcategorylist.get(i).getSlug());
                    //args.putString("catID", subcategorylist.get(i).getSubcategory_id());
                    //args.putString("catName", subcategorylist.get(i).getSubcategory_name());
                    //args.putString("parentID",catid);
                    //args.putString("parentName",catName);
                    //args.putString("activity","subcategory");
                    //fr.setArguments(args);
                    ft.replace(R.id.container_body, fr);
                    ft.commit();
                    ((MainActivity) getActivity()).Title(title);*/
                }

            }
        });
        ll_back.setOnClickListener(this);
        rlCounter_toolbar.setOnClickListener(this);
        iv_right_search.setOnClickListener(this);
    }


    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    private void refreshSubCategory(String subcategory) {

        progressDialog.show();
/*

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("catid",catid);

        } catch (JSONException e) {
            e.printStackTrace();
        }
*/

        String requestURL = Webservice.BASE_URL + "" + Webservice.URL_ProductSubCategory;
        AppDebugLog.println("requestURL in sendNewDataRequest : " + requestURL);
        RequestTask requestTask = new RequestTask(requestURL, AppConstant.HttpRequestType.DataUpdateRequest);
        requestTask.delegate = this;
        requestTask.execute(requestURL, "subcategory");

    }

    private void displaysubcategory() {
        mSwipeRefreshLayout.setRefreshing(false);
        subcategorylist = dataWrite.fetchSubCategorys(AppConstant.catID);

        if (subcategorylist.size() == 0) {

            refreshSubCategory("subcategory");

        } else {
            progressDialog.dismiss();
            CategoryRecyAdapter productCategoryAdapter = new CategoryRecyAdapter(subcategorylist);
            recyclerView.setAdapter(productCategoryAdapter);


          /*  if (cate_viewtype==0){
                ProductSubCategoryAdapter productsubCategoryAdapter = new ProductSubCategoryAdapter(subcategorylist);
                productsubCategoryAdapter.notifyDataSetChanged();
                gridView.setAdapter(productsubCategoryAdapter);
                gridView.deferNotifyDataSetChanged();

            }else {
                CategoryRecyAdapter productCategoryAdapter = new CategoryRecyAdapter(subcategorylist);
                recyclerView.setAdapter(productCategoryAdapter);
            }*/

        }

    }

    private void displaysubcategory(String catid) {
        mSwipeRefreshLayout.setRefreshing(false);
        subcategorylist = dataWrite.fetchSubCategorys(catid);

        if (subcategorylist.size() == 0) {

            // refreshSubCategory("subcategory");
            dataWrite.showToast(getString(R.string.subcategory_not_found));
            progressDialog.dismiss();
            CategoryRecyAdapter productCategoryAdapter = new CategoryRecyAdapter(subcategorylist);
            recyclerView.setAdapter(productCategoryAdapter);

           /* ProductSubCategoryAdapter productsubCategoryAdapter = new ProductSubCategoryAdapter(subcategorylist);
            productsubCategoryAdapter.notifyDataSetChanged();
            gridView.setAdapter(productsubCategoryAdapter);
            gridView.deferNotifyDataSetChanged();*/
        } else {

            progressDialog.dismiss();
            CategoryRecyAdapter productCategoryAdapter = new CategoryRecyAdapter(subcategorylist);
            recyclerView.setAdapter(productCategoryAdapter);

           /* ProductSubCategoryAdapter productsubCategoryAdapter = new ProductSubCategoryAdapter(subcategorylist);
            productsubCategoryAdapter.notifyDataSetChanged();
            gridView.setAdapter(productsubCategoryAdapter);
            gridView.deferNotifyDataSetChanged();*/
        }

    }


/*    BroadcastReceiver receive=new BroadcastReceiver() {
        JSONObject object;
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            Bundle bundle = intent.getExtras();
            String data=bundle.getString(Intent_Class_Data.FILENAME);
            subcategorylist = new ArrayList<BeanSubCategory>();
            if(bundle!=null){

                if (bundle.getString("type").equalsIgnoreCase("subcategory")) {
                   // Log.d("TEST", "Sub CATEGORY is " + data);

                    try {
                        object = new JSONObject(data);

                        if (object.getInt("success")==1){
                            JSONObject jsonObject = object.optJSONObject("data");
                            JSONArray jsonArray = jsonObject.optJSONArray("sub_categories");

                            for (int i =0;i<jsonArray.length();i++){
                                JSONObject object1 = jsonArray.optJSONObject(i);

                                BeanSubCategory bean = new BeanSubCategory();
                                bean.setSubcategory_id(object1.getString("subcategory_id"));
                                bean.setSubcategory_name(object1.getString("subcategory_name"));
                                bean.setDescription(object1.getString("description"));
                                bean.setImage(object1.getString("image"));
                                bean.setCount(object1.getString("count"));
                                bean.setSlug(object1.getString("slug"));
                                bean.setParent(object1.getString("parent"));

                                subcategorylist.add(bean);

                            }
                            Log.d("TEST", "Sub CATEGORY is " + subcategorylist.size());
                            ProductSubCategoryAdapter productsubCategoryAdapter = new ProductSubCategoryAdapter(subcategorylist);
                            gridView.setAdapter(productsubCategoryAdapter);
                            progressDialog.dismiss();
                        }else{

                            dataWrite.showToast("Unknown error");

                        }




                    } catch (Exception e) {
                        // TODO: handle exception
                        Log.d("TEST", "error =" + e);
                    }


                }
            }



        }


    };*/


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
        // getActivity().unregisterReceiver(receive);

    }


    @Override
    public void backgroundActivityComp(String response, AppConstant.HttpRequestType completedRequestType) {
        AppConstant.HTTPResponseCode responseCode = appData.getResponseCode();
        AppDebugLog.println("responseCode : " + responseCode);

        switch (responseCode) {

            case Subcategory:

                //Log.d("TEST", "Sub CATEGORY is ");
                displaysubcategory();
                break;

            case NetworkError:
                appData.showUserAlert(activity, getString(R.string.alert_title_message),
                        getString(R.string.alert_body_network_error), null);
                break;

            case ServerError:
                progressDialog.dismiss();
                appData.showUserAlert(activity, getString(R.string.alert_title_message),
                        getString(R.string.alert_body_server_error), null);
                break;
        }
    }

    @Override
    public void timeOut() {

    }

    @Override
    public void codeError(int code) {

    }

    @Override
    public void percentageDownloadCompleted(int percentage, Object record) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                finish();
                break;

            case R.id.rlCounter_toolbar:
                startActivity(new Intent(activity, CartActivity.class));
                break;

            case R.id.iv_right_search:
                SearchDialogOpen();
                break;

        }
    }


    public class CategoryRecyAdapter extends RecyclerView.Adapter<CategoryRecyAdapter.MainViewHolder> {
        Context context;
        ArrayList<BeanSubCategory> bean_list;
        private static final int TYPE_0 = 0;
        private static final int TYPE_1 = 1;
        private static final int TYPE_2 = 2;
        private static final int TYPE_3 = 3;
        private static final int TYPE_4 = 4;
        private static final int TYPE_5 = 5;


        @Override
        public CategoryRecyAdapter.MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            switch (viewType) {

                case TYPE_0:

                    View leftview1 = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.inflater_category, parent, false);


                    return new CategoryRecyAdapter.MainViewHolder(leftview1, viewType);
                case TYPE_1:


                    View leftview = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.inflater_category_list, parent, false);


                    return new CategoryRecyAdapter.MainViewHolder(leftview, viewType);

                case TYPE_2:
                    View leftview2 = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.inflater_category_list2, parent, false);

                    //ViewHolder2 viewHolder2 = new ViewHolder2(leftview2);

                    return new CategoryRecyAdapter.MainViewHolder(leftview2, viewType);


                case TYPE_3:
                    View leftview3 = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.inflater_category_list3, parent, false);

                    //ViewHolder2 viewHolder2 = new ViewHolder2(leftview2);

                    return new CategoryRecyAdapter.MainViewHolder(leftview3, viewType);

                case TYPE_4:
                    View leftview4 = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.inflater_category_list5, parent, false);

                    //ViewHolder1 viewHolder3 = new ViewHolder1(leftview3);

                    return new CategoryRecyAdapter.MainViewHolder(leftview4, viewType);

                case TYPE_5:
                    View leftview5 = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.inflater_category_list6, parent, false);

                    //ViewHolder1 viewHolder4 = new ViewHolder1(leftview4);

                    return new CategoryRecyAdapter.MainViewHolder(leftview5, viewType);

            }

            return null;
        }


        @Override
        public int getItemViewType(int position) {

            switch (cate_viewtype) {
                case 0:
                    return TYPE_0;
                case 1:
                    return TYPE_1;
                case 2:
                    return TYPE_2;
                case 3:
                    return TYPE_3;
                case 4:
                    return TYPE_4;
                case 5:
                    return TYPE_5;

                default:
                    return -1;
            }

        }

        @Override
        public void onBindViewHolder(final CategoryRecyAdapter.MainViewHolder vh, final int i) {

            vh.categoryName.setText(bean_list.get(i).getSubcategory_name());
            Glide.with(activity).load(bean_list.get(i).getImage())
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            vh.progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            vh.progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(vh.categoryImage);


            vh.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (subcategorylist.get(i).getChild().equals("1")) {

                        // AppConstant.catslug = subcategorylist.get(i).getSlug();
                        AppConstant.catID = bean_list.get(i).getSubcategory_id();
                        //AppConstant.catName = subcategorylist.get(i).getSubcategory_name();
                        //  AppConstant._Activty = "subcategory";
                        //Log.d("catslug","AppConstant.catID==== " +AppConstant.catID);
                        displaysubcategory(AppConstant.catID);


                    } else {

                        AppConstant.catslug = bean_list.get(i).getSlug();
                        AppConstant.catID = bean_list.get(i).getSubcategory_id();
                        AppConstant.catName = bean_list.get(i).getSubcategory_name();
                        AppConstant._Activty = "subcategory";
                        AppConstant.page_view = "category";
                        //AppConstant.sub_catID = AppConstant.catID;
                        //AppConstant.sub_catName = AppConstant.catName;

                        String title = getString(R.string.app_name);
                        Intent intent = new Intent(activity, ProductActivity.class);
                         intent.putExtra("beanSearchProducts", beanSearchProducts);
                        startActivity(intent);
                        /*  Fragment fr=new ProductFragment();
                        title = bean_list.get(i).getSubcategory_name();
                        FragmentManager fm=getFragmentManager();
                        FragmentTransaction ft=fm.beginTransaction();
                        Fragment current = fm.findFragmentByTag(title);
                        if (current!=null){
                            ft.remove(current);

                        }
                        ft.addToBackStack(title);
                        //Bundle args = new Bundle();
                        //args.putString("catslug", subcategorylist.get(i).getSlug());
                        //args.putString("catID", subcategorylist.get(i).getSubcategory_id());
                        //args.putString("catName", subcategorylist.get(i).getSubcategory_name());
                        //args.putString("parentID",catid);
                        //args.putString("parentName",catName);
                        //args.putString("activity","subcategory");
                        //fr.setArguments(args);
                        ft.replace(R.id.container_body, fr);
                        ft.commit();

                        ((MainActivity) getActivity()).Title(title);*/
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return bean_list.size();
        }

        public CategoryRecyAdapter(ArrayList<BeanSubCategory> beanProductCategories_list) {

            this.bean_list = beanProductCategories_list;

        }


        public class MainViewHolder extends RecyclerView.ViewHolder {
            TextView categoryName;
            ImageView categoryImage;
            CardView cardView;
            ProgressBar progressBar;
            View vcate3;
            LinearLayout llCtName;
            ImageView catback;

            public MainViewHolder(View itemView, int viewType) {
                super(itemView);

                switch (viewType) {
                    case TYPE_0:
                        categoryName = (TextView) itemView.findViewById(R.id.tvCategoryname);
                        categoryImage = (ImageView) itemView.findViewById(R.id.ivCategoryImage);
                        llCtName = (LinearLayout) itemView.findViewById(R.id.llCtName);
                        progressBar = (ProgressBar) itemView.findViewById(R.id.pbarCategory);
                        cardView = (CardView) itemView.findViewById(R.id.cardCategory2);
                        categoryName.setTextColor(Color.parseColor(color_cate_name));
                        llCtName.setBackgroundColor(Color.parseColor(color_cate_shape));


                        break;

                    case TYPE_1:
                        categoryName = (TextView) itemView.findViewById(R.id.tvCategoryname);
                        categoryImage = (ImageView) itemView.findViewById(R.id.ivCategoryImage);
                        progressBar = (ProgressBar) itemView.findViewById(R.id.pbarCategory);
                        cardView = (CardView) itemView.findViewById(R.id.cardCategory2);
                        catback = (ImageView) itemView.findViewById(R.id.rlCatlist);


                        categoryName.setTextColor(Color.parseColor(color_cate_name));
                        Drawable myIcon = getResources().getDrawable(R.drawable.shape_left_corner);
                        //ColorFilter filter = new LightingColorFilter( Color.TRANSPARENT, Color.TRANSPARENT );
                        myIcon.setColorFilter(Color.parseColor(color_cate_shape), PorterDuff.Mode.SRC_ATOP);
                        //catback.setImageDrawable(myIcon);
                        break;
                    case TYPE_2:
                        categoryName = (TextView) itemView.findViewById(R.id.tvCategoryname);
                        categoryImage = (ImageView) itemView.findViewById(R.id.ivCategoryImage);
                        progressBar = (ProgressBar) itemView.findViewById(R.id.pbarCategory);
                        cardView = (CardView) itemView.findViewById(R.id.cardCategory2);


                        categoryName.setTextColor(Color.parseColor(color_cate_name));
                        ShapeDrawable badge = new ShapeDrawable(new RectShape());
                        badge.getPaint().setColor(Color.parseColor("#99".concat(color_cate_shape.replace("#", ""))));
                        //badge.getPaint().setColor(Color.parseColor(color_back));
                        categoryName.setBackground(badge);

                        break;
                    case TYPE_3:
                        categoryName = (TextView) itemView.findViewById(R.id.tvCategoryname);
                        categoryImage = (ImageView) itemView.findViewById(R.id.ivCategoryImage);
                        progressBar = (ProgressBar) itemView.findViewById(R.id.pbarCategory);
                        cardView = (CardView) itemView.findViewById(R.id.cardCategory2);
                        vcate3 = (View) itemView.findViewById(R.id.viewCategory3);

                        categoryName.setTextColor(Color.parseColor(color_cate_name));
                        ShapeDrawable badge3 = new ShapeDrawable(new RectShape());
                        badge3.getPaint().setColor(Color.parseColor("#99".concat(color_cate_shape.replace("#", ""))));
                        //badge3.getPaint().setColor(Color.parseColor(color_back));
                        vcate3.setBackground(badge3);
                        break;
                    case TYPE_4:
                        categoryName = (TextView) itemView.findViewById(R.id.tvCategoryname);
                        categoryImage = (ImageView) itemView.findViewById(R.id.ivCategoryImage);
                        progressBar = (ProgressBar) itemView.findViewById(R.id.pbarCategory);
                        cardView = (CardView) itemView.findViewById(R.id.cardCategory2);
                        catback = (ImageView) itemView.findViewById(R.id.rlCatlist);


                        categoryName.setTextColor(Color.parseColor(color_cate_name));
                        Drawable myIcon5 = getResources().getDrawable(R.drawable.shape_middle_angle);
                        //ColorFilter filter5 = new LightingColorFilter( Color.TRANSPARENT, Color.TRANSPARENT);
                        myIcon5.setColorFilter(Color.parseColor(color_cate_shape), PorterDuff.Mode.SRC_ATOP);

                        break;

                    case TYPE_5:
                        categoryName = (TextView) itemView.findViewById(R.id.tvCategoryname);
                        categoryImage = (ImageView) itemView.findViewById(R.id.ivCategoryImage);
                        progressBar = (ProgressBar) itemView.findViewById(R.id.pbarCategory);
                        cardView = (CardView) itemView.findViewById(R.id.cardCategory2);
                        catback = (ImageView) itemView.findViewById(R.id.rlCatlist);

                        categoryName.setTextColor(Color.parseColor(color_cate_name));
                        Drawable myIcon6 = getResources().getDrawable(R.drawable.shape_right_corner);
                        //ColorFilter filter6 = new LightingColorFilter(Color.parseColor(color_back), Color.TRANSPARENT);
                        myIcon6.setColorFilter(Color.parseColor(color_cate_shape), PorterDuff.Mode.SRC_ATOP);
                        //catback.setBackgroundDrawable(myIcon6);
                        break;

                    default:
                        break;

                }
            }

        }



    /*    public class MyViewHolder extends RecyclerView.ViewHolder{

            TextView categoryName;
            ImageView categoryImage;
            CardView cardView;
            ProgressBar progressBar;
            View vcate3;

            public MyViewHolder(View itemView) {
                super(itemView);

                *//*--------- inflater_category2 --------*//*
                categoryName = (TextView)itemView.findViewById(R.id.tvCategoryname);
                categoryImage =(ImageView)itemView.findViewById(R.id.ivCategoryImage);
                progressBar = (ProgressBar)itemView.findViewById(R.id.pbarCategory);
                cardView = (CardView)itemView.findViewById(R.id.cardCategory2) ;

              *//*  ShapeDrawable badge = new ShapeDrawable (new RectShape());
                badge.getPaint().setColor(Color.parseColor("#99".concat(color_back.replace("#",""))));
                //badge.getPaint().setColor(Color.parseColor(color_back));
                categoryName.setBackground(badge);

*//*


                 *//* ------------inflater_category3---------------*//*
                vcate3 = (View)itemView.findViewById(R.id.viewCategory3);

                ShapeDrawable badge = new ShapeDrawable (new RectShape());
                badge.getPaint().setColor(Color.parseColor("#99".concat(color_back.replace("#",""))));
                //badge.getPaint().setColor(Color.parseColor(color_back));
                vcate3.setBackground(badge);


                //vh.categoryName.setTextColor(Color.parseColor(color_font));




            }
        }*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        setCounter(AppConstant.counter);
    }

    public void setCounter(String counter) {
        if (counter.equals("0")) {
            tvCounter.setVisibility(View.GONE);
        } else {
            tvCounter.setVisibility(View.VISIBLE);
            tvCounter.setText(counter);
        }

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
        EditText et_search = (EditText) search_dialog.findViewById(R.id.et_search);
        RecyclerView rv_product_list = (RecyclerView) search_dialog.findViewById(R.id.rv_product_list);
        LinearLayoutManager gridLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv_product_list.setLayoutManager(gridLayoutManager);
        adapterSearch = new AdapterSearch(beanSearchProducts, activity);
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
