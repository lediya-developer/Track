package com.lediya.trackingapp.utility

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.lediya.trackingapp.R


class OtpEditText : AppCompatEditText, TextWatcher {
    private lateinit var mLinesPaint: Paint
    private lateinit var mStrokePaint: Paint
    private lateinit var mTextPaint: Paint
    private lateinit var mHintPaint: Paint
    private var defStyleAttr = 0
    private var mMaxLength = 4
    private var mPrimaryColor = 0
    private var mSecondaryColor = 0
    private var mTextColor = 0
    private var mHintTextColor = 0
    private var mLineStrokeSelected = 2f //2dp by default
    private var mLineStroke = 1f //1dp by default
    private var mSpace = 8f //24 dp by default, space between the lines
    private var mCharSize = 0f
    private var mNumChars = 6f
    private var mLineSpacing = 10f //8dp by default, height of the text from our lines
    private var mMaskCharacter = "*"
    private var mHintText: String? = ""
    lateinit var textWidths: FloatArray
    var hintWidth = FloatArray(1)

    constructor(context: Context?) : super(context!!) {}
    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        this.defStyleAttr = defStyleAttr
        init(context, attrs)
    }

    private fun init(
        context: Context,
        attrs: AttributeSet?
    ) {
        attrs?.let { getAttrsFromTypedArray(it) }
        mTextPaint = paint
        mTextPaint.color=mTextColor
        mHintPaint = Paint(paint)
        mHintPaint.color = mHintTextColor
        addTextChangedListener(this)
        val multi = context.resources.displayMetrics.density
        mLineStroke *= multi
        mLineStrokeSelected *= multi
        mLinesPaint = Paint(paint)
        mLinesPaint.strokeWidth = mLineStroke
        setBackgroundResource(0)
        mSpace *= multi //convert to pixels for our density
        mNumChars = mMaxLength.toFloat()
        //setC

        //Disable copy paste
        super.setCustomSelectionActionModeCallback(object : ActionMode.Callback {
            override fun onPrepareActionMode(
                mode: ActionMode,
                menu: Menu
            ): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) {}
            override fun onCreateActionMode(
                mode: ActionMode,
                menu: Menu
            ): Boolean {
                return false
            }

            override fun onActionItemClicked(
                mode: ActionMode,
                item: MenuItem
            ): Boolean {
                return false
            }
        })
        // When tapped, move cursor to end of text.
        super.setOnClickListener { v ->
            setSelection(text!!.length)
          //  mClickListener.onClick(v)
        }
    }

    private fun getAttrsFromTypedArray(attributeSet: AttributeSet) {
        val a = context.obtainStyledAttributes(
            attributeSet,
            R.styleable.OtpEditText,
            defStyleAttr,
            0
        )
        mMaxLength = attributeSet.getAttributeIntValue(
            XML_NAMESPACE_ANDROID,
            "maxLength",
            6
        )
        mHintText =
            attributeSet.getAttributeValue(XML_NAMESPACE_ANDROID, "hint")
        mHintTextColor = attributeSet.getAttributeIntValue(
            XML_NAMESPACE_ANDROID,
            "textColorHint",
            resources.getColor(R.color.white)
        )
        mPrimaryColor = a.getColor(
            R.styleable.OtpEditText_oev_primary_color,
            resources.getColor(R.color.button_default_selector_color)
        )
        mSecondaryColor = a.getColor(
            R.styleable.OtpEditText_oev_secondary_color,
            resources.getColor(R.color.button_default_selector_color)
        )
        mTextColor = a.getColor(
            R.styleable.OtpEditText_oev_text_color,
            resources.getColor(R.color.black)
        )
            a.getString(R.styleable.OtpEditText_oev_mask_character).toString().substring(0, 1)

        mStrokePaint = Paint(paint)
        mStrokePaint.strokeWidth = 8f
        mStrokePaint.style = Paint.Style.STROKE
        a.recycle()
    }

    val otpValue: String?
        get() = if (text.toString().length != mMaxLength) {
            triggerErrorAnimation()
            null
        } else {
            text.toString()
        }

    fun triggerErrorAnimation() {
        startAnimation(
            AnimationUtils.loadAnimation(
                context,
                R.anim.shake
            )
        )
    }

    override fun setCustomSelectionActionModeCallback(actionModeCallback: ActionMode.Callback) {
        throw RuntimeException("setCustomSelectionActionModeCallback() not supported.")
    }

    override fun onDraw(canvas: Canvas) {
        val availableWidth = width - paddingRight - paddingLeft
        mCharSize = if (mSpace < 0) {
            availableWidth / (mNumChars * 2 - 1)
        } else {
            (availableWidth - mSpace * (mNumChars - 1)) / mNumChars
        }
        mLineSpacing = (height * .6).toFloat()
        var startX:Float = paddingLeft.toFloat()
        var hintStartX:Float  = paddingLeft.toFloat()
        val bottom = height - paddingBottom
        val top = paddingTop

        //Text Width
        val text = text
        val textLength = text!!.length
      //  textWidths = FloatArray(textLength)
        textWidths = floatArrayOf(40F,40F,40F,40F)
        if (text.length == 0 && mHintText != null && !mHintText!!.isEmpty()) {
            paint.getTextWidths("1", 0, 1, hintWidth)
            var i = 0
            while (i < mNumChars && i < mHintText!!.length) {
                val middle = hintStartX + mCharSize / 2
                mHintText?.let {
                    canvas.drawText(
                        it,
                        i,
                        i + 1,
                        middle - hintWidth[0] / 2,
                        mLineSpacing,
                        mHintPaint
                    )
                }
                hintStartX += if (mSpace < 0) {
                    mCharSize * 2
                } else {
                    mCharSize + mSpace.toInt()
                }
                i++
            }
        }
        paint.getTextWidths(getText(), 0, textLength, textWidths)
        var i = 0
        while (i < mNumChars) {
            updateColorForLines(i <= textLength, i == textLength)
                    canvas.drawRect(
                        startX.toFloat(),
                        top.toFloat(),
                        startX + mCharSize,
                        bottom.toFloat(),
                        mLinesPaint
                    )
                    canvas.drawRect(
                        startX.toFloat(),
                        top.toFloat(),
                        startX + mCharSize,
                        bottom.toFloat(),
                        mStrokePaint
                    )

            if (getText()!!.length > i) {
                val middle = startX + mCharSize / 2
                    canvas.drawText(
                        text,
                        i,
                        i + 1,
                        middle - textWidths[0] / 2,
                        mLineSpacing,
                        mTextPaint
                    )
            }
            startX += if (mSpace < 0) {
                mCharSize * 2
            } else {
                mCharSize + mSpace.toInt()
            }
            i++
        }
    }

    private val maskText: String
        private get() {
            val length = text.toString().length
            val out = StringBuilder()
            for (i in 0 until length) {
                out.append(mMaskCharacter)
            }
            return out.toString()
        }

    /**
     * @param next Is the current char the next character to be input?
     */
    private fun updateColorForLines(next: Boolean, current: Boolean) {
        if (next) {
            mStrokePaint.color = mSecondaryColor
            mLinesPaint.color = mSecondaryColor
        } else {
            mStrokePaint.color = mSecondaryColor
            mLinesPaint.color = ContextCompat.getColor(context, R.color.white)
        }
        if (current) {
            mLinesPaint.color = ContextCompat.getColor(context, R.color.white)
            mStrokePaint.color = mPrimaryColor
        }
    }

    override fun beforeTextChanged(
        s: CharSequence,
        start: Int,
        count: Int,
        after: Int
    ) {
    }

    override fun onTextChanged(
        s: CharSequence,
        start: Int,
        before: Int,
        count: Int
    ) {
    }

    override fun afterTextChanged(s: Editable) {
        if (s.length.toFloat() == mNumChars) {
            s.toString()
        }
    }

    companion object {
        const val XML_NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android"
    }
}
