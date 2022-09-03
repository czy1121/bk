package me.reezy.cosmo.bk

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.core.graphics.ColorUtils
import me.reezy.cosmo.R
import java.util.*
import kotlin.math.hypot
import kotlin.math.min


// 支持 背景色[正常/选中/按下/禁用]
// 支持 渐变[类型/半径/方向/中心点XY坐标(0.0~1.0)/颜色(开始-中心-结束)]
// 支持 圆角[半径，位置]，根据宽高自动计算圆角半径 min(height,width) / 2
// 支持 阴影[颜色(默认跟随背景色)/半径/边距(容纳阴影的空间)]
// 支持 描边[颜色/厚度/虚线长度/虚线间隙]
class BKDrawable(context: Context, attrs: AttributeSet? = null) : Drawable() {


    private var mBackgroundColors: ColorStateList? = null

    private var mStrokeWidth = 0f
    private var mStrokeColors: ColorStateList? = null
    private var mStrokeDashWidth = 0f
    private var mStrokeDashGap = 0f
    private var mStrokePaint: Paint? = null

    private var mIsAutoCornerRadius: Boolean = false
    private var mCornerPosition: Int = 0
    private var mCornerRadius: Float = 0f
    private var mCornerRadiusArray: FloatArray? = null

    private val mFillPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val mClipPaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        }
    }

    private var mIsRectDirty = true
    private var mIsPathDirty = true
    private var mIsClipDirty = true
    private var mIsGradientDirty = true
    private var mIsCornerDirty = true

    private val mRect = RectF()
    private val mPath = Path()

    private val mClipRect = RectF()
    private val mClipPath = Path()
    private val mTempPath = Path()

    private val mShadowPadding: Rect = Rect()
    private val mContentPadding: Rect = Rect()

    private var mAlpha: Int = 0xff

    private val mShadowRenderer = ShadowRenderer()
    private val mGradientState = GradientState()


    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.Background)

        // 状态背景色
        mBackgroundColors = a.getColorStateList(
            R.styleable.Background_bkBackgroundColor,
            R.styleable.Background_bkBackgroundColorSelected,
            R.styleable.Background_bkBackgroundColorPressed,
            R.styleable.Background_bkBackgroundColorDisabled,
        )


        // 渐变背景色
        mGradientState.gradientColors = a.getGradientColors()
        mGradientState.gradientType = a.getInt(R.styleable.Background_bkGradientType, GradientDrawable.LINEAR_GRADIENT)
        mGradientState.gradientRadius = a.getDimension(R.styleable.Background_bkGradientRadius, 0f)
        mGradientState.gradientCenterX = a.getFloat(R.styleable.Background_bkGradientCenterX, 0.5f)
        mGradientState.gradientCenterY = a.getFloat(R.styleable.Background_bkGradientCenterY, 0.5f)
        mGradientState.gradientOrientation = try {
            GradientDrawable.Orientation.values()[a.getInt(R.styleable.Background_bkGradientOrientation, 0)]
        } catch (e: IndexOutOfBoundsException) {
            GradientDrawable.Orientation.TOP_BOTTOM
        }


        // 圆角
        val radius = a.getLayoutDimension(R.styleable.Background_bkCornerRadius, 0)
        mIsAutoCornerRadius = radius < 0
        mCornerRadius = if (radius < 0) 0f else radius.toFloat()
        mCornerPosition = a.getInt(R.styleable.Background_bkCornerPosition, CORNERS_NORMAL)

        // 描边
        mStrokeColors = a.getColorStateList(
            R.styleable.Background_bkStrokeColor,
            R.styleable.Background_bkStrokeColorSelected,
            R.styleable.Background_bkStrokeColorPressed,
            R.styleable.Background_bkStrokeColorDisabled,
        )
        mStrokeWidth = a.getDimension(R.styleable.Background_bkStrokeWidth, 0f)
        mStrokeDashWidth = a.getDimension(R.styleable.Background_bkStrokeDashWidth, 0f)
        mStrokeDashGap = a.getDimension(R.styleable.Background_bkStrokeDashGap, 0f)

        // 阴影
        mShadowRenderer.shadowColor = a.getColor(R.styleable.Background_bkShadowColor, Color.TRANSPARENT)
        mShadowRenderer.shadowRadius = a.getDimension(R.styleable.Background_bkShadowRadius, 0f)

        val shadowPadding = a.getDimensionPixelSize(R.styleable.Background_bkShadowPadding, mShadowRenderer.shadowRadius.toInt())
        mShadowPadding.left = a.getDimensionPixelSize(R.styleable.Background_bkShadowPaddingLeft, shadowPadding)
        mShadowPadding.top = a.getDimensionPixelSize(R.styleable.Background_bkShadowPaddingTop, shadowPadding)
        mShadowPadding.right = a.getDimensionPixelSize(R.styleable.Background_bkShadowPaddingRight, shadowPadding)
        mShadowPadding.bottom = a.getDimensionPixelSize(R.styleable.Background_bkShadowPaddingBottom, shadowPadding)

        val contentPadding = a.getDimensionPixelSize(R.styleable.Background_bkContentPadding, 0)
        mContentPadding.left = a.getDimensionPixelSize(R.styleable.Background_bkContentPaddingLeft, contentPadding)
        mContentPadding.top = a.getDimensionPixelSize(R.styleable.Background_bkContentPaddingTop, contentPadding)
        mContentPadding.right = a.getDimensionPixelSize(R.styleable.Background_bkContentPaddingRight, contentPadding)
        mContentPadding.bottom = a.getDimensionPixelSize(R.styleable.Background_bkContentPaddingBottom, contentPadding)
        a.recycle()


        updateBackgroundColor()

        if (mStrokeWidth > 0f && mStrokeColors != null) {
            updateStroke()
        }
    }

    var isAutoCornerRadius: Boolean
        get() = mIsAutoCornerRadius
        set(value) {
            if (mIsAutoCornerRadius == value) return
            mIsAutoCornerRadius = value
            mIsCornerDirty = true
            invalidateSelf()
        }
    var cornerRadius: Float
        get() = mCornerRadius
        set(value) {
            if (mCornerRadius == value) return
            mCornerRadius = value
            mIsCornerDirty = true
            invalidateSelf()
        }
    var cornerPosition: Int
        get() = mCornerPosition
        set(value) {
            if (mCornerPosition == value) return
            mCornerPosition = value
            mIsCornerDirty = true
            invalidateSelf()
        }


    var backgroundColor: ColorStateList?
        get() = mBackgroundColors
        set(value) {
            if (mBackgroundColors == value) return
            mBackgroundColors = value
            updateBackgroundColor()
        }

    var gradientColors: IntArray?
        get() = mGradientState.gradientColors
        set(value) {
            if (Arrays.equals(mGradientState.gradientColors, value)) return
            mGradientState.gradientColors = if (value == null || value.size < 2) null else value
            mIsGradientDirty = true
            invalidateSelf()
        }
    var gradientType: Int
        get() = mGradientState.gradientType
        set(value) {
            if (mGradientState.gradientType == value) return
            mGradientState.gradientType = value
            mIsGradientDirty = true
            invalidateSelf()
        }
    var gradientOrientation: GradientDrawable.Orientation
        get() = mGradientState.gradientOrientation
        set(value) {
            if (mGradientState.gradientOrientation == value) return
            mGradientState.gradientOrientation = value
            mIsGradientDirty = true
            invalidateSelf()
        }
    var gradientRadius: Float
        get() = mGradientState.gradientRadius
        set(value) {
            if (mGradientState.gradientRadius == value) return
            mGradientState.gradientRadius = value
            mIsGradientDirty = true
            invalidateSelf()
        }
    var gradientCenterX: Float
        get() = mGradientState.gradientCenterX
        set(value) {
            if (mGradientState.gradientCenterX == value) return
            mGradientState.gradientCenterX = value
            mIsGradientDirty = true
            invalidateSelf()
        }
    var gradientCenterY: Float
        get() = mGradientState.gradientCenterY
        set(value) {
            if (mGradientState.gradientCenterY == value) return
            mGradientState.gradientCenterY = value
            mIsGradientDirty = true
            invalidateSelf()
        }

    fun setGradientColors(colors: String?) {
        mGradientState.gradientColors = parseColors(colors)
        mIsGradientDirty = true
        invalidateSelf()
    }

    fun setGradientCenter(x: Float, y: Float) {
        mGradientState.gradientCenterX = x
        mGradientState.gradientCenterY = y
        mIsGradientDirty = true
        invalidateSelf()
    }

    var isAutoShadowColor: Boolean
        get() = mShadowRenderer.isAuto
        set(value) {
            if (mShadowRenderer.isAuto == value) return
            mShadowRenderer.isAuto = value
            mShadowRenderer.update()
            invalidateSelf()
        }

    var shadowColor: Int
        get() = mShadowRenderer.shadowColor
        set(value) {
            if (mShadowRenderer.shadowColor == value) return
            mShadowRenderer.shadowColor = value
            mShadowRenderer.update()
            invalidateSelf()
        }
    var shadowRadius: Float
        get() = mShadowRenderer.shadowRadius
        set(value) {
            if (mShadowRenderer.shadowRadius == value) return
            mShadowRenderer.shadowRadius = value
            invalidateSelf()
        }
    var shadowPadding: Rect
        get() = Rect(mShadowPadding)
        set(value) {
            if (mShadowPadding == value) return
            mShadowPadding.set(value)
            mIsRectDirty = true
            invalidateSelf()
        }

    var strokeWidth: Float
        get() = mStrokeWidth
        set(value) {
            if (mStrokeWidth == value) return
            mStrokeWidth = value
            updateStroke()
        }
    var strokeColor: ColorStateList?
        get() = mStrokeColors
        set(value) {
            if (mStrokeColors == value) return
            mStrokeColors = value
            updateStroke()
        }

    fun setStroke(width: Float, colors: ColorStateList) {
        mStrokeWidth = width
        mStrokeColors = colors
        updateStroke()
    }

    fun setStrokeDash(dashWidth: Float, dashGap: Float) {
        mStrokeDashWidth = dashWidth
        mStrokeDashGap = dashGap
        updateStroke()
    }


    fun clip(canvas: Canvas) {
        if (mIsClipDirty) {
            mClipPath.reset()
            mTempPath.reset()

            if (mCornerRadiusArray != null) {
                mTempPath.addRoundRect(mClipRect, mCornerRadiusArray!!, Path.Direction.CW)
            } else {
                mTempPath.addRoundRect(mClipRect, mCornerRadius, mCornerRadius, Path.Direction.CW)
            }

            mClipPath.addRect(RectF(bounds), Path.Direction.CW)
            mClipPath.op(mTempPath, Path.Op.DIFFERENCE)
            mIsClipDirty = false
        }
        canvas.drawPath(mClipPath, mClipPaint)
    }

    override fun draw(canvas: Canvas) {

        buildRectIfDirty()

        if (mRect.isEmpty) {
            return
        }
        buildCornerIfDirty()
        buildGradientIfDirty()

        val haveFill: Boolean = mFillPaint.alpha > 0 || mFillPaint.shader != null
        val haveStroke = mStrokeWidth > 0 && mStrokePaint != null && mStrokePaint!!.alpha > 0

        if (mCornerRadiusArray != null) {

            buildPathIfDirty()
            mCornerRadiusArray?.let {
                mShadowRenderer.draw(canvas, mRect, it[0], it[2], it[4], it[6])
            }

            if (haveFill) {
                canvas.drawPath(mPath, mFillPaint)
            }
            if (haveStroke) {
                canvas.drawPath(mPath, mStrokePaint!!)
            }
        } else if (mCornerRadius > 0.0f) {
            val rad: Float = min(mCornerRadius, min(mRect.width(), mRect.height()) * 0.5f)

            mShadowRenderer.draw(canvas, mRect, rad, rad, rad, rad)

            if (haveFill) {
                canvas.drawRoundRect(mRect, rad, rad, mFillPaint)
            }

            if (haveStroke) {
                canvas.drawRoundRect(mRect, rad, rad, mStrokePaint!!)
            }
        } else {
            mShadowRenderer.draw(canvas, mRect, 0f, 0f, 0f, 0f)

            if (haveFill) {
                canvas.drawRect(mRect, mFillPaint)
            }

            if (haveStroke) {
                canvas.drawRect(mRect, mStrokePaint!!)
            }
        }
    }

    override fun getPadding(padding: Rect): Boolean {
        val sp = mShadowPadding
        val cp = mContentPadding
        val halfStrokeWidth = (mStrokeWidth * 0.5f).toInt()
        val hasPadding = sp.left or sp.right or sp.top or sp.bottom != 0

        if (hasPadding) {
            padding.left = sp.left + cp.left + halfStrokeWidth
            padding.right = sp.right + cp.right + halfStrokeWidth
            padding.top = sp.top + cp.top + halfStrokeWidth
            padding.bottom = sp.bottom + cp.bottom + halfStrokeWidth
        } else {
            padding.set(0, 0, 0, 0)
        }
        return hasPadding
    }

    override fun onBoundsChange(bounds: Rect) {
        mIsRectDirty = true
        mIsPathDirty = true
        mIsClipDirty = true
    }


    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    override fun getAlpha(): Int {
        return mAlpha
    }

    override fun setAlpha(alpha: Int) {
        if (alpha != mAlpha) {
            mAlpha = alpha
            invalidateSelf()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = if (mAlpha == 255) PixelFormat.OPAQUE else PixelFormat.TRANSLUCENT

    override fun onStateChange(stateSet: IntArray?): Boolean {
        var invalidateSelf = false
        mBackgroundColors?.let {
            val newColor = it.getColorForState(stateSet, it.defaultColor)
            val oldColor = mFillPaint.color
            if (oldColor != newColor) {
                mFillPaint.color = newColor
                mShadowRenderer.autoColor = newColor
                mShadowRenderer.update()
                invalidateSelf = true
            }
        }
        if (mStrokeWidth > 0f) {
            mStrokeColors?.let {
                val newColor = it.getColorForState(stateSet, it.defaultColor)
                val oldColor = mStrokePaint?.color
                if (oldColor != newColor) {
                    mStrokePaint?.color = newColor
                    invalidateSelf = true
                }
            }
        }
        if (invalidateSelf) {
            invalidateSelf()
            return true
        }
        return false
    }

    override fun isStateful(): Boolean {
        return (mBackgroundColors?.isStateful ?: false || mStrokeColors?.isStateful ?: false)
    }


    private fun buildRectIfDirty() {
        if (mIsRectDirty) {
            val halfStrokeWidth = mStrokeWidth * 0.5f
            val sp = mShadowPadding

            mRect.set(
                bounds.left + sp.left + halfStrokeWidth,
                bounds.top + sp.top + halfStrokeWidth,
                bounds.right - sp.right - halfStrokeWidth,
                bounds.bottom - sp.bottom - halfStrokeWidth
            )

            mClipRect.set(
                bounds.left + sp.left + mStrokeWidth,
                bounds.top + sp.top + mStrokeWidth,
                bounds.right - sp.right - mStrokeWidth,
                bounds.bottom - sp.bottom - mStrokeWidth
            )
            mIsCornerDirty = true

            mIsRectDirty = false
        }
    }

    private fun buildCornerIfDirty() {
        if (mIsCornerDirty) {
            if (mIsAutoCornerRadius) {
                updateCornerRadius(mRect, mCornerPosition, min(mRect.width(), mRect.height()) / 2f)
            } else {
                updateCornerRadius(mRect, mCornerPosition, mCornerRadius)
            }
            mIsCornerDirty = false
        }
    }

    private fun buildGradientIfDirty() {
        if (mIsGradientDirty) {
            mFillPaint.shader = mGradientState.createFillShader(mRect)
            mIsGradientDirty = false
        }
    }

    private fun buildPathIfDirty() {
        if (mIsPathDirty) {
            mPath.reset()
            mPath.addRoundRect(mRect, mCornerRadiusArray!!, Path.Direction.CW)
            mIsPathDirty = false
        }
    }

    private fun updateBackgroundColor() {
        mFillPaint.color = mBackgroundColors?.getColorForState(state, Color.TRANSPARENT) ?: Color.TRANSPARENT
        mShadowRenderer.autoColor = mFillPaint.color
        mShadowRenderer.update()
        invalidateSelf()
    }

    private fun updateStroke() {
        if (mStrokePaint == null) {
            mStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            mStrokePaint?.style = Paint.Style.STROKE
        }
        mStrokePaint?.let {
            it.color = mStrokeColors?.getColorForState(state, Color.TRANSPARENT) ?: Color.TRANSPARENT
            it.strokeWidth = mStrokeWidth
            it.pathEffect = if (mStrokeDashWidth > 0.0f) DashPathEffect(floatArrayOf(mStrokeDashWidth, mStrokeDashGap), 0f) else null
            invalidateSelf()
        }
    }

    private fun updateCornerRadius(b: RectF, cornerPosition: Int, radius: Float) {
        val r = when (cornerPosition) {
            CORNERS_NORMAL -> radius
            CORNERS_TOP_LEFT, CORNERS_TOP_RIGHT, CORNERS_BOTTOM_LEFT, CORNERS_BOTTOM_RIGHT -> min(radius, min(b.width(), b.height()))
            CORNERS_SIDE_LEFT, CORNERS_SIDE_RIGHT -> min(radius, min(b.width(), b.height() * 0.5f))
            CORNERS_SIDE_TOP, CORNERS_SIDE_BOTTOM -> min(radius, min(b.width() * 0.5f, b.height()))
            CORNERS_DIAGONAL_DOWNWARD, CORNERS_DIAGONAL_UPWARD -> min(radius, min(b.width(), b.height()))
            else -> radius
        }
        mCornerRadius = r
        mCornerRadiusArray = when (cornerPosition) {
            CORNERS_NORMAL -> null
            CORNERS_TOP_LEFT -> floatArrayOf(r, r, 0f, 0f, 0f, 0f, 0f, 0f)
            CORNERS_TOP_RIGHT -> floatArrayOf(0f, 0f, r, r, 0f, 0f, 0f, 0f)
            CORNERS_BOTTOM_LEFT -> floatArrayOf(0f, 0f, 0f, 0f, r, r, 0f, 0f)
            CORNERS_BOTTOM_RIGHT -> floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, r, r)
            CORNERS_SIDE_LEFT -> floatArrayOf(r, r, 0f, 0f, 0f, 0f, r, r)
            CORNERS_SIDE_RIGHT -> floatArrayOf(0f, 0f, r, r, r, r, 0f, 0f)
            CORNERS_SIDE_TOP -> floatArrayOf(r, r, r, r, 0f, 0f, 0f, 0f)
            CORNERS_SIDE_BOTTOM -> floatArrayOf(0f, 0f, 0f, 0f, r, r, r, r)
            CORNERS_DIAGONAL_DOWNWARD -> floatArrayOf(r, r, 0f, 0f, r, r, 0f, 0f)
            CORNERS_DIAGONAL_UPWARD -> floatArrayOf(0f, 0f, r, r, 0f, 0f, r, r)
            else -> null
        }
        mIsPathDirty = true
        mIsClipDirty = true
    }

    private fun TypedArray.getColorStateList(default: Int = -1, selected: Int = -1, pressed: Int = -1, disabled: Int = -1): ColorStateList? {
        val color = if (default >= 0 && hasValue(default)) getColorStateList(default) else null
        if (color?.isStateful == true) {
            return color
        }

        return bkColorStateList(
            color?.defaultColor,
            if (selected >= 0 && hasValue(selected)) getColor(selected, Color.TRANSPARENT) else null,
            if (pressed >= 0 && hasValue(pressed)) getColor(pressed, Color.TRANSPARENT) else null,
            if (disabled >= 0 && hasValue(disabled)) getColor(disabled, Color.TRANSPARENT) else null
        )
    }


    private fun TypedArray.getGradientColors(): IntArray? {
        var colors = parseColors(getString(R.styleable.Background_bkGradientColors))

        if (colors == null) {
            val hasStartColor = hasValue(R.styleable.Background_bkGradientStartColor)
            val hasCenterColor = hasValue(R.styleable.Background_bkGradientCenterColor)
            val hasEndColor = hasValue(R.styleable.Background_bkGradientEndColor)

            if (hasStartColor || hasCenterColor || hasEndColor) {
                val start = getColor(R.styleable.Background_bkGradientStartColor, 0x000000)
                val center = getColor(R.styleable.Background_bkGradientCenterColor, 0x000000)
                val end = getColor(R.styleable.Background_bkGradientEndColor, 0x000000)
                colors = if (hasCenterColor) intArrayOf(start, center, end) else intArrayOf(start, end)
            }
        }
        return colors
    }

    private fun parseColors(colors: String?): IntArray? {
        if (colors.isNullOrEmpty()) return null
        return try {
            colors.replace(" ", "").split(",").dropWhile { it.isEmpty() }.map { Color.parseColor(it) }.toIntArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


//    // 灰度
//    private fun greyer(color: Int): Int {
//        val blue = color and 0x000000FF shr 0
//        val green = color and 0x0000FF00 shr 8
//        val red = color and 0x00FF0000 shr 16
//        val grey = (red * 0.299f + green * 0.587f + blue * 0.114f).roundToInt()
//        return Color.argb(0xff, grey, grey, grey)
//    }
//
//    // 明度
//    private fun darker(color: Int, ratio: Float): Int {
//        val nowColor = if (color shr 24 == 0) 0x22808080 else color
//        val hsv = FloatArray(3)
//        Color.colorToHSV(nowColor, hsv)
//        hsv[2] *= ratio
//        return Color.HSVToColor(nowColor shr 24, hsv)
//    }

    companion object {

        const val CORNERS_NORMAL = 0
        const val CORNERS_TOP_LEFT = 1
        const val CORNERS_TOP_RIGHT = 2
        const val CORNERS_BOTTOM_LEFT = 3
        const val CORNERS_BOTTOM_RIGHT = 4
        const val CORNERS_SIDE_LEFT = 5
        const val CORNERS_SIDE_RIGHT = 6
        const val CORNERS_SIDE_TOP = 7
        const val CORNERS_SIDE_BOTTOM = 8
        const val CORNERS_DIAGONAL_DOWNWARD = 9
        const val CORNERS_DIAGONAL_UPWARD = 10

    }

    private class GradientState {

        var gradientColors: IntArray? = null
        var gradientType: Int = GradientDrawable.LINEAR_GRADIENT
        var gradientOrientation: GradientDrawable.Orientation = GradientDrawable.Orientation.TL_BR
        var gradientCenterX: Float = 0.5f
        var gradientCenterY: Float = 0.5f
        var gradientRadius: Float = 0.5f

        fun createFillShader(rect: RectF): Shader? = when {
            gradientColors == null -> null
            gradientType == GradientDrawable.LINEAR_GRADIENT -> {
                val r = when (gradientOrientation) {
                    GradientDrawable.Orientation.TOP_BOTTOM -> RectF(rect.left, rect.top, rect.left, rect.bottom)
                    GradientDrawable.Orientation.TR_BL -> RectF(rect.right, rect.top, rect.left, rect.bottom)
                    GradientDrawable.Orientation.RIGHT_LEFT -> RectF(rect.right, rect.top, rect.left, rect.top)
                    GradientDrawable.Orientation.BR_TL -> RectF(rect.right, rect.bottom, rect.left, rect.top)
                    GradientDrawable.Orientation.BOTTOM_TOP -> RectF(rect.left, rect.bottom, rect.left, rect.top)
                    GradientDrawable.Orientation.BL_TR -> RectF(rect.left, rect.bottom, rect.right, rect.top)
                    GradientDrawable.Orientation.LEFT_RIGHT -> RectF(rect.left, rect.top, rect.right, rect.top)
                    else -> RectF(rect.left, rect.top, rect.right, rect.bottom)
                }
                LinearGradient(r.left, r.top, r.right, r.bottom, gradientColors!!, null, Shader.TileMode.CLAMP)
            }
            gradientType == GradientDrawable.RADIAL_GRADIENT -> {
                if (gradientRadius > 0) {
                    val x0 = rect.left + (rect.right - rect.left) * gradientCenterX
                    val y0 = rect.top + (rect.bottom - rect.top) * gradientCenterY
                    RadialGradient(x0, y0, gradientRadius, gradientColors!!, null, Shader.TileMode.CLAMP)
                } else {
                    null
                }
            }
            gradientType == GradientDrawable.SWEEP_GRADIENT -> {
                val x0 = rect.left + (rect.right - rect.left) * gradientCenterX
                val y0 = rect.top + (rect.bottom - rect.top) * gradientCenterY
                SweepGradient(x0, y0, gradientColors!!, null)
            }
            else -> null
        }

    }

    private class ShadowRenderer {
        private val cornerShadowPaint: Paint = Paint(Paint.DITHER_FLAG or Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        private val edgeShadowPaint: Paint = Paint(cornerShadowPaint)

        private var shadowStartColor = 0
        private var shadowMiddleColor = 0
        private var shadowEndColor = 0


        var isAuto: Boolean = true
        var shadowRadius: Float = 0f
        var shadowColor: Int = Color.TRANSPARENT
        var autoColor: Int = Color.TRANSPARENT

        fun update() {
            val value = if (isAuto) autoColor else shadowColor
            shadowStartColor = ColorUtils.setAlphaComponent(value, COLOR_ALPHA_START)
            shadowMiddleColor = ColorUtils.setAlphaComponent(value, COLOR_ALPHA_MIDDLE)
            shadowEndColor = ColorUtils.setAlphaComponent(value, COLOR_ALPHA_END)
        }

        fun draw(canvas: Canvas, b: RectF, tl: Float, tr: Float, br: Float, bl: Float) {

            if (shadowRadius <= 0) return

            drawCornerShadow(canvas, b.left + tl, b.top + tl, tl, shadowRadius, 180f)
            drawCornerShadow(canvas, b.left + bl, b.bottom - bl, bl, shadowRadius, 90f)
            drawCornerShadow(canvas, b.right - tr, b.top + tr, tr, shadowRadius, 270f)
            drawCornerShadow(canvas, b.right - br, b.bottom - br, br, shadowRadius, 0f)

            drawEdgeShadow(canvas, b.left + tl, b.top, b.right - tr, b.top, shadowRadius, 0f)
            drawEdgeShadow(canvas, b.left, b.bottom - bl, b.left, b.top + tl, shadowRadius, 270f)
            drawEdgeShadow(canvas, b.right - br, b.bottom, b.left + bl, b.bottom, shadowRadius, 180f)
            drawEdgeShadow(canvas, b.right, b.top + tr, b.right, b.bottom - br, shadowRadius, 90f)
        }

        private fun drawEdgeShadow(canvas: Canvas, x0: Float, y0: Float, x1: Float, y1: Float, elevation: Float, angle: Float) {
            val bounds = RectF(0f, -elevation, hypot(x1 - x0, y1 - y0), 0f)

            edgeColors[0] = shadowEndColor
            edgeColors[1] = shadowMiddleColor
            edgeColors[2] = shadowStartColor
            edgeShadowPaint.shader = LinearGradient(bounds.left, bounds.top, bounds.left, bounds.bottom, edgeColors, edgePositions, Shader.TileMode.CLAMP)
            canvas.save()
            canvas.translate(x0, y0)
            canvas.rotate(angle)
            canvas.drawRect(bounds, edgeShadowPaint)
            canvas.restore()
        }


        private fun drawCornerShadow(canvas: Canvas, centerX: Float, centerY: Float, cornerRadius: Float, elevation: Float, startAngle: Float) {

            val radius = cornerRadius + elevation

            cornerColors[0] = 0
            cornerColors[1] = 0
            cornerColors[2] = shadowStartColor
            cornerColors[3] = shadowMiddleColor
            cornerColors[4] = shadowEndColor
            val startRatio = 1f - elevation / radius
            val middleRatio = startRatio + (1f - startRatio) / 2f
            cornerPositions[1] = startRatio
            cornerPositions[2] = startRatio
            cornerPositions[3] = middleRatio
            cornerShadowPaint.shader = RadialGradient(centerX, centerY, radius, cornerColors, cornerPositions, Shader.TileMode.CLAMP)

            canvas.save()
            canvas.drawArc(RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius), startAngle, 90f, true, cornerShadowPaint)
            canvas.restore()
        }

        companion object {
            private const val COLOR_ALPHA_START = 0x44
            private const val COLOR_ALPHA_MIDDLE = 0x14
            private const val COLOR_ALPHA_END = 0

            private val edgeColors = IntArray(3)
            private val edgePositions = floatArrayOf(0f, .5f, 1f)

            private val cornerColors = IntArray(5)
            private val cornerPositions = floatArrayOf(0f, 0f, 0f, .5f, 1f)
        }
    }
}