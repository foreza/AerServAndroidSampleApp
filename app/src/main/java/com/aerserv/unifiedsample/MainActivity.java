package com.aerserv.unifiedsample;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAppOptions;
import com.aerserv.sdk.AerServBanner;
import com.aerserv.sdk.AerServConfig;
import com.aerserv.sdk.AerServEvent;
import com.aerserv.sdk.AerServEventListener;
import com.aerserv.sdk.AerServInterstitial;
import com.aerserv.sdk.AerServTransactionInformation;
import com.aerserv.sdk.AerServVirtualCurrency;
import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdRegistration;
import com.amazon.device.ads.DTBAdCallback;
import com.amazon.device.ads.DTBAdLoader;
import com.amazon.device.ads.DTBAdRequest;
import com.amazon.device.ads.DTBAdResponse;
import com.amazon.device.ads.DTBAdSize;
import com.inmobi.sdk.InMobiSdk;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    private static final String LOG_TAG = "UnifiedSampleApp";

    private AerServBanner banner;
    private AerServInterstitial interstitial;

    private static final String APP_KEY = "a9_onboarding_app_id";
    private static final String SLOT_320x50 = "54fb2d08-c222-40b1-8bbe-4879322dc04b";
    private static final String SLOT_INTERSTITIAL = "4e918ac0-5c68-4fe1-8d26-4e76e8f74831";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView version = (TextView) findViewById(R.id.sdkVersion);
        version.setText("v" + InMobiSdk.getVersion());

        // To pre-initialize mediation adapters, uncomment the following line and change to your site ID
        // AerServSdk.init(this, "101190");

    }
    
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(banner != null){
            banner.kill();
        }
    }

    public void loadInterstitial(View view) {

        final Switch preloadSwitch = (Switch) findViewById(R.id.preloadSwitch);
        Switch a9Switch = (Switch) findViewById(R.id.a9Switch);

        findViewById(R.id.showInterstitial).setVisibility(View.INVISIBLE);

        final AerServEventListener interstitialListener = new AerServEventListener(){
            @Override
            public void onAerServEvent(final AerServEvent event, final List<Object> args){
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String msg = null;
                        AerServVirtualCurrency vc = null;
                        AerServTransactionInformation ti = null;
                        switch (event) {
                            case PRELOAD_READY:
                                msg = "PRELOAD_READY event fired with args: " + args.toString();
                                findViewById(R.id.showInterstitial).setVisibility(View.VISIBLE);
                                break;
                            case AD_FAILED:
                                if (args.size() > 1) {
                                    Integer adFailedCode =
                                        (Integer) args.get(AerServEventListener.AD_FAILED_CODE);
                                    String adFailedReason =
                                        (String) args.get(AerServEventListener.AD_FAILED_REASON);
                                    msg = "Ad failed with code=" + adFailedCode + ", reason=" + adFailedReason;
                                } else {
                                    msg = "Ad Failed with message: " + args.get(0).toString();
                                }
                                break;
                            case VC_READY:
                                vc = (AerServVirtualCurrency) args.get(0);
                                msg = "Virtual Currency PLC has loaded:"
                                    + "\n name=" + vc.getName()
                                    + "\n amount=" + vc.getAmount().toString()
                                    + "\n buyerName=" + vc.getBuyerName()
                                    + "\n buyerPrice=" + vc.getBuyerPrice();
                                break;
                            case VC_REWARDED:
                                vc = (AerServVirtualCurrency) args.get(0);
                                msg = "Virtual Currency PLC has rewarded:"
                                    + "\n name=" + vc.getName()
                                    + "\n amount=" + vc.getAmount().toString()
                                    + "\n buyerName=" + vc.getBuyerName()
                                    + "\n buyerPrice=" + vc.getBuyerPrice();
                                break;
                            case LOAD_TRANSACTION:
                                ti = (AerServTransactionInformation) args.get(0);
                                msg = "Load Transaction Information PLC has:"
                                    + "\n buyerName=" + ti.getBuyerName()
                                    + "\n buyerPrice=" + ti.getBuyerPrice();
                                break;
                            case SHOW_TRANSACTION:
                                ti = (AerServTransactionInformation) args.get(0);
                                msg = "Show Transaction Information PLC has:"
                                    + "\n buyerName=" + ti.getBuyerName()
                                    + "\n buyerPrice=" + ti.getBuyerPrice();
                                break;
                            default:
                                msg = event.toString() + " event fired with args: " + args.toString();
                        }
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        Log.d(LOG_TAG, msg);
                    }
                });
            }
        };

        if (a9Switch.isChecked()) {
            AdRegistration.getInstance(APP_KEY, this);
            AdRegistration.enableLogging(true);
            AdRegistration.enableTesting(true);
            AdRegistration.useGeoLocation(true);

            DTBAdLoader adLoader = new DTBAdRequest();
            adLoader.setSizes(new DTBAdSize.DTBInterstitialAdSize(SLOT_INTERSTITIAL));
            adLoader.loadAd(new DTBAdCallback() {
                @Override
                public void onFailure(AdError adError) {
                    Log.e("A9", "Failed to get the interstitial ad from Amazon: " + adError.getMessage());
                    final AerServConfig config = new AerServConfig(MainActivity.this, getPlc())
                        .setAPSAdResponses(null)
                        .setEventListener(interstitialListener)
                        .setPreload(preloadSwitch.isChecked());
                    interstitial = new AerServInterstitial(config);
                    if(!preloadSwitch.isChecked()) {
                        interstitial.show();
                    }
                }

                @Override
                public void onSuccess(DTBAdResponse dtbAdResponse) {
                    List<DTBAdResponse> responses = new ArrayList<DTBAdResponse>();
                    responses.add(dtbAdResponse);

                    Log.i("A9", "Successfully get " + dtbAdResponse.getDTBAds().size() + " interstitial ad from Amazon");

                    final AerServConfig config = new AerServConfig(MainActivity.this, getPlc())
                            .setAPSAdResponses(responses)
                            .setEventListener(interstitialListener)
                            .setPreload(preloadSwitch.isChecked());
                    interstitial = new AerServInterstitial(config);
                    if(!preloadSwitch.isChecked()) {
                        interstitial.show();
                    }
                }
            });

        } else {
            final AerServConfig config = new AerServConfig(this, getPlc())
                    .setEventListener(interstitialListener)
                    .setPreload(preloadSwitch.isChecked());
            interstitial = new AerServInterstitial(config);

            if(!preloadSwitch.isChecked()) {
                interstitial.show();
            }
        }
    }

    public void showInterstitial(View view) {
        findViewById(R.id.showInterstitial).setVisibility(View.INVISIBLE);
        if (interstitial != null) {
            interstitial.show();
        }
    }

    public void pauseBanner(View view) {
        findViewById(R.id.loadBanner).setVisibility(View.INVISIBLE);
        findViewById(R.id.showBanner).setVisibility(View.INVISIBLE);
        findViewById(R.id.killBanner).setVisibility(View.VISIBLE);
        findViewById(R.id.pauseBanner).setVisibility(View.INVISIBLE);
        findViewById(R.id.playBanner).setVisibility(View.VISIBLE);
        if (banner != null) {
            banner.pause();
        }
    }

    public void playBanner(View view) {
        findViewById(R.id.loadBanner).setVisibility(View.INVISIBLE);
        findViewById(R.id.showBanner).setVisibility(View.INVISIBLE);
        findViewById(R.id.killBanner).setVisibility(View.VISIBLE);
        findViewById(R.id.pauseBanner).setVisibility(View.VISIBLE);
        findViewById(R.id.playBanner).setVisibility(View.INVISIBLE);
        if (banner != null) {
            banner.play();
        }
    }

    public void loadBanner(View view) {
        final Switch preloadSwitch = (Switch) findViewById(R.id.preloadSwitch);
        final Switch a9Switch = (Switch) findViewById(R.id.a9Switch);

        findViewById(R.id.loadBanner).setVisibility(View.VISIBLE);
        findViewById(R.id.showBanner).setVisibility(View.INVISIBLE);
        findViewById(R.id.killBanner).setVisibility(View.INVISIBLE);
        findViewById(R.id.pauseBanner).setVisibility(View.INVISIBLE);
        findViewById(R.id.playBanner).setVisibility(View.INVISIBLE);
        final AerServEventListener bannerListener = new AerServEventListener(){
            @Override
            public void onAerServEvent(final AerServEvent event, final List<Object> args){
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String msg = null;
                        AerServVirtualCurrency vc = null;
                        AerServTransactionInformation ti = null;
                        switch (event) {
                            case PRELOAD_READY:
                                msg = "PRELOAD_READY event fired with args: " + args.toString();
                                findViewById(R.id.loadBanner).setVisibility(View.VISIBLE);
                                findViewById(R.id.showBanner).setVisibility(View.VISIBLE);
                                findViewById(R.id.killBanner).setVisibility(View.INVISIBLE);
                                findViewById(R.id.pauseBanner).setVisibility(View.INVISIBLE);
                                findViewById(R.id.playBanner).setVisibility(View.INVISIBLE);
                                break;
                            case AD_LOADED:
                                msg = "AD_LOAD event fired with args: " + args.toString();
                                findViewById(R.id.loadBanner).setVisibility(View.INVISIBLE);
                                findViewById(R.id.showBanner).setVisibility(View.INVISIBLE);
                                findViewById(R.id.killBanner).setVisibility(View.VISIBLE);
                                findViewById(R.id.pauseBanner).setVisibility(View.VISIBLE);
                                findViewById(R.id.playBanner).setVisibility(View.INVISIBLE);
                                break;
                            case AD_FAILED:
                                if (args.size() > 1) {
                                    Integer adFailedCode =
                                        (Integer) args.get(AerServEventListener.AD_FAILED_CODE);
                                    String adFailedReason =
                                        (String) args.get(AerServEventListener.AD_FAILED_REASON);
                                    msg = "Ad failed with code=" + adFailedCode + ", reason=" + adFailedReason;
                                } else {
                                    msg = "Ad Failed with message: " + args.get(0).toString();
                                }
                                break;
                            case VC_READY:
                                vc = (AerServVirtualCurrency) args.get(0);
                                msg = "Virtual Currency PLC has loaded:"
                                    + "\n name=" + vc.getName()
                                    + "\n amount=" + vc.getAmount().toString()
                                    + "\n buyerName=" + vc.getBuyerName()
                                    + "\n buyerPrice=" + vc.getBuyerPrice();
                                break;
                            case VC_REWARDED:
                                vc = (AerServVirtualCurrency) args.get(0);
                                msg = "Virtual Currency PLC has rewarded:"
                                    + "\n name=" + vc.getName()
                                    + "\n amount=" + vc.getAmount().toString()
                                    + "\n buyerName=" + vc.getBuyerName()
                                    + "\n buyerPrice=" + vc.getBuyerPrice();
                                break;
                            case LOAD_TRANSACTION:
                                ti = (AerServTransactionInformation) args.get(0);
                                msg = "Load Transaction Information PLC has:"
                                    + "\n buyerName=" + ti.getBuyerName()
                                    + "\n buyerPrice=" + ti.getBuyerPrice();
                                break;
                            case SHOW_TRANSACTION:
                                ti = (AerServTransactionInformation) args.get(0);
                                msg = "Show Transaction Information PLC has:"
                                    + "\n buyerName=" + ti.getBuyerName()
                                    + "\n buyerPrice=" + ti.getBuyerPrice();
                                break;
                            default:
                                msg = event.toString() + " event fired with args: " + args.toString();
                        }
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        Log.d(LOG_TAG, msg);
                    }
                });
            }
        };

        if (a9Switch.isChecked()) {
            AdRegistration.getInstance(APP_KEY, this);
            AdRegistration.enableLogging(true);
            AdRegistration.enableTesting(true);
            AdRegistration.useGeoLocation(true);

            final DTBAdRequest loader = new DTBAdRequest();
            loader.setSizes(new DTBAdSize(300, 250, SLOT_320x50));

            //Send ad request to Amazon
            loader.loadAd(new DTBAdCallback() {
                @Override
                public void onFailure(AdError adError) {
                    Log.e("A9", "Failed to get banner ad from Amazon: " + adError.getMessage());
                    final AerServConfig config = new AerServConfig(MainActivity.this, getPlc())
                        .setAPSAdResponses(null)
                        .setEventListener(bannerListener)
                        .setPreload(preloadSwitch.isChecked())
                        .setRefreshInterval(60);
                    banner = (AerServBanner) findViewById(R.id.banner);
                    banner.configure(config);
                    if (!preloadSwitch.isChecked()) {
                        banner.show();
                    }
                }

                @Override
                public void onSuccess(DTBAdResponse dtbAdResponse) {
                    List<DTBAdResponse> responses = new ArrayList<DTBAdResponse>();
                    responses.add(dtbAdResponse);
                    Log.i("A9", "Successfully get " + dtbAdResponse.getDTBAds().size()
                        + " banner ad from Amazon");

                    final AerServConfig config = new AerServConfig(MainActivity.this, getPlc())
                        .setAPSAdResponses(responses)
                        .setEventListener(bannerListener)
                        .setPreload(preloadSwitch.isChecked())
                        .setRefreshInterval(60);
                    banner = (AerServBanner) findViewById(R.id.banner);
                    banner.configure(config);
                    if (!preloadSwitch.isChecked()) {
                        banner.show();
                    }
                }
            });
        } else {
            final AerServConfig config = new AerServConfig(MainActivity.this, getPlc())
                .setEventListener(bannerListener)
                .setPreload(preloadSwitch.isChecked())
                .setRefreshInterval(60);
            banner = (AerServBanner) findViewById(R.id.banner);
            banner.configure(config);
            if (!preloadSwitch.isChecked()) {
                banner.show();
            }
        }
    }

    public void showBanner(View view) {
        findViewById(R.id.loadBanner).setVisibility(View.INVISIBLE);
        findViewById(R.id.showBanner).setVisibility(View.INVISIBLE);
        findViewById(R.id.killBanner).setVisibility(View.VISIBLE);
        findViewById(R.id.pauseBanner).setVisibility(View.VISIBLE);
        findViewById(R.id.playBanner).setVisibility(View.INVISIBLE);
        if (banner != null) {
            banner.show();
        }
    }

    public void killBanner(View view) {
        findViewById(R.id.loadBanner).setVisibility(View.VISIBLE);
        findViewById(R.id.showBanner).setVisibility(View.INVISIBLE);
        findViewById(R.id.killBanner).setVisibility(View.INVISIBLE);
        findViewById(R.id.pauseBanner).setVisibility(View.INVISIBLE);
        findViewById(R.id.playBanner).setVisibility(View.INVISIBLE);
        if (banner != null) {
            banner.kill();
        }
    }

    private String getPlc() {
        EditText plcEditText = (EditText) findViewById(R.id.plcET);
        return plcEditText.getText().toString();
    }
}
