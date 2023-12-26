package com.mcuhq.simplebluetooth2.profile

import android.annotation.SuppressLint
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mcuhq.simplebluetooth2.R
import com.library.KTLibrary.viewmodel.SharedViewModel

class profile_1 : Fragment() {
    var viewModel: SharedViewModel? = null
    var myEmail: String? = null
    private val saveButton: Button? = null
    private val sharedPreferences: SharedPreferences? = null
    private val dateSetListener: OnDateSetListener? = null
    private val profile1_name: EditText? = null
    private var profile1_number: EditText? = null
    private val profile1_height: EditText? = null
    private val profile1_weight: EditText? = null
    private val profile1_sleep1: EditText? = null
    private val profile1_sleep2: EditText? = null
    private var profile1check = false
    var sv: ScrollView? = null
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_profile1, container, false)
        sv = view.findViewById(R.id.scrollView1)
        val saveButton = view.findViewById<Button>(R.id.profile1_save)

        // 계정 정보
        viewModel = ViewModelProvider(requireActivity()).get(
            SharedViewModel::class.java
        )
        myEmail = viewModel!!.getMyEmail().toString()

        //개인정보 edittext
        profile1_number = view.findViewById(R.id.profile1_number)
        val args = arguments

        //SharedPreferences에서 개인정보 불러오기
        val sharedPreferences = activity?.getSharedPreferences(myEmail, Context.MODE_PRIVATE)
        val savedText2 = sharedPreferences?.getString("number", "01012345678")
        profile1check = sharedPreferences?.getBoolean("profile1check", false)!!
        profile1_number?.setText(savedText2)


        //개인정보 저장버튼
        saveButton.setOnClickListener {
            val textToSave2 = profile1_number?.getText().toString()

            // SharedPreferences를 이용하여 데이터 저장
            val sharedPreferences =
                activity?.getSharedPreferences("UserDetails", Context.MODE_PRIVATE)
            val editor = sharedPreferences?.edit()
            editor?.putString("number", textToSave2)
            editor?.putBoolean("profile1check", true)
            editor?.apply()
            viewModel = ViewModelProvider(requireActivity()).get(
                SharedViewModel::class.java
            )


            // 저장한 후, 필요한 작업 수행 (예: 토스트 메시지 표시 등)
            Toast.makeText(activity, resources.getString(R.string.saveData), Toast.LENGTH_SHORT)
                .show()
        }

        //입력할때 키보드에 대한 높이조절
        profile1_number?.setOnFocusChangeListener(OnFocusChangeListener { v, hasFocus ->
            KeyboardUp(
                200
            )
        })
        return view
    }

    fun KeyboardUp(size: Int) {
        sv!!.postDelayed({ sv!!.smoothScrollTo(0, size) }, 200)
    }
}