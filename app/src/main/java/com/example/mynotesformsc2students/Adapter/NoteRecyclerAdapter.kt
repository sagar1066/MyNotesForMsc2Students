package com.example.mynotesformsc2students.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotesformsc2students.Model.Notes
import com.example.mynotesformsc2students.R
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot

class NoteRecyclerAdapter(options:FirebaseRecyclerOptions<Notes>,val noteListener: NoteListener) :
    FirebaseRecyclerAdapter<Notes,NoteRecyclerAdapter.NoteViewHolder>(options){

    class NoteViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val txtNote:TextView = itemView.findViewById(R.id.txtNote)
        val txtDate:TextView = itemView.findViewById(R.id.txtDate)
        val cbIsCompleted:CheckBox = itemView.findViewById(R.id.cbIsCompleted)
        val itemLayout:CheckBox = itemView.findViewById(R.id.itemlayout)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout,parent,false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int, notes: Notes) {
        holder.txtNote.setText(notes.text)
        val date:CharSequence = android.text.format.DateFormat.format("EEEE, MMM d,yyyy h:mm:ss",
        notes.currentTime!!)

        holder.txtDate.setText(date)
        holder.cbIsCompleted.isChecked = notes.isCompleted!!

        holder.cbIsCompleted.setOnCheckedChangeListener { buttonView, isChecked ->
            val dataSnapshot = snapshots.getSnapshot(holder.adapterPosition)
            noteListener.handleCheckedChange(isChecked,dataSnapshot)
        }
        holder.itemLayout.setOnClickListener {
            val dataSnapshot = snapshots.getSnapshot(holder.adapterPosition)
            noteListener.handleEditClickListener(dataSnapshot)
        }
    }

    public fun deleteItem(position: Int){
        Log.d("DeleteItem",position.toString())
        noteListener.handleDeleteListener(snapshots.getSnapshot(position))
    }
    interface NoteListener {
        public fun handleCheckedChange(iaCheck:Boolean,dataSnapshot: DataSnapshot)
        public fun handleEditClickListener(dataSnapshot: DataSnapshot)
        public fun handleDeleteListener(dataSnapshot: DataSnapshot)
    }
}