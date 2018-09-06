package com.lujayn.wootouch.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.lujayn.wootouch.R;
import com.lujayn.wootouch.bean.BeanCart;
import com.lujayn.wootouch.bean.BeanCoupon;
import com.lujayn.wootouch.bean.BeanPaymentInfo;
import com.lujayn.wootouch.bean.BeanUserData;
import com.lujayn.wootouch.bean.Options;
import com.lujayn.wootouch.bean.Setting;
import com.lujayn.wootouch.bean.SettingOption;
import com.lujayn.wootouch.bean.Shipping_method;
import com.lujayn.wootouch.bean.Taxes;
import com.lujayn.wootouch.common.AppConstant;
import com.lujayn.wootouch.common.ApplicationContext;
import com.lujayn.wootouch.common.ApplicationData;

import com.lujayn.wootouch.common.Config;
import com.lujayn.wootouch.common.SessionManager;
import com.lujayn.wootouch.common.Webservice;
import com.lujayn.wootouch.custom.AppDebugLog;
import com.lujayn.wootouch.database.DataWrite;
import com.lujayn.wootouch.network.RequestTask;
import com.lujayn.wootouch.network.RequestTaskDelegate;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

/**
 * Created by Shailesh on 08/08/16.
 */
public class CheckOutActivity extends AppCompatActivity implements RequestTaskDelegate {
    RadioGroup rg_paymentMethod, rg_shipping;
    TextView tv_subTotal, tv_subtotalLable, tv_total, tv_exCharge, tv_couponcode,
            tv_couponCost, tv_placeOrder, tv_billingAddress, tv_shippingAddress,
            tv_changeBilling, tv_changeShipping, tv_title, tv_taxlabel, tv_tax, tv_taxincl, tv_taxshipIncl;
    EditText et_code;
    ArrayList<Double> array_total;
    ArrayList<BeanUserData> userDataArrayList;
    ArrayList<BeanCart> productArrayList;
    ArrayList<BeanCoupon> coupons;
    DataWrite dataWrite;
    BeanPaymentInfo paymentInfo;
    ArrayList<Shipping_method> firstShippinginfo;
    CheckBox chbx_AsShipping;
    CardView cardshipping_address, card_coupon;
    double sum = 0;
    double total_amount = 0;
    double pay_amount = 0;
    double ship_amount = 0;
    double coupon_cost = 0;
    double coupon_discount = 0;
    double ori_ship_amount = 0;
    double coupon_tax = 0;
    double ori_total = 0;
    double withoutTax_total = 0;
    LayoutInflater inflater;
    TextView btn_applyCoupon;
    ImageView iv_cnclCoupon;
    SessionManager manager;
    SettingOption settingOption;
    Options options;
    String shippingMethod = "", shipping_id, method_id;
    String paymentMethod = "";
    String payment_id = "";
    String paymentDetails = "";
    String coupon_type = "";
    String enable_shipping = "none";
    String key, TAXRATE, currency_code;
    String user_id, bill_address, bill_city, bill_state, bill_country, bill_email, bill_company, bill_country_code,
            bill_state_code, bill_fname, bill_lname, bill_phone, bill_postcode, ship_address, ship_city, ship_state, ship_country, ship_country_code,
            ship_company, ship_state_code, ship_fname, ship_lname, ship_postcode;
    String color_statusbar, color_tool_bg, color_tool_title, color_tool_icon, color_btn_bg, color_btn_text;
    RelativeLayout rl_Appcouponcode, rl_coupooncode, rl_tool, rl_tax, rl_shippingmethods;
    View vwCheckout13;
    LinearLayout ll_back;
    private android.app.AlertDialog progressDialog;
    //Payment Amount
    private String paymentAmount;
    double tax_sum = 0;
    double tax_b = 0;
    double ship_tax = 0;
    float Total_tax = 0;
    CardInputWidget mCardInputWidget;
    String label, ship_tx_label, baseAddress_country, baseAddress_state, stripe_token;
    ArrayList<Setting> settings;
    boolean iscall = false;
    private ApplicationData appData;
    //Paypal intent request code to track onActivityResult method
    public static final int PAYPAL_REQUEST_CODE = 123;
    ArrayList<Taxes>taxesArrayList = new ArrayList<Taxes>();//change


    //Paypal Configuration Object
    private static PayPalConfiguration config = new PayPalConfiguration()
            // Start with mock environment.  When ready, switch to sandbox (ENVIRONMENT_SANDBOX)
            // or live (ENVIRONMENT_PRODUCTION)
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(Config.PAYPAL_CLIENT_ID);


    /*payUBiz paymentgateway*/
    String checksum_paytm, hashKey, payment_related_details_for_mobile_sdk, vas_for_mobile_sdk_hash, get_merchant_ibibo_codes_hash, verify_payment_hash,
            send_sms_hash, delete_user_card_hash, get_user_cards_hash, edit_user_card_hash, save_user_card_hash;
    PaymentParams mPaymentParams;
    PayuConfig payuConfig;
    private String postData;
    private String url = "https://test.payu.in/_payment";//for testing
    //  private String url = "https://secure.payu.in/_payment";//for production
    //Mandatory feilds for hash generation
    //private static String key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCyj2olpMSBfDCjxpMKL2wDe3iskiHfst4t4aNSSCp2RjLq+N1Ua8vJn8KfvuHi0IxOae0KI5WAFhgDl35Wzbk9Mwq6+ZrFWs+SmDqSpRVz7sLcjzPD6UpJlDGqPpZqHLS0/lFkjShxXbfLgdgIWTrd0aOGSsdKMjWww8Rks2ia2fVeASdovu00stDB33d2kkyH9SXSmY0K2VjwVsHhga5YwRt1dn94L4hlCmpa38CNX7BM79DgvbRFKWSLLjEhGxs1rBRoDlAfpv4PTcM1iZGeq7vzbAnXuqVvqPUZKmPqTVW7yqgMz4Mn5MzHE0Q/GZa7efowDiW4+qqY7sNnN3nHAgMBAAECggEASailtg+pTG0Jt5Xj9FCpsyoNFstVk+06gBzTfc7tTUsaGBo/W2L4dx0jcTiSADndlz9VHFifhG4Yy+qDoJVO8/MJWyY6dW1HsYaAqxvUpLV6xvd/+DJVhdLPRhZ5gB6FYSCEPQGKDHIHrrjKgG8mvHiNh4lzeqUJxviLHKtx3GaavL4I9rgZSourpdKveijqIpv6vN/l1Tc8sT8M34EFQE+TZKwrU0r4EICyhshsiMrShJxm5YvY5LdsnlThyBkj4wkpTywAZNJkvoXxPm8rmpPkbs9NGQqHSRDdu36ZdZPjtYkVH2shHGrETBXVJssYap1wNgA4CuaZ4lUFt6Mv0QKBgQDb/fg4PcmR3xIkNyxJQpvCWeOOrTqSpBGddXypHHBmQUhvdFSOXuhiaHfm61XzeMIVvozdpfn+ckaMf8v/dzxU+qVo61/BU+22usmtBy3JI41VdCMa35gXnwTlEWr2oBJcjyl90e2/WweGDZLBzZVsLvkVy9gSm4mfmyrirbzOTwKBgQDPyWHJXnrTPKhe4VIxq5CcRUk/Q376M5k2BC1Df8NtmJhZZIj1Eklvwbk0TnQOigLws/OdqCVOIXCMJEEizA4GnCuRbyDkosV9EZTDnZIIJ33STCdY2ce9vhRtFxgdrkATE/uxnbDQQa16eajMfJfIz3iZSXnALE/FKn01a5r3CQKBgAPj9urPtOvefQyk6SSWh/ik7FBOofWL7z/QnxbCiP5V8zj9Xq1x4jwzSsOLr+ZxYUVPLGCTGdu1G6hKbkojSq7aC+jSGMxrdo2/uedUdCvVdB9pwWEtSRxJ1KVUbIbWB90ZLwuplcHHk1d8j/bbcuQJ5J/NDhIuK/3lmZmOpwMjAoGAUsD8jgtv1XNCB0+rwZZ3qJU7n9OM3a7xJP8y28AloB93BZsxpJEc6EJ6NWfNdwDmPOEedDrEaMKOpXHG80nbiCxoeaqNqiFdj/sRiXrW7yJzTO8BeLR7hYkPb4cUFrYQVMjPpCSOh9Kweae8X3x7owFmIX6O28eKGZdbeIwbkqkCgYEA2WzepGNwviWcDz8pO/ByE/qqSS81Xn+nbRXDHfvMWsHUAE1eFKMmCv7v3nOazCj085PKpZOEKDDGTwUYPAD1xOmsiXSKIQBdTQoyCvFBnoAByUA3A1Zxw6P8sS/O2uVaKw8rpCs2pycptJv/8zRFlzDmsJlOEwB+iwzH2wDUYWo=";//for test production,will work only at PayU only
    private static String marchent_key = "gtKFFx"; //for testing
    //private static String key = "0MQaQP";
    private static String transaction_Id;
    private static String amount = "10.00";
    private static String product_info = "products";
    private static String f_Name = "shailesh";
    private static String email = "shailesh@lujayninfoways.com";
    private static String s_Url = "https://payu.herokuapp.com/success";
    private static String f_Url = "https://payu.herokuapp.com/failure";
    private static String user_credentials = "";
    private static String udf1 = "";
    private static String udf2 = "";
    private static String udf3 = "";
    private static String udf4 = "";
    private static String udf5 = "";
    //optional feilds for hash generation
    private static String offer_key = " ";
    private static String cardBin = " ";
    private static String salt = "xrGqBLD6";
    private static int PAYUBIZ_REQUEST_CODE = 111;
    String orderid = "";

      /* Paytm:  checksum generating*/
    // String MID = "SimHom38504169920765";
    // String WEBSITE = "SimHomWAP";//"APP_STAGING"
    //  String MERCHANT_KEY = "zsDh1PAozMIAckDC";
    //  String Industry_type_id = "Retail109";
    //  String CHANNEL_ID = "WAP";
    // String CALLBACK_URL = "https://securegw.paytm.in/theia/paytmCallback?ORDER_ID=";
    Bundle PaytmBundle;
    boolean paytmBoolean = false;

    String MID = "Simply65869370763970";
    String WEBSITE = "APP_STAGING";//"APP_STAGING"
    String MERCHANT_KEY = "7unAxEM0&iLy4pg9";
    String Industry_type_id = "Retail";
    String CHANNEL_ID = "WAP";
    String CALLBACK_URL = "https://pguat.paytm.com/paytmchecksum/paytmCallback.jsp";
    Activity activity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        activity=this;
        manager = new SessionManager();
        appData = ApplicationData.getSharedInstance();
        coupons = new ArrayList<BeanCoupon>();
        settingOption = new SettingOption();
        options = new Options();

        settingOption = manager.getPreferencesOption(activity, AppConstant.SETTING_OPTION);

        options = settingOption.getData().getOptions();
        color_statusbar = settingOption.getData().getOptions().getStatus_bar_color();
        color_tool_bg = settingOption.getData().getOptions().getToolbar_back_color();
        color_tool_title = settingOption.getData().getOptions().getToolbar_title_color();
        color_tool_icon = settingOption.getData().getOptions().getToolbar_icon_color();
        color_btn_bg = settingOption.getData().getOptions().getCate_shape_color();
        color_btn_text = settingOption.getData().getOptions().getCate_name_color();
        currency_code = settingOption.getData().getOptions().getCurrency_code();

        String b = settingOption.getData().getOptions().getBase_location().getWoocommerce_default_country();


        String base[] = b.split(":");
        if (base.length > 1) {
            baseAddress_country = base[0];
            baseAddress_state = base[1];
        } else {
            baseAddress_country = base[0];
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(color_statusbar));
        }

        initialize();

    }

    private void initialize() {
        progressDialog = new SpotsDialog(activity, R.style.Custom);
        tv_title = (TextView) findViewById(R.id.tvTitle);
        tv_exCharge = (TextView) findViewById(R.id.tvcharge);
        tv_subTotal = (TextView) findViewById(R.id.tvSubtotal);
        tv_subtotalLable = (TextView) findViewById(R.id.tvlableSubtotal);
        tv_total = (TextView) findViewById(R.id.tvTotal);
        tv_placeOrder = (TextView) findViewById(R.id.btnPlaceOrder);
        tv_changeBilling = (TextView) findViewById(R.id.tvChangeBilling);
        tv_changeShipping = (TextView) findViewById(R.id.tvChangeShipping);
        btn_applyCoupon = (TextView) findViewById(R.id.btnApplyCoupon);
        iv_cnclCoupon = (ImageView) findViewById(R.id.ivcancelCoupon);
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        et_code = (EditText) findViewById(R.id.etCouponcode1);
        tv_couponcode = (TextView) findViewById(R.id.tvCouponcode);
        tv_couponCost = (TextView) findViewById(R.id.tvCouponCost);
        tv_billingAddress = (TextView) findViewById(R.id.tvBillingAddress_checkout);
        tv_shippingAddress = (TextView) findViewById(R.id.tvShippingAddress_checkout);
        tv_taxlabel = (TextView) findViewById(R.id.tvlableTax);
        tv_tax = (TextView) findViewById(R.id.tvTax);
        tv_taxincl = (TextView) findViewById(R.id.tvTaxIncl);
        tv_taxshipIncl = (TextView) findViewById(R.id.tvtaxShip);

        chbx_AsShipping = (CheckBox) findViewById(R.id.chbxBillingsameAsShippinn);
        rl_Appcouponcode = (RelativeLayout) findViewById(R.id.rlcellCoupon);
        rl_coupooncode = (RelativeLayout) findViewById(R.id.rlcellCoupon1);
        rl_tool = (RelativeLayout) findViewById(R.id.tool);
        rl_tax = (RelativeLayout) findViewById(R.id.rlcellTax);
        rl_shippingmethods = (RelativeLayout) findViewById(R.id.rlcellshipping);
        cardshipping_address = (CardView) findViewById(R.id.cardShipping);
        card_coupon = (CardView) findViewById(R.id.card_view_coupon);

        vwCheckout13 = (View) findViewById(R.id.vwCheckout13);
        RelativeLayout rl_header = (RelativeLayout) findViewById(R.id.rl_header);

        dataWrite = new DataWrite(this);
        dataWrite.open();
        FatchCartData();
        FatchUserData();

        tv_title.setText(R.string.checkout);
        tv_title.setTextColor(Color.parseColor(color_tool_title));

        rl_tool.setBackgroundColor(Color.parseColor(color_tool_bg));
        tv_changeBilling.setBackgroundColor(Color.parseColor(color_btn_bg));
        tv_changeBilling.setTextColor(Color.parseColor(color_btn_text));
        tv_changeShipping.setBackgroundColor(Color.parseColor(color_btn_bg));
        tv_changeShipping.setTextColor(Color.parseColor(color_btn_text));
        btn_applyCoupon.setBackgroundColor(Color.parseColor(color_btn_bg));
        btn_applyCoupon.setTextColor(Color.parseColor(color_btn_text));
        tv_placeOrder.setBackgroundColor(Color.parseColor(color_btn_bg));
        tv_placeOrder.setTextColor(Color.parseColor(color_btn_text));
        rl_header.setBackgroundColor(Color.parseColor(color_btn_bg));

        Drawable myIcon5 = getResources().getDrawable(R.drawable.ic_arrow_back);
        //ColorFilter filter5 = new LightingColorFilter( Color.TRANSPARENT, Color.TRANSPARENT);
        myIcon5.setColorFilter(Color.parseColor(color_tool_icon), PorterDuff.Mode.SRC_ATOP);


        rl_coupooncode.setVisibility(View.GONE);
        vwCheckout13.setVisibility(View.GONE);


        chbx_AsShipping.setChecked(true);

        Intent i = getIntent();
        if (i.hasExtra("GUEST")) {
            key = i.getStringExtra("GUEST");
        }

        if (i.hasExtra("USER")) {
            key = i.getStringExtra("USER");

            AppDebugLog.println("key==== " + key);
        }

        if (i.hasExtra("USER1")) {
            key = i.getStringExtra("USER1");
            AppDebugLog.println("key==== " + key);
            chbx_AsShipping.setChecked(false);
        }


        if (chbx_AsShipping.isChecked()) {
            manager.setPreferences(activity, AppConstant.BILL_AS_SHIP, "1");
            cardshipping_address.setVisibility(View.GONE);

        } else {

            manager.setPreferences(activity, AppConstant.BILL_AS_SHIP, "0");
            cardshipping_address.setVisibility(View.VISIBLE);
        }

        String billing = bill_fname + " " + bill_lname + "\n" +
                bill_address + "\n" + bill_country + " " + bill_state + "\n" +
                bill_city + " " + bill_postcode + "\n" +
                bill_phone;
        tv_billingAddress.setText(billing.trim());

        String shipping = ship_fname + " " + ship_lname + "\n" +
                ship_address + "\n" + ship_country + " " + ship_state + "\n" +
                ship_city + " " + ship_postcode;

        if (shipping.trim().equals("") || ship_fname == null) {
            tv_shippingAddress.setText(R.string.please_add_your_shipping_address);
        } else {
            tv_shippingAddress.setText(shipping.trim());
        }


        if (settingOption.getData().getOptions().getCoupons_enabled().equals("yes")) {
            card_coupon.setVisibility(View.VISIBLE);
        } else {
            card_coupon.setVisibility(View.GONE);
        }


        paymentmethods_call("method");
        FechDataoftotal();

        inflater = (LayoutInflater) getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater = LayoutInflater.from(activity);

        rg_paymentMethod = (RadioGroup) findViewById(R.id.rgPaymentMethod);

        rg_shipping = (RadioGroup) findViewById(R.id.rgShipping);
        tv_placeOrder.setOnClickListener(onClickMethod);
        btn_applyCoupon.setOnClickListener(onClickMethod);
        iv_cnclCoupon.setOnClickListener(onClickMethod);
        tv_changeShipping.setOnClickListener(onClickMethod);
        tv_changeBilling.setOnClickListener(onClickMethod);
        chbx_AsShipping.setOnClickListener(onClickMethod);
        ll_back.setOnClickListener(onClickMethod);
    }

    public View.OnClickListener onClickMethod = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnPlaceOrder:
                    if (payment_id.equals("stripe")) {

                        final Dialog dialog = new Dialog(activity);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setCancelable(false);
                        dialog.setContentView(R.layout.stripe_card);

                        mCardInputWidget = (CardInputWidget) dialog.findViewById(R.id.card_input_widget);
                        Button payButton = (Button) dialog.findViewById(R.id.btn_purchase);
                        /*payButton.setText(String.format(Locale.ENGLISH,
                                "Pay %s", mPrice));*/
                        payButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                getStripeToken();
                            }
                        });

                        dialog.show();

                    } else if (payment_id.equals("paypal")) {
                        getPayment();

                    } else if (payment_id.equals("payuindia")) {
                        callPayUbiz();

                    } else if (payment_id.equals("paytm")) {
                        //callPaytm();

                        sendData("order");

                    } else {
                        sendData("order");
                    }
                    break;


                case R.id.btnApplyCoupon:

                    applycoupon();


                    break;
                case R.id.ivcancelCoupon:

                    CancelCoupon();
                    break;


                case R.id.tvChangeBilling:
                    if (key.equals("guest")) {
                        Intent i = new Intent(activity, UpdateAddressActivity.class);
                        i.putExtra("G_BILL", "g_bill");
                        i.putExtra("GUEST", key);
                        startActivity(i);
                        finish();

                    } else {
                        Intent i = new Intent(activity, UpdateAddressActivity.class);
                        i.putExtra("BILL", "billing");
                        i.putExtra("USER", "user");
                        startActivity(i);
                        finish();
                    }

                    break;

                case R.id.tvChangeShipping:

                    if (key.equals("guest")) {
                        Intent intent = new Intent(activity, UpdateAddressActivity.class);
                        intent.putExtra("G_SHIP", "g_ship");
                        intent.putExtra("GUEST", key);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(activity, UpdateAddressActivity.class);
                        intent.putExtra("SHIP", "shipping");
                        intent.putExtra("USER", "user");
                        startActivity(intent);
                        finish();
                    }
                    break;

                case R.id.chbxBillingsameAsShippinn:

                    if (chbx_AsShipping.isChecked()) {
                        manager.setPreferences(activity, AppConstant.BILL_AS_SHIP, "1");
                        cardshipping_address.setVisibility(View.GONE);
                        rg_shipping.removeAllViews();
                        paymentmethods_call("method");

                    } else {
                        manager.setPreferences(activity, AppConstant.BILL_AS_SHIP, "0");
                        cardshipping_address.setVisibility(View.VISIBLE);
                        rg_shipping.removeAllViews();
                        paymentmethods_call("method");
                    }
                    break;

                case R.id.ll_back:
                    //activity.finish();
            /*        Intent i = new Intent(activity, MainActivity.class);
                    startActivity(i);*/
                    finish();

                   /* if (key.equals("user")){
                        Intent i = new Intent(activity, MainActivity.class);
                        startActivity(i);
                        finish();
                    }else {
                        activity.finish();
                    }*/
                    break;
            }

        }
    };


    private void callPayUbiz() {
        paymentAmount = "" + pay_amount;

        transaction_Id = System.currentTimeMillis() + "";
        postData = "&txnid=" + transaction_Id +
                "&device_type=1" +
                "&ismobileview=1" +
                "&productinfo=" + product_info +
                "&user_credentials=" + user_credentials +
                "&key=" + marchent_key +
                "&instrument_type=android device " +
                "&surl=" + s_Url +
                "&furl=" + f_Url + "" +
                "&instrument_id=7dd17561243c202" +
                "&firstname=" + bill_fname +
                "&email=" + bill_email +
                "&phone=" + bill_phone +
                "&amount=" + paymentAmount +
//                "&bankcode=PAYUW" + //for PayU Money
//                "&pg=WALLET"+//for PayU Money
                "&hash=";

        generateHashFromServer();
    }

    private void generateHashFromServer() {

        JSONObject object = new JSONObject();
        try {
            object.put("key", marchent_key);
            object.put("amount", paymentAmount);
            object.put("txnid", transaction_Id);
            object.put("email", bill_email);
            object.put("productinfo", product_info);
            object.put("firstname", bill_fname);
            object.put("udf1", udf1);
            object.put("udf2", udf2);
            object.put("udf3", udf3);
            object.put("udf4", udf4);
            object.put("udf5", udf5);
            object.put("user_credentials", user_credentials);

        } catch (Exception e) {
            // TODO: handle exception
        }

        //   Log.d("shailesh", "data  is==="+ object.toString());
//http://192.168.100.108/hashkey/hashkey.php
        //String requestURL = "http://192.168.100.107/woodemo/?webservice=1&vootouchservice=get_hashkey";
        String requestURL = Webservice.BASE_URL + "" + Webservice.URL_KEYHASH;
        AppDebugLog.println("requestURL in sendNewDataRequest : " + requestURL);
        RequestTask requestTask = new RequestTask(requestURL, AppConstant.HttpRequestType.DataUpdateRequest);
        requestTask.delegate = this;
        requestTask.execute(requestURL, "hashkey", object.toString());


    }

    private void getStripeToken() {
        Card card = mCardInputWidget.getCard();

        if (card == null) {
            //displayError("Card Input Error");
            return;
        }
        Stripe stripe = new Stripe(activity, "pk_test_mCHAO4yY5b49HBS3J5VSkKDK");
        stripe.createToken(card, new TokenCallback() {
            @Override
            public void onError(Exception error) {

            }

            @Override
            public void onSuccess(Token token) {
                Log.d("TAG", "TOKEN created= " + token.getId());
                stripe_token = token.getId();
                sendData("order");
            }
        });
    }


    private void CancelCoupon() {
        if (et_code.getText().length() == 0) {
            et_code.setError(getString(R.string.please_enter_code));

        } else {
            progressDialog.show();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("code", et_code.getText().toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }

            String requestURL = Webservice.BASE_URL + "" + Webservice.URL_REMOVE_COUPON;
            AppDebugLog.println("requestURL in sendNewDataRequest : " + requestURL);
            RequestTask requestTask = new RequestTask(requestURL, AppConstant.HttpRequestType.DataUpdateRequest);
            requestTask.delegate = this;
            requestTask.execute(requestURL, "removecode", jsonObject.toString());


        }
    }

    private void applycoupon() {

        if (et_code.getText().length() == 0) {
            et_code.setError(getString(R.string.please_enter_code));

        } else {
            progressDialog.show();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("code", et_code.getText().toString());
                if (user_id.equals("0.5")) {

                    jsonObject.put("guest_email", bill_email);
                    jsonObject.put("cart_total", total_amount);
                } else {
                    jsonObject.put("user_id", user_id);
                    jsonObject.put("cart_total", total_amount);

                }


                JSONArray array = new JSONArray();
                JSONObject jsonObject1;

                for (int j = 0; j < productArrayList.size(); j++) {
                    jsonObject1 = new JSONObject();

                    if (productArrayList.get(j).getVariation_id() == null) {
                        jsonObject1.putOpt("product_id", productArrayList.get(j).getProduct_id());

                    } else {
                        jsonObject1.putOpt("product_id", productArrayList.get(j).getVariation_id());

                    }

                    array.put(jsonObject1);
                }
                jsonObject.putOpt("product_data", array);

            } catch (JSONException e) {
                e.printStackTrace();
            }


            String requestURL = Webservice.BASE_URL + "" + Webservice.URL_COUPON_CODE;
            AppDebugLog.println("requestURL in sendNewDataRequest : " + requestURL);
            RequestTask requestTask = new RequestTask(requestURL, AppConstant.HttpRequestType.DataUpdateRequest);
            requestTask.delegate = this;
            requestTask.execute(requestURL, "code", jsonObject.toString());


        }
    }


    private void FatchUserData() {
        userDataArrayList = dataWrite.FetchUserData();
        for (int i = 0; i < userDataArrayList.size(); i++) {

            user_id = userDataArrayList.get(i).getID();
            bill_address = userDataArrayList.get(i).getBilling_address_1();
            bill_city = userDataArrayList.get(i).getBilling_city();
            bill_state = userDataArrayList.get(i).getBilling_state();
            bill_country = userDataArrayList.get(i).getBilling_country();
            bill_email = userDataArrayList.get(i).getBilling_email();
            bill_fname = userDataArrayList.get(i).getBilling_first_name();
            bill_lname = userDataArrayList.get(i).getBilling_last_name();
            bill_phone = userDataArrayList.get(i).getBilling_phone();
            bill_postcode = userDataArrayList.get(i).getBilling_postcode();
            bill_company = userDataArrayList.get(i).getBilling_company();
            bill_country_code = userDataArrayList.get(i).getBilling_country_code();
            bill_state_code = userDataArrayList.get(i).getBilling_state_code();

            ship_address = userDataArrayList.get(i).getShipping_address_1();
            ship_city = userDataArrayList.get(i).getShipping_city();
            ship_state = userDataArrayList.get(i).getShipping_state();
            ship_country = userDataArrayList.get(i).getShipping_country();
            ship_fname = userDataArrayList.get(i).getShipping_first_name();
            ship_lname = userDataArrayList.get(i).getShipping_last_name();
            ship_postcode = userDataArrayList.get(i).getShipping_postcode();
            ship_country_code = userDataArrayList.get(i).getShipping_country_code();
            ship_state_code = userDataArrayList.get(i).getShipping_state_code();
            ship_company = userDataArrayList.get(i).getShipping_company();

        }

        user_credentials = marchent_key + ":" + user_id;
    }

    private void FatchCartData() {
        productArrayList = dataWrite.fatchCartData();

        AppDebugLog.println("list size=== " + productArrayList.size());
        tv_subtotalLable.setText(getString(R.string.price) + "(" + productArrayList.size() + " " + getString(R.string.items) + ")");
        for (int i = 0; i < productArrayList.size(); i++) {

            if (productArrayList.get(i).getTax_status().equals("taxable")) {
                ori_total = ori_total + Double.parseDouble(productArrayList.get(i).getOri_price())
                        * Double.parseDouble(productArrayList.get(i).getQnty());
            } else {
                withoutTax_total = withoutTax_total + Double.parseDouble(productArrayList.get(i).getOri_price())
                        * Double.parseDouble(productArrayList.get(i).getQnty());

            }

        }

    }

    private void sendData(String order) {
        progressDialog.show();
        orderid = manager.getPreferences(ApplicationContext.getAppContext(), AppConstant.ORDER_ID);
        JSONObject object = new JSONObject();
        if (user_id.equals("0.5")) {

        } else {
            try {
                object.putOpt("user_id", user_id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        AppDebugLog.println("ship_fname === " + ship_fname);

        if (chbx_AsShipping.isChecked()) {

            if (payment_id.equals("")) {

                dataWrite.showToast(getString(R.string.please_Check_Payment_method));
                progressDialog.dismiss();
            } else {

                try {

                    //change
                    taxesArrayList= paymentInfo.getData().getTaxes();
                    JSONArray jsonArrayTaxId = new JSONArray();
                    JSONObject jsonObjectTaxId = new JSONObject();
                    for (int i = 0;i<taxesArrayList.size();i++)
                    {
                        String txtId = paymentInfo.getData().getTaxes().get(i).getTax_id();
                        jsonObjectTaxId.putOpt("TaxID", txtId);
                        jsonArrayTaxId.put(jsonObjectTaxId);
                        AppDebugLog.println("taxId-=====---=-=--=-=-=-=--="+jsonArrayTaxId);//chan
                    }
                    // change

                    object.putOpt("shipp_method", shippingMethod);
                    object.putOpt("ship_amount", ori_ship_amount);
                    object.putOpt("ship_id", shipping_id);
                    object.putOpt("ship_method_id", method_id);
                    object.putOpt("payment_method", paymentMethod);
                    object.putOpt("payment_id", payment_id);
                    object.putOpt("total_amount", total_amount);
                    object.putOpt("pay_amount", pay_amount);
                    object.putOpt("tax_before_coupon", tax_b);
                    object.putOpt("ship_tax", ship_tax);
                    object.putOpt("tax_id", jsonArrayTaxId);//change
                    AppDebugLog.println("taxId-=====---=-=--=-=-=-=--="+jsonArrayTaxId);//change
//                    object.putOpt("tax_id", paymentInfo.getData().getTaxes().getTax_id()); //change this is old whenever tax object use

                    String Asshipp = manager.getPreferences(activity, AppConstant.BILL_AS_SHIP);
                    if (Asshipp.equals("1")) {
                        object.putOpt("use_billing_address", Asshipp);
                    } else {
                        object.putOpt("use_billing_address", Asshipp);

                        object.putOpt("shipping_first_name", ship_fname);
                        object.putOpt("shipping_last_name", ship_lname);
                        object.putOpt("shipping_country", ship_country);
                        object.putOpt("shipping_address_1", ship_address);
                        object.putOpt("shipping_city", ship_city);
                        object.putOpt("shipping_state", ship_state);
                        object.putOpt("shipping_postcode", ship_postcode);
                        object.putOpt("shipping_company", ship_company);
                    }

                    object.putOpt("billing_first_name", bill_fname);
                    object.putOpt("billing_last_name", bill_lname);
                    object.putOpt("billing_email", bill_email);
                    object.putOpt("billing_phone", bill_phone);
                    object.putOpt("billing_country", bill_country);
                    object.putOpt("billing_address_1", bill_address);
                    object.putOpt("billing_city", bill_city);
                    object.putOpt("billing_state", bill_state);
                    object.putOpt("billing_postcode", bill_postcode);
                    object.putOpt("billing_company", bill_company);


                    if (coupons.size() != 0) {

                        object.putOpt("coupon_amount", coupons.get(0).getAmount());
                        object.putOpt("coupon_code", coupons.get(0).getCode());
                        object.putOpt("coupon_id", coupons.get(0).getId());
                        object.put("coupon_type", coupons.get(0).getType());
                        object.putOpt("discount_coupon", coupon_discount);
                        object.putOpt("tax_after_coupon", coupon_tax);
                        //object.putOpt("discount_coupon_tax",discount_coupon_tax);

                    } else {
                        object.putOpt("tax_after_coupon", tax_b);
                    }

                    JSONArray array = new JSONArray();
                    JSONObject jsonObject;
                    for (int j = 0; j < productArrayList.size(); j++) {
                        jsonObject = new JSONObject();
                        if (productArrayList.get(j).getVariation_id() == null) {
                            jsonObject.putOpt("product_id", productArrayList.get(j).getProduct_id());
                        } else {
                            jsonObject.putOpt("product_id", productArrayList.get(j).getProduct_id());
                            jsonObject.putOpt("variation_id", productArrayList.get(j).getVariation_id());
                        }

                        jsonObject.putOpt("qty", productArrayList.get(j).getQnty());
                        array.put(jsonObject);
                    }
                    object.putOpt("postdata", array);
                    if (payment_id.equals("paypal")) {
                        JSONObject paymentDetail = new JSONObject();
                        JSONObject paymentObject1 = new JSONObject(paymentDetails);

                        JSONObject clientObject1 = paymentObject1.optJSONObject("client");
                        JSONObject client = new JSONObject();
                        client.putOpt("environment", clientObject1.getString("environment"));
                        client.putOpt("paypal_sdk_version", clientObject1.getString("paypal_sdk_version"));
                        client.putOpt("platform", clientObject1.getString("platform"));
                        client.putOpt("product_name", clientObject1.getString("product_name"));

                        paymentDetail.putOpt("client", client);

                        JSONObject responseJsonObject1 = paymentObject1.optJSONObject("response");
                        JSONObject response = new JSONObject();
                        response.putOpt("create_time", responseJsonObject1.getString("create_time"));
                        response.putOpt("id", responseJsonObject1.getString("id"));
                        response.putOpt("intent", responseJsonObject1.getString("intent"));
                        response.putOpt("state", responseJsonObject1.getString("state"));

                        paymentDetail.putOpt("response", response);
                        paymentDetail.putOpt("response_type", paymentObject1.getString("response_type"));

                        object.putOpt("payment_detail", paymentDetail);
                    }

                    if (payment_id.equals("stripe")) {
                        object.putOpt("stripe_token", stripe_token);
                    }

                    if (paytmBoolean == false) {

                    } else {
                        if (payment_id.equals("paytm")) {

                            JSONObject paytmdata = new JSONObject();
                            paytmdata.putOpt("STATUS", PaytmBundle.getString("STATUS"));
                            paytmdata.putOpt("CHECKSUMHASH", PaytmBundle.getString("CHECKSUMHASH"));
                            paytmdata.putOpt("BANKNAME", PaytmBundle.getString("BANKNAME"));
                            paytmdata.putOpt("ORDERID", PaytmBundle.getString("ORDERID"));
                            paytmdata.putOpt("TXNAMOUNT", PaytmBundle.getString("TXNAMOUNT"));
                            paytmdata.putOpt("TXNDATE", PaytmBundle.getString("TXNDATE"));
                            paytmdata.putOpt("MID", PaytmBundle.getString("MID"));
                            paytmdata.putOpt("TXNID", PaytmBundle.getString("TXNID"));
                            paytmdata.putOpt("RESPCODE", PaytmBundle.getString("RESPCODE"));
                            paytmdata.putOpt("PAYMENTMODE", PaytmBundle.getString("PAYMENTMODE"));
                            paytmdata.putOpt("BANKTXNID", PaytmBundle.getString("BANKTXNID"));
                            paytmdata.putOpt("CURRENCY", PaytmBundle.getString("CURRENCY"));
                            paytmdata.putOpt("GATEWAYNAME", PaytmBundle.getString("GATEWAYNAME"));
                            paytmdata.putOpt("RESPMSG", PaytmBundle.getString("RESPMSG"));

                            object.putOpt("paytm", paytmdata);
                            object.putOpt("order_id", orderid);

                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }


                String requestURL = Webservice.BASE_URL + "" + Webservice.URL_SAVE_ORDERS;
                AppDebugLog.println("requestURL in sendNewDataRequest : " + requestURL);
                RequestTask requestTask = new RequestTask(requestURL, AppConstant.HttpRequestType.DataUpdateRequest);
                requestTask.delegate = this;
                requestTask.execute(requestURL, order, object.toString());

            }

        } else {

            if (shippingMethod.equals("") || payment_id.equals("") || ship_fname.equals("")) {
                // Toast.makeText(activity,"please Select shipping Method and Payment method",Toast.LENGTH_SHORT).show();
                dataWrite.showToast(getString(R.string.please_Check_shipping_Method_Payment_method_and_Shipping_address));
                progressDialog.dismiss();
            } else {

                try {

                    //change
                    JsonArray jsonArrayTaxId = new JsonArray();
                    for (int i = 0;i<taxesArrayList.size();i++)
                    {
                        String txtId = paymentInfo.getData().getTaxes().get(i).getTax_id();
                        jsonArrayTaxId.add(txtId);
                    }
                    // change

                    object.putOpt("shipp_method", shippingMethod);
                    object.putOpt("ship_amount", ship_amount);
                    object.putOpt("ship_id", shipping_id);
                    object.putOpt("ship_method_id", method_id);
                    object.putOpt("payment_method", paymentMethod);
                    object.putOpt("payment_id", payment_id);
                    object.putOpt("total_amount", total_amount);
                    object.putOpt("pay_amount", pay_amount);
                    object.putOpt("tax_before_coupon", tax_b);
                    object.putOpt("ship_tax", ship_tax);
//                    object.putOpt("tax_id", paymentInfo.getData().getTaxes().getTax_id()); //change
                    object.putOpt("tax_id", jsonArrayTaxId);//change


                    String Asshipp = manager.getPreferences(activity, AppConstant.BILL_AS_SHIP);
                    if (Asshipp.equals("1")) {
                        object.putOpt("use_billing_address", Asshipp);
                    } else {
                        object.putOpt("use_billing_address", Asshipp);

                        object.putOpt("shipping_first_name", ship_fname);
                        object.putOpt("shipping_last_name", ship_lname);
                        object.putOpt("shipping_country", ship_country);
                        object.putOpt("shipping_address_1", ship_address);
                        object.putOpt("shipping_city", ship_city);
                        object.putOpt("shipping_state", ship_state);
                        object.putOpt("shipping_postcode", ship_postcode);
                        object.putOpt("shipping_company", ship_company);
                    }

                    object.putOpt("billing_first_name", bill_fname);
                    object.putOpt("billing_last_name", bill_lname);
                    object.putOpt("billing_email", bill_email);
                    object.putOpt("billing_phone", bill_phone);
                    object.putOpt("billing_country", bill_country);
                    object.putOpt("billing_address_1", bill_address);
                    object.putOpt("billing_city", bill_city);
                    object.putOpt("billing_state", bill_state);
                    object.putOpt("billing_postcode", bill_postcode);
                    object.putOpt("billing_company", bill_company);


                    if (coupons.size() != 0) {

                        object.putOpt("coupon_amount", coupons.get(0).getAmount());
                        object.putOpt("coupon_code", coupons.get(0).getCode());
                        object.putOpt("coupon_id", coupons.get(0).getId());
                        object.put("coupon_type", coupons.get(0).getType());
                        object.putOpt("discount_coupon", coupon_discount);
                        object.putOpt("tax_after_coupon", coupon_tax);
                        //object.putOpt("discount_coupon_tax",discount_coupon_tax);

                    } else {
                        object.putOpt("tax_after_coupon", tax_b);
                    }

                    JSONArray array = new JSONArray();
                    JSONObject jsonObject;
                    for (int j = 0; j < productArrayList.size(); j++) {
                        jsonObject = new JSONObject();
                        if (productArrayList.get(j).getVariation_id() == null) {
                            jsonObject.putOpt("product_id", productArrayList.get(j).getProduct_id());
                        } else {
                            jsonObject.putOpt("product_id", productArrayList.get(j).getProduct_id());
                            jsonObject.putOpt("variation_id", productArrayList.get(j).getVariation_id());
                        }

                        jsonObject.putOpt("qty", productArrayList.get(j).getQnty());
                        array.put(jsonObject);
                    }
                    object.putOpt("postdata", array);
                    if (payment_id.equals("paypal")) {
                        JSONObject paymentDetail = new JSONObject();
                        JSONObject paymentObject1 = new JSONObject(paymentDetails);

                        JSONObject clientObject1 = paymentObject1.optJSONObject("client");
                        JSONObject client = new JSONObject();
                        client.putOpt("environment", clientObject1.getString("environment"));
                        client.putOpt("paypal_sdk_version", clientObject1.getString("paypal_sdk_version"));
                        client.putOpt("platform", clientObject1.getString("platform"));
                        client.putOpt("product_name", clientObject1.getString("product_name"));

                        paymentDetail.putOpt("client", client);

                        JSONObject responseJsonObject1 = paymentObject1.optJSONObject("response");
                        JSONObject response = new JSONObject();
                        response.putOpt("create_time", responseJsonObject1.getString("create_time"));
                        response.putOpt("id", responseJsonObject1.getString("id"));
                        response.putOpt("intent", responseJsonObject1.getString("intent"));
                        response.putOpt("state", responseJsonObject1.getString("state"));

                        paymentDetail.putOpt("response", response);
                        paymentDetail.putOpt("response_type", paymentObject1.getString("response_type"));

                        object.putOpt("payment_detail", paymentDetail);
                    }


                    if (payment_id.equals("stripe")) {
                        object.putOpt("stripe_token", stripe_token);
                    }

                    if (paytmBoolean == false) {

                    } else {

                        if (payment_id.equals("paytm")) {

                            JSONObject paytmdata = new JSONObject();
                            paytmdata.putOpt("STATUS", PaytmBundle.getString("STATUS"));
                            paytmdata.putOpt("CHECKSUMHASH", PaytmBundle.getString("CHECKSUMHASH"));
                            paytmdata.putOpt("BANKNAME", PaytmBundle.getString("BANKNAME"));
                            paytmdata.putOpt("ORDERID", PaytmBundle.getString("ORDERID"));
                            paytmdata.putOpt("TXNAMOUNT", PaytmBundle.getString("TXNAMOUNT"));
                            paytmdata.putOpt("TXNDATE", PaytmBundle.getString("TXNDATE"));
                            paytmdata.putOpt("MID", PaytmBundle.getString("MID"));
                            paytmdata.putOpt("TXNID", PaytmBundle.getString("TXNID"));
                            paytmdata.putOpt("RESPCODE", PaytmBundle.getString("RESPCODE"));
                            paytmdata.putOpt("PAYMENTMODE", PaytmBundle.getString("PAYMENTMODE"));
                            paytmdata.putOpt("BANKTXNID", PaytmBundle.getString("BANKTXNID"));
                            paytmdata.putOpt("CURRENCY", PaytmBundle.getString("CURRENCY"));
                            paytmdata.putOpt("GATEWAYNAME", PaytmBundle.getString("GATEWAYNAME"));
                            paytmdata.putOpt("RESPMSG", PaytmBundle.getString("RESPMSG"));

                            object.putOpt("paytm", paytmdata);
                            object.putOpt("order_id", orderid);

                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }


                String requestURL = Webservice.BASE_URL + "" + Webservice.URL_SAVE_ORDERS;
                AppDebugLog.println("requestURL in sendNewDataRequest : " + requestURL);
                RequestTask requestTask = new RequestTask(requestURL, AppConstant.HttpRequestType.DataUpdateRequest);
                requestTask.delegate = this;
                requestTask.execute(requestURL, order, object.toString());


            }
        }

    }

    private void getPayment() {
        //Getting the amount from editText
        paymentAmount = "" + pay_amount;

        //Creating a paypalpayment
        PayPalPayment payment = new PayPalPayment(new BigDecimal(String.valueOf(paymentAmount)), currency_code, "vootouch fee",
                PayPalPayment.PAYMENT_INTENT_SALE);

        //Creating Paypal Payment activity intent
        Intent intent = new Intent(this, PaymentActivity.class);

        //putting the paypal configuration to the intent
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        //Puting paypal payment to the intent
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

        //Starting the intent activity for result
        //the request code will be used on the method onActivityResult
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //If the result is from paypal
        if (requestCode == PAYPAL_REQUEST_CODE) {

            //If the result is OK i.e. user has not canceled the payment
            if (resultCode == Activity.RESULT_OK) {
                //Getting the payment confirmation
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);

                //if confirmation is not null
                if (confirm != null) {
                    try {
                        //Getting the payment details
                        paymentDetails = confirm.toJSONObject().toString(4);
                        Log.i("paymentExample", paymentDetails);
                        AppDebugLog.println("paymentExample = " + paymentDetails);
                        sendData("order");

                        //Starting a new activity for the payment details and also putting the payment details with intent
                        startActivity(new Intent(this, ConfirmationActivity.class)
                                .putExtra("PaymentDetails", paymentDetails)
                                .putExtra("PaymentAmount", paymentAmount));

                    } catch (JSONException e) {

                        AppDebugLog.println("an extremely unlikely failure occurred: " + e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("paymentExample", "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i("paymentExample", "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        } else if (requestCode == PAYUBIZ_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
//success
                if (data != null)
                    // Toast.makeText(this, "Success" + data.getStringExtra("result"), Toast.LENGTH_LONG).show();
                    // AppDebugLog.println("TEST----"+data.getStringExtra("result"));
                    Log.e("TEST", "resutldata== " + data.getStringExtra("result"));
                sendData("order");

            }
            if (resultCode == RESULT_CANCELED) {
//failed
                if (data != null)
                    Toast.makeText(this, "Failed" + data.getStringExtra("result"), Toast.LENGTH_LONG).show();
                // AppDebugLog.println("TEST----"+data.getStringExtra("result"));
                Log.e("TEST", "resutldata== " + data.getStringExtra("result"));
            }
        }
    }

    @Override
    public void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    private void paymentmethods_call(String method) {
        progressDialog.show();
        dataWrite.deletedMethod();

        JSONObject object = new JSONObject();
        //change
        JSONObject jsonObject2 = null;//change
        JSONArray array2 = new JSONArray();//change
        //change
        try {
            String Asshipp = manager.getPreferences(activity, AppConstant.BILL_AS_SHIP);
            if (Asshipp.equals("1")) {

                //change
                productArrayList = dataWrite.fatchCartData();
                for (int j = 0; j < productArrayList.size(); j++) {
                    jsonObject2 = new JSONObject();

                    jsonObject2.putOpt("product_id", productArrayList.get(j).getProduct_id());
                    array2.put(jsonObject2);
                }
                //changeover

                object.put("country", bill_country_code);
                object.put("state", bill_state_code);
                object.put("postcode", bill_postcode);
                object.put("city", bill_city);
                object.put("product_data", array2);// chaange reqired



                AppDebugLog.println("bill_country_code= " + bill_country_code);
                AppDebugLog.println("bill_state_code = " + bill_state_code);
                AppDebugLog.println("bill_postcode = " + bill_postcode);
                AppDebugLog.println("bill_city = " + bill_city);
                AppDebugLog.println("product_data = " + array2);//change



            } else {

                object.put("country", ship_country_code);
                object.put("state", ship_state_code);
                object.put("postcode", ship_postcode);
                object.put("city", ship_city);
                object.put("product_data", array2); //change



                AppDebugLog.println("ship_country_code= " + ship_country_code);
                AppDebugLog.println("ship_state_code = " + ship_state_code);
                AppDebugLog.println("ship_postcode = " + ship_postcode);
                AppDebugLog.println("ship_city = " + ship_city);
                AppDebugLog.println("product_data = " + array2);//change


            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String requestURL = Webservice.BASE_URL + "" + Webservice.URL_SHIPPIN_METHOD;
        AppDebugLog.println("requestURL in sendNewDataRequest : " + requestURL);
        RequestTask requestTask = new RequestTask(requestURL, AppConstant.HttpRequestType.DataUpdateRequest);
        requestTask.delegate = this;
        requestTask.execute(requestURL, method, object.toString());

    }

    private void paymentMethod() {
        final RadioButton[] radioButton = new RadioButton[paymentInfo.getData().getPayment_gateways().size()];

        for (int i = 0; i < paymentInfo.getData().getPayment_gateways().size(); i++) {

            radioButton[i] = new RadioButton(activity);
            radioButton[i].setTextColor(getResources().getColor(R.color.color_grey_light));
            radioButton[i].setText(paymentInfo.getData().getPayment_gateways().get(i).getTitle());


            final Rect rc = new Rect();
            radioButton[i].getWindowVisibleDisplayFrame(rc);
            int[] xy = new int[2];
            radioButton[i].getLocationInWindow(xy);
            rc.offset(xy[0], xy[1]);

            final int finalI = i;
            final int finalI1 = i;
            radioButton[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String id = paymentInfo.getData().getPayment_gateways().get(finalI).getId();
                    String pay_title = paymentInfo.getData().getPayment_gateways().get(finalI).getTitle();

                    AppDebugLog.println("shipping name" + paymentInfo.getData().getPayment_gateways().get(finalI).getTitle());

                    if (id.equals("bacs")) {

                        AppDebugLog.println("shipping name= " + id);
                        paymentMethod = pay_title;
                        payment_id = id;

                        AppConstant.payment_gateways = paymentInfo.getData().getPayment_gateways().get(finalI);
                    } else if (id.equals("cheque")) {
                        AppDebugLog.println("shipping name= " + id);

                        paymentMethod = pay_title;
                        payment_id = id;
                        AppConstant.payment_gateways = paymentInfo.getData().getPayment_gateways().get(finalI);
                    } else if (id.equals("cod")) {
                        paymentMethod = pay_title;
                        payment_id = id;
                        AppConstant.payment_gateways = paymentInfo.getData().getPayment_gateways().get(finalI);
                        //fragment = new CashOnDelivery();
                        AppDebugLog.println("shipping name= " + id);
                    } else if (id.equals("paypal")) {
                        AppDebugLog.println("shipping name= " + id);
                        paymentMethod = pay_title;
                        payment_id = id;
                        AppConstant.payment_gateways = paymentInfo.getData().getPayment_gateways().get(finalI);
                        // fragment = new PaypalFragment();

                    } else if (id.equals("stripe")) {

                        AppDebugLog.println("shipping name= " + id);
                        paymentMethod = pay_title;
                        payment_id = id;
                        AppConstant.payment_gateways = paymentInfo.getData().getPayment_gateways().get(finalI);

                    } else if (id.equals("payuindia")) {
                        AppDebugLog.println("shipping name= " + id);
                        paymentMethod = pay_title;
                        payment_id = id;
                        AppConstant.payment_gateways = paymentInfo.getData().getPayment_gateways().get(finalI);
                    } else if (id.equals("paytm")) {
                        AppDebugLog.println("shipping name= " + id);
                        paymentMethod = pay_title;
                        payment_id = id;
                        AppConstant.payment_gateways = paymentInfo.getData().getPayment_gateways().get(finalI);
                    }

                    LayoutInflater layoutInflater1
                            = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    final View popupView1 = layoutInflater1.inflate(R.layout.popup_paymentmethod, null);
                    final PopupWindow popupWindow1 = new PopupWindow(
                            popupView1,
                            ViewGroup.LayoutParams.FILL_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    popupWindow1.setBackgroundDrawable(new BitmapDrawable());
                    popupWindow1.setOutsideTouchable(true);
                    popupWindow1.setAnimationStyle(R.style.PopupAnimation);
                    LinearLayout llPopup = (LinearLayout) popupView1.findViewById(R.id.llpopup);
                    llPopup.setBackgroundColor(Color.parseColor(color_btn_bg));
                    TextView tv_inst = (TextView) popupView1.findViewById(R.id.tvInstruction_payment);
                    tv_inst.setText(Html.fromHtml("" + paymentInfo.getData().getPayment_gateways().get(finalI).getDescription()));
                    popupWindow1.showAtLocation(radioButton[finalI1], Gravity.NO_GRAVITY, rc.left, rc.centerY());

                }
            });


            if (method_id != null) {

                if (paymentInfo.getData().getPayment_gateways().get(i).getEnable_for_methods().size() == 0) {
                    rg_paymentMethod.addView(radioButton[i]);

                } else {

                    for (int j = 0; j < paymentInfo.getData().getPayment_gateways().get(i).getEnable_for_methods().size(); j++) {

                        if (method_id.equals(paymentInfo.getData().getPayment_gateways().get(i).getEnable_for_methods().get(j).toString())) {
                            rg_paymentMethod.addView(radioButton[i]);
                            //Log.d("shipping", "cash on delvery added");
                        }
                    }
                }

            } else {
                rg_paymentMethod.addView(radioButton[i]);
            }


        }
    }


    private void ShippingMethod(double v) {


        firstShippinginfo = new ArrayList<Shipping_method>();

        final RadioButton[] radioButton = new RadioButton[paymentInfo.getData().getShipping_method().size()];
        for (int i = 0; i < paymentInfo.getData().getShipping_method().size(); i++) {
            //Log.e("TEST", "setting=----------------min_cost----- " + min_cost+",, requires== "+requires);
            String requires = paymentInfo.getData().getShipping_method().get(i).getRequires();
            float min_cost = 0;
            if (paymentInfo.getData().getShipping_method().get(i).getMin_amount().equals("")) {
                min_cost = 0;
            } else {
                min_cost = Float.parseFloat(paymentInfo.getData().getShipping_method().get(i).getMin_amount());
            }
            final Shipping_method shipmethod = new Shipping_method();
            radioButton[i] = new RadioButton(activity);
            radioButton[i].setText(paymentInfo.getData().getShipping_method().get(i).getTitle());
            radioButton[i].setTextColor(getResources().getColor(R.color.color_grey_light));


            AppDebugLog.println("shipping name =" + requires);
            if (requires.equals("min_amount")) {
                if (v < min_cost) {

                    AppDebugLog.println("free_shipping------------");
                } else {
                    final int finalI = i;
                    radioButton[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            //change
                            for (int i = 0;i<taxesArrayList.size();i++)
                            {
                                String txtId = paymentInfo.getData().getTaxes().get(i).getTax_id();
                                AppDebugLog.println("TaxId-=-=-=-=-=-=-=-=-=-=-=-=-"+txtId);
                            }
                            // change

//                            TAXRATE = paymentInfo.getData().getTaxes().getRate(); //change
                            TAXRATE = paymentInfo.getData().getTaxes().get(0).getRate();

                            shippingMethod = paymentInfo.getData().getShipping_method().get(finalI).getTitle();
                            shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                            method_id = paymentInfo.getData().getShipping_method().get(finalI).getMethod_id();


                            if ((paymentInfo.getData().getShipping_method().get(finalI).getCost() == null) ||
                                    (paymentInfo.getData().getShipping_method().get(finalI).getCost().equals(""))) {
                                tv_exCharge.setText(options.getCurrency_symbol() + " " + 0.0);
                                ship_amount = 0;
                                ori_ship_amount = 0;
                                //total_amount = sum;

                                tv_taxshipIncl.setText("");

                                tv_taxincl.setText(" (" + settingOption.getData().getOptions().getTax_display_cart() + " " +
                                        settingOption.getData().getOptions().getCurrency_symbol() + "" + ApplicationData.getStringValue(tax_sum + ship_tax) + " " + label + ")");

                                if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {
                                    ship_tax = 0;
                                    if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                        exludetax(tax_sum, ship_tax);

                                    } else {
                                        includtax(tax_sum, ship_tax);
                                    }

                                } else {
                                    tv_taxincl.setVisibility(View.GONE);
                                    rl_tax.setVisibility(View.GONE);
                                    tax_sum = 0;
                                    ship_tax = 0;

                                }
                                TotalCount(total_amount, coupon_cost, ship_amount, coupon_type);

                                //tv_total.setText(options.getCurrency_symbol()+" "+sum);
                            } else {
                                // double ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                double s = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                //shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                                ori_ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {

                                    if (paymentInfo.getData().getShipping_method().get(finalI).getTax_status().equals("taxable")) {
                                        //AppDebugLog.println("ship_tax,   TAXRATE== is== "+ship_tax+"-----------"+TAXRATE);
                                        ship_tax = s * (Double.parseDouble(TAXRATE) / 100);

                                        if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                            tv_taxshipIncl.setText("");

                                            exludetax(tax_sum, ship_tax);

                                        } else {

                                            s = s + ship_tax;
                                            tv_taxshipIncl.setText(ship_tx_label);
                                            includtax(tax_sum, ship_tax);

                                        }

                                    } else {
                                        tv_taxshipIncl.setText("");
                                        ship_tax = 0;

                                        if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                            exludetax(tax_sum, ship_tax);
                                        } else {
                                            includtax(tax_sum, ship_tax);
                                        }

                                    }

                                } else {
                                    tv_taxincl.setVisibility(View.GONE);
                                    tv_taxshipIncl.setText("");
                                    ship_tax = 0;
                                    tax_sum = 0;

                                }

                                tv_exCharge.setText(options.getCurrency_symbol() + " " + s);
                                ship_amount = s;
                                //total_amount = sub+s;
                                tv_taxincl.setText(" (" + settingOption.getData().getOptions().getTax_display_cart() + " " +
                                        settingOption.getData().getOptions().getCurrency_symbol() + "" + ApplicationData.getStringValue(tax_sum + ship_tax) + " " + label + ")");

                                TotalCount(total_amount, coupon_cost, s, coupon_type);
                                //tv_total.setText(options.getCurrency_symbol()+" "+total_amount);

                            }

                            rg_paymentMethod.removeAllViews();

                            paymentMethod();
                        }
                    });

                    shipmethod.setCost(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                    shipmethod.setId(paymentInfo.getData().getShipping_method().get(finalI).getId());
                    shipmethod.setMethod_id(paymentInfo.getData().getShipping_method().get(finalI).getMethod_id());
                    shipmethod.setMin_amount(paymentInfo.getData().getShipping_method().get(finalI).getMin_amount());
                    shipmethod.setRequires(paymentInfo.getData().getShipping_method().get(finalI).getRequires());
                    shipmethod.setTax_status(paymentInfo.getData().getShipping_method().get(finalI).getTax_status());
                    shipmethod.setTitle(paymentInfo.getData().getShipping_method().get(finalI).getTitle());

                    firstShippinginfo.add(shipmethod);

                    rg_shipping.addView(radioButton[i]);

                }
            } else if (requires.equals("both")) {

                AppDebugLog.println("shipping name =" + requires);
                if (v >= min_cost && enable_shipping.equals("true")) {
                    final int finalI = i;
                    radioButton[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
//                            TAXRATE = paymentInfo.getData().getTaxes().getRate(); //change
                            TAXRATE = paymentInfo.getData().getTaxes().get(0).getRate();
                            shippingMethod = paymentInfo.getData().getShipping_method().get(finalI).getTitle();
                            shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                            method_id = paymentInfo.getData().getShipping_method().get(finalI).getMethod_id();


                            if ((paymentInfo.getData().getShipping_method().get(finalI).getCost() == null) ||
                                    (paymentInfo.getData().getShipping_method().get(finalI).getCost().equals(""))) {
                                tv_exCharge.setText(options.getCurrency_symbol() + " " + 0.0);
                                ship_amount = 0;
                                ori_ship_amount = 0;
                                //total_amount = sum;

                                tv_taxshipIncl.setText("");

                                tv_taxincl.setText(" (" + settingOption.getData().getOptions().getTax_display_cart() + " " +
                                        settingOption.getData().getOptions().getCurrency_symbol() + "" + ApplicationData.getStringValue(tax_sum + ship_tax) + " " + label + ")");

                                if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {
                                    ship_tax = 0;
                                    if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                        exludetax(tax_sum, ship_tax);

                                    } else {
                                        includtax(tax_sum, ship_tax);
                                    }

                                } else {
                                    tv_taxincl.setVisibility(View.GONE);
                                    rl_tax.setVisibility(View.GONE);
                                    tax_sum = 0;
                                    ship_tax = 0;

                                }
                                TotalCount(total_amount, coupon_cost, ship_amount, coupon_type);

                                //tv_total.setText(options.getCurrency_symbol()+" "+sum);
                            } else {
                                // double ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                double s = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                //shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                                ori_ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {

                                    if (paymentInfo.getData().getShipping_method().get(finalI).getTax_status().equals("taxable")) {
                                        ship_tax = s * (Double.parseDouble(TAXRATE) / 100);

                                        if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                            tv_taxshipIncl.setText("");

                                            exludetax(tax_sum, ship_tax);

                                        } else {

                                            s = s + ship_tax;
                                            tv_taxshipIncl.setText(ship_tx_label);
                                            includtax(tax_sum, ship_tax);

                                        }

                                    } else {
                                        tv_taxshipIncl.setText("");
                                        ship_tax = 0;

                                        if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                            exludetax(tax_sum, ship_tax);
                                        } else {
                                            includtax(tax_sum, ship_tax);
                                        }

                                    }

                                } else {
                                    tv_taxincl.setVisibility(View.GONE);
                                    tv_taxshipIncl.setText("");
                                    ship_tax = 0;
                                    tax_sum = 0;

                                }

                                tv_exCharge.setText(options.getCurrency_symbol() + " " + s);
                                ship_amount = s;
                                //total_amount = sub+s;
                                tv_taxincl.setText(" (" + settingOption.getData().getOptions().getTax_display_cart() + " " +
                                        settingOption.getData().getOptions().getCurrency_symbol() + "" + ApplicationData.getStringValue(tax_sum + ship_tax) + " " + label + ")");

                                TotalCount(total_amount, coupon_cost, s, coupon_type);
                                //tv_total.setText(options.getCurrency_symbol()+" "+total_amount);

                            }

                            rg_paymentMethod.removeAllViews();

                            paymentMethod();
                        }
                    });

                    shipmethod.setCost(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                    shipmethod.setId(paymentInfo.getData().getShipping_method().get(finalI).getId());
                    shipmethod.setMethod_id(paymentInfo.getData().getShipping_method().get(finalI).getMethod_id());
                    shipmethod.setMin_amount(paymentInfo.getData().getShipping_method().get(finalI).getMin_amount());
                    shipmethod.setRequires(paymentInfo.getData().getShipping_method().get(finalI).getRequires());
                    shipmethod.setTax_status(paymentInfo.getData().getShipping_method().get(finalI).getTax_status());
                    shipmethod.setTitle(paymentInfo.getData().getShipping_method().get(finalI).getTitle());

                    firstShippinginfo.add(shipmethod);

                    rg_shipping.addView(radioButton[i]);

                } else {

                }
            } else if (requires.equals("either")) {

                //AppDebugLog.println("shipping name ="+ requires);
                if ((v >= min_cost || enable_shipping.equals("true"))) {
                    final int finalI = i;
                    radioButton[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
//                            TAXRATE = paymentInfo.getData().getTaxes().getRate(); //change
                            TAXRATE = paymentInfo.getData().getTaxes().get(0).getRate();
                            shippingMethod = paymentInfo.getData().getShipping_method().get(finalI).getTitle();
                            shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                            method_id = paymentInfo.getData().getShipping_method().get(finalI).getMethod_id();


                            if ((paymentInfo.getData().getShipping_method().get(finalI).getCost() == null) ||
                                    (paymentInfo.getData().getShipping_method().get(finalI).getCost().equals(""))) {
                                tv_exCharge.setText(options.getCurrency_symbol() + " " + 0.0);
                                ship_amount = 0;
                                ori_ship_amount = 0;
                                //total_amount = sum;

                                tv_taxshipIncl.setText("");

                                tv_taxincl.setText(" (" + settingOption.getData().getOptions().getTax_display_cart() + " " +
                                        settingOption.getData().getOptions().getCurrency_symbol() + "" + ApplicationData.getStringValue(tax_sum + ship_tax) + " " + label + ")");

                                if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {
                                    ship_tax = 0;
                                    if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                        exludetax(tax_sum, ship_tax);

                                    } else {
                                        includtax(tax_sum, ship_tax);
                                    }

                                } else {
                                    tv_taxincl.setVisibility(View.GONE);
                                    rl_tax.setVisibility(View.GONE);
                                    tax_sum = 0;
                                    ship_tax = 0;

                                }
                                TotalCount(total_amount, coupon_cost, ship_amount, coupon_type);

                                //tv_total.setText(options.getCurrency_symbol()+" "+sum);
                            } else {
                                // double ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                double s = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                //shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                                ori_ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {

                                    if (paymentInfo.getData().getShipping_method().get(finalI).getTax_status().equals("taxable")) {
                                        ship_tax = s * (Double.parseDouble(TAXRATE) / 100);

                                        if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                            tv_taxshipIncl.setText("");

                                            exludetax(tax_sum, ship_tax);

                                        } else {

                                            s = s + ship_tax;
                                            tv_taxshipIncl.setText(ship_tx_label);
                                            includtax(tax_sum, ship_tax);

                                        }

                                    } else {
                                        tv_taxshipIncl.setText("");
                                        ship_tax = 0;

                                        if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                            exludetax(tax_sum, ship_tax);
                                        } else {
                                            includtax(tax_sum, ship_tax);
                                        }

                                    }

                                } else {
                                    tv_taxincl.setVisibility(View.GONE);
                                    tv_taxshipIncl.setText("");
                                    ship_tax = 0;
                                    tax_sum = 0;

                                }

                                tv_exCharge.setText(options.getCurrency_symbol() + " " + s);
                                ship_amount = s;
                                //total_amount = sub+s;
                                tv_taxincl.setText(" (" + settingOption.getData().getOptions().getTax_display_cart() + " " +
                                        settingOption.getData().getOptions().getCurrency_symbol() + "" + ApplicationData.getStringValue(tax_sum + ship_tax) + " " + label + ")");

                                TotalCount(total_amount, coupon_cost, s, coupon_type);
                                //tv_total.setText(options.getCurrency_symbol()+" "+total_amount);

                            }

                            rg_paymentMethod.removeAllViews();

                            paymentMethod();
                        }
                    });
                    shipmethod.setCost(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                    shipmethod.setId(paymentInfo.getData().getShipping_method().get(finalI).getId());
                    shipmethod.setMethod_id(paymentInfo.getData().getShipping_method().get(finalI).getMethod_id());
                    shipmethod.setMin_amount(paymentInfo.getData().getShipping_method().get(finalI).getMin_amount());
                    shipmethod.setRequires(paymentInfo.getData().getShipping_method().get(finalI).getRequires());
                    shipmethod.setTax_status(paymentInfo.getData().getShipping_method().get(finalI).getTax_status());
                    shipmethod.setTitle(paymentInfo.getData().getShipping_method().get(finalI).getTitle());

                    firstShippinginfo.add(shipmethod);
                    rg_shipping.addView(radioButton[i]);
                }
            } else if (requires.equals("coupon")) {


                // AppDebugLog.println("shipping name ="+ requires);
                if (enable_shipping.equals("true") && requires.equals("coupon")) {

                    final int finalI = i;
                    radioButton[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
//                            TAXRATE = paymentInfo.getData().getTaxes().getRate(); //change
                            TAXRATE = paymentInfo.getData().getTaxes().get(0).getRate();
                            shippingMethod = paymentInfo.getData().getShipping_method().get(finalI).getTitle();
                            shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                            method_id = paymentInfo.getData().getShipping_method().get(finalI).getMethod_id();

                            if ((paymentInfo.getData().getShipping_method().get(finalI).getCost() == null) ||
                                    (paymentInfo.getData().getShipping_method().get(finalI).getCost().equals(""))) {
                                tv_exCharge.setText(options.getCurrency_symbol() + " " + 0.0);
                                ship_amount = 0;
                                ori_ship_amount = 0;
                                //total_amount = sum;

                                tv_taxshipIncl.setText("");

                                tv_taxincl.setText(" (" + settingOption.getData().getOptions().getTax_display_cart() + " " +
                                        settingOption.getData().getOptions().getCurrency_symbol() + "" + ApplicationData.getStringValue(tax_sum + ship_tax) + " " + label + ")");

                                if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {
                                    ship_tax = 0;
                                    if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                        exludetax(tax_sum, ship_tax);

                                    } else {
                                        includtax(tax_sum, ship_tax);
                                    }

                                } else {
                                    tv_taxincl.setVisibility(View.GONE);
                                    rl_tax.setVisibility(View.GONE);
                                    tax_sum = 0;
                                    ship_tax = 0;

                                }
                                TotalCount(total_amount, coupon_cost, ship_amount, coupon_type);

                                //tv_total.setText(options.getCurrency_symbol()+" "+sum);
                            } else {
                                // double ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                double s = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                //shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                                ori_ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {

                                    if (paymentInfo.getData().getShipping_method().get(finalI).getTax_status().equals("taxable")) {
                                        ship_tax = s * (Double.parseDouble(TAXRATE) / 100);

                                        if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                            tv_taxshipIncl.setText("");

                                            exludetax(tax_sum, ship_tax);

                                        } else {

                                            s = s + ship_tax;
                                            tv_taxshipIncl.setText(ship_tx_label);
                                            includtax(tax_sum, ship_tax);

                                        }

                                    } else {
                                        tv_taxshipIncl.setText("");
                                        ship_tax = 0;

                                        if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                            exludetax(tax_sum, ship_tax);
                                        } else {
                                            includtax(tax_sum, ship_tax);
                                        }

                                    }

                                } else {
                                    tv_taxincl.setVisibility(View.GONE);
                                    tv_taxshipIncl.setText("");
                                    ship_tax = 0;
                                    tax_sum = 0;

                                }

                                tv_exCharge.setText(options.getCurrency_symbol() + " " + s);
                                ship_amount = s;
                                //total_amount = sub+s;
                                tv_taxincl.setText(" (" + settingOption.getData().getOptions().getTax_display_cart() + " " +
                                        settingOption.getData().getOptions().getCurrency_symbol() + "" + ApplicationData.getStringValue(tax_sum + ship_tax) + " " + label + ")");

                                TotalCount(total_amount, coupon_cost, s, coupon_type);
                                //tv_total.setText(options.getCurrency_symbol()+" "+total_amount);

                            }

                            rg_paymentMethod.removeAllViews();

                            paymentMethod();
                        }
                    });

                    shipmethod.setCost(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                    shipmethod.setId(paymentInfo.getData().getShipping_method().get(finalI).getId());
                    shipmethod.setMethod_id(paymentInfo.getData().getShipping_method().get(finalI).getMethod_id());
                    shipmethod.setMin_amount(paymentInfo.getData().getShipping_method().get(finalI).getMin_amount());
                    shipmethod.setRequires(paymentInfo.getData().getShipping_method().get(finalI).getRequires());
                    shipmethod.setTax_status(paymentInfo.getData().getShipping_method().get(finalI).getTax_status());
                    shipmethod.setTitle(paymentInfo.getData().getShipping_method().get(finalI).getTitle());

                    firstShippinginfo.add(shipmethod);

                    rg_shipping.addView(radioButton[i]);

                } else {

                }


            } else {


                //AppDebugLog.println("shipping requires not here ="+ requires);
                final int finalI = i;
                radioButton[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        TAXRATE = paymentInfo.getData().getTaxes().getRate(); // change
                        TAXRATE = paymentInfo.getData().getTaxes().get(0).getRate();

                        shippingMethod = paymentInfo.getData().getShipping_method().get(finalI).getTitle();
                        shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                        method_id = paymentInfo.getData().getShipping_method().get(finalI).getMethod_id();

                        if ((paymentInfo.getData().getShipping_method().get(finalI).getCost() == null) ||
                                (paymentInfo.getData().getShipping_method().get(finalI).getCost().equals(""))) {
                            tv_exCharge.setText(options.getCurrency_symbol() + " " + 0.0);
                            ship_amount = 0;
                            ori_ship_amount = 0;
                            //total_amount = sum;

                            tv_taxshipIncl.setText("");

                            tv_taxincl.setText(" (" + settingOption.getData().getOptions().getTax_display_cart() + " " +
                                    settingOption.getData().getOptions().getCurrency_symbol() + "" + ApplicationData.getStringValue(tax_sum + ship_tax) + " " + label + ")");

                            if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {
                                ship_tax = 0;
                                if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                    exludetax(tax_sum, ship_tax);

                                } else {
                                    includtax(tax_sum, ship_tax);
                                }

                            } else {
                                tv_taxincl.setVisibility(View.GONE);
                                rl_tax.setVisibility(View.GONE);
                                tax_sum = 0;
                                ship_tax = 0;

                            }

                            TotalCount(total_amount, coupon_cost, ship_amount, coupon_type);

                            //tv_total.setText(options.getCurrency_symbol()+" "+sum);
                        } else {
                            // double ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                            double s = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                            //shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                            ori_ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                            if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {

                                if (paymentInfo.getData().getShipping_method().get(finalI).getTax_status().equals("taxable")) {
                                    // AppDebugLog.println("ship_tax,   TAXRATE== is== "+ship_tax+"-----------"+TAXRATE);
                                    ship_tax = s * (Double.parseDouble(TAXRATE) / 100);

                                    if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                        tv_taxshipIncl.setText("");

                                        exludetax(tax_sum, ship_tax);

                                    } else {

                                        s = s + ship_tax;
                                        tv_taxshipIncl.setText(ship_tx_label);
                                        includtax(tax_sum, ship_tax);

                                    }

                                } else {
                                    tv_taxshipIncl.setText("");
                                    ship_tax = 0;

                                    if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                        exludetax(tax_sum, ship_tax);
                                    } else {
                                        includtax(tax_sum, ship_tax);
                                    }

                                }

                            } else {
                                tv_taxincl.setVisibility(View.GONE);
                                tv_taxshipIncl.setText("");
                                ship_tax = 0;
                                tax_sum = 0;

                            }

                            tv_exCharge.setText(options.getCurrency_symbol() + " " + s);
                            ship_amount = s;
                            //total_amount = sub+s;
                            tv_taxincl.setText(" (" + settingOption.getData().getOptions().getTax_display_cart() + " " +
                                    settingOption.getData().getOptions().getCurrency_symbol() + "" + ApplicationData.getStringValue(tax_sum + ship_tax) + " " + label + ")");
                            TotalCount(total_amount, coupon_cost, s, coupon_type);
                            //tv_total.setText(options.getCurrency_symbol()+" "+total_amount);

                        }

                        rg_paymentMethod.removeAllViews();

                        paymentMethod();
                    }
                });

                shipmethod.setCost(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                shipmethod.setId(paymentInfo.getData().getShipping_method().get(finalI).getId());
                shipmethod.setMethod_id(paymentInfo.getData().getShipping_method().get(finalI).getMethod_id());
                shipmethod.setMin_amount(paymentInfo.getData().getShipping_method().get(finalI).getMin_amount());
                shipmethod.setRequires(paymentInfo.getData().getShipping_method().get(finalI).getRequires());
                shipmethod.setTax_status(paymentInfo.getData().getShipping_method().get(finalI).getTax_status());
                shipmethod.setTitle(paymentInfo.getData().getShipping_method().get(finalI).getTitle());

                firstShippinginfo.add(shipmethod);
                rg_shipping.addView(radioButton[i]);

            }


        }

        if (paymentInfo.getData().getShipping_method().size() == 0) {

        } else {
            //AppDebugLog.println("shipping requires repeat =");
            //View view = null;
            //radioButton[count].setChecked(true);
            View view = rg_shipping.getChildAt(0);
            RadioButton r = (RadioButton) view;
            r.setChecked(true);

            shippingMethod = firstShippinginfo.get(0).getTitle();

            shipping_id = firstShippinginfo.get(0).getId();

            method_id = firstShippinginfo.get(0).getMethod_id();


            if ((firstShippinginfo.get(0).getCost() == null) ||
                    (firstShippinginfo.get(0).getCost().equals(""))) {

                tv_exCharge.setText(options.getCurrency_symbol() + " " + 0.0);
                ship_amount = 0;
                ori_ship_amount = 0;
                //total_amount = sum;

                tv_taxshipIncl.setText("");

                tv_taxincl.setText(" (" + settingOption.getData().getOptions().getTax_display_cart() + " " +
                        settingOption.getData().getOptions().getCurrency_symbol() + "" + ApplicationData.getStringValue(tax_sum + ship_tax) + " " + label + ")");

                if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {
                    ship_tax = 0;
                    if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                        exludetax(tax_sum, ship_tax);

                    } else {
                        includtax(tax_sum, ship_tax);
                    }

                } else {
                    rl_tax.setVisibility(View.GONE);
                    tax_sum = 0;
                    ship_tax = 0;

                }
                TotalCount(total_amount, coupon_cost, ship_amount, coupon_type);


            } else {

                double s = Double.parseDouble(firstShippinginfo.get(0).getCost());
                ori_ship_amount = Double.parseDouble(firstShippinginfo.get(0).getCost());
                if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {
                    if (firstShippinginfo.get(0).getTax_status().equals("taxable")) {
                        ship_tax = s * (Double.parseDouble(TAXRATE) / 100);

                        if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                            tv_taxshipIncl.setText("");

                            exludetax(tax_sum, ship_tax);

                        } else {
                            s = s + ship_tax;
                            tv_taxshipIncl.setText(ship_tx_label);
                            includtax(tax_sum, ship_tax);
                        }
                    } else {
                        tv_taxshipIncl.setText("");
                        ship_tax = 0;


                        if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {

                            AppDebugLog.println("tax_sum= " + tax_sum + " ,ship_tax=== " + ship_tax);
                            exludetax(tax_sum, ship_tax);
                        } else {
                            includtax(tax_sum, ship_tax);
                        }
                    }

                } else {
                    tv_taxshipIncl.setText("");
                    ship_tax = 0;
                    tax_sum = 0;

                }

                tv_exCharge.setText(options.getCurrency_symbol() + " " + s);

                ship_amount = s;
                //total_amount = sub+s;
                //tv_total.setText(options.getCurrency_symbol()+" "+total_amount);
                tv_taxincl.setText(" (" + settingOption.getData().getOptions().getTax_display_cart() + " " +
                        settingOption.getData().getOptions().getCurrency_symbol() + "" + ApplicationData.getStringValue(tax_sum + ship_tax) + " " + label + ")");

                TotalCount(total_amount, coupon_cost, s, coupon_type);
            }
            rg_paymentMethod.removeAllViews();

            paymentMethod();
        }


///////////-----------------------------------------------

/*        if (requires.equals("")) {
            final RadioButton[] radioButton = new RadioButton[paymentInfo.getData().getShipping_method().size()];
            TextView cost;
            for (int i = 0; i < paymentInfo.getData().getShipping_method().size(); i++) {
                radioButton[i] = new RadioButton(activity);
                radioButton[i].setText(paymentInfo.getData().getShipping_method().get(i).getTitle());

                final int finalI = i;
                radioButton[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        shippingMethod = paymentInfo.getData().getShipping_method().get(finalI).getTitle();
                        shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                        method_id = paymentInfo.getData().getShipping_method().get(finalI).getMethod_id();
                        Log.d("shipping", "shipping name " + paymentInfo.getData().getShipping_method().get(finalI).getTitle());
                        if ((paymentInfo.getData().getShipping_method().get(finalI).getCost() == null)||
                                (paymentInfo.getData().getShipping_method().get(finalI).getCost().equals(""))) {
                            tv_exCharge.setText(options.getCurrency_symbol() + " " + 0.0);
                            ship_amount = 0;
                            ori_ship_amount = 0;
                            //total_amount = sum;

                            tv_taxshipIncl.setText("");

                            tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                                    settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");

                            if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {
                                ship_tax = 0;
                                if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                    exludetax(tax_sum, ship_tax);

                                } else {
                                    includtax(tax_sum, ship_tax);
                                }

                            } else {
                                rl_tax.setVisibility(View.GONE);
                                tax_sum = 0;
                                ship_tax = 0;

                            }
                            TotalCount(total_amount, coupon_cost, ship_amount, coupon_type);

                            //tv_total.setText(options.getCurrency_symbol()+" "+sum);
                        } else {
                            // double ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                            double s = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                            //shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                            ori_ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                            if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {

                                if (paymentInfo.getData().getShipping_method().get(finalI).getTax_status().equals("taxable")) {
                                    ship_tax = s * (Double.parseDouble(TAXRATE) / 100);

                                    if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                        tv_taxshipIncl.setText("");

                                        exludetax(tax_sum, ship_tax);

                                    } else {

                                        s = s + ship_tax;
                                        tv_taxshipIncl.setText("(" + settingOption.getData().getOptions().getTax_display_cart() + ". tax)");
                                        includtax(tax_sum, ship_tax);

                                    }

                                } else {
                                    tv_taxshipIncl.setText("");
                                    ship_tax = 0;

                                    if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                        exludetax(tax_sum, ship_tax);
                                    } else {
                                        includtax(tax_sum, ship_tax);
                                    }

                                }

                            } else {
                                tv_taxshipIncl.setText("");
                                ship_tax = 0;
                                tax_sum = 0;

                            }

                            tv_exCharge.setText(options.getCurrency_symbol() + " " + s);
                            ship_amount = s;
                            //total_amount = sub+s;
                            tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                                    settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");

                            TotalCount(total_amount, coupon_cost, s, coupon_type);
                            //tv_total.setText(options.getCurrency_symbol()+" "+total_amount);

                        }

                        rg_paymentMethod.removeAllViews();

                        paymentMethod();
                    }
                });

                rg_shipping.addView(radioButton[i]);

                radioButton[0].setChecked(true);
                shippingMethod = paymentInfo.getData().getShipping_method().get(0).getTitle();
                shipping_id = paymentInfo.getData().getShipping_method().get(0).getId();
                method_id = paymentInfo.getData().getShipping_method().get(0).getMethod_id();
                double s = Double.parseDouble(paymentInfo.getData().getShipping_method().get(0).getCost());
                ori_ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(0).getCost());

                if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {
                    if (paymentInfo.getData().getShipping_method().get(0).getTax_status().equals("taxable")) {
                        ship_tax = s * (Double.parseDouble(TAXRATE) / 100);

                        if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                            tv_taxshipIncl.setText("");

                            exludetax(tax_sum, ship_tax);

                        } else {
                            s = s + ship_tax;
                            tv_taxshipIncl.setText("(" + settingOption.getData().getOptions().getTax_display_cart() + ". tax)");
                            includtax(tax_sum, ship_tax);
                        }
                    } else {
                        tv_taxshipIncl.setText("");
                        ship_tax = 0;


                        if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                            Log.e("TAX", "tax_sum= " + tax_sum + " ,ship_tax=== " + ship_tax);
                            exludetax(tax_sum, ship_tax);
                        } else {
                            includtax(tax_sum, ship_tax);
                        }
                    }

                } else {
                    tv_taxshipIncl.setText("");
                    ship_tax = 0;
                    tax_sum = 0;

                }


                tv_exCharge.setText(options.getCurrency_symbol() + " " + s);

                ship_amount = s;
                //total_amount = sub+s;
                //tv_total.setText(options.getCurrency_symbol()+" "+total_amount);
                tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                        settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");
                TotalCount(total_amount, coupon_cost, s, coupon_type);
            }

            rg_paymentMethod.removeAllViews();
            paymentMethod();

        } else if (requires.equals("min_amount")) {

            final RadioButton[] radioButton = new RadioButton[paymentInfo.getData().getShipping_method().size()];
            TextView cost;
            for (int i = 0; i < paymentInfo.getData().getShipping_method().size(); i++) {
                // double testtotal = 50;
                String methodID = paymentInfo.getData().getShipping_method().get(i).getMethod_id();

                if (v < min_cost && requires.equals("min_amount") && methodID.equals("free_shipping_9")) {

                    Log.e("TEST", "free_shipping------------");
                } else {
                    radioButton[i] = new RadioButton(activity);
                    radioButton[i].setText(paymentInfo.getData().getShipping_method().get(i).getTitle());

                    final int finalI = i;
                    radioButton[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            shippingMethod = paymentInfo.getData().getShipping_method().get(finalI).getTitle();
                            shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                            method_id = paymentInfo.getData().getShipping_method().get(finalI).getMethod_id();
                            Log.d("shipping", "shipping name " + paymentInfo.getData().getShipping_method().get(finalI).getTitle());
                            if ((paymentInfo.getData().getShipping_method().get(finalI).getCost() == null)||
                                    (paymentInfo.getData().getShipping_method().get(finalI).getCost().equals(""))) {
                                tv_exCharge.setText(options.getCurrency_symbol() + " " + 0.0);
                                ship_amount = 0;
                                ori_ship_amount = 0;
                                //total_amount = sum;

                                tv_taxshipIncl.setText("");

                            tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                                    settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");

                                if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {
                                    ship_tax = 0;
                                    if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                        exludetax(tax_sum, ship_tax);

                                    } else {
                                        includtax(tax_sum, ship_tax);
                                    }

                                } else {
                                    rl_tax.setVisibility(View.GONE);
                                    tax_sum = 0;
                                    ship_tax = 0;

                                }
                                TotalCount(total_amount, coupon_cost, ship_amount, coupon_type);

                                //tv_total.setText(options.getCurrency_symbol()+" "+sum);
                            } else {
                                // double ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                double s = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                //shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                                ori_ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {

                                    if (paymentInfo.getData().getShipping_method().get(finalI).getTax_status().equals("taxable")) {
                                        ship_tax = s * (Double.parseDouble(TAXRATE) / 100);

                                        if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                            tv_taxshipIncl.setText("");

                                            exludetax(tax_sum, ship_tax);

                                        } else {

                                            s = s + ship_tax;
                                            tv_taxshipIncl.setText("(" + settingOption.getData().getOptions().getTax_display_cart() + ". tax)");
                                            includtax(tax_sum, ship_tax);

                                        }

                                    } else {
                                        tv_taxshipIncl.setText("");
                                        ship_tax = 0;

                                        if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                            exludetax(tax_sum, ship_tax);
                                        } else {
                                            includtax(tax_sum, ship_tax);
                                        }

                                    }

                                } else {
                                    tv_taxshipIncl.setText("");
                                    ship_tax = 0;
                                    tax_sum = 0;

                                }

                                tv_exCharge.setText(options.getCurrency_symbol() + " " + s);
                                ship_amount = s;
                                //total_amount = sub+s;
                            tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                                    settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");

                                TotalCount(total_amount, coupon_cost, s, coupon_type);
                                //tv_total.setText(options.getCurrency_symbol()+" "+total_amount);

                            }

                            rg_paymentMethod.removeAllViews();

                            paymentMethod();
                        }
                    });

                    rg_shipping.addView(radioButton[i]);

                    radioButton[0].setChecked(true);
                    shippingMethod = paymentInfo.getData().getShipping_method().get(0).getTitle();
                    shipping_id = paymentInfo.getData().getShipping_method().get(0).getId();
                    method_id = paymentInfo.getData().getShipping_method().get(0).getMethod_id();
                    double s = Double.parseDouble(paymentInfo.getData().getShipping_method().get(0).getCost());
                    ori_ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(0).getCost());

                    if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {
                        if (paymentInfo.getData().getShipping_method().get(0).getTax_status().equals("taxable")) {
                            ship_tax = s * (Double.parseDouble(TAXRATE) / 100);

                            if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                tv_taxshipIncl.setText("");

                                exludetax(tax_sum, ship_tax);

                            } else {
                                s = s + ship_tax;
                                tv_taxshipIncl.setText("(" + settingOption.getData().getOptions().getTax_display_cart() + ". tax)");
                                includtax(tax_sum, ship_tax);
                            }
                        } else {
                            tv_taxshipIncl.setText("");
                            ship_tax = 0;


                            if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                Log.e("TAX", "tax_sum= " + tax_sum + " ,ship_tax=== " + ship_tax);
                                exludetax(tax_sum, ship_tax);
                            } else {
                                includtax(tax_sum, ship_tax);
                            }
                        }

                    } else {
                        tv_taxshipIncl.setText("");
                        ship_tax = 0;
                        tax_sum = 0;

                    }


                    tv_exCharge.setText(options.getCurrency_symbol() + " " + s);

                    ship_amount = s;
                    //total_amount = sub+s;
                    //tv_total.setText(options.getCurrency_symbol()+" "+total_amount);
                tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                        settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");

                    TotalCount(total_amount, coupon_cost, s, coupon_type);
                }


            }
        } else*/ /*if (requires.equals("both")) {

            final RadioButton[] radioButton = new RadioButton[paymentInfo.getData().getShipping_method().size()];
            TextView cost;
            for (int i = 0; i < paymentInfo.getData().getShipping_method().size(); i++) {

                if (v >= min_cost && enable_shipping.equals("true") && requires.equals("both")) {

                    Log.e("TEST", "free_shipping---- v>=min_cost && enable_shipping.equals(\"true\")");

                    radioButton[i] = new RadioButton(activity);
                    radioButton[i].setText(paymentInfo.getData().getShipping_method().get(i).getTitle());

                    final int finalI = i;
                    radioButton[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            shippingMethod = paymentInfo.getData().getShipping_method().get(finalI).getTitle();
                            shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                            method_id = paymentInfo.getData().getShipping_method().get(finalI).getMethod_id();
                            Log.d("shipping", "shipping name " + paymentInfo.getData().getShipping_method().get(finalI).getTitle());
                            if ((paymentInfo.getData().getShipping_method().get(finalI).getCost() == null)||
                                    (paymentInfo.getData().getShipping_method().get(finalI).getCost().equals(""))) {
                                tv_exCharge.setText(options.getCurrency_symbol() + " " + 0.0);
                                ship_amount = 0;
                                ori_ship_amount = 0;
                                //total_amount = sum;

                                tv_taxshipIncl.setText("");

                            tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                                    settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");

                                if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {
                                    ship_tax = 0;
                                    if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                        exludetax(tax_sum, ship_tax);

                                    } else {
                                        includtax(tax_sum, ship_tax);
                                    }

                                } else {
                                    rl_tax.setVisibility(View.GONE);
                                    tax_sum = 0;
                                    ship_tax = 0;

                                }
                                TotalCount(total_amount, coupon_cost, ship_amount, coupon_type);

                                //tv_total.setText(options.getCurrency_symbol()+" "+sum);
                            } else {
                                // double ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                double s = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                //shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                                ori_ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {

                                    if (paymentInfo.getData().getShipping_method().get(finalI).getTax_status().equals("taxable")) {
                                        ship_tax = s * (Double.parseDouble(TAXRATE) / 100);

                                        if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                            tv_taxshipIncl.setText("");

                                            exludetax(tax_sum, ship_tax);

                                        } else {

                                            s = s + ship_tax;
                                            tv_taxshipIncl.setText("(" + settingOption.getData().getOptions().getTax_display_cart() + ". tax)");
                                            includtax(tax_sum, ship_tax);

                                        }

                                    } else {
                                        tv_taxshipIncl.setText("");
                                        ship_tax = 0;

                                        if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                            exludetax(tax_sum, ship_tax);
                                        } else {
                                            includtax(tax_sum, ship_tax);
                                        }

                                    }

                                } else {
                                    tv_taxshipIncl.setText("");
                                    ship_tax = 0;
                                    tax_sum = 0;

                                }

                                tv_exCharge.setText(options.getCurrency_symbol() + " " + s);
                                ship_amount = s;
                                //total_amount = sub+s;
                            tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                                    settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");

                                TotalCount(total_amount, coupon_cost, s, coupon_type);
                                //tv_total.setText(options.getCurrency_symbol()+" "+total_amount);

                            }

                            rg_paymentMethod.removeAllViews();

                            paymentMethod();
                        }
                    });

                    rg_shipping.addView(radioButton[i]);

                    radioButton[0].setChecked(true);
                    shippingMethod = paymentInfo.getData().getShipping_method().get(0).getTitle();
                    shipping_id = paymentInfo.getData().getShipping_method().get(0).getId();
                    method_id = paymentInfo.getData().getShipping_method().get(0).getMethod_id();
                    double s = Double.parseDouble(paymentInfo.getData().getShipping_method().get(0).getCost());
                    ori_ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(0).getCost());

                    if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {
                        if (paymentInfo.getData().getShipping_method().get(0).getTax_status().equals("taxable")) {
                            ship_tax = s * (Double.parseDouble(TAXRATE) / 100);

                            if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                tv_taxshipIncl.setText("");

                                exludetax(tax_sum, ship_tax);

                            } else {
                                s = s + ship_tax;
                                tv_taxshipIncl.setText("(" + settingOption.getData().getOptions().getTax_display_cart() + ". tax)");
                                includtax(tax_sum, ship_tax);
                            }
                        } else {
                            tv_taxshipIncl.setText("");
                            ship_tax = 0;


                            if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                Log.e("TAX", "tax_sum= " + tax_sum + " ,ship_tax=== " + ship_tax);
                                exludetax(tax_sum, ship_tax);
                            } else {
                                includtax(tax_sum, ship_tax);
                            }
                        }

                    } else {
                        tv_taxshipIncl.setText("");
                        ship_tax = 0;
                        tax_sum = 0;

                    }

                    tv_exCharge.setText(options.getCurrency_symbol() + " " + s);

                    ship_amount = s;
                    //total_amount = sub+s;
                    //tv_total.setText(options.getCurrency_symbol()+" "+total_amount);
                tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                        settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");

                    TotalCount(total_amount, coupon_cost, s, coupon_type);

                } else {

                    if (paymentInfo.getData().getShipping_method().get(i).getMethod_id().equals("free_shipping_9")) {

                    } else {
                        radioButton[i] = new RadioButton(activity);
                        radioButton[i].setText(paymentInfo.getData().getShipping_method().get(i).getTitle());

                        final int finalI = i;
                        radioButton[i].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                shippingMethod = paymentInfo.getData().getShipping_method().get(finalI).getTitle();
                                shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                                method_id = paymentInfo.getData().getShipping_method().get(finalI).getMethod_id();
                                Log.d("shipping", "shipping name " + paymentInfo.getData().getShipping_method().get(finalI).getTitle());
                                if ((paymentInfo.getData().getShipping_method().get(finalI).getCost() == null)||
                                        (paymentInfo.getData().getShipping_method().get(finalI).getCost().equals(""))) {
                                    tv_exCharge.setText(options.getCurrency_symbol() + " " + 0.0);
                                    ship_amount = 0;
                                    ori_ship_amount = 0;
                                    //total_amount = sum;

                                    tv_taxshipIncl.setText("");

                            tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                                    settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");

                                    if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {
                                        ship_tax = 0;
                                        if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                            exludetax(tax_sum, ship_tax);

                                        } else {
                                            includtax(tax_sum, ship_tax);
                                        }

                                    } else {
                                        rl_tax.setVisibility(View.GONE);
                                        tax_sum = 0;
                                        ship_tax = 0;

                                    }
                                    TotalCount(total_amount, coupon_cost, ship_amount, coupon_type);

                                    //tv_total.setText(options.getCurrency_symbol()+" "+sum);
                                } else {
                                    // double ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                    double s = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                    //shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                                    ori_ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                    if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {

                                        if (paymentInfo.getData().getShipping_method().get(finalI).getTax_status().equals("taxable")) {
                                            ship_tax = s * (Double.parseDouble(TAXRATE) / 100);

                                            if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                                tv_taxshipIncl.setText("");

                                                exludetax(tax_sum, ship_tax);

                                            } else {

                                                s = s + ship_tax;
                                                tv_taxshipIncl.setText("(" + settingOption.getData().getOptions().getTax_display_cart() + ". tax)");
                                                includtax(tax_sum, ship_tax);

                                            }

                                        } else {
                                            tv_taxshipIncl.setText("");
                                            ship_tax = 0;

                                            if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                                exludetax(tax_sum, ship_tax);
                                            } else {
                                                includtax(tax_sum, ship_tax);
                                            }

                                        }

                                    } else {
                                        tv_taxshipIncl.setText("");
                                        ship_tax = 0;
                                        tax_sum = 0;

                                    }

                                    tv_exCharge.setText(options.getCurrency_symbol() + " " + s);
                                    ship_amount = s;
                                    //total_amount = sub+s;
                            tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                                    settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");

                                    TotalCount(total_amount, coupon_cost, s, coupon_type);
                                    //tv_total.setText(options.getCurrency_symbol()+" "+total_amount);

                                }

                                rg_paymentMethod.removeAllViews();

                                paymentMethod();
                            }
                        });

                        rg_shipping.addView(radioButton[i]);

                        radioButton[0].setChecked(true);
                        shippingMethod = paymentInfo.getData().getShipping_method().get(0).getTitle();
                        shipping_id = paymentInfo.getData().getShipping_method().get(0).getId();
                        method_id = paymentInfo.getData().getShipping_method().get(0).getMethod_id();
                        double s = Double.parseDouble(paymentInfo.getData().getShipping_method().get(0).getCost());
                        ori_ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(0).getCost());

                        if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {
                            if (paymentInfo.getData().getShipping_method().get(0).getTax_status().equals("taxable")) {
                                ship_tax = s * (Double.parseDouble(TAXRATE) / 100);

                                if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                    tv_taxshipIncl.setText("");

                                    exludetax(tax_sum, ship_tax);

                                } else {
                                    s = s + ship_tax;
                                    tv_taxshipIncl.setText("(" + settingOption.getData().getOptions().getTax_display_cart() + ". tax)");
                                    includtax(tax_sum, ship_tax);
                                }
                            } else {
                                tv_taxshipIncl.setText("");
                                ship_tax = 0;


                                if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                    Log.e("TAX", "tax_sum= " + tax_sum + " ,ship_tax=== " + ship_tax);
                                    exludetax(tax_sum, ship_tax);
                                } else {
                                    includtax(tax_sum, ship_tax);
                                }
                            }

                        } else {
                            tv_taxshipIncl.setText("");
                            ship_tax = 0;
                            tax_sum = 0;

                        }

                        tv_exCharge.setText(options.getCurrency_symbol() + " " + s);

                        ship_amount = s;
                        //total_amount = sub+s;
                        //tv_total.setText(options.getCurrency_symbol()+" "+total_amount);
                tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                        settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");

                        TotalCount(total_amount, coupon_cost, s, coupon_type);
                    }


                }


            }

        } else */
       /* if (requires.equals("either")) {

          //  final RadioButton[] radioButton = new RadioButton[paymentInfo.getData().getShipping_method().size()];
            TextView cost;
           // Log.e("TEST", "either--- "+paymentInfo.getData().getShipping_method().size());

            for (int i = 0; i < paymentInfo.getData().getShipping_method().size(); i++) {
                Log.e("TEST", "either--- "+i);
               // String methodID = paymentInfo.getData().getShipping_method().get(i).getMethod_id();
                if ((v >= min_cost || enable_shipping.equals("true")) && requires.equals("either")) {

                    Log.e("TEST", "free_shipping---------v>=min_cost || enable_shipping.equals(\"true\")---");

                    radioButton[i] = new RadioButton(activity);
                    radioButton[i].setText(paymentInfo.getData().getShipping_method().get(i).getTitle());

                    final int finalI = i;
                    radioButton[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            shippingMethod = paymentInfo.getData().getShipping_method().get(finalI).getTitle();
                            shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                            method_id = paymentInfo.getData().getShipping_method().get(finalI).getMethod_id();
                            Log.d("shipping", "shipping name " + paymentInfo.getData().getShipping_method().get(finalI).getTitle());
                            if ((paymentInfo.getData().getShipping_method().get(finalI).getCost() == null)||
                                    (paymentInfo.getData().getShipping_method().get(finalI).getCost().equals(""))) {
                                tv_exCharge.setText(options.getCurrency_symbol() + " " + 0.0);
                                ship_amount = 0;
                                ori_ship_amount = 0;
                                //total_amount = sum;

                                tv_taxshipIncl.setText("");

                            tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                                    settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");

                                if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {
                                    ship_tax = 0;
                                    if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                        exludetax(tax_sum, ship_tax);

                                    } else {
                                        includtax(tax_sum, ship_tax);
                                    }

                                } else {
                                    rl_tax.setVisibility(View.GONE);
                                    tax_sum = 0;
                                    ship_tax = 0;

                                }
                                TotalCount(total_amount, coupon_cost, ship_amount, coupon_type);

                                //tv_total.setText(options.getCurrency_symbol()+" "+sum);
                            } else {
                                // double ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                double s = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                //shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                                ori_ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {

                                    if (paymentInfo.getData().getShipping_method().get(finalI).getTax_status().equals("taxable")) {
                                        ship_tax = s * (Double.parseDouble(TAXRATE) / 100);

                                        if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                            tv_taxshipIncl.setText("");

                                            exludetax(tax_sum, ship_tax);

                                        } else {

                                            s = s + ship_tax;
                                            tv_taxshipIncl.setText("(" + settingOption.getData().getOptions().getTax_display_cart() + ". tax)");
                                            includtax(tax_sum, ship_tax);

                                        }

                                    } else {
                                        tv_taxshipIncl.setText("");
                                        ship_tax = 0;

                                        if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                            exludetax(tax_sum, ship_tax);
                                        } else {
                                            includtax(tax_sum, ship_tax);
                                        }

                                    }

                                } else {
                                    tv_taxshipIncl.setText("");
                                    ship_tax = 0;
                                    tax_sum = 0;

                                }

                                tv_exCharge.setText(options.getCurrency_symbol() + " " + s);
                                ship_amount = s;
                                //total_amount = sub+s;
                            tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                                    settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");

                                TotalCount(total_amount, coupon_cost, s, coupon_type);
                                //tv_total.setText(options.getCurrency_symbol()+" "+total_amount);

                            }

                            rg_paymentMethod.removeAllViews();

                            paymentMethod();
                        }
                    });

                    rg_shipping.addView(radioButton[i]);

                    radioButton[0].setChecked(true);
                    shippingMethod = paymentInfo.getData().getShipping_method().get(0).getTitle();
                    shipping_id = paymentInfo.getData().getShipping_method().get(0).getId();
                    method_id = paymentInfo.getData().getShipping_method().get(0).getMethod_id();
                    double s = Double.parseDouble(paymentInfo.getData().getShipping_method().get(0).getCost());
                    ori_ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(0).getCost());

                    if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {
                        if (paymentInfo.getData().getShipping_method().get(0).getTax_status().equals("taxable")) {
                            ship_tax = s * (Double.parseDouble(TAXRATE) / 100);

                            if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                tv_taxshipIncl.setText("");

                                exludetax(tax_sum, ship_tax);

                            } else {
                                s = s + ship_tax;
                                tv_taxshipIncl.setText("(" + settingOption.getData().getOptions().getTax_display_cart() + ". tax)");
                                includtax(tax_sum, ship_tax);
                            }
                        } else {
                            tv_taxshipIncl.setText("");
                            ship_tax = 0;


                            if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                Log.e("TAX", "tax_sum= " + tax_sum + " ,ship_tax=== " + ship_tax);
                                exludetax(tax_sum, ship_tax);
                            } else {
                                includtax(tax_sum, ship_tax);
                            }
                        }

                    } else {
                        tv_taxshipIncl.setText("");
                        ship_tax = 0;
                        tax_sum = 0;

                    }

                    tv_exCharge.setText(options.getCurrency_symbol() + " " + s);

                    ship_amount = s;
                    //total_amount = sub+s;
                    //tv_total.setText(options.getCurrency_symbol()+" "+total_amount);
                tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                        settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");

                    TotalCount(total_amount, coupon_cost, s, coupon_type);

                } else {

                    if (paymentInfo.getData().getShipping_method().get(i).getMethod_id().equals("free_shipping_9")) {
                        Log.e("TEST", "free_shipping_9 not add ");
                    } else {
                        radioButton[i] = new RadioButton(activity);
                        radioButton[i].setText(paymentInfo.getData().getShipping_method().get(i).getTitle());

                        Log.e("TEST", "method add = "+paymentInfo.getData().getShipping_method().get(i).getTitle());
                        final int finalI = i;
                        radioButton[i].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                shippingMethod = paymentInfo.getData().getShipping_method().get(finalI).getTitle();
                                shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                                method_id = paymentInfo.getData().getShipping_method().get(finalI).getMethod_id();
                                Log.d("shipping", "shipping name " + paymentInfo.getData().getShipping_method().get(finalI).getTitle());
                                if ((paymentInfo.getData().getShipping_method().get(finalI).getCost() == null)||
                                        (paymentInfo.getData().getShipping_method().get(finalI).getCost().equals(""))) {
                                    tv_exCharge.setText(options.getCurrency_symbol() + " " + 0.0);
                                    ship_amount = 0;
                                    ori_ship_amount = 0;
                                    //total_amount = sum;

                                    tv_taxshipIncl.setText("");

                            tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                                    settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");

                                    if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {
                                        ship_tax = 0;
                                        if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                            exludetax(tax_sum, ship_tax);

                                        } else {
                                            includtax(tax_sum, ship_tax);
                                        }

                                    } else {
                                        rl_tax.setVisibility(View.GONE);
                                        tax_sum = 0;
                                        ship_tax = 0;

                                    }
                                    TotalCount(total_amount, coupon_cost, ship_amount, coupon_type);

                                    //tv_total.setText(options.getCurrency_symbol()+" "+sum);
                                } else {
                                    // double ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                    double s = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                    //shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                                    ori_ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                    if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {

                                        if (paymentInfo.getData().getShipping_method().get(finalI).getTax_status().equals("taxable")) {
                                            ship_tax = s * (Double.parseDouble(TAXRATE) / 100);

                                            if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                                tv_taxshipIncl.setText("");

                                                exludetax(tax_sum, ship_tax);

                                            } else {

                                                s = s + ship_tax;
                                                tv_taxshipIncl.setText("(" + settingOption.getData().getOptions().getTax_display_cart() + ". tax)");
                                                includtax(tax_sum, ship_tax);

                                            }

                                        } else {
                                            tv_taxshipIncl.setText("");
                                            ship_tax = 0;

                                            if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                                exludetax(tax_sum, ship_tax);
                                            } else {
                                                includtax(tax_sum, ship_tax);
                                            }

                                        }

                                    } else {
                                        tv_taxshipIncl.setText("");
                                        ship_tax = 0;
                                        tax_sum = 0;

                                    }

                                    tv_exCharge.setText(options.getCurrency_symbol() + " " + s);
                                    ship_amount = s;
                                    //total_amount = sub+s;
                            tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                                    settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");

                                    TotalCount(total_amount, coupon_cost, s, coupon_type);
                                    //tv_total.setText(options.getCurrency_symbol()+" "+total_amount);

                                }

                                rg_paymentMethod.removeAllViews();

                                paymentMethod();
                            }
                        });

                        rg_shipping.addView(radioButton[i]);

                        radioButton[0].setChecked(true);
                        shippingMethod = paymentInfo.getData().getShipping_method().get(0).getTitle();
                        shipping_id = paymentInfo.getData().getShipping_method().get(0).getId();
                        method_id = paymentInfo.getData().getShipping_method().get(0).getMethod_id();

                        Log.e("TEST", "tax_sum= " + tax_sum + " ,ship_tax=== " + ship_tax);
                        if ((paymentInfo.getData().getShipping_method().get(0).getCost() == null)||
                                (paymentInfo.getData().getShipping_method().get(0).getCost().equals(""))){

                            tv_exCharge.setText(options.getCurrency_symbol() + " " + 0.0);
                            ship_amount = 0;
                            ori_ship_amount = 0;
                            //total_amount = sum;

                            tv_taxshipIncl.setText("");

                            tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                                    settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");

                            if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {
                                ship_tax = 0;
                                if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                    exludetax(tax_sum, ship_tax);

                                } else {
                                    includtax(tax_sum, ship_tax);
                                }

                            } else {
                                rl_tax.setVisibility(View.GONE);
                                tax_sum = 0;
                                ship_tax = 0;

                            }
                            TotalCount(total_amount, coupon_cost, ship_amount, coupon_type);


                        }else {

                            double s = Double.parseDouble(paymentInfo.getData().getShipping_method().get(0).getCost());
                            ori_ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(0).getCost());
                            if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {
                                if (paymentInfo.getData().getShipping_method().get(0).getTax_status().equals("taxable")) {
                                    ship_tax = s * (Double.parseDouble(TAXRATE) / 100);

                                    if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                        tv_taxshipIncl.setText("");

                                        exludetax(tax_sum, ship_tax);

                                    } else {
                                        s = s + ship_tax;
                                        tv_taxshipIncl.setText("(" + settingOption.getData().getOptions().getTax_display_cart() + ". tax)");
                                        includtax(tax_sum, ship_tax);
                                    }
                                } else {
                                    tv_taxshipIncl.setText("");
                                    ship_tax = 0;


                                    if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                        Log.e("TAX", "tax_sum= " + tax_sum + " ,ship_tax=== " + ship_tax);
                                        exludetax(tax_sum, ship_tax);
                                    } else {
                                        includtax(tax_sum, ship_tax);
                                    }
                                }

                            } else {
                                tv_taxshipIncl.setText("");
                                ship_tax = 0;
                                tax_sum = 0;

                            }

                            tv_exCharge.setText(options.getCurrency_symbol() + " " + s);

                            ship_amount = s;
                            //total_amount = sub+s;
                            //tv_total.setText(options.getCurrency_symbol()+" "+total_amount);
                tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                        settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");

                            TotalCount(total_amount, coupon_cost, s, coupon_type);
                        }


                    }


                }


            }


        } else if (requires.equals("coupon")) {
          //  final RadioButton[] radioButton = new RadioButton[paymentInfo.getData().getShipping_method().size()];
            TextView cost;
            for (int i = 0; i < paymentInfo.getData().getShipping_method().size(); i++) {

                if (enable_shipping.equals("true") && requires.equals("coupon")) {

                    Log.e("TEST", "free_shipping--------enable_shipping.equals(\"true\")----");

                    radioButton[i] = new RadioButton(activity);
                    radioButton[i].setText(paymentInfo.getData().getShipping_method().get(i).getTitle());

                    final int finalI = i;
                    radioButton[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            shippingMethod = paymentInfo.getData().getShipping_method().get(finalI).getTitle();
                            shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                            method_id = paymentInfo.getData().getShipping_method().get(finalI).getMethod_id();
                            Log.d("shipping", "shipping name " + paymentInfo.getData().getShipping_method().get(finalI).getTitle());
                            if ((paymentInfo.getData().getShipping_method().get(finalI).getCost() == null)||
                                    (paymentInfo.getData().getShipping_method().get(finalI).getCost().equals(""))) {
                                tv_exCharge.setText(options.getCurrency_symbol() + " " + 0.0);
                                ship_amount = 0;
                                ori_ship_amount = 0;
                                //total_amount = sum;

                                tv_taxshipIncl.setText("");

                            tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                                    settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");

                                if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {
                                    ship_tax = 0;
                                    if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                        exludetax(tax_sum, ship_tax);

                                    } else {
                                        includtax(tax_sum, ship_tax);
                                    }

                                } else {
                                    rl_tax.setVisibility(View.GONE);
                                    tax_sum = 0;
                                    ship_tax = 0;

                                }
                                TotalCount(total_amount, coupon_cost, ship_amount, coupon_type);

                                //tv_total.setText(options.getCurrency_symbol()+" "+sum);
                            } else {
                                // double ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                double s = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                //shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                                ori_ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {

                                    if (paymentInfo.getData().getShipping_method().get(finalI).getTax_status().equals("taxable")) {
                                        ship_tax = s * (Double.parseDouble(TAXRATE) / 100);

                                        if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                            tv_taxshipIncl.setText("");

                                            exludetax(tax_sum, ship_tax);

                                        } else {

                                            s = s + ship_tax;
                                            tv_taxshipIncl.setText("(" + settingOption.getData().getOptions().getTax_display_cart() + ". tax)");
                                            includtax(tax_sum, ship_tax);

                                        }

                                    } else {
                                        tv_taxshipIncl.setText("");
                                        ship_tax = 0;

                                        if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                            exludetax(tax_sum, ship_tax);
                                        } else {
                                            includtax(tax_sum, ship_tax);
                                        }

                                    }

                                } else {
                                    tv_taxshipIncl.setText("");
                                    ship_tax = 0;
                                    tax_sum = 0;

                                }

                                tv_exCharge.setText(options.getCurrency_symbol() + " " + s);
                                ship_amount = s;
                                //total_amount = sub+s;
                            tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                                    settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");

                                TotalCount(total_amount, coupon_cost, s, coupon_type);
                                //tv_total.setText(options.getCurrency_symbol()+" "+total_amount);

                            }

                            rg_paymentMethod.removeAllViews();

                            paymentMethod();
                        }
                    });

                    rg_shipping.addView(radioButton[i]);

                    radioButton[0].setChecked(true);
                    shippingMethod = paymentInfo.getData().getShipping_method().get(0).getTitle();
                    shipping_id = paymentInfo.getData().getShipping_method().get(0).getId();
                    method_id = paymentInfo.getData().getShipping_method().get(0).getMethod_id();
                    double s = Double.parseDouble(paymentInfo.getData().getShipping_method().get(0).getCost());
                    ori_ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(0).getCost());

                    if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {
                        if (paymentInfo.getData().getShipping_method().get(0).getTax_status().equals("taxable")) {
                            ship_tax = s * (Double.parseDouble(TAXRATE) / 100);

                            if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                tv_taxshipIncl.setText("");

                                exludetax(tax_sum, ship_tax);

                            } else {
                                s = s + ship_tax;
                                tv_taxshipIncl.setText("(" + settingOption.getData().getOptions().getTax_display_cart() + ". tax)");
                                includtax(tax_sum, ship_tax);
                            }
                        } else {
                            tv_taxshipIncl.setText("");
                            ship_tax = 0;


                            if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                Log.e("TAX", "tax_sum= " + tax_sum + " ,ship_tax=== " + ship_tax);
                                exludetax(tax_sum, ship_tax);
                            } else {
                                includtax(tax_sum, ship_tax);
                            }
                        }

                    } else {
                        tv_taxshipIncl.setText("");
                        ship_tax = 0;
                        tax_sum = 0;

                    }

                    tv_exCharge.setText(options.getCurrency_symbol() + " " + s);

                    ship_amount = s;
                    //total_amount = sub+s;
                    //tv_total.setText(options.getCurrency_symbol()+" "+total_amount);
                tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                        settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");

                    TotalCount(total_amount, coupon_cost, s, coupon_type);

                } else {

                    if (paymentInfo.getData().getShipping_method().get(i).getMethod_id().equals("free_shipping_9")) {

                    } else {
                        radioButton[i] = new RadioButton(activity);
                        radioButton[i].setText(paymentInfo.getData().getShipping_method().get(i).getTitle());

                        final int finalI = i;
                        radioButton[i].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                shippingMethod = paymentInfo.getData().getShipping_method().get(finalI).getTitle();
                                shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                                method_id = paymentInfo.getData().getShipping_method().get(finalI).getMethod_id();
                                Log.d("shipping", "shipping name " + paymentInfo.getData().getShipping_method().get(finalI).getTitle());
                                if ((paymentInfo.getData().getShipping_method().get(finalI).getCost() == null)||
                                        (paymentInfo.getData().getShipping_method().get(finalI).getCost().equals(""))) {
                                    tv_exCharge.setText(options.getCurrency_symbol() + " " + 0.0);
                                    ship_amount = 0;
                                    ori_ship_amount = 0;
                                    //total_amount = sum;

                                    tv_taxshipIncl.setText("");

                            tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                                    settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");

                                    if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {
                                        ship_tax = 0;
                                        if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                            exludetax(tax_sum, ship_tax);

                                        } else {
                                            includtax(tax_sum, ship_tax);
                                        }

                                    } else {
                                        rl_tax.setVisibility(View.GONE);
                                        tax_sum = 0;
                                        ship_tax = 0;

                                    }
                                    TotalCount(total_amount, coupon_cost, ship_amount, coupon_type);

                                    //tv_total.setText(options.getCurrency_symbol()+" "+sum);
                                } else {
                                    // double ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                    double s = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                    //shipping_id = paymentInfo.getData().getShipping_method().get(finalI).getId();
                                    ori_ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(finalI).getCost());
                                    if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {

                                        if (paymentInfo.getData().getShipping_method().get(finalI).getTax_status().equals("taxable")) {
                                            ship_tax = s * (Double.parseDouble(TAXRATE) / 100);

                                            if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                                tv_taxshipIncl.setText("");

                                                exludetax(tax_sum, ship_tax);

                                            } else {

                                                s = s + ship_tax;
                                                tv_taxshipIncl.setText("(" + settingOption.getData().getOptions().getTax_display_cart() + ". tax)");
                                                includtax(tax_sum, ship_tax);

                                            }

                                        } else {
                                            tv_taxshipIncl.setText("");
                                            ship_tax = 0;

                                            if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                                exludetax(tax_sum, ship_tax);
                                            } else {
                                                includtax(tax_sum, ship_tax);
                                            }

                                        }

                                    } else {
                                        tv_taxshipIncl.setText("");
                                        ship_tax = 0;
                                        tax_sum = 0;

                                    }

                                    tv_exCharge.setText(options.getCurrency_symbol() + " " + s);
                                    ship_amount = s;
                                    //total_amount = sub+s;
                            tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                                    settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");

                                    TotalCount(total_amount, coupon_cost, s, coupon_type);
                                    //tv_total.setText(options.getCurrency_symbol()+" "+total_amount);

                                }

                                rg_paymentMethod.removeAllViews();

                                paymentMethod();
                            }
                        });

                        rg_shipping.addView(radioButton[i]);

                        radioButton[0].setChecked(true);
                        shippingMethod = paymentInfo.getData().getShipping_method().get(0).getTitle();
                        shipping_id = paymentInfo.getData().getShipping_method().get(0).getId();
                        method_id = paymentInfo.getData().getShipping_method().get(0).getMethod_id();
                        double s = Double.parseDouble(paymentInfo.getData().getShipping_method().get(0).getCost());
                        ori_ship_amount = Double.parseDouble(paymentInfo.getData().getShipping_method().get(0).getCost());

                        if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {
                            if (paymentInfo.getData().getShipping_method().get(0).getTax_status().equals("taxable")) {
                                ship_tax = s * (Double.parseDouble(TAXRATE) / 100);

                                if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                    tv_taxshipIncl.setText("");

                                    exludetax(tax_sum, ship_tax);

                                } else {
                                    s = s + ship_tax;
                                    tv_taxshipIncl.setText("(" + settingOption.getData().getOptions().getTax_display_cart() + ". tax)");
                                    includtax(tax_sum, ship_tax);
                                }
                            } else {
                                tv_taxshipIncl.setText("");
                                ship_tax = 0;


                                if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {
                                    Log.e("TAX", "tax_sum= " + tax_sum + " ,ship_tax=== " + ship_tax);
                                    exludetax(tax_sum, ship_tax);
                                } else {
                                    includtax(tax_sum, ship_tax);
                                }
                            }

                        } else {
                            tv_taxshipIncl.setText("");
                            ship_tax = 0;
                            tax_sum = 0;

                        }

                        tv_exCharge.setText(options.getCurrency_symbol() + " " + s);

                        ship_amount = s;
                        //total_amount = sub+s;
                        //tv_total.setText(options.getCurrency_symbol()+" "+total_amount);
                tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                        settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");

                        TotalCount(total_amount, coupon_cost, s, coupon_type);
                    }

                }

            }

        }*/

        //  rg_paymentMethod.removeAllViews();
        //  paymentMethod();


    }

    private void Taxdisplay() {


        if (settingOption.getData().getOptions().getEnable_taxes().equals("yes")) {

        /*    if (settingOption.getData().getOptions().getTax_based_on().equals("base")){

                tv_taxincl.setVisibility(View.GONE);
                tv_taxshipIncl.setText("");
                rl_tax.setVisibility(View.GONE);
                tax_sum=0;
                tax_b=0;
                ship_tax=0;

            }else {*/

            if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {


                float tax;
                tax = (float) ((ori_total) * (Double.parseDouble(TAXRATE) / 100));

                   /* float tax = (float) ((ori_total*Float.parseFloat(paymentInfo.getData().getTaxes().getRate()))/
                            (100+Float.parseFloat(paymentInfo.getData().getTaxes().getRate())));*/
                tax_sum = ApplicationData.getFloatValue(tax);
                tax_b = tax;

                rl_tax.setVisibility(View.VISIBLE);
                tv_taxshipIncl.setText("");
                tv_taxincl.setVisibility(View.GONE);

                //   $tax_amount = $price * ( $rate['rate'] / 100 );

//                tv_taxlabel.setText(paymentInfo.getData().getTaxes().getLabel()); //change
                tv_taxlabel.setText(paymentInfo.getData().getTaxes().get(0).getLabel()); //change

                exludetax(tax_sum, ship_tax);


            } else {

                float tax;

                tax = (float) ((ori_total) * (Double.parseDouble(TAXRATE) / 100));

                   /* float tax = (float) ((ori_total*Float.parseFloat(paymentInfo.getData().getTaxes().getRate()))/
                            (100+Float.parseFloat(paymentInfo.getData().getTaxes().getRate())));*/
                Log.e("TAG", "tax=== " + tax);

                tax_sum = ApplicationData.getFloatValue(tax);
                tax_b = tax;

                rl_tax.setVisibility(View.GONE);
                tv_taxincl.setVisibility(View.VISIBLE);


           /* tv_taxincl.setText(" ("+settingOption.getData().getOptions().getTax_display_cart()+" "+
                    settingOption.getData().getOptions().getCurrency_symbol()+""+(tax_sum+ship_tax)+" "+paymentInfo.getData().getTaxes().getLabel()+")");
*/
                includtax(tax_sum, ship_tax);
            }


            // }

        } else {

            tv_taxincl.setVisibility(View.GONE);
            tv_taxshipIncl.setText("");
            rl_tax.setVisibility(View.GONE);

            tax_sum = 0;
            tax_b = 0;
            ship_tax = 0;
        }


    }

    private void includtax(double tax_sum, double ship_tax) {
        float tax = (float) tax_sum;
        float shiptax = (float) ship_tax;
        Total_tax = tax + shiptax;
        tv_taxincl.setText(" (" + settingOption.getData().getOptions().getTax_display_cart() + " " +
                settingOption.getData().getOptions().getCurrency_symbol() + "" + (Total_tax) + " " + label + ")");

    }

    private void exludetax(double tax_sum, double ship_tax) {
        float tax = (float) tax_sum;
        float shiptax = (float) ship_tax;
        Total_tax = tax + shiptax;

        AppDebugLog.println("Total_tax= " + Total_tax);
        tv_tax.setText(settingOption.getData().getOptions().getCurrency_symbol() + "" + (Total_tax));

    }


    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        //registerReceiver(receive, new IntentFilter(Intent_Class_Data.NOTIFICATION));

    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        //unregisterReceiver(receive);

    }

    public void FechDataoftotal() {
        array_total = dataWrite.fechtotal();

        AppDebugLog.println("array total size is = " + array_total.size());

        for (int i = 0; i < array_total.size(); i++) {
            sum = sum + array_total.get(i);
            total_amount = sum;

            AppDebugLog.println("array sum is = " + sum);
        }
        tv_subTotal.setText(options.getCurrency_symbol() + " " + ApplicationData.getStringValue(total_amount));
        TotalCount(total_amount, coupon_cost, ship_amount, coupon_type);

    }

    public void TotalCount(double total_amount, double coupon_cost, double ship_amount, String coupon_type) {

        AppDebugLog.println("array total_amount =  " + total_amount);
        AppDebugLog.println("array coupon_cost =  " + coupon_cost);
        AppDebugLog.println("array ship_amount is = " + ship_amount);
        AppDebugLog.println("array coupon_type is = " + coupon_type);

        if (settingOption.getData().getOptions().getTax_display_cart().equals("excl")) {

            //TAXRATE = paymentInfo.getData().getTaxes().getRate();
            if (coupon_type.equals("fixed_cart")) {


                if (total_amount > coupon_cost) {

                    double with_tax_total = (ori_total / total_amount) * coupon_cost;

                    double without_tax = (withoutTax_total / total_amount) * coupon_cost;


                    double ori1 = ori_total - with_tax_total;
                    double without_tax_total = withoutTax_total - without_tax;
                    //discount_coupon_tax= coupon_cost*Double.parseDouble(TAXRATE)/100;
                    tax_sum = ori1 * (Double.parseDouble(TAXRATE) / 100);
                    coupon_tax = tax_sum;
                    exludetax(tax_sum, ship_tax);
                    double shipamount1 = ship_amount + Total_tax;

                    float total = (float) (ori1 + shipamount1 + without_tax_total);

                    coupon_discount = coupon_cost;
                    pay_amount = total;
                    tv_total.setText(options.getCurrency_symbol() + " " + ApplicationData.getStringValue(total));
                    tv_couponCost.setText("-" + options.getCurrency_symbol() + ApplicationData.getStringValue(coupon_discount));

                    if (iscall == false) {
                        iscall = true;
                        rg_shipping.removeAllViews();
                        ShippingMethod((ori1 + without_tax_total));
                    }


                } else {

                    double ori1 = ori_total - ori_total;
                    //discount_coupon_tax= ori_total*Double.parseDouble(TAXRATE)/100;


                    tax_sum = ori1 * (Double.parseDouble(TAXRATE) / 100);
                    coupon_tax = tax_sum;
                    exludetax(tax_sum, ship_tax);
                    double shipamount1 = ship_amount + Total_tax;

                    float total = (float) (ori1 + shipamount1);

                    coupon_discount = ori_total + withoutTax_total;
                    pay_amount = total;
                    tv_total.setText(options.getCurrency_symbol() + " " + ApplicationData.getStringValue(total));
                    tv_couponCost.setText("-" + options.getCurrency_symbol() + ApplicationData.getStringValue(coupon_discount));

                    if (iscall == false) {
                        iscall = true;
                        rg_shipping.removeAllViews();
                        ShippingMethod((ori1));
                    }
                }


            } else if (coupon_type.equals("percent_product")) {
                float sum1 = 0;
                float without_total = 0;
                double sub_percent = 0;

                for (int i = 0; i < productArrayList.size(); i++) {
                    double product_percent = 0;

                    if (productArrayList.get(i).getTax_status().equals("taxable")) {

                        product_percent = (Double.parseDouble(productArrayList.get(i).getSubTotal()) * coupon_cost) / 100;
                        sub_percent = sub_percent + product_percent;

                        double product_minus = Double.parseDouble(productArrayList.get(i).getSubTotal()) - product_percent;
                        sum1 = (float) (sum1 + product_minus);


                    } else {

                        product_percent = (Double.parseDouble(productArrayList.get(i).getSubTotal()) * coupon_cost) / 100;
                        sub_percent = sub_percent + product_percent;

                        double product_minus = Double.parseDouble(productArrayList.get(i).getSubTotal()) - product_percent;
                        without_total = (float) (without_total + product_minus);

                    }


                    coupon_discount = sub_percent;

                }

                //discount_coupon_tax= ori_total*Double.parseDouble(TAXRATE)/100;

                tax_sum = sum1 * (Double.parseDouble(TAXRATE) / 100);
                coupon_tax = tax_sum;
                exludetax(tax_sum, ship_tax);

                float shipamount1 = (float) (ship_amount + Total_tax);

                float sum2 = sum1 + shipamount1 + without_total;
                pay_amount = sum2;
                tv_total.setText(options.getCurrency_symbol() + " " + ApplicationData.getStringValue(sum2));
                tv_couponCost.setText("-" + options.getCurrency_symbol() + ApplicationData.getStringValue(coupon_discount));
                if (iscall == false) {
                    iscall = true;
                    rg_shipping.removeAllViews();
                    ShippingMethod((sum1 + without_total));
                }

            } else if (coupon_type.equals("percent")) {


                double total_percent;
                total_percent = (ori_total * coupon_cost) / 100;

                double without_total = (withoutTax_total * coupon_cost) / 100;

                double without_tax_total = withoutTax_total - without_total;
                double ori1 = ori_total - total_percent;

                tax_sum = ori1 * (Double.parseDouble(TAXRATE) / 100);
                coupon_tax = tax_sum;
                exludetax(tax_sum, ship_tax);
                double shipamount1 = ship_amount + Total_tax;

                float total = (float) (ori1 + shipamount1 + without_tax_total);

                coupon_discount = total_percent + without_total;
                pay_amount = total;


                tv_total.setText(options.getCurrency_symbol() + " " + ApplicationData.getStringValue(total));
                tv_couponCost.setText("-" + options.getCurrency_symbol() + ApplicationData.getStringValue(coupon_discount));

                if (iscall == false) {
                    iscall = true;
                    rg_shipping.removeAllViews();
                    ShippingMethod((ori1 + without_tax_total));
                }


            } else if (coupon_type.equals("fixed_product")) {

                float sum3 = 0;
                double sub_minus = 0;
                float without_tax_total = 0;
                for (int i = 0; i < productArrayList.size(); i++) {
                    double sub = 0;
                    double without_total = 0;

                    if (Double.parseDouble(productArrayList.get(i).getOri_price()) < coupon_cost) {

                        sub_minus = sub_minus + Double.parseDouble(productArrayList.get(i).getOri_price()) * Double.parseDouble(productArrayList.get(i).getQnty());

                        sub = Double.parseDouble(productArrayList.get(i).getSubTotal()) -
                                (Double.parseDouble(productArrayList.get(i).getOri_price()) * Double.parseDouble(productArrayList.get(i).getQnty()));

                        sum3 = (float) (sum3 + sub);


                        coupon_discount = sub_minus;


                    } else {

                        sub_minus = sub_minus + coupon_cost * Double.parseDouble(productArrayList.get(i).getQnty());

                        if (productArrayList.get(i).getTax_status().equals("taxable")) {


                            sub = Double.parseDouble(productArrayList.get(i).getSubTotal()) -
                                    (coupon_cost * Double.parseDouble(productArrayList.get(i).getQnty()));
                            sum3 = (float) (sum3 + sub);

                        } else {

                            without_total = Double.parseDouble(productArrayList.get(i).getSubTotal()) -
                                    (coupon_cost * Double.parseDouble(productArrayList.get(i).getQnty()));
                            without_tax_total = (float) (without_tax_total + without_total);

                        }


                        coupon_discount = sub_minus;
                    }

                }

                tax_sum = sum3 * (Double.parseDouble(TAXRATE) / 100);
                coupon_tax = tax_sum;
                exludetax(tax_sum, ship_tax);
                double shipamount1 = ship_amount + Total_tax;

                float sum4 = (float) (sum3 + shipamount1 + without_tax_total);

                pay_amount = sum4;
                tv_total.setText(options.getCurrency_symbol() + " " + ApplicationData.getStringValue(sum4));
                //AppDebugLog.println("coupon_discount =  " + coupon_discount);
                tv_couponCost.setText("-" + options.getCurrency_symbol() + ApplicationData.getStringValue(coupon_discount));

                if (iscall == false) {
                    iscall = true;
                    rg_shipping.removeAllViews();
                    ShippingMethod((sum3 + without_tax_total));
                }


            } else {

                float shipamount1 = (float) (ship_amount + Total_tax);

                AppDebugLog.println("array total_amount =  " + total_amount + ",shipamount1= " + shipamount1);

                float total = (float) (total_amount + shipamount1);
                pay_amount = total;
                tv_total.setText(options.getCurrency_symbol() + " " + ApplicationData.getStringValue(total));

                //ShippingMethod((ori_total+withoutTax_total),min_cost,requires);

            }

        } else {

//            TAXRATE = "0";  //change this is used when object used
            TAXRATE = String.valueOf(coupon_cost);

            if (coupon_type.equals("fixed_cart")) {

                float ship_amount2 = (float) (ship_amount);

                if ((ori_total + withoutTax_total) > coupon_cost) {

                    double with_tax_total = (ori_total / (ori_total + withoutTax_total)) * coupon_cost;

                    double without_tax = (withoutTax_total / (ori_total + withoutTax_total)) * coupon_cost;

                    double ori_total1 = ori_total - with_tax_total;
                    double without_tax_total = withoutTax_total - without_tax;

                    tax_sum = ori_total1 * (Double.parseDouble(TAXRATE) / 100);
                    coupon_tax = tax_sum;
                    includtax(tax_sum, ship_tax);

                    double tax_coupon = coupon_cost * (Double.parseDouble(TAXRATE) / 100);
                    coupon_cost = coupon_cost + tax_coupon;

                    float total = (float) (ori_total1 + without_tax_total + ship_amount2 + tax_sum);

                    coupon_discount = coupon_cost;
                    pay_amount = total;
                    tv_total.setText(options.getCurrency_symbol() + " " + ApplicationData.getStringValue(total));
                    tv_couponCost.setText("-" + options.getCurrency_symbol() + ApplicationData.getStringValue(coupon_discount));

                    if (iscall == false) {
                        iscall = true;
                        rg_shipping.removeAllViews();
                        ShippingMethod((ori_total1 + without_tax_total));
                    }


                } else {

                    float total = (float) ((total_amount - total_amount) + ship_amount2);
                    tv_taxincl.setText("");
                    coupon_discount = total_amount;
                    pay_amount = total;
                    tv_total.setText(options.getCurrency_symbol() + " " + ApplicationData.getStringValue(total));
                    tv_couponCost.setText("-" + options.getCurrency_symbol() + ApplicationData.getStringValue(coupon_discount));

                    if (iscall == false) {
                        iscall = true;
                        rg_shipping.removeAllViews();
                        ShippingMethod((total_amount));
                    }
                }

            } else if (coupon_type.equals("percent_product")) {
                float sum1 = 0;
                double sub_percent = 0;
                float without_total = 0;
                for (int i = 0; i < productArrayList.size(); i++) {
                    double product_percent = 0;
                    if (productArrayList.get(i).getTax_status().equals("taxable")) {
                        product_percent = (Double.parseDouble(productArrayList.get(i).getOri_price()) *
                                Double.parseDouble(productArrayList.get(i).getQnty()) * coupon_cost) / 100;
                        sub_percent = sub_percent + product_percent;

                        double product_minus = (Double.parseDouble(productArrayList.get(i).getOri_price()) *
                                Double.parseDouble(productArrayList.get(i).getQnty())) - product_percent;
                        sum1 = (float) (sum1 + product_minus);


                    } else {

                        product_percent = (Double.parseDouble(productArrayList.get(i).getOri_price()) *
                                Double.parseDouble(productArrayList.get(i).getQnty()) * coupon_cost) / 100;
                        sub_percent = sub_percent + product_percent;

                        double product_minus = (Double.parseDouble(productArrayList.get(i).getOri_price()) *
                                Double.parseDouble(productArrayList.get(i).getQnty())) - product_percent;

                        without_total = (float) (without_total + product_minus);


                    }


                    coupon_discount = sub_percent;

                }

                double coupon_discount1 = coupon_discount * (Double.parseDouble(TAXRATE) / 100);
                coupon_discount = coupon_discount + coupon_discount1;

                double coupon_per = (ori_total * coupon_cost) / 100;

                double ori1 = ori_total - coupon_per;

                tax_sum = ori1 * (Double.parseDouble(TAXRATE) / 100);
                coupon_tax = tax_sum;
                includtax(tax_sum, ship_tax);

                float sum2 = (float) (sum1 + ship_amount + tax_sum + without_total);
                pay_amount = sum2;
                tv_total.setText(options.getCurrency_symbol() + " " + ApplicationData.getStringValue(sum2));
                tv_couponCost.setText("-" + options.getCurrency_symbol() + ApplicationData.getStringValue(coupon_discount));

                if (iscall == false) {
                    iscall = true;
                    rg_shipping.removeAllViews();
                    ShippingMethod((sum1 + without_total));
                }


            } else if (coupon_type.equals("percent")) {

                float ship_amount2 = (float) (ship_amount);

                double sum2 = 0;
                for (int i = 0; i < array_total.size(); i++) {
                    sum2 = sum2 + array_total.get(i);

                }

                double total_percent;
                total_percent = ((ori_total + withoutTax_total) * coupon_cost) / 100;

                double couponPer = total_percent * (Double.parseDouble(TAXRATE) / 100);
                total_percent = total_percent + couponPer;


                double coupon_per = (ori_total * coupon_cost) / 100;

                double ori1 = ori_total - coupon_per;

                tax_sum = ori1 * (Double.parseDouble(TAXRATE) / 100);
                coupon_tax = tax_sum;
                includtax(tax_sum, ship_tax);


                float total = (float) (sum2 + (-total_percent) + ship_amount2);

                coupon_discount = total_percent;
                pay_amount = total;

                tv_total.setText(options.getCurrency_symbol() + " " + ApplicationData.getStringValue(total));
                tv_couponCost.setText("-" + options.getCurrency_symbol() + ApplicationData.getStringValue(coupon_discount));


                if (iscall == false) {
                    iscall = true;
                    rg_shipping.removeAllViews();
                    ShippingMethod((ori1));
                }


            } else if (coupon_type.equals("fixed_product")) {

                float sum3 = 0;
                double sub_minus = 0;
                //double coupon_per=0;
                double ori_coupon = 0;
                double ori_code = 0;
                float without_tax_total = 0;
                ori_code = coupon_cost;
                double tax_coupon = coupon_cost * (Double.parseDouble(TAXRATE) / 100);
                coupon_cost = coupon_cost + tax_coupon;

                for (int i = 0; i < productArrayList.size(); i++) {
                    double sub = 0;
                    double without_total = 0;

                    if (Double.parseDouble(productArrayList.get(i).getOri_price()) < coupon_cost) {

                        ori_coupon = ori_coupon + (Double.parseDouble(productArrayList.get(i).getOri_price()) *
                                Double.parseDouble(productArrayList.get(i).getQnty()));

                        sub_minus = sub_minus + (Double.parseDouble(productArrayList.get(i).getOri_price()) *
                                Double.parseDouble(productArrayList.get(i).getQnty()));

                        sub = Double.parseDouble(productArrayList.get(i).getSubTotal()) -
                                (Double.parseDouble(productArrayList.get(i).getSubTotal()));

                        sum3 = (float) (sum3 + sub);
                        coupon_discount = sub_minus;
                    } else {
                        //coupon_per = coupon_per+(coupon_cost*Double.parseDouble(productArrayList.get(i).getQnty()))/100;
                        sub_minus = sub_minus + ori_code * Double.parseDouble(productArrayList.get(i).getQnty());

                        if (productArrayList.get(i).getTax_status().equals("taxable")) {

                            ori_coupon = ori_coupon + (ori_code * Double.parseDouble(productArrayList.get(i).getQnty()));
                            sub = (Double.parseDouble(productArrayList.get(i).getOri_price()) *
                                    Double.parseDouble(productArrayList.get(i).getQnty())) -
                                    (ori_code * Double.parseDouble(productArrayList.get(i).getQnty()));

                            sum3 = (float) (sum3 + sub);

                        } else {

                            without_total = (Double.parseDouble(productArrayList.get(i).getOri_price()) *
                                    Double.parseDouble(productArrayList.get(i).getQnty())) -
                                    (ori_code * Double.parseDouble(productArrayList.get(i).getQnty()));
                            without_tax_total = (float) (without_tax_total + without_total);
                        }

                        coupon_discount = sub_minus;

                    }

                }


                double coupon_discount1 = coupon_discount * (Double.parseDouble(TAXRATE) / 100);
                coupon_discount = coupon_discount + coupon_discount1;

                double ori1 = ori_total - ori_coupon;

                tax_sum = ori1 * (Double.parseDouble(TAXRATE) / 100);
                coupon_tax = tax_sum;
                includtax(tax_sum, ship_tax);

                float sum4 = (float) (sum3 + ship_amount + tax_sum + without_tax_total);
                pay_amount = sum4;
                tv_total.setText(options.getCurrency_symbol() + " " + ApplicationData.getStringValue(sum4));
                //AppDebugLog.println("coupon_discount =  " + coupon_discount);
                tv_couponCost.setText("-" + options.getCurrency_symbol() + ApplicationData.getStringValue(coupon_discount));

                if (iscall == false) {
                    iscall = true;
                    rg_shipping.removeAllViews();
                    ShippingMethod((sum3 + without_tax_total));
                }

            } else {

                float ship_amount2 = (float) (ship_amount);


                AppDebugLog.println("array total_amount =  " + total_amount + ",ship_amount= " + ship_amount2);

                float total = (float) (ori_total + withoutTax_total + ship_amount2 + tax_sum);
                // float total = (float) (ori_total+ship_amount2);
                pay_amount = total;
                tv_total.setText(options.getCurrency_symbol() + " " + ApplicationData.getStringValue(total));

                //ShippingMethod((ori_total+withoutTax_total),min_cost,requires);

            }

        }
    }

    @Override
    public void onBackPressed() {
        // activity.finish();
        Intent i = new Intent(activity, MainActivity.class);
        startActivity(i);
        finish();

     /*   if (key.equals("user")){
            Intent i = new Intent(activity, MainActivity.class);
            startActivity(i);
            finish();
        }else {
            activity.finish();
        }*/

    }

    @Override
    public void backgroundActivityComp(String response, AppConstant.HttpRequestType completedRequestType) {
        AppConstant.HTTPResponseCode responseCode = appData.getResponseCode();
        AppDebugLog.println("responseCode : " + responseCode);

        switch (responseCode) {

            case Method:
                String data = dataWrite.fetchMethod();
                try {
                    paymentInfo = new BeanPaymentInfo();
                    settings = new ArrayList<Setting>();
                    Gson gson = new Gson();
                    if (data != null) {
                        Type type_one = new TypeToken<BeanPaymentInfo>() {
                        }.getType();
                        paymentInfo = gson.fromJson(data, type_one);
                            /*AppConstant.payment_gateways = paymentInfo.getData().getPayment_gateways();*/
                        if (paymentInfo.getSuccess().equals("0")) {
                            //Toast.makeText(ApplicationContext.getAppContext(),"unkwon error",Toast.LENGTH_SHORT).show();
                            dataWrite.showToast("unkwon error");
                            progressDialog.dismiss();
                        } else {
                                /*if (settingOption.getData().getOptions().getTax_based_on().equals("base")){
                                    TAXRATE = "0";
                                    ship_tx_label = "";
                                }else {
                                    TAXRATE = paymentInfo.getData().getTaxes().getRate();
                                }*/
//                            TAXRATE = paymentInfo.getData().getTaxes().getRate(); //change
                            TAXRATE = paymentInfo.getData().getTaxes().get(0).getRate();
                            Taxdisplay();
//                            if (paymentInfo.getData().getTaxes().getLabel().equals("0")) { //change
                            if (paymentInfo.getData().getTaxes().get(0).getLabel().equals("0")) {
                                rl_tax.setVisibility(View.GONE);
                                label = "";
                            } else {
//                                label = paymentInfo.getData().getTaxes().getLabel(); //change
                                label = paymentInfo.getData().getTaxes().get(0).getLabel();
                            }

                            if (TAXRATE.equals("0")) {
                                tv_taxincl.setVisibility(View.GONE);
                                ship_tx_label = "";
                            } else {
                               /* ship_tx_label = "(" + settingOption.getData().getOptions().getTax_display_cart() + "."
                                        + paymentInfo.getData().getTaxes().getLabel() + ")";*/  //change
                                ship_tx_label = "(" + settingOption.getData().getOptions().getTax_display_cart() + "."
                                        + paymentInfo.getData().getTaxes().get(0).getLabel() + ")";
                            }

                            if (paymentInfo.getData().getShipping_method().size() == 0) {
                                rg_paymentMethod.removeAllViews();

                                paymentMethod();
                                rl_shippingmethods.setVisibility(View.GONE);
                            } else {
                                ShippingMethod(ori_total + withoutTax_total);
                                rl_shippingmethods.setVisibility(View.VISIBLE);
                            }

                            progressDialog.dismiss();
                        }
                    }

                } catch (Exception e) {
                    // TODO: handle exception
                    AppDebugLog.println("error =" + e);
                    progressDialog.dismiss();
                }
                break;

            case saveOrder:
                progressDialog.dismiss();
                if (payment_id.equals("paytm")) {
                    generatechecksumfromServer();
                } else {
                    Intent i = new Intent(activity, ConfirmationActivity.class);
                    startActivity(i);
                    finish();
                }

                break;

            case Applycode:
                coupon();
                break;

            case CancelCode:
                Intent c = new Intent(activity, CheckOutActivity.class);
                startActivity(c);
                finish();
                break;

            case ServerError:
                appData.showUserAlert(this, getString(R.string.alert_title_message),
                        getString(R.string.alert_body_server_error), null);
                break;

            case Success:
                break;

            case hash_success:

                String hashkeys = manager.getPreferences(ApplicationContext.getAppContext(), AppConstant.hashKey);

                AppDebugLog.println("Login payment_hash :==== " + hashkeys);


                try {
                    JSONObject object = new JSONObject(hashkeys);
                    JSONObject jsonObject = object.getJSONObject("data");
                    hashKey = jsonObject.getString("payment_hash");
                    payment_related_details_for_mobile_sdk = jsonObject.getString("payment_related_details_for_mobile_sdk_hash");
                    vas_for_mobile_sdk_hash = jsonObject.getString("vas_for_mobile_sdk_hash");
                    get_merchant_ibibo_codes_hash = jsonObject.getString("get_merchant_ibibo_codes_hash");
                    verify_payment_hash = jsonObject.getString("verify_payment_hash");
                    send_sms_hash = jsonObject.getString("send_sms_hash");
                    delete_user_card_hash = jsonObject.getString("delete_user_card_hash");
                    get_user_cards_hash = jsonObject.getString("get_user_cards_hash");
                    edit_user_card_hash = jsonObject.getString("edit_user_card_hash");
                    save_user_card_hash = jsonObject.getString("save_user_card_hash");


                  /*  AppDebugLog.println("Login payment_hash :==== " + hashKey+"\n"+payment_related_details_for_mobile_sdk+"\n"+
                            vas_for_mobile_sdk_hash);*/
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                Intent intent = new Intent(activity, PaymentsActivity.class);
                intent.putExtra("url", url);
                intent.putExtra("postData", postData + hashKey);
                startActivityForResult(intent, PAYUBIZ_REQUEST_CODE);

                break;


            case checksum_success:
                String checksumkeys = manager.getPreferences(ApplicationContext.getAppContext(), AppConstant.checkSumKey);

                //AppDebugLog.println("Login payment_hash :==== " + checksumkeys);

                try {

                    JSONObject object = new JSONObject(checksumkeys);
                    JSONObject jsonObject = object.getJSONObject("data");
                    checksum_paytm = jsonObject.getString("checksum");
                    AppDebugLog.println("Login checksum_paytme :==== " + checksum_paytm);

                    onStartTransactionPaytm(checksum_paytm);
                  /*  AppDebugLog.println("Login payment_hash :==== " + hashKey+"\n"+payment_related_details_for_mobile_sdk+"\n"+
                            vas_for_mobile_sdk_hash);*/

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;


            case paytmResponce:

                final Dialog dialog = new Dialog(activity);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_messages);

                TextView tv_ok = (TextView) dialog.findViewById(R.id.tvOK);
                TextView tv_message = (TextView) dialog.findViewById(R.id.tvMessages);

                //tv_ok.setBackgroundColor(Color.parseColor(color_btn_bg));
                tv_ok.setTextColor(Color.parseColor(color_btn_bg));

                tv_message.setText("Transation Successfull");
                tv_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent i = new Intent(activity, ConfirmationActivity.class);
                        startActivity(i);
                        finish();

                        dialog.dismiss();

                    }
                });

                dialog.show();

                break;

        }
    }


    /* Generate checksum from server */
    private void generatechecksumfromServer() {
        /*Random r = new Random(System.currentTimeMillis());
        orderId = "ORDER" + (1 + r.nextInt(2)) * 10000
                + r.nextInt(10000);*/
        paymentAmount = "" + pay_amount;
        orderid = manager.getPreferences(ApplicationContext.getAppContext(), AppConstant.ORDER_ID);
        Log.d("shailesh", "data  is===" + orderid);
        JSONObject object = new JSONObject();
        try {
            object.put("mid", MID);
            object.put("order_id", orderid);
            object.put("cust_id", user_id);
            object.put("industry_type_id", Industry_type_id);
            object.put("channel_id", CHANNEL_ID);
            object.put("amount", paymentAmount);
            object.put("mobile_no", bill_phone);
            object.put("email", bill_email);
            object.put("website", WEBSITE);
            object.put("callback_url", CALLBACK_URL + "" + orderid);
            //object.put("callback_url","http://192.168.100.108/PaytmChecksum/generateChecksum.php");


        } catch (Exception e) {
            // TODO: handle exception
        }

        // String requestURL = "http://192.168.100.116/PaytmChecksum/generateChecksum.php";
        String requestURL = Webservice.BASE_URL + "" + Webservice.URL_KEYCHECKSUM;
        AppDebugLog.println("requestURL in sendNewDataRequest : " + requestURL);
        RequestTask requestTask = new RequestTask(requestURL, AppConstant.HttpRequestType.DataUpdateRequest);
        requestTask.delegate = this;
        requestTask.execute(requestURL, "paytmhash", object.toString());

    }

    /* send data to paytm server with checksum */
    private void onStartTransactionPaytm(String checksum_paytm) {

        PaytmPGService Service = PaytmPGService.getProductionService();

        //Kindly create complete Map and checksum on your server side and then put it here in paramMap.
        orderid = manager.getPreferences(ApplicationContext.getAppContext(), AppConstant.ORDER_ID);
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("MID", MID);
        paramMap.put("ORDER_ID", orderid);
        paramMap.put("CUST_ID", user_id);
        paramMap.put("INDUSTRY_TYPE_ID", Industry_type_id);
        paramMap.put("CHANNEL_ID", CHANNEL_ID);
        paramMap.put("TXN_AMOUNT", paymentAmount);
        paramMap.put("WEBSITE", WEBSITE);
        paramMap.put("EMAIL", bill_email);
        paramMap.put("MOBILE_NO", bill_phone);
        paramMap.put("CALLBACK_URL", CALLBACK_URL + "" + orderid);
        paramMap.put("CHECKSUMHASH", checksum_paytm);
        PaytmOrder Order = new PaytmOrder(paramMap);


        Service.initialize(Order, null);


        Service.startPaymentTransaction(activity, true, true,
                new PaytmPaymentTransactionCallback() {

                    @Override
                    public void someUIErrorOccurred(String inErrorMessage) {
                        // Some UI Error Occurred in Payment Gateway Activity.
                        // // This may be due to initialization of views in
                        // Payment Gateway Activity or may be due to //
                        // initialization of webview. // Error Message details
                        // the error occurred.
                        Log.d("LOG", "Payment inErrorMessage : " + inErrorMessage);
                    }

                    @Override
                    public void onTransactionResponse(Bundle inResponse) {
                        Log.d("LOG", "Payment Transaction : " + inResponse);

                        PaytmBundle = inResponse;
                        // Toast.makeText(getApplicationContext(), "Payment Transaction response "+inResponse.getString("STATUS"), Toast.LENGTH_LONG).show();

                        paytmBoolean = true;
                        sendData("paytmorder");

                    }

                    @Override
                    public void networkNotAvailable() {
                        // If network is not
                        // available, then this
                        // method gets called.
                    }

                    @Override
                    public void clientAuthenticationFailed(String inErrorMessage) {
                        // This method gets called if client authentication
                        // failed. // Failure may be due to following reasons //
                        // 1. Server error or downtime. // 2. Server unable to
                        // generate checksum or checksum response is not in
                        // proper format. // 3. Server failed to authenticate
                        // that client. That is value of payt_STATUS is 2. //
                        // Error Message describes the reason for failure.
                    }

                    @Override
                    public void onErrorLoadingWebPage(int iniErrorCode,
                                                      String inErrorMessage, String inFailingUrl) {

                    }

                    // had to be added: NOTE
                    @Override
                    public void onBackPressedCancelTransaction() {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
                        Log.d("LOG", "Payment Transaction Failed " + inErrorMessage);
                        Toast.makeText(getBaseContext(), "Payment Transaction Failed ", Toast.LENGTH_LONG).show();
                    }

                });
    }

    private void coupon() {

        try {
            JSONObject object = new JSONObject(AppConstant.ApplyCoupon);
            JSONObject jsonObject = object.optJSONObject("data");
            if (object.getInt("success") == 1) {

                JSONObject object2 = jsonObject.optJSONObject("coupons");
                BeanCoupon beanCoupon = new BeanCoupon();
                beanCoupon.setAmount(object2.getString("amount"));
                beanCoupon.setId(object2.getString("id"));
                beanCoupon.setCode(object2.getString("code"));
                beanCoupon.setType(object2.getString("type"));
                beanCoupon.setEnable_free_shipping(object2.getString("enable_free_shipping"));
                coupons.add(beanCoupon);

                double c = Double.parseDouble(coupons.get(0).getAmount());
                coupon_cost = c;

                tv_couponcode.setText(coupons.get(0).getCode());
                enable_shipping = coupons.get(0).getEnable_free_shipping();

                coupon_type = coupons.get(0).getType();


                TotalCount(total_amount, coupon_cost, ship_amount, coupon_type);


                dataWrite.showToast(jsonObject.getString("err_msg"));

                rl_Appcouponcode.setVisibility(View.GONE);

                rl_coupooncode.setVisibility(View.VISIBLE);
                vwCheckout13.setVisibility(View.VISIBLE);

                progressDialog.dismiss();

            } else {

                dataWrite.showToast(jsonObject.getString("err_msg"));
                et_code.setError(jsonObject.getString("err_msg"));
                progressDialog.dismiss();
            }


        } catch (JSONException e) {
            // TODO: handle exception

            AppDebugLog.println("error =" + e);
            progressDialog.dismiss();
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
}
