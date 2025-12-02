package com.cookandroid.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.converter.gson.GsonConverterFactory;

public class MapFragment extends Fragment implements OnMapReadyCallback, OnCameraIdleListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private CheckBox checkGym, checkPark, checkTrack, checkCenter;
    private final HashMap<String, List<Marker>> markerMap = new HashMap<>();
    private android.location.Location lastLocation;
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/"; // Web API의 기본 URL
    private PlacesApiService apiService;

    // 마커 색상 지정을 위한 상수 정의
    private static final float HUE_PARK = BitmapDescriptorFactory.HUE_BLUE;
    private static final float HUE_GYM = BitmapDescriptorFactory.HUE_ORANGE;
    private static final float HUE_TRACK = BitmapDescriptorFactory.HUE_GREEN;
    private static final float HUE_CENTER = BitmapDescriptorFactory.HUE_RED;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // 체크박스 연결
        checkGym = view.findViewById(R.id.check_gym);
        checkPark = view.findViewById(R.id.check_park);
        checkTrack = view.findViewById(R.id.check_track);
        checkCenter = view.findViewById(R.id.check_center);

        // 체크박스 이벤트 등록
        checkGym.setOnCheckedChangeListener((v, checked) ->
                toggleSearch("gym", Place.Type.GYM, checked));

        checkPark.setOnCheckedChangeListener((v, checked) ->
                toggleSearch("park", Place.Type.PARK, checked));

        checkTrack.setOnCheckedChangeListener((v, checked) ->
                toggleSearch("track", Place.Type.STADIUM, checked));

        checkCenter.setOnCheckedChangeListener((v, checked) ->
                toggleSearch("sports center", Place.Type.HEALTH, checked));

        // Retrofit 초기화
        retrofit2.Retrofit retrofit = new retrofit2.Retrofit.Builder()
                .baseUrl(BASE_URL)
                // PlacesResponse에서 내부 클래스(Result, Geometry 등)를 사용하려면 Gson 컨버터 필요
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(PlacesApiService.class);

        // 현재 위치 제공 서비스
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    100
            );
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                lastLocation = location;

                LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 17));
                mMap.setMyLocationEnabled(true);
            } else {
                Toast.makeText(requireContext(),
                        "위치를 가져올 수 없습니다. 기본 위치를 사용합니다.",
                        Toast.LENGTH_SHORT).show();

                if (lastLocation == null) {
                    lastLocation = new Location("manual");
                    lastLocation.setLatitude(37.2215);
                    lastLocation.setLongitude(127.1868);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {

        if (requestCode == 100) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation();
            } else {
                Toast.makeText(requireContext(),
                        "위치 권한이 거부되었습니다. 기본 위치(명지대)를 사용합니다.",
                        Toast.LENGTH_SHORT).show();
                // 권한이 없으면 명지대 좌표가 초기값으로 설정된 상태를 유지
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraIdleListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        LatLng myongji = new LatLng(37.2215, 127.1868);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myongji, 17));

        requestLocation();
    }

    // 체크박스 선택
    private void toggleSearch(String key, Place.Type type, boolean checked) {
        Log.d("DEBUG", "toggleSearch 실행됨 → key = " + key + ", checked = " + checked);
        if (checked) searchNearbyPlaces(key, type);
        else clearMarkers(key);
    }

    // 체크박스 검색 (Retrofit NearBy Search)
    private void searchNearbyPlaces(String keyword, Place.Type type) {

        // 기존 마커 제거
        clearMarkers(keyword);

        // 검색 파라미터 준비
        String locationStr = lastLocation.getLatitude() + "," + lastLocation.getLongitude();
        String typeStr = type.toString().toLowerCase();
        int radius = 5000;

        // 마커 색상 결정
        final float markerColor;
        if ("park".equals(keyword)) {
            markerColor = HUE_PARK;
        } else if ("gym".equals(keyword)) {
            markerColor = HUE_GYM;
        } else if ("track".equals(keyword)) {
            markerColor = HUE_TRACK;
        } else if ("sports center".equals(keyword)) {
            markerColor = HUE_CENTER;
        } else {
            markerColor = BitmapDescriptorFactory.HUE_RED; // 기본값
        }

        // Retrofit API 호출 및 응답 처리
        apiService.searchNearby(
                locationStr,
                radius,
                typeStr,
                BuildConfig.PLACES_API_KEY
        ).enqueue(new retrofit2.Callback<PlacesResponse>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<PlacesResponse> call, @NonNull retrofit2.Response<PlacesResponse> response) {

                // 응답이 성공적이고 바디가 null이 아닐 때
                if (response.isSuccessful() && response.body() != null) {
                    PlacesResponse placesResponse = response.body();

                    // API 호출 상태 확인
                    if (!"OK".equals(placesResponse.getStatus())) {
                        Log.e("WebAPI Failure", "API Status Error: " + placesResponse.getStatus());
                        // ⚠️ 여기서 REQUEST_DENIED 오류를 확인할 수 있습니다.
                        return;
                    }

                    // 결과 처리 및 마커 추가
                    List<PlacesResponse.Result> results = placesResponse.getResults();
                    List<Marker> newMarkers = new ArrayList<>();

                    Log.d("WebAPI Success", "Found " + results.size() + " places for type: " + typeStr);

                    for (PlacesResponse.Result result : results) {
                        String name = result.getName();
                        PlacesResponse.Location loc = result.getGeometry().getLocation();

                        LatLng pos = new LatLng(loc.getLat(), loc.getLng());

                        // 지도에 마커 추가
                        Marker marker = mMap.addMarker(
                                new MarkerOptions().position(pos).title(name).icon(com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                                        markerColor)).flat(true)
                        );
                        newMarkers.add(marker);
                    }

                    // 맵에 마커 목록 저장
                    markerMap.put(keyword, newMarkers);
                } else {
                    Log.e("WebAPI Failure", "Response not successful. Code: " + response.code() + ", Message: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<PlacesResponse> call, @NonNull Throwable t) {
                Log.e("WebAPI Failure", "Network Error: " + t.getMessage());
            }
        });
    }


    // 체크박스 해제
    private void clearMarkers(String keyword) {
        if (!markerMap.containsKey(keyword)) return;

        for (Marker m : markerMap.get(keyword)) {
            m.remove();
        }
        markerMap.remove(keyword);
    }

    @Override
    public void onCameraIdle() {
        LatLng centerLatLng = mMap.getCameraPosition().target;

        if (lastLocation == null) {
            lastLocation = new android.location.Location("manual");
        }

        lastLocation.setLatitude(centerLatLng.latitude);
        lastLocation.setLongitude(centerLatLng.longitude);

        if (checkPark.isChecked()) {
            searchNearbyPlaces("park", Place.Type.PARK);
        }

        if (checkGym.isChecked()) {
            searchNearbyPlaces("gym", Place.Type.GYM);
        }

        if (checkTrack.isChecked()) {
            searchNearbyPlaces("track", Place.Type.STADIUM);
        }

        if (checkCenter.isChecked()) {
            searchNearbyPlaces("sports center", Place.Type.HEALTH);
        }
    }
}
