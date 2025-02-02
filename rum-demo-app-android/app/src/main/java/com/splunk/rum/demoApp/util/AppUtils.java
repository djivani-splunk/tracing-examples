package com.splunk.rum.demoApp.util;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.gson.Gson;
import com.splunk.rum.SplunkRum;
import com.splunk.rum.demoApp.R;
import com.splunk.rum.demoApp.model.entity.response.Product;
import com.splunk.rum.demoApp.model.entity.response.ProductListResponse;
import com.splunk.rum.demoApp.network.RetrofitException;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public final class AppUtils {

    private static final String APP_UTILS = "AppUtils.java";

    /**
     * @param product The product class for store product data into the cart
     */
    @SuppressWarnings("ALL")
    public static void storeProductInCart(Context context, Product product) {
        ProductListResponse productListResponse = getProductsFromPref(context);
        if (productListResponse != null) {
            if (!CollectionUtils.isEmpty(productListResponse.getProducts())) {
                for (Product productDetails : productListResponse.getProducts()) {
                    if (product.getId().equalsIgnoreCase(productDetails.getId())) {
                        productDetails.setQuantity(productDetails.getQuantity() + product.getQuantity());
                    } else {
                        productListResponse.getProducts().add(product);
                    }
                    break;
                }
            } else {
                productListResponse.getProducts().add(product);
            }
        } else {
            productListResponse = new ProductListResponse();
            productListResponse.getProducts().add(product);
        }

        String cart_product_json = new Gson().toJson(productListResponse);
        PreferenceHelper.setValue(context, AppConstant.SharedPrefKey.CART_PRODUCTS, cart_product_json);
    }

    public static ProductListResponse getProductsFromPref(Context context) {
        String cart_product_json = PreferenceHelper
                .getValue(context, AppConstant.SharedPrefKey.CART_PRODUCTS, String.class, "");
        try {
            if (StringHelper.isNotEmpty(cart_product_json)) {
                return new Gson().fromJson(cart_product_json, ProductListResponse.class);
            } else {
                return new ProductListResponse();
            }
        } catch (Exception e) {
            AppUtils.handleRumException(e);
        }
        return new ProductListResponse();
    }

    /**
     * @param throwable Super class of all exception class
     */
    public static void handleRumException(Throwable throwable) {
        if (SplunkRum.getInstance() != null) {
            SplunkRum.getInstance().addRumException(throwable);
        }
    }


    /**
     * @param context    View context
     * @param statusCode HTTP status code
     * @return error message from the status code
     */
    public static String getHttpErrorMessage(Context context, int statusCode) {
        String errorMessage;
        switch (statusCode) {
            case 400:
                errorMessage = context.getString(R.string.error_bad_request_400);
                break;
            case 401:
                errorMessage = context.getString(R.string.error_unauthorized_401);
                break;
            case 403:
                errorMessage = context.getString(R.string.error_forbidden_403);
                break;
            case 404:
                errorMessage = context.getString(R.string.error_not_found_404);
                break;
            case 405:
                errorMessage = context.getString(R.string.error_method_not_allowed_405);
                break;
            case 408:
                errorMessage = context.getString(R.string.error_request_timeout_408);
                break;
            case 413:
                errorMessage = context.getString(R.string.error_request_entity_too_large_413);
                break;
            case 414:
                errorMessage = context.getString(R.string.error_request_uri_too_long_414);
                break;
            case 500:
                errorMessage = context.getString(R.string.error_internal_server_error_500);
                break;
            case 504:
                errorMessage = context.getString(R.string.error_internal_server_error_504);
                break;
            default:
                errorMessage = context.getString(R.string.error_unknown);
                break;
        }
        return errorMessage;

    }


    /**
     * @param context           View context.
     * @param retrofitException Using this parameter handle which kind of retrofit exception occur.
     */
    public static void handleApiError(Context context, RetrofitException retrofitException) {
        try {
            if (context == null) return;

            if (retrofitException != null) {

                switch (retrofitException.getKind()) {
                    case HTTP:
                        if (retrofitException.getRetrofitExceptionBody() != null && retrofitException.getRetrofitExceptionBody().getStatus() != 0) {
                            String errorMessage = getHttpErrorMessage(context, retrofitException.getRetrofitExceptionBody().getStatus());
                            AlertDialogHelper.showDialog(context, null, errorMessage
                                    , context.getString(R.string.ok), null, false,
                                    null, 0);
                        } else if (retrofitException.getResponse() != null && retrofitException.getResponse().code() != 0) {
                            String errorMessage = getHttpErrorMessage(context, retrofitException.getResponse().code());
                            AlertDialogHelper.showDialog(context, null, errorMessage
                                    , context.getString(R.string.ok), null, false,
                                    null, 0);
                        } else if (StringHelper.isNotEmpty(retrofitException.getMessage())) {
                            AlertDialogHelper.showDialog(context, null, retrofitException.getMessage()
                                    , context.getString(R.string.ok), null, false,
                                    null, 0);
                        }
                        break;
                    case NETWORK:
                        if (StringHelper.isEmpty(retrofitException.getMessage())) {
                            AlertDialogHelper.showDialog(context, null, context.getString(R.string.error_network)
                                    , context.getString(R.string.ok), null, false,
                                    null, 0);
                        } else {
                            AlertDialogHelper.showDialog(context, null, retrofitException.getMessage()
                                    , context.getString(R.string.ok), null, false,
                                    null, 0);
                        }
                        break;
                    case UNEXPECTED:
                        AlertDialogHelper.showDialog(context, null, context.getString(R.string.error_unknown)
                                , context.getString(R.string.ok), null, false,
                                null, 0);
                        break;
                }
            } else {
                AlertDialogHelper.showDialog(context, null, context.getString(R.string.error_unknown_utils)
                        , context.getString(R.string.ok), null, false,
                        null, 0);
            }
        } catch (Exception e) {
            Log.e(APP_UTILS, e.getMessage());
        }
    }

    /**
     * @param activity To get current activity screen width
     * @return width of screen
     */
    public static float getScreenWidth(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return Math.min(metrics.widthPixels, metrics.heightPixels) / metrics.density;
    }

    /**
     * @param activity To get current activity screen width >= 720
     * @return is10InchTablet or not
     */
    @SuppressWarnings("ALL")
    public static boolean is10InchTablet(Activity activity) {
        return getScreenWidth(activity) >= 720;
    }

    /**
     * @param mContext  View context
     * @param imageName image name which is same as in drawable without extension (air_plant)
     * @return int identifier
     */
    public static int getImage(Context mContext, String imageName) {
        if (mContext != null) {
            return mContext.getResources().getIdentifier(imageName, "drawable", mContext.getPackageName());
        }
        return 0;
    }

    /**
     * @param context View context
     * @param message Toast message
     */
    public static void showError(final Context context, final String message) {
        getToast(context, message).show();
    }

    /**
     * @param context View context
     * @param message Toast message
     */
    @SuppressWarnings("unused")
    public static void showShortMessage(Context context, String message) {
        getToast(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * @param context View context
     * @param message Toast message
     * @return Long duration toast
     */
    private static Toast getToast(Context context, String message) {
        return getToast(context, message, Toast.LENGTH_LONG);
    }

    /**
     * @param context View context
     * @param message Toast message
     * @param length  How long to display the message. Either LENGTH_SHORT or LENGTH_LONG
     * @return apply lenth toast
     */
    private static Toast getToast(Context context, String message, int length) {
        return Toast.makeText(context, message, length);
    }


    /**
     * @param context View context
     * @return isNetworkAvailable or not
     */
    @SuppressLint("ObsoleteSdkInt")
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            NetworkCapabilities cap = cm.getNetworkCapabilities(cm.getActiveNetwork());
            if (cap == null) return false;
            return cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network[] networks = cm.getAllNetworks();
            for (Network n : networks) {
                NetworkInfo nInfo = cm.getNetworkInfo(n);
                if (nInfo != null && nInfo.isConnected()) return true;
            }
        } else {
            NetworkInfo[] networks = cm.getAllNetworkInfo();
            for (NetworkInfo nInfo : networks) {
                if (nInfo != null && nInfo.isConnected()) return true;
            }
        }
        return false;
    }

    /**
     * @param activity pass current activity instance
     * @return screensize of mobile
     */
    public static double getScreenSizeInInch(Activity activity) {
        if (activity != null) {
            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
            double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
            double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
            double screenInches = Math.sqrt(x + y);
            screenInches = (double) Math.round(screenInches * 10) / 10;
            return screenInches;
        }
        return 0.0;
    }

    /**
     * @param context   context View context
     * @param latitude  latitude of location
     * @param longitude longitude of location
     * @return country name (Note: It may return blank or null in some devices)
     */
    public static String getCountryName(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getCountryName();
            }
            return null;
        } catch (IOException ioException) {
            AppUtils.handleRumException(ioException);
        }
        return null;
    }

    /**
     * @param isLoading    pass true/false to show or hide progress view
     * @param loaderView   parent of progress dialog view
     * @param parentLayout parent layout of fragment view (If loading progressBar is visible then Hide background view)
     */
    @SuppressWarnings("ALL")
    public static void showHideLoader(boolean isLoading, View loaderView, View parentLayout) {
        if (isLoading) {
            loaderView.setVisibility(View.VISIBLE);
            if (parentLayout != null) {
                parentLayout.setVisibility(View.GONE);
            }
        } else {
            loaderView.setVisibility(View.GONE);
            if (parentLayout != null) {
                parentLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * @param isEnable pass true/false to enable or disable the button
     * @param button pass button which is enable or disable
     */
    public static void enableDisableBtn(boolean isEnable, AppCompatButton button) {
        if (button != null) {
            button.setEnabled(isEnable);
            button.setClickable(isEnable);
        }
    }
}
