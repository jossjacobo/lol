package joss.jacobo.lol.lcs.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import joss.jacobo.lol.lcs.R;
import joss.jacobo.lol.lcs.model.DrawerItemModel;
import joss.jacobo.lol.lcs.views.DrawerItem;
import joss.jacobo.lol.lcs.views.DrawerItemSectionTitle;


public class MainActivity extends BaseActivity{

    private static final String FRAGMENT_TAG = "fragment_tag";
    private static final String CURRENT_FRAG = "current_frag";

    public FrameLayout contentView;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private MenuListAdapter adapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private int currentFrag = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contentView = (FrameLayout) findViewById(R.id.content_container);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.drawer_list_view);

        setUpDrawerLayout();
        setupActionBar();

        if (savedInstanceState != null)
            currentFrag = savedInstanceState.getInt(CURRENT_FRAG);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment == null) {
//            selectFragment(R.id.fragment_location_finder, 0);
        }

        Log.e("MainActivity", datastore.getVersion() + "");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen((View) mDrawerList.getParent());
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(!drawerOpen);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.isDrawerIndicatorEnabled()) {
            if (mDrawerToggle.onOptionsItemSelected(item)) {
                return true;
            }
        }
        // Handle your other action bar items...
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void setUpDrawerLayout() {
//        mDrawerLayout.setScrimColor(Color.TRANSPARENT);
        adapter = new MenuListAdapter(this, getDrawerItems());
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private List<DrawerItemModel> getDrawerItems() {
        List<DrawerItemModel> items = new ArrayList<DrawerItemModel>();

        items.add(new DrawerItemModel(DrawerItemModel.TYPE_SECTION_TITLE, 0, "LIVE"));
        items.add(new DrawerItemModel(DrawerItemModel.TYPE_LIVESTREAM, 0, "Livesteam"));
        items.add(new DrawerItemModel(DrawerItemModel.TYPE_LIVETICKER, 0, "Liveticker"));

        items.add(new DrawerItemModel(DrawerItemModel.TYPE_SECTION_TITLE, 0, "GENERAL"));
        items.add(new DrawerItemModel(DrawerItemModel.TYPE_OVERVIEW, 0, "Overview"));
        items.add(new DrawerItemModel(DrawerItemModel.TYPE_NEWS, 0, "News"));
        items.add(new DrawerItemModel(DrawerItemModel.TYPE_SCHEDULE_RESULTS, 0, "Schedule & Results"));
        items.add(new DrawerItemModel(DrawerItemModel.TYPE_STANDINGS, 0, "Standings"));

        items.add(new DrawerItemModel(DrawerItemModel.TYPE_SECTION_TITLE, 0, "Teams"));
        items.add(new DrawerItemModel(DrawerItemModel.TYPE_TEAM, 9, "Cloud 9"));
        items.add(new DrawerItemModel(DrawerItemModel.TYPE_TEAM, 10, "Counter Logic Gaming"));
        items.add(new DrawerItemModel(DrawerItemModel.TYPE_TEAM, 11, "Curse"));
        items.add(new DrawerItemModel(DrawerItemModel.TYPE_TEAM, 12, "Evil Geniuses"));
        items.add(new DrawerItemModel(DrawerItemModel.TYPE_TEAM, 13, "Complexity Black"));
        items.add(new DrawerItemModel(DrawerItemModel.TYPE_TEAM, 14, "Team Dignitas"));
        items.add(new DrawerItemModel(DrawerItemModel.TYPE_TEAM, 15, "Team Solomid"));
        items.add(new DrawerItemModel(DrawerItemModel.TYPE_TEAM, 16, "LMQ"));

        return items;
    }

    public class MenuListAdapter extends BaseAdapter {

        private Context context;
        private List<DrawerItemModel> items;
        private int hintPosition;

        public MenuListAdapter(Context c, List<DrawerItemModel> i) {
            this.context = c;
            this.items = i;
        }

        public void setItems(List<DrawerItemModel> items){
            this.items = items;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position){
            return items.get(position).type;
        }

        @Override
        public int getViewTypeCount(){
            return DrawerItemModel.TYPE_MAX;
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        public void setHint(int position) {
            hintPosition = position;
            notifyDataSetChanged();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            DrawerItemModel item = items.get(i);

            switch(item.type){
                case DrawerItemModel.TYPE_SECTION_TITLE:
                    DrawerItemSectionTitle drawerItemSectionTitle = view == null ? new DrawerItemSectionTitle(context) : (DrawerItemSectionTitle) view;
                    drawerItemSectionTitle.setContent(item);
                    drawerItemSectionTitle.setSelected(i == hintPosition);
                    return drawerItemSectionTitle;
                default:
                    DrawerItem drawerItem = view == null ? new DrawerItem(context) : (DrawerItem) view;
                    drawerItem.setContent(item);
                    drawerItem.title.setSelected(i == hintPosition);
                    return drawerItem;
            }
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position > 0) {

                //Compensate for headerView in position 0
                DrawerItemModel clicked = adapter.items.get(position);

                if(clicked.type != DrawerItemModel.TYPE_SECTION_TITLE)
                    adapter.setHint(position);

                switch (clicked.type){
                    case DrawerItemModel.TYPE_LIVESTREAM:

                        break;

                    case DrawerItemModel.TYPE_LIVETICKER:

                        break;

                    case DrawerItemModel.TYPE_OVERVIEW:

                        break;

                    case DrawerItemModel.TYPE_NEWS:

                        break;

                    case DrawerItemModel.TYPE_SCHEDULE_RESULTS:

                        break;

                    case DrawerItemModel.TYPE_STANDINGS:

                        break;

                    case DrawerItemModel.TYPE_TEAM:

                        break;
                }
            }
        }
    }

}
