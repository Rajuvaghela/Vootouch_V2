package com.lujayn.wootouch.activity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lujayn.wootouch.R;
import com.lujayn.wootouch.bean.BeanCart;
import com.lujayn.wootouch.bean.Options;
import com.lujayn.wootouch.bean.SettingOption;
import com.lujayn.wootouch.common.AppConstant;
import com.lujayn.wootouch.common.ApplicationContext;
import com.lujayn.wootouch.common.ApplicationData;
import com.lujayn.wootouch.common.ImageLoader;
import com.lujayn.wootouch.common.SessionManager;
import com.lujayn.wootouch.custom.AppDebugLog;
import com.lujayn.wootouch.database.DataWrite;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class CartActivity extends AppCompatActivity implements View.OnClickListener {

    TextView tv_cartTotal, tv_noProductfound, btn_proccedToOrder;
    TextView btn_cntShopping;
    ArrayList<BeanCart> cartlist;
    DataWrite dataWrite;
    LinearLayout llCartlist, ll_noproduct;
    RelativeLayout rl_footer;
    ArrayList<TextView> textViews = new ArrayList<TextView>();
    ArrayList<TextView> textprice = new ArrayList<TextView>();
    LayoutInflater inflater;
    SessionManager manager;
    SettingOption settingOption;
    Options options;
    String status;
    boolean fragName = false;
    String color_back, color_font;
    ArrayList<Double> array_total;
    String product_id, product_name, variations_id, image, short_description, full_description, sale_price,
            regular_price, variation_price, variations, qnty, subTotal, product_type, ori_price, tax_status
            ,color_statusbar;
    Activity activity;
    LinearLayout ll_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        activity = this;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);

        inflater = (LayoutInflater) activity.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater = LayoutInflater.from(activity);

        manager = new SessionManager();
        status = manager.getPreferences(activity, AppConstant.STATUS);

        settingOption = new SettingOption();
        options = new Options();

        settingOption = manager.getPreferencesOption(activity, AppConstant.SETTING_OPTION);
        options = settingOption.getData().getOptions();
        color_back = settingOption.getData().getOptions().getCate_shape_color();
        color_font = settingOption.getData().getOptions().getCate_name_color();
        llCartlist = (LinearLayout) findViewById(R.id.llCartlist);
        ll_noproduct = (LinearLayout) findViewById(R.id.llNoproduct);
        rl_footer = (RelativeLayout) findViewById(R.id.rlTopCartFooter);
        RelativeLayout rl_header = (RelativeLayout) findViewById(R.id.rl_header);

        btn_cntShopping = (TextView) findViewById(R.id.btnCtnueShopping);
        btn_proccedToOrder = (TextView) findViewById(R.id.btnCheckout);

        btn_proccedToOrder.setBackgroundColor(Color.parseColor(color_back));
        btn_proccedToOrder.setTextColor(Color.parseColor(color_font));

        btn_cntShopping.setBackgroundColor(Color.parseColor(color_back));
        btn_cntShopping.setTextColor(Color.parseColor(color_font));

        tv_cartTotal = (TextView) findViewById(R.id.tvCartTotal);
        ll_back = (LinearLayout) findViewById(R.id.ll_back);

        color_statusbar = settingOption.getData().getOptions().getStatus_bar_color();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(color_statusbar));
        }

        rl_header.setBackgroundColor(Color.parseColor(color_back));


        dataWrite = new DataWrite(activity);
        dataWrite.open();

        fatchCart("cart");
        FechDataoftotal();

        btn_cntShopping.setOnClickListener(onClickMethod);

        btn_proccedToOrder.setOnClickListener(onClickMethod);
        ll_back.setOnClickListener(this);

    }

    private View.OnClickListener onClickMethod = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {

                case R.id.btnCtnueShopping:
                    startActivity(new Intent(activity, MainActivity.class));
                    finish();
                    break;

                case R.id.btnCheckout:

                    // Log.d("status", "array sum is = " + status);
                    if (status.equals("1")) {

                        Intent intent = new Intent(activity, CheckOutActivity.class);
                        intent.putExtra("USER", "user");
                        startActivity(intent);

                    } else {

                        Intent i = new Intent(activity, LoginActivity.class);
                        i.putExtra("SHOP", "shopping");
                        startActivity(i);


                    }


                    break;

            }
        }
    };

    public void FechDataoftotal() {
        array_total = dataWrite.fechtotal();

        double sum = 0;
        for (int i = 0; i < array_total.size(); i++) {
            sum = sum + array_total.get(i);

        }
        tv_cartTotal.setText(options.getCurrency_symbol() + " " + ApplicationData.getStringValue(sum));
    }

    @SuppressLint("NewApi")
    private void fatchCart(final String cart) {
        cartlist = dataWrite.fatchCartData();
        // Log.d("shailesh", "counter==== "+ cartlist.size());
        AppConstant.counter = "" + cartlist.size();
        MainActivity.setCounter(AppConstant.counter);
        TextView tv_product_Name, tv_price, tv_subtotal, tv_qnty, tv_variation;
        ImageView iv_image, iv_add, iv_delete, iv_remove;


        if (cartlist.size() == 0) {
            Toast.makeText(ApplicationContext.getAppContext(), R.string.cart_is_empty, Toast.LENGTH_SHORT).show();
            ll_noproduct.setVisibility(View.VISIBLE);
            rl_footer.setVisibility(View.GONE);
        } else {
            ll_noproduct.setVisibility(View.GONE);
            for (int i = 0; i < cartlist.size(); i++) {
                final View view = inflater.inflate(R.layout.inflater_shopingcart, null);
                tv_product_Name = (TextView) view.findViewById(R.id.tvNameCart);
                tv_price = (TextView) view.findViewById(R.id.tvPriceCart);
                tv_qnty = (TextView) view.findViewById(R.id.tvQnty);
                iv_image = (ImageView) view.findViewById(R.id.ivCartImage);
                iv_add = (ImageView) view.findViewById(R.id.ivAdd);
                iv_delete = (ImageView) view.findViewById(R.id.ivDelete);
                iv_remove = (ImageView) view.findViewById(R.id.ivRemove);
                tv_variation = (TextView) view.findViewById(R.id.tvVariation);

                textViews.add(tv_qnty);
                textprice.add(tv_price);

                final int index = i;
                tv_product_Name.setText(cartlist.get(i).getProduct_name());
                tv_qnty.setText(cartlist.get(i).getQnty());
                double p = Double.parseDouble(cartlist.get(i).getSubTotal());
                // Log.d("TAG", "getSubTotal()==== "+ ApplicationData.getFloatValue(p));
                tv_price.setText(options.getCurrency_symbol() + " " + ApplicationData.getStringValue(p));


                String s = cartlist.get(i).getVariation_id();
                if (s != null) {
                    tv_variation.setText(cartlist.get(i).getVariations());
                }



            /*    Picasso.with(activity)
                        .load(cartlist.get(i).getImage())
                        .into(iv_image);*/

                Glide.with(activity).load(cartlist.get(i).getImage())
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(iv_image);

                iv_remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        dataWrite.deletedFromCart(cartlist.get(index).getProduct_id(), cartlist.get(index).getVariation_id(),
                                cartlist.get(index).getVariations());
                        llCartlist.removeAllViews();
                        fatchCart("cart");
                        FechDataoftotal();
                    }
                });

                final TextView finalTv_qnty = tv_qnty;
                final TextView finalTv_price = tv_price;

                iv_add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int stock = 0;
                        int t = Integer.parseInt(finalTv_qnty.getText().toString());
                        int count = Integer.parseInt(String.valueOf(t + 1));
                        if ((cartlist.get(index).getMax_stock() == null) || cartlist.get(index).getMax_stock().equals("")) {
                            stock = 0;
                            finalTv_qnty.setText("" + count);
                            textViews.get(index).setText("" + count);

                            double p = Double.parseDouble(cartlist.get(index).getPrice());
//                            double price = count * p; // change
                            double price = (Math.round(count * p)); //change

                            finalTv_price.setText(options.getCurrency_symbol() + " " + ApplicationData.getStringValue(price));


                            textprice.get(index).setText(options.getCurrency_symbol() + " " + price);
                            subTotal = "" + price;
                            qnty = "" + count;

                            product_name = cartlist.get(index).getProduct_name();
                            product_id = cartlist.get(index).getProduct_id();
                            variations_id = cartlist.get(index).getVariation_id();

                            variations = cartlist.get(index).getVariations();
                            variation_price = cartlist.get(index).getPrice();
                            ori_price = cartlist.get(index).getOri_price();
                            tax_status = cartlist.get(index).getTax_status();
                            image = cartlist.get(index).getImage();

                            dataWrite.insertTOcart(product_id, product_name, variations, variations_id, variation_price, qnty, image, subTotal, ori_price, tax_status);
                            FechDataoftotal();

                        } else {

                            stock = Integer.parseInt(cartlist.get(index).getMax_stock());
                            if (count > stock) {
                                dataWrite.showToast(getString(R.string.there_is_no_more_product_available));
                            } else {
                                finalTv_qnty.setText("" + count);
                                textViews.get(index).setText("" + count);

                                double p = Double.parseDouble(cartlist.get(index).getPrice());
//                                double price = count * p; //change
                                double price = (Math.round(count * p)); //change

                                finalTv_price.setText(options.getCurrency_symbol() + " " + ApplicationData.getStringValue(price));

                                textprice.get(index).setText(options.getCurrency_symbol() + " " + price);
                                subTotal = "" + price;
                                qnty = "" + count;

                                product_name = cartlist.get(index).getProduct_name();
                                product_id = cartlist.get(index).getProduct_id();
                                variations_id = cartlist.get(index).getVariation_id();
                                variations = cartlist.get(index).getVariations();
                                variation_price = cartlist.get(index).getPrice();
                                ori_price = cartlist.get(index).getOri_price();
                                tax_status = cartlist.get(index).getTax_status();
                                image = cartlist.get(index).getImage();

                                dataWrite.insertTOcart(product_id, product_name, variations, variations_id, variation_price, qnty, image, subTotal, ori_price, tax_status);
                                FechDataoftotal();
                            }
                        }

                    }
                });

                iv_delete.setOnClickListener(new View.OnClickListener() {
                    int count;
                    int t = 0;

                    @Override
                    public void onClick(View view) {


                        t = Integer.parseInt(finalTv_qnty.getText().toString());
                        count = t - 1;
                        if (count <= 1) {
                            count = 1;
                            finalTv_qnty.setText("" + count);

                        } else {
                            finalTv_qnty.setText("" + count);
                        }
                        textViews.get(index).setText("" + count);

                        double p = Double.parseDouble(cartlist.get(index).getPrice());
                        double price = count * p;
                        finalTv_price.setText(options.getCurrency_symbol() + " " + ApplicationData.getStringValue(price));
                        textprice.get(index).setText(options.getCurrency_symbol() + " " + price);
                        subTotal = "" + price;
                        qnty = "" + count;
                        product_name = cartlist.get(index).getProduct_name();
                        product_id = cartlist.get(index).getProduct_id();
                        variations_id = cartlist.get(index).getVariation_id();
                        variations = cartlist.get(index).getVariations();
                        variation_price = cartlist.get(index).getPrice();
                        ori_price = cartlist.get(index).getOri_price();
                        tax_status = cartlist.get(index).getTax_status();
                        image = cartlist.get(index).getImage();

                        dataWrite.insertTOcart(product_id, product_name, variations, variations_id, variation_price, qnty, image, subTotal, ori_price, tax_status);
                        FechDataoftotal();
                    }
                });

                llCartlist.addView(view);

            }

        }

      /*  ShoppingcartAdapter shoppingcartAdapter = new ShoppingcartAdapter(cartlist);
        listView.setAdapter(shoppingcartAdapter);
        listView.deferNotifyDataSetChanged();*/
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                finish();
                break;
        }
    }

    private class ShoppingcartAdapter extends BaseAdapter {

        ArrayList<BeanCart> cartArrayList;
        LayoutInflater inflater;

        public ShoppingcartAdapter(ArrayList<BeanCart> cartlist) {
            this.cartArrayList = cartlist;
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return cartArrayList.size();
        }

        @Override
        public Object getItem(int i) {
            return cartArrayList.get(i);
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
                view = inflater.inflate(R.layout.inflater_shopingcart, null);
                vh.tv_product_Name = (TextView) view.findViewById(R.id.tvNameCart);
                vh.tv_price = (TextView) view.findViewById(R.id.tvPriceCart);
                vh.tv_qnty = (TextView) view.findViewById(R.id.tvQnty);
                vh.iv_image = (ImageView) view.findViewById(R.id.ivCartImage);
                vh.iv_add = (ImageView) view.findViewById(R.id.ivAdd);
                vh.iv_delete = (ImageView) view.findViewById(R.id.ivDelete);
                vh.iv_remove = (ImageView) view.findViewById(R.id.ivRemove);

            /*    textViews.add(vh.tv_qnty);
                textViews.get(i).requestLayout();
                textViews.get(i).setCursorVisible(true);*/
                vh.tv_qnty.requestLayout();
                vh.tv_qnty.setCursorVisible(true);
                view.setTag(vh);
            } else {

                vh = (ViewHolder) view.getTag();
            }

            final int index = i;
            vh.tv_product_Name.setText(cartArrayList.get(i).getProduct_name());
            vh.tv_qnty.setText(cartArrayList.get(i).getQnty());
            vh.tv_price.setText(options.getCurrency_symbol() + " " + cartArrayList.get(i).getPrice());


            URL image_string;
            try {
                image_string = new URL(cartArrayList.get(i).getImage());
                Log.d("TEST", "image_string is ==" + image_string);
                ImageLoader imageLoader = new ImageLoader(activity);
                imageLoader.DisplayImage(image_string.toString(), vh.iv_image);

            } catch (MalformedURLException e) {
                Log.d("TEST", "ERROR is ==" + e);
                e.printStackTrace();
            }

            vh.iv_remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dataWrite.deletedFromCart(cartArrayList.get(index).getProduct_id(),
                            cartArrayList.get(index).getVariation_id(), cartArrayList.get(index).getVariations());
                    fatchCart("cart");
                }
            });

            vh.iv_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int t = Integer.parseInt(vh.tv_qnty.getText().toString());
                    int count = Integer.parseInt(String.valueOf(t + 1));
                    vh.tv_qnty.setText("" + count);

                }
            });

            vh.iv_delete.setOnClickListener(new View.OnClickListener() {
                int count;
                int t = 0;

                @Override
                public void onClick(View view) {


                    t = Integer.parseInt(vh.tv_qnty.getText().toString());
                    count = t - 1;
                    if (count <= 0) {
                        count = 0;
                        vh.tv_qnty.setText("" + count);

                    } else {
                        vh.tv_qnty.setText("" + count);
                    }

                }
            });


            return view;
        }


        public class ViewHolder {
            TextView tv_product_Name, tv_price, tv_subtotal, tv_qnty;
            ImageView iv_image, iv_add, iv_delete, iv_remove;
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        status = manager.getPreferences(activity, AppConstant.STATUS);

        AppDebugLog.println("onResume is ====  " + status);
    }

    @Override
    public void onPause() {
        super.onPause();

    }
}
