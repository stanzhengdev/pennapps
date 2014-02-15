package com.marcochiang.justice;

import java.util.ArrayList;

import android.app.Application;
import android.content.pm.ResolveInfo;

public class JusticeApplication extends Application {
	
	private static ArrayList<ResolveInfo> mApplicationList;
	
	public static ArrayList<ResolveInfo> getApplicationList() {
		return mApplicationList;
	}
	
	public static void setApplicationList(ArrayList<ResolveInfo> data) {
		mApplicationList = data;
	}
}
