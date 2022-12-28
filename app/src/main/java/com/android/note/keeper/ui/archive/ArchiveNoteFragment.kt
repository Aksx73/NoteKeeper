package com.android.note.keeper.ui.archive

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
                        val action =
                            NoteListFragmentDirections.actionNoteListFragmentToNoteDetailFragment(
                                task
                            )
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
            val action =
                ArchiveNoteFragmentDirections.actionArchiveNoteFragmentToNoteDetailFragment(task)
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
           // deleteNote(task)
            bottomSheetDialog.dismiss()
        }

        addRemovePassword.setOnClickListener {
           // addPassword(task)
            bottomSheetDialog.dismiss()
        }

        pin.setOnClickListener {
          //  pinUnpin(task)
            bottomSheetDialog.dismiss()
        }

        archive.setOnClickListener {
           // archive(task)
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