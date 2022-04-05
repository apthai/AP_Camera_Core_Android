package com.apthai.apcameraxcore.galahad.editor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apthai.apcameraxcore.galahad.R
import com.apthai.apcameraxcore.galahad.databinding.FragmentApEditorShapesSelectDialogBinding
import com.apthai.apcameraxcore.galahad.editor.ColorPickerAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ja.burhanrashid52.photoeditor.shape.ShapeType

class ApEditorShapeSelectorFragment : BottomSheetDialogFragment(), SeekBar.OnSeekBarChangeListener {
    private var mProperties: Properties? = null

    interface Properties {
        fun onColorChanged(colorCode: Int)
        fun onOpacityChanged(opacity: Int)
        fun onShapeSizeChanged(shapeSize: Int)
        fun onShapePicked(shapeType: ShapeType?)
    }

    private var fragmentApEditorShapesSelectDialogBinding : FragmentApEditorShapesSelectDialogBinding?=null
    private val binding get() = fragmentApEditorShapesSelectDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ap_editor_shapes_select_dialog, container, false)
        fragmentApEditorShapesSelectDialogBinding = FragmentApEditorShapesSelectDialogBinding.bind(view)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // shape picker
        binding?.apEditorSelectShapeDialogShapeTypeRadioGroup?.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
            when (checkedId) {
                R.id.ap_editor_select_shape_dialog_shape_line_type_radio_text -> {
                    mProperties?.onShapePicked(ShapeType.LINE)
                }
                R.id.ap_editor_select_shape_dialog_shape_oval_type_radio_text -> {
                    mProperties?.onShapePicked(ShapeType.OVAL)
                }
                R.id.ap_editor_select_shape_dialog_shape_rectangle_type_radio_text -> {
                    mProperties?.onShapePicked(ShapeType.RECTANGLE)
                }
                else -> {
                    mProperties?.onShapePicked(ShapeType.BRUSH)
                }
            }
        }
        binding?.apEditorSelectShapeDialogShapeOpacitySeekBarView?.setOnSeekBarChangeListener(this)
        binding?.apEditorSelectShapeDialogShapeSizeSeekBarView?.setOnSeekBarChangeListener(this)

        // TODO(lucianocheng): Move layoutManager to a xml file.
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding?.apEditorSelectShapeDialogShapeColorRecyclerView?.layoutManager = layoutManager
        binding?.apEditorSelectShapeDialogShapeColorRecyclerView?.setHasFixedSize(true)
        val colorPickerAdapter = ColorPickerAdapter(requireActivity())
        colorPickerAdapter.setOnColorPickerClickListener(object :
                ColorPickerAdapter.OnColorPickerClickListener {
                override fun onColorPickerClickListener(colorCode: Int) {
                    dismiss()
                    mProperties?.onColorChanged(colorCode)
                }
            })
        binding?.apEditorSelectShapeDialogShapeColorRecyclerView?.adapter = colorPickerAdapter
    }

    fun setPropertiesChangeListener(properties: Properties?) {
        mProperties = properties
    }

    override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
        when (seekBar.id) {
            R.id.ap_editor_select_shape_dialog_shape_opacity_seek_bar_view -> mProperties?.onOpacityChanged(i)
            R.id.ap_editor_select_shape_dialog_shape_size_seek_bar_view -> mProperties?.onShapeSizeChanged(i)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}
    override fun onStopTrackingTouch(seekBar: SeekBar) {}
}
