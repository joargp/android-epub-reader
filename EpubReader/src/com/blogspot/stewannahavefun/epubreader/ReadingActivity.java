package com.blogspot.stewannahavefun.epubreader;

import java.io.File;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.blogspot.stewannahavefun.epubreader.EpubReader.Books;
import com.blogspot.stewannahavefun.epubreader.EpubReader.Contents;

public class ReadingActivity extends Activity implements
		LoaderCallbacks<Cursor> {
	private DrawerLayout mDrawerLayout;
	private ListView mNavigationList;
	private ActionBarDrawerToggle mDrawerToggle;
	private WebView mBookView;

	private SimpleCursorAdapter mAdapter;

	private String mNavigationDrawerTitle;
	private String mActivityTitle;

	private String EPUB_LOCATION;
	private String BOOK_ID;

	private static final String SCHEME = "file://";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reading);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		mActivityTitle = mNavigationDrawerTitle = getTitle().toString();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.reading_drawer_layout);
		mNavigationList = (ListView) findViewById(R.id.navigation_list);
		mBookView = (WebView) findViewById(R.id.book_view);

		mDrawerToggle = new ActionBarDrawerToggle(
				this,
				mDrawerLayout,
				R.drawable.ic_drawer,
				R.string.navigation_drawer_open,
				R.string.navigation_drawer_close
				) {

					@Override
					public void onDrawerClosed(View drawerView) {
						getActionBar().setTitle(mActivityTitle);
						invalidateOptionsMenu();
					}

					@Override
					public void onDrawerOpened(View drawerView) {
						getActionBar().setTitle(mNavigationDrawerTitle);
						invalidateOptionsMenu();
					}

				};

		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

		String[] from = { Contents.NAVIGATION_LABEL, Contents.NAVIGATION_DEPTH };
		int[] to = { R.id.navigation_item, R.id.navigation_item };

		mAdapter = new NavigationAdapter(this, from, to);
		mNavigationList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int pos,
					long id) {
				onNavigationLabelClick(id);
			}
		});

		Bundle args = getIntent().getExtras();
		String link = args.getString(EpubReader.READ_BOOK_PROJECTION[1]);
		int pageNumber = args.getInt(EpubReader.READ_BOOK_PROJECTION[2]);
		int playOrder = args.getInt(EpubReader.READ_BOOK_PROJECTION[3]);
		BOOK_ID = args.getString(EpubReader.READ_BOOK_PROJECTION[4]);
		String location = args.getString(EpubReader.READ_BOOK_PROJECTION[5]);

		EPUB_LOCATION = location;

		String url = constructUrl(link);
		mBookView.loadUrl(url);
		// TODO: restore last reading location, using JavaScript
		mNavigationList.setItemChecked(playOrder, true);

	}

	private void onNavigationLabelClick(long id) {
		Uri navigation = ContentUris.withAppendedId(
				Contents.CONTENTS_ID_URI_BASE, id);
		Cursor c = getContentResolver().query(
				navigation,
				EpubReader.CONTENTS_ITEM_PROJECTION,
				null, null, null);
		String link = c.getString(1);

		mBookView.loadUrl(constructUrl(link));
	}

	private String constructUrl(String link) {
		return SCHEME + EPUB_LOCATION + File.separator + link;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.reading, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		mDrawerToggle.syncState();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String selection = Books.BOOK_ID + " = " + BOOK_ID;

		return new CursorLoader(
				this,
				Contents.CONTENTS_URI,
				EpubReader.CONTENTS_PROJECTION,
				selection,
				null,
				null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		mAdapter.swapCursor(c);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	private class NavigationAdapter extends SimpleCursorAdapter {

		public NavigationAdapter(Context context, String[] from, int[] to) {
			super(context, R.layout.navigation_list_item, null, from, to, 0);

			setViewBinder(new ViewBinder() {

				@Override
				public boolean setViewValue(View view, Cursor cursor,
						int columnIndex) {
					TextView labelView = (TextView) view;

					switch (columnIndex) {
					case 1:
						String text = cursor.getString(columnIndex);
						labelView.setText(text);
						break;

					case 3:
						int depth = cursor.getInt(columnIndex);
						int oldLeftPadding = labelView.getPaddingLeft();
						int newLeftPadding = oldLeftPadding * depth;
						labelView.setPadding(
								newLeftPadding,
								labelView.getPaddingTop(),
								labelView.getPaddingRight(),
								labelView.getPaddingBottom());
						break;

					default:
						break;
					}

					return true;
				}
			});
		}

	}
}