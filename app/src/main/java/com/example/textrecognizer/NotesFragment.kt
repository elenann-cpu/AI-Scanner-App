package com.example.textrecognizer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class NotesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_notes, container, false)

        val etTitle = view.findViewById<EditText>(R.id.et_note_title)
        val etContent = view.findViewById<EditText>(R.id.et_note_content)
        val btnSave = view.findViewById<Button>(R.id.btn_save_note)

        // TODO: Тука подоцна ќе го повлечеме скенираниот текст од камерата за да се пастира сам!


        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()

            if (content.isNotEmpty()) {

                Toast.makeText(requireContext(), "Note saved!", Toast.LENGTH_SHORT).show()

                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), "Text can't be empty!", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
