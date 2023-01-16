package com.android.note.keeper.ui.deleted

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.fragment.app.Fragment
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.note.keeper.R
import com.android.note.keeper.data.PreferenceManager
import com.android.note.keeper.data.model.DeletedNote
import com.android.note.keeper.databinding.FragmentArchiveNoteBinding
import com.android.note.keeper.databinding.FragmentDeletedNoteBinding
import com.android.note.keeper.ui.MainActivity
import com.android.note.keeper.ui.notelist.NoteAdapter
import com.android.note.keeper.ui.notelist.NoteListFragmentDirections
import com.android.note.keeper.ui.notelist.NoteListViewModel
import com.android.note.keeper.util.Constants
import com.android.note.keeper.util.MyItemDetailsLookup
import com.android.note.keeper.util.MyItemKeyProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DeletedNoteFragment : Fragment(R.layout.fragment_deleted_note), MenuProvider,
    DeletedNotesAdapter.OnItemClickListener {
    private var _binding: FragmentDeletedNoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var noteAdapter: DeletedNotesAdapter
    private val viewModel by viewModels<DeletedNoteViewModel>()

    private var tracker: SelectionTracker<Long>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDeletedNoteBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //val binding = FragmentDeletedNoteBinding.bind(view)
        (activity as MainActivity).readMode.isVisible = false

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        if (savedInstanceState != null)
            tracker?.onRestoreInstanceState(savedInstanceState)

        noteAdapter = DeletedNotesAdapter(this)

        binding.apply {
            recyclerView.adapter = noteAdapter
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }

        tracker = SelectionTracker.Builder<Long>(
            "mySelection",
            binding.recyclerView,
            StableIdKeyProvider(binding.recyclerView),
            // MyItemKeyProvider(binding.recyclerView),
            MyItemDetailsLookup(binding.recyclerView),
            StorageStrategy.createLongStorage()
        )
            .withSelectionPredicate(SelectionPredicates.createSelectAnything())
            // .withOnItemActivatedListener(myItemActivatedListener)
            .build()

        noteAdapter.tracker = tracker

        tracker?.addObserver(
            object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()
                    val items = tracker?.selection!!.size()
                    if(items > 0) {
                        // Change title and color of action bar

                    } else {
                        // Reset color and title to default values

                    }
                }
            })

        viewModel.deletedNotes.observe(viewLifecycleOwner) { notes ->
            noteAdapter.submitList(notes)
            binding.emptyView.isVisible = notes.isEmpty()
        }


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (outState != null)
            tracker?.onSaveInstanceState(outState)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_deleted, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_empty_bin -> {
                showDeleteAllDialog()
                true
            }
            else -> false
        }
    }

    private fun showDeleteAllDialog() {
        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Empty Recycle Bin?")
            .setMessage("All notes in Recycle Bin will be permanently deleted.")
            .setPositiveButton("Empty bin") { _, _ ->
                viewModel.onDeleteAllClick()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        alertDialog.show()

    }

    override fun onItemClick(task: DeletedNote) {
        val action =
            DeletedNoteFragmentDirections.actionDeletedNoteFragmentToNoteDetailFragment(deletedNote = task)
        findNavController().navigate(action)
    }

    override fun onItemLongClick(task: DeletedNote) {
        // TODO("Not yet implemented")
    }
}