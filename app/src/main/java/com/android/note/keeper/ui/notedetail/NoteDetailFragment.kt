package com.android.note.keeper.ui.notedetail

import android.os.Bundle
import android.text.method.KeyListener
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.navArgs
import com.android.note.keeper.R
import com.android.note.keeper.data.model.Note
import com.android.note.keeper.databinding.FragmentNoteDetailBinding
import com.android.note.keeper.ui.MainActivity
import com.android.note.keeper.util.Utils
import com.google.android.material.appbar.MaterialToolbar
import dagger.hilt.android.AndroidEntryPoint


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */

@AndroidEntryPoint
class NoteDetailFragment : Fragment(R.layout.fragment_note_detail), MenuProvider {

    private var _binding: FragmentNoteDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel by viewModels<NoteDetailViewModel>()
    private val args: NoteDetailFragmentArgs by navArgs()
    private var note: Note? = null
    private var editMode: Boolean = true
    private lateinit var keyListener: KeyListener
    private lateinit var readOnlyTag: TextView

    //private var menuSave: MenuItem? = null
    private var menuEdit: MenuItem? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNoteDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentNoteDetailBinding.bind(view)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        readOnlyTag = (activity as MainActivity).toolbar.findViewById(R.id.currentMode)

        note = args.note
        keyListener = binding.etTitle.keyListener

        note?.let {
            binding.apply {

                //todo -> get toolbar reference from activity and make 'read mode' tag textview visible
                readOnlyTag.isVisible = true
                editMode = false
                etTitle.setText(it.title)
                etContent.setText(it.content)
                etTitle.keyListener = null
                etContent.keyListener = null
            }
        }

        binding.parent.setOnClickListener {
            if (note == null || editMode) {
                //todo -> get toolbar reference from activity and make 'read mode' tag textview invisible

                binding.etContent.requestFocus()
                Utils.showKeyboard(requireActivity(), binding.etContent)
            }
        }


        //todo move editMode variable to viewmodel and observe the change to set action as per


    }

    private fun updateMenuEditSave() {
        // menuSave?.isVisible = editMode
        menuEdit?.icon =
            if (editMode) ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_check_24,
                null
            ) else resources.getDrawable(R.drawable.ic_edit_24)
        // menuEdit?.isVisible = !editMode
        readOnlyTag.isVisible = !editMode
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_detail, menu)

        // menuSave = menu.findItem(R.id.action_save)
        menuEdit = menu.findItem(R.id.action_edit)

        updateMenuEditSave()
    }

    private fun disableInputs() {
        binding.etTitle.keyListener = null
        binding.etContent.keyListener = null
        binding.etTitle.isCursorVisible = false
        binding.etContent.isCursorVisible = false
    }

    private fun enableInputs(){
        binding.etTitle.keyListener = keyListener
        binding.etContent.keyListener = keyListener
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_edit -> {

                if (editMode) {
                    //save note
                    editMode = false
                    disableInputs()
                    updateMenuEditSave()
                    //todo update/save note
                    viewModel.onSaveClick()
                } else {
                    //here we enable editing
                    editMode = true
                    enableInputs()
                    updateMenuEditSave()
                }

                true
            }
            R.id.action_save -> {
                editMode = false
                updateMenuEditSave()
                //todo save note
                true
            }
            else -> false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}