package com.example.seed


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.ch20_firebase.util.dateToString
import com.example.seed.databinding.ActivityAddBinding
import java.io.File
import java.util.*

//AddActivity는 파이어베이스에 데이터나 사진을 올리기위한 코드임
class AddActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddBinding
    lateinit var filePath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    val requestLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult())
    {
        if(it.resultCode === RESULT_OK){
            Glide
                .with(getApplicationContext())
                .load(it.data?.data)
                .apply(RequestOptions().override(250, 200))
                .centerCrop()
                .into(binding.addImageView)


            val cursor = contentResolver.query(it.data?.data as Uri,
                arrayOf<String>(MediaStore.Images.Media.DATA), null, null, null);
            cursor?.moveToFirst().let {
                filePath=cursor?.getString(0) as String
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId === R.id.menu_add_gallery){
            val intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*"
            )
            requestLauncher.launch(intent)
        }else if(item.itemId === R.id.menu_add_save){
            if(binding.addImageView.drawable !== null && binding.addEditView.text.isNotEmpty()){
                //store 에 먼저 데이터를 저장후 document id 값으로 업로드 파일 이름 지정
                saveStore()
            }else {
                Toast.makeText(this, "데이터가 모두 입력되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }

        }
        return super.onOptionsItemSelected(item)
    }
    //....................
    private fun saveStore(){
        //add............................글을 입력하고 저장하고자 할대 호줄 되는 함수 (파이어스토에 글을 등록하는 코드 작성)
        val data = mapOf(
            "email" to MyApplication.email,//글 쓴 사람의 이메일
            "content" to binding.addEditView.text.toString(), //실제 입력한 글
            "date" to dateToString(Date()) //글쓴 시간
        )
        //데이터 저장을 위한 코드
        MyApplication.db.collection("씨앗")
            .add(data)
            .addOnSuccessListener {
                uploadImage(it.id)
            }
            .addOnFailureListener{
                Log.d("Lee","data save error", it)
            }

    }
    private fun uploadImage(docId: String){
        //add............................이미지 업로드
        val storage = MyApplication.storage
        val storageRef = storage.reference
        val imgRef = storageRef.child("images/${docId}.jpg")

        val file = Uri.fromFile(File(filePath))
        imgRef.putFile(file)
            .addOnSuccessListener {
                Toast.makeText(this,"save ok",Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener{
                Log.d("Lee","file save error", it)
            }
    }
}