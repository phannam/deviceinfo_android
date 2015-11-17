package info.device.deviceinfo.activities;


import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import info.device.deviceinfo.R;
import info.device.deviceinfo.fragments.BaseFragment;
import info.device.deviceinfo.fragments.DeviceFragment;
import info.device.deviceinfo.fragments.DisplayFragment;
import info.device.deviceinfo.utils.AppManager;
import info.device.deviceinfo.utils.Constant;
import info.device.deviceinfo.views.SlidingFragmentTab;
import info.device.deviceinfo.views.SlidingTab;
import info.device.deviceinfo.views.SlidingTabRenderer;

/**
 * Created by namphan on 11/16/15.
 */
public class MainActivity extends TabActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Info");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.actionbar_bg_color)));

        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        // Fetch Alerts to notify
        initTabs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_share:
                checkExistingEmailAccountAndSendEmail();
                break;
            default:
                break;
        }

        return true;
    }

    private void checkExistingEmailAccountAndSendEmail() {
        Intent filterEmailIntent = new Intent(Intent.ACTION_SENDTO);
        String emailData = "mailto:" + "namphanbk@gmail.com" +
                "?subject=" + Uri.encode(Build.DISPLAY + "information") +
                "&body=" + Uri.encode("");
        filterEmailIntent.setData(Uri.parse(emailData));

        PackageManager pm = getPackageManager();
        List<ResolveInfo> infoList = pm.queryIntentActivities(filterEmailIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
        if (infoList.size() > 0) {
            if (infoList.size() == 1) {
                ActivityInfo targetActivityInfo = infoList.get(0).activityInfo;
                sendEmail(targetActivityInfo);
            } else {
                sendEmail(null);
            }
        } else {
            Toast.makeText(this, "No email client!", Toast.LENGTH_LONG).show();
        }
    }

    private void sendEmail(ActivityInfo targetActivityInfo) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, "");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, Build.MODEL + " information");
        emailIntent.putExtra(Intent.EXTRA_TEXT, AppManager.getInstance().getAllInfos());
        if (targetActivityInfo != null) {
            emailIntent.setComponent(new ComponentName(targetActivityInfo.packageName, targetActivityInfo.name));
        }
        try {
            startActivity(emailIntent);
        } catch (Exception ex) {
            Toast.makeText(this, "Application not found!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void initTabs() {
        SlidingTab slidingTab;
        slidingTabs = new LinkedList<>();
        slidingTab = new SlidingTab(Constant.Tab.TAB_DEVICEINFO, R.mipmap.ic_info_normal, "Device",
                getResources().getColor(R.color.tab_normal_color));
        slidingTab.setSelectedResource(R.mipmap.ic_info_selected,
                getResources().getColor(R.color.tab_selected_color));
        slidingTabs.add(slidingTab);

        slidingTab = new SlidingTab(Constant.Tab.TAB_DISPLAY, R.mipmap.ic_display_normal, "Display",
                getResources().getColor(R.color.tab_normal_color));
        slidingTab.setSelectedResource(R.mipmap.ic_display_selected,
                getResources().getColor(R.color.tab_selected_color));
        slidingTabs.add(slidingTab);

        slidingTabRenderer = new SlidingTabRenderer(slidingTabs, this);

        int tabType = Constant.Tab.TAB_DEVICEINFO;
        currentTab = getTabWithTypeOrFirstTab(tabType);
        mSlidingFragmentTab = (SlidingFragmentTab) findViewById(R.id.sliding_tabs);
        mSlidingFragmentTab.setCustomTabView(R.layout.layout_tabview, R.id.tv_tabtitle, R.id.img_tabicon);
        mSlidingFragmentTab.setDividerColors(R.color.tab_device_color);
        mSlidingFragmentTab.selectTab(slidingTabs.indexOf(currentTab), slidingTabs.indexOf(currentTab));
        mSlidingFragmentTab.setTabItemRenderer(slidingTabRenderer);
        selectTab(currentTab, currentTab);
    }

    @Override
    protected BaseFragment getFragment(SlidingTab slidingTab) {
        BaseFragment fragment;
        switch (slidingTab.getType()) {
            case Constant.Tab.TAB_DEVICEINFO:
                fragment = new DeviceFragment();
                break;
            default:
                fragment = new DisplayFragment();
                break;
        }
        return fragment;
    }

    @Override
    protected int getFragmentContainer() {
        return R.id.fragment_container;
    }
}
