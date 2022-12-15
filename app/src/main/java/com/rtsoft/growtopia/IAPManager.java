package com.rtsoft.growtopia;

import android.app.Activity;
import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class IAPManager implements PurchasesUpdatedListener, BillingClientStateListener {
    private final ConcurrentHashMap<String, Purchase> purchasedList = new ConcurrentHashMap<>();
    private final BillingClient billingClient;
    private final Activity mainActivity;
    private boolean isReady = false;

    public IAPManager(Activity activity) {
        mainActivity = activity;
        billingClient = BillingClient.newBuilder(activity).setListener(this)
            .enablePendingPurchases().build();
        billingClient.startConnection(this);
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
        if (billingResult.getResponseCode() == 0 && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == 1) {
            SharedActivity.nativeSendGUIEx(
                SharedActivity.MESSAGE_TYPE_IAP_RESULT,
                billingResult.getResponseCode(),
                0,
                0
            );
        } else {
            SharedActivity.nativeSendGUIEx(
                SharedActivity.MESSAGE_TYPE_IAP_RESULT,
                billingResult.getResponseCode(),
                0,
                0
            );
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() != 1) {
            return;
        }

        SharedActivity.nativeSendGUIStringEx(
            SharedActivity.MESSAGE_TYPE_IAP_RESULT,
            0,
            0,
            0,
            purchase.getOriginalJson() + "|" + purchase.getSignature()
        );
    }

    @Override
    public void onBillingServiceDisconnected() {
        isReady = false;
        billingClient.startConnection(this);
    }

    @Override
    public void onBillingSetupFinished(BillingResult billingResult) {
        if (billingResult.getResponseCode() == 0) {
            isReady = true;
        }
    }

    void IAPPurchase(final String itemId) {
        if (!billingClient.isReady() || !isReady) {
            return;
        }

        if (itemId == null || itemId.isEmpty()) {
            return;
        }

        mainActivity.runOnUiThread(() -> PerformPurchase(itemId));
    }

    private void PerformPurchase(String itemId) {
        ArrayList<QueryProductDetailsParams.Product> arrayList = new ArrayList<>();
        arrayList.add(QueryProductDetailsParams.Product.newBuilder().setProductId(itemId)
            .setProductType("inapp").build());

        billingClient.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder()
                .setProductList(arrayList).build(),
            (result, productDetailsList) -> {
                if (result.getResponseCode() != 0) {
                    return;
                }

                for (ProductDetails productDetails : productDetailsList) {
                    ArrayList<BillingFlowParams.ProductDetailsParams> arrayList2 = new ArrayList<>();
                    arrayList2.add(BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails).build());

                    BillingResult launchBillingFlow = billingClient.launchBillingFlow(mainActivity,
                        BillingFlowParams.newBuilder().setProductDetailsParamsList(arrayList2)
                            .build()
                    );

                    if (launchBillingFlow.getResponseCode() != 0) {
                        Log.e("IAPManager",
                            "Error during call of store: Error = " + launchBillingFlow.getResponseCode()
                        );
                    }
                }
            }
        );
    }

    void RequestAIPPurchasedList() {
        if (!billingClient.isReady() || !isReady) {
            return;
        }

        billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType("inapp")
                .build(),
            (billingResult, purchases) -> {
                for (Purchase purchase : purchases) {
                    if (purchase.getPurchaseState() != 1) {
                        continue;
                    }

                    Log.d("IAPManager",
                        "Product[0]:" + purchase.getProducts()
                            .get(0) + " PurchaseToken:" + purchase.getPurchaseToken() + " PackageName:" + purchase.getPackageName()
                    );

                    purchasedList.put(purchase.getPackageName(), purchase);

                    SharedActivity.nativeSendGUIStringEx(SharedActivity.MESSAGE_TYPE_IAP_PURCHASED_LIST_STATE,
                        0,
                        0,
                        0,
                        purchase.getPackageName() + "|" + purchase.getOriginalJson() + "|" + purchase.getSignature()
                    );
                }

                SharedActivity.nativeSendGUIEx(SharedActivity.MESSAGE_TYPE_IAP_PURCHASED_LIST_STATE,
                    -1,
                    0,
                    0
                );
            }
        );
    }

    void ConsumeItem(String itemId) {
        if (!billingClient.isReady() || !isReady) {
            return;
        }

        if (itemId == null || itemId.isEmpty()) {
            return;
        }

        if (!purchasedList.containsKey(itemId)) {
            return;
        }

        if (purchasedList.get(itemId) == null) {
            return;
        }

        billingClient.consumeAsync(
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchasedList.get(itemId).getPurchaseToken()).build(),
            (billingResult, s) -> {
                if (billingResult.getResponseCode() == 0) {
                    IAPManager.this.purchasedList.remove(itemId);
                }
            }
        );
    }

    void RequestItemDetails(String item) {
        if (!billingClient.isReady() || !isReady) {
            return;
        }

        if (item == null || item.isEmpty()) {
            return;
        }

        try {
            ArrayList<QueryProductDetailsParams.Product> arrayList = new ArrayList<>();
            arrayList.add(QueryProductDetailsParams.Product.newBuilder().setProductId(item)
                .setProductType("inapp").build());

            billingClient.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder()
                    .setProductList(arrayList).build(),
                (billingResult, productDetailsList) -> {
                    if (billingResult.getResponseCode() != 0) {
                        return;
                    }

                    String str = "";
                    for (ProductDetails productDetails : productDetailsList) {
                        if (productDetails.getOneTimePurchaseOfferDetails() == null) {
                            continue;
                        }

                        str = productDetails.getProductId() + "," +
                            productDetails.getOneTimePurchaseOfferDetails()
                                .getPriceCurrencyCode() + "," +
                            productDetails.getOneTimePurchaseOfferDetails()
                                .getFormattedPrice().replaceAll("[A-Za-z]", "");
                    }

                    if (str.isEmpty()) {
                        return;
                    }

                    SharedActivity.nativeSendGUIStringEx(54, 0, 0, 0, str);
                }
            );
        } catch (Exception e) {
            Log.d("Get Item Info", "Failed : " + e.getMessage());
        }
    }
}