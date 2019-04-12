package com.hie2j.baidumappoisearchandrouteplanning;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.ArcOptions;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiBoundSearchOption;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorPlanNode;
import com.baidu.mapapi.search.route.IndoorRoutePlanOption;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRoutePlanOption;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
    private UiSettings mUiSettings;
    private PoiSearch mPoiSearch;
    private LocationClient mLocationClient;
    private LatLng mLatLng;
    private RoutePlanSearch mSearch;

    private EditText editText_1;
    private EditText editText_2;

    private static final String TAG = "MainActivity";

    //准备起终点信息
    private static PlanNode stNode = PlanNode.withCityNameAndPlaceName("广州", "长湴地铁站");
    private static PlanNode enNode = PlanNode.withCityNameAndPlaceName("广州", "广东交通职业技术学院南校区");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("POI检索和路线规划");

        editText_1 = findViewById(R.id.edit_1);
        editText_2 = findViewById(R.id.edit_2);

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        // mBaiduMap.showMapPoi(false);

        //开启地图的定位图层
        mBaiduMap.setMyLocationEnabled(true);

        //定位初始化
        mLocationClient = new LocationClient(this);

        //通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);

        //设置locationClientOption
        mLocationClient.setLocOption(option);

        //注册LocationListener监听器
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
        //开启地图定位图层
        mLocationClient.start();

        //创建路线规划检索实例
        mSearch = RoutePlanSearch.newInstance();

        mSearch.setOnGetRoutePlanResultListener(new OnGetRoutePlanResultListener() {
            //步行路线规划
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
                //创建WalkingRouteOverlay实例
                WalkingRouteOverlay overlay = new WalkingRouteOverlay(mBaiduMap);
                if (walkingRouteResult.getRouteLines().size() > 0) {
                    //获取路径规划数据,(以返回的第一条数据为例)
                    //为WalkingRouteOverlay实例设置路径数据
                    overlay.setData(walkingRouteResult.getRouteLines().get(0));
                    //在地图上绘制WalkingRouteOverlay
                    overlay.addToMap();
                }
            }
            //市内公交路线规划
            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
                //创建TransitRouteOverlay实例
                TransitRouteOverlay overlay = new TransitRouteOverlay(mBaiduMap);
                //获取路径规划数据,(以返回的第一条数据为例)
                //为TransitRouteOverlay实例设置路径数据
                if (transitRouteResult.getRouteLines().size() > 0) {
                    overlay.setData(transitRouteResult.getRouteLines().get(0));
                    //在地图上绘制TransitRouteOverlay
                    overlay.addToMap();
                }
            }
            //跨城公交路线规划
            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {
                //创建MassTransitRouteOverlay实例
                MassTransitRouteOverlay overlay = new MassTransitRouteOverlay(mBaiduMap);
                if (massTransitRouteResult.getRouteLines() != null && massTransitRouteResult.getRouteLines().size() > 0){
                    //获取路线规划数据（以返回的第一条数据为例）
                    //为MassTransitRouteOverlay设置数据
                    overlay.setData(massTransitRouteResult.getRouteLines().get(0));
                    //在地图上绘制Overlay
                    overlay.addToMap();
                }
            }
            //驾车路线规划
            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
                //创建DrivingRouteOverlay实例
                DrivingRouteOverlay overlay = new DrivingRouteOverlay(mBaiduMap);
                if (drivingRouteResult.getRouteLines().size() > 0) {
                    //获取路径规划数据,(以返回的第一条路线为例）
                    //为DrivingRouteOverlay实例设置数据
                    overlay.setData(drivingRouteResult.getRouteLines().get(0));
                    //在地图上绘制DrivingRouteOverlay
                    overlay.addToMap();
                }
            }
            //室内路线规划
            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {
                //创建IndoorRouteOverlay实例
                IndoorRouteOverlay overlay = new IndoorRouteOverlay(mBaiduMap);
                if (indoorRouteResult.getRouteLines() != null && indoorRouteResult.getRouteLines().size() > 0) {
                    //获取室内路径规划数据（以返回的第一条路线为例）
                    //为IndoorRouteOverlay实例设置数据
                    overlay.setData(indoorRouteResult.getRouteLines().get(0));
                    //在地图上绘制IndoorRouteOverlay
                    overlay.addToMap();
                }
            }
            //骑行路线规划
            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
                //创建BikingRouteOverlay实例
                BikingRouteOverlay overlay = new BikingRouteOverlay(mBaiduMap);
                if (bikingRouteResult.getRouteLines().size() > 0) {
                    //获取路径规划数据,(以返回的第一条路线为例）
                    //为BikingRouteOverlay实例设置数据
                    overlay.setData(bikingRouteResult.getRouteLines().get(0));
                    //在地图上绘制BikingRouteOverlay
                    overlay.addToMap();
                }
            }
        });

        //发起检索
        findViewById(R.id.btn5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearch.walkingSearch((new WalkingRoutePlanOption())
                        .from(stNode)
                        .to(enNode));
            }
        });

        findViewById(R.id.btn6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearch.bikingSearch((new BikingRoutePlanOption())
                        .from(stNode)
                        .to(enNode)
                        // ridingType  0 普通骑行，1 电动车骑行
                        // 默认普通骑行
                        .ridingType(0));
            }
        });

        findViewById(R.id.btn7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearch.drivingSearch((new DrivingRoutePlanOption())
                        .from(stNode)
                        .to(enNode));
            }
        });

        findViewById(R.id.btn8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PlanNode stNode1 = PlanNode.withCityNameAndPlaceName("北京", "天安门");
                PlanNode enNode1 = PlanNode.withCityNameAndPlaceName("上海", "东方明珠");

                mSearch.masstransitSearch((new MassTransitRoutePlanOption())
                        .from(stNode1)
                        .to(enNode1));
            }
        });

        findViewById(R.id.btn9).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearch.transitSearch((new TransitRoutePlanOption())
                        .from(stNode)
                        .to(enNode)
                        .city("广州"));
            }
        });

        findViewById(R.id.btn0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IndoorPlanNode startNode = new IndoorPlanNode(new LatLng(39.917380, 116.37978), "F1");
                IndoorPlanNode endNode = new IndoorPlanNode(new LatLng(39.917239, 116.37955), "F6");

                mSearch.walkingIndoorSearch(new IndoorRoutePlanOption()
                        .from(startNode)
                        .to(endNode));
            }
        });



        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                List<PoiInfo> poiInfos = poiResult.getAllPoi();
                for (int i = 0; i < poiInfos.size(); i++) {
                    Log.e(TAG, poiInfos.get(i).toString());
                }
                if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    mBaiduMap.clear();

                    //创建PoiOverlay对象
                    PoiOverlay poiOverlay = new PoiOverlay(mBaiduMap);

                    //设置Poi检索数据
                    poiOverlay.setData(poiResult);

                    //将poiOverlay添加至地图并缩放至合适级别
                    poiOverlay.addToMap();
                    poiOverlay.zoomToSpan();
                }
            }

            //废弃
            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

            }

            @Override
            public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {
                Log.e(TAG, poiDetailSearchResult.getPoiDetailInfoList().get(0).toString());
            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }
        });

        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPoiSearch.searchInCity(new PoiCitySearchOption().city(editText_1.getText().toString()).keyword(editText_2.getText().toString()));
            }
        });

        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPoiSearch.searchPoiDetail(new PoiDetailSearchOption().poiUids("8ba3655c28ee4226bca82270"));
            }
        });

        findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * 以广交院为中心，搜索半径1000米以内的餐厅
                 */
                mPoiSearch.searchNearby(new PoiNearbySearchOption()
                        .location(mLatLng)
                        .radius(1000)
                        .keyword("餐厅"));
            }
        });

        findViewById(R.id.btn4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * 设置矩形检索区域
                 */
                LatLngBounds searchBounds = new LatLngBounds.Builder()
                        .include(new LatLng(mLatLng.latitude+0.05, mLatLng.longitude-0.05))
                        .include(new LatLng(mLatLng.latitude-0.05, mLatLng.longitude+0.05))
                        .build();

                /**
                 * 在searchBounds区域内检索餐厅
                 */
                mPoiSearch.searchInBound(new PoiBoundSearchOption()
                        .bound(searchBounds)
                        .keyword("学校"));
            }
        });

        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Toast.makeText(MainActivity.this,
                        "单击 纬度" + latLng.latitude +
                                " 经度 " + latLng.longitude,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                Toast.makeText(MainActivity.this,
                        "MapPoi单击 " + mapPoi.getName() +
                                " 坐标 " + mapPoi.getPosition().toString(),
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        mBaiduMap.setOnMapDoubleClickListener(new BaiduMap.OnMapDoubleClickListener() {
            @Override
            public void onMapDoubleClick(LatLng latLng) {
                Toast.makeText(MainActivity.this,
                        "单击 纬度" + latLng.latitude +
                                " 经度 " + latLng.longitude,
                        Toast.LENGTH_SHORT).show();
                LatLng p1 = latLng;
                LatLng p2 = new LatLng(p1.latitude - 0.05, p1.longitude);
                LatLng p3 = new LatLng(p1.latitude - 0.05, p1.longitude + 0.05);
                LatLng p4 = new LatLng(p1.latitude, p1.longitude + 0.05);
                List<LatLng> points = new ArrayList<LatLng>();
                points.add(p1);
                points.add(p2);
                points.add(p3);
                points.add(p4);

                //设置折线的属性
                OverlayOptions mOverlayOptions = new PolylineOptions()
                        .width(10)
                        .color(0xAAFF0000)
                        .points(points);
                //在地图上绘制折线
                mBaiduMap.addOverlay(mOverlayOptions);


            }
        });

        mBaiduMap.setOnMapLongClickListener(new BaiduMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mLatLng = latLng;
                Toast.makeText(MainActivity.this,
                        "长按 纬度" + latLng.latitude +
                                " 经度 " + latLng.longitude,
                        Toast.LENGTH_SHORT).show();

                // 绘制矩形
//                drawRect(latLng);


                // 绘制Marker
//                drawMarker(latLng);

                // 绘制弧线
//                drawArc(latLng);

                // 绘制圆形
//                drawCircle(latLng);

                // 绘制文字
//                drawText(latLng);

                // 绘制信息窗
                drawInfoWindow(latLng);
            }
        });

        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(MainActivity.this,
                        marker.getTitle(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    private void drawInfoWindow(LatLng latLng) {
        //用来构造InfoWindow的Button
        Button button = new Button(getApplicationContext());
        button.setBackgroundResource(R.mipmap.ic_launcher);
        button.setText("InfoWindow");

//构造InfoWindow
//point 描述的位置点
//-100 InfoWindow相对于point在y轴的偏移量
        InfoWindow mInfoWindow = new InfoWindow(button, latLng, -100);

//使InfoWindow生效
        mBaiduMap.showInfoWindow(mInfoWindow);
    }

    private void drawText(LatLng latLng) {
        //文字覆盖物位置坐标
        LatLng llText = latLng;

//构建TextOptions对象
        OverlayOptions mTextOptions = new TextOptions()
                .text("百度地图SDK") //文字内容
                .bgColor(0xAAFFFF00) //背景色
                .fontSize(24) //字号
                .fontColor(0xFFFF00FF) //文字颜色
                .rotate(-90) //旋转角度
                .position(llText);

//在地图上显示文字覆盖物
        mBaiduMap.addOverlay(mTextOptions);
    }

    private void drawCircle(LatLng latLng) {

        //圆心位置
        LatLng center = latLng;

//构造CircleOptions对象
        CircleOptions mCircleOptions = new CircleOptions().center(center)
                .radius(1400)
                .fillColor(0xAA0000FF) //填充颜色
                .stroke(new Stroke(5, 0xAA00ff00)); //边框宽和边框颜色

//在地图上显示圆
        mBaiduMap.addOverlay(mCircleOptions);
    }

    private void drawArc(LatLng latLng) {
        LatLng p1 = latLng;
        LatLng p2 = new LatLng(p1.latitude - 0.02, p1.longitude + 0.02);
        LatLng p3 = new LatLng(p1.latitude, p1.longitude + 0.04);
        //构造ArcOptions对象
        OverlayOptions mArcOptions = new ArcOptions()
                .color(Color.RED)
                .width(10)
                .points(p1, p2, p3);

        //在地图上显示弧线
        mBaiduMap.addOverlay(mArcOptions);


    }

    private void drawMarker(LatLng latLng) {
        //定义Maker坐标点
        LatLng point = latLng;
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.marker);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .title("故宫博物院")
                .position(point)
                .icon(bitmap);
        //在地图上添加Marker，并显示
        mBaiduMap.addOverlay(option);
    }

    private void drawRect(LatLng latLng) {
        LatLng p1 = latLng;
        LatLng p2 = new LatLng(p1.latitude - 0.05, p1.longitude);
        LatLng p3 = new LatLng(p1.latitude - 0.05, p1.longitude + 0.05);
        LatLng p4 = new LatLng(p1.latitude, p1.longitude + 0.05);
        List<LatLng> points = new ArrayList<LatLng>();
        points.add(p1);
        points.add(p2);
        points.add(p3);
        points.add(p4);
        points.add(p1);

        //设置折线的属性
        OverlayOptions mOverlayOptions = new PolylineOptions()
                .width(10)
                .color(0xAAFF0000)
                .points(points);
        //在地图上绘制折线
        mBaiduMap.addOverlay(mOverlayOptions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null){
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
        }
    }
}
