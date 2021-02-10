package com.dayakar.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.dayakar.instasaver.Insta.InstaSaver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    lateinit var displayText:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button=findViewById<Button>(R.id.showAction)
        val urlEditText=findViewById<EditText>(R.id.editTextLink)
        displayText=findViewById<TextView>(R.id.showLinks)
        button.setOnClickListener {
            val link=urlEditText.text.toString()

            loadPost(link)
        }
    }

    private  fun loadPost(url:String){

        CoroutineScope(Dispatchers.Main).launch {
            val post=InstaSaver.getInstaPost(url)
            val links= "Download Urls = ${post?.downloadLinks}"
            val userName="User Name = ${post?.userName}\n"
            val caption="Caption= ${post?.caption}\n"
            val timeStamp="TimeStamp = ${post?.timestamp}\n"
            displayText.text= userName+caption+timeStamp+links
        }

    }
}