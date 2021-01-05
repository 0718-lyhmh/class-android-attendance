package com.example.attendance

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class StaffRegister : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_register)
        var staffId  = findViewById<EditText>(R.id.userId).text
        var username = findViewById<EditText>(R.id.editTextPersonName).text
        var password = findViewById<EditText>(R.id.editTextTextPassword).text
        var submit = findViewById<Button>(R.id.register)

        submit.setOnClickListener(View.OnClickListener {
            Thread() {
                Looper.prepare()
                val postContent: String = "staffId=" + URLEncoder.encode(
                    staffId.toString(), "UTF-8") + "&"+"username=" + URLEncoder.encode(
                    username.toString(), "UTF-8") + "&" + "password=" + URLEncoder.encode(
                    password.toString(), "UTF-8")
                var response = RequestFunction().requestByPost( "/staffRegister/",postContent)

                runOnUiThread {
                    if(response == "注册成功"){
                        val intent = Intent(this, com.example.attendance.MainActivity::class.java)
                        startActivity(intent)
                    }
                }
                Looper.loop()
            }.start()
        })
    }
}