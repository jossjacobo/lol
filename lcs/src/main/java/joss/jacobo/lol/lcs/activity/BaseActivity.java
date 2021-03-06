package joss.jacobo.lol.lcs.activity;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import javax.inject.Inject;

import butterknife.InjectView;
import joss.jacobo.lol.lcs.Datastore;
import joss.jacobo.lol.lcs.MainApp;
import joss.jacobo.lol.lcs.R;
import joss.jacobo.lol.lcs.purchaseUtils.PurchaseConstants;
import joss.jacobo.lol.lcs.purchaseUtils.IabHelper;
import joss.jacobo.lol.lcs.purchaseUtils.IabResult;
import joss.jacobo.lol.lcs.purchaseUtils.Inventory;
import joss.jacobo.lol.lcs.purchaseUtils.Purchase;
import joss.jacobo.lol.lcs.views.ToolbarTitle;

/**
 * Created by jossayjacobo on 7/20/14
 */
public abstract class BaseActivity extends ActionBarActivity{

    @Inject
    Datastore datastore;

    @InjectView(R.id.toolbar)
    public Toolbar toolbar;

    IabHelper mHelper;

    @Override
    public void onCreate(Bundle savedState){
        super.onCreate(savedState);
        ((MainApp) getApplication()).inject(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = this.getWindow();

        // Eliminates color banding
        window.setFormat(PixelFormat.RGBA_8888);
    }

    protected void setupActionBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    public void setToolbarTitle(String title) {
        setTitle(title, null);
    }

    public void setToolbarTitleAndSubtitle(String title, String subtitle) {
        setTitle(title, subtitle);
    }

    public void setTitle(String title, String subtitle){
        if (getSupportActionBar() != null && title != null){

            if (toolbar == null){
                return;
            }

            ToolbarTitle toolbarTitle = (ToolbarTitle) toolbar.findViewById(R.id.toolbar_title);
            toolbarTitle.setContent(title, subtitle);
        }
    }
}
