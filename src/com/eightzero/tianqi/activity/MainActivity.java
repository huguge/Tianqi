package com.eightzero.tianqi.activity;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.eightzero.tianqi.tool.Application;
import com.eightzero.tianqi.tool.BaseConnection;
import com.eightzero.tianqi.tool.CallBack;
import com.eightzero.tianqi.tool.Constants;
import com.eightzero.tianqi.tool.MyToast;
import com.eightzero.tianqi.tool.NetworkHelper;
import com.eightzero.tianqi.tool.SharePreferenceUtil;
import com.eightzero.tianqi.view.SlidingMenu;
import com.example.tianqi.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

	private LocationClient mLocationClient;
	private Application mApplication;
	private SharePreferenceUtil sharePreferenceUtil;
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
	
	private boolean flag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initDate();
		initView();
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
//		memberCenter.setBackgroundResource(R.drawable.layout_background_color);//点击灰色效果
		// 打开网络才能访问服务器
		if (NetworkHelper.getNetworkType(MainActivity.this) != NetworkHelper.NONE) {
			mLocationClient.start();// 启动定位
			mLocationClient.requestLocation();
		} else {
			MyToast.showShort(MainActivity.this, R.string.net_err);
		}
	}

	/**
	 * 初始化数据
	 */
	private void initDate() {
		mApplication = Application.getInstance();
		sharePreferenceUtil = mApplication.getSharePreferenceUtil();
		mLocationClient = mApplication.getLocationClient();
		MyToast.showShort(this, "正在定位...");
		mLocationClient.registerLocationListener(mBdLocationListener);
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
				System.out.println(result + "1111111111111111111111111");
			}
		});
	}

	/**
	 * 定位监听
	 */
	BDLocationListener mBdLocationListener = new BDLocationListener() {
		@Override
		public void onReceivePoi(BDLocation location) {
			if (location == null) {
				return;
			}
		}

		@Override
		public void onReceiveLocation(BDLocation location) {
			// 获取定位的到当前的城市
			cityName = location.getCity();
			cityNametText.setText(cityName.substring(0,cityName.length()-1));
			sharePreferenceUtil.setCity(cityName.substring(0,cityName.length()-1));
			MyToast.showLong(MainActivity.this, "定位后的城市是" + cityName);
			//updateWeatherInfo(cityName);//定位成功请求天气数据
			// 定位成功后停止定位
			mLocationClient.stop();
		}
	};

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
