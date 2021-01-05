package com.example.attendance

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

class RequestFunction : AppCompatActivity() {
    var baseURL  = "http://8.135.29.17:8088/"
    var response = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

     fun requestByGet(path : String) : String {
        val url = URL(baseURL + path)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
         connection.connectTimeout = 60000
         connection.readTimeout = 60000

        val inputStream = connection.inputStream
        val reader = inputStream.bufferedReader()
        while (true) {
            val line = reader.readLine() ?: break
            response.append(line)
        }
        reader.close()
        connection.disconnect()
        return response.toString()
    }

    fun requestByPost(path: String,postContent : String):String{
        val url = URL(baseURL + path )
        val connection = url.openConnection() as HttpURLConnection
        connection.doInput = true  //决定着当前链接可以进行数据读取
        connection.doOutput = true  //决定着当前链接可以进行数据提交工作
        connection.requestMethod = "POST"
        connection.useCaches = false  //取消用户缓存
        connection.connectTimeout = 60000
        connection.readTimeout = 60000

        val outputStream  = connection.outputStream
        outputStream.write(postContent.toByteArray())
        println(postContent)
        outputStream.flush();
        outputStream.close();
        // 执行完dos.close()后，输出流关闭，接下来可以开启输入流进行返回数据的读取

        val inputStream = connection.inputStream
        val reader = inputStream.bufferedReader()
        var response = StringBuilder()
        while (true) {
            val line = reader.readLine() ?: break
            response.append(line)
        }
        reader.close()
        inputStream.close()
        connection.disconnect()
        return response.toString()
    }
}