package com.example.attendance

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.text.Layout
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL


class ManagerIndex : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_index)
        var add_staff = findViewById<Button>(R.id.add_staff)

            Thread() {
                Looper.prepare()
                var response = RequestFunction().requestByGet("getAllStaff/")
                runOnUiThread {
                    val json1 = JSONObject(response)
                    println(json1)
                    var it : Iterator<String> = json1.keys()
                    var tableLayout = findViewById<TableLayout>(R.id.tableLayout)
                    var tableRow = TableRow(this)

                    var arr = arrayOf("工号","姓名","年龄","职位","详情")
                    for(i in arr.indices)
                    {
                        var tv = TextView(this)
                        tv.text = arr[i]
                        tv.gravity = Gravity.CENTER
                        tv.setBackgroundResource(R.drawable.shape)
                        val params_1 = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT)
                        params_1.weight = 1F
                        tableRow.addView(tv,params_1)
                    }
                    tableLayout.addView(tableRow)

                    while(it.hasNext()){
                        var key1 = it.next()
                        var jsonChild = JSONObject(json1.get(key1).toString())
                        println(jsonChild)
                        var tableRow = TableRow(this)
                        var tt : Iterator<String> = jsonChild.keys()
                        var userId : Int = 0
                        while(tt.hasNext()){
                            var tv = TextView(this)
                            var key2 = tt.next()
                            tv.text = jsonChild.get(key2).toString()
                            if (key2 == "职员ID")userId = jsonChild.get(key2).toString().toInt()
                            tv.gravity = Gravity.CENTER
                            tv.setBackgroundResource(R.drawable.shape)
                            val params_1 = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT)
                            params_1.weight = 1F
                            tableRow.addView(tv,params_1)
                        }
                        var textview = TextView(this)
                        textview.text = "查看"
                        textview.tag = userId
                        textview.gravity = Gravity.CENTER
                        textview.setBackgroundResource(R.drawable.shape)
                        val params_1 = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT)
                        params_1.weight = 1F

                        textview.setOnClickListener(View.OnClickListener {
                            var id : Int= it.tag as Int
                            println(id)
                            println(userId)
                            println()
                            val intent = Intent(this, DetailStaffActivity::class.java)
                            intent.putExtra("userId",id)
                            startActivity(intent)
                        })
                        tableRow.addView(textview,params_1)
                        tableLayout.addView(tableRow)
                    }
                }
                Looper.loop()
            }.start()

        add_staff.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, AddStaffActivity::class.java)
            startActivity(intent)
        })
    }

}