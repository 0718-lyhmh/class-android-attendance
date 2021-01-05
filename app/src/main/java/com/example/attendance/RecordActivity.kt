package com.example.attendance

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_detail_staff.*
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*


class RecordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)
        var userId = intent.getIntExtra("userId",0)
        var message = findViewById<TextView>(R.id.message)

        var year = findViewById<EditText>(R.id.year).text
        var month = findViewById<EditText>(R.id.month).text
        var searchByMonth = findViewById<Button>(R.id.searchByMonth)

        searchByMonth.setOnClickListener(View.OnClickListener {
            if(year.toString() == "" || month.toString() == ""){
                Toast.makeText(this, "请填写好年份月份", Toast.LENGTH_SHORT).show()
            }
            else{
                Thread() {
                    Looper.prepare()
                    val postContent: String = "staffId=" + URLEncoder.encode(
                        userId.toString(), "UTF-8") + "&"+"year=" + URLEncoder.encode(
                        year.toString(), "UTF-8") + "&" + "month=" + URLEncoder.encode(
                        month.toString(), "UTF-8")
                    var response = RequestFunction().requestByPost("searchAttendanceById/",postContent)
                    runOnUiThread {
                        var json = JSONObject(response)
                        var it : Iterator<String> = json.keys()
                        var str  = ""
                       while (it.hasNext()){
                           var key = it.next()
                           if(json[key].toString() != "{}")
                               str += key + "\u3000:\u3000" + json[key].toString() + "\n"
                        }
                        message.text = str
                        println(json)
                    }
                    Looper.loop()
                }.start()
            }
        })

        var choose_day = findViewById<Button>(R.id.choose_day)
        var record_message = findViewById<TextView>(R.id.record_message)
        choose_day.setOnClickListener(View.OnClickListener {
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
                            }
                            Looper.loop()
                        }.start()
                    }, year, month, day
            ).show()
        })
    }
}