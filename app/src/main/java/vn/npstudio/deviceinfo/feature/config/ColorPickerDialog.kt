package vn.npstudio.deviceinfo.feature.config

import android.app.Dialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.databinding.DialogColorPickerBinding
import vn.npstudio.deviceinfo.domain.model.AppColor
import vn.npstudio.deviceinfo.ui.theme.ColorRole
import vn.npstudio.deviceinfo.ui.theme.ThemeOverlays

/** Primary/accent color dialog (spec 002 section 4.4). Back or tapping outside discards the pick. */
class ColorPickerDialog : DialogFragment() {

    private val viewModel: ConfigViewModel by activityViewModels()

    private lateinit var role: ColorRole
    private lateinit var picked: AppColor
    private var binding: DialogColorPickerBinding? = null
    private var adapter: SwatchAdapter? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()
        role = ColorRole.valueOf(checkNotNull(args.getString(ARG_ROLE)))
        picked = AppColor.valueOf(checkNotNull((savedInstanceState ?: args).getString(ARG_COLOR)))

        val binding = DialogColorPickerBinding.inflate(LayoutInflater.from(requireContext()))
        this.binding = binding
        binding.headerTitle.setText(
            if (role == ColorRole.PRIMARY) R.string.config_primary_color else R.string.config_accent_color,
        )
        val adapter = SwatchAdapter(role, picked, ::select)
        this.adapter = adapter
        binding.swatchList.adapter = adapter
        binding.swatchList.scrollToPosition(picked.ordinal)
        bindHeader()

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setNeutralButton(R.string.config_recommend, null)
            .setPositiveButton(android.R.string.ok, null)
            .create()
        // Replace the default listeners: Recommend must not close, and OK saves after the dialog is gone.
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener { select(AppColor.BRAND) }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { save() }
        }
        return dialog
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ARG_COLOR, picked.name)
    }

    override fun onDestroy() {
        binding = null
        adapter = null
        super.onDestroy()
    }

    private fun select(color: AppColor) {
        picked = color
        adapter?.selected = color
        bindHeader()
    }

    private fun bindHeader() {
        val header = binding?.headerTitle ?: return
        val context = header.context
        ViewCompat.setBackgroundTintList(
            header,
            ColorStateList.valueOf(ContextCompat.getColor(context, ThemeOverlays.swatch(role, picked))),
        )
        header.setTextColor(ContextCompat.getColor(context, ThemeOverlays.onSwatch(role, picked)))
    }

    // Remove the dialog synchronously before saving: a color change recreates the Activity, and a
    // still-attached dialog fragment would be restored and shown again.
    private fun save() {
        val viewModel = viewModel
        val role = role
        val color = picked
        dismissNow()
        viewModel.onColorSelected(role, color)
    }

    companion object {
        const val TAG = "color_picker"
        private const val ARG_ROLE = "role"
        private const val ARG_COLOR = "color"

        fun newInstance(role: ColorRole, current: AppColor) = ColorPickerDialog().apply {
            arguments = bundleOf(ARG_ROLE to role.name, ARG_COLOR to current.name)
        }
    }
}
