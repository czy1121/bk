package me.reezy.cosmo.bk

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.LinearLayoutCompat
import me.reezy.cosmo.bk.BKDrawable


class BKLinearLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    LinearLayoutCompat(context, attrs, defStyle) {


    val bk: BKDrawable = BKDrawable(context, attrs)

    init {
        background = bk
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.saveLayer(RectF(bk.bounds), null)
        super.dispatchDraw(canvas)
        bk.clip(canvas)
        canvas.restore()
    }
}