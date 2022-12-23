package com.android.note.keeper.ui.archive

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.fragment.app.Fragment
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
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

       /* val master_password = menu.findItem(R.id.action_master_password)

        master_password.title = if (masterPassword.isBlank()) "Create master password"
        else "Update master password"*/

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

       // setUpMenuViewModeIcon(viewModel.isMultiColumnView)

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
        TODO("Not yet implemented")
    }

    override fun onOptionClick(task: Note) {
        TODO("Not yet implemented")
    }


}