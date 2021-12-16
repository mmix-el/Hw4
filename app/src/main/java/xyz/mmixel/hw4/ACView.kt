package xyz.mmixel.hw4

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.min

class ACView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private companion object {
        private const val FULL_TURN = 360F
        private const val H_MINUTES = 60F
        private const val H_SECONDS = 3600F
        private const val M_SECONDS = 60F
        private const val H_PER_TURN = 12F
        private const val M_PER_TURN = 60F
        private const val S_PER_TURN = 60F
        private const val PHI = -90f
        private const val H_DELTA_PHI = FULL_TURN / H_PER_TURN
        private const val M_DELTA_PHI = FULL_TURN / M_PER_TURN
        private const val S_DELTA_PHI = FULL_TURN / S_PER_TURN
        private const val DELAY = 1000L
    }

    private var hourHandAngle = 0F
    private var minuteHandAngle = 0F
    private var secondHandAngle = 0F
    private var hourHandSize = 400F
    private var minuteHandSize = 450F
    private var secondHandSize = 500F
    private var padding = 100F
    private var radius = 0F
    private var hourHandColor = Color.RED
    private var minuteHandColor = Color.LTGRAY
    private var secondHandColor = Color.GREEN
    private var isTicking = true
    private var calendar = Calendar.getInstance()

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ACView)
        hourHandColor =
            typedArray.getColor(R.styleable.ACView_hour_hand_color, hourHandColor)
        minuteHandColor =
            typedArray.getColor(R.styleable.ACView_minute_hand_color, minuteHandColor)
        secondHandColor =
            typedArray.getColor(R.styleable.ACView_second_hand_color, secondHandColor)
        hourHandSize =
            typedArray.getDimension(R.styleable.ACView_hour_hand_size, hourHandSize)
        minuteHandSize =
            typedArray.getDimension(R.styleable.ACView_minute_hand_size, minuteHandSize)
        secondHandSize =
            typedArray.getDimension(R.styleable.ACView_second_hand_size, secondHandSize)

        typedArray.recycle()
    }

    private val paintBorder = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        strokeWidth = 20f
        this.style = Paint.Style.STROKE
    }

    private val paintHourHand = Paint().apply {
        color = hourHandColor
        strokeWidth = 20F
        this.style = Paint.Style.STROKE
    }

    private val paintMinuteHand = Paint().apply {
        color = minuteHandColor
        strokeWidth = 15F
        this.style = Paint.Style.STROKE
    }

    private val paintSecondHand = Paint().apply {
        color = secondHandColor
        strokeWidth = 10F
        this.style = Paint.Style.STROKE
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        thread {
            while (isTicking) {
                calendar = Calendar.getInstance()
                val hours = calendar[Calendar.HOUR_OF_DAY]
                val minutes = calendar[Calendar.MINUTE]
                val seconds = calendar[Calendar.SECOND]

                hourHandAngle = hourHandAngle(hours, minutes, seconds)
                minuteHandAngle = minuteHandAngle(minutes, seconds)
                secondHandAngle = secondHandAngle(seconds)
                invalidate()
                Thread.sleep(DELAY)
            }
        }
    }

    private fun hourHandAngle(h: Int, m: Int, s: Int): Float =
        PHI + h * H_DELTA_PHI + m * (H_DELTA_PHI / H_MINUTES) + s * (H_DELTA_PHI / H_SECONDS)

    private fun minuteHandAngle(m: Int, s: Int): Float =
        PHI + m * M_DELTA_PHI + s * (M_DELTA_PHI / M_SECONDS)

    private fun secondHandAngle(s: Int): Float = PHI + s * S_DELTA_PHI

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        radius = min(height, width) / 2F - padding
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2F
        val cy = height / 2F
        drawDial(canvas, cx, cy, paintBorder)
        drawHands(canvas, cx, cy)
    }

    private fun drawDial(canvas: Canvas, cx: Float, cy: Float, paint: Paint) {
        canvas.drawColor(Color.DKGRAY)
        var phi = 0F
        for (hour in 1..12) {
            canvas.save()
            canvas.rotate(phi, cx, cy)
            canvas.drawRect(cx - radius, cy, cx - radius * 0.8F, cy, paint)
            canvas.restore()
            phi += H_DELTA_PHI
        }
        canvas.drawCircle(cx, cy, radius, paint)
    }

    private fun drawHands(canvas: Canvas, cx: Float, cy: Float) {
        drawHand(canvas, cx, cy, hourHandSize, hourHandAngle, paintHourHand)
        drawHand(canvas, cx, cy, minuteHandSize, minuteHandAngle, paintMinuteHand)
        drawHand(canvas, cx, cy, secondHandSize, secondHandAngle, paintSecondHand)
    }

    private fun drawHand(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        handSize: Float,
        angle: Float,
        paint: Paint
    ) {
        canvas.save()
        canvas.rotate(angle, cx, cy)
        canvas.drawRect(cx - handSize * 0.25F, cy, cx + handSize * 0.75F, cy, paint)
        canvas.restore()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isTicking = false
    }
}
