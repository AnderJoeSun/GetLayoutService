package me.zhengnian.getlayoutservice;

import java.io.OutputStream;
import java.util.List;

import org.xmlpull.v1.XmlSerializer;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.util.Xml;

import me.zhengnian.getlayoutservice.IGetLayout.Stub;

public class GetLayoutService extends Service {
	private static final int ITEM_TYPE_APPWIDGET = 4;

	private MyBinder binder = new MyBinder();

	public class MyBinder extends Stub {
		@Override
		public boolean getLayout() {
			try {
				Log.e("GetLayoutService", "GetLayoutService getLayout() 898989");
				readAndSaveAsXML(GetLayoutService.this);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.e("GetLayoutService", "GetLayoutService is Binded");
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.e("GetLayoutService", "GetLayoutService is Created");
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.e("GetLayoutService", "GetLayoutService is Unbinded");
		return true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e("GetLayoutService", "GetLayoutService is Destroyed");
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
		Log.e("GetLayoutService", "GetLayoutService is ReBinded");
	}

	private void readAndSaveAsXML(Context context) throws Exception {
		final ContentResolver cr = context.getContentResolver();
		String AUTHORITY = getAuthorityFromPermission(context, "com.android.launcher.permission.READ_SETTINGS");
		if (AUTHORITY == null) {
			AUTHORITY = getAuthorityFromPermission(context, "com.android.launcher.permission.WRITE_SETTINGS");
		}
		if (AUTHORITY == null) {
			return;
		}

		Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites?notify=true");

		Cursor c = cr.query(CONTENT_URI, null, null, null, null);
		Log.e("GetLayoutService", " openFileOutput : /data/data/me.zhengnian.getlayoutservice/files/home_layout.xml");
		OutputStream out = this.openFileOutput("home_layout.xml", Context.MODE_WORLD_READABLE);
		XmlSerializer serializer = Xml.newSerializer();
		serializer.setOutput(out, "UTF-8");
		serializer.startDocument("UTF-8", true);
		serializer.startTag(null, "favorites");
		serializer.attribute(null, "xmlns:launcher", "http://schemas.android.com/apk/res/com.android.launcher");

		if (c != null) {

			int appWidgetId;
			AppWidgetManager appWidgetMgr = AppWidgetManager.getInstance(context);
			AppWidgetProviderInfo awpi;
			ComponentName cn;

			String intent;
			String packageName;
			String className;
			int itemType;
			while (c.moveToNext()) {
				itemType = c.getInt(c.getColumnIndex("itemType"));

				if (itemType == ITEM_TYPE_APPWIDGET) {
					appWidgetId = c.getInt(c.getColumnIndex("appWidgetId"));

					awpi = appWidgetMgr.getAppWidgetInfo(appWidgetId);
					cn = awpi.provider;
					packageName = cn.getPackageName();
					className = cn.getClassName();

					serializer.startTag(null, "appwidget");
					serializer.attribute(null, "launcher:packageName", packageName);
					serializer.attribute(null, "launcher:className", className);
					serializer.attribute(null, "launcher:screen", String.valueOf(c.getInt(c.getColumnIndex("screen"))));
					serializer.attribute(null, "launcher:x", String.valueOf(c.getInt(c.getColumnIndex("cellX"))));
					serializer.attribute(null, "launcher:y", String.valueOf(c.getInt(c.getColumnIndex("cellY"))));
					serializer.attribute(null, "launcher:spanX", String.valueOf(c.getInt(c.getColumnIndex("spanX"))));
					serializer.attribute(null, "launcher:spanY", String.valueOf(c.getInt(c.getColumnIndex("spanY"))));
					serializer.endTag(null, "appwidget");

				} else {

					intent = c.getString(c.getColumnIndex("intent"));
					intent = intent.substring(intent.indexOf("component=") + "component=".length(),
							intent.indexOf(";end"));
					packageName = intent.substring(0, intent.indexOf("/"));
					className = intent.substring(intent.indexOf("/") + 1);
					if (className.startsWith(".")) {
						className = packageName + className;
					}

					serializer.startTag(null, "favorite");
					serializer.attribute(null, "launcher:packageName", packageName);
					serializer.attribute(null, "launcher:className", className);

					if (c.getInt(c.getColumnIndex("container")) == -101) {
						serializer.attribute(null, "launcher:container", "-101");
					}

					serializer.attribute(null, "launcher:screen", String.valueOf(c.getInt(c.getColumnIndex("screen"))));
					serializer.attribute(null, "launcher:x", String.valueOf(c.getInt(c.getColumnIndex("cellX"))));
					serializer.attribute(null, "launcher:y", String.valueOf(c.getInt(c.getColumnIndex("cellY"))));
					serializer.endTag(null, "favorite");
				}

			}

		}

		serializer.endTag(null, "favorites");
		serializer.endDocument();
		out.flush();
		out.close();
	}

	private String getAuthorityFromPermission(Context context, String permission) {
		if (permission == null)
			return null;
		List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS);
		if (packs != null) {
			for (PackageInfo pack : packs) {
				ProviderInfo[] providers = pack.providers;
				if (providers != null) {
					for (ProviderInfo provider : providers) {
						if (permission.equals(provider.readPermission))
							return provider.authority;
						if (permission.equals(provider.writePermission))
							return provider.authority;
					}
				}
			}
		}
		return null;
	}

}


