package com.mcuhq.simplebluetooth2.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mcuhq.simplebluetooth2.R
import com.mcuhq.simplebluetooth2.profile.profile_1
import com.library.KTLibrary.viewmodel.SharedViewModel

class ProfileFragment : Fragment() {
    var viewModel: SharedViewModel? = null
    var myEmail: String? = null
    private var profile_name: TextView? = null
    private var profile_email: TextView? = null
    private var profile_day: TextView? = null
    private var profile_logout: Button? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // 계정 정보
        viewModel = ViewModelProvider(requireActivity()).get(
            SharedViewModel::class.java
        )

        myEmail = viewModel!!.getMyEmail().toString()
        profile_name = view.findViewById(R.id.profile_name)
        profile_email = view.findViewById(R.id.profile_email)
        profile_day = view.findViewById(R.id.profile_day)
        profile_logout = view.findViewById(R.id.profile_logout_btn)


        //sharedpreferneces에서 불러오기 (이름,이메일)
        val sharedPreferences = activity?.getSharedPreferences(myEmail, Context.MODE_PRIVATE)
        val savedText1 = sharedPreferences?.getString("name", "")
        val savedText2 = sharedPreferences?.getString("email", "")
        val savedText3 = sharedPreferences?.getString("current_date", "")
        profile_name?.setText(savedText1)
        profile_email?.setText(savedText2)
        profile_day?.setText(savedText3)


        // 프래그먼트 전환 버튼
        val btn1 = view.findViewById<Button>(R.id.profile_information)


        //기본정보 프래그먼트(profile_1) 기본으로
        val childFragment = profile_1()
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.inner_fragment_container, childFragment)
        fragmentTransaction.commit()
        btn1.setTextColor(Color.BLACK)
        btn1.textSize = 20f
        btn1.setOnClickListener {
            val childFragment = profile_1()
            val fragmentManager = parentFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.inner_fragment_container, childFragment)
            fragmentTransaction.commit()
            btn1.setTextColor(Color.BLACK)
            btn1.textSize = 20f
        }


        //로그아웃버튼 눌렀을때
        profile_logout?.setOnClickListener(View.OnClickListener {
            val sharedPreferences =
                activity?.getSharedPreferences("autologin", Context.MODE_PRIVATE)
            val autoLoginCheck = sharedPreferences?.getBoolean("autologin", false)
            if (autoLoginCheck!!) {
                val editor = sharedPreferences.edit()
                editor.putBoolean("autologin", false)
                editor.apply()
            }

            // 다이얼로그 창을 띄웁니다.
            showConfirmationDialog()
        })
        // Inflate the layout for this fragment
        return view
    }

    //로그아웃 버튼으로 로그인페이지 이동
    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(resources.getString(R.string.logout))
            .setMessage(resources.getString(R.string.logoutHelp))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which -> activity?.finish() }
            .setNegativeButton(resources.getString(R.string.rejectLogout)) { dialog, which -> // 취소 버튼을 눌렀을 때 아무 작업 없이 다이얼로그 창을 닫습니다.
                dialog.dismiss()
            }
        val dialog: Dialog = builder.create()
        dialog.show()
    }
}