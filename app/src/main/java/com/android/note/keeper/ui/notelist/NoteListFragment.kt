package com.android.note.keeper.ui.notelist

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
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.android.note.keeper.R
import com.android.note.keeper.data.PreferenceManager
import com.android.note.keeper.data.model.Note
import com.android.note.keeper.databinding.FragmentNoteListBinding
import com.android.note.keeper.ui.MainActivity
import com.android.note.keeper.ui.settings.SettingsActivity
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@AndroidEntryPoint
class NoteListFragment : Fragment(R.layout.fragment_note_list), NoteAdapter.OnItemClickListener,
    MenuProvider {

    private var _binding: FragmentNoteListBinding? = null
    private val binding get() = _binding!!

    private lateinit var noteAdapter: NoteAdapter
    private val viewModel by viewModels<NoteListViewModel>()
    private lateinit var masterPassword: String

    private lateinit var searchView: SearchView
    private var menuViewMode: MenuItem? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNoteListBinding.inflate(inflater, container, false)

        DemoUtils.addBottomSpaceInsetsIfNeeded(binding.root as ViewGroup, container)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentNoteListBinding.bind(view)

        (activity as MainActivity).toolbar.findViewById<TextView>(R.id.currentMode).isVisible =
            false

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        noteAdapter = NoteAdapter(this)

        //observeUiViewMode()

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

            fab.setOnClickListener {
                val action =
                    NoteListFragmentDirections.actionNoteListFragmentToNoteDetailFragment(null)
                findNavController().navigate(action)
            }
        }

        viewModel.notes.observe(viewLifecycleOwner) { notes ->
            noteAdapter.submitList(notes) {
                // binding.recyclerView.scrollToPosition(0)
            }
            binding.emptyView.isVisible = notes.isEmpty()
        }

        loadMasterPassword()

        //used this to update masterPassword value with updated password
        viewModel.masterPasswordLiveData.observe(viewLifecycleOwner) {
            masterPassword = it
            Log.d("TAG", "mastPassword live: $masterPassword")
        }

        observeEvents()
    }

    private fun observeEvents() {
        lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.collectLatest { event ->
                when (event) {
                    is NoteListViewModel.TasksEvent.OnNoteUpdatedConfirmationMessage -> {
                       //nothing here
                    }
                    is NoteListViewModel.TasksEvent.ShowUndoDeleteNoteMessage -> {
                        Snackbar.make(requireView(), "Note deleted", Snackbar.LENGTH_LONG)
                            .setAction("Undo") {
                                viewModel.onUndoDeleteClick(event.note)
                            }.show()
                    }
                    is NoteListViewModel.TasksEvent.OnNewNoteSavedConfirmationMessage -> {
                       // nothing here
                    }
                }
            }
        }
    }

    private fun loadMasterPassword() {
        //used this to get masterPassword value immediately
        viewLifecycleOwner.lifecycleScope.launch {
            masterPassword = viewModel.masterPasswordFlow.first()
            Log.d("TAG", "masterPasswordFLow: $masterPassword")
        }
    }

    private fun observeUiViewMode() {
        viewModel.viewModeLiveData.observe(viewLifecycleOwner) { viewMode ->
            when (viewMode) {
                PreferenceManager.SINGLE_COLUMN -> viewModel.isMultiColumnView = false
                PreferenceManager.MULTI_COLUMN -> viewModel.isMultiColumnView = true
            }
            updateListView(viewMode)
            setUpMenuViewModeIcon(viewModel.isMultiColumnView)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        searchView.setOnQueryTextListener(null)
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        menuInflater.inflate(R.menu.menu_list, menu)
        menuViewMode = menu.findItem(R.id.action_view)
        val menuSearch = menu.findItem(R.id.action_search)
        searchView = menuSearch.actionView as SearchView
        searchView.queryHint = "Search your notes"

        val master_password = menu.findItem(R.id.action_master_password)

        master_password.title = if (masterPassword.isBlank()) "Create master password"
        else "Update master password"

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
            R.id.action_master_password -> {
                bottomSheetUpdateMasterPassword()
                true
            }
            R.id.action_setting -> {
                val intent = Intent((activity as MainActivity), SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_view -> {
                lifecycleScope.launch {
                    when (viewModel.isMultiColumnView) {
                        true -> viewModel.onViewModeChanged(PreferenceManager.SINGLE_COLUMN)
                        false -> viewModel.onViewModeChanged(PreferenceManager.MULTI_COLUMN)
                    }
                }
                observeUiViewMode()
                //setUpMenuViewModeIcon(viewModel.isMultiColumnView)
                true
            }
            else -> false
        }
    }

    private fun bottomSheetUpdateMasterPassword() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomsheet: View =
            LayoutInflater.from(context).inflate(R.layout.bs_create_password, null)
        bottomSheetDialog.setCancelable(false)

        val et_currentPassword =
            bottomsheet.findViewById<TextInputEditText>(R.id.et_currentPassword)
        val ly_currentPassword =
            bottomsheet.findViewById<TextInputLayout>(R.id.lyt_currentPassword)
        val et_password = bottomsheet.findViewById<TextInputEditText>(R.id.et_addPassword)
        val ly_password = bottomsheet.findViewById<TextInputLayout>(R.id.lyt_addPassword)
        val et_confirm = bottomsheet.findViewById<TextInputEditText>(R.id.et_confirmPassword)
        val ly_confirm = bottomsheet.findViewById<TextInputLayout>(R.id.lyt_confirmPassword)
        val bt_save = bottomsheet.findViewById<MaterialButton>(R.id.bt_save)
        val bt_cancel = bottomsheet.findViewById<MaterialButton>(R.id.bt_cancel)

        //todo

        ly_currentPassword.isVisible = masterPassword.isNotBlank()

        bt_save.setOnClickListener {
            //crete new master password
            if (masterPassword.isBlank()) {
                if (et_password.text.toString().isNotBlank() && et_confirm.text.toString()
                        .isNotBlank()
                ) {
                    if (et_password.text.toString() == et_confirm.text.toString()) {
                        //todo save to datastore
                        viewModel.setMasterPassword(et_password.text.toString())
                        masterPassword = et_password.text.toString()
                        Snackbar.make(
                            binding.parent,
                            "Master password added!",
                            Snackbar.LENGTH_LONG
                        )
                            .setAnchorView(binding.fab)
                            .show()
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
            } else {
                if (et_currentPassword.text.toString() == masterPassword) {
                    // correct
                    if (et_password.text.toString().isNotBlank() && et_confirm.text.toString()
                            .isNotBlank()
                    ) {
                        if (et_password.text.toString() == et_confirm.text.toString()) {
                            //todo save to datastore
                            viewModel.setMasterPassword(et_password.text.toString())
                            masterPassword = et_password.text.toString()
                            Snackbar.make(
                                binding.parent,
                                "Master password updated!",
                                Snackbar.LENGTH_LONG
                            )
                                .setAnchorView(binding.fab)
                                .show()
                            loadMasterPassword()

                            bottomSheetDialog.dismiss()
                        } else { //password not match
                            ly_confirm.isErrorEnabled = true
                            ly_confirm.error = "New password doesn't match"
                        }
                    } else { // show error for edit text
                        ly_confirm.isErrorEnabled = true
                        ly_confirm.error = "Re-enter new password here"
                    }
                } else {
                    ly_currentPassword.isErrorEnabled = true
                    ly_currentPassword.error = "Current password doesn't match"
                }

            }

        }

        et_confirm.doOnTextChanged { text, start, before, count ->
            ly_confirm.isErrorEnabled = false
            ly_confirm.error = null
        }

        et_currentPassword.doOnTextChanged { text, start, before, count ->
            ly_currentPassword.isErrorEnabled = false
            ly_currentPassword.error = null
        }


        bt_cancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }


        bottomSheetDialog.setContentView(bottomsheet)
        bottomSheetDialog.show()
    }

    private fun deleteNote(note: Note) {
        if (note.isPasswordProtected) {
            //todo ask for password before deleting
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
                    viewModel.onDeleteClick(note)
                    Utils.showSnackBar(binding.parent, "Note deleted", binding.fab)
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
                        .setAnchorView(binding.fab)
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
                    Utils.showSnackBar(binding.parent, "Lock disabled", binding.fab)
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
                    Utils.showSnackBar(binding.parent, "Lock enabled", binding.fab)
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

            et_password.doOnTextChanged { text, start, before, count ->
                ly_password.isErrorEnabled = false
                ly_password.error = null
            }

            val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
                .setTitle("Enter password")
                //.setCancelable(false)
                .setView(dialogView)
                .setPositiveButton("Confirm") { _, _ ->
                    if (et_password.text.toString() == masterPassword) {
                        val action =
                            NoteListFragmentDirections.actionNoteListFragmentToNoteDetailFragment(
                                task
                            )
                        findNavController().navigate(action)
                    } else {
                        ly_password.isErrorEnabled = true
                        ly_password.error = "Incorrect password"
                    }
                }.setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                .create()
            dialogBuilder.show()
        } else {
            val action =
                NoteListFragmentDirections.actionNoteListFragmentToNoteDetailFragment(task)
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

        addRemovePassword.isVisible = true
        label.isVisible = false

        if (task.isPasswordProtected) addRemovePassword.text = "Remove lock"
        else addRemovePassword.text = "Add lock"

        delete.setOnClickListener {
            deleteNote(task)
            bottomSheetDialog.dismiss()
        }

        addRemovePassword.setOnClickListener {
            addPassword(task)
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