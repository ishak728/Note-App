package com.ishak.deneme

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ishak.deneme.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var noteList:ArrayList<Note>
    private lateinit var noteAdapter:NoteAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        noteList=ArrayList()
        noteAdapter=NoteAdapter(noteList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
       // binding.recyclerView.layoutManager=GridLayoutManager(this,100)             grid layoutManager nasıl kullanılır
        binding.recyclerView.adapter = noteAdapter
        try {
            val database=this.openOrCreateDatabase("NoteDb",Context.MODE_PRIVATE,null)
            var cursor=database.rawQuery("SELECT * FROM NoteTb",null)
            val topicIndex=cursor.getColumnIndex("topic")
            val idIndex=cursor.getColumnIndex("id")
            while (cursor.moveToNext()){
                var topic=cursor.getString(topicIndex)
                var id=cursor.getInt(idIndex)
                var note=Note(topic,id)
                noteList.add(note)
            }
            noteAdapter.notifyDataSetChanged()//notedaptörün kendini güncellemesini sağlayacak.çünkü yeni veriler geldi
            cursor.close()
        }
        catch (e:Exception){
            e.printStackTrace()
        }

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater//getMenuInflater() ve menuInflater aynı şeydir
        menuInflater.inflate(R.menu.add_note,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId==R.id.add_note_id){
            val intent=Intent(this,DetailActivity::class.java)
            intent.putExtra("info",0)//yeni not eklenecek
            startActivity(intent)
            //finish() eklesek daha iyi olmaz mı
        }

        return super.onOptionsItemSelected(item)
    }


}