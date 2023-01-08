package com.android.note.keeper.ui.deleted

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
import com.android.note.keeper.R
import com.android.note.keeper.data.PreferenceManager
import com.android.note.keeper.data.model.DeletedNote
import com.android.note.keeper.databinding.FragmentArchiveNoteBinding
import com.android.note.keeper.databinding.FragmentDeletedNoteBinding
import com.android.note.keeper.ui.MainActivity
import com.android.note.keeper.ui.notelist.NoteAdapter
import com.android.note.keeper.ui.notelist.NoteListViewModel
import com.android.note.keeper.util.Constants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch


class DeletedNoteFragment : Fragment(R.layout.fragment_deleted_note), MenuProvider,
    DeletedNotesAdapter.OnItemClickListener {
    private var _binding: FragmentDeletedNoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var noteAdapter: DeletedNotesAdapter
    private val viewModel by viewModels<DeletedNoteViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDeletedNoteBinding.inflate(inflater, container, false)

        // DemoUtils.addBottomSpaceInsetsIfNeeded(binding.root as ViewGroup, container)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentArchiveNoteBinding.bind(view)
        (activity as MainActivity).readMode.isVisible = false

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        noteAdapter = DeletedNotesAdapter(this)

        binding.apply {
            recyclerView.adapter = noteAdapter
            recyclerView.setHasFixedSize(true)

        }

        viewModel.deletedNotes.observe(viewLifecycleOwner) { notes ->
            noteAdapter.submitList(notes)
            binding.emptyView.isVisible = notes.isEmpty()
        }


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
                //todo call delete all notes from view model
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        alertDialog.show()

    }

    override fun onItemClick(task: DeletedNote) {
        TODO("Not yet implemented")
    }
}