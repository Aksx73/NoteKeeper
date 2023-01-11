package com.android.note.keeper.ui.notedetail

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.method.KeyListener
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.note.keeper.R
import com.android.note.keeper.databinding.FragmentNoteDetailBinding
import com.android.note.keeper.ui.MainActivity
import com.android.note.keeper.util.ColorsUtil
import com.android.note.keeper.util.Constants
import com.android.note.keeper.util.Utils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


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

    private lateinit var keyListener: KeyListener
    private lateinit var readOnlyTag: TextView

    private var menuEdit: MenuItem? = null
    private var menuPin: MenuItem? = null
    private var menuArchive: MenuItem? = null
    private lateinit var masterPassword: String

    private val colorsUtil = ColorsUtil()

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

        readOnlyTag = (activity as MainActivity).readMode

        viewModel.setCurrentNote(args.note)
        keyListener = binding.etTitle.keyListener

        if (viewModel.currentNote.value != null) { //old note
            viewModel.currentNote.value?.let {
                binding.apply {
                    readOnlyTag.isVisible = true
                    //editMode = false
                    viewModel.setEditMode(false)
                    viewModel.setPinValue(it.pin)
                    viewModel.setArchiveValue(it.archived)
                    etTitle.setText(it.title)
                    etContent.setText(it.content)
                    disableInputs()
                    binding.bottomActionBar.txtTime.text =
                        "Edited ${Utils.getFormattedDate(it.created)}"

                    //note background color
                    if (it.color == Constants.COLOR_DEFAULT) {
                        val colorInt = Utils.getColorFromAttr(
                            requireContext(),
                            com.google.android.material.R.attr.colorSurface
                        )
                        binding.parent.setBackgroundColor(colorInt)
                    } else {
                        val colorInt = Color.parseColor(
                            requireContext().resources.getString(
                                colorsUtil.getColor(it.color)
                            )
                        )
                        binding.parent.setBackgroundColor(colorInt)
                    }
                }
            }
        } else { //new note
            if (args.deletedNote == null) {
                binding.parent.setOnClickListener {
                    if (viewModel.currentNote.value == null || viewModel.editMode.value == true) {
                        //todo -> get toolbar reference from activity and make 'read mode' tag textview invisible

                        binding.etContent.requestFocus()
                        Utils.showKeyboard(requireActivity(), binding.etContent)
                    }
                }

                binding.bottomActionBar.txtTime.text =
                    "Edited ${Utils.getFormattedTime(System.currentTimeMillis())}"

                val colorInt = Utils.getColorFromAttr(
                    requireContext(),
                    com.google.android.material.R.attr.colorSurface
                )
                binding.parent.setBackgroundColor(colorInt)

            } else {  //deleted note
                //todo disable edit text
                //disable bottom action bar actions


            }
        }


        viewModel.editMode.observe(viewLifecycleOwner) {
            updateUIState(it)
        }

        viewModel.pinValue.observe(viewLifecycleOwner) {
            updateMenuPinUnpin()
        }

        viewModel.archiveValue.observe(viewLifecycleOwner) {
            updateMenuArchive()
        }

        viewModel.selectedColor.observe(viewLifecycleOwner) { colorPosition ->
            var colorName = Constants.COLOR_DEFAULT
            if (colorPosition == 0) {
                val colorInt = Utils.getColorFromAttr(
                    requireContext(),
                    com.google.android.material.R.attr.colorSurface
                )
                binding.parent.setBackgroundColor(colorInt)
            } else {
                colorName = colorsUtil.getPosition(colorPosition)
                val colorHex = requireContext().resources.getString(colorsUtil.getColor(colorName))
                val colorInt = Color.parseColor(colorHex)
                binding.parent.setBackgroundColor(colorInt)
            }

            //todo update note color in room table here
            if (viewModel.currentNote.value != null) { //old note
                val updatedNote = viewModel.currentNote.value!!.copy(color = colorName)
                viewModel.setCurrentNote(updatedNote)
                viewModel.onUpdateClick(updatedNote)
            } else { //new note
                val updatedTempNote = viewModel.tempNote.value!!.copy(color = colorName)
                viewModel.setTempNote(updatedTempNote)
                //tempNote = tempNote.copy(color = colorName)
            }
        }

        viewModel.masterPasswordLiveData.observe(viewLifecycleOwner) {
            masterPassword = it
            Log.d("TAG", "mastPassword live: $masterPassword")
        }


        loadMasterPassword()
        updatePasswordIcon()
        bottomActionClickEvent()

        observeEvents()


        //showing tooltip about password protection feature
        /*  binding.bottomActionBar.btPassword.showAlignTop(
              Utils.setTooltip(
                  requireContext(),
                  "Password protect your note from here",
                  R.drawable.ic_lock_24,
                  R.color.colorOnPrimary,
                  R.color.colorPrimary,
                  viewLifecycleOwner
              ), 0,20
          )*/

        //used this to update masterPassword value with updated password
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.collectLatest { event ->
                when (event) {
                    is NoteDetailViewModel.TasksEvent.ShowUndoDeleteNoteMessage -> {
                        //todo fragment with result to note list fragment to show undo button
                    }
                    is NoteDetailViewModel.TasksEvent.OnNewNoteSavedConfirmationMessage -> {
                        //todo fragment with result to note list fragment to scroll recycler view to top
                    }
                    is NoteDetailViewModel.TasksEvent.OnNoteUpdatedConfirmationMessage -> {
                        //nothing here
                    }
                }
            }
        }
    }

    private fun loadMasterPassword() {
        //used this to get masterPassword value immediately
        viewLifecycleOwner.lifecycleScope.launch {
            masterPassword = viewModel.masterPasswordFlow.first()
            Log.d("TAG", "masterPassword: $masterPassword")
        }
    }

    private fun bottomActionClickEvent() {
        binding.bottomActionBar.btPassword.isEnabled = args.deletedNote == null
        binding.bottomActionBar.btColor.isEnabled = args.deletedNote == null

        binding.bottomActionBar.apply {

            btColor.setOnClickListener {
                showColorPaletteBottomSheet()
            }

            btOptions.setOnClickListener {
                onOptionClicked()
            }

            btPassword.setOnClickListener {
                addPassword()
            }
        }


    }

    private fun onOptionClicked() {
        //todo show bottomSheet with options -> delete,add/update password, mark as complete,etc

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomsheet: View = LayoutInflater.from(context).inflate(R.layout.bs_options, null)

        val addRemovePassword = bottomsheet.findViewById<TextView>(R.id.add_password)
        val delete = bottomsheet.findViewById<TextView>(R.id.delete)
        val share = bottomsheet.findViewById<TextView>(R.id.share)
        val label = bottomsheet.findViewById<TextView>(R.id.label)
        val pin = bottomsheet.findViewById<TextView>(R.id.pin)
        val archive = bottomsheet.findViewById<TextView>(R.id.archive)
        val restore = bottomsheet.findViewById<TextView>(R.id.restore)
        val deleteForever = bottomsheet.findViewById<TextView>(R.id.delete_forever)

        if (args.deletedNote==null) {
            label.isVisible = true
            delete.isVisible = true
            share.isVisible = true
            pin.isVisible = false
            addRemovePassword.isVisible = false
            archive.isVisible = false
            restore.isVisible = false
            deleteForever.isVisible = false
        }else{
            restore.isVisible = true
            deleteForever.isVisible = true
            addRemovePassword.isVisible = false
            delete.isVisible = false
            share.isVisible = false
            label.isVisible = false
            pin.isVisible = false
            archive.isVisible = false
        }

        delete.setOnClickListener {
            deleteNote()
            bottomSheetDialog.dismiss()
        }

        share.setOnClickListener {
            //todo
            bottomSheetDialog.dismiss()
        }

        label.setOnClickListener {
            //todo
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(bottomsheet)
        bottomSheetDialog.show()
    }

    private fun updatePasswordIcon() {
        viewModel.currentNote.value?.let {
            if (it.isPasswordProtected)
                binding.bottomActionBar.btPassword.icon =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.ic_lock_filled_24)
            else AppCompatResources.getDrawable(requireContext(), R.drawable.ic_lock_open_24)
        }

        viewModel.tempNote.value?.let {
            if (it.isPasswordProtected)
                binding.bottomActionBar.btPassword.icon =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.ic_lock_filled_24)
            else AppCompatResources.getDrawable(requireContext(), R.drawable.ic_lock_open_24)
        }

    }

    private fun updateMenuPinUnpin() {
        menuPin?.let {
            if (viewModel.pinValue.value == true) {
                it.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_pin_24)
                it.title = "Unpin"
            } else {
                it.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_pin_outline_24)
                it.title = "Pin"
            }
        }
    }

    private fun updateMenuArchive() {
        menuArchive?.let {
            if (viewModel.archiveValue.value == true) {
                it.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_unarchive_24)
                it.title = "Unarchive"
            } else {
                it.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_archive_24)
                it.title = "Archive"
            }
        }

    }


    private fun updateMenuEditSave() {
        menuEdit?.let {
            if (viewModel.editMode.value == true) {
                it.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_check_24)
                it.title = "Save"
            } else {
                it.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_edit_24)
                it.title = "Edit"
            }
        }
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
        menuPin = menu.findItem(R.id.action_pin)
        menuArchive = menu.findItem(R.id.action_archive)

        updateMenuEditSave()
        updateMenuPinUnpin()
        updateMenuArchive()
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
            binding.etContent.requestFocus()
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
                            setFragmentResult(
                                Constants.FRAGMENT_RESULT_REQUEST_KEY,
                                bundleOf(
                                    "result" to Constants.NOTE_UPDATED_RESULT_OK,
                                    "note" to updatedNote
                                )
                            )
                        } else {  //create new note
                            val newNote =
                                viewModel.tempNote.value!!.copy(title = title, content = content)
                            viewModel.onSaveClick(newNote)
                            viewModel.setCurrentNote(newNote)
                            viewModel.setEditMode(false)
                            //todo here consider @tempNote properties
                            Utils.showSnackBar(
                                binding.parent,
                                "Note saved",
                                binding.bottomActionBar.bottomActionBar
                            )
                            setFragmentResult(
                                Constants.FRAGMENT_RESULT_REQUEST_KEY,
                                bundleOf(
                                    "result" to Constants.NOTE_ADDED_RESULT_OK,
                                    "note" to newNote
                                )
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
                val currentNote = viewModel.currentNote.value
                if (currentNote != null) { //already existing note
                    if (currentNote.pin) { //unpin
                        val updatedNote = currentNote.copy(pin = false)
                        viewModel.setCurrentNote(updatedNote)
                        viewModel.onUpdateClick(updatedNote)
                        viewModel.setPinValue(false)
                    } else { //pin
                        val updatedNote = currentNote.copy(pin = true)
                        viewModel.setCurrentNote(updatedNote)
                        viewModel.onUpdateClick(updatedNote)
                        viewModel.setPinValue(true)
                    }
                } else { //new note
                    val currentNote = viewModel.tempNote.value
                    if (currentNote!!.pin) { //unpin
                        val updatedNote = currentNote.copy(pin = false)
                        viewModel.setTempNote(updatedNote)
                        viewModel.setPinValue(false)
                    } else { //pin
                        val updatedNote = currentNote.copy(pin = true)
                        viewModel.setTempNote(updatedNote)
                        viewModel.setPinValue(true)
                    }
                }
                true
            }
            R.id.action_archive -> {
                val currentNote = viewModel.currentNote.value
                if (currentNote != null) { //already existing note
                    if (currentNote.archived) { //unarchived
                        val updatedNote = currentNote.copy(archived = false)
                        viewModel.setCurrentNote(updatedNote)
                        viewModel.onUpdateClick(updatedNote)
                        viewModel.setArchiveValue(false)
                        //todo show snackbar for unarchived and undo button
                        Utils.hideKeyboard(requireActivity())
                        Snackbar.make(binding.parent, "Note unarchived", Snackbar.LENGTH_SHORT)
                            .setAction("Undo") {
                                //todo archive the note
                                viewModel.setCurrentNote(currentNote.copy(archived = true))
                                viewModel.onUpdateClick(currentNote.copy(archived = true))
                                viewModel.setArchiveValue(true)
                            }.show()
                    } else { //archived
                        val updatedNote = currentNote.copy(archived = true)
                        viewModel.setCurrentNote(updatedNote)
                        viewModel.onUpdateClick(updatedNote)
                        viewModel.setArchiveValue(true)
                        //todo go back and show snackbar and undo action
                        setFragmentResult(
                            Constants.FRAGMENT_RESULT_REQUEST_KEY,
                            bundleOf(
                                "result" to Constants.NOTE_ARCHIVED_RESULT_OK,
                                "note" to viewModel.currentNote.value
                            )
                        )
                        findNavController().popBackStack()
                    }
                } else { //new note
                    val currentNote = viewModel.tempNote.value
                    if (currentNote!!.archived) { //unarchived
                        val updatedNote = currentNote.copy(archived = false)
                        viewModel.setTempNote(updatedNote)
                        viewModel.setArchiveValue(false)
                        //todo issue: show snackbar above soft keyboard
                        Utils.hideKeyboard(requireActivity())
                        Snackbar.make(binding.parent, "Note unarchived", Snackbar.LENGTH_SHORT)
                            .setAnchorView(binding.bottomActionBar.bottomActionBar)
                            .show()
                    } else { //archived
                        val updatedNote = currentNote.copy(archived = true)
                        viewModel.setTempNote(updatedNote)
                        viewModel.setArchiveValue(true)
                        //todo issue: show snackbar above soft keyboard
                        Utils.hideKeyboard(requireActivity())
                        Snackbar.make(binding.parent, "Note archived", Snackbar.LENGTH_SHORT)
                            .setAnchorView(binding.bottomActionBar.bottomActionBar)
                            .show()
                    }
                }
                true
            }
            else -> false
        }
    }

    private fun addPassword() {
        if (masterPassword.isBlank()) {
            bottomSheetCreateMasterPassword()
        } else {
            bottomSheetEnableDisableLock(masterPassword)
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
                        .setAnchorView(binding.bottomActionBar.bottomActionBar).show()
                    loadMasterPassword()
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

        et_confirm.doOnTextChanged { _, _, _, _ ->
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
        // bottomSheetDialog.setCancelable(false)

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
            if (viewModel.tempNote.value!!.isPasswordProtected) { //remove
                title.text = "Remove password protection"
                subtitle.text =
                    "Confirm your current master password to remove password protection for this note"
                bt_save.text = "Remove"

                bt_save.setOnClickListener {
                    //todo remove password

                    ly_password.isErrorEnabled = false
                    ly_password.error = null
                    if (et_password.text.toString() == password) {
                        val updatedTempNote =
                            viewModel.tempNote.value!!.copy(isPasswordProtected = false)
                        viewModel.setTempNote(updatedTempNote)
                        //tempNote = tempNote.copy(isPasswordProtected = false)
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
                        val updatedTempNote =
                            viewModel.tempNote.value!!.copy(isPasswordProtected = true)
                        viewModel.setTempNote(updatedTempNote)
                        //tempNote = tempNote.copy(isPasswordProtected = true)
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
        val currentNote = viewModel.currentNote.value
        if (currentNote != null) {
            if (currentNote.isPasswordProtected) {
                //todo task for password before deleting
                val bottomSheetDialog = BottomSheetDialog(requireContext())
                val bottomsheet: View =
                    LayoutInflater.from(context).inflate(R.layout.bs_add_password, null)

                val title = bottomsheet.findViewById<TextView>(R.id.txtTitle)
                val subtitle = bottomsheet.findViewById<TextView>(R.id.txtSubTitle)
                val progressBar = bottomsheet.findViewById<LinearProgressIndicator>(R.id.progress)
                val et_password = bottomsheet.findViewById<TextInputEditText>(R.id.et_addPassword)
                val ly_password = bottomsheet.findViewById<TextInputLayout>(R.id.lyt_addPassword)
                val bt_save = bottomsheet.findViewById<MaterialButton>(R.id.bt_save)
                val bt_cancel = bottomsheet.findViewById<MaterialButton>(R.id.bt_cancel)

                title.text = "Delete password protected note?"
                subtitle.text = "Master password needed for deleting this note"
                bt_save.text = "Confirm and Delete"
                bt_cancel.text = "Cancel"

                bt_save.setTextColor(
                    Utils.getColorFromAttr(
                        requireContext(),
                        com.google.android.material.R.attr.colorOnError
                    )
                )
                bt_save.setBackgroundColor(
                    Utils.getColorFromAttr(
                        requireContext(),
                        com.google.android.material.R.attr.colorError
                    )
                )
                // bt_cancel.setTextColor(Utils.getColorFromAttr(requireContext(), com.google.android.material.R.attr.colorOnErrorContainer))

                bt_save.setOnClickListener {
                    ly_password.isErrorEnabled = false
                    ly_password.error = null
                    if (et_password.text.toString() == masterPassword) {
                        viewModel.onDeleteClick(viewModel.currentNote.value!!)
                        bottomSheetDialog.dismiss()
                        setFragmentResult(
                            Constants.FRAGMENT_RESULT_REQUEST_KEY,
                            bundleOf(
                                "result" to Constants.NOTE_DELETE_RESULT_OK,
                                "note" to viewModel.currentNote.value
                            )
                        )
                        findNavController().popBackStack()
                    } else {
                        ly_password.error = "Wrong master password!"
                    }
                }

                et_password.doOnTextChanged { _, _, _, _ ->
                    ly_password.isErrorEnabled = false
                    ly_password.error = null
                }

                bt_cancel.setOnClickListener {
                    bottomSheetDialog.dismiss()
                }

                bottomSheetDialog.setContentView(bottomsheet)
                bottomSheetDialog.show()

            } else {
                val alertDialog = MaterialAlertDialogBuilder(requireContext())
                    .setMessage("Delete this note?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.onDeleteClick(viewModel.currentNote.value!!)

                        setFragmentResult(
                            Constants.FRAGMENT_RESULT_REQUEST_KEY, /*bundle*/
                            bundleOf(
                                "result" to Constants.NOTE_DELETE_RESULT_OK,
                                "note" to viewModel.currentNote.value
                            )
                        )
                        findNavController().popBackStack()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                alertDialog.show()
            }
        } else {
            findNavController().popBackStack()
        }
    }

    private fun showColorPaletteBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomsheet: View =
            LayoutInflater.from(context).inflate(R.layout.bs_color_palette, null)

        val recyclerView = bottomsheet.findViewById<RecyclerView>(R.id.rv_color_picker)

        val layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = ColorPickerAdapter(requireContext(), viewModel)

        bottomSheetDialog.setContentView(bottomsheet)
        bottomSheetDialog.show()
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
                    setFragmentResult(
                        Constants.FRAGMENT_RESULT_REQUEST_KEY, /*bundle*/
                        bundleOf(
                            "result" to Constants.NOTE_UPDATED_RESULT_OK,
                            "note" to updatedNote
                        )
                    )
                }
            } else { //new note
                if (title.isNotBlank() || content.isNotBlank()) {
                    //todo save new note
                    val newTempNote =
                        viewModel.tempNote.value!!.copy(title = title, content = content)
                    viewModel.setTempNote(newTempNote)
                    //val newNote = viewModel.tempNote.value!!.copy(title = title, content = content)
                    viewModel.onSaveClick(viewModel.tempNote.value!!)
                    setFragmentResult(
                        Constants.FRAGMENT_RESULT_REQUEST_KEY, /*bundle*/
                        bundleOf(
                            "result" to Constants.NOTE_ADDED_RESULT_OK,
                            "note" to viewModel.tempNote.value!!
                        )
                    )
                }

            }
        }
        super.onDestroyView()
        _binding = null
    }
}