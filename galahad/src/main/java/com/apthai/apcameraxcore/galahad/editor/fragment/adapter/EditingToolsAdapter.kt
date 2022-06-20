package com.apthai.apcameraxcore.galahad.editor.fragment.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.apthai.apcameraxcore.galahad.R
import com.apthai.apcameraxcore.galahad.editor.tools.ToolType
import java.util.ArrayList

/**
 * @author [Burhanuddin Rashid](https://github.com/burhanrashid52)
 * @version 0.1.2
 * @since 5/23/2018
 */
class EditingToolsAdapter(private val context: Context, private val mOnItemSelected: OnItemSelected) :
    RecyclerView.Adapter<EditingToolsAdapter.ViewHolder>() {
    private val mToolList: MutableList<ToolModel> = ArrayList()

    interface OnItemSelected {
        fun onToolSelected(toolType: ToolType?)
    }

    internal inner class ToolModel(
        val mToolName: String,
        val mToolIcon: Int,
        val mToolType: ToolType
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ap_editor_tools_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mToolList[position]
        holder.txtTool.text = item.mToolName
        holder.imgToolIcon.setImageResource(item.mToolIcon)
    }

    override fun getItemCount(): Int {
        return mToolList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgToolIcon: ImageView = itemView.findViewById(R.id.ap_editor_item_main_tool_image_view)
        val txtTool: TextView = itemView.findViewById(R.id.ap_editor_item_main_tool_text_label)

        init {
            itemView.setOnClickListener { _: View? ->
                mOnItemSelected.onToolSelected(
                    mToolList[layoutPosition].mToolType
                )
            }
        }
    }

    init {
        mToolList.add(
            ToolModel(
                context.resources.getString(R.string.ap_editor_main_tools_shape_text_label), R.drawable.ic_ap_editor_tool_shape_oval,
                ToolType.SHAPE
            )
        )
        mToolList.add(
            ToolModel(
                context.resources.getString(R.string.ap_editor_main_tools_add_text_label), R.drawable.ic_ap_editor_tool_add_text,
                ToolType.TEXT
            )
        )
        mToolList.add(
            ToolModel(
                context.resources.getString(R.string.ap_editor_main_tools_eraser_text_label), R.drawable.ic_ap_editor_tool_eraser,
                ToolType.ERASER
            )
        )
        mToolList.add(
            ToolModel(
                context.resources.getString(R.string.ap_editor_main_tools_emoji_text_label), R.drawable.ic_ap_editor_tool_emoji,
                ToolType.EMOJI
            )
        )
        mToolList.add(
            ToolModel(
                context.resources.getString(R.string.ap_editor_main_tools_sticker_text_label), R.drawable.ic_ap_editor_tool_sticker,
                ToolType.STICKER
            )
        )
    }
}
