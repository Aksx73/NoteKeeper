package com.android.note.keeper.ui.deleted

import android.os.Bundle
import android.view.*
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.MenuHost
import androidx.fragment.app.Fragment
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.android.note.keeper.R
import com.android.note.keeper.data.PreferenceManager
import com.android.note.keeper.databinding.FragmentArchiveNoteBinding
import com.android.note.keeper.databinding.FragmentDeletedNoteBinding
import com.android.note.keeper.ui.MainActivity
import kotlinx.coroutines.launch


class DeletedNoteFragment : Fragment(R.layout.fragment_deleted_note) , MenuProvider {
    private var _binding: FragmentDeletedNoteBinding? = null
    private val binding get() = _binding!!

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

    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_deleted, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_empty_bin -> {
                //todo delete all notes
                true
            }
            else -> false
        }
    }
}