package com.android.note.keeper.ui.notedetail

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.KeyListener
import android.view.*
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.android.note.keeper.R
import com.android.note.keeper.data.model.Note
import com.android.note.keeper.databinding.FragmentNoteDetailBinding
import com.android.note.keeper.ui.MainActivity
import com.android.note.keeper.util.DemoUtils
import com.android.note.keeper.util.Utils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
    //private var note: Note? = null

    private lateinit var keyListener: KeyListener
    private lateinit var readOnlyTag: TextView

    //private var menuSave: MenuItem? = null
    private var menuEdit: MenuItem? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNoteDetailBinding.inflate(inflater, container, false)

        DemoUtils.addBottomSpaceInsetsIfNeeded(binding.root as ViewGroup, container)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentNoteDetailBinding.bind(view)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        readOnlyTag = (activity as MainActivity).toolbar.findViewById(R.id.currentMode)

        viewModel.setCurrentNote(args.note)
        keyListener = binding.etTitle.keyListener

        viewModel.currentNote.value?.let {
            binding.apply {
                readOnlyTag.isVisible = true
                //editMode = false
                viewModel.setEditMode(false)
                etTitle.setText(it.title)
                etContent.setText(it.content)
                disableInputs()
            }
        }

        binding.parent.setOnClickListener {
            if (viewModel.currentNote.value == null || viewModel.editMode.value == true) {
                //todo -> get toolbar reference from activity and make 'read mode' tag textview invisible

                binding.etContent.requestFocus()
                Utils.showKeyboard(requireActivity(), binding.etContent)
            }
        }


        //todo move editMode variable to viewmodel and observe the change to set action as per
        viewModel.editMode.observe(viewLifecycleOwner) {
            updateUIState(it)
        }

        setUpBottomAction()
    }

    private fun setUpBottomAction() {
        binding.bottomActionBar.apply {

            viewModel.currentNote.value?.let {
                if (it.isPasswordProtected) btPassword.icon =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.ic_lock_filled_24)
                else AppCompatResources.getDrawable(requireContext(), R.drawable.ic_lock_open_24)
            }

            btColor.setOnClickListener {
                //todo
            }

            btDelete.setOnClickListener {
                deleteNote()
            }

            btPassword.setOnClickListener {
                addPassword()
            }
        }
    }


    private fun updateMenuEditSave() {
        // menuSave?.isVisible = editMode
        menuEdit?.icon =
            if (viewModel.editMode.value == true) ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_check_24,
                null
            ) else ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_edit_24,
                null
            )
        // menuEdit?.isVisible = !editMode
        readOnlyTag.isVisible = !viewModel.editMode.value!!
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
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

    private fun enableInputs() {
        binding.etTitle.keyListener = keyListener
        binding.etContent.keyListener = keyListener
        binding.etContent.isCursorVisible = true
        binding.etTitle.isCursorVisible = true
    }

    private fun updateUIState(edited: Boolean) {
        if (edited) {
            enableInputs()
            updateMenuEditSave()
            binding.etContent.setSelection(binding.etContent.length())
            Utils.showKeyboard(requireActivity(), binding.etContent)
        } else {
            disableInputs()
            updateMenuEditSave()
            Utils.hideKeyboard(requireActivity())
        }
    }


    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_edit -> {

                if (viewModel.editMode.value == true) {
                    //save note
                    val title = binding.etTitle.text.toString()
                    val content = binding.etContent.text.toString()

                    if (title.isNotBlank() || content.isNotBlank()) {
                        if (viewModel.currentNote.value != null) {  //update note
                            val updatedNote =
                                viewModel.currentNote.value!!.copy(title = title, content = content)
                            viewModel.setCurrentNote(updatedNote)
                            viewModel.onUpdateClick(updatedNote)
                            viewModel.setEditMode(false)
                            Utils.showSnackBar(
                                binding.parent,
                                "Note updated",
                                binding.bottomActionBar.bottomActionBar
                            )
                        } else {  //create new note
                            val newNote = Note(title = title, content = content)
                            viewModel.onSaveClick(newNote)
                            viewModel.setCurrentNote(newNote)
                            viewModel.setEditMode(false)
                            Utils.showSnackBar(
                                binding.parent,
                                "Note saved",
                                binding.bottomActionBar.bottomActionBar
                            )
                        }
                    } else {
                        Snackbar.make(
                            binding.parent,
                            "Note content cannot be blank",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }

                } else {
                    //here we enable editing
                    viewModel.setEditMode(true)
                }

                true
            }
            R.id.action_save -> {
                /*val title = binding.etTitle.text.toString()
                val content = binding.etContent.toString()
                if (title.isNotBlank() || content.isNotBlank()){
                    //save note
                    viewModel.onSaveClick(Note(title = title, content = content))
                    editMode = false
                    updateMenuEditSave()
                }else{
                    Snackbar.make(binding.parent,"Note content cannot be blank",Snackbar.LENGTH_SHORT).show()
                }*/
                true
            }
            /* R.id.action_delete -> {

                 true
             }
             R.id.action_password -> {

                 true
             }*/
            else -> false
        }
    }

    private fun addPassword() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomsheet: View =
            LayoutInflater.from(context).inflate(R.layout.bs_add_password, null)

        val et_password = bottomsheet.findViewById<TextInputEditText>(R.id.et_addPassword)
        val ly_password = bottomsheet.findViewById<TextInputLayout>(R.id.lyt_addPassword)
        val bt_save = bottomsheet.findViewById<MaterialButton>(R.id.bt_save)


        bottomSheetDialog.setContentView(bottomsheet)
        bottomSheetDialog.show()

        //todo
    }

    private fun deleteNote() {
        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setMessage("Delete this note?")
            .setPositiveButton("Yes") { _, _ ->
                if (viewModel.currentNote.value != null) {
                    viewModel.onDeleteClick(viewModel.currentNote.value!!)
                }
                findNavController().popBackStack()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        alertDialog.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}