package com.example.mynotesformsc2students.Activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotesformsc2students.Adapter.NoteRecyclerAdapter
import com.example.mynotesformsc2students.LoginActivity
import com.example.mynotesformsc2students.Model.Notes
import com.example.mynotesformsc2students.R
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import java.util.*
import kotlin.collections.HashMap
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator


class MainActivity : AppCompatActivity(),NoteRecyclerAdapter.NoteListener {

    val TAG = "MainActivity"
    lateinit var fab:FloatingActionButton
    lateinit var recyclerView: RecyclerView
    lateinit var notesAdapter: NoteRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (FirebaseAuth.getInstance().currentUser  == null) {
            startLoginActivity()
        }
        fab = findViewById(R.id.fab)
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        initRecyclerAdapter()
    }

    private fun initRecyclerAdapter() {
        val query:Query = FirebaseDatabase.getInstance().reference
            .child("Notes")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
//            .child(FirebaseAuth.getInstance().uid!!)

        val  options:FirebaseRecyclerOptions<Notes> = FirebaseRecyclerOptions.Builder<Notes>()
            .setQuery(query,Notes::class.java)
            .build()

        notesAdapter = NoteRecyclerAdapter(options,this)

        recyclerView.adapter = notesAdapter

        var ItemTouchHelper = ItemTouchHelper(simpleCallback)
        ItemTouchHelper.attachToRecyclerView(recyclerView)
    }

        var simpleCallback:ItemTouchHelper.SimpleCallback =
            object:ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT){
                override fun onMove(
                    recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                    TODO("Not yet implemented")
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                   if (direction == ItemTouchHelper.RIGHT){
                       Toast.makeText(this@MainActivity,"Swipe to Right",Toast.LENGTH_LONG).show()
                       notesAdapter.deleteItem(viewHolder.adapterPosition)
                   }
                }

                override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                    RecyclerViewSwipeDecorator.Builder(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                        .addBackgroundColor(
                            ContextCompat.getColor(
                                this@MainActivity,R.color.teal_700
                            )
                        )
                        .addActionIcon(R.drawable.ic_delete_24)
                        .create()
                        .decorate()
                    super.onChildDraw(c, recyclerView, viewHolder,dX, dY, actionState, isCurrentlyActive)
                }

            }

    override fun onStart() {
        super.onStart()

        fab.setOnClickListener {
            createAlertDialog()
        }

        notesAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        notesAdapter.stopListening()
    }

    private fun createAlertDialog() {

        var editText:EditText = EditText(this)

        AlertDialog.Builder(this)
            .setTitle("Add Note")
            .setView(editText)
            .setPositiveButton("ADD",object : DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    //when user click on Add button
                    Log.d(TAG,"In Alert Dialog")
                    addNotetoFirebaseDatabase(editText.text.toString())
                }
            })
            .setNegativeButton("CANCEL",null)
            .create()
            .show()
    }

    private fun addNotetoFirebaseDatabase(text:String)  {

        Log.d(TAG,"In Add Note to Firebase Database Function")
        val ref = FirebaseDatabase.getInstance().reference

        val notes = Notes(text,
            false,
            System.currentTimeMillis())


        ref.child("Notes")
            .child(FirebaseAuth.getInstance().uid.toString())
            .child(UUID.randomUUID().toString())
            .setValue(notes)
            .addOnSuccessListener {
                Log.d(TAG,"addOnSuccessListener : Notes Added Successfully.")
                Toast.makeText(this,"Notes Added Successfully.",Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Log.d(TAG,"addOnFailureListener : ${it.message}")
                Toast.makeText(this,"Error : ${it.message}",Toast.LENGTH_LONG).show()
            }

    }

    fun startLoginActivity() {
        var intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_actionbar,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_Logout -> {
                AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            startLoginActivity()
                        }else{
                            Log.d(TAG,"addOnCompleteListener : ${it.exception}")
                        }
                    }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun handleCheckedChange(isCheck: Boolean, dataSnapshot: DataSnapshot) {
        Log.d("MainActivity","Checked Change")

        val mapOf = HashMap<String,Any>()
        mapOf.put("completed",isCheck)

        dataSnapshot.ref.updateChildren(mapOf)
            .addOnSuccessListener {
                Log.d("MainActivity","onSuccess: Checkbox Updated")
            }
            .addOnFailureListener {
                Log.d("MainActivity","onFailure: Checkbox is not Updated")
            }
    }

    override fun handleEditClickListener(dataSnapshot: DataSnapshot) {
        Log.d("MainActivity", "Edit Item")

        val note= dataSnapshot.getValue<Notes>(Notes::class.java)
        val editText:EditText = EditText(this)
        editText.setText(note!!.text)
        editText.setSelection(note.text!!.length)

        AlertDialog.Builder(this)
            .setTitle("Edit Note")
            .setView(editText)
            .setPositiveButton("Done"){dialogInterface,i ->

//                val newNoteText = editText.text.toString()
//
//               note.text = newNoteText
//
//                dataSnapshot.ref.setValue(note)
//                    .addOnSuccessListener {
//                        Log.d("MainActivity","onSuccess: Note Updated")
//                    }
//                    .addOnFailureListener{
//                        Log.d("MainActivity","onFailure: Note is not Updated")
//                    }
            }
            .setNegativeButton("Cancel",null)
            .show()
    }

    override fun handleDeleteListener(dataSnapshot: DataSnapshot) {
        dataSnapshot.ref.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this,"Note Deleted Successfully.",Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener{
                Toast.makeText(this,"Note Not Deleted.",Toast.LENGTH_LONG).show()
            }
    }
}