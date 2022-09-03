package me.reezy.cosmo.bk

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.Layout
import android.util.AttributeSet
import android.view.Gravity
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.TextViewCompat
import me.reezy.cosmo.R

class BKTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : AppCompatTextView(context, attrs, defStyle) {
    companion object {

        const val ICON_GRAVITY_START = 0x1
        const val ICON_GRAVITY_TEXT_START = 0x2
        const val ICON_GRAVITY_END = 0x3
        const val ICON_GRAVITY_TEXT_END = 0x4
        const val ICON_GRAVITY_TOP = 0x10
        const val ICON_GRAVITY_TEXT_TOP = 0x20
    }

    @IntDef(
        ICON_GRAVITY_START,
        ICON_GRAVITY_TEXT_START,
        ICON_GRAVITY_END,
        ICON_GRAVITY_TEXT_END,
        ICON_GRAVITY_TOP,
        ICON_GRAVITY_TEXT_TOP
    )
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class IconGravity


    private var mIcon: Drawable? = null
    private var mIconSize: Int = 0
    private var mIconPadding: Int = 0
    private var mIconGravity: Int = 0
    private var mIconTintMode: PorterDuff.Mode? = null
    private var mIconTint: ColorStateList? = null

    private var iconTop: Int = 0
    private var iconLeft: Int = 0

    val bk = BKDrawable(context, attrs)

    init {
        super.setBackgroundDrawable(bk)

        val a = context.obtainStyledAttributes(attrs, R.styleable.BKTextView)
        mIcon = a.getDrawable(R.styleable.BKTextView_bkIcon)
        mIconSize = a.getDimensionPixelSize(R.styleable.BKTextView_bkIconSize, 0)
        mIconPadding = a.getDimensionPixelSize(R.styleable.BKTextView_bkIconPadding, 0)
        mIconGravity = a.getInteger(R.styleable.BKTextView_bkIconGravity, ICON_GRAVITY_START)
        mIconTintMode = parseTintMode(a.getInt(R.styleable.BKTextView_bkIconTintMode, -1)) ?: PorterDuff.Mode.SRC_IN
        mIconTint = a.getColorStateList(R.styleable.BKTextView_bkIconTint)
        a.recycle()

        compoundDrawablePadding = mIconPadding
        updateIcon(mIcon != null)
    }

    @IconGravity
    var iconGravity: Int
        get() = mIconGravity
        set(value) {
            if (mIconGravity != value) {
                mIconGravity = value
                updateIconPosition(measuredWidth, measuredHeight)
            }
        }
    var iconPadding: Int
        get() = mIconPadding
        set(value) {
            if (mIconPadding != value) {
                mIconPadding = value
                compoundDrawablePadding = value
            }
        }
    var iconSize: Int
        get() = mIconSize
        set(value) {
            require(value >= 0) { "iconSize cannot be less than 0" }
            if (mIconSize != value) {
                mIconSize = value
                updateIcon(true)
            }
        }
    var icon: Drawable?
        get() = mIcon
        set(value) {
            if (mIcon != value) {
                mIcon = value
                updateIcon(true)
                updateIconPosition(measuredWidth, measuredHeight)
            }
        }
    var iconTint: ColorStateList?
        get() = mIconTint
        set(value) {
            if (mIconTint != value) {
                mIconTint = value
                updateIcon(false)
            }
        }
    var iconTintMode: PorterDuff.Mode?
        get() = mIconTintMode
        set(value) {
            if (mIconTintMode != value) {
                mIconTintMode = value
                updateIcon(false)
            }
        }

    fun setIconResource(@DrawableRes resId: Int) {
        icon = if (resId > 0) AppCompatResources.getDrawable(context, resId) else null
    }
    fun setIconTintResource(@ColorRes iconTintResourceId: Int) {
        iconTint = AppCompatResources.getColorStateList(context, iconTintResourceId)
    }


    override fun setBackground(background: Drawable) {

    }

    @Deprecated("Deprecated in Java")
    override fun setBackgroundDrawable(background: Drawable?) {

    }

    override fun invalidateDrawable(drawable: Drawable) {
        super.invalidateDrawable(drawable)
        if (bk == drawable) {
            bk.state = drawableState
        }
    }

    override fun setTextAlignment(textAlignment: Int) {
        super.setTextAlignment(textAlignment)
        updateIconPosition(measuredWidth, measuredHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        updateIconPosition(measuredWidth, measuredHeight)
    }

    override fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
        super.onTextChanged(charSequence, i, i1, i2)
        updateIconPosition(measuredWidth, measuredHeight)
    }

    private fun getGravityTextAlignment(): Layout.Alignment {
        return when (gravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
            Gravity.CENTER_HORIZONTAL -> Layout.Alignment.ALIGN_CENTER
            Gravity.END, Gravity.RIGHT -> Layout.Alignment.ALIGN_OPPOSITE
            Gravity.START, Gravity.LEFT -> Layout.Alignment.ALIGN_NORMAL
            else -> Layout.Alignment.ALIGN_NORMAL
        }
    }

    private fun getActualTextAlignment(): Layout.Alignment {
        return when (textAlignment) {
            TEXT_ALIGNMENT_GRAVITY -> getGravityTextAlignment()
            TEXT_ALIGNMENT_CENTER -> Layout.Alignment.ALIGN_CENTER
            TEXT_ALIGNMENT_TEXT_END, TEXT_ALIGNMENT_VIEW_END -> Layout.Alignment.ALIGN_OPPOSITE
            TEXT_ALIGNMENT_TEXT_START, TEXT_ALIGNMENT_VIEW_START, TEXT_ALIGNMENT_INHERIT -> Layout.Alignment.ALIGN_NORMAL
            else -> Layout.Alignment.ALIGN_NORMAL
        }
    }

    private fun getTextWidth(): Int {
        val textPaint: Paint = paint
        var buttonText = text.toString()
        if (transformationMethod != null) {
            buttonText = transformationMethod.getTransformation(buttonText, this).toString()
        }
        return textPaint.measureText(buttonText).toInt().coerceAtMost(layout.ellipsizedWidth)
    }

    private fun getTextHeight(): Int {
        val textPaint: Paint = paint
        var buttonText = text.toString()
        if (transformationMethod != null) {
            buttonText = transformationMethod.getTransformation(buttonText, this).toString()
        }
        val bounds = Rect()
        textPaint.getTextBounds(buttonText, 0, buttonText.length, bounds)
        return bounds.height().coerceAtMost(layout.height)
    }

    private fun resetIconDrawable() {
        if (isIconStart()) {
            TextViewCompat.setCompoundDrawablesRelative(this, mIcon, null, null, null)
        } else if (isIconEnd()) {
            TextViewCompat.setCompoundDrawablesRelative(this, null, null, mIcon, null)
        } else if (isIconTop()) {
            TextViewCompat.setCompoundDrawablesRelative(this, null, mIcon, null, null)
        }
    }

    private fun isIconStart(): Boolean = mIconGravity == ICON_GRAVITY_START || mIconGravity == ICON_GRAVITY_TEXT_START
    private fun isIconEnd(): Boolean = mIconGravity == ICON_GRAVITY_END || mIconGravity == ICON_GRAVITY_TEXT_END
    private fun isIconTop(): Boolean = mIconGravity == ICON_GRAVITY_TOP || mIconGravity == ICON_GRAVITY_TEXT_TOP
    private fun isLayoutRTL(): Boolean = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL

    private fun updateIcon(needsIconReset: Boolean) {
        if (mIcon != null) {
            mIcon = DrawableCompat.wrap(mIcon!!).mutate().also {
                DrawableCompat.setTintList(it, mIconTint)
                if (mIconTintMode != null) {
                    DrawableCompat.setTintMode(it, mIconTintMode!!)
                }
                val width = if (mIconSize != 0) mIconSize else it.intrinsicWidth
                val height = if (mIconSize != 0) mIconSize else it.intrinsicHeight
                it.setBounds(iconLeft, iconTop, iconLeft + width, iconTop + height)
                it.setVisible(true, needsIconReset)
            }
        }

        if (needsIconReset) {
            resetIconDrawable()
            return
        }

        val (start, top, end) = TextViewCompat.getCompoundDrawablesRelative(this)
        val hasIconChanged = isIconStart() && start !== mIcon || isIconEnd() && end !== mIcon || isIconTop() && top !== mIcon
        if (hasIconChanged) {
            resetIconDrawable()
        }
    }

    private fun updateIconPosition(buttonWidth: Int, buttonHeight: Int) {
        if (mIcon == null || layout == null) {
            return
        }
        when(mIconGravity) {
            ICON_GRAVITY_START, ICON_GRAVITY_END, ICON_GRAVITY_TOP -> {
                iconTop = 0
                iconLeft = 0
                updateIcon(false)
            }
            ICON_GRAVITY_TEXT_TOP -> {
                iconLeft = 0
                val localIconSize: Int = if (mIconSize == 0) mIcon!!.intrinsicHeight else mIconSize
                val newIconTop: Int = (buttonHeight - getTextHeight() - paddingTop - localIconSize - mIconPadding - paddingBottom) / 2
                if (iconTop != newIconTop) {
                    iconTop = newIconTop
                    updateIcon(false)
                }
            }
            else -> {
                iconTop = 0
                val textAlignment: Layout.Alignment = getActualTextAlignment()
                if (mIconGravity == ICON_GRAVITY_TEXT_START && textAlignment == Layout.Alignment.ALIGN_NORMAL || mIconGravity == ICON_GRAVITY_TEXT_END && textAlignment == Layout.Alignment.ALIGN_OPPOSITE) {
                    iconLeft = 0
                    updateIcon(false)
                    return
                }
                val localIconSize: Int = if (mIconSize == 0) mIcon!!.intrinsicWidth else mIconSize
                val availableWidth: Int = (buttonWidth - getTextWidth() - ViewCompat.getPaddingEnd(this) - localIconSize - mIconPadding - ViewCompat.getPaddingStart(this))
                var newIconLeft = if (textAlignment == Layout.Alignment.ALIGN_CENTER) availableWidth / 2 else availableWidth

                if (isLayoutRTL() != (iconGravity == ICON_GRAVITY_TEXT_END)) {
                    newIconLeft = -newIconLeft
                }
                if (iconLeft != newIconLeft) {
                    iconLeft = newIconLeft
                    updateIcon(false)
                }
            }
        }
    }

    private fun parseTintMode(value: Int): PorterDuff.Mode? {
        return when (value) {
            3 -> PorterDuff.Mode.SRC_OVER
            5 -> PorterDuff.Mode.SRC_IN
            9 -> PorterDuff.Mode.SRC_ATOP
            14 -> PorterDuff.Mode.MULTIPLY
            15 -> PorterDuff.Mode.SCREEN
            16 -> PorterDuff.Mode.ADD
            else -> null
        }
    }

}