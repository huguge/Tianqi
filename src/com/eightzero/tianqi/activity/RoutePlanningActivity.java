package com.eightzero.tianqi.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption.DrivingPolicy;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRoutePlanOption.TransitPolicy;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.eightzero.tianqi.tool.Constants;
import com.example.tianqi.R;

/**
 * 路线规划
 * 
 * @author ys
 * 
 */
public class RoutePlanningActivity extends Activity implements OnClickListener {

	private MapView mapView;
	private BaiduMap bdMap;

	private EditText startEt;
	private EditText endEt;

	private String startPlace;// 开始地点
	private String endPlace;// 结束地点

	private Button driveBtn;// 驾车
	private Button walkBtn;// 步行
	private Button transitBtn;// 换成 （公交）
	private Button nextLineBtn;

	private Spinner drivingSpinner, transitSpinner;

	private RoutePlanSearch routePlanSearch;// 路径规划搜索接口

	private int index = -1;
	private int totalLine = 0;// 记录某种搜索出的方案数量
	private int drivintResultIndex = 0;// 驾车路线方案index
	private int transitResultIndex = 0;// 换乘路线方案index
	
	private String city;// 所在城市
	private Double shopLontitude;// 商家所在地区经度
	private Double shopLatitude;// 商家所在地区纬度
	private String shopAddress;// 商家所在地址
	
	// marker
	private Marker marker;
	
	// 用户地址信息
	private Double userLatitude;
	private Double userLongitude;
	private String userAddrStr;
	private String street;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_route_planning_for_me);
		
		// 获取启动该资源的Intent
		Intent intent = getIntent();
		// 获取该intent携带的数据
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			city = bundle.getString("city");
			shopLontitude = bundle.getDouble("shopLongitude");
			shopLatitude = bundle.getDouble("shopLatitude");
			shopAddress = bundle.getString("shopAddress");
		} else {
			finish();
		}
		
		getUserLocationInfo();// 获取用户地址信息
		init();
	}

	/**
	 * 
	 */
	private void init() {
		
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.showZoomControls(false);// 不显示默认的缩放控件
		bdMap = mapView.getMap();// 获取百度地图对象
		
		LatLng userLatLng = new LatLng(userLatitude,userLongitude);
		// 设定商家位置(中心点)坐标
		LatLng shopLatLng = new LatLng(shopLatitude,shopLontitude);// 注意这里的构造方法的参数-->(latitude,longitude),而不是百度地图上得到的是（longitude，latitude）
		// 定义地图状态
		MapStatus mMapStatus = new MapStatus.Builder()
		.target(shopLatLng)
		.zoom(17)
		.build();
		
		// 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
		MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
		// 改变地图状态
		bdMap.animateMapStatus(mMapStatusUpdate);// 最新的是这样设置的
		
		// 构建商企所在地的maker图标
		BitmapDescriptor bitmap = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_marka);
		
		// 构建markerOption，用于在地图上添加商企marker
		OverlayOptions options = new MarkerOptions()//
				.position(shopLatLng)// 设置marker的经纬度坐标
				.icon(bitmap)// 设置marker的图标
				.zIndex(9)// 设置marker的所在层级
				.draggable(true);// 设置手势拖拽
		// 在地图上添加marker，并显示
		marker = (Marker) bdMap.addOverlay(options);
		
		displayInfoWindow(shopLatLng,shopAddress);// 显示商家所在信息弹出窗
		
		// 圆点覆盖物
//		DotOptions dotOptions = new DotOptions();  
//        dotOptions.center(userLatLng);//设置圆心坐标  
//        dotOptions.color(0XFFfaa755);//颜色  
//        dotOptions.radius(25);//设置半径  
//        bdMap.addOverlay(dotOptions); 
		
		// 圆心覆盖物 设置用户所在地
		CircleOptions circleOptions = new CircleOptions();
		circleOptions.center(userLatLng);// 设置圆心坐标
		circleOptions.fillColor(getResources().getColor(R.color.green));// 圆的填充颜色
		circleOptions.radius(15);// 设置半径
		circleOptions.stroke(new Stroke(2, getResources().getColor(R.color.red)));// 设置边框
		bdMap.addOverlay(circleOptions);
        
		
		// 文字覆盖物
//		TextOptions textOptions = new TextOptions();
//		textOptions.bgColor(getResources().getColor(R.color.white)) // 设置文字覆盖物的颜色
//				.fontSize(45) // 设置字体大小
////				.fontColor(0x000000)// 设置字体颜色,默认黑色
//				.text(shopAddress) // 商家所在地
//				.zIndex(9)
//				.rotate(0) // 设置文字的旋转角度
//				.position(latLng);// 设置经纬度
//		bdMap.addOverlay(textOptions);
		
//		startEt = (EditText) findViewById(R.id.start_et);
//		endEt = (EditText) findViewById(R.id.end_et);
		
		driveBtn = (Button) findViewById(R.id.drive_btn);
		transitBtn = (Button) findViewById(R.id.transit_btn);
		walkBtn = (Button) findViewById(R.id.walk_btn);
		nextLineBtn = (Button) findViewById(R.id.nextline_btn);
		nextLineBtn.setEnabled(false);
		driveBtn.setOnClickListener(this);
		transitBtn.setOnClickListener(this);
		walkBtn.setOnClickListener(this);
		nextLineBtn.setOnClickListener(this);

//		drivingSpinner = (Spinner) findViewById(R.id.driving_spinner);
//		String[] drivingItems = getResources().getStringArray(R.array.driving_spinner);
//		ArrayAdapter<String> drivingAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, drivingItems);
//		drivingSpinner.setAdapter(drivingAdapter);
//		drivingSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
//
//			@Override
//			public void onItemSelected(AdapterView<?> parent, View view,
//					int position, long id) {
//				if (index == 0) {
//					drivintResultIndex = 0;
//					drivingSearch(drivintResultIndex);
//				}
//			}
//
//			@Override
//			public void onNothingSelected(AdapterView<?> parent) {
//			}
//		});

//		transitSpinner = (Spinner) findViewById(R.id.transit_spinner);
//		String[] transitItems = getResources().getStringArray(
//				R.array.transit_spinner);
//		ArrayAdapter<String> transitAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, transitItems);
//		transitSpinner.setAdapter(transitAdapter);
//		transitSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
//			@Override
//			public void onItemSelected(AdapterView<?> parent, View view,
//					int position, long id) {
//				if (index == 1) {
//					transitResultIndex = 0;
//					transitSearch(transitResultIndex);
//				}
//			}
//
//			@Override
//			public void onNothingSelected(AdapterView<?> parent) {
//
//			}
//		});

		routePlanSearch = RoutePlanSearch.newInstance();
		routePlanSearch.setOnGetRoutePlanResultListener(routePlanResultListener);
	}
	
	// 获取用户所在地信息
	private void getUserLocationInfo(){
		SharedPreferences sharedPreferences = getSharedPreferences(Constants.USER_LOCATION_INFORMATION, Context.MODE_PRIVATE);
		userLatitude = Double.parseDouble(sharedPreferences.getString("latitude", ""));
		userLongitude = Double.parseDouble(sharedPreferences.getString("longitude", ""));
		userAddrStr = sharedPreferences.getString("addrStr", "");
		System.err.println("用户所在地的纬度------"+sharedPreferences.getString("latitude", ""));
		System.err.println("用户所在地的经度------"+sharedPreferences.getString("longitude", ""));
		System.err.println("用户所在地的userAddrStr------"+userAddrStr);
		System.err.println("street-----------"+sharedPreferences.getString("street", ""));
	}
	
	/**
	 * 显示弹出窗口覆盖物
	 */
	private void displayInfoWindow(final LatLng latLng,String address) {
		// 创建infowindow展示的view
		Button btn = new Button(getApplicationContext());
		btn.setBackgroundResource(R.drawable.popup);
		btn.setText(address);
		btn.setTextColor(getResources().getColor(R.color.black));
		BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromView(btn);
		// infowindow点击事件
		OnInfoWindowClickListener infoWindowClickListener = new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick() {
//				reverseGeoCode(latLng);
//				// 隐藏InfoWindow
//				bdMap.hideInfoWindow();
			}
		};
		// 创建infowindow
		InfoWindow infoWindow = new InfoWindow(bitmapDescriptor, latLng, -60,infoWindowClickListener);

		// 显示InfoWindow
		bdMap.showInfoWindow(infoWindow);
	}

	/**
	 * 路线规划结果回调
	 */
	OnGetRoutePlanResultListener routePlanResultListener = new OnGetRoutePlanResultListener() {

		/**
		 * 步行路线结果回调
		 */
		@Override
		public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
			bdMap.clear();
			if (walkingRouteResult == null || walkingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
				Toast.makeText(RoutePlanningActivity.this, "抱歉，未找到结果",Toast.LENGTH_SHORT).show();
			}
			if (walkingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
				// TODO
				return;
			}
			if (walkingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
				WalkingRouteOverlay walkingRouteOverlay = new WalkingRouteOverlay(bdMap);
				walkingRouteOverlay.setData(walkingRouteResult.getRouteLines().get(drivintResultIndex));
				bdMap.setOnMarkerClickListener(walkingRouteOverlay);
				walkingRouteOverlay.addToMap();
				walkingRouteOverlay.zoomToSpan();
				totalLine = walkingRouteResult.getRouteLines().size();
				Toast.makeText(RoutePlanningActivity.this,"共查询出" + totalLine + "条符合条件的线路", 1000).show();
				if (totalLine > 1) {
					nextLineBtn.setEnabled(true);
				}
			}
		}

		/**
		 * 换乘路线结果回调
		 */
		@Override
		public void onGetTransitRouteResult(
				TransitRouteResult transitRouteResult) {
			bdMap.clear();
			if (transitRouteResult == null || transitRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
				Toast.makeText(RoutePlanningActivity.this, "抱歉，未找到结果",Toast.LENGTH_SHORT).show();
			}
			if (transitRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
				// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
				// drivingRouteResult.getSuggestAddrInfo()
				return;
			}
			if (transitRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
				TransitRouteOverlay transitRouteOverlay = new TransitRouteOverlay(bdMap);
				transitRouteOverlay.setData(transitRouteResult.getRouteLines().get(drivintResultIndex));// 设置一条驾车路线方案
				bdMap.setOnMarkerClickListener(transitRouteOverlay);
				transitRouteOverlay.addToMap();
				transitRouteOverlay.zoomToSpan();
				totalLine = transitRouteResult.getRouteLines().size();
				Toast.makeText(RoutePlanningActivity.this,"共查询出" + totalLine + "条符合条件的线路", 1000).show();
				if (totalLine > 1) {
					nextLineBtn.setEnabled(true);
				}
				// 通过getTaxiInfo()可以得到很多关于打车的信息
				Toast.makeText(RoutePlanningActivity.this,"该路线打车总路程"+ transitRouteResult.getTaxiInfo().getDistance(), 1000).show();
			}
		}

		/**
		 * 驾车路线结果回调   查询的结果可能包括多条驾车路线方案
		 */
		@Override
		public void onGetDrivingRouteResult(
				DrivingRouteResult drivingRouteResult) {
			bdMap.clear();
			if (drivingRouteResult == null
					|| drivingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
				Toast.makeText(RoutePlanningActivity.this, "抱歉，未找到结果",
						Toast.LENGTH_SHORT).show();
			}
			if (drivingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
				// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
				// drivingRouteResult.getSuggestAddrInfo()
				return;
			}
			if (drivingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
				DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
						bdMap);
				drivingRouteOverlay.setData(drivingRouteResult.getRouteLines()
						.get(drivintResultIndex));// 设置一条驾车路线方案
				bdMap.setOnMarkerClickListener(drivingRouteOverlay);
				drivingRouteOverlay.addToMap();
				drivingRouteOverlay.zoomToSpan();
				totalLine = drivingRouteResult.getRouteLines().size();
				Toast.makeText(RoutePlanningActivity.this,
						"共查询出" + totalLine + "条符合条件的线路", 1000).show();
				if (totalLine > 1) {
					nextLineBtn.setEnabled(true);
				}
				// 通过getTaxiInfo()可以得到很多关于打车的信息
				Toast.makeText(
						RoutePlanningActivity.this,
						"该路线打车总路程"
								+ drivingRouteResult.getTaxiInfo()
										.getDistance(), 1000).show();
			}
		}
	};

	/**
	 * 驾车线路查询
	 */
	private void drivingSearch(int index) {
		DrivingRoutePlanOption drivingOption = new DrivingRoutePlanOption();
//		drivingOption.policy(DrivingPolicy.values()[drivingSpinner.getSelectedItemPosition()]);// 设置驾车路线策略
		// 通过名称节点设置出行线路
//		drivingOption.from(PlanNode.withCityNameAndPlaceName(city, userAddrStr));// 设置起点
//		drivingOption.to(PlanNode.withCityNameAndPlaceName(city, shopAddress));// 设置终点
		// 通过经纬度坐标设置出行线路
		drivingOption.from(PlanNode.withLocation(new LatLng(userLatitude, userLongitude)));
		drivingOption.to(PlanNode.withLocation(new LatLng(shopLatitude,shopLontitude)));
		routePlanSearch.drivingSearch(drivingOption);// 发起驾车路线规划
	}

	/**
	 * 换乘路线查询
	 */
	private void transitSearch(int index) {
		TransitRoutePlanOption transitOption = new TransitRoutePlanOption();
		transitOption.city(city);// 设置换乘路线规划城市，起终点中的城市将会被忽略
		// 通过名称节点设置出行线路
//		transitOption.from(PlanNode.withCityNameAndPlaceName(city, userAddrStr));
//		transitOption.to(PlanNode.withCityNameAndPlaceName(city, shopAddress));
		// 通过经纬度坐标设置出行线路
		transitOption.from(PlanNode.withLocation(new LatLng(userLatitude, userLongitude)));
		transitOption.to(PlanNode.withLocation(new LatLng(shopLatitude,shopLontitude)));
//		transitOption.policy(TransitPolicy.values()[transitSpinner.getSelectedItemPosition()]);// 设置换乘策略
		routePlanSearch.transitSearch(transitOption);
	}

	/**
	 * 步行路线查询
	 */
	private void walkSearch() {
		WalkingRoutePlanOption walkOption = new WalkingRoutePlanOption();
		// 通过名称节点设置出行线路
//		walkOption.from(PlanNode.withCityNameAndPlaceName(city, userAddrStr));
//		walkOption.to(PlanNode.withCityNameAndPlaceName(city, shopAddress));
		// 通过经纬度坐标设置出行线路
		walkOption.from(PlanNode.withLocation(new LatLng(userLatitude, userLongitude)));
		walkOption.to(PlanNode.withLocation(new LatLng(shopLatitude,shopLontitude)));
		routePlanSearch.walkingSearch(walkOption);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.drive_btn:// 驾车
			index = 0;
			drivintResultIndex = 0;
//			startPlace = startEt.getText().toString();
//			endPlace = endEt.getText().toString();
			driveBtn.setEnabled(false);
			transitBtn.setEnabled(true);
			walkBtn.setEnabled(true);
			nextLineBtn.setEnabled(false);
			drivingSearch(drivintResultIndex);
			break;
		case R.id.transit_btn:// 换乘
			index = 1;
			transitResultIndex = 0;
//			startPlace = startEt.getText().toString();
//			endPlace = endEt.getText().toString();
			transitBtn.setEnabled(false);
			driveBtn.setEnabled(true);
			walkBtn.setEnabled(true);
			nextLineBtn.setEnabled(false);
			transitSearch(transitResultIndex);
			break;
		case R.id.walk_btn:// 步行
			index = 2;
//			startPlace = startEt.getText().toString();
//			endPlace = endEt.getText().toString();
			walkBtn.setEnabled(false);
			driveBtn.setEnabled(true);
			transitBtn.setEnabled(true);
			nextLineBtn.setEnabled(false);
			walkSearch();
			break;
		case R.id.nextline_btn:// 下一条
			switch (index) {
			case 0:
				drivingSearch(++drivintResultIndex);
				break;
			case 1:
				transitSearch(transitResultIndex);
				break;
			case 2:

				break;
			}
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		routePlanSearch.destroy();// 释放检索实例
		mapView.onDestroy();
	}

}
