package com.apthai.apcameraxcore.galahad.editor.fragment

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apthai.apcameraxcore.galahad.R
import com.apthai.apcameraxcore.galahad.databinding.FragmentApEditorAddTextDialogBinding
import com.apthai.apcameraxcore.galahad.editor.ColorPickerAdapter
import kotlin.jvm.JvmOverloads

/**
 * Created by Burhanuddin Rashid on 1/16/2018.
 */
class ApEditorAddTextEditorFragment : DialogFragment() {

    private var mInputMethodManager: InputMethodManager? = null
    private var mColorCode = 0
    private var mTextEditorListener: TextEditorListener? = null

    private var fragmentApEditorAddTextDialogBinding : FragmentApEditorAddTextDialogBinding?=null
    private val binding get() = fragmentApEditorAddTextDialogBinding

    interface TextEditorListener {
        fun onDone(inputText: String?, colorCode: Int)
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog?.window?.setLayout(width, height)
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ap_editor_add_text_dialog, container, false)
        fragmentApEditorAddTextDialogBinding = FragmentApEditorAddTextDialogBinding.bind(view)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mInputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        // Setup the color picker for text color
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding?.apEditorAddTextDialogColorPickerRecyclerView?.layoutManager = layoutManager
        binding?.apEditorAddTextDialogColorPickerRecyclerView?.setHasFixedSize(true)
        val colorPickerAdapter = ColorPickerAdapter(requireActivity())

        // This listener will change the text color when clicked on any color from picker
        colorPickerAdapter.setOnColorPickerClickListener(object :
                ColorPickerAdapter.OnColorPickerClickListener {
                override fun onColorPickerClickListener(colorCode: Int) {
                    mColorCode = colorCode
                    binding?.apEditorAddTextDialogTextField?.setTextColor(colorCode)
                }
            })
        binding?.apEditorAddTextDialogColorPickerRecyclerView?.adapter = colorPickerAdapter
        binding?.apEditorAddTextDialogTextField?.setText(requireArguments().getString(EXTRA_INPUT_TEXT))
        mColorCode = requireArguments().getInt(EXTRA_COLOR_CODE)
        binding?.apEditorAddTextDialogTextField?.setTextColor(mColorCode)
        binding?.apEditorAddTextDialogTextField?.typeface =
            ResourcesCompat.getFont(requireActivity(), R.font.ap_galahad_camera_bold)
        mInputMethodManager?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

        // Make a callback on activity when user is done with text editing
        binding?.apEditorAddTextDialogDoneTextLabel?.setOnClickListener { onClickListenerView ->
            mInputMethodManager?.hideSoftInputFromWindow(onClickListenerView.windowToken, 0)
            dismiss()
            val inputText = binding?.apEditorAddTextDialogTextField?.text.toString()
            if (!TextUtils.isEmpty(inputText) && mTextEditorListener != null) {
                mTextEditorListener?.onDone(inputText, mColorCode)
            }
        }
    }

    // Callback to listener if user is done with text editing
    fun setOnTextEditorListener(textEditorListener: TextEditorListener) {
        mTextEditorListener = textEditorListener
    }

    companion object {
        private val TAG: String = ApEditorAddTextEditorFragment::class.java.simpleName
        const val EXTRA_INPUT_TEXT = "extra_input_text"
        const val EXTRA_COLOR_CODE = "extra_color_code"

        // Show dialog with provide text and text color
        // Show dialog with default text input as empty and text color white
        @JvmOverloads
        fun show(
            appCompatActivity: AppCompatActivity,
            inputText: String = "",
            @ColorInt colorCode: Int = ContextCompat.getColor(appCompatActivity, R.color.white)
        ): ApEditorAddTextEditorFragment {
            val args = Bundle()
            args.putString(EXTRA_INPUT_TEXT, inputText)
            args.putInt(EXTRA_COLOR_CODE, colorCode)
            val fragment = ApEditorAddTextEditorFragment()
            fragment.arguments = args
            fragment.show(appCompatActivity.supportFragmentManager, TAG)
            return fragment
        }
    }
}
