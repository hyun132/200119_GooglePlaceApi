package com.example.a200119_googleplaceapi

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : AppCompatActivity() {

    var googleMap: GoogleMap? = null
    var locManager: LocationManager? = null

    var permission_list = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //        안드로이드 버전이 마쉬멜로우 이상일 경우 권한 체크
            requestPermissions(permission_list, 0)
        } else {
//            마쉬멜로우 이전 버전인경우 권한요구하지않고 바로 init
            init()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (result in grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
//                권한 거절된거있으면 바로 리턴
                return
            }
        }
//        권한이 모두 허용되었다면 init실행
        init()
    }

    fun init() {
//        mapFragment 로 googlemap을 표시해주고
        var callback = mapReadyCallBack()
        var mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getMapAsync(callback)
    }

    //    구글 맵 로드하는데는 시간이 걸리기 때문에 그 이전에 화면에 처리해주는? 단계 필요
    inner class mapReadyCallBack : OnMapReadyCallback {
        //    map fragment가 googleMap객체 사용할 준비가 되면 이 객체가 실행됨.
        override fun onMapReady(p0: GoogleMap?) {
//    mapFragment가 실행된 준비가 되면 googlemap과 매핑
            googleMap = p0
            getMyLocation()
        }
    }

    fun getMyLocation() {
        locManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                return
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                return
            }
        }

//        현재 내 위치 찾기 전에 최근 위치를 찾아서 임시로 보여주는 부분
//        gps가 network보다 조금 더 정확하나 조금 더 오래걸림
        var gpsLocation = locManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        var networkLocation = locManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (gpsLocation != null) {
            setMyLocation(gpsLocation)
        } else if (networkLocation != null) {
            setMyLocation(networkLocation!!)
        }

        var listener = getMyLocationListener()

//        GpsProvider로 위치 측정이 가능한 경우
        if (locManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)!! == true) {
            locManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10f, listener)
        } else if (locManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true) {
            locManager?.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000,
                10f,
                listener
            )
        }
    }

    fun setMyLocation(location: Location) {
        Log.d("내 위치 :", "위도 ${location.latitude}")
        Log.d("내 위치 :", "경도 ${location.longitude}")

//        받아온 location을 position에 저장
        var position = LatLng(location.latitude, location.longitude)

//        카메라의 position을 받아온 position으로 설정해줌
        var updateCamera = CameraUpdateFactory.newLatLng(position)
//        지도에서 카메라로 받아온 위치를 보여줌
        googleMap?.moveCamera(updateCamera)
        googleMap?.animateCamera(CameraUpdateFactory.zoomTo(15f))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                return
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                return
            }
        }
//        내 위치 지도에 찍기
        googleMap?.isMyLocationEnabled = true

//        맵 타입 설정 (위성,none 등) default는 normal
        googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
    }

    inner class getMyLocationListener : LocationListener {
        //        gps로 위치 찾으면 호출되는부분
        override fun onLocationChanged(location: Location?) {
            setMyLocation(location!!)
            locManager?.removeUpdates(this)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

        }

        override fun onProviderEnabled(provider: String?) {

        }

        override fun onProviderDisabled(provider: String?) {

        }

    }


}
