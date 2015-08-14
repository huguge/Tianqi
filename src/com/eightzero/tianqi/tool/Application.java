package com.eightzero.tianqi.tool;

public class Application extends android.app.Application{

	private static Application mApplication;
	private SharePreferenceUtil mPreferenceUtil;
	
	public static synchronized Application getInstance(){
		return mApplication;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mApplication = this;
		initData();
	}
	
	/**
	 * 初始化
	 */
	public void initData(){
		mPreferenceUtil = new SharePreferenceUtil(this,SharePreferenceUtil.CITY_SHAREPRE_FILE);
	}
	
	public synchronized SharePreferenceUtil getSharePreferenceUtil() {
		if (mPreferenceUtil == null)
			mPreferenceUtil = new SharePreferenceUtil(this,SharePreferenceUtil.CITY_SHAREPRE_FILE);
		return mPreferenceUtil;
	}
}
