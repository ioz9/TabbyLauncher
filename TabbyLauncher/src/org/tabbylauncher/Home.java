package org.tabbylauncher;

import java.util.ArrayList;
import java.util.List;

import android.app.SearchManager;
import org.tabbylauncher.component.StackLayoutBar;
import org.tabbylauncher.component.StackLayoutBar.OnItemSelectedListener;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class Home extends BaseActivity implements OnItemClickListener{
	TextView mApplicationNameTextView;
	Rotor mRotor;
	StackLayoutBar mStackLayoutBar;
	GridView mGridView;
	private Animation mGridEntry;
	private Animation mGridExit;

	// Identifiers for option menu items
	private static final int MENU_WALLPAPER_SETTINGS = Menu.FIRST + 1;
	private static final int MENU_TAG = MENU_WALLPAPER_SETTINGS + 1;
	private static final int MENU_SETTINGS = MENU_TAG + 1;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.home);
		mApplicationNameTextView = (TextView) findViewById(R.id.application_name_text_view);
		mRotor = (Rotor) findViewById(R.id.rotor);
		
		loadApplications(true, mRotor);
		
		mRotor.setArrayElement(mApplicationLoaderThread, mApplications);
		mRotor.setOnItemSelectedListener(new Rotor.OnItemSelectedListener() {
			@Override
			public void onItemSelected(Rotor rotor, List<ApplicationInfo> appList,
					ApplicationInfo appInfo, int index) {
				mApplicationNameTextView.setText(appInfo.title);
			}
		});
		mRotor.setOnItemClickListener(new Rotor.OnRotorClickListener() {
			@Override
			public void onItemClick(Rotor rotor, List<ApplicationInfo> appList,
					ApplicationInfo appInfo, int index) {
				startActivity(appInfo.intent);
			}

			@Override
			public void onQuadrantListener(int idx, Intent intent) {
				startActivity(intent);
			}
		});

		mGridView = (GridView) findViewById(R.id.all_apps);
		mGridView.setOnItemClickListener(this);
		mGridEntry = AnimationUtils.loadAnimation(this, R.anim.grid_entry);
		mGridExit = AnimationUtils.loadAnimation(this, R.anim.grid_exit);


		mStackLayoutBar = (StackLayoutBar) findViewById(R.id.stacklayoutbar);
		mStackLayoutBar.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected() {
				mGridView.setAdapter(new ApplicationsAdapter(getBaseContext(), mApplications));
				if(mGridView.isShown()){
					mGridView.startAnimation(mGridExit);
					mGridView.setVisibility(View.GONE);
					mRotor.setVisibility(View.VISIBLE);					
					mApplicationNameTextView.setVisibility(View.VISIBLE);					

				}else{
					mGridView.startAnimation(mGridEntry);
					mGridView.setVisibility(View.VISIBLE);
					mRotor.setVisibility(View.GONE);					
					mApplicationNameTextView.setVisibility(View.GONE);					
				}
			}
		});

	}

	private class ApplicationsAdapter extends ArrayAdapter<ApplicationInfo> {
		private Rect mOldBounds = new Rect();

		public ApplicationsAdapter(Context context, ArrayList<ApplicationInfo> apps) {
			super(context, 0, apps);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ApplicationInfo info = mApplications.get(position);

			if (convertView == null) {
				final LayoutInflater inflater = getLayoutInflater();
				convertView = inflater.inflate(R.layout.application, parent, false);
			}

			Drawable icon = info.icon;

			if (!info.filtered) {
				//final Resources resources = getContext().getResources();
				int width = 42;//(int) resources.getDimension(android.R.dimen.app_icon_size);
				int height = 42;//(int) resources.getDimension(android.R.dimen.app_icon_size);

				final int iconWidth = icon.getIntrinsicWidth();
				final int iconHeight = icon.getIntrinsicHeight();

				if (icon instanceof PaintDrawable) {
					PaintDrawable painter = (PaintDrawable) icon;
					painter.setIntrinsicWidth(width);
					painter.setIntrinsicHeight(height);
				}

				if (width > 0 && height > 0 && (width < iconWidth || height < iconHeight)) {
					final float ratio = (float) iconWidth / iconHeight;

					if (iconWidth > iconHeight) {
						height = (int) (width / ratio);
					} else if (iconHeight > iconWidth) {
						width = (int) (height * ratio);
					}

					final Bitmap.Config c =
							icon.getOpacity() != PixelFormat.OPAQUE ?
									Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
					final Bitmap thumb = Bitmap.createBitmap(width, height, c);
					final Canvas canvas = new Canvas(thumb);
					canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG, 0));
					// Copy the old bounds to restore them later
					// If we were to do oldBounds = icon.getBounds(),
					// the call to setBounds() that follows would
					// change the same instance and we would lose the
					// old bounds
					mOldBounds.set(icon.getBounds());
					icon.setBounds(0, 0, width, height);
					icon.draw(canvas);
					icon.setBounds(mOldBounds);
					icon = info.icon = new BitmapDrawable(thumb);
					info.filtered = true;
				}
			}

			final TextView textView = (TextView) convertView.findViewById(R.id.label);
			textView.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
			textView.setText(info.title);

			return convertView;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

		ApplicationInfo applicationInfo = mApplications.get(position);
		if(applicationInfo != null && applicationInfo.intent != null){
			startActivity(applicationInfo.intent);
		}
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_WALLPAPER_SETTINGS, 0, R.string.menu_wallpaper)
		.setIcon(android.R.drawable.ic_menu_gallery)
		.setAlphabeticShortcut('W');
		menu.add(0, MENU_TAG, 0, R.string.menu_tag)
		.setIcon(android.R.drawable.btn_star)
		.setAlphabeticShortcut(SearchManager.MENU_KEY);


		menu.add(0, MENU_SETTINGS, 0, R.string.menu_settings)
		.setIcon(android.R.drawable.ic_menu_preferences)
		.setIntent(new Intent(android.provider.Settings.ACTION_SETTINGS));

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_WALLPAPER_SETTINGS:
			startWallpaper();
			return true;
		case MENU_TAG:
			startTagging();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void startTagging() {
		final Intent tagI = new Intent(this,TagListActivity.class);
		startActivity(tagI);

	}

	private void startWallpaper() {
		final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
		startActivity(Intent.createChooser(pickWallpaper, getString(R.string.menu_wallpaper)));
	}

}
