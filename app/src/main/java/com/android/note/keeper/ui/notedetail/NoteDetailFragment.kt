package com.android.note.keeper.ui.notedetail

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.KeyListener
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
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
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


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

    private var tempNote: Note = Note(title = "", content = "")

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

        updatePasswordIcon()
        bottomActionClickEvent()
    }

    private fun bottomActionClickEvent() {
        binding.bottomActionBar.apply {

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

    private fun updatePasswordIcon() {
        viewModel.currentNote.value?.let {
            if (it.isPasswordProtected)
                binding.bottomActionBar.btPassword.icon =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.ic_lock_filled_24)
            else AppCompatResources.getDrawable(requireContext(), R.drawable.ic_lock_open_24)
        }

        tempNote?.let {
            if (it.isPasswordProtected)
                binding.bottomActionBar.btPassword.icon =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.ic_lock_filled_24)
            else AppCompatResources.getDrawable(requireContext(), R.drawable.ic_lock_open_24)
        }

    }


    private fun updateMenuEditSave() {
        // menuSave?.isVisible = editMode
        menuEdit?.icon =
            if (viewModel.editMode.value == true) ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_check_24
            )
            else ContextCompat.getDrawable(requireContext(), R.drawable.ic_edit_24)
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
                            val newNote = Note(
                                title = title,
                                content = content,
                                isPasswordProtected = tempNote.isPasswordProtected
                            )
                            viewModel.onSaveClick(newNote)
                            viewModel.setCurrentNote(newNote)
                            viewModel.setEditMode(false)
                            //todo here consider @tempNote properties
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
            R.id.action_pin -> {

                true
            }
            /*R.id.action_archieve -> {

                true
            }*/
            else -> false
        }
    }

    private fun addPassword() {
        var password: String? = null

        runBlocking {
            password = viewModel.masterPasswordFlow.first()
            Log.d("TAG", "addPassword: $password")
        }
        password?.let {
            if (password!!.isBlank()) {
                //todo create password
                bottomSheetCreateMasterPassword()
            } else {
                //todo enable lock
                bottomSheetEnableDisableLock(it)
            }
        }


    }

    private fun bottomSheetCreateMasterPassword() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomsheet: View =
            LayoutInflater.from(context).inflate(R.layout.bs_create_password, null)
        bottomSheetDialog.setCancelable(false)

        val et_password = bottomsheet.findViewById<TextInputEditText>(R.id.et_addPassword)
        val ly_password = bottomsheet.findViewById<TextInputLayout>(R.id.lyt_addPassword)
        val et_confirm = bottomsheet.findViewById<TextInputEditText>(R.id.et_confirmPassword)
        val ly_confirm = bottomsheet.findViewById<TextInputLayout>(R.id.lyt_confirmPassword)
        val bt_save = bottomsheet.findViewById<MaterialButton>(R.id.bt_save)
        val bt_cancel = bottomsheet.findViewById<MaterialButton>(R.id.bt_cancel)

        //todo

        bt_save.setOnClickListener {
            if (et_password.text.toString().isNotBlank() && et_confirm.text.toString()
                    .isNotBlank()
            ) {
                if (et_password.text.toString() == et_confirm.text.toString()) {
                    //todo save to datastore
                    viewModel.setMasterPassword(et_password.text.toString())
                    Snackbar.make(
                        binding.parent,
                        "Master password added! Click on lock option again to enable lock",
                        Snackbar.LENGTH_LONG
                    )
                        .setAnchorView(binding.bottomActionBar.bottomActionBar)
                        .show()
                    bottomSheetDialog.dismiss()
                } else { //password not match
                    ly_confirm.isErrorEnabled = true
                    ly_confirm.error = "Password doesn't match"
                }
            } else { // show error for edit text
                ly_confirm.isErrorEnabled = true
                ly_confirm.error = "Re-enter password here"
            }

        }

        et_confirm.doOnTextChanged { text, start, before, count ->
            ly_confirm.isErrorEnabled = false
            ly_confirm.error = null
        }


        bt_cancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }


        bottomSheetDialog.setContentView(bottomsheet)
        bottomSheetDialog.show()
    }

    private fun bottomSheetEnableDisableLock(password: String) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomsheet: View =
            LayoutInflater.from(context).inflate(R.layout.bs_add_password, null)
        bottomSheetDialog.setCancelable(false)

        val title = bottomsheet.findViewById<TextView>(R.id.txtTitle)
        val subtitle = bottomsheet.findViewById<TextView>(R.id.txtSubTitle)
        val progressBar = bottomsheet.findViewById<LinearProgressIndicator>(R.id.progress)
        val et_password = bottomsheet.findViewById<TextInputEditText>(R.id.et_addPassword)
        val ly_password = bottomsheet.findViewById<TextInputLayout>(R.id.lyt_addPassword)
        val bt_save = bottomsheet.findViewById<MaterialButton>(R.id.bt_save)
        val bt_cancel = bottomsheet.findViewById<MaterialButton>(R.id.bt_cancel)

        val currentNote = viewModel.currentNote.value
        if (currentNote != null) { //already existing note
            if (currentNote.isPasswordProtected) { //remove password
                title.text = "Remove password protection"
                subtitle.text =
                    "Confirm your current master password to remove password protection for this note"
                bt_save.text = "Remove"

                bt_save.setOnClickListener {
                    //todo remove password

                    ly_password.isErrorEnabled = false
                    ly_password.error = null
                    if (et_password.text.toString() == password) {
                        val updatedNote = currentNote.copy(isPasswordProtected = false)
                        viewModel.setCurrentNote(updatedNote)
                        viewModel.onUpdateClick(updatedNote)
                        viewModel.setEditMode(false)
                        updatePasswordIcon()
                        Utils.showSnackBar(
                            binding.parent,
                            "Lock disabled",
                            binding.bottomActionBar.bottomActionBar
                        )
                        bottomSheetDialog.dismiss()
                    } else {
                        ly_password.error = "Wrong master password!"
                    }
                }
            } else { //enable password
                title.text = "Add password protection"
                subtitle.text =
                    "Confirm your current master password to enable password protection for this note"
                bt_save.text = "Add"

                bt_save.setOnClickListener {
                    //todo add password

                    ly_password.isErrorEnabled = false
                    ly_password.error = null
                    if (et_password.text.toString() == password) {
                        val updatedNote = currentNote.copy(isPasswordProtected = true)
                        viewModel.setCurrentNote(updatedNote)
                        viewModel.onUpdateClick(updatedNote)
                        viewModel.setEditMode(false)
                        updatePasswordIcon()
                        Utils.showSnackBar(
                            binding.parent,
                            "Lock enabled",
                            binding.bottomActionBar.bottomActionBar
                        )
                        bottomSheetDialog.dismiss()
                    } else {
                        ly_password.error = "Wrong master password!"
                    }
                }
            }
        } else { //new note
            //create temp note object as set it to current note in viewModel
            //todo
            if (tempNote.isPasswordProtected) { //remove
                title.text = "Remove password protection"
                subtitle.text =
                    "Confirm your current master password to remove password protection for this note"
                bt_save.text = "Remove"

                bt_save.setOnClickListener {
                    //todo remove password

                    ly_password.isErrorEnabled = false
                    ly_password.error = null
                    if (et_password.text.toString() == password) {
                        tempNote = tempNote.copy(isPasswordProtected = false)
                        updatePasswordIcon()
                        Utils.showSnackBar(
                            binding.parent,
                            "Lock disabled",
                            binding.bottomActionBar.bottomActionBar
                        )
                        bottomSheetDialog.dismiss()
                    } else {
                        ly_password.error = "Wrong master password!"
                    }


                }
            } else { //add password to new note
                title.text = "Add password protection"
                subtitle.text =
                    "Confirm your current master password to enable password protection for this note"
                bt_save.text = "Add"

                bt_save.setOnClickListener {
                    //todo add password

                    ly_password.isErrorEnabled = false
                    ly_password.error = null
                    if (et_password.text.toString() == password) {
                        tempNote = tempNote.copy(isPasswordProtected = true)
                        updatePasswordIcon()
                        Utils.showSnackBar(
                            binding.parent,
                            "Lock enabled",
                            binding.bottomActionBar.bottomActionBar
                        )
                        bottomSheetDialog.dismiss()
                    } else {
                        ly_password.error = "Wrong master password!"
                    }
                }
            }

        }

        et_password.doOnTextChanged { text, start, before, count ->
            ly_password.isErrorEnabled = false
            ly_password.error = null
        }


        bt_cancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }


        bottomSheetDialog.setContentView(bottomsheet)
        bottomSheetDialog.show()
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
        //todo save note
        if (viewModel.editMode.value == true) {
            //save note
            val title = binding.etTitle.text.toString()
            val content = binding.etContent.text.toString()

            if (viewModel.currentNote.value != null) { // Old Note
                if (title.isBlank() && content.isBlank()) {
                    // If the user removed everything from the note
                    //todo delete note
                    viewModel.onDeleteClick(viewModel.currentNote.value!!)
                } else {
                    //todo update note
                    val updatedNote =
                        viewModel.currentNote.value!!.copy(title = title, content = content)
                    viewModel.onUpdateClick(updatedNote)
                }
            } else { //new note
                if (title.isNotBlank() || content.isNotBlank()) {
                    //todo save new note
                    val newNote = Note(
                        title = title,
                        content = content,
                        isPasswordProtected = tempNote.isPasswordProtected
                    )
                    viewModel.onSaveClick(newNote)
                }

            }
        }
        super.onDestroyView()
        _binding = null
    }
}