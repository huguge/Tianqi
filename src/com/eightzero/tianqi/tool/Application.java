package com.eightzero.tianqi.tool;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class Application extends android.app.Application{

	private LocationClient mLocationClient = null;
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
	
	private LocationClientOption getLocationClientOption(){
		LocationClientOption option = new LocationClientOption();//声明LocationClient类 
		option.setOpenGps(true);//打开gps定位
		option.setAddrType("all");
		option.setServiceName(this.getPackageName());
		option.setPriority(LocationClientOption.NetWorkFirst); //设置优先级，此为网络优先
		option.setScanSpan(0);
		option.disableCache(true);
		return option;
	}
	
	/**
	 * 初始化
	 */
	public void initData(){
		mLocationClient = new LocationClient(this, getLocationClientOption());
		mPreferenceUtil = new SharePreferenceUtil(this,SharePreferenceUtil.CITY_SHAREPRE_FILE);
	}
	
	
	public synchronized LocationClient getLocationClient() {
		if (mLocationClient == null)
			mLocationClient = new LocationClient(this,getLocationClientOption());
		return mLocationClient;
	}
	
	public synchronized SharePreferenceUtil getSharePreferenceUtil() {
		if (mPreferenceUtil == null)
			mPreferenceUtil = new SharePreferenceUtil(this,SharePreferenceUtil.CITY_SHAREPRE_FILE);
		return mPreferenceUtil;
	}
}
