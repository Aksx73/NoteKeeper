package com.android.note.keeper.ui.notelist

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.note.keeper.R
import com.android.note.keeper.data.model.Note
import com.android.note.keeper.databinding.FragmentNoteListBinding
import com.android.note.keeper.ui.MainActivity
import com.android.note.keeper.util.DemoUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
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

        binding.apply {
            recyclerView.apply {
                adapter = noteAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            fab.setOnClickListener {
                val action =
                    NoteListFragmentDirections.actionNoteListFragmentToNoteDetailFragment(null)
                findNavController().navigate(action)
            }

        }

        viewModel.notes.observe(viewLifecycleOwner) { notes ->
            noteAdapter.submitList(notes)
            binding.emptyView.isVisible = notes.isEmpty()
        }

        loadMasterPassword()


        //todo observe other events


    }

    private fun loadMasterPassword(){
        viewLifecycleOwner.lifecycleScope.launch {
            masterPassword = viewModel.masterPasswordFlow.first()
            Log.d("TAG", "addPassword: $masterPassword")
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        menuInflater.inflate(R.menu.menu_list, menu)

        val master_password = menu.findItem(R.id.action_master_password)
        master_password.title =
            if (masterPassword.isBlank()) "Create master password" else "Update master password"
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_master_password -> {
                bottomSheetUpdateMasterPassword()
                true
            }
            R.id.action_setting -> {
                //todo
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
        val ly_currentPassword = bottomsheet.findViewById<TextInputLayout>(R.id.lyt_currentPassword)
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
                        Snackbar.make(binding.parent, "Master password added!", Snackbar.LENGTH_LONG)
                            .setAnchorView(binding.fab)
                            .show()
                        //loadMasterPassword()

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
                            Snackbar.make(binding.parent, "Master password updated!", Snackbar.LENGTH_LONG)
                                .setAnchorView(binding.fab)
                                .show()
                            //loadMasterPassword()

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
            val action = NoteListFragmentDirections.actionNoteListFragmentToNoteDetailFragment(task)
            findNavController().navigate(action)
        }
    }

    override fun onOptionClick(task: Note) {
        //todo show bottomSheet with options -> delete,add/update password, mark as complete,etc

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomsheet: View = LayoutInflater.from(context).inflate(R.layout.bs_options, null)

        /* val et_password = bottomsheet.findViewById<TextInputEditText>(R.id.et_addPassword)
         val ly_password = bottomsheet.findViewById<TextInputLayout>(R.id.lyt_addPassword)
         val bt_save = bottomsheet.findViewById<MaterialButton>(R.id.bt_save)*/


        bottomSheetDialog.setContentView(bottomsheet)
        bottomSheetDialog.show()

    }
}