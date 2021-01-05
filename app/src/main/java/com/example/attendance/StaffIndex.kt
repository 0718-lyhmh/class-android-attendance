package com.example.attendance

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_staff_index.*
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*

class StaffIndex : AppCompatActivity() {
    val TAKE_PHOTO: Int = 1
    var picture: ImageView? = null
    var imageUri: Uri? = null
    var result: String? = null
    var userId : Int? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_index)


        var sign_in = findViewById<Button>(R.id.sign_in)
        var personal_record = findViewById<Button>(R.id.personal_record)
        var date:String = ""
        picture = findViewById<ImageView>(R.id.picture)
        userId = intent.getIntExtra("userId",0)
        var record_message = findViewById<TextView>(R.id.record_message)

//        打卡
        sign_in.setOnClickListener(View.OnClickListener {
//            判断网络
            val connMgr: ConnectivityManager = applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo: NetworkCapabilities? = connMgr.getNetworkCapabilities(connMgr.activeNetwork)
            if (networkInfo != null) {
                if (networkInfo.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){
//                    var view3: TextView = findViewById(R.id.text3)
//                    view3.text = "网络类型：WIFI"

                    val wm: WifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
                    val wi: WifiInfo = wm.connectionInfo

//                    begin.text = "ipAddress"+wi.ipAddress.toString()
//                    var view1: TextView = findViewById(R.id.text1)
//                    view1.text = "信号强度：" + wi.rssi.toString()
//
//                    //view1.text = wi.macAddress
//                    var view2: TextView = findViewById(R.id.text2)
//                    view2.text = "bssid："+wi.bssid

                    Thread(){
                        Looper.prepare()
                        val postContent: String = "rssi=" + URLEncoder.encode(
                                wi.rssi.toString(), "UTF-8") + "&"+"ipaddress=" + URLEncoder.encode(
                                wi.ipAddress.toString(), "UTF-8") + "&"+"bssid=" + URLEncoder.encode(
                                wi.bssid, "UTF-8")
                        var response = RequestFunction().requestByPost("testWifi/",postContent)

                        runOnUiThread(){
                            if(response == "定位成功"){
                                //定位成功，开始照相打卡
                                var outputImage = File(externalCacheDir,"output_image.jpg")
                                var intent = Intent()
                                try{
                                    if(outputImage.exists()) {
                                        outputImage.delete()
                                    }
                                }catch (ex: IOException) {
                                    ex.printStackTrace()
                                }
                                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                                    imageUri = Uri.fromFile(outputImage)
                                }else {
                                    imageUri = FileProvider.getUriForFile(this,"com.example.attendance.fileprovider",outputImage)
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                }
                                intent.action = MediaStore.ACTION_IMAGE_CAPTURE;
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                startActivityForResult(intent, TAKE_PHOTO)
                            }
                            else{
//                                Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
                                record_message.text = response
                            }
                        }
                        Looper.loop()
                    }.start()
                } else{
//                    var view3: TextView = findViewById(R.id.text3)
//                    view3.text = "网络类型：移动网络"
//                    Toast.makeText(this, "移动网络，请连接公司wifi", Toast.LENGTH_SHORT).show()
                    record_message.text = "移动网络，请连接公司wifi"
                }
            }
            else{
//                var view3: TextView = findViewById(R.id.text3)
//                view3.text = "网络类型：无网络"
//                Toast.makeText(this, "无网络，请连接公司wifi", Toast.LENGTH_SHORT).show()
                record_message.text = "无网络，请连接公司wifi"
            }
        })

//       记录
        personal_record.setOnClickListener(View.OnClickListener {
            val mcalendar: Calendar = Calendar.getInstance() //  获取当前时间    —   年、月、日
            val year: Int = mcalendar.get(Calendar.YEAR) //  得到当前年
            val month: Int = mcalendar.get(Calendar.MONTH) //  得到当前月
            val day: Int = mcalendar.get(Calendar.DAY_OF_MONTH) //  得到当前日

            var new_year = ""
            var new_month = ""
            var new_day = ""

            DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    new_year = year.toString()
                    new_month = (month+1).toString()
                    new_day = dayOfMonth.toString()
                    Thread(){
                        Looper.prepare()
                        val postContent: String = "staffId=" + URLEncoder.encode(
                              userId.toString(), "UTF-8") + "&"+"year=" + URLEncoder.encode(
                               new_year, "UTF-8") + "&"+"month=" + URLEncoder.encode(new_month,"UTF-8")+ "&"+"day=" + URLEncoder.encode(
                                new_day, "UTF-8")
                        var response = RequestFunction().requestByPost("searchAttendanceByIdWithDay/",postContent)
                        runOnUiThread(){
                            if(response == "{}")record_message.text = "无记录"
                            else if(response == "周末查询失败")record_message.text = response
                            else{
                                var json = JSONObject(response)
                                var it : Iterator<String> = json.keys()
                                var str  = ""
                                while (it.hasNext()){
                                    var key = it.next()
                                    var jsonChild = JSONObject(json.get(key).toString())
                                    var tt:Iterator<String> = jsonChild.keys()
                                    while (tt.hasNext()){
                                        var childKey = tt.next()
                                        if(childKey!="职员ID") {
                                            str += jsonChild.get(childKey).toString() + "\u3000\u3000\u3000"
                                        }
                                    }
                                    str += "\n"
                                }
                                record_message.text = str
                            }
                            Looper.loop()
                        }
                    }.start()
                }, year, month, day
            ).show()
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            TAKE_PHOTO -> {
                if(resultCode == Activity.RESULT_OK) {
                    try{
                        val bitmap = BitmapFactory.decodeStream(imageUri?.let {
                            contentResolver.openInputStream(
                                it
                            )
                        })
//                        picture?.setImageBitmap(bitmap)

                        var bStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bStream);
                        var bytes = bStream.toByteArray();
                        result = Base64.encodeToString(bytes, Base64.DEFAULT)

                        Thread(){
                            Looper.prepare()
                            val postContent: String = "staffId=" + URLEncoder.encode(
                                userId.toString(), "UTF-8") + "&"+"file=" + URLEncoder.encode(
                                result.toString(), "UTF-8")
                            var response = RequestFunction().requestByPost("faceTest/",postContent)
                            runOnUiThread {
                                println(response)

                                var similarity = response.toDouble()
                                if(similarity>=70.0){
                                    Thread(){
                                        Looper.prepare()
                                        val postContent: String = "staffId=" + URLEncoder.encode(
                                                userId.toString(), "UTF-8")
                                        var response = RequestFunction().requestByPost("attendanceWithoutStatus/",postContent)
                                        runOnUiThread(){
                                            println(response)
//                                            Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
                                            record_message.text = response
                                        }
//                                        Looper.loop()
                                    }.start()
                                }
                                else {
//                                    Toast.makeText(this, "识别失败，请重新打卡", Toast.LENGTH_SHORT).show()
                                    record_message.text = "识别失败，请重新打卡"
                                }
                            }
                            Looper.loop()
                        }.start()
                    }catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }
        }
    }
}