package com.android.note.keeper.ui.deleted

import android.content.ClipData.Item
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.view.ActionMode
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.note.keeper.R
import com.android.note.keeper.data.model.DeletedNote
import com.android.note.keeper.databinding.FragmentDeletedNoteBinding
import com.android.note.keeper.ui.MainActivity
import com.android.note.keeper.util.MyItemDetailsLookup
import com.android.note.keeper.util.MyItemKeyProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DeletedNoteFragment : Fragment(R.layout.fragment_deleted_note), MenuProvider,
    DeletedNotesAdapter.OnItemClickListener, ActionMode.Callback {
    private var _binding: FragmentDeletedNoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var noteAdapter: DeletedNotesAdapter
    private val viewModel by viewModels<DeletedNoteViewModel>()

    private var tracker: SelectionTracker<Long>? = null
    private var actionMode: ActionMode? = null

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

        tracker = SelectionTracker.Builder(
            "mySelection",
            binding.recyclerView,
            //StableIdKeyProvider(binding.recyclerView),
            MyItemKeyProvider(binding.recyclerView),
            MyItemDetailsLookup(binding.recyclerView),
            StorageStrategy.createLongStorage()
        )
            .withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .withOnItemActivatedListener { item, _ ->
                Log.d("TAG", "Selected ItemId: $item")
                true
            }
            .withOnDragInitiatedListener {
                Log.d("TAG", "onDragInitiated")
                true
            }
            .build()

        noteAdapter.tracker = tracker

        tracker?.addObserver(
            object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()
                    val items = tracker?.selection!!.size()

                    if (actionMode == null) {
                        val currentActivity = activity as MainActivity
                        actionMode = currentActivity.startSupportActionMode(this@DeletedNoteFragment)!!
                        //todo
                    }

                    if (items > 0) {
                        actionMode?.title = items.toString()
                    } else {
                        actionMode?.finish()
                    }
                }

            })

        viewModel.deletedNotes.observe(viewLifecycleOwner) { notes ->
            noteAdapter.submitList(notes)
            binding.emptyView.isVisible = notes.isEmpty()
        }

        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    tracker?.let {
                        if (it.clearSelection()) {
                            return;
                        } else {
                            requireActivity().onBackPressed()
                        }
                    }
                    //todo handle back press

                }
            }
            )


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (outState != null)
            tracker?.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        tracker?.onRestoreInstanceState(savedInstanceState)
        if (tracker!!.hasSelection()) {
            actionMode =
                (activity as MainActivity).startSupportActionMode(this@DeletedNoteFragment)!!
            actionMode?.title = tracker!!.selection.size().toString()
        }
        super.onViewStateRestored(savedInstanceState)
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

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.menuInflater?.inflate(R.menu.menu_action_mode_deleted, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = true

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_restore -> {

                actionMode?.finish()
                true
            }

            R.id.action_delete_forever -> {
                //todo
                true
            }
            else -> {
                false
            }
        }
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        tracker?.clearSelection()
        actionMode = null
        //todo set adapter to recycler view
    }
}