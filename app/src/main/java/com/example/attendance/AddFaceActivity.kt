package com.example.attendance


//import android.support.v7.app.AppCompatActivity
import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class AddFaceActivity : AppCompatActivity() {
    val TAKE_PHOTO: Int = 1
    val CHOOSE_PHOTO: Int = 2

    var picture: ImageView? = null
    var imageUri: Uri? = null
    var result: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_face)
        var takePhoto = findViewById<Button>(R.id.take_photo)
        var chooseFromAlbum = findViewById<Button>(R.id.choose_from_album)
        var submit =  findViewById<Button>(R.id.submit)

        picture = findViewById<ImageView>(R.id.picture)
        var baseURL  = "http://8.135.29.17:8088/"

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

//        chooseFromAlbum.setOnClickListener{
//            if(ContextCompat.checkSelfPermission(MainActivity@this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(MainActivity@this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),1)
//            }else {
//                openAlbum()
//            }
//        }

        submit.setOnClickListener(View.OnClickListener {
            if(result!=null){
                Thread() {
                    val url = URL(baseURL+"faceAdd/")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.doInput = true  //决定着当前链接可以进行数据读取
                    connection.doOutput = true  //决定着当前链接可以进行数据提交工作
                    connection.requestMethod = "POST"
                    connection.useCaches = false  //取消用户缓存
                    val outputStream  = connection.outputStream
                    val postContent: String = "staffId=5" + "&"+"file=" + URLEncoder.encode(
                        result, "UTF-8")
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
                    runOnUiThread {
                        println(response.toString())
                    }
                }.start()
            }
            else println("result is null")
        })
    }


    fun openAlbum() {
        val intent = Intent("android.intent.action.GET_CONTENT")
        intent.setType("image/*")
        startActivityForResult(intent, CHOOSE_PHOTO)
    }


//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        when(requestCode) {
//            1 -> {
//                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    openAlbum()
//                }else {
//                    Toast.makeText(MainActivity@this,"You denied this permission!!", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            else -> {
//
//            }
//        }
//    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun handleImageOnKitkat(data: Intent?) {
        var imagePath:String? = null
        var uri = data?.data
        Log.d("TAG","handleImageOnKitkat: Uri is: "+uri)
        if(DocumentsContract.isDocumentUri(this,uri)) {
            var docId = DocumentsContract.getDocumentId(uri)
            if("com.android.providers.media.documents".equals(uri?.authority)) {
                val id = docId.split(":")[1]
                var selection = MediaStore.Images.Media._ID + "=" + id
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection)
            }else if ("com.android.providers.downloads.documents".equals(uri?.authority)) {
                val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), docId.toLong())
                imagePath = getImagePath(contentUri,null)
            }
        }else if("content".equals(uri?.scheme,true)){
            imagePath = getImagePath(uri, null)
        }else if("file".equals(uri?.scheme,true)) {
            imagePath = uri?.getPath()
        }

        displayImage(imagePath)
    }


//    fun handleImageBeforeKitkat(data: Intent?) {
//        val uri = data?.data
//        var imagePath:String?  = getImagePath(uri,null)
//        displayImage(imagePath)
//    }

    fun  getImagePath(uri: Uri?, selection: String?): String? {
        var path: String? = null
        var cursor = uri?.let { contentResolver.query(it,null,selection,null,null) }
        if(cursor!=null) {
            if(cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path
    }


    fun displayImage(imagePath:String?) {
        if(imagePath!=null) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            picture?.setImageBitmap(bitmap)//展示图片
        }else {
            Toast.makeText(this,"Failed to get image!!", Toast.LENGTH_SHORT).show()
        }
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
                        picture?.setImageBitmap(bitmap)

                        var bStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bStream);
                        var bytes = bStream.toByteArray();
                        result = Base64.encodeToString(bytes, Base64.DEFAULT)
                    }catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }

//            CHOOSE_PHOTO -> {
//                if(resultCode == Activity.RESULT_OK) {
//                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                        handleImageOnKitkat(data)
//                    }else {
//                        handleImageBeforeKitkat(data)
//                    }
//                }
//            }
        }
    }
}