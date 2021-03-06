package joss.jacobo.lol.lcs;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.multidex.MultiDex;

import com.bugsnag.android.Bugsnag;

import javax.inject.Inject;

import dagger.ObjectGraph;
import joss.jacobo.lol.lcs.dagger.AppModule;
import joss.jacobo.lol.lcs.dagger.IObjectGraph;
import oak.util.PRNGFixes;

public class MainApp extends Application implements IObjectGraph {

    public static String TAG = "lol-android";
    private static ObjectGraph applicationGraph;
    private static Context sContext;

    @Inject
    Datastore mDataStore;

    public static Context getContext() {
        return sContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (!BuildConfig.DEBUG)
            Bugsnag.register(this, "3f3a012ad7eb6703e8b327f33ae37abd");

        /**
         * Ensure the keys generated for the CryptoSharedPreferences are strong and sufficiently
         * random.
         *
         * See http://android-developers.blogspot.com/2013/08/some-securerandom-thoughts.html
         */
        PRNGFixes.apply();

        sContext = this;
        applicationGraph = ObjectGraph.create(getAppModule());
        applicationGraph.inject(this);
        try {
            int newVersionCode = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionCode;
            int oldVersionCode = mDataStore.getVersion();
            if (oldVersionCode != 0 && oldVersionCode != newVersionCode) {
                onVersionUpdate(oldVersionCode, newVersionCode);
            }
            mDataStore.persistVersion(newVersionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void attachBaseContext(Context base){
        super.attachBaseContext(base);

        MultiDex.install(base);
    }

    private void onVersionUpdate(int oldVersionCode, int newVersionCode) {
        //this method is called when the version code changes, use comparison operators to control migration
    }

    protected Object getAppModule() {
        return new AppModule(this);
    }

    @Override
    public ObjectGraph getObjectGraph() {
        return applicationGraph;
    }

    public void inject(Object object) {
        applicationGraph.inject(object);
    }
}

