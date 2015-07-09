package com.eightzero.tianqi.activity;

import android.app.Activity;
import android.app.Service;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Window;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.BDNotifyListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.tianqi.R;

public class ShowMapAcitivity extends Activity{

	// 百度地图控件
	private MapView mMapView = null;
	// 百度地图对象
	private BaiduMap bdMap;
	
	private LocationClient locationClient;
	private BDLocationListener locationListener;
	private BDNotifyListener notifyListener;
	
	private double longitude;// 精度
	private double latitude;// 维度
	private float radius;// 定位精度半径，单位是米
	private String addrStr;// 反地理编码
	private String province;// 省份信息
	private String city;// 城市信息
	private String district;// 区县信息
	private float direction;// 手机方向信息

	private int locType;
	
	// 定位模式 （普通-跟随-罗盘）
	private MyLocationConfiguration.LocationMode currentMode;

	// 振动器设备
	private Vibrator mVibrator;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_show_map);
		
		mMapView = (MapView) findViewById(R.id.bmapview);
		bdMap = mMapView.getMap();
		
		mMapView.showZoomControls(false);// 不显示默认的缩放控件
		mMapView.showScaleControl(false);// 不显示默认比例尺控件
		
		currentMode = MyLocationConfiguration.LocationMode.NORMAL;
		mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
		initMap();
	}
	
	// 初始化地图
	private void initMap(){
		bdMap.setMyLocationEnabled(true);
		// 1. 初始化LocationClient类
		locationClient = new LocationClient(getApplicationContext());
		// 2. 声明LocationListener类
		locationListener = new MyLocationListener();
		// 3. 注册监听函数
		locationClient.registerLocationListener(locationListener);
		// 4. 设置参数
		LocationClientOption locOption = new LocationClientOption();
//		locOption.setLocationMode(LocationMode.FOLLOWING);// 设置定位模式
		locOption.setCoorType("bd09ll");// 设置定位结果类型
		locOption.setScanSpan(5000);// 设置发起定位请求的间隔时间,ms
//		locOption.setIsNeedAddress(true);// 返回的定位结果包含地址信息
//		locOption.setNeedDeviceDirect(true);// 设置返回结果包含手机的方向

		locationClient.setLocOption(locOption);
		// 5. 注册位置提醒监听事件
		notifyListener = new MyNotifyListener();
		notifyListener.SetNotifyLocation(longitude, latitude, 3000, "bd09ll");//精度，维度，范围，坐标类型
		locationClient.registerNotify(notifyListener);
		// 6. 开启/关闭 定位SDK
		locationClient.start();
	}
	
	class MyLocationListener implements BDLocationListener {
		// 异步返回的定位结果
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null) {
				return;
			}
			locType = location.getLocType();
			Toast.makeText(ShowMapAcitivity.this, "当前定位的返回值是："+locType, Toast.LENGTH_SHORT).show();
			longitude = location.getLongitude();
			latitude = location.getLatitude();
			if (location.hasRadius()) {// 判断是否有定位精度半径
				radius = location.getRadius();
			}
			if (locType == BDLocation.TypeGpsLocation) {//
				Toast.makeText(ShowMapAcitivity.this,
						"当前速度是：" + location.getSpeed() + "~~定位使用卫星数量："
								+ location.getSatelliteNumber(),
						Toast.LENGTH_SHORT).show();
			} else if (locType == BDLocation.TypeNetWorkLocation) {
				addrStr = location.getAddrStr();// 获取反地理编码(文字描述的地址)
				Toast.makeText(ShowMapAcitivity.this, addrStr,Toast.LENGTH_SHORT).show();
			}
//			direction = location.getDirection();// 获取手机方向，【0~360°】,手机上面正面朝北为0°
			province = location.getProvince();// 省份
			city = location.getCity();// 城市
			district = location.getDistrict();// 区县
			Toast.makeText(ShowMapAcitivity.this,province + "~" + city + "~" + district, Toast.LENGTH_SHORT).show();
			
			
			// 构造定位数据
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(radius)//
					.direction(direction)// 方向
					.latitude(latitude)//
					.longitude(longitude)//
					.build();
			// 设置定位数据
			bdMap.setMyLocationData(locData);
			LatLng latLng = new LatLng(latitude, longitude);
			MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
			bdMap.animateMapStatus(msu);

		}

		public void onReceivePoi(BDLocation arg0) {
			
		}
	}
	
	/**
	 * 位置提醒监听器
	 * @author ys
	 *
	 */
	class MyNotifyListener extends BDNotifyListener {
		@Override
		public void onNotify(BDLocation bdLocation, float distance) {
			super.onNotify(bdLocation, distance);
			mVibrator.vibrate(1000);//振动提醒已到设定位置附近
	    	Toast.makeText(ShowMapAcitivity.this, "震动提醒", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
	}
	
	@Override
	protected void onDestroy() {
		mMapView.onDestroy();
		mMapView = null;
		super.onDestroy();
	}
}
