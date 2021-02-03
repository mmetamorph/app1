package com.example.avtorization

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PersonActivity : AppCompatActivity(), UpdateAndDelete {

    private lateinit var logoutButton: Button

    lateinit var database:DatabaseReference

    var toDOList : MutableList<ToDoModel>? = null

    lateinit var adapter: ToDoAdapter
    
    private var listViewItem : ListView?= null

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_person)

        mAuth = FirebaseAuth.getInstance()

        val fab = findViewById<View>(R.id.fab) as FloatingActionButton

        listViewItem = findViewById<ListView>(R.id.item_listView)

        database = FirebaseDatabase.getInstance().reference

        fab.setOnClickListener{ view ->
            val alertDialog = AlertDialog.Builder(this)
            val textEditText = EditText(this)
            alertDialog.setMessage("დაამატეთ ახალი ელემენტი")
            alertDialog.setTitle("შეიყვანეთ სიის ელემენტი")
            alertDialog.setView(textEditText)
            alertDialog.setPositiveButton("დამატება"){dialog, i ->
                val todoItemData = ToDoModel.createList()
                todoItemData.itemDataText = textEditText.text.toString()
                todoItemData.done = false

                val newItemData = database.child("todo").push()
                todoItemData.UID = newItemData.key

                newItemData.setValue(todoItemData)
                dialog.dismiss()
                Toast.makeText(this, "სიის ელემენტი შენახულია", Toast.LENGTH_SHORT).show()

            }
            alertDialog.show()
        }

        toDOList = mutableListOf<ToDoModel>()
        adapter = ToDoAdapter(this, toDOList!!)
        listViewItem!!.adapter = adapter
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                toDOList!!.clear()
                addItemToList(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "ელემენტი არ დაემატა", Toast.LENGTH_LONG).show()
            }

        })


        logoutButton = findViewById(R.id.logoutButton)


        logoutButton.setOnClickListener {
            mAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()

        }
                }

    private fun addItemToList(snapshot: DataSnapshot) {

        val items = snapshot.children.iterator()
        if (items.hasNext()) {
            val toDoIndexedValue = items.next()
            val itemsIterator = toDoIndexedValue.children.iterator()

            while(itemsIterator.hasNext()) {
                val currentItem = itemsIterator.next()
                val toDoItemData = ToDoModel.createList()
                val map = currentItem.getValue() as HashMap<String, Any>

                toDoItemData.UID = currentItem.key
                toDoItemData.done = map.get("done") as Boolean?
                toDoItemData.itemDataText = map.get("itemDataText") as String?
                toDOList!!.add(toDoItemData)

            }
        }

        adapter.notifyDataSetChanged()

    }

    override fun modifyItem(itemUID: String, isDone: Boolean) {
        val itemReference = database.child("todo").child(itemUID)
        itemReference.child("done").setValue(isDone)
    }

    override fun onItemDelete(itemUID: String) {
        val itemReference = database.child("todo").child(itemUID)
        itemReference.removeValue()
        adapter.notifyDataSetChanged()
    }

}
