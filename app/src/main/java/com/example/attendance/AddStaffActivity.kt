package com.example.attendance

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class AddStaffActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_staff)

        var name = findViewById<EditText>(R.id.name).text
        var age = findViewById<EditText>(R.id.age).text
        var position = findViewById<EditText>(R.id.position).text
        var submit = findViewById<Button>(R.id.submit)


        submit.setOnClickListener(View.OnClickListener {
            Thread() {
               Looper.prepare()
                val postContent: String = "name=" + URLEncoder.encode(
                    name.toString(), "UTF-8") + "&"+"age=" + URLEncoder.encode(
                    age.toString(), "UTF-8") + "&" + "positionId=" + URLEncoder.encode(
                    position.toString(), "UTF-8")
               var response = RequestFunction().requestByPost("staffAdd/" , postContent)

                runOnUiThread {
                    if(response == "添加成功"){
                        val intent = Intent(this, ManagerIndex::class.java)
                        startActivity(intent)
                    }
                    else {
                        Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
                    }
                }
                Looper.loop()
            }.start()
        })
    }
}