package com.bruce.kotlin.readhub.adapter

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * @author yang.jianan
 * @date 2018/01/08 17:13.
 */

class SpaceItemDecoration(internal var mSpace: Int) : RecyclerView.ItemDecoration() {

    /**
     * Retrieve any offsets for the given item. Each field of `outRect` specifies
     * the number of pixels that the item view should be inset by, similar to padding or margin.
     * The default implementation sets the bounds of outRect to 0 and returns.
     *
     *
     *
     *
     * If this ItemDecoration does not affect the positioning of item views, it should set
     * all four fields of `outRect` (left, top, right, bottom) to zero
     * before returning.
     *
     *
     *
     *
     * If you need to access Adapter for additional data, you can call
     * [RecyclerView.getChildAdapterPosition] to get the adapter position of the
     * View.
     *
     * @param outRect Rect to receive the output.
     * @param view    The child view to decorate
     * @param parent  RecyclerView this ItemDecoration is decorating
     * @param state   The current state of RecyclerView.
     */
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.left = mSpace
        outRect.right = mSpace
        outRect.bottom = mSpace
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = mSpace
        }

    }
}
