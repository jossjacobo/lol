package joss.jacobo.lol.lcs.views;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import javax.inject.Inject;

import joss.jacobo.lol.lcs.Datastore;
import joss.jacobo.lol.lcs.MainApp;
import joss.jacobo.lol.lcs.R;

/**
 * Created by jossayjacobo on 10/13/14
 */
public class CancelableAdView extends RelativeLayout implements View.OnClickListener {

    @Inject
    Datastore datastore;

    Context context;
    ImageView cancel;
    AdView mAdView;

    public CancelableAdView(Context context) {
        this(context, null);
    }

    public CancelableAdView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CancelableAdView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        ((MainApp) ((Activity) context).getApplication()).inject(this);
        LayoutInflater.from(context).inflate(R.layout.view_cancelable_adview, this, true);

        cancel = (ImageView) findViewById(R.id.cancel);
        mAdView = (AdView) findViewById(R.id.ads);

        cancel.setOnClickListener(this);
    }

    public void initAds(){

        switch (GooglePlayServicesUtil.isGooglePlayServicesAvailable(context)){
            case ConnectionResult.SUCCESS:
                if(datastore.isAdsFree()){
                    setVisibility(GONE);
                }else{
                    AdRequest adRequest = new AdRequest.Builder().build();
                    mAdView.loadAd(adRequest);
                }
                break;
            default:
                setVisibility(GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cancel:
                hide();
                break;
        }
    }

    private void hide() {
        CancelableAdView.this.animate().translationY(getMeasuredHeight()).setDuration(500).alpha(0.5f).start();
    }
}
