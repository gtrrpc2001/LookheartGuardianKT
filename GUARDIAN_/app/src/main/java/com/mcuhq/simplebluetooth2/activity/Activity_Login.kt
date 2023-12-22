package com.mcuhq.simplebluetooth2.activity

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.SpannedString
import android.text.TextWatcher
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.util.Base64
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mcuhq.simplebluetooth2.R
import com.mcuhq.simplebluetooth2.server.RetrofitServerManager
import com.mcuhq.simplebluetooth2.server.RetrofitServerManager.ServerTaskCallback
import java.nio.charset.StandardCharsets
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class Activity_Login : AppCompatActivity() {
    var retrofitServerManager: RetrofitServerManager? = null

    /*이메일/비밀번호 정규식*/ //region
    val emailPattern = "^[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,20}$"
    val passwordPattern = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&<>*~:`-]).{10,}$"
    val phoneNumberPattern = "^[0-9]{9,11}$"
    private var email: String? = ""
    private var password: String? = ""
    private var guardian: String? = ""

    //endregion
    /*editText*/ //region
    var emailEditText: EditText? = null
    var passwordEditText: EditText? = null
    var guardianEditText: EditText? = null

    //endregion
    /*button*/ //region
    var autoLoginButton: Button? = null
    var loginButton: Button? = null

    //endregion
    /*autologin*/ //region
    var autoLoginCheck = false
    var autoLogin = false

    //endregion
    /*check*/ //region
    var emailCheck: Boolean? = null
    var passwordCheck: Boolean? = null
    var guardianCheck: Boolean? = null
    lateinit var dataCheck: MutableMap<String, Boolean>

    //endregion
    var autoLoginImageButton: ImageButton? = null
    var sv: ScrollView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        retrofitServerManager = RetrofitServerManager()

        setViewID()

        // 입력 데이터 초기화
        setDataCheckClear()

        // 초기화
        setCheckClear()

        // edit Text hint set
        setHintText()
        val autoLoginSP = getSharedPreferences("autologin", MODE_PRIVATE)
        val editor = autoLoginSP.edit()
        autoLogin = autoLoginSP.getBoolean("autologin", false)
        //        autoLogin = true;
        if (autoLogin) {
            val intent = Intent(this@Activity_Login, Activity_Main::class.java)
            //            Intent intent = new Intent(Activity_Login.this, OverviewActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        // email event
        setEmailEditTextEvent()

        // password event
        setPasswordEditTextEvent()
        setguardianEditTextEvent()
        setButtonEvent(editor)
    }

    fun setguardianEditTextEvent() {
        guardianEditText!!.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                // 포커스가 없어질 때
                if (guardianCheck!!) {
                    // 입력 값이 유효한 경우
                } else {
                    // 입력 값이 유효하지 않은 경우
                }
            } else {
                // 포커스를 얻었을 때
                KeyboardUp(400)
            }
        }
        guardianEditText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                guardian = editable.toString()
                if (editable.toString().trim { it <= ' ' }.matches(phoneNumberPattern.toRegex())) {
                    // 매칭되는 경우
                    dataCheck!!["guardian"] = true
                } else {
                    // 매칭되지 않는 경우
                    dataCheck!!["guardian"] = false
                }
                // 유효성 체크
                guardianCheck = dataCheck!!["guardian"]
            }
        })
    }

    fun setPasswordEditTextEvent() {
        passwordEditText!!.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                // 포커스가 없어질 때
                if (passwordCheck!!) {
                    // 입력 값이 유효한 경우
                } else {
                    // 입력 값이 유효하지 않은 경우
                }
            } else {
                // 포커스를 얻었을 때
                KeyboardUp(400)
            }
        }
        passwordEditText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                password = s.toString()
                if (s.toString().trim { it <= ' ' }.matches(passwordPattern.toRegex())) {
                    // 매칭되는 경우
                    dataCheck!!["password"] = true
                } else {
                    // 매칭되지 않는 경우
                    dataCheck!!["password"] = false
                }
                // 유효성 체크
                passwordCheck = dataCheck!!["password"]
            }
        })
    }

    fun setButtonEvent(editor: SharedPreferences.Editor) {
        loginButton!!.setOnClickListener(View.OnClickListener { v ->
            // null 값 확인
            if ((email == null || email!!.isEmpty()) && (password == null || password!!.isEmpty()) && (guardian == null || guardian!!.isEmpty())) {
                Toast.makeText(
                    this@Activity_Login,
                    resources.getString(R.string.lignAlert),
                    Toast.LENGTH_SHORT
                ).show()
                return@OnClickListener
            } else if (email == null || email!!.isEmpty()) {
                Toast.makeText(
                    this@Activity_Login,
                    resources.getString(R.string.email_Hint),
                    Toast.LENGTH_SHORT
                ).show()
                return@OnClickListener
            } else if (password == null || password!!.isEmpty()) {
                Toast.makeText(
                    this@Activity_Login,
                    resources.getString(R.string.password_Hint),
                    Toast.LENGTH_SHORT
                ).show()
                return@OnClickListener
            } else if (guardian == null || guardian!!.isEmpty()) {
                Toast.makeText(
                    this@Activity_Login,
                    resources.getString(R.string.guardian_Hint),
                    Toast.LENGTH_SHORT
                ).show()
                return@OnClickListener
            }

            // 암호화
            val encPw = encryptECB("MEDSYSLAB.CO.KR.LOOKHEART.ENCKEY", password!!)
            retrofitServerManager!!.loginTask(
                email!!,
                encPw!!.trim { it <= ' ' },
                guardian!!,
                null,
                object : ServerTaskCallback {
                    override fun onSuccess(result: String?) {
                        if (result?.lowercase(Locale.getDefault())?.contains("true")!!) {
                            val sharedPref = getSharedPreferences(email, MODE_PRIVATE)
                            val userEditor = sharedPref.edit()
                            val emailSharedPreferences = getSharedPreferences("User", MODE_PRIVATE)
                            val emailEditor = emailSharedPreferences.edit()
                            userEditor.putString("email", email)
                            userEditor.putString("guardian", guardian)
                            userEditor.putString("password", encPw.trim { it <= ' ' })
                            userEditor.commit()
                            emailEditor.putString("email", email)
                            emailEditor.commit()
                            editor.putBoolean("autologin", autoLoginCheck)
                            editor.commit()
                            val intent = Intent(this@Activity_Login, Activity_Main::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                            runOnUiThread {
                                Toast.makeText(
                                    this@Activity_Login,
                                    resources.getString(R.string.loginSuccess),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            runOnUiThread {
                                val builder = AlertDialog.Builder(this@Activity_Login)
                                builder.setTitle(resources.getString(R.string.loginFailed))
                                    .setMessage(resources.getString(R.string.incorrectlyLogin))
                                    .setNegativeButton(resources.getString(R.string.ok)) { dialog, which ->
                                        // 취소 버튼 클릭 시 수행할 동작
                                        dialog.cancel() // 팝업창 닫기
                                    }
                                    .show()
                            }
                        }
                        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(v.windowToken, 0)
                    }

                    override fun onFailure(e: Exception?) {
                        runOnUiThread {
                            Toast.makeText(
                                this@Activity_Login,
                                resources.getString(R.string.serverErr),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
        })
    }

    fun setEmailEditTextEvent() {
        emailEditText!!.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                // 포커스가 없어질 때
                if (emailCheck!!) {
                    // 입력 값이 유효한 경우
                } else {
                    // 입력 값이 유효하지 않은 경우
                }
            } else {
                // 포커스를 얻었을 때
                KeyboardUp(400)
            }
        }
        emailEditText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                email = s.toString()
                if (s.toString().trim { it <= ' ' }.matches(emailPattern.toRegex())) {
                    // 매칭되는 경우
                    dataCheck!!["email"] = true
                } else {
                    // 매칭되지 않는 경우
                    dataCheck!!["email"] = false
                }
                // 유효성 체크
                emailCheck = dataCheck!!["email"]
            }
        })
    }

    fun setViewID() {
        // edit text
        emailEditText = findViewById(R.id.editEmail)
        passwordEditText = findViewById(R.id.editPassword)
        guardianEditText = findViewById(R.id.guardian_EditText)

        // auto Login Button
        autoLoginButton = findViewById(R.id.autoLogin)
        autoLoginImageButton = findViewById(R.id.autoLoginImage)
        loginButton = findViewById(R.id.loginButton)
        sv = findViewById(R.id.scrollView)
    }

    fun setDataCheckClear() {
        dataCheck = HashMap()
        dataCheck["email"] = false
        dataCheck["password"] = false
    }

    fun setCheckClear() {
        autoLoginCheck = false
        emailCheck = false
        passwordCheck = false
    }

    fun KeyboardUp(size: Int) {
        sv!!.postDelayed({ sv!!.smoothScrollTo(0, size) }, 200)
    }

    // 자동 로그인 클릭 이벤트
    fun autoLoginClickEvent(v: View?) {
        autoLoginCheck = !autoLoginCheck
        if (autoLoginCheck) {
            autoLoginImageButton!!.setImageResource(R.drawable.login_autologin_press)
        } else {
            autoLoginImageButton!!.setImageResource(R.drawable.login_autologin_normal)
        }
    }

    fun setHintText() {
        // EditText에 힌트 텍스트 스타일을 적용
        val emailHintText = resources.getString(R.string.email_Hint)
        val passwordHintText = resources.getString(R.string.password_Hint)
        val guardianHintText = resources.getString(R.string.guardian_Hint)

        // 힌트 텍스트에 스타일을 적용
        val ssEmail = SpannableString(emailHintText)
        val ssPassword = SpannableString(passwordHintText)
        val ssGuardian = SpannableString(guardianHintText)
        val assEmail = AbsoluteSizeSpan(12, true) // 힌트 텍스트 크기 설정
        val assPassword = AbsoluteSizeSpan(12, true)
        val assGuardian = AbsoluteSizeSpan(12, true)
        ssEmail.setSpan(assEmail, 0, ssEmail.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) // 크기 적용
        ssPassword.setSpan(assPassword, 0, ssPassword.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssGuardian.setSpan(assGuardian, 0, ssGuardian.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // 힌트 텍스트 굵기 설정
        ssEmail.setSpan(StyleSpan(Typeface.NORMAL), 0, ssEmail.length, 0) // 굵게
        ssPassword.setSpan(StyleSpan(Typeface.NORMAL), 0, ssPassword.length, 0)
        ssGuardian.setSpan(StyleSpan(Typeface.NORMAL), 0, ssGuardian.length, 0)

        // 스타일이 적용된 힌트 텍스트를 EditText에 설정
        val emailText = findViewById<View>(R.id.editEmail) as EditText
        val passwordText = findViewById<View>(R.id.editPassword) as EditText
        val guardianText = findViewById<View>(R.id.guardian_EditText) as EditText
        emailText.hint = SpannedString(ssEmail) // 크기가 적용된 힌트 텍스트 설정
        passwordText.hint = SpannedString(ssPassword)
        guardianText.hint = SpannedString(ssGuardian)
        emailText.setHintTextColor(Color.parseColor("#555555")) // 색 변
        passwordText.setHintTextColor(Color.parseColor("#555555"))
        guardianText.setHintTextColor(Color.parseColor("#555555"))
    }

    companion object {
        fun encryptECB(key: String, value: String): String? {
            try {
                val skeySpec = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "AES")
                val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
                cipher.init(Cipher.ENCRYPT_MODE, skeySpec)
                val encrypted = cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
                return Base64.encodeToString(encrypted, Base64.DEFAULT)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
    }
}