package com.mcuhq.simplebluetooth2.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.library.KTLibrary.fragment.ArrFragment
import com.library.KTLibrary.fragment.SummaryFragment
import com.mcuhq.simplebluetooth2.R
import com.mcuhq.simplebluetooth2.fragment.HomeFragment
import com.mcuhq.simplebluetooth2.fragment.ProfileFragment
import com.library.KTLibrary.viewmodel.SharedViewModel

class Activity_Main : AppCompatActivity() {
    var viewModel: SharedViewModel? = null
    var bottomNav: BottomNavigationView? = null
    private var home: Fragment? = null
    private var summary: Fragment? = null
    private var arrF: Fragment? = null
    private val workout: Fragment? = null
    private var profile: Fragment? = null
    var fragmentManager: FragmentManager? = null
    @SuppressLint("BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 화면 자동 꺼짐 방지
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        bottomNav = findViewById(R.id.bottom_navigation)
        fragmentManager = supportFragmentManager

        // Start Fragment
        home = HomeFragment()
        fragmentManager!!.beginTransaction().replace(R.id.main_frame, home as HomeFragment)
            .commit() //FrameLayout에 fragment.xml 띄우기


        // 바텀 네비게이션 버튼 클릭 시 프래그먼트 전환
        bottomNav?.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            val id = item.itemId
            when (id) {
                R.id.bottom_home -> {
                    if (home == null) {
                        home = HomeFragment()
                        fragmentManager!!.beginTransaction().add(R.id.main_frame,
                            home as HomeFragment
                        ).commit()
                    }
                    if (home != null) fragmentManager!!.beginTransaction().show(home as HomeFragment).commit()
                    if (summary != null) fragmentManager!!.beginTransaction().hide(summary!!)
                        .commit()
                    if (arrF != null) fragmentManager!!.beginTransaction().hide(arrF!!).commit()
                    if (workout != null) fragmentManager!!.beginTransaction().hide(workout).commit()
                    if (profile != null) fragmentManager!!.beginTransaction().hide(profile!!)
                        .commit()
                }

                R.id.bottom_summary -> {
                    if (summary == null) {
                        summary = SummaryFragment()
                        fragmentManager!!.beginTransaction().add(R.id.main_frame,
                            summary as SummaryFragment
                        ).commit()
                    }
                    if (home != null) fragmentManager!!.beginTransaction().hide(home as HomeFragment).commit()
                    if (summary != null) fragmentManager!!.beginTransaction().show(summary!!)
                        .commit()
                    if (arrF != null) fragmentManager!!.beginTransaction().hide(arrF!!).commit()
                    if (workout != null) fragmentManager!!.beginTransaction().hide(workout).commit()
                    if (profile != null) fragmentManager!!.beginTransaction().hide(profile!!)
                        .commit()
                    viewModel = ViewModelProvider(this@Activity_Main).get(
                        SharedViewModel::class.java
                    )
                    viewModel!!.setSummaryRefreshCheck(true)
                }

                R.id.bottom_arr -> {
                    if (arrF == null) {
                        val email = getSharedPreferences("User", MODE_PRIVATE).getString("email","")
                        arrF = ArrFragment(email)
                        fragmentManager!!.beginTransaction().add(R.id.main_frame,
                            arrF as ArrFragment
                        ).commit()
                    }
                    if (home != null) fragmentManager!!.beginTransaction().hide(home as HomeFragment).commit()
                    if (summary != null) fragmentManager!!.beginTransaction().hide(summary!!)
                        .commit()
                    if (arrF != null) fragmentManager!!.beginTransaction().show(arrF!!).commit()
                    if (workout != null) fragmentManager!!.beginTransaction().hide(workout).commit()
                    if (profile != null) fragmentManager!!.beginTransaction().hide(profile!!)
                        .commit()
                }

                R.id.bottom_profile -> {
                    if (profile == null) {
                        profile = ProfileFragment()
                        fragmentManager!!.beginTransaction().add(R.id.main_frame,
                            profile as ProfileFragment
                        ).commit()
                    }
                    if (home != null) fragmentManager!!.beginTransaction().hide(home as HomeFragment).commit()
                    if (summary != null) fragmentManager!!.beginTransaction().hide(summary!!)
                        .commit()
                    if (arrF != null) fragmentManager!!.beginTransaction().hide(arrF!!).commit()
                    if (workout != null) fragmentManager!!.beginTransaction().hide(workout).commit()
                    if (profile != null) fragmentManager!!.beginTransaction().show(profile!!)
                        .commit()
                }
            }
            true
        })
    }
}