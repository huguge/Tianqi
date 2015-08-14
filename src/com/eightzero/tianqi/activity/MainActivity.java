package com.eightzero.tianqi.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.eightzero.tianqi.tool.BaseConnection;
import com.eightzero.tianqi.tool.CallBack;
import com.eightzero.tianqi.tool.Constants;
import com.eightzero.tianqi.tool.MyToast;
import com.eightzero.tianqi.tool.NetworkHelper;
import com.eightzero.tianqi.view.SlidingMenu;
import com.example.tianqi.R;

public class MainActivity extends Activity {

	// 定位客户端
	private LocationClient mLocationClient;
	// 定位监听器
	public MyLocationListener mMyLocationListener;
	
	// 地位所在城市
	private TextView cityNametText;
	// 侧滑菜单按钮
	private ImageView imageViewmenu;
	// 当前所在城市
	private String cityName;
	// 自定义侧滑菜单
	private SlidingMenu mLeftMenu ; 
	// 会员中心点击layout
	private RelativeLayout memberCenter;
	private RelativeLayout browsingHistory;
	
	private ImageView changeCity;// 地图
	
	private boolean flag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);
//		initDate();
		initView();
		// 打开网络才能访问服务器
		if (NetworkHelper.getNetworkType(MainActivity.this) != NetworkHelper.NONE) {
			// 初始化定位
			initMyLocation();
		} else {
			MyToast.showShort(MainActivity.this, R.string.net_err);
		}
		initOnclickListener();
	}

	/**
	 * 初始化布局
	 */
	private void initView() {
		mLeftMenu = (SlidingMenu) findViewById(R.id.id_menu);
		cityNametText = (TextView) findViewById(R.id.city_name);
		imageViewmenu = (ImageView) findViewById(R.id.btn_menu);
		memberCenter = (RelativeLayout)findViewById(R.id.rlyt_member_center);
		browsingHistory = (RelativeLayout)findViewById(R.id.rlyt_browsing_history);
		changeCity = (ImageView)findViewById(R.id.btn_switch_city);
	}

	// 初始化定位相关代码
	private void initMyLocation() {
		mLocationClient = new LocationClient(getApplicationContext());
		mMyLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mMyLocationListener);
		
		// 设定相关配置
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 是否开启GPS
		option.setIsNeedAddress(true);// 是否需要地址信息
		option.setCoorType("bd09ll");// 设置坐标类型,注意这个后面不要写成11，这个是经纬度单词的首字母LL小写
		option.setScanSpan(100000);// 设置扫描间隔，单位(毫秒)
		mLocationClient.setLocOption(option);
	}
	
	/**
	 * 实现定位回调监听
	 * @author huweyiang
	 * @date 2015年7月9日
	 * @time 下午5:20:21
	 */
	public class MyLocationListener implements BDLocationListener{

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;
			// 获取定位的到当前的城市
			cityName = location.getCity();
			saveLocation(location);// 将数据保存到sp中
			if (cityName != null && !"".equals(cityName.trim())) {
				cityNametText.setText(cityName.substring(0, cityName.length() - 1));
//				sharePreferenceUtil.setCity(cityName.substring(0,cityName.length() - 1));
				MyToast.showLong(MainActivity.this, "您当前所在=====>" + cityName);
			} else {
				MyToast.showLong(MainActivity.this, "抱歉，没有定位到您当前的所在地!");
			}
			
			// 定位成功后停止定位
			mLocationClient.stop();
			// updateWeatherInfo(cityName);//定位成功请求天气数据
		}
		
	}
	
	/**
	 * 更新天气界面
	 */
	private void updateWeatherInfo(String cityName) {
		BaseConnection connection = new BaseConnection(this);
		MyToast.showShort(this, "获取天气数据");
		//String apiUrl = SmartWeatherUrlUtil.getInterfaceURL("101100101",Constants.TYPE_V);// 向气象数据平台发送请求
		connection.openConnection(Constants.JUHE_IP+"cityname="+cityName+"&dtype="+Constants.JUHE_DTYPE+"&key="+Constants.JUHE_APPKEY, "get", new CallBack() {
			@Override
			public void executeResult(String result) {
				System.out.println(result);
			}
		});
	}

	@Override
	protected void onStart() {
		if (!mLocationClient.isStarted()) {
			mLocationClient.start();
		}
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		mLocationClient.stop();
		super.onStop();
	}
	
	// 保存当前用户的地址信息
	private void saveLocation(BDLocation location) {
		SharedPreferences sharedPreferences = getSharedPreferences(Constants.USER_LOCATION_INFORMATION,
				Context.MODE_PRIVATE); // 私有数据
		Editor editor = sharedPreferences.edit();// 获取编辑器
		
		editor.putString("province", location.getProvince());
		editor.putString("city", location.getCity());
		editor.putString("cityCode", location.getCityCode());
		editor.putString("district", location.getDistrict());
		editor.putString("latitude", String.valueOf(location.getLatitude()));
		editor.putString("longitude", String.valueOf(location.getLongitude()));
		editor.putString("addrStr", location.getAddrStr());
		editor.putString("direction", String.valueOf(location.getDirection()));
		editor.putString("street", location.getStreet());
		editor.putString("streetNumber", location.getStreetNumber());
		editor.putString("radius", String.valueOf(location.getRadius()));
		editor.putString("locType", String.valueOf(location.getLocType()));
		editor.commit();// 提交保存
	}
	
	/**
	 * 初始化监听
	 */
	private void initOnclickListener() {
		
		// 侧滑菜单
		imageViewmenu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mLeftMenu.toggle();
			}
		});
		
		//会员中心
		memberCenter.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),PersonnelCenterActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.push_left_in, R.anim.fade_out);
			}
		});
		
		//浏览历史
		browsingHistory.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),BrowsingHistoryActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.push_left_in, R.anim.fade_out);
			}
		});
		
		// 进入地图
		changeCity.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
//				Intent intent = new Intent(MainActivity.this,ShowMapAcitivity.class);// 进入地图
				Intent intent = new Intent(MainActivity.this,RoutePlanningActivity.class);// 进入路线规划
				Bundle bundle = new Bundle();
				bundle.putString("city","太原");// 当前所在城市
				bundle.putString("shopAddress", "大马村");// 商家地址
				bundle.putDouble("shopLongitude", 112.557268);// 经度
				bundle.putDouble("shopLatitude", 37.804593);// 纬度
				intent.putExtras(bundle);// 传递bundle
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**
	 * 连续按两次返回键就退出
	 */
	private long firstTime;

	@Override
	public void onBackPressed() {
		if(flag)
		MyToast.showShort(getApplicationContext(), "mLeftMenu"+mLeftMenu);
		if (System.currentTimeMillis() - firstTime < 2000) {
			finish();
		} else {
			firstTime = System.currentTimeMillis();
			MyToast.showShort(this, "再按一次退出程序");
		}
	}
}