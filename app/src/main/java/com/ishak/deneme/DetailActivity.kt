package com.ishak.deneme

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.ishak.deneme.databinding.ActivityDetailBinding
import com.ishak.deneme.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream
import java.util.jar.Manifest

class DetailActivity : AppCompatActivity() {

    var selectedBitmap:Bitmap?=null
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionResultLaunchaer:ActivityResultLauncher<String>
    private lateinit var binding: ActivityDetailBinding
    private lateinit var database : SQLiteDatabase
   var id:Int=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityDetailBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)

        database=this.openOrCreateDatabase("NoteDb", Context.MODE_PRIVATE,null)
        var intent=intent
        id=intent.getIntExtra("id",0)
        if(intent.getIntExtra("info",0)==1){
            showregisteredNotes(id)//fonksiyona gidilip istenilen kayıt gösterilecek
        }

        if (id==0){
            binding.deletebtn.visibility=View.INVISIBLE
            binding.exchangebtn.visibility=View.INVISIBLE
        }
        registerLauncher()
    }


    fun selectImage(view: View){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){//izin verilmemiş
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(view,"permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Please give permission",View.OnClickListener {
                    permissionResultLaunchaer.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }).show()
            }
            else{
                permissionResultLaunchaer.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        else{//izin verilmişse galeriye gider
            val intentToGallery=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
        }
    }
    fun scalePicture(image:Bitmap):Bitmap{

        var width=image.width
        var height=image.height
        val ratio=width/height
        if(ratio>1){//resimin yatay olduğunu anlarız.width daha büyük
            width=300
            height=width/ratio
        }
        else{
            height=300
            width=height*ratio
        }

        return Bitmap.createScaledBitmap(image,width,height,true)//ölçeklendirilmiş bitmap döndürür
    }
    fun save(view: View){


        val topic=binding.topicEt.text.toString()
        val comment= binding.commentEt.text.toString()
        if(selectedBitmap!=null){
            val scaledDownImage=scalePicture(selectedBitmap!!)
            val outputStream=ByteArrayOutputStream()
            scaledDownImage.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray=outputStream.toByteArray()//veri tabanında resimler 0 ve 1 lere çevrilip kaydedilmesi gerekir

            try {
                database.execSQL("CREATE TABLE IF NOT EXISTS NoteTb(id INTEGER PRIMARY KEY ,topic VARCHAR,comment VARCHAR,image BLOB )")
                val sqlInsert="INSERT INTO NoteTb(topic,comment,image) VALUES(?,?,?)"
                val statement=database.compileStatement(sqlInsert)
                statement.bindString(1,topic)
                statement.bindString(2,comment)
                statement.bindBlob(3,byteArray)
                statement.execute()

            }
            catch (e:Exception){
                e.printStackTrace()
            }
            val intent = Intent(this,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)//ne kadar açık aktivite varsa kapatılacak
            startActivity(intent)
        }
        else{
            Toast.makeText(this,"select an image",Toast.LENGTH_LONG).show()
        }
    }

    fun exchange(view: View){
        val topic=binding.topicEt.text.toString()
        val comment= binding.commentEt.text.toString()

        if(selectedBitmap==null){
            val cursor = database.rawQuery("SELECT * FROM NoteTb WHERE id = ?", arrayOf(id.toString()))
            val imageIx=cursor.getColumnIndex("image")
            while (cursor.moveToNext()){//selecteBitmap null olmasın diye değiştirilecek notun resimini selectedBitmap'e atayacam.hata vermesin diye.
                val byteArray=cursor.getBlob(imageIx)
                val bitmap=BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                selectedBitmap=bitmap
            }
        }
        if(selectedBitmap!=null){
            val scaledDownImage=scalePicture(selectedBitmap!!)
            val outputStream=ByteArrayOutputStream()
            scaledDownImage.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray=outputStream.toByteArray()//veri tabanında resimler 0 ve 1 lere çevrilip kaydedilmesi gerekir

            try {
                database.execSQL("CREATE TABLE IF NOT EXISTS NoteTb(id INTEGER PRIMARY KEY ,topic VARCHAR,comment VARCHAR,image BLOB )")
                val sqlInsert="INSERT INTO NoteTb(topic,comment,image) VALUES(?,?,?)"
                val statement=database.compileStatement(sqlInsert)
                statement.bindString(1,topic)
                statement.bindString(2,comment)
                statement.bindBlob(3,byteArray)
                statement.execute()

            }
            catch (e:Exception){
                e.printStackTrace()
            }
            delete(view)
            val intent = Intent(this,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)//ne kadar açık aktivite varsa kapatılacak
            startActivity(intent)
        }
        else{
            Toast.makeText(this,"select same or diffrent image",Toast.LENGTH_LONG).show()
        }

    }

    fun delete(view: View){
        database.execSQL("DELETE FROM NoteTb WHERE id=$id")
        val intent = Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }


    fun registerLauncher(){
        activityResultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if(result.resultCode== RESULT_OK){
                val getIntentFromResult=result.data
                if (getIntentFromResult!=null){
                    val imageDataUrı=getIntentFromResult.data
                    selectedBitmap=MediaStore.Images.Media.getBitmap(this@DetailActivity.contentResolver,imageDataUrı)//bitmap'e çevriliyor
                    binding.imageView.setImageBitmap(selectedBitmap)
                }
            }
        }
        permissionResultLaunchaer=registerForActivityResult(ActivityResultContracts.RequestPermission()){result->
            //yukarda permission.launch yapıldıktan sonra yes ve no durumuna göre result için belli bir değer döndürülyor ve bu kısıma giriyor direkt.
            //yes ve no durumunda ne yapılacağını burada yazacaz
            if(result){
                val intentToGalerry=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)//galeriye gidip uri'sini tutuyor ve intenttogalery'e atıyor
                activityResultLauncher.launch(intentToGalerry)
            }
            else{
                Toast.makeText(this@DetailActivity,"Permission dennied.permission needed",Toast.LENGTH_LONG).show()
            }
        }
    }
    fun showregisteredNotes( id:Int){//kaydedilmiş notlar gösterilecek

        val cursor = database.rawQuery("SELECT * FROM NoteTb WHERE id = ?", arrayOf(id.toString()))
        val topicIx=cursor.getColumnIndex("topic")
        val commentIx=cursor.getColumnIndex("comment")
        val imageIx=cursor.getColumnIndex("image")
        while (cursor.moveToNext()){
            binding.topicEt.setText(cursor.getString(topicIx))
            binding.commentEt.setText(cursor.getString(commentIx))
            val byteArray=cursor.getBlob(imageIx)
            val bitmap=BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
            binding.imageView.setImageBitmap(bitmap)
        }
        binding.savebtn.visibility=View.INVISIBLE
    }

}