/*
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * 
 */
package com.odoo;

import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.odoo.auth.OdooAccountManager;
import com.odoo.base.about.AboutFragment;
import com.odoo.base.account.AccountsDetail;
import com.odoo.base.account.UserProfile;
import com.odoo.base.ir.IrModel;
import com.odoo.base.login_signup.AccountCreate;
import com.odoo.base.login_signup.LoginSignup;
import com.odoo.crm.R;
import com.odoo.support.OUser;
import com.odoo.support.fragment.BaseFragment;
import com.odoo.support.fragment.FragmentListener;
import com.odoo.util.Base64Helper;
import com.odoo.util.OAppRater;
import com.odoo.util.PreferenceManager;
import com.odoo.util.drawer.DrawerAdatper;
import com.odoo.util.drawer.DrawerHelper;
import com.odoo.util.drawer.DrawerItem;
import com.odoo.util.drawer.DrawerListener;
import com.openerp.OETouchListener;

/**
 * The Class MainActivity.
 */
public class MainActivity extends FragmentActivity implements
		DrawerItem.DrawerItemClickListener, FragmentListener, DrawerListener {

	private static final String TAG = "com.odoo.MainActivity";
	private static final int RESULT_SETTINGS = 1;
	private Context mContext = null;

	private DrawerLayout mDrawerLayout = null;
	private ActionBarDrawerToggle mDrawerToggle = null;
	private List<DrawerItem> mDrawerListItems = new ArrayList<DrawerItem>();
	private DrawerAdatper mDrawerAdatper = null;
	private String mAppTitle = "", mDrawerTitle = "", mDrawerSubtitle = "";
	private int mDrawerItemSelectedPosition = -1;
	private ListView mDrawerListView = null;
	private boolean mNewFragment = false;

	private FragmentManager mFragment = null;

	private enum SettingKeys {
		GLOBAL_SETTING, PROFILE, ACCOUNTS, ABOUT_US
	}

	private CharSequence mTitle;
	private OETouchListener mTouchAttacher;
	private boolean mTwoPane;

	private OUser mAccount = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getActionBar().setIcon(R.drawable.ic_odoo_o);
		mContext = this;
		mFragment = getSupportFragmentManager();
		if (OUser.current(mContext) != null && savedInstanceState == null) {
			IrModel models = new IrModel(mContext);
			if (models.count() <= 0) {
				// Database cleaned so re-creating database
				updateAccount();
			} else {
				onTaskDone(savedInstanceState);
			}
		} else {
			onTaskDone(savedInstanceState);
		}
	}

	private void updateAccount() {
		AccountCreate account = new AccountCreate();
		Bundle args = new Bundle();
		args.putBoolean("no_config_wizard", true);
		args.putAll(OUser.current(mContext).getAsBundle());
		account.setArguments(args);
		startMainFragment(account, false);
	}

	public void onTaskDone(Bundle savedInstanceState) {
		initTouchListener();
		initDrawerControls();
		if (findViewById(R.id.fragment_detail_container) != null) {
			findViewById(R.id.fragment_detail_container).setVisibility(
					View.GONE);
			mTwoPane = true;
		}
		if (savedInstanceState != null) {
			mDrawerItemSelectedPosition = savedInstanceState
					.getInt("current_drawer_item");
			if (OdooAccountManager.isAnyUser(mContext)) {
				setDrawerItems();
				initDrawerListeners();
			}
			return;
		}
		init();
	}

	private void checkForRateApplication() {
		OAppRater.app_launched(this);
	}

	private void init() {
		Log.d(TAG, "MainActivity->init()");
		initDrawerControls();
		boolean reqForNewAccount = getIntent().getBooleanExtra(
				"create_new_account", false);
		/**
		 * checks for available account related to Odoo
		 */
		if (!OdooAccountManager.hasAccounts(this) || reqForNewAccount) {
			getActionBar().setDisplayHomeAsUpEnabled(false);
			getActionBar().setHomeButtonEnabled(false);
			lockDrawer(true);
			LoginSignup loginSignUp = new LoginSignup();
			startMainFragment(loginSignUp, false);

		} else {
			lockDrawer(false);
			/**
			 * User found but not logged in. Requesting for login with available
			 * accounts.
			 */
			if (!OdooAccountManager.isAnyUser(mContext)) {
				accountSelectionDialog(
						OdooAccountManager.fetchAllAccounts(mContext)).show();
			} else {
				initDrawer();
			}
		}
		checkForRateApplication();
	}

	private void initTouchListener() {
		OETouchListener.DEFAULT_HEADER_LAYOUT = R.layout.default_header;
		OETouchListener.DEFAULT_ANIM_HEADER_IN = R.anim.fade_in;
		OETouchListener.DEFAULT_ANIM_HEADER_OUT = R.anim.fade_out;
		OETouchListener.ptr_progress = R.id.ptr_progress;
		OETouchListener.ptr_text = R.id.ptr_text;
		OETouchListener.refresh_pull_label = R.string.pull_to_refresh_pull_label;
		OETouchListener.refreshing_label = R.string.pull_to_refresh_refreshing_label;
		OETouchListener.release_label = R.string.pull_to_refresh_release_label;
		OETouchListener.contentView = R.id.ptr_content;
		OETouchListener.opaqueBackground = R.id.ptr_text_opaque_bg;
		mTouchAttacher = new OETouchListener(this);
	}

	private void initDrawerControls() {
		Log.d(TAG, "MainActivity->initDrawerControls()");
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerListView = (ListView) findViewById(R.id.left_drawer);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_navigation_drawer, R.string.drawer_open,
				R.string.app_name) {

			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				getActionBar().setIcon(R.drawable.ic_odoo_o);
				setTitle(mAppTitle, null);
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				setTitle(R.string.app_name);
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	private void setDrawerHeader() {
		OUser user = OUser.current(mContext);
		mDrawerTitle = user.getName();
		mDrawerSubtitle = (user.isOAauthLogin()) ? user.getInstanceUrl() : user
				.getHost();
		View v = getLayoutInflater().inflate(R.layout.drawer_header,
				mDrawerListView, false);
		TextView mUserName, mUserURL;
		ImageView imgUserPic = (ImageView) v
				.findViewById(R.id.imgUserProfilePic);
		mUserName = (TextView) v.findViewById(R.id.txvUserProfileName);
		mUserURL = (TextView) v.findViewById(R.id.txvUserServerURL);
		mUserName.setText(mDrawerTitle);
		mUserURL.setText(mDrawerSubtitle);
		if (OUser.current(mContext) != null
				&& !OUser.current(mContext).getAvatar().equals("false")) {
			Bitmap profPic = Base64Helper.getRoundedCornerBitmap(this,
					Base64Helper.getBitmapImage(this, OUser.current(mContext)
							.getAvatar()), true);
			if (profPic != null)
				imgUserPic.setImageBitmap(profPic);
		}
		v.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onSettingItemSelected(SettingKeys.PROFILE);
				mDrawerLayout.closeDrawers();
			}
		});
		v.findViewById(R.id.imgBtnSetting).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						onSettingItemSelected(SettingKeys.GLOBAL_SETTING);
						mDrawerLayout.closeDrawers();
					}
				});
		mDrawerListView.addHeaderView(v, null, false);
	}

	private void setDrawerItems() {
		Log.d(TAG, "MainActivity->setDrawerItems()");
		if (OUser.current(mContext) != null)
			setDrawerHeader();
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		mDrawerListItems.addAll(DrawerHelper.drawerItems(mContext));
		mDrawerListItems.addAll(setSettingMenu());
		mDrawerAdatper = new DrawerAdatper(this, R.layout.drawer_item_layout,
				R.layout.drawer_item_group_layout, mDrawerListItems);
		mDrawerListView.setAdapter(mDrawerAdatper);
		mDrawerAdatper.notifyDataSetChanged();
		if (mDrawerItemSelectedPosition >= 0) {
			mDrawerListView.setItemChecked(mDrawerItemSelectedPosition, true);
		}
	}

	private int initDrawerListeners() {
		mDrawerListView.setOnItemClickListener(this);
		int position = 1;
		if (mDrawerListItems.size() > 0) {
			if (!mDrawerListItems.get(1).isGroupTitle()) {
				mDrawerListView.setItemChecked(1 + 1, true);
				position = 1;
			} else {
				mDrawerListView.setItemChecked(2 + 1, true);
				position = 2;
			}
		}
		if (mDrawerItemSelectedPosition >= 0) {
			position = mDrawerItemSelectedPosition;
		}
		mAppTitle = mDrawerListItems.get(position).getTitle();
		setTitle(mAppTitle);
		return position;
	}

	private void initDrawer() {
		Log.d(TAG, "MainActivity->initDrawer()");
		setDrawerItems();
		int position = initDrawerListeners();

		/**
		 * TODO: handle intent request from outside
		 */
		if (getIntent().getAction() != null
				&& !getIntent().getAction().toString()
						.equalsIgnoreCase("android.intent.action.MAIN")) {

			/**
			 * TODO: handle widget fragment requests.
			 */

		} else {
			if (position > 0) {
				if (position != mDrawerItemSelectedPosition) {
					loadFragment(mDrawerListItems.get(position));
				}
			}
		}
	}

	private String[] accountList(List<OUser> accounts) {
		String[] account_list = new String[accounts.size()];
		int i = 0;
		for (OUser user : accounts) {
			account_list[i] = user.getAndroidName();
			i++;
		}
		return account_list;
	}

	private Dialog accountSelectionDialog(final List<OUser> accounts) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(R.string.title_select_account)
				.setSingleChoiceItems(accountList(accounts), 1,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mAccount = accounts.get(which);
							}
						})
				.setNeutralButton(R.string.label_new, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						getActionBar().setDisplayHomeAsUpEnabled(false);
						getActionBar().setHomeButtonEnabled(false);
						LoginSignup loginSignUp = new LoginSignup();
						startMainFragment(loginSignUp, false);
					}
				})
				// Set the action buttons
				.setPositiveButton(R.string.label_login,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								if (mAccount != null) {
									OdooAccountManager.loginUser(mContext,
											mAccount.getAndroidName());
								} else {
									Toast.makeText(mContext,
											"Please select account",
											Toast.LENGTH_LONG).show();
								}
								init();
							}
						})
				.setNegativeButton(R.string.label_cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								finish();
							}
						});

		return builder.create();
	}

	@Override
	public void refreshDrawer(String tag_key) {
		Log.d(TAG, "MainActivity->DrawerListener->refreshDrawer()");
		int start_index = -1;
		List<DrawerItem> updated_menus = new ArrayList<DrawerItem>();
		for (int i = 0; i < mDrawerListItems.size(); i++) {
			DrawerItem item = mDrawerListItems.get(i);
			if (item.getKey().equals(tag_key) && !item.isGroupTitle()) {
				if (start_index < 0) {
					start_index = i - 1;
					BaseFragment instance = (BaseFragment) item
							.getFragmentInstace();
					updated_menus.addAll(instance.drawerMenus(mContext));
					break;
				}
			}
		}
		for (DrawerItem item : updated_menus) {
			mDrawerAdatper.updateDrawerItem(start_index, item);
			start_index++;
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	public void setTitle(CharSequence title, CharSequence subtitle) {
		mTitle = title;
		this.setTitle(mTitle);
		getActionBar().setSubtitle(subtitle);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private boolean onSettingItemSelected(SettingKeys key) {
		switch (key) {
		case GLOBAL_SETTING:
			Intent i = new Intent(this, AppSettingsActivity.class);
			startActivityForResult(i, RESULT_SETTINGS);
			return true;
		case ABOUT_US:
			getActionBar().setDisplayHomeAsUpEnabled(false);
			getActionBar().setHomeButtonEnabled(false);
			AboutFragment about = new AboutFragment();
			startMainFragment(about, true);
			return true;
		case ACCOUNTS:
			AccountsDetail acFragment = new AccountsDetail();
			startMainFragment(acFragment, true);
			return true;
		case PROFILE:
			UserProfile profileFragment = new UserProfile();
			startMainFragment(profileFragment, true);
			return true;
		default:
			return true;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case RESULT_SETTINGS:
			updateSyncSettings();
			break;
		}

	}

	private void updateSyncSettings() {
		Log.d(TAG, "MainActivity->updateSyncSettings()");

		PreferenceManager mPref = new PreferenceManager(mContext);
		int sync_interval = mPref.getInt("sync_interval", 1440);

		List<String> default_authorities = new ArrayList<String>();
		default_authorities.add("com.android.calendar");
		default_authorities.add("com.android.contacts");

		SyncAdapterType[] list = ContentResolver.getSyncAdapterTypes();

		Account mAccount = OdooAccountManager.getAccount(mContext, OUser
				.current(mContext).getAndroidName());

		for (SyncAdapterType lst : list) {
			if (lst.authority.contains("com.odoo")
					&& lst.authority.contains("providers")) {
				default_authorities.add(lst.authority);
			}
		}
		for (String authority : default_authorities) {
			boolean isSyncActive = ContentResolver.getSyncAutomatically(
					mAccount, authority);
			if (isSyncActive) {
				setSyncPeriodic(authority, sync_interval, 60, 1);
			}
		}
		Toast.makeText(this, R.string.toast_setting_saved, Toast.LENGTH_LONG)
				.show();
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		if (mDrawerToggle != null) {
			mDrawerToggle.syncState();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		if (mDrawerToggle != null) {
			mDrawerToggle.onConfigurationChanged(newConfig);
		}
	}

	// PullToRefresh
	public OETouchListener getTouchAttacher() {
		return mTouchAttacher;
	}

	/**
	 * Sets the auto sync.
	 * 
	 * @param authority
	 *            the authority
	 * @param isON
	 *            the is on
	 */
	public void setAutoSync(String authority, boolean isON) {
		try {
			Account account = OdooAccountManager.getAccount(this, OUser
					.current(mContext).getAndroidName());
			if (!ContentResolver.isSyncActive(account, authority)) {
				ContentResolver.setSyncAutomatically(account, authority, isON);
			}
		} catch (NullPointerException eNull) {

		}
	}

	/**
	 * Request sync.
	 * 
	 * @param authority
	 *            the authority
	 * @param bundle
	 *            the extra data
	 */
	public void requestSync(String authority, Bundle bundle) {
		Account account = OdooAccountManager.getAccount(
				getApplicationContext(), OUser.current(getApplicationContext())
						.getAndroidName());
		Bundle settingsBundle = new Bundle();
		settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
		if (bundle != null) {
			settingsBundle.putAll(bundle);
		}
		ContentResolver.requestSync(account, authority, settingsBundle);
	}

	/**
	 * Request sync.
	 * 
	 * @param authority
	 *            the authority
	 */
	public void requestSync(String authority) {
		requestSync(authority, null);
	}

	/**
	 * Sets the sync periodic.
	 * 
	 * @param authority
	 *            the authority
	 * @param interval_in_minute
	 *            the interval_in_minute
	 * @param seconds_per_minute
	 *            the seconds_per_minute
	 * @param milliseconds_per_second
	 *            the milliseconds_per_second
	 */
	public void setSyncPeriodic(String authority, long interval_in_minute,
			long seconds_per_minute, long milliseconds_per_second) {
		Account account = OdooAccountManager.getAccount(this,
				OUser.current(mContext).getAndroidName());
		Bundle extras = new Bundle();
		this.setAutoSync(authority, true);
		ContentResolver.setIsSyncable(account, authority, 1);
		final long sync_interval = interval_in_minute * seconds_per_minute
				* milliseconds_per_second;
		ContentResolver.addPeriodicSync(account, authority, extras,
				sync_interval);

	}

	/**
	 * Cancel sync.
	 * 
	 * @param authority
	 *            the authority
	 */
	public void cancelSync(String authority) {
		Account account = OdooAccountManager.getAccount(this,
				OUser.current(mContext).getAndroidName());
		ContentResolver.cancelSync(account, authority);
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position,
			long id) {
		int item_position = position - 1;
		DrawerItem item = mDrawerListItems.get(item_position);
		if (!item.isGroupTitle()) {
			if (!item.getKey().equals("com.odoo.settings")) {
				mDrawerItemSelectedPosition = item_position + 1;
			}
			mAppTitle = item.getTitle();
			loadFragment(item);
			mDrawerLayout.closeDrawers();
		}
		mDrawerListView.setItemChecked(mDrawerItemSelectedPosition, true);

	}

	private void loadFragment(DrawerItem item) {

		Fragment fragment = (Fragment) item.getFragmentInstace();
		if (item.getTagColor() != null
				&& !fragment.getArguments().containsKey("tag_color")) {
			Bundle tagcolor = fragment.getArguments();
			tagcolor.putInt("tag_color", Color.parseColor(item.getTagColor()));
			fragment.setArguments(tagcolor);
		}
		loadFragment(fragment);
	}

	private void loadFragment(Object instance) {
		if (instance instanceof Intent) {
			startActivity((Intent) instance);
		} else {
			Fragment fragment = (Fragment) instance;
			if (fragment.getArguments() != null
					&& fragment.getArguments().containsKey("settings")) {
				onSettingItemSelected(SettingKeys.valueOf(fragment
						.getArguments().get("settings").toString()));
			}
			if (fragment != null
					&& !fragment.getArguments().containsKey("settings")) {
				startMainFragment(fragment, false);
			}
		}
	}

	private List<DrawerItem> setSettingMenu() {
		List<DrawerItem> sys = new ArrayList<DrawerItem>();
		String key = "com.odoo.settings";
		Resources r = getResources();
		sys.add(new DrawerItem(key, r.getString(R.string.title_settings), true));
		sys.add(new DrawerItem(key, r.getString(R.string.title_profile), 0,
				R.drawable.ic_action_user, getFragBundle(new Fragment(),
						"settings", SettingKeys.PROFILE)));

		sys.add(new DrawerItem(key, r.getString(R.string.title_accounts), 0,
				R.drawable.ic_action_accounts, getFragBundle(new Fragment(),
						"settings", SettingKeys.ACCOUNTS)));
		sys.add(new DrawerItem(key, r.getString(R.string.title_about_us), 0,
				R.drawable.ic_action_about, getFragBundle(new Fragment(),
						"settings", SettingKeys.ABOUT_US)));
		return sys;
	}

	private Fragment getFragBundle(Fragment fragment, String key,
			SettingKeys val) {
		Bundle bundle = new Bundle();
		bundle.putString(key, val.toString());
		fragment.setArguments(bundle);
		return fragment;
	}

	public void lockDrawer(boolean flag) {
		if (!flag) {
			mDrawerLayout.setDrawerLockMode(DrawerLayout.STATE_IDLE);
		} else {
			mDrawerLayout
					.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("current_drawer_item", mDrawerItemSelectedPosition);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void startMainFragment(Fragment fragment, boolean addToBackState) {
		Log.d(TAG, "MainActivity->FragmentListener->startMainFragment()");
		int container_id = R.id.fragment_container;

		if (isTwoPane()) {
			findViewById(R.id.fragment_detail_container).setVisibility(
					View.GONE);
			Fragment detail = mFragment.findFragmentByTag("detail_fragment");
			if (detail != null && !mNewFragment && !detail.isInLayout()) {
				startDetailFragment(recreateFragment(detail));
			}

		}
		FragmentTransaction tran = mFragment.beginTransaction().replace(
				container_id, fragment, "main_fragment");
		tran.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		if (addToBackState) {
			tran.addToBackStack(null);
		}
		tran.commitAllowingStateLoss();
	}

	@Override
	public void startDetailFragment(Fragment fragment) {
		Log.d(TAG, "MainActivity->FragmentListener->startDetailFragment()");
		int container_id = (isTwoPane()) ? R.id.fragment_detail_container
				: R.id.fragment_container;
		FragmentTransaction tran = mFragment.beginTransaction().replace(
				container_id, fragment, "detail_fragment");
		tran.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		if (!isTwoPane()) {
			tran.addToBackStack(null);
			tran.commit();
		} else {
			findViewById(R.id.fragment_detail_container).setVisibility(
					View.VISIBLE);
			tran.commitAllowingStateLoss();
		}
	}

	private Fragment recreateFragment(Fragment fragment) {
		Log.d(TAG, "recreateFragment()");
		Fragment newInstance = null;
		try {
			Fragment.SavedState savedState = mFragment
					.saveFragmentInstanceState(fragment);

			newInstance = fragment.getClass().newInstance();
			newInstance.setInitialSavedState(savedState);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newInstance;
	}

	@Override
	public void restart() {
		Log.d(TAG, "MainActivity->FragmentListener->restart()");
		getIntent().putExtra("create_new_account", false);
		init();
	}

	@Override
	public boolean isTwoPane() {
		return mTwoPane;
	}
}
