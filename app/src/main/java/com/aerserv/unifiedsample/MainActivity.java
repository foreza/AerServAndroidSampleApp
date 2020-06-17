package com.aerserv.unifiedsample;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.aerserv.sdk.AerServBanner;
import com.aerserv.sdk.AerServConfig;
import com.aerserv.sdk.AerServEvent;
import com.aerserv.sdk.AerServEventListener;
import com.aerserv.sdk.AerServInterstitial;
import com.aerserv.sdk.AerServSdk;
import com.aerserv.sdk.AerServTransactionInformation;
import com.aerserv.sdk.AerServVirtualCurrency;


import com.inmobi.sdk.InMobiSdk;
import com.inmobi.sdk.SdkInitializationListener;

import java.math.BigDecimal;
import java.util.List;


public class MainActivity extends Activity {
    private static final String LOG_TAG = "9XX Mediation Sample";

    private AerServBanner banner;
    private AerServInterstitial interstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView version = (TextView) findViewById(R.id.sdkVersion);
        version.setText("v" + InMobiSdk.getVersion());

        InMobiSdk.setLogLevel(InMobiSdk.LogLevel.DEBUG);

        // 1022221: SE/Publisher test app id
        // 1021436: MSFT Android test app id




        AerServSdk.init(this, "1021436", new SdkInitializationListener() {
            @Override
            public void onInitializationComplete(@Nullable Error error) {
                // Handle this!
            }
        });
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
        Switch a9Switch = (Switch) findViewById(R.id.mrecSwitch);

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

                                Log.d(LOG_TAG, "PROFILE: AD_FAILED");


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

                                Log.d(LOG_TAG, "PROFILE: LOAD_TRANSACTION");


                                ti = (AerServTransactionInformation) args.get(0);
                                msg = "Load Transaction Information PLC has:"
                                    + "\n buyerName=" + ti.getBuyerName()
                                    + "\n buyerPrice=" + ti.getBuyerPrice();
                                break;
                            case SHOW_TRANSACTION:


                                Log.d(LOG_TAG, "PROFILE: SHOW_TRANSACTION");


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


            final AerServConfig config = new AerServConfig(this, getPlc())
                    .setEventListener(interstitialListener)
//                    .setDebug(true)
                    .setPreload(preloadSwitch.isChecked());
            interstitial = new AerServInterstitial(config);

            if(!preloadSwitch.isChecked()) {
                interstitial.show();
            }

    }

    public void showInterstitial(View view) {
        findViewById(R.id.showInterstitial).setVisibility(View.INVISIBLE);
        if (interstitial != null) {
            interstitial.show();
            interstitial.pause();

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

    AerServEventListener bannerListener = new AerServEventListener() {
        @Override
        public void onAerServEvent(AerServEvent event, List params) {
            switch (event) {
                case PRELOAD_READY:
                    // Bid is available
                    break;
                case LOAD_TRANSACTION:
                    // Get bid info here
                    AerServTransactionInformation ti = (AerServTransactionInformation) params.get(0);
                    String buyerName = ti.getBuyerName();
                    BigDecimal buyerPrice = ti.getBuyerPrice();
                    break;
                case AD_LOADED:
                    // Execute some code when AD_LOADED event occurs.
                    break;
                case AD_DISMISSED:
                    // Execute some code when AD_DISMISSED event occurs.
                    break;
                case AD_FAILED:
                    // Execute some code when AD_FAILED event occurs.
                    break;
            }
        }
    };

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

        if (banner != null) {
            banner.kill();
        }

        final Switch preloadSwitch = (Switch) findViewById(R.id.preloadSwitch);
        final Switch mrecSwitch = (Switch) findViewById(R.id.mrecSwitch);



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
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                        Log.d(LOG_TAG, msg);
                    }
                });
            }
        };

        final AerServConfig config = new AerServConfig(getApplicationContext(), getPlc())
                .setEventListener(bannerListener)
                .setPreload(preloadSwitch.isChecked())
                .setRefreshInterval(30);

        if (mrecSwitch.isChecked()){
            banner = (AerServBanner) findViewById(R.id.mrec);
        } else {
            banner = (AerServBanner) findViewById(R.id.banner);

        }
        banner.configure(config);
        if (!preloadSwitch.isChecked()) {
            banner.show();
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
