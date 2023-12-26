package com.mcuhq.simplebluetooth2.fragment

//import androidx.activity.OnBackPressedDispatcher.addCallback
import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.library.KTLibrary.viewmodel.SharedViewModel
import com.mcuhq.simplebluetooth2.R
import com.mcuhq.simplebluetooth2.firebase.FirebaseMessagingService
import com.mcuhq.simplebluetooth2.server.RetrofitServerManager
import com.mcuhq.simplebluetooth2.server.RetrofitServerManager.ArrDataCallback
import com.mcuhq.simplebluetooth2.server.RetrofitServerManager.HourlyDataCallback
import com.mcuhq.simplebluetooth2.server.RetrofitServerManager.RealBpmCallback
import com.mcuhq.simplebluetooth2.server.RetrofitServerManager.UserDataCallback
import com.mcuhq.simplebluetooth2.server.model.UserProfile
import com.mcuhq.simplebluetooth2.service.ForegroundService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Arrays
import java.util.Date
import java.util.LinkedList
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {
    /*ArrCount*/ //region
    private var preArr = 0 // Yesterday ArrCnt
    private var currentArrCnt = 0 // 현재 시간 기준 발생한 비정상맥박 횟수
    private var previousArrCnt = 0 // 이전에 발생한 비정상맥박 횟수
    private val arrCnt = 0
    private var serverArrCnt = 0 // 화면 상단에 표시되는 arrCnt

    //endregion
    /*check*/ //region
    private var arrCheck = false // 두번째 부터 알림 뜨게 하는 Flag
    private var hourlyArrCheck = false // 두번째 부터 알림 뜨게 하는 Flag
    private var HeartAttackCheck = false

    //endregion
    /*Arr variables*/ //region
    private val arrList = ArrayList<String>()
    private var arrIdx = "0"
    private val bpmLastLines = LinkedList<Double>() // bpm graph
    private val realBPM = 0.0
    private var doubleTEMP = 0.0
    private var allstep = 0
    private var distance = 0.0
    private var dCal = 0.0
    private var dExeCal = 0.0

    //endregion
    /*SharedPreferences variables*/ //region
    var userDetailsSharedPref: SharedPreferences? = null
    var userDetailsEditor: SharedPreferences.Editor? = null

    //endregion
    /*ServerTask variables*/ //region
    var retrofitServerManager: RetrofitServerManager? = null
    private var timeLoop = true
    private var disposable: Disposable? = null

    //endregion
    /*Server 요청 시작 날짜 저장 변수*/ //region
    var preDate: String? = null
    var bpmStartDate: String? = null
    var arrStartDate: String? = null
    var hourlyStartDate: String? = null

    //endregion
    /*Scheduler*/ //region
    var bpmScheduler = Executors.newScheduledThreadPool(1)
    var bpmDataScheduler = Executors.newScheduledThreadPool(1)
    var hourlyDataScheduler = Executors.newScheduledThreadPool(1)
    private var notificationId = 0
    private var notificationsPermissionCheck = false
    private var notificationManager: NotificationManager? = null

    //endregion
    /*Permission variables*/ //region
    private val permissions = ArrayList<String>()
    private var PERMISSIONS: Array<String>? = null
    private lateinit var multiplePermissionsContract: ActivityResultContracts.RequestMultiplePermissions
    private lateinit var multiplePermissionLauncher: ActivityResultLauncher<Array<String>>

    //endregion
    /*Profile variables*/ //region
    private var myEmail: String? = null
    private var sleep = 0
    private var wakeup = 0
    private var eCalBPM = 0
    private val currentDTHandler = Handler(Looper.getMainLooper()) // DateTime Handler
    private var currentDateTime: Date? = null
    private var currentYear: String? = null
    private var currentMonth: String? = null
    private var currentDay: String? = null
    private var currentHour: String? = null
    private var currentDate: String? = null
    private var currentTime: String? = null
    private var targetDate: String? = null // currentDate 기준 다음 날을 저장하는 변수 (DB Select)

    //endregion
    /*TextView variables*/ //region
    var arrStatus: TextView? = null
    var exerciseText: TextView? = null
    var preArrLabel: TextView? = null
    var restText: TextView? = null
    var sleepText: TextView? = null
    var arr_value: TextView? = null
    var bpm_value: TextView? = null
    var eCal_value: TextView? = null
    var preArr_value: TextView? = null
    var distance_value: TextView? = null
    var step_value: TextView? = null
    var temp_value: TextView? = null

    //endregion
    /*ImageView variables*/ //region
    var exerciseImg: ImageView? = null
    var filledHeart: ImageView? = null
    var restImg: ImageView? = null
    var sleepImg: ImageView? = null

    //endregion
    /*LinearLayout variables*/ //region
    var exerciseBackground: LinearLayout? = null
    var restBackground: LinearLayout? = null
    var sleepBackground: LinearLayout? = null

    //endregion
    /*Other layout variables*/ //region
    private var view: View? = null
    private var testButton: FrameLayout? = null
    private var chart: LineChart? = null

    //endregion
    /*BroadcastReceiver variables*/ //region
    private var messageReceiver: BroadcastReceiver? = null

    //endregion
    var serviceIntent: Intent? = null // Foreground Service var
    private var viewModel: SharedViewModel? = null // View Model var
    private var onBackPressedDialog: AlertDialog? = null // backPressed
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 뷰 초기화
        view = inflater.inflate(R.layout.fragment_home, container, false)

        // 사용자 정보 가져오기
        retrieveUserDetails()

        // UI 컴포넌트 초기화
        initializeUIComponents()

        // 데이터와 서비스 초기화
        initializeDataAndServices()

        // 이벤트 처리
        setEventListeners()
        return view
    }

    private fun retrieveUserDetails() {
        val emailSharedPreferences =
            safeGetActivity()!!.getSharedPreferences("User", Context.MODE_PRIVATE)
        myEmail = emailSharedPreferences.getString("email", "null")
        viewModel = ViewModelProvider(requireActivity()).get(
            SharedViewModel::class.java
        )
        viewModel!!.setEmail(myEmail!!)
        userDetailsSharedPref =
            safeGetActivity()!!.getSharedPreferences(myEmail, Context.MODE_PRIVATE)
        userDetailsEditor = userDetailsSharedPref?.edit()

        // previous data select start date
        bpmStartDate = userDetailsSharedPref?.getString("bpmStartDate", "")
        arrStartDate = userDetailsSharedPref?.getString("arrStartDate", "")
        hourlyStartDate = userDetailsSharedPref?.getString("hourlyStartDate", "")
        preDate = userDetailsSharedPref?.getString("preDate", "2023-01-01")
    }

    private fun initializeUIComponents() {
        bpm_value = view?.findViewById(R.id.bpm_Value)
        arr_value = view?.findViewById(R.id.arr_value)
        eCal_value = view?.findViewById(R.id.eCal_Value)
        step_value = view?.findViewById(R.id.step_Value)
        temp_value = view?.findViewById(R.id.temp_Value)
        distance_value = view?.findViewById(R.id.distance_Value)
        preArr_value = view?.findViewById(R.id.homeArrValue)
        preArrLabel = view?.findViewById(R.id.preArrLabel)
        arrStatus = view?.findViewById(R.id.homeArrStatus)
        filledHeart = view?.findViewById(R.id.filledHeart)
        exerciseImg = view?.findViewById(R.id.exerciseImg)
        exerciseText = view?.findViewById(R.id.exerciseText)
        exerciseBackground = view?.findViewById(R.id.exercise)
        restImg = view?.findViewById(R.id.restImg)
        restText = view?.findViewById(R.id.restText)
        restBackground = view?.findViewById(R.id.rest)
        sleepImg = view?.findViewById(R.id.sleepImg)
        sleepText = view?.findViewById(R.id.sleepText)
        sleepBackground = view?.findViewById(R.id.sleep)
        chart = view?.findViewById(R.id.myChart)
        testButton = view?.findViewById(R.id.testButton)
    }

    private fun initializeDataAndServices() {
        retrofitServerManager = RetrofitServerManager()
        currentTimeCheck()
        dateCalculate(1, true)
        profile
        loadPreviousUserData()
        startTimeCheck()
        initVar()
        permissionsCheck()
        startService()
        setFCM()
    }

    private fun setEventListeners() {
        setOnBackPressed()
        testButton!!.setOnClickListener { v: View? ->
//            setFCM();
            profile
        }
    }

    fun dateCalculate(myDay: Int, startDate: String?): String {
        var startDate = startDate
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        var date: LocalDate
        date = LocalDate.parse(startDate, formatter)
        date = date.plusDays(myDay.toLong())
        startDate = date.format(formatter)
        date = LocalDate.parse(startDate, formatter)
        return date.toString()
    }

    fun loadPreviousUserData() {
        safeGetActivity()!!.runOnUiThread {
            Toast.makeText(
                activity,
                resources.getString(R.string.loadUserData),
                Toast.LENGTH_SHORT
            ).show()
        }

        val observable = Observable.create { emitter: ObservableEmitter<Boolean> ->
            try {
                loadPreviousBpmData(bpmStartDate)
                loadPreviousHourlyData(hourlyStartDate)
                loadPreviousArrData(arrStartDate)
                emitter.onNext(true)
                emitter.onComplete()
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }

        observable
            .subscribeOn(Schedulers.io()) // 작업을 IO 스레드에서 실행
            .observeOn(AndroidSchedulers.mainThread()) // 결과를 메인 스레드에서 받음
            .subscribe(object : Observer<Boolean> {
                override fun onSubscribe(d: Disposable) {
                    disposable = d
                }

                override fun onNext(aBoolean: Boolean) {
                    Log.i("onNext", "onNext")
                }

                override fun onError(e: Throwable) {
                    // 데이터 로딩 실패, 에러 메시지 표시
                    Toast.makeText(
                        activity,
                        resources.getString(R.string.failLoadUserData),
                        Toast.LENGTH_SHORT
                    ).show()
                    e.printStackTrace()
                }

                override fun onComplete() {
                    Log.i("onComplete", "onComplete")
                    realTimeBPMLoop()
                    bpmLoop()
                    hourlyDataLoop()
                    refreshArrData()
                }
            })
    }

    fun loadPreviousBpmData(startDate: String?) {
        retrofitServerManager!!.getBpmData(
            "BpmData",
            myEmail!!,
            startDate!!,
            targetDate!!,
            object : RetrofitServerManager.DataCallback {
                override fun getData(bpmData: List<Map<String, String>>?) {
                    val dataList = ArrayList<String>()
                    for (i in 1 until bpmData!!.size - 1) {
                        val startDate = bpmData[i]["time"]!!.split(" ").toTypedArray()
                        val nextDate = bpmData[i + 1]["time"]!!.split(" ").toTypedArray()
                        if (startDate[0] == nextDate[0]) {
                            dataList.add(bpmData[i].toString())
                        } else {
                            writeBpmDataToFile(startDate[0], dataList)
                            dataList.clear()
                        }
                        if (i == bpmData!!.size - 2) {
                            dataList.clear()
                        }
                    }
                    safeGetActivity()!!.runOnUiThread {
                        Toast.makeText(
                            activity,
                            resources.getString(R.string.sucBpmData),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    userDetailsEditor!!.putString("bpmStartDate", currentDate)
                    userDetailsEditor!!.apply()
                }

                override fun onFailure(e: Exception?) {
                    safeGetActivity()!!.runOnUiThread {
                        Toast.makeText(
                            activity,
                            resources.getString(R.string.failBpmData),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
    }

    private fun writeBpmDataToFile(startDate: String, dataList: ArrayList<String>) {
        val spStartDate = startDate.split("-").toTypedArray()
        val directoryName = "LOOKHEART/" + myEmail + "/" + spStartDate[0] + "/" + spStartDate[1] + "/" + spStartDate[2]
        val directory = File(safeGetActivity()!!.filesDir, directoryName)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, "BpmData.csv")
        try {
            FileOutputStream(file, false).use { fos ->
                val csvBuilder = StringBuilder()
                for (dataStr in dataList) {
                    val sybDataStr = dataStr.substring(1, dataStr.length - 1).trim { it <= ' ' } // 중괄호, 공백 제거
                    val data = sybDataStr.split(",").toTypedArray() // 쉼표 기준 분리
                    Log.e("data",Arrays.toString(data))
                    // 키, 값 저장
                    val map: MutableMap<String, String> = HashMap()
                    for (key in data) {
                        val entry = key.split("=").toTypedArray()
                        if (entry.size < 2) map[entry[0].trim { it <= ' ' }] =
                            "null" else map[entry[0].trim { it <= ' ' }] =
                            entry[1].trim { it <= ' ' }
                    }
                    val time = map["time"]!!.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    val csvData = """
                    ${time[1]},${map["utcOffset"]},${map["bpm"]},${map["temp"]},${map["hrv"]}
                    
                    """.trimIndent()
                    csvBuilder.append(csvData)
                }
                fos.write(csvBuilder.toString().toByteArray())
                fos.close()
            }
        } catch (e: IOException) {
            // handle exception
        }
    }

    fun loadPreviousHourlyData(startDate: String?) {
        retrofitServerManager!!.getHourlyData(
            "calandDistanceData",
            myEmail!!,
            startDate!!,
            targetDate!!,
            object : HourlyDataCallback {
                override fun hourlyData(data: List<*>?) {
                    val dataList = ArrayList<String>()
                    var i = 1
                    while (data!!.size - 1 > i) {
                        val stringData = data[i] as String
                        val stringNextData = data[i + 1] as String
                        val spStringData =
                            stringData.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        val nextSpStringData =
                            stringNextData.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        val startDate =
                            spStringData[0].split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        val nextDate =
                            nextSpStringData[0].split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        if (startDate[0] == nextDate[0]) {
                            val realData = stringData.split(",".toRegex(), limit = 2).toTypedArray()
                            dataList.add(realData[1])
                        } else {
                            writeHourlyDataToFile(startDate[0], dataList)
                            dataList.clear()
                        }

                        // 마지막 값
                        if (i == data.size - 2) {
                            dataList.clear()
                        }
                        i++
                    }
                    safeGetActivity()!!.runOnUiThread {
                        Toast.makeText(
                            activity,
                            resources.getString(R.string.sucDailyData),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    searchYesterdayArrCnt(currentDate)
                    userDetailsEditor!!.putString("hourlyStartDate", currentDate)
                    userDetailsEditor!!.apply()
                }

                override fun onFailure(e: Exception?) {
                    safeGetActivity()!!.runOnUiThread {
                        Toast.makeText(
                            activity,
                            resources.getString(R.string.failDailyData),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
    }

    private fun writeHourlyDataToFile(startDate: String, dataList: ArrayList<String>) {
        val spStartDate =
            startDate.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val directoryName =
            "LOOKHEART/" + myEmail + "/" + spStartDate[0] + "/" + spStartDate[1] + "/" + spStartDate[2]
        val directory = File(safeGetActivity()!!.filesDir, directoryName)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, "CalAndDistanceData.csv")
        try {
            FileOutputStream(file, false).use { fos ->
                val csvBuilder = StringBuilder()
                for (dataStr in dataList) {
                    val data = dataStr.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray() // 쉼표 기준 분리

//                Log.e("data", Arrays.toString(data));

                    // 시간, utcOffset, 걸음, 거리, 칼로리, 활동 칼로리, 비정상맥박
                    val csvData = """
                    ${data[1]},${data[0]},${data[2]},${data[3]},${data[4]},${data[5]},${data[6]}
                    
                    """.trimIndent()
                    csvBuilder.append(csvData)
                }
                fos.write(csvBuilder.toString().toByteArray())
                fos.close()
            }
        } catch (e: IOException) {
            // handle exception
        }
    }

    fun loadPreviousArrData(startDate: String?) {
        retrofitServerManager!!.getArrData(
            "arrEcgData",
            arrIdx,
            myEmail!!,
            startDate!!,
            targetDate!!,
            object : ArrDataCallback {
                override fun getData(result: List<String>?) {
                    val dataList = ArrayList<String>()
                    var i = 1
                    while (result!!.size - 1 > i) {

//                    Log.e("result.get", "i : " + i + " data : " + result.get(i).toString());
                        val data = result[i]
                        val nextData = result[i + 1]
                        val spData =
                            data.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        val nextSpData =
                            nextData.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        val time = spData[0].split("\\|".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        val nextTime =
                            nextSpData[0].split("\\|".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        if (time.size != nextTime.size) {
                            i++
                            continue
                        }
                        val date = time[1].split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        val nextDate =
                            nextTime[1].split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        if (date[0] == nextDate[0]) {
                            val realData =
                                data.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()
                            dataList.add(realData[2])
                        } else {
                            writeArrDataToFile(date[0], dataList)
                            dataList.clear()
                        }

                        // 마지막 값
                        if (i == result.size - 2) {
                            dataList.clear()
                        }
                        i++
                    }
                    if (isAdded) safeGetActivity()!!.runOnUiThread {
                        Toast.makeText(
                            activity,
                            resources.getString(R.string.sucArrData),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    userDetailsEditor!!.putString("arrStartDate", currentDate)
                    userDetailsEditor!!.apply()
                }

                override fun onFailure(e: Exception?) {
                    safeGetActivity()!!.runOnUiThread {
                        Toast.makeText(
                            activity,
                            resources.getString(R.string.failArrData),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
    }

    private fun writeArrDataToFile(startDate: String, dataList: ArrayList<String>) {
        var arrCnt = 0
        var arrType: String? = null
        val spStartDate =
            startDate.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        Log.e("spStartDate", Arrays.toString(spStartDate))
        val directoryName =
            "LOOKHEART/" + myEmail + "/" + spStartDate[0] + "/" + spStartDate[1] + "/" + spStartDate[2] + "/" + "arrEcgData"
        val directory = File(safeGetActivity()!!.filesDir, directoryName)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        var i = 0
        while (dataList.size > i) {
            val data = dataList[i]
            val spData = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (spData.size <= 500) {
                i++
                continue
            }

//            Log.e("data", data);
            arrType = spData[3]
            var date: String
            var ecgData = ""
            val filename = ""
            var startEcgIndex = 0
            when (arrType) {
                "arr" -> startEcgIndex = data.indexOf("arr,") + 4
                "fast" -> startEcgIndex = data.indexOf("fast,") + 5
                "slow" -> startEcgIndex = data.indexOf("slow,") + 5
                "irregular" -> startEcgIndex = data.indexOf("irregular,") + 10
            }
            ecgData = data.substring(startEcgIndex)
            date = startDate + "_" + spData[0] + "_"
            try {
                arrCnt++
                val file = File(directory, "arrEcgData_$date$arrCnt.csv")
                val fos = FileOutputStream(file, false) // 'true' to append
                val csvData =
                    spData[0] + "," + spData[1] + "," + spData[2] + "," + spData[3] + "," + ecgData
                fos.write(csvData.toByteArray())
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            i++
        }
    }

    fun bpmChart(myBpm: String?) {
        val entries: MutableList<Entry> = ArrayList()
        bpmLastLines.add(java.lang.Double.valueOf(myBpm))
        if (bpmLastLines.size > BPM_GRAPH_MAX) {  // 250개 라인만 저장
            bpmLastLines.removeFirst()
        }

        // 그래프에 들어갈 데이터 저장
        var i = 0
        while (bpmLastLines.size > i) {
            entries.add(Entry(i.toFloat(), bpmLastLines[i].toFloat()))
            i++
        }

        // 그래프 Set
        val dataSet = LineDataSet(entries, "BPM")
        dataSet.setDrawCircles(false)
        dataSet.color = Color.BLUE
        dataSet.lineWidth = 1.0f
        dataSet.setDrawValues(false)
        val lineData = LineData(dataSet)
        chart!!.data = lineData
        val bpmChartData = LineData(dataSet)
        chart!!.data = bpmChartData // 차트에 표시되는 데이터 설정
        chart!!.setNoDataText("") // 데이터가 없는 경우 차트에 표시되는 텍스트 설정
        chart!!.xAxis.isEnabled = false // x축 활성화(true)
        chart!!.legend.textSize = 15f // 범례 텍스트 크기 설정("BPM" size)
        chart!!.legend.typeface = Typeface.DEFAULT_BOLD
        chart!!.setVisibleXRangeMaximum(500f) // 한 번에 보여지는 x축 최대 값
        chart!!.xAxis.granularity = 1f // 축의 최소 간격
        chart!!.xAxis.position = XAxis.XAxisPosition.BOTTOM // x축 위치
        chart!!.xAxis.setDrawGridLines(false) // 축의 그리드 선
        chart!!.description.isEnabled = false // 차트 설명
        chart!!.axisLeft.axisMaximum = 200f // y 축 최대값
        chart!!.axisLeft.axisMinimum = 40f // y 축 최소값
        chart!!.axisRight.isEnabled = false // 참조 반환
        chart!!.setDrawMarkers(false) // 값 마커
        chart!!.isDragEnabled = false // 드래그 기능
        chart!!.setPinchZoom(false) // 줌 기능
        chart!!.isDoubleTapToZoomEnabled = false // 더블 탭 줌 기능
        chart!!.isHighlightPerDragEnabled = false // 드래그 시 하이라이트
        chart!!.legend.isEnabled = false // 라벨 제거
        chart!!.setTouchEnabled(false) // 터치 불가
        chart!!.data.notifyDataChanged() // 차트에게 데이터가 변경되었음을 알림
        chart!!.notifyDataSetChanged() // 차트에게 데이터가 변경되었음을 알림
        chart!!.moveViewToX(0f) // 주어진 x값의 위치로 뷰 이동
        chart!!.invalidate() // 차트 다시 그림
    }

    fun startTimeCheck() {
        currentDTHandler.postDelayed(object : Runnable {
            override fun run() {
                currentTimeCheck()
                currentDTHandler.postDelayed(this, 1000)
            }
        }, 1000)
    }

    fun dateCalculate(myDay: Int, check: Boolean) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        var date: LocalDate
        if (check) {
            // tomorrow
            date = LocalDate.parse(currentDate, formatter)
            date = date.plusDays(myDay.toLong())
            targetDate = date.format(formatter)
            println(targetDate)
        } else {
            // yesterday
            date = LocalDate.parse(currentDate, formatter)
            date = date.minusDays(myDay.toLong())
            targetDate = date.format(formatter)
            println(targetDate)
        }
    }

    fun setUI() {
        eCal_value!!.text =
            dExeCal.toInt().toString() + " " + resources.getString(R.string.kcalValue)
        step_value!!.text = allstep.toString() + " " + resources.getString(R.string.stepValue2)
        distance_value!!.text = String.format(
            "%.3f",
            distance / 1000
        ) + " " + resources.getString(R.string.distanceValue2)
        temp_value!!.text = String.format(
            "%.1f",
            doubleTEMP
        ) + " " + resources.getString(R.string.temperatureValue2)
    }

    fun setOnBackPressed() {
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressedDialog = AlertDialog.Builder(safeGetActivity())
                    .setTitle(resources.getString(R.string.noti))
                    .setMessage(resources.getString(R.string.exit))
                    .setNegativeButton(resources.getString(R.string.rejectLogout), null)
                    .setPositiveButton(resources.getString(R.string.exit2)) { arg0, arg1 -> safeGetActivity()!!.finish() }
                    .create()
                onBackPressedDialog?.show()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    fun startService() {
        serviceIntent = Intent(safeGetActivity(), ForegroundService::class.java)
        safeGetActivity()!!.startService(serviceIntent)
    }

    fun stopService() {
        serviceIntent = Intent(safeGetActivity(), ForegroundService::class.java)
        safeGetActivity()!!.stopService(serviceIntent)
    }

    private fun permissionsCheck() {
        val notiCheck = NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()
        Log.e("NotiCheck", notiCheck.toString())

        // 알림 채널 생성
        createNotificationChannel()

        // 배터리 최적화 기능 무시
        requestBatteryOptimizationsPermission()

        // 권한 요청
        requestPermissions()
    }

    private fun requestBatteryOptimizationsPermission() {
        val pm =
            safeGetActivity()!!.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isWhiteListing =
            pm.isIgnoringBatteryOptimizations(safeGetActivity()!!.applicationContext.packageName)
        if (!isWhiteListing) {
            val intent = Intent()
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data =
                Uri.parse("package:" + safeGetActivity()!!.applicationContext.packageName)
            startActivity(intent)
        }
    }

    private fun requestPermissions() {
        // 권한 리스트 설정
        if (Build.VERSION.SDK_INT >= 33) {
            PERMISSIONS = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        }

        // 권한 요청
        multiplePermissionsContract = ActivityResultContracts.RequestMultiplePermissions()
        multiplePermissionLauncher = registerForActivityResult(multiplePermissionsContract) { isGranted: Map<String, Boolean> ->
                handlePermissionResult(isGranted)
            }

        // 필요 권한 확인 후 권한 요청
        askPermissions(multiplePermissionLauncher)
    }

    private fun handlePermissionResult(isGranted: Map<String, Boolean>) {
        if (isGranted.containsValue(false)) {
            handlePermissionDenied()
        } else {
            handlePermissionGranted()
        }
    }

    private fun handlePermissionDenied() {
        Toast.makeText(
            safeGetActivity(),
            resources.getString(R.string.permissionToast),
            Toast.LENGTH_SHORT
        ).show()
        askPermissions(multiplePermissionLauncher!!)
    }

    private fun handlePermissionGranted() {
        createNotificationChannel()
        notificationsPermissionCheck = true
        userDetailsEditor!!.putBoolean("noti", notificationsPermissionCheck)
    }

    private fun askPermissions(multiplePermissionLauncher: ActivityResultLauncher<Array<String>>) {
        if (!hasPermissions(PERMISSIONS)) {
            Log.d(
                "PERMISSIONS",
                "Launching multiple contract permission launcher for ALL required permissions"
            )
            try {
                multiplePermissionLauncher?.launch(PERMISSIONS)
            }catch (e: Exception){
                Log.e("multiplePermissionLauncher",e.message.toString())
            }

        } else {
            Log.d("PERMISSIONS", "All permissions are already granted")
        }
    }

    private fun hasPermissions(permissions: Array<String>?): Boolean {
        if (permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        safeGetActivity()!!,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.d("PERMISSIONS", "Permission is not granted: $permission")
                    return false
                }
                Log.d("PERMISSIONS", "Permission already granted: $permission")
            }
            return true
        }
        return false
    }

    fun initVar() {
        distance = 0.0
        dCal = 0.0
        dExeCal = 0.0
        allstep = 0
        safeGetActivity()!!.runOnUiThread { arrStatus() }
        bpm_value!!.text = realBPM.toInt().toString()
        eCal_value!!.text = dExeCal.toInt().toString() + " kcal"
        step_value!!.text = "$allstep step"
        distance_value!!.text = String.format("%.3f", distance / 1000) + " km"
        arr_value!!.text = Integer.toString(serverArrCnt)
        temp_value!!.text = String.format("%.1f", doubleTEMP) + " °C"
    }

    fun currentTimeCheck() {
        val currentMillis = System.currentTimeMillis()
        currentDateTime = Date(currentMillis)
        currentDate = DATE_FORMAT.format(currentDateTime)
        currentTime = TIME_FORMAT.format(currentDateTime)
        currentYear = YEAR_FORMAT.format(currentDateTime)
        currentMonth = MONTH_FORMAT.format(currentDateTime)
        currentDay = DAY_FORMAT.format(currentDateTime)
        currentHour = HOUR_FORMAT.format(currentDateTime)
        if (currentDate != preDate) {
            preDate = currentDate
            serverArrCnt = 0
            userDetailsEditor!!.putString("preDate", preDate)
            userDetailsEditor!!.putInt("serverArrCnt", 0)
            userDetailsEditor!!.apply()
        }

        // 날이 바뀌는 경우 targetDate 갱신
        if (currentDate == targetDate) dateCalculate(1, true)
    }

    val profile: Unit
        get() {
            retrofitServerManager!!.getProfile(myEmail!!, object : UserDataCallback {
                override fun userData(user: UserProfile?) {
                    Log.i("getProfile", "LoadUserData")
                    eCalBPM = user?.activityBPM?.toInt()!!
                    sleep = user?.sleepStart?.toInt()!!
                    wakeup = user?.sleepEnd?.toInt()!!

                    // 로컬 저장
                    userDetailsEditor!!.putString("gender", user.gender)
                    userDetailsEditor!!.putString("birthday", user.birthday)
                    userDetailsEditor!!.putString("age", user.age)
                    userDetailsEditor!!.putString("name", user.name)
                    userDetailsEditor!!.putString("number", user.phone)
                    userDetailsEditor!!.putString("weight", user.weight)
                    userDetailsEditor!!.putString("height", user.height)
                    userDetailsEditor!!.putString("sleep1", user.sleepStart)
                    userDetailsEditor!!.putString("sleep2", user.sleepEnd)
                    userDetailsEditor!!.putString("current_date", user.joinDate)
                    userDetailsEditor!!.putString("o_cal", user.dailyCalorie)
                    userDetailsEditor!!.putString("o_ecal", user.dailyActivityCalorie)
                    userDetailsEditor!!.putString("o_step", user.dailyStep)
                    userDetailsEditor!!.putString("o_distance", user.dailyDistance)
                    userDetailsEditor!!.apply()
                }

                override fun onFailure(e: Exception?) {
                    Log.e("getProfile", "onFailure")
                    e?.printStackTrace()
                }
            })
        }

    fun createNotificationChannel() {

        // notification manager 생성
        notificationManager =
            safeGetActivity()!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel = NotificationChannel(
            PRIMARY_CHANNEL_ID,
            PRIMARY_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)
        notificationChannel.description = "Notification"
        notificationChannel.setSound(
            Uri.parse("android.resource://" + safeGetActivity()!!.packageName + "/" + R.raw.arrsound),
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
        )
        notificationManager!!.createNotificationChannel(notificationChannel)
    }

    fun sendNotification(noti: String) {
        // Builder 생성
        val notifyBuilder = getNotificationBuilder(noti)
        notificationManager!!.notify(notificationId, notifyBuilder.build())
        // alert
        safeGetActivity()!!.runOnUiThread { arrEvent(noti.toInt()) }
    }

    private fun getNotificationBuilder(noti: String): NotificationCompat.Builder {
        val notifyBuilder = NotificationCompat.Builder(safeGetActivity()!!, PRIMARY_CHANNEL_ID)
        return when (noti) {
            "50" -> {
                notifyBuilder.setContentTitle(resources.getString(R.string.arrCnt50))
                notifyBuilder.setContentText(resources.getString(R.string.arrCnt50Text))
                notifyBuilder.setSmallIcon(R.mipmap.ic_launcher_round)
                notifyBuilder
            }

            "100" -> {
                notifyBuilder.setContentTitle(resources.getString(R.string.arrCnt100))
                notifyBuilder.setContentText(resources.getString(R.string.arrCnt100Text))
                notifyBuilder.setSmallIcon(R.mipmap.ic_launcher_round)
                notifyBuilder
            }

            "200" -> {
                notifyBuilder.setContentTitle(resources.getString(R.string.arrCnt200))
                notifyBuilder.setContentText(resources.getString(R.string.arrCnt200Text))
                notifyBuilder.setSmallIcon(R.mipmap.ic_launcher_round)
                notifyBuilder
            }

            "300" -> {
                notifyBuilder.setContentTitle(resources.getString(R.string.arrCnt300))
                notifyBuilder.setContentText(resources.getString(R.string.arrCnt300Text))
                notifyBuilder.setSmallIcon(R.mipmap.ic_launcher_round)
                notifyBuilder
            }

            else -> notifyBuilder
        }
    }

    private fun heartAttackEvent(eventTime: String, location: String) {
        HeartAttackCheck = true

        // 시스템 사운드 재생
        val mediaPlayer = MediaPlayer.create(
            safeGetActivity(),
            R.raw.heartattacksound
        ) // res/raw 폴더에 사운드 파일을 넣어주세요
        mediaPlayer.isLooping = true // 반복
        mediaPlayer.start()

        // 알림 대화상자 표시
        val builder = AlertDialog.Builder(safeGetActivity(), R.style.AlertDialogTheme)
        val v = LayoutInflater.from(safeGetActivity()).inflate(
            R.layout.heartattack_dialog,
            view?.findViewById<View>(R.id.layoutDialog) as LinearLayout
        )
        builder.setView(v)
        (v.findViewById<View>(R.id.heartattack_title) as TextView).text =
            resources.getString(R.string.emergency)
        (v.findViewById<View>(R.id.textMessage) as TextView).text = """
            ${resources.getString(R.string.occurrenceTime)}$eventTime
            ${resources.getString(R.string.occurrenceLocation)}
            $location
            """.trimIndent()
        (v.findViewById<View>(R.id.btnOk) as TextView).text = resources.getString(R.string.ok)
        val alertDialog = builder.create()
        v.findViewById<View>(R.id.btnOk)
            .setOnClickListener { //                    mediaPlayer.stop();
                mediaPlayer.release()
                HeartAttackCheck = false
                alertDialog.dismiss()
            }

        // 다이얼로그 형태 지우기
        if (alertDialog.window != null) {
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        alertDialog.show()
    }

    private fun arrEvent(arrCnt: Int) {
        var title: String? = null
        var message: String? = null
        when (arrCnt) {
            50 -> {
                title = resources.getString(R.string.arrCnt50)
                message = resources.getString(R.string.arrCnt50Text)
            }

            100 -> {
                title = resources.getString(R.string.arrCnt100)
                message = resources.getString(R.string.arrCnt100Text)
            }

            200 -> {
                title = resources.getString(R.string.arrCnt200)
                message = resources.getString(R.string.arrCnt200Text)
            }

            300 -> {
                title = resources.getString(R.string.arrCnt300)
                message = resources.getString(R.string.arrCnt300Text)
            }
        }

        // 알림 대화상자 표시
        val builder = AlertDialog.Builder(safeGetActivity(), R.style.AlertDialogTheme)
        val v = LayoutInflater.from(safeGetActivity()).inflate(
            R.layout.heartattack_dialog,
            view?.findViewById<View>(R.id.layoutDialog) as LinearLayout
        )
        builder.setView(v)
        (v.findViewById<View>(R.id.heartattack_title) as TextView).text = title
        (v.findViewById<View>(R.id.textMessage) as TextView).text = message
        (v.findViewById<View>(R.id.btnOk) as TextView).text = resources.getString(R.string.ok)
        val alertDialog = builder.create()
        v.findViewById<View>(R.id.btnOk).setOnClickListener { alertDialog.dismiss() }

        // 다이얼로그 형태 지우기
        if (alertDialog.window != null) {
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        alertDialog.show()
    }

    private fun hourlyArrEvent(arrCnt: Int) {
        var title: String? = null
        var message: String? = null
        var hourlyArrFlag = 0
        val thresholds = intArrayOf(10, 20, 30, 50)
        for (i in thresholds.indices) {
            hourlyArrFlag =
                if (arrCnt >= thresholds[i]) i + 1 else break // 현재 임계값 보다 arrCnt 값이 작으면 반복문 탈출
        }
        when (hourlyArrFlag) {
            1 -> {
                title = resources.getString(R.string.notiHourlyArr10)
                message = resources.getString(R.string.notiHourlyArr10Text)
            }

            2 -> {
                title = resources.getString(R.string.notiHourlyArr20)
                message = resources.getString(R.string.notiHourlyArr20Text)
            }

            3 -> {
                title = resources.getString(R.string.notiHourlyArr30)
                message = resources.getString(R.string.notiHourlyArr30Text)
            }

            4 -> {
                title = resources.getString(R.string.notiHourlyArr50)
                message = resources.getString(R.string.notiHourlyArr50Text)
            }
        }

        // 알림 대화 상자 표시
        val builder = AlertDialog.Builder(safeGetActivity(), R.style.AlertDialogTheme)
        val v = LayoutInflater.from(safeGetActivity()).inflate(
            R.layout.arr_dialog,
            view?.findViewById<View>(R.id.layoutDialog) as LinearLayout
        )
        builder.setView(v)
        (v.findViewById<View>(R.id.heartattack_title) as TextView).text = title
        (v.findViewById<View>(R.id.textMessage) as TextView).text = message
        (v.findViewById<View>(R.id.btnOk) as TextView).text = resources.getString(R.string.ok)
        val alertDialog = builder.create()
        v.findViewById<View>(R.id.btnOk).setOnClickListener { alertDialog.dismiss() }

        // 다이얼로그 형태 지우기
        if (alertDialog.window != null) {
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        alertDialog.show()
    }

    fun playSound(message: String) {
        val audioManager =
            safeGetActivity()!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        var mediaPlayer: MediaPlayer? = null
        val type = message.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        when (type[0]) {
            "비정상맥박", "I.H.R.", "느린맥박", "Slow", "빠른맥박", "Fast", "연속적인", "Heavy" -> mediaPlayer =
                MediaPlayer.create(safeGetActivity(), R.raw.arrsound)
        }
        if (audioManager != null && (audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT || audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE)) {
            // 무음 또는 진동 모드일 때
            // mediaPlayer를 시작하지 않음
        } else {
            // 소리 모드일 때
            mediaPlayer?.start()
        }
        val finalMediaPlayer = mediaPlayer
        Handler(Looper.getMainLooper()).postDelayed(
            { finalMediaPlayer?.release() },
            3000
        ) // 3초 (3000ms)
    }

    fun statusCheck(myBpm: Int) {
        val intCurrentHour = currentHour!!.toInt()
        if (eCalBPM <= myBpm) {
            // 활동중
            exerciseBackground!!.background =
                ContextCompat.getDrawable(safeGetActivity()!!, R.drawable.rest_round_press)
            exerciseText!!.setTextColor(Color.WHITE)
            exerciseImg!!.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
            restBackground!!.setBackgroundColor(Color.TRANSPARENT)
            restText!!.setTextColor(ContextCompat.getColor(safeGetActivity()!!, R.color.lightGray))
            restImg!!.setColorFilter(ContextCompat.getColor(safeGetActivity()!!, R.color.lightGray))
            sleepBackground!!.setBackgroundColor(Color.TRANSPARENT)
            sleepText!!.setTextColor(ContextCompat.getColor(safeGetActivity()!!, R.color.lightGray))
            sleepImg!!.setColorFilter(
                ContextCompat.getColor(
                    safeGetActivity()!!,
                    R.color.lightGray
                )
            )
        } else if (sleep < intCurrentHour || wakeup > intCurrentHour) {
            // 수면중
            exerciseBackground!!.setBackgroundColor(Color.TRANSPARENT)
            exerciseText!!.setTextColor(
                ContextCompat.getColor(
                    safeGetActivity()!!,
                    R.color.lightGray
                )
            )
            exerciseImg!!.setColorFilter(
                ContextCompat.getColor(
                    safeGetActivity()!!,
                    R.color.lightGray
                )
            )
            restBackground!!.setBackgroundColor(Color.TRANSPARENT)
            restText!!.setTextColor(ContextCompat.getColor(safeGetActivity()!!, R.color.lightGray))
            restImg!!.setColorFilter(ContextCompat.getColor(safeGetActivity()!!, R.color.lightGray))
            sleepBackground!!.background =
                ContextCompat.getDrawable(safeGetActivity()!!, R.drawable.rest_round_press)
            sleepText!!.setTextColor(Color.WHITE)
            sleepImg!!.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
        } else {
            // 휴식중
            exerciseBackground!!.setBackgroundColor(Color.TRANSPARENT)
            exerciseText!!.setTextColor(
                ContextCompat.getColor(
                    safeGetActivity()!!,
                    R.color.lightGray
                )
            )
            exerciseImg!!.setColorFilter(
                ContextCompat.getColor(
                    safeGetActivity()!!,
                    R.color.lightGray
                )
            )
            restBackground!!.background =
                ContextCompat.getDrawable(safeGetActivity()!!, R.drawable.rest_round_press)
            restText!!.setTextColor(Color.WHITE)
            restImg!!.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
            sleepBackground!!.setBackgroundColor(Color.TRANSPARENT)
            sleepText!!.setTextColor(ContextCompat.getColor(safeGetActivity()!!, R.color.lightGray))
            sleepImg!!.setColorFilter(
                ContextCompat.getColor(
                    safeGetActivity()!!,
                    R.color.lightGray
                )
            )
        }
    }

    private fun safeGetActivity(): FragmentActivity? {
        val activity = activity
        if (activity == null) {
            Log.e("HomeFragment", "Fragment is not attached to an activity.")
            return null
        }
        return activity
    }

    fun arrStatus() {
        var progressPercentage = 0.0f
        val intCurrentHour = currentHour!!.toInt()
        if (preArr < serverArrCnt) {
            // 어제보다 많음
            preArrLabel!!.text = resources.getString(R.string.moreArr)
            preArrLabel!!.setTextColor(ContextCompat.getColor(safeGetActivity()!!, R.color.myRed))
            preArr_value!!.text = (serverArrCnt - preArr).toString()
        } else {
            // 어제보다 적음
            preArrLabel!!.text = resources.getString(R.string.lessArr)
            preArrLabel!!.setTextColor(ContextCompat.getColor(safeGetActivity()!!, R.color.myBlue))
            preArr_value!!.text = (preArr - serverArrCnt).toString()
        }
        if (serverArrCnt < 50) {
            arrStatus!!.text = resources.getString(R.string.arrStatusGood)
            val fill = 100f / (serverArrCnt * 2) * 100
            val percentageRelativeTo70 = 100 / fill * 100
            progressPercentage = percentageRelativeTo70 / 100
            filledHeart!!.setColorFilter(
                ContextCompat.getColor(
                    safeGetActivity()!!,
                    R.color.myLightGreen
                )
            )
        } else if (serverArrCnt > 50 && serverArrCnt < 100) {
            arrStatus!!.text = resources.getString(R.string.arrStatusCaution)
            val fill = 100f / ((serverArrCnt - 50) * 2) * 100
            val percentageRelativeTo70 = 100 / fill * 100
            progressPercentage = percentageRelativeTo70 / 100
            filledHeart!!.setColorFilter(
                ContextCompat.getColor(
                    safeGetActivity()!!,
                    R.color.myBlue
                )
            )
        } else if (arrCnt >= 100) {
            arrStatus!!.text = resources.getString(R.string.arrStatusWarning)
            arrStatus!!.setTextColor(ContextCompat.getColor(safeGetActivity()!!, R.color.white))
            val fill = 100f / ((serverArrCnt - 100) * 2) * 100
            val percentageRelativeTo70 = 100 / fill * 100
            progressPercentage = percentageRelativeTo70 / 100
            filledHeart!!.setColorFilter(ContextCompat.getColor(safeGetActivity()!!, R.color.myRed))
        }
        val finalProgressPercentage = progressPercentage
        filledHeart!!.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                filledHeart!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val originalHeight = filledHeart!!.height
                val clippedHeight = (finalProgressPercentage * originalHeight).toInt()
                val clipBounds =
                    Rect(0, originalHeight - clippedHeight, filledHeart!!.width, originalHeight)
                filledHeart!!.clipBounds = clipBounds
            }
        })
    }

    fun searchYesterdayArrCnt(currentDate: String?) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        var date: LocalDate
        date = LocalDate.parse(currentDate, formatter)
        date = date.minusDays(1)
        val yesterday = date.format(formatter)
        val spYesterday =
            yesterday.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        // 경로
        val directoryName =
            "LOOKHEART/" + myEmail + "/" + spYesterday[0] + "/" + spYesterday[1] + "/" + spYesterday[2]
        val directory = File(activity?.filesDir, directoryName)

        // 파일 경로와 이름
        val file = File(directory, "CalAndDistanceData.csv")
        if (file.exists()) {
            // 파일이 있는 경우
            try {
                // file read
                val br = BufferedReader(FileReader(file))
                var line: String? = null
                while (br?.readLine()?.also { line = it } != null) {
                    val columns:Array<String>? = line?.split(",".toRegex())?.dropLastWhile { it.isEmpty() }
                        ?.toTypedArray() // 데이터 구분
                    preArr += columns?.get(6)?.toInt()!!
                }
                br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            Log.i("yesterday Arr file", "preArr : $preArr")
        } else {
            // 파일이 없는 경우
            Log.i("yesterday Arr file", "not find")
            preArr = 0
        }
    }

    private fun realTimeBPMLoop() {
        bpmScheduler!!.scheduleAtFixedRate({ responseRealBPM() }, 0, 1, TimeUnit.SECONDS)
    }

    private fun bpmLoop() {
        bpmDataScheduler!!.scheduleAtFixedRate({
            responseBpmData(
                currentDate,
                targetDate,
                currentYear,
                currentMonth,
                currentDay
            )
        }, 0, 10, TimeUnit.SECONDS)
    }

    private fun hourlyDataLoop() {
        hourlyDataScheduler!!.scheduleAtFixedRate({
            responseHourlyData(
                currentDate,
                targetDate,
                currentYear,
                currentMonth,
                currentDay
            )
        }, 0, 10, TimeUnit.SECONDS)
    }

    fun responseRealBPM() {
        retrofitServerManager!!.getRealBPM(myEmail!!, object : RealBpmCallback {
            override fun getBpm(bpm: String?) {
                val myBpm = bpm!!.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                Log.e("myBpm", Arrays.toString(myBpm))
                if (myBpm.size > 1 && isAdded) {
                    if (!myBpm[1].isEmpty() && safeGetActivity() != null) {
                        // UI Update
                        safeGetActivity()!!.runOnUiThread {
                            bpmChart(myBpm[1])
                            bpm_value!!.text = myBpm[1]
                            statusCheck(myBpm[1].toInt())
                        }
                    }
                }
            }

            override fun onFailure(e: Exception?) {
                Log.e("responseRealBPM", "responseRealBPM sendErr")
                e?.printStackTrace()
            }
        })
    }

    fun responseBpmData(
        currentDate: String?,
        targetDate: String?,
        year: String?,
        month: String?,
        day: String?
    ) {
        retrofitServerManager!!.getBpmData(
            "BpmData",
            myEmail!!,
            currentDate!!,
            targetDate!!,
            object : RetrofitServerManager.DataCallback {
                override fun getData(bpmData: List<Map<String, String>>?) {
                    if (bpmData!!.size > 0 && isAdded) {
                        try {
                            val directoryName = "LOOKHEART/$myEmail/$year/$month/$day"
                            val directory = File(safeGetActivity()!!.filesDir, directoryName)
                            if (!directory.exists()) {
                                directory.mkdirs()
                            }
                            val file = File(directory, "BpmData.csv")

                            // 시간, bpm, temp, hrv
                            val fos = FileOutputStream(file, false) // 'true' to append
                            for (data in bpmData) {
                                if (data["time"] != "writetime") {
                                    val time = data["time"]!!.split(" ".toRegex())
                                        .dropLastWhile { it.isEmpty() }
                                        .toTypedArray()
                                    val csvData = """
                                    ${time[1]},${data["utcOffset"]},${data["bpm"]},${data["temp"]},${data["hrv"]}
                                    
                                    """.trimIndent()
                                    Log.e("data",csvData)
                                    //                                Log.e("csvData", csvData);
                                    doubleTEMP = data["temp"]!!.toDouble()
                                    fos.write(csvData.toByteArray())
                                }
                            }
                            fos.close()
                        } catch (e: IOException) {
                            Log.e("responseBpmData", "responseBpmData writeFileErr")
                            e.printStackTrace()
                        }
                    }
                }

                override fun onFailure(e: Exception?) {
                    Log.e("responseBpmData", "responseBpmData sendErr")
                    e?.printStackTrace()
                }
            })
    }

    fun responseHourlyData(
        currentDate: String?,
        targetDate: String?,
        currentYear: String?,
        currentMonth: String?,
        currentDay: String?
    ) {
        allstep = 0
        distance = 0.0
        dCal = 0.0
        dExeCal = 0.0
        retrofitServerManager!!.getHourlyData(
            "calandDistanceData",
            myEmail!!,
            currentDate!!,
            targetDate!!,
            object : HourlyDataCallback {
                override fun hourlyData(data: List<*>?) {
                    if (safeGetActivity() != null) {
                        try {
                            // 경로
                            val directoryName =
                                "LOOKHEART/$myEmail/$currentYear/$currentMonth/$currentDay"
                            val directory = File(safeGetActivity()!!.filesDir, directoryName)

                            // 디렉토리가 없는 경우 생성
                            if (!directory.exists()) {
                                directory.mkdirs()
                            }

                            // 파일 경로와 이름
                            val file = File(directory, "CalAndDistanceData.csv")
                            val fos = FileOutputStream(file, false) // 'true' to append
                            var i = 1
                            while (data!!.size > i) {
                                val spData = data[i] as String // data
                                val result =
                                    spData.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                        .toTypedArray()
                                allstep += result[3].toInt()
                                distance += result[4].toInt()
                                dCal += result[5].toInt()
                                dExeCal += result[6].toInt()
                                val csvData =
                                    result[2] + "," + result[1] + "," + result[3] + "," + result[4] + "," + result[5] + "," + result[6] + "," + result[7]
                                fos.write(csvData.toByteArray())

                                // last hourly data
                                if (data.size - 1 == i && hourlyArrCheck) {
                                    currentArrCnt = result[7].toInt()
                                    if (shouldNotify(previousArrCnt, currentArrCnt, 10) ||
                                        shouldNotify(previousArrCnt, currentArrCnt, 20) ||
                                        shouldNotify(previousArrCnt, currentArrCnt, 30) ||
                                        shouldNotify(previousArrCnt, currentArrCnt, 50)
                                    ) {
                                        safeGetActivity()!!.runOnUiThread {
                                            hourlyArrEvent(
                                                currentArrCnt
                                            )
                                        }
                                    }
                                    previousArrCnt = currentArrCnt // 현재 값을 이전 값으로 업데이트
                                }
                                i++
                            }
                            safeGetActivity()!!.runOnUiThread { setUI() }
                            fos.close()
                        } catch (e: IOException) {
                            Log.e("responseHourlyData", "responseHourlyData writeErr")
                            e.printStackTrace()
                        }
                        hourlyArrCheck = true
                    }
                }

                override fun onFailure(e: Exception?) {
                    Log.e("responseHourlyData", "responseHourlyData sendErr")
                    e?.printStackTrace()
                }
            })
    }

    fun refreshArrData() {
        retrofitServerManager!!.getArrData(
            "arrEcgData",
            arrIdx,
            myEmail!!,
            currentDate!!,
            targetDate!!,
            object : ArrDataCallback {
                override fun getData(dataList: List<String>?) {
                    var arrType: String? = null
                    var i = 1
                    while (dataList!!.size > i) {
                        val data = dataList[i]
                        val spData =
                            data.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        val time = spData[0].split("\\|".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        val spTime = time[1].split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        if (spData.size <= 500) {
                            Log.e("HeartAttackCheck", data)
                            // 응급상황 발생
                            if (arrCheck && !HeartAttackCheck) // 두번째 호출부터 동작
                                heartAttackEvent(time[1], time[3])
                            arrIdx = time[0] // 다음 Select idx 값 저장
                            i++
                            continue
                        }
                        Log.e("data", data)
                        val writeTime = time[2]
                        arrType = spData[3]
                        if (spTime[0] != currentDate) {
                            Log.e("return", spTime[0])
                            return
                        } else if (arrIdx == time[0]) {
                            Log.e("return", time[0])
                            return
                        }
                        var date: String
                        var ecgData = ""
                        var startEcgIndex = 0
                        when (arrType) {
                            "arr" -> startEcgIndex = data.indexOf("arr,") + 4
                            "fast" -> startEcgIndex = data.indexOf("fast,") + 5
                            "slow" -> startEcgIndex = data.indexOf("slow,") + 5
                            "irregular" -> startEcgIndex = data.indexOf("irregular,") + 10
                        }
                        ecgData = data.substring(startEcgIndex)

                        // last Data
                        if (i == dataList.size - 1) {
                            val lastArrDate =
                                spData[0].split("\\|".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()
                            arrIdx = lastArrDate[0]
                            Log.e("lastArrDate", Arrays.toString(lastArrDate))
                        }

                        // time
                        if (time.size > 2) {
                            spData[0] = writeTime
                            date = currentDate + "_" + writeTime + "_"
                        } else {
                            date = currentDate + "_" + time[0] + "_"
                        }
                        try {
                            val directoryName =
                                "LOOKHEART/$myEmail/$currentYear/$currentMonth/$currentDay/arrEcgData"
                            val directory = File(safeGetActivity()!!.filesDir, directoryName)
                            if (!directory.exists()) {
                                directory.mkdirs()
                            }
                            val copyServerArrCnt = serverArrCnt + 1
                            val file = File(directory, "arrEcgData_$date$copyServerArrCnt.csv")
                            val fos = FileOutputStream(file, false) // 'true' to append
                            val csvData =
                                writeTime + "," + spData[1] + "," + spData[2] + "," + spData[3] + "," + ecgData

                            // arrFragment Update
                            viewModel!!.addArrList(spData[0])
                            arrList.add(csvData)
                            serverArrCnt++

                            // UI Update
                            safeGetActivity()!!.runOnUiThread {
                                safeGetActivity()!!.runOnUiThread { arrStatus() }
                                safeGetActivity()!!.runOnUiThread {
                                    arr_value!!.text = Integer.toString(serverArrCnt)
                                }
                            }
                            fos.write(csvData.toByteArray())
                            fos.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        i++
                    }

                    // arr Noti And AlertDialog
                    if (arrCheck) {
                        when (serverArrCnt) {
                            50, 100, 200, 300 -> {
                                notificationId = serverArrCnt
                                sendNotification(serverArrCnt.toString())
                                notificationId = 0
                            }
                        }
                    }
                    arrCheck = true // 두번째 반복부터 알림 뜨게 하는 Flag
                }

                override fun onFailure(e: Exception?) {
                    Log.e("refreshArrData", "refreshArrData sendErr")
                    e?.printStackTrace()
                }
            })
    }

    private fun shouldNotify(previous: Int, current: Int, threshold: Int): Boolean {
        return previous < threshold && current >= threshold
    }

    private fun setFCM() {
        val guardianCheck = userDetailsSharedPref!!.getBoolean("FCMCheck", false)
        if (!guardianCheck) {
            val firebaseMessagingService = FirebaseMessagingService()
            firebaseMessagingService.sendToken(requireContext())
            userDetailsEditor!!.putBoolean("FCMCheck", true)
            userDetailsEditor!!.apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timeLoop = false
        currentDTHandler.removeCallbacksAndMessages(null)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(messageReceiver!!)
        if (serviceIntent != null) stopService()
        if (disposable != null && !disposable!!.isDisposed) disposable!!.dispose()
        if (bpmScheduler != null && !bpmScheduler!!.isShutdown) bpmScheduler!!.shutdown()
        if (hourlyDataScheduler != null && !hourlyDataScheduler!!.isShutdown) hourlyDataScheduler!!.shutdown()
        if (bpmDataScheduler != null && !bpmDataScheduler!!.isShutdown) bpmDataScheduler!!.shutdown()
    }

    override fun onStart() {
        super.onStart()
        // 인텐트 필터를 사용하여 액션 리시버 등록
        val filter = IntentFilter("arr-event")
        messageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val message = intent.getStringExtra("message")
                Log.e("message", message!!)
                refreshArrData()
            }
        }
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(messageReceiver!!, filter)
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(messageReceiver!!)
    }

    override fun onResume() {
        super.onResume()
        Log.i("onResume", "onResume")
        if (arrCheck) refreshArrData() // Arr Update
    }

    override fun onPause() {
        super.onPause()
        if (onBackPressedDialog != null && onBackPressedDialog!!.isShowing) {
            onBackPressedDialog!!.dismiss()
        }
    }

    companion object {
        //endregion
        /*UI variables*/ //region
        private const val BPM_GRAPH_MAX = 250

        //endregion
        /*Notification variables*/ //region
        private const val PRIMARY_CHANNEL_ID = "LOOKHEART_GUARDIAN"
        private const val PRIMARY_CHANNEL_NAME = "GUARDIAN"

        //endregion
        /*currentTimeCheck() variables*/ //region
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd")
        private val TIME_FORMAT = SimpleDateFormat("HH:mm:ss")
        private val YEAR_FORMAT = SimpleDateFormat("yyyy")
        private val MONTH_FORMAT = SimpleDateFormat("MM")
        private val DAY_FORMAT = SimpleDateFormat("dd")
        private val HOUR_FORMAT = SimpleDateFormat("HH")
    }
}