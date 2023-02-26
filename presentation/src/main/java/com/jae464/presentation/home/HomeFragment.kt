package com.jae464.presentation.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.jae464.domain.model.post.Memo
import com.jae464.presentation.R
import com.jae464.presentation.base.BaseMapFragment
import com.jae464.presentation.databinding.FragmentHomeBinding
import com.jae464.presentation.databinding.ItemMemoMarkerBinding
import com.jae464.presentation.login.LoginActivity
import com.jae464.presentation.markerIconList
import com.jae464.presentation.regionToString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*


@AndroidEntryPoint
class HomeFragment : BaseMapFragment<FragmentHomeBinding>(R.layout.fragment_home),
    GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {

    private val TAG: String = "HomeFragment"

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var mapFragment: SupportMapFragment
    private var currentMarker: Marker? = null
    private lateinit var viewPagerAdapter: HomeViewPagerAdapter
    private var currentMemoId = -1L
    private var user = FirebaseAuth.getInstance().currentUser

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG,"onViewCreated")
        binding.viewModel = viewModel
        binding.memoPreview.memoCardView.visibility = View.INVISIBLE

        // Coroutine Test
//        CoroutineScope(Dispatchers.Main).launch {
//            delay(1000)
//            CoroutineScope(Dispatchers.Main).launch {
//                coroutineFun1()
//            }
//            CoroutineScope(Dispatchers.Main).launch {
//                coroutineFun2()
//            }
//
//        }
    }

    override fun onMapReady(map: GoogleMap) {
        Log.d(TAG, "onMapReady")
        super.onMapReady(map)
        map.apply {
            setMinZoomPreference(6.0f)
            setMaxZoomPreference(16.0f)
        }

        setUserLocation()

        val seoul = LatLng(37.554891, 126.970814)
        val location = getLocation()

        val defaultLatLng: LatLng = if (location == null) {
            seoul
        } else {
            LatLng(location.latitude, location.longitude)
        }

        mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment = mapFragment.also {
            val mapOptions = GoogleMapOptions().useViewLifecycleInFragment(true)
            SupportMapFragment.newInstance(mapOptions)
        }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 36F))

        initObserver()
        initListener()
        initAppBar()
        viewModel.getAllMemo()
    }

    private fun initAppBar() {
        val appBarConfiguration =
            AppBarConfiguration(findNavController().graph, binding.drawerLayout)
        binding.homeToolBar.setupWithNavController(findNavController(), appBarConfiguration)

        if (user == null) {
            binding.drawerNavigationView.inflateMenu(R.menu.drawer_menu_not_login)
        } else {
            binding.drawerNavigationView.inflateMenu(R.menu.drawer_menu)
        }
    }

    private fun initListener() {
        map.setOnMapClickListener { location ->

            // 현재 보여지고 있는 메모가 있는 경우 해당 메모를 지운다.
            if (binding.memoPreview.memoCardView.visibility == View.VISIBLE) {
                binding.memoPreview.memoCardView.visibility = View.GONE
                binding.currentLocationButton.visibility = View.VISIBLE
                return@setOnMapClickListener
            }

            displayCurrentMarker(location)
        }

        map.setOnMarkerClickListener(this)
        map.setOnInfoWindowClickListener(this)

        binding.postButton.setOnClickListener {
            currentMarker ?: return@setOnClickListener
            val bundle = Bundle().apply {
                putDouble("latitude", currentMarker!!.position.latitude)
                putDouble("longitude", currentMarker!!.position.longitude)
            }

            findNavController().navigate(
                R.id.action_home_to_post,
                bundle
            )
        }

        binding.currentLocationButton.setOnClickListener {
//            getLocation()
            val currentLocation = getLocation() ?: return@setOnClickListener
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(currentLocation.latitude, currentLocation.longitude), 16f), 1000, null)
        }

        // TODO 메모 프리뷰를 클릭하면, 해당 메모의 디테일 페이지로 이동한다.
        binding.memoPreview.memoCardView.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeToDetailMemo(currentMemoId)
            findNavController().navigate(
                action
            )
        }

        binding.chipGroupType.setOnCheckedStateChangeListener { group, checkedIds ->
            Log.d("HomeFragment", checkedIds.toString())
            // 모든 칩이 체크 해제되어 있는 경우 전체에 다시 체크함
            Log.d(TAG, checkedIds.toString())
            if (checkedIds.isEmpty()) {
                binding.chipTypeAll.isChecked = true
                return@setOnCheckedStateChangeListener
            }

            if (checkedIds.size > 1 && checkedIds.contains(R.id.chip_type_all)) {
                binding.chipTypeAll.isChecked = false
                return@setOnCheckedStateChangeListener
            }

            checkedIds.forEach {
                when (it) {
                    R.id.chip_type_all -> {
                        viewModel.getAllMemo()
                    }
                    R.id.chip_type_food -> {
                        viewModel.getMemoByCategory(com.jae464.domain.model.post.Category.RESTAURANT)
                    }
                    R.id.chip_type_tourist -> {
                        viewModel.getMemoByCategory(com.jae464.domain.model.post.Category.TOURIST)
                    }
                    R.id.chip_type_cafe -> {
                        viewModel.getMemoByCategory(com.jae464.domain.model.post.Category.CAFE)
                    }
                    R.id.chip_type_hotel -> {
                        viewModel.getMemoByCategory(com.jae464.domain.model.post.Category.HOTEL)
                    }
                    R.id.chip_type_other -> {
                        viewModel.getMemoByCategory(com.jae464.domain.model.post.Category.OTHER)
                    }
                }
            }
        }

        binding.chipTypeAll.setOnClickListener {
            clearChipGroup()
            binding.chipTypeAll.isChecked = true
        }

        binding.drawerNavigationView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.login -> {
                    val intent = Intent(context, LoginActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    //
    private fun initObserver() {
        Log.d(tag, "initObserver")
        viewModel.memoList.observe(viewLifecycleOwner) {memoList ->
            map.clear()
            memoList.forEach { memo ->

                // TODO 썸네일 가져오는 로직 추후 수정 필요
                val imagePathList = viewModel.getMemoImagePathList(memo.id)

                val thumbnailImage = if (imagePathList.isEmpty()) null else imagePathList[0]
                val thumbnailImageBitmap = thumbnailImage.let {path ->
                    BitmapFactory.decodeFile(path)
                }

                // TODO Glide 로 이미지 비동기로 로드하는거 필요

                val thumbnailMarkerView = ItemMemoMarkerBinding.inflate(LayoutInflater.from(context), null, false).apply {
                    if(thumbnailImageBitmap == null) {
                        cvThumbnail.setImageResource(R.drawable.ic_sample_profile)
                    }
                    else {
                        cvThumbnail.setImageBitmap(thumbnailImageBitmap)
                    }
                }

                val thumbnailBitmap = createDrawableFromView(requireContext(), thumbnailMarkerView.root)

                Log.d(TAG, memo.title)
                val resourceId = markerIconList[memo.category.ordinal] ?: R.drawable.marker

                val memoMarker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(memo.latitude, memo.longitude))
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        .icon(BitmapDescriptorFactory.fromBitmap((thumbnailBitmap!!)))

                    )
                memoMarker?.tag = memo
            }
        }

        viewModel.currentAddress.observe(viewLifecycleOwner) {
            currentMarker?.snippet = it
            currentMarker?.showInfoWindow()
        }


    }

    private fun clearChipGroup() {
        binding.chipTypeFood.isChecked = false
        binding.chipTypeCafe.isChecked = false
        binding.chipTypeHotel.isChecked = false
        binding.chipTypeOther.isChecked = false
    }

    override fun onMarkerClick(marker: Marker): Boolean {

        if (marker.tag == "current") {
            marker.remove()
            binding.postButton.visibility = View.GONE
            return false
        }

        val memo = marker.tag as Memo
        currentMemoId = memo.id
        val cameraUpdate =
            CameraUpdateFactory.newLatLngZoom(LatLng(memo.latitude, memo.longitude), 16F)

        map.animateCamera(cameraUpdate, 200, null)

        displayMemoPreview(memo)

        return true
    }

    private fun displayCurrentMarker(location: LatLng) {
        currentMarker?.remove()
        currentMarker = map.addMarker(
            MarkerOptions()
                .position(LatLng(location.latitude, location.longitude))
                .title("주소")
        )

        currentMarker?.tag = "current"
        val cameraUpdate =
            CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 16F)

        map.animateCamera(cameraUpdate, 500, null)
        viewModel.getAddressName(location.latitude, location.longitude)
        binding.postButton.visibility = View.VISIBLE
    }

    private fun displayMemoPreview(memo: com.jae464.domain.model.post.Memo) {
        currentMarker?.remove()

        val imageList = viewModel.getMemoImagePathList(memo.id)
        Log.d(TAG, imageList.toString())

        binding.memoPreview.memo = memo
        binding.memoPreview.locationTextView.text = regionToString(memo.area1, memo.area2, memo.area3)

        viewPagerAdapter = HomeViewPagerAdapter(imageList)
        binding.memoPreview.thumbnailViewPager.adapter = viewPagerAdapter
        binding.memoPreview.dotIndicator.attachTo(binding.memoPreview.thumbnailViewPager)

        binding.memoPreview.memoCardView.visibility = View.VISIBLE
        binding.currentLocationButton.visibility = View.GONE
        binding.postButton.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        map.clear()
    }

    override fun onInfoWindowClick(p0: Marker) {
        currentMarker ?: return
        Log.d(TAG, currentMarker.toString())
        val bundle = Bundle().apply {
            putDouble("latitude", currentMarker!!.position.latitude)
            putDouble("longitude", currentMarker!!.position.longitude)
        }

        findNavController().navigate(
            R.id.action_home_to_post,
            bundle
        )
    }

//    suspend fun coroutineFun1() {
//        for (i in 0..10) {
//            Log.d("coroutineFun1", i.toString())
//            delay(1000)
//        }
//    }
//
//    suspend fun coroutineFun2() {
//        for (i in 0..10) {
//            Log.d("coroutineFun2", i.toString())
//            delay(1000)
//        }
//    }

    // View를 Bitmap으로 변환
    private fun createDrawableFromView(context: Context, view: View): Bitmap? {
        val displayMetrics = DisplayMetrics()
//        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)

        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.buildDrawingCache()
        val bitmap =
            Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}


