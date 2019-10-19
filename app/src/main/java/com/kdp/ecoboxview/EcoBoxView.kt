package com.kdp.ecoboxview
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.min


class EcoBoxView : View{
    private val AREA_NUMBER = 3
    private val BOX_SIZE_SCALE = 0.5//罗盘宽高占比
    private val HOLE_SIZE_SCALE = 0.45//中心圆宽高占比
    private lateinit var mBgCirclePaint:Paint//背景Paint
    private lateinit var mHolePaint: Paint//中心圆Paint
    private lateinit var mBgCircleGapPaint: Paint //背景圆间隙Paint
    private lateinit var mArcPaint: Paint//圆弧Paint
    private lateinit var mRingPaint: Paint//圆环
    private lateinit var mArcGapPaint: Paint //圆弧间隙Paint
    private var mBoxSize = 0 //罗盘的宽高
    private var mHoleSize = 0 //中心圆的宽高
    private lateinit var mCenter: PointF //中心点
    private lateinit var mRect: RectF

    private lateinit var data:MutableList<Part>


    //属性
    private var mBgCircleColor: Int = 0xFFF0ECEE.toInt() //背景圆填充色
    private var mBgCircleStrokeColor: Int = 0xFFC8C0C4.toInt() //背景圆边框色
    private var mBgCircleStrokeWidth = 10f //背景圆边框宽度
    private var mArcColor: Int = 0x90008577.toInt() //圆弧颜色
    private var mArcGapColor: Int = 0x30008577 //圆弧间隙颜色

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
                recycle()
            }

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
        mBoxSize = min(width,height)
        mHoleSize = mBoxSize.times(HOLE_SIZE_SCALE).toInt()
        setMeasuredDimension(mBoxSize,mBoxSize)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCenter = PointF(w.div(2f),h.div(2f))
        mCircleGap = mCenter.x.minus(mHoleSize.div(2f)).minus(mBgCircleStrokeWidth*11).div(10)
        mRect = RectF()
    }


    public fun setData(data:MutableList<Part>){
        this.data = data
    }

    private fun initPaint() {
        mBgCirclePaint = Paint().apply {
            style = Paint.Style.FILL
            color = mBgCircleColor
        }

        mHolePaint = Paint().apply {
            style = Paint.Style.FILL
            color = Color.WHITE
        }

        mBgCircleGapPaint = Paint().apply {
            style = Paint.Style.STROKE
            color = mBgCircleStrokeColor
            strokeWidth = mBgCircleStrokeWidth
        }

        mArcPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = mBgCircleStrokeWidth
            color = mArcColor
        }

        mRingPaint = Paint().apply {
            style = Paint.Style.STROKE
            color = mArcGapColor
        }
        mArcGapPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = mBgCircleStrokeWidth
            color = mArcColor
        }

    }

    override fun onDraw(canvas: Canvas?) {

        //画背景圆环
        drawBgRing(canvas)
        //画中心圆
        drawHole(canvas)
        //画背景圆间隙
        drawBgCircleGap(canvas)
        //画背景圆
        drawBgCircle(canvas)
        //画各部分圆弧
        drawArc(canvas)
    }

    private fun drawArc(canvas: Canvas?) {
        canvas?.save()
        for (i in 0 until data.size){
            canvas?.rotate(if (i== 0) 90f else 120f,mCenter.x,mCenter.y)
            val part = data[i]

            for (j in 0 until part.percent.times(10).toInt().plus(1)){
                    mRect.set(
                        mCenter.x.minus(mHoleSize.div(2f)).minus((mBgCircleStrokeWidth.plus(mCircleGap)).times(j)),
                        mCenter.y.minus(mHoleSize.div(2f)).minus((mBgCircleStrokeWidth.plus(mCircleGap)).times(j)),
                        mCenter.x.plus(mHoleSize.div(2f)).plus((mBgCircleStrokeWidth.plus(mCircleGap)).times(j)),
                        mCenter.y.plus(mHoleSize.div(2f)).plus((mBgCircleStrokeWidth.plus(mCircleGap)).times(j)))
                    canvas?.drawArc(mRect, 0f,120f, false, mArcPaint)

                    //画圆环

                if (j > 0){
                    mRect.set(
                        mCenter.x.minus(mHoleSize.div(2f)).minus((mBgCircleStrokeWidth.plus(mCircleGap)).times(j-1)).minus(mBgCircleStrokeWidth),
                        mCenter.y.minus(mHoleSize.div(2f)).minus((mBgCircleStrokeWidth.plus(mCircleGap)).times(j-1)).minus(mBgCircleStrokeWidth),
                        mCenter.x.plus(mHoleSize.div(2f)).plus((mBgCircleStrokeWidth.plus(mCircleGap)).times(j-1)).plus(mBgCircleStrokeWidth),
                        mCenter.y.plus(mHoleSize.div(2f)).plus((mBgCircleStrokeWidth.plus(mCircleGap)).times(j-1)).plus(mBgCircleStrokeWidth))
                    mRingPaint.strokeWidth = mCircleGap
                    canvas?.drawArc(mRect, 0f,120f, false, mRingPaint)
                }


                //绘制间隙
                canvas?.drawLine(mCenter.x.plus(mHoleSize.div(2f)),
                    mCenter.y,
                    mCenter.x.plus(mHoleSize.div(2f)).plus((mBgCircleStrokeWidth.plus(mCircleGap)).times(j)),
                    mCenter.y,
                    mArcGapPaint)
                canvas?.rotate(120f,mCenter.x,mCenter.y)
                canvas?.drawLine(mCenter.x.plus(mHoleSize.div(2f)),
                    mCenter.y,
                    mCenter.x.plus(mHoleSize.div(2f)).plus((mBgCircleStrokeWidth.plus(mCircleGap)).times(j)),
                    mCenter.y,
                    mArcGapPaint)
                canvas?.rotate(-120f,mCenter.x,mCenter.y)
            }


        }
        canvas?.restore()
    }

    private fun drawBgCircle(canvas: Canvas?) {
        for (i in 0..10){
            canvas?.drawCircle(mCenter.x,mCenter.y,mHoleSize.div(2f).plus(i.times(mBgCircleStrokeWidth+mCircleGap)),mBgCircleGapPaint)
        }
    }

    private fun drawBgCircleGap(canvas: Canvas?) {
        canvas?.save()
        for (i in 0 until AREA_NUMBER){
            canvas?.rotate(if (i == 0) 60f else 120f,mCenter.x,mCenter.y)
            canvas?.drawLine(mCenter.x,mBoxSize.div(2f).minus(mHoleSize.div(2f)),mCenter.x,mBgCircleStrokeWidth,mBgCircleGapPaint)
        }
        canvas?.restore()
    }

    private fun drawHole(canvas: Canvas?) {
        canvas?.drawCircle(mCenter.x,mCenter.y,mHoleSize.div(2f),mHolePaint)
    }

    private fun drawBgRing(canvas: Canvas?) {
        canvas?.drawCircle(mCenter.x,mCenter.y,mBoxSize.div(2f),mBgCirclePaint)
    }


    private fun getScreenWidth() = resources.displayMetrics.widthPixels
    private fun getScreenHeight() = resources.displayMetrics.heightPixels

}