package com.kdp.ecoboxview
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import kotlin.math.min
import android.view.animation.LinearInterpolator

class EcoBoxView : View{
    private val AREA_NUMBER = 3
    private val BOX_SIZE_SCALE = 0.5//罗盘宽高占比
    private val HOLE_SIZE_SCALE = 0.45//中心圆宽高占比
    private lateinit var mBgCirclePaint:Paint//背景Paint
    private lateinit var mHolePaint: Paint//中心圆Paint
    private lateinit var mBgCircleGapPaint: Paint //背景圆间隙Paint
    private lateinit var mEqualLinePaint: Paint//平分线Paint
    private lateinit var mArcPaint: Paint//圆弧Paint
    private lateinit var mRingPaint: Paint//圆环
    private lateinit var mArcGapPaint: Paint //圆弧间隙Paint
    private var mBoxSize = 0f //罗盘的宽高
    private var mHoleSize = 0f //中心圆的宽高
    private lateinit var mCenter: PointF //中心点
    private lateinit var mRect: RectF
    private lateinit var data:MutableList<Part>


    //属性
    private var mBgCircleColor: Int = 0xFFF3F3F3.toInt() //背景圆填充色
    private var mBgCircleStrokeColor: Int = 0xFFDDDDDD.toInt() //背景圆边框色
    private var mBgCircleStrokeWidth = 2f //背景圆边框宽度
    private var mEqualLineWidth = 5f //平分线宽度
    private var mArcColor: Int = 0xFF0BD9B6.toInt() //圆弧颜色
    private var mArcGapColor: Int = 0xFFC4EDE6.toInt() //圆弧间隙颜色

    private var mCircleGap = 0f//圆与圆之间的间隙

    constructor(context: Context?) : this(context,null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){
        context?.obtainStyledAttributes(attrs,R.styleable.EcoBoxView,defStyleAttr,0)
            ?.apply {
                mBgCircleColor = getColor(R.styleable.EcoBoxView_bgCircleColor, mBgCircleColor)
                mBgCircleStrokeColor = getColor(R.styleable.EcoBoxView_bgCircleStrokeColor,mBgCircleStrokeColor)
                mBgCircleStrokeWidth = getDimension(R.styleable.EcoBoxView_bgCircleStrokeWidth,mBgCircleStrokeWidth)
                mEqualLineWidth = getDimension(R.styleable.EcoBoxView_equalLineWidth,mEqualLineWidth)
                mArcColor = getColor(R.styleable.EcoBoxView_arcColor,mArcColor)
                mArcGapColor = getColor(R.styleable.EcoBoxView_arcGapColor,mArcGapColor)
                recycle()
            }

        setLayerType(LAYER_TYPE_SOFTWARE,null)
        initPaint()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //确定罗盘宽高
        var width = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        if (widthMode == MeasureSpec.AT_MOST)
            width = getScreenWidth().times(BOX_SIZE_SCALE).toInt()
        if (heightMode == MeasureSpec.AT_MOST)
            height = getScreenHeight().times(BOX_SIZE_SCALE).toInt()
        mBoxSize = min(width,height).toFloat()
        mHoleSize = mBoxSize.times(HOLE_SIZE_SCALE).toFloat()
        setMeasuredDimension(mBoxSize.toInt(),mBoxSize.toInt())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCenter = PointF(w.div(2f),h.div(2f))
        mCircleGap = mBoxSize.div(2f).minus(mHoleSize.div(2f)).minus(mBgCircleStrokeWidth.times(10)).minus(mEqualLineWidth).div(10)
        mRect = RectF()
    }

    private fun initPaint() {
        mBgCirclePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = mBgCircleColor
        }

        mHolePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.WHITE
        }

        mBgCircleGapPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = mBgCircleStrokeColor
            strokeWidth = mBgCircleStrokeWidth.times(2)
        }
        mEqualLinePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = mBgCircleStrokeColor
            strokeWidth = mEqualLineWidth.times(2)
        }

        mArcPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = mBgCircleStrokeWidth.times(2)
            color = mArcColor
        }


        mRingPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = mArcGapColor
        }
        mArcGapPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = mEqualLineWidth.times(2)
            color = mArcColor
        }

    }

    override fun onDraw(canvas: Canvas?) {
        if (data.isNullOrEmpty()) return
        drawBackground(canvas)
        drawParts(canvas)
    }

    private fun drawParts(canvas: Canvas?) {
        canvas?.save()
        var left: Float
        var top: Float
        var right: Float
        var bottom: Float
        for (i in 0 until data.size){
            canvas?.rotate(if (i== 0) 90f else 120f,mCenter.x,mCenter.y)
            val part = data[i]
            val size  = part.percent.times(10).toInt()
            val gapScale = part.percent.times(10)-size
            //绘制透明圆弧
            mRingPaint.strokeWidth = mCircleGap.plus(mBgCircleStrokeWidth).times(size).plus(mCircleGap.times(gapScale))
            left = mBoxSize.div(2).minus(mHoleSize.div(2)).minus(mRingPaint.strokeWidth.div(2))
            top = left
            right = mBoxSize.div(2).plus(mHoleSize.div(2)).plus(mRingPaint.strokeWidth.div(2))
            bottom = right
            mRect.set(left, top, right, bottom)
            drawArc(canvas,mRingPaint)
            //绘制part
            for (j in 0 until size.plus(1)){
                mArcPaint.strokeWidth = mBgCircleStrokeWidth.times(2)
                left =  mBoxSize.div(2).minus(mHoleSize.div(2)).minus(j.times(mCircleGap.plus(mBgCircleStrokeWidth)))
                top = left
                right = mBoxSize.div(2).plus(mHoleSize.div(2)).plus(j.times(mCircleGap.plus(mBgCircleStrokeWidth)))
                bottom =  right
                mRect.set(left, top, right, bottom)
                if (j == size){
                    mArcPaint.strokeWidth = mEqualLineWidth.times(2)
                    left = mRect.left.minus(mCircleGap.times(gapScale))
                    top = left
                    right =  mRect.right.plus(mCircleGap.times(gapScale))
                    bottom = right
                }
                mRect.set(left, top, right, bottom)
                drawArc(canvas,mArcPaint)
                //绘制间隙
                val startX = mBoxSize.div(2).plus(mHoleSize.div(2f))
                val startY =  mBoxSize.div(2)
                val endX = startX.plus(mBgCircleStrokeWidth.plus(mCircleGap).times(j)).plus(mBgCircleStrokeWidth).plus(mEqualLineWidth.div(2)).plus(mCircleGap.times(gapScale))
                drawArcGap(startX,startY,endX,startY,canvas)
                canvas?.rotate(120f,mCenter.x,mCenter.y)
                drawArcGap(startX,startY,endX,startY,canvas)
                canvas?.rotate(-120f,mCenter.x,mCenter.y)
            }

        }
        canvas?.restore()
    }

    private fun drawArcGap(startX:Float,startY: Float,endX:Float,endY:Float,canvas: Canvas?) {
        canvas?.drawLine(
            startX,
            startY,
            endX,
            endY,
            mArcGapPaint)
    }

    private fun drawArc(canvas: Canvas?,paint: Paint) {
        canvas?.drawArc(mRect, 0f,120f, false, paint)
    }


    private fun drawBackground(canvas: Canvas?) {
        //背景圆
        canvas?.drawCircle(mCenter.x,mCenter.y,mBoxSize.div(2f),mBgCirclePaint)

        //中心圆
        mHolePaint.setShadowLayer(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,1f,resources.displayMetrics),0f,0f,0x20000000)
        mHolePaint.maskFilter = BlurMaskFilter(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,10f,resources.displayMetrics),BlurMaskFilter.Blur.INNER)
        canvas?.drawCircle(mCenter.x,mCenter.y,mHoleSize.div(2f),mHolePaint)
        //各部分区域间隔线
        canvas?.save()
        for (i in 0 until AREA_NUMBER){
            canvas?.rotate(if (i == 0) 60f else 120f,mCenter.x,mCenter.y)
            canvas?.drawLine(mCenter.x,mBoxSize.div(2f).minus(mHoleSize.div(2f)),mCenter.x,mBgCircleStrokeWidth,mEqualLinePaint)
        }
        canvas?.restore()
        //背景圆环
        for (i in 0..10){
            canvas?.drawCircle(mCenter.x,mCenter.y,mHoleSize.div(2f).plus(i.times(mBgCircleStrokeWidth.plus(mCircleGap))),mBgCircleGapPaint)
        }

    }

    fun setData(data:MutableList<Part>){
        this.data = data
    }

    fun invalidateAnimate(mDuration: Long = 3000){
        if (data.isNullOrEmpty()) return
        val tempData = data.map {
            it.percent
        }
        val count = tempData.max()?.times(10)?.toInt()?.plus(1)
        var currentValue: Float
        ValueAnimator.ofInt(0,count ?: 0).apply {
            interpolator = LinearInterpolator()
            duration = mDuration
            addUpdateListener {
                for (i in 0 until data.size){
                    currentValue = (it.animatedValue as Int).times(0.1).toFloat()
                    if (currentValue > tempData[i]) continue
                    if (currentValue.times(10).toInt() == tempData[i].times(10).toInt()){
                        currentValue = tempData[i]
                    }
                    data[i].percent = currentValue
                }
                invalidate()
            }
            start()
        }
    }


    private fun getScreenWidth() = resources.displayMetrics.widthPixels
    private fun getScreenHeight() = resources.displayMetrics.heightPixels

}