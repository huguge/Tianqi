package com.eightzero.tianqi.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
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
public class RoutePlanningActivity extends Activity implements OnGetRoutePlanResultListener{

	private MapView mapView;
	private BaiduMap bdMap;
	
	// 搜索相关
    private RoutePlanSearch mSearch = null;// 路径规划搜索接口
    
    private List<TransitRouteLine> transitLineList = null;// 换乘线路
    private int busLineIndex = 0;
    
	// 浏览路线节点相关
	Button mBtnPre = null;// 上一个节点
	Button mBtnNext = null;// 下一个节点
	int nodeIndex = -1;// 节点索引,供浏览节点时使用
	RouteLine route = null;// 线路详情
    
    OverlayManager routeOverlay = null;
    boolean useDefaultIcon = false;
    
	private Button popupBtn = null;// 泡泡view
    
	LatLng userLatLng;// 用户经纬度
	LatLng shopLatLng;// 商户经纬度
	
	// 商户地址信息
	private String city;// 所在城市
	private Double shopLontitude;// 商家所在地区经度
	private Double shopLatitude;// 商家所在地区纬度
	private String shopAddress;// 商家所在地址
	
	// 用户地址信息
	private Double userLatitude;
	private Double userLongitude;
//	private String userAddrStr;
//	private String street;
	
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
		transitLineList = new ArrayList<TransitRouteLine>();
		getUserLocationInfo();// 获取用户地址信息
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		mapView = (MapView) findViewById(R.id.mapview);
		mBtnPre = (Button) findViewById(R.id.pre);
        mBtnNext = (Button) findViewById(R.id.next);
        
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
		
        // 初始化搜索模块，注册事件监听
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);
        
		mapView.showZoomControls(false);// 不显示默认的缩放控件
		bdMap = mapView.getMap();// 获取百度地图对象
		
		userLatLng = new LatLng(userLatitude,userLongitude);
		// 设定商家位置(中心点)坐标
		shopLatLng = new LatLng(shopLatitude,shopLontitude);// 注意这里的构造方法的参数-->(latitude,longitude),而不是百度地图上得到的是（longitude，latitude）
		// 定义地图状态
		MapStatus mMapStatus = new MapStatus.Builder()
		.target(shopLatLng)
		.zoom(16)
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
		bdMap.addOverlay(options);
		
		// 显示商家所在信息弹出窗
		displayInfoWindow(shopLatLng,shopAddress);
		
		// 构建用户所在地的maker图标
		BitmapDescriptor bitmap1 = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_geo);
		
		// 构建markerOption，用于在地图上添加用户marker
		OverlayOptions options1 = new MarkerOptions()//
				.position(userLatLng)// 设置marker的经纬度坐标
				.icon(bitmap1)// 设置marker的图标
				.zIndex(9)// 设置marker的所在层级
				.draggable(true);// 设置手势拖拽
		// 在地图上添加marker，并显示
		bdMap.addOverlay(options1);
	}
	
	/**
	 * 路线规划搜索
	 * @author huweiyang
	 * @param v
	 */
	public void SearchButtonProcess(View v) {
		// 重置浏览节点的路线数据
        route = null;
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
        bdMap.clear();
		// 设置起终点信息，对于tranist search 来说，城市名无意义
//        PlanNode stNode = PlanNode.withCityNameAndPlaceName(city, userAddrStr);
//        PlanNode enNode = PlanNode.withCityNameAndPlaceName(city, shopAddress);
        
        // 设置起终点经纬度
        PlanNode stNode = PlanNode.withLocation(userLatLng);
        PlanNode enNode = PlanNode.withLocation(shopLatLng);

        // 实际使用中请对起点终点城市进行正确的设定
        if (v.getId() == R.id.drive) {
            mSearch.drivingSearch((new DrivingRoutePlanOption())
                    .from(stNode)
                    .to(enNode));
        } else if (v.getId() == R.id.transit) {
            mSearch.transitSearch((new TransitRoutePlanOption())
                    .from(stNode)
                    .city(city)
                    .to(enNode));
        } else if (v.getId() == R.id.walk) {
            mSearch.walkingSearch((new WalkingRoutePlanOption())
                    .from(stNode)
                    .to(enNode));
        }
    }
	
	/**
     * 节点浏览
     *
     * @param v
     */
    public void nodeClick(View v) {
        if (route == null || route.getAllStep() == null) {
            return;
        }
        if (nodeIndex == -1 && v.getId() == R.id.pre) {
        	return;
        }
        //设置节点索引
        if (v.getId() == R.id.next) {
            if (nodeIndex < route.getAllStep().size() - 1) {
            	nodeIndex++;
            } else {
            	// 如果当前换乘线路小于总换乘线路，就继续进行换乘指引
        		if (busLineIndex < transitLineList.size()-1 && busLineIndex != -1) {
        			busLineIndex++;// 换乘线路加
                	route = transitLineList.get(busLineIndex);
                	nodeIndex = 0;
				} else {
					return;
				}
            }
        } else if (v.getId() == R.id.pre) {
        	if (nodeIndex > 0) {
        		nodeIndex--;
        	} else { 
        		// 如果当前换乘线路大于等于总线路，就回到线路1
        		if (busLineIndex >= transitLineList.size()-1 && busLineIndex != -1) {
        			busLineIndex = 0;
                	route = transitLineList.get(busLineIndex);
                	nodeIndex = 0;
				} else {
					return;
				}
            }
        }
		// 获取节结果信息
        LatLng nodeLocation = null;
        String nodeTitle = null;
        Object step = route.getAllStep().get(nodeIndex);
        if (step instanceof DrivingRouteLine.DrivingStep) {
            nodeLocation = ((DrivingRouteLine.DrivingStep) step).getEntrace().getLocation();
            nodeTitle = ((DrivingRouteLine.DrivingStep) step).getInstructions();
        } else if (step instanceof WalkingRouteLine.WalkingStep) {
            nodeLocation = ((WalkingRouteLine.WalkingStep) step).getEntrace().getLocation();
            nodeTitle = ((WalkingRouteLine.WalkingStep) step).getInstructions();
        } else if (step instanceof TransitRouteLine.TransitStep) {
            nodeLocation = ((TransitRouteLine.TransitStep) step).getEntrace().getLocation();
            nodeTitle = ((TransitRouteLine.TransitStep) step).getInstructions();
        }

        if (nodeLocation == null || nodeTitle == null) {
            return;
        }
		// 移动节点至中心
        bdMap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
		// show popup,这里popup本来是TextView的，但是TextView出来的文字左右都超出泡泡了，用Button效果居然好一点，就这么用了
        popupBtn = new Button(RoutePlanningActivity.this);
        popupBtn.setBackgroundResource(R.drawable.popup);
        popupBtn.setTextColor(getResources().getColor(R.color.black));
        popupBtn.setTextSize(14);
        popupBtn.setText(nodeTitle);
        bdMap.showInfoWindow(new InfoWindow(popupBtn, nodeLocation, 0));
    }
    
	@Override
	public void onGetDrivingRouteResult(DrivingRouteResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanningActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            //result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            busLineIndex = -1;// 给换乘线路下标赋值，使不与自驾线路混淆
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);
            route = result.getRouteLines().get(0);
        	DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(bdMap);
            routeOverlay = overlay;
            bdMap.setOnMarkerClickListener(overlay);
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
	}

	@Override
	public void onGetTransitRouteResult(TransitRouteResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanningActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			// result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            busLineIndex = 0;// 重新对换乘线路初始值赋值
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);
            for (TransitRouteLine line:result.getRouteLines()) {
            	transitLineList.add(line);
			}
            route = transitLineList.get(busLineIndex);
        	TransitRouteOverlay overlay = new MyTransitRouteOverlay(bdMap);
            bdMap.setOnMarkerClickListener(overlay);
            routeOverlay = overlay;
            overlay.setData(transitLineList.get(busLineIndex));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
	}

	@Override
	public void onGetWalkingRouteResult(WalkingRouteResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanningActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			// result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            busLineIndex = -1;// 给换乘线路下标赋值，使不与步行线路混淆
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);
            route = result.getRouteLines().get(0);
        	WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(bdMap);
            bdMap.setOnMarkerClickListener(overlay);
            routeOverlay = overlay;
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
	}
	
	// 定制RouteOverly
    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    private class MyTransitRouteOverlay extends TransitRouteOverlay {

        public MyTransitRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }
	
	// 获取用户所在地信息
	private void getUserLocationInfo(){
		SharedPreferences sharedPreferences = getSharedPreferences(Constants.USER_LOCATION_INFORMATION, Context.MODE_PRIVATE);
		userLatitude = Double.parseDouble(sharedPreferences.getString("latitude", ""));
		userLongitude = Double.parseDouble(sharedPreferences.getString("longitude", ""));
//		userAddrStr = sharedPreferences.getString("addrStr", "");
	}
	
	/**
	 * 显示商户弹出窗口覆盖物
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
		mSearch.destroy();// 释放检索实例
		mapView.onDestroy();
	}

}
