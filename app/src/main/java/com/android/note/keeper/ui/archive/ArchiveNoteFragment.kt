package com.android.note.keeper.ui.archive

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.fragment.app.Fragment
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.note.keeper.R
import com.android.note.keeper.data.PreferenceManager
import com.android.note.keeper.data.model.Note
import com.android.note.keeper.databinding.FragmentArchiveNoteBinding
import com.android.note.keeper.databinding.FragmentNoteListBinding
import com.android.note.keeper.ui.MainActivity
import com.android.note.keeper.ui.notelist.NoteAdapter
import com.android.note.keeper.ui.notelist.NoteListFragmentDirections
import com.android.note.keeper.ui.notelist.NoteListViewModel
import com.android.note.keeper.ui.settings.SettingsActivity
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

@AndroidEntryPoint
class ArchiveNoteFragment : Fragment(R.layout.fragment_archive_note), NoteAdapter.OnItemClickListener,
    MenuProvider {

    private var _binding: FragmentArchiveNoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var noteAdapter: NoteAdapter
    private val viewModel by viewModels<NoteListViewModel>()
    private lateinit var masterPassword: String

    private lateinit var searchView: SearchView
    private var menuViewMode: MenuItem? = null
    private var updateNow = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentArchiveNoteBinding.inflate(inflater, container, false)

       // DemoUtils.addBottomSpaceInsetsIfNeeded(binding.root as ViewGroup, container)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentArchiveNoteBinding.bind(view)
        (activity as MainActivity).readMode.isVisible = false

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        noteAdapter = NoteAdapter(this)

        binding.apply {
            recyclerView.apply {
                adapter = noteAdapter
                //here layout manager is set using livedata observer
                when (runBlocking { viewModel.viewModeFlow.first() }) {
                    PreferenceManager.SINGLE_COLUMN -> {
                        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                    }
                    PreferenceManager.MULTI_COLUMN -> {
                        binding.recyclerView.layoutManager =
                            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    }
                }
                setHasFixedSize(true)
            }
        }

        loadMasterPassword()

        //used this to update masterPassword value with updated password
        viewModel.masterPasswordLiveData.observe(viewLifecycleOwner) {
            masterPassword = it
            Log.d("TAG", "mastPassword live: $masterPassword")
        }

        viewModel.archiveNotes.observe(viewLifecycleOwner) { notes ->
            noteAdapter.submitList(notes)
            binding.emptyView.isVisible = notes.isEmpty()
        }

        observeUiViewMode()

    }

    private fun observeUiViewMode() {
        viewModel.viewModeLiveData.observe(viewLifecycleOwner) { viewMode ->
            when (viewMode) {
                PreferenceManager.SINGLE_COLUMN -> viewModel.isMultiColumnView = false
                PreferenceManager.MULTI_COLUMN -> viewModel.isMultiColumnView = true
            }
            if (updateNow) {
                updateListView(viewMode)
                updateNow = false
            }
            setUpMenuViewModeIcon(viewModel.isMultiColumnView)
        }
    }

    private fun loadMasterPassword() {
        //used this to get masterPassword value immediately
        viewLifecycleOwner.lifecycleScope.launch {
            masterPassword = viewModel.masterPasswordFlow.first()
            Log.d("TAG", "masterPasswordFLow: $masterPassword")
        }
    }


    private fun updateListView(mode: Int) {
        when (mode) {
            PreferenceManager.SINGLE_COLUMN -> {
                binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                noteAdapter.notifyDataSetChanged()
            }
            PreferenceManager.MULTI_COLUMN -> {
                binding.recyclerView.layoutManager =
                    StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                noteAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun setUpMenuViewModeIcon(isMultiColumn: Boolean) {
        menuViewMode?.let {
            if (isMultiColumn) {
                it.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_list_view_24)
                it.title = "Single-column view"
            } else {
                it.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_grid_view_24)
                it.title = "Multi-column view"
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        menuInflater.inflate(R.menu.menu_archive, menu)
        menuViewMode = menu.findItem(R.id.action_view)
        val menuSearch = menu.findItem(R.id.action_search)
        searchView = menuSearch.actionView as SearchView
        searchView.queryHint = "Search your notes"

        val pendingQuery = viewModel.searchQuery.value
        if (pendingQuery != null && pendingQuery.isNotEmpty()) {
            menuSearch.expandActionView()
            searchView.setQuery(pendingQuery, false)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchQuery.value = newText
                return true
            }
        })

        setUpMenuViewModeIcon(viewModel.isMultiColumnView)

    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_view -> {
                lifecycleScope.launch {
                    when (viewModel.isMultiColumnView) {
                        true -> viewModel.onViewModeChanged(PreferenceManager.SINGLE_COLUMN)
                        false -> viewModel.onViewModeChanged(PreferenceManager.MULTI_COLUMN)
                    }
                }
                updateNow = true
                true
            }
            else -> false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        searchView.setOnQueryTextListener(null)
    }

    private fun deleteNote(note: Note) {
        if (note.isPasswordProtected) {
            //todo ask for password before deleting
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            val bottomsheet: View = LayoutInflater.from(context).inflate(R.layout.bs_add_password, null)

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

            bt_save.setOnClickListener {
                ly_password.isErrorEnabled = false
                ly_password.error = null
                if (et_password.text.toString() == masterPassword) {
                    viewModel.onDeleteClick(note)
                    Utils.showSnackBar(binding.parent, "Note deleted", binding.parent)
                    bottomSheetDialog.dismiss()
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
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.onDeleteClick(note)
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
            alertDialog.show()
        }
    }

    private fun addPassword(note: Note) {
        if (masterPassword.isBlank()) {
            bottomSheetCreateMasterPassword(note)
            //todo also enable password + create master password
        } else {
            bottomSheetEnableDisableLock(note)
        }
    }

    private fun pinUnpin(note: Note){
        if (note.pin) { //unpin
            val updatedNote = note.copy(pin = false)
            viewModel.onUpdateClick(updatedNote)
        }else { //pin
            val updatedNote = note.copy(pin = true)
            viewModel.onUpdateClick(updatedNote)
        }
    }

    private fun archive(note: Note){
        if (note.archived) { //unarchive
            val updatedNote = note.copy(archived = false)
            viewModel.onUpdateClick(updatedNote)
            Snackbar.make(binding.parent,"Note unarchived", Snackbar.LENGTH_SHORT)
                .setAction("Undo"){
                    //todo archive the note
                    val archivedNote = note.copy(archived = true)
                    viewModel.onUpdateClick(archivedNote)
                }.show()
        }else { //archive
            val updatedNote = note.copy(archived = true)
            viewModel.onUpdateClick(updatedNote)
            Snackbar.make(binding.parent,"Note archived", Snackbar.LENGTH_SHORT)
                .setAction("Undo"){
                    //todo unarchive the note
                    val unarchivedNote = note.copy(archived = false)
                    viewModel.onUpdateClick(unarchivedNote)
                }.show()
        }
    }

    private fun bottomSheetCreateMasterPassword(note: Note) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomsheet: View =
            LayoutInflater.from(context).inflate(R.layout.bs_create_password, null)
        // bottomSheetDialog.setCancelable(false)

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
                    //enable lock of this note
                    val updatedNote = note.copy(isPasswordProtected = true)
                    viewModel.onUpdateClick(updatedNote)

                    Snackbar.make(
                        binding.parent,
                        "Master password added! And lock enabled for this note",
                        Snackbar.LENGTH_LONG
                    )
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

    private fun bottomSheetEnableDisableLock(note: Note) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomsheet: View =
            LayoutInflater.from(context).inflate(R.layout.bs_add_password, null)
        //bottomSheetDialog.setCancelable(false)

        val title = bottomsheet.findViewById<TextView>(R.id.txtTitle)
        val subtitle = bottomsheet.findViewById<TextView>(R.id.txtSubTitle)
        val progressBar = bottomsheet.findViewById<LinearProgressIndicator>(R.id.progress)
        val et_password = bottomsheet.findViewById<TextInputEditText>(R.id.et_addPassword)
        val ly_password = bottomsheet.findViewById<TextInputLayout>(R.id.lyt_addPassword)
        val bt_save = bottomsheet.findViewById<MaterialButton>(R.id.bt_save)
        val bt_cancel = bottomsheet.findViewById<MaterialButton>(R.id.bt_cancel)

        if (note.isPasswordProtected) {
            //remove password
            title.text = "Remove password protection"
            subtitle.text =
                "Confirm your current master password to remove password protection for this note"
            bt_save.text = "Remove"

            bt_save.setOnClickListener {
                //todo remove password

                ly_password.isErrorEnabled = false
                ly_password.error = null
                if (et_password.text.toString() == masterPassword) {
                    val updatedNote = note.copy(isPasswordProtected = false)
                    viewModel.onUpdateClick(updatedNote)
                    Utils.showSnackBar(binding.parent, "Lock disabled", binding.parent)
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
                if (et_password.text.toString() == masterPassword) {
                    val updatedNote = note.copy(isPasswordProtected = true)
                    viewModel.onUpdateClick(updatedNote)
                    Utils.showSnackBar(binding.parent, "Lock enabled", binding.parent)
                    bottomSheetDialog.dismiss()
                } else {
                    ly_password.error = "Wrong master password!"
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

    override fun onItemClick(task: Note) {
        if (task.isPasswordProtected) {
            //todo show material dialog to enter master password

            val dialogView: View = layoutInflater.inflate(R.layout.dialog_enter_password, null)
            val et_password = dialogView.findViewById(R.id.et_password) as TextInputEditText
            val ly_password = dialogView.findViewById(R.id.ly_password) as TextInputLayout

            et_password.doOnTextChanged { _, _, _, _ ->
                ly_password.isErrorEnabled = false
                ly_password.error = null
            }

            val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
                .setTitle("Enter password")
                //.setCancelable(false)
                .setView(dialogView)
                .setPositiveButton("Confirm") { _, _ ->
                    if (et_password.text.toString() == masterPassword) {
                        val action = ArchiveNoteFragmentDirections.actionArchiveNoteFragmentToNoteDetailFragment(note = task)
                        findNavController().navigate(action)
                    } else {
                        ly_password.isErrorEnabled = true
                        ly_password.error = "Incorrect password"
                    }
                }.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            dialogBuilder.show()
        } else {
            val action = ArchiveNoteFragmentDirections.actionArchiveNoteFragmentToNoteDetailFragment(note = task)
            findNavController().navigate(action)
        }

    }

    override fun onOptionClick(task: Note) {
        //todo show bottomSheet with options -> delete,add/update password, mark as complete,etc

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomsheet: View = LayoutInflater.from(context).inflate(R.layout.bs_options, null)

        val addRemovePassword = bottomsheet.findViewById<TextView>(R.id.add_password)
        val delete = bottomsheet.findViewById<TextView>(R.id.delete)
        val share = bottomsheet.findViewById<TextView>(R.id.share)
        val label = bottomsheet.findViewById<TextView>(R.id.label)
        val pin = bottomsheet.findViewById<TextView>(R.id.pin)
        val archive = bottomsheet.findViewById<TextView>(R.id.archive)

        addRemovePassword.isVisible = true
        pin.isVisible = true
        label.isVisible = false

        if (task.isPasswordProtected){
            addRemovePassword.text = "Remove lock"
            addRemovePassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_filled_24, 0, 0, 0);
        } else{
            addRemovePassword.text = "Add lock"
            addRemovePassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_open_24, 0, 0, 0);
        }
        if (task.pin){
            pin.text = "Unpin"
            pin.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pin_24, 0, 0, 0);
        }
        else{
            pin.text = "Pin"
            pin.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pin_outline_24, 0, 0, 0);
        }
        if (task.archived){
            archive.text = "Unarchive"
            archive.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_archive_24, 0, 0, 0);
        }
        else{
            archive.text = "Archive"
            archive.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_unarchive_24, 0, 0, 0);
        }

        delete.setOnClickListener {
            deleteNote(task)
            bottomSheetDialog.dismiss()
        }

        addRemovePassword.setOnClickListener {
            addPassword(task)
            bottomSheetDialog.dismiss()
        }

        pin.setOnClickListener {
            pinUnpin(task)
            bottomSheetDialog.dismiss()
        }

        archive.setOnClickListener {
            archive(task)
            bottomSheetDialog.dismiss()
        }

        share.setOnClickListener {
            //todo
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(bottomsheet)
        bottomSheetDialog.show()

    }


}