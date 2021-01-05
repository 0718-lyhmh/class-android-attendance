package com.example.attendance

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var userId = findViewById<EditText>(R.id.editTextPersonName).text
        var password = findViewById<EditText>(R.id.editTextTextPassword).text


        var managerLogin = findViewById<Button>(R.id.managerLogin)
        var staffLogin = findViewById<Button>(R.id.staffLogin)
        var register = findViewById<Button>(R.id.register)

        //        管理员登录
        managerLogin.setOnClickListener(View.OnClickListener
        {
            Thread(){
                Looper.prepare()
                var response = RequestFunction().requestByGet("manageLogin/?username="+userId+"&password="+password)
                runOnUiThread(){
                    val json1 = JSONObject(response.toString())
                    println(json1)
                    if(json1["status"].toString() == "登录成功"){
                        val intent = Intent(this, ManagerIndex::class.java)
                        startActivity(intent)
                    }
                    else{
                    Toast.makeText(this, json1["message"].toString(), Toast.LENGTH_SHORT).show()
                    }
                }
                Looper.loop()
            }.start()

        })
//        职员登录
        staffLogin.setOnClickListener(View.OnClickListener
        {

            Thread() {
                Looper.prepare()
                var response = RequestFunction().requestByGet("staffLogin/?username="+userId+"&password="+password)
                runOnUiThread {
                    val json1 = JSONObject(response)
                    println(json1)
                    if(json1["status"].toString() == "登录成功"){
                        val intent = Intent(this, StaffIndex::class.java)
                        intent.putExtra("userId", json1["staffId"].toString().toInt())
                        startActivity(intent)
                    }
                    else{
                        Toast.makeText(this, json1["message"].toString(), Toast.LENGTH_SHORT).show()
                    }
                }
                Looper.loop()
            }.start()
        })
//        职员注册
        register.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, StaffRegister::class.java)
            startActivity(intent)
        })

    }
}