package com.example.attendance

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_detail_staff.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class DetailStaffActivity : AppCompatActivity() {
    val TAKE_PHOTO: Int = 1
    var id : Int = 0
    var picture: ImageView? = null
    var imageUri: Uri? = null
    var result: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_staff)
        id = intent.getIntExtra("userId", 0)
        var add_face = findViewById<Button>(R.id.add_face)
        picture = findViewById<ImageView>(R.id.picture)

//        请求某位职员信息

        Thread() {
            Looper.prepare()
            val postContent: String = "staffId=" + URLEncoder.encode(
                    id.toString(), "UTF-8")
            var response = RequestFunction().requestByPost("getStaffById/", postContent)

            runOnUiThread {
                var json = JSONObject(response)
                println(json)
                var it: Iterator<String> = json.keys()
                var tableLayout = findViewById<TableLayout>(R.id.tableLayout)
                var tableRow = TableRow(this)

                while (it.hasNext()) {
                    var key = it.next()
                    if (key == "图片") {
                        if (json[key].toString() == "还没有录入人脸") {
                            var message = findViewById<TextView>(R.id.message)
                            println(json[key].toString())
                            message.text = json[key].toString()
                        } else {
//                            Glide.get(this).clearMemory()
//                            Glide.get(this).clearDiskCache()
                            println(json[key].toString())
                            Glide.with(this).load(json[key].toString()).apply(RequestOptions()
                                    .centerCrop()
                                    .skipMemoryCache(true)//跳过内存缓存
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                            ).into(picture!!)
                        }
                    } else {
                        var tv = TextView(this)
                        tv.text = json.get(key).toString()
                        tv.gravity = Gravity.CENTER
                        tv.setBackgroundResource(R.drawable.shape)
                        val params_1 = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
                        params_1.weight = 1F
                        tableRow.addView(tv, params_1)
                    }
                }
                var tv = TextView(this)
                tv.text = "记录"
                tv.gravity = Gravity.CENTER
                tv.setBackgroundResource(R.drawable.shape)
                val params_1 = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
                params_1.weight = 1F

                //点击记录查看
                tv.setOnClickListener(View.OnClickListener {
                    val intent = Intent(this, RecordActivity::class.java)
                    intent.putExtra("userId", id)
                    startActivity(intent)
                })

                tableRow.addView(tv, params_1)
                tableLayout.addView(tableRow)
            }
            Looper.loop()
        }.start()

        add_face.setOnClickListener(View.OnClickListener {

            var outputImage = File(externalCacheDir, "output_image.jpg")
            var intent = Intent()

            try {
                if (outputImage.exists()) {
                    outputImage.delete()
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                imageUri = Uri.fromFile(outputImage)
            } else {
                imageUri = FileProvider.getUriForFile(this, "com.example.attendance.fileprovider", outputImage)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            intent.action = MediaStore.ACTION_IMAGE_CAPTURE;
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, TAKE_PHOTO)
        })
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            TAKE_PHOTO -> {
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        val bitmap = BitmapFactory.decodeStream(imageUri?.let {
                            contentResolver.openInputStream(
                                it
                            )
                        })
                        picture?.setImageBitmap(bitmap)
                        var bStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bStream);
                        var bytes = bStream.toByteArray()
                        result = Base64.encodeToString(bytes, Base64.DEFAULT)
                        println(result)

                        if(result!=null) {
                            Thread() {
                                Looper.prepare()
                                val postContent: String = "staffId=" + URLEncoder.encode(
                                        id.toString(), "UTF-8") + "&" + "file=" + URLEncoder.encode(
                                        result, "UTF-8")
                                var response = RequestFunction().requestByPost("faceAdd/", postContent)
                                runOnUiThread {
                                    Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
                                    message.text = response
                                }
                                Looper.loop()
                            }.start()
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }
        }
    }
}