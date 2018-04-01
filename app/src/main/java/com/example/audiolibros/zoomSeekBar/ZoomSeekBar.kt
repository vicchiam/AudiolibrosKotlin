package com.example.audiolibros.zoomSeekBar

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

import com.example.audiolibros.R

/**
 * Created by vicch on 31/01/2018.
 */

class ZoomSeekBar(context: Context, attrs: AttributeSet) : View(context, attrs) {

    // Valor a controlar
    var value = 160 // valor seleccionado
    var valMin = 100 // valor mínimo
        set(valMin: Int){
            field = valMin
            if (escalaMin > valMin) {
                escalaMin = valMin
            }
            if (escalaIni > valMin) {
                escalaIni = valMin
            }
            invalidate()
        }
    var valMax = 200 // valor máximo
        set(valMax: Int){
            field = valMax
            if (escalaMax > valMax) {
                escalaMax = valMax
            }
            invalidate()
        }
    var escalaMin = 150 // valor mínimo visualizado
        set(escalaMin: Int){
            if (escalaMin >= valMin && escalaMin <= valMax) {
                field = escalaMin
            }
            invalidate()
        }
    var escalaMax = 180 // valor máximo visualizado
        set(escalaMax: Int){
            if (escalaMin >= valMin && escalaMax <= valMax) {
                field = escalaMax
            }
            invalidate()
        }
    var escalaIni = 100 // origen de la escala
        set(escalaIni: Int){
            if (escalaIni >= valMin && escalaIni <= valMax) {
                field = escalaIni
            }
            invalidate()
        }
    var escalaRaya = 2 // cada cuantas unidades una rayas
        set(escalaRaya: Int){
            field = escalaRaya
            invalidate()
        }
    var escalaRayaLarga = 5 // cada cuantas rayas una larga
        set(escalaRayaLarga){
            field = escalaRayaLarga
            invalidate()
        }
    // Dimensiones en pixels
    private var altoNumeros: Int = 0
    private var altoRegla: Int = 0
    private var altoBar: Int = 0
    private var altoPalanca: Int = 0
    private var anchoPalanca: Int = 0
    private var altoGuia: Int = 0
    // Valores que indican donde dibujar
    private var xIni: Int = 0
    private var yIni: Int = 0
    private var ancho: Int = 0
    // Objetos Rect con diferentes regiones
    private val escalaRect = Rect()
    private val barRect = Rect()
    private val guiaRect = Rect()
    private val palancaRect = Rect()
    // Objetos Paint globales para no tener que crearlos cada vez
    private val textoPaint = Paint()
    private val reglaPaint = Paint()
    private val guiaPaint = Paint()
    private val palancaPaint = Paint()
    internal var estado = Estado.SIN_PULSACION
    internal var antVal_0: Int = 0
    internal var antVal_1: Int = 0

    //Escuchador
    private var escuchador: OnValListener? = null

    // Variables globales usadas en onTouchEvent()
    internal enum class Estado {
        SIN_PULSACION, PALANCA_PULSADA, ESCALA_PULSADA, ESCALA_PULSADA_DOBLE
    }

    init {
        val dp = resources.displayMetrics.density
        val a = context.theme.obtainStyledAttributes(attrs,
                R.styleable.ZoomSeekBar, 0, 0)
        try {
            altoNumeros = a.getDimensionPixelSize(
                    R.styleable.ZoomSeekBar_altoNumeros, (30 * dp).toInt())
            altoRegla = a.getDimensionPixelSize(
                    R.styleable.ZoomSeekBar_altoRegla, (20 * dp).toInt())
            altoBar = a.getDimensionPixelSize(
                    R.styleable.ZoomSeekBar_altoBar, (70 * dp).toInt())
            altoPalanca = a.getDimensionPixelSize(R.styleable.ZoomSeekBar_altoPalanca, (40 * dp).toInt())
            altoGuia = a.getDimensionPixelSize(R.styleable.ZoomSeekBar_altoGuia, (10 * dp).toInt())
            anchoPalanca = a.getDimensionPixelSize(R.styleable.ZoomSeekBar_anchoPalanca, (20 * dp).toInt())
            textoPaint.textSize = a.getDimension(R.styleable.ZoomSeekBar_altoTexto, 16 * dp)
            textoPaint.color = a.getColor(R.styleable.ZoomSeekBar_colorTexto, Color.BLACK)
            reglaPaint.color = a.getColor(R.styleable.ZoomSeekBar_colorRegla, Color.BLACK)
            guiaPaint.color = a.getColor(R.styleable.ZoomSeekBar_colorGuia, Color.BLUE)
            palancaPaint.color = a.getColor(
                    R.styleable.ZoomSeekBar_colorPalanca, -0xffff81)

            value = a.getInteger(R.styleable.ZoomSeekBar_val, 1)
            valMin = a.getInteger(R.styleable.ZoomSeekBar_valMin, 1)
            valMax = a.getInteger(R.styleable.ZoomSeekBar_valMax, 10)
            escalaMin = a.getInteger(R.styleable.ZoomSeekBar_escalaMin, 1)
            escalaMax = a.getInteger(R.styleable.ZoomSeekBar_escalaMax, 10)
            escalaIni = a.getInteger(R.styleable.ZoomSeekBar_escalaIni, 1)
            escalaRaya = a.getInteger(R.styleable.ZoomSeekBar_escalaRaya, 1)
            escalaRayaLarga = a.getInteger(R.styleable.ZoomSeekBar_escalaRayaLarga, 5)
        } finally {
            a.recycle()
        }
        textoPaint.isAntiAlias = true
        textoPaint.textAlign = Paint.Align.CENTER
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        xIni = paddingLeft
        //yIni = getPaddingTop();
        yIni = (h - (altoBar + altoNumeros + altoRegla)) / 2
        if (yIni < 0) yIni = 0
        ancho = width - paddingRight - paddingLeft
        barRect.set(xIni, yIni, xIni + ancho, yIni + altoBar)
        escalaRect.set(xIni, yIni + altoBar, xIni + ancho, yIni + altoBar
                + altoNumeros + altoRegla)
        val y = yIni + (altoBar - altoGuia) / 2
        guiaRect.set(xIni, y, xIni + ancho, y + altoGuia)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Dibujamos Barra con palanca
        canvas.drawRect(guiaRect, guiaPaint)
        var y = yIni + (altoBar - altoPalanca) / 2
        var x = xIni + ancho * (value - escalaMin) / (escalaMax - escalaMin) - anchoPalanca / 2
        palancaRect.set(x, y, x + anchoPalanca, y + altoPalanca)
        canvas.drawRect(palancaRect, palancaPaint)
        palancaRect.set(x - anchoPalanca / 2, y, x + 3 * anchoPalanca / 2, y + altoPalanca)
        // Dibujamos Escala
        var v = escalaIni
        while (v <= escalaMax) {
            if (v >= escalaMin) {
                x = xIni + ancho * (v - escalaMin) / (escalaMax - escalaMin)
                if ((v - escalaIni) / escalaRaya % escalaRayaLarga == 0) {
                    y = yIni + altoBar + altoRegla
                    canvas.drawText(Integer.toString(v), x.toFloat(), (y + altoNumeros).toFloat(),
                            textoPaint)
                } else {
                    y = yIni + altoBar + altoRegla * 1 / 3
                }
                canvas.drawLine(x.toFloat(), (yIni + altoBar).toFloat(), x.toFloat(), y.toFloat(), reglaPaint)
            }
            v += escalaRaya
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x_0: Int
        val y_0: Int
        val x_1: Int
        val y_1: Int
        x_0 = event.getX(0).toInt()
        y_0 = event.getY(0).toInt()
        val val_0 = escalaMin + (x_0 - xIni) * (escalaMax - escalaMin) / ancho
        if (event.pointerCount > 1) {
            x_1 = event.getX(1).toInt()
            y_1 = event.getY(1).toInt()
        } else {
            x_1 = x_0
            y_1 = y_0
        }
        val val_1 = escalaMin + (x_1 - xIni) * (escalaMax - escalaMin) / ancho
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> if (palancaRect.contains(x_0, y_0)) {
                estado = Estado.PALANCA_PULSADA
            } else if (barRect.contains(x_0, y_0)) {
                if (val_0 > value) value++ else value--
                invalidate(barRect)
            } else if (escalaRect.contains(x_0, y_0)) {
                estado = Estado.ESCALA_PULSADA
                antVal_0 = val_0
            }
            MotionEvent.ACTION_POINTER_DOWN -> if (estado == Estado.ESCALA_PULSADA) {
                if (escalaRect.contains(x_1, y_1)) {
                    antVal_1 = val_1
                    estado = Estado.ESCALA_PULSADA_DOBLE
                }
            }
            MotionEvent.ACTION_UP -> estado = Estado.SIN_PULSACION
            MotionEvent.ACTION_POINTER_UP -> if (estado == Estado.ESCALA_PULSADA_DOBLE) {
                estado = Estado.ESCALA_PULSADA
            }
            MotionEvent.ACTION_MOVE -> {
                if (estado == Estado.PALANCA_PULSADA) {
                    //val = ponDentroRango(val_0, escalaMin, escalaMax);
                    setVal(ponDentroRango(val_0, escalaMin, escalaMax))
                    invalidate(barRect)
                }
                if (estado == Estado.ESCALA_PULSADA_DOBLE) {
                    escalaMin = antVal_0 + (xIni - x_0) * (antVal_0 - antVal_1) / (x_0 - x_1)
                    escalaMin = ponDentroRango(escalaMin, valMin, value)
                    escalaMax = antVal_0 + (ancho + xIni - x_0) * (antVal_0 - antVal_1) / (x_0 - x_1)
                    escalaMax = ponDentroRango(escalaMax, value, valMax)
                    invalidate()
                }
            }
        }
        return true
    }

    private fun ponDentroRango(value: Int, valMin: Int, valMax: Int): Int {
        return if (value < valMin) {
            valMin
        } else if (value > valMax) {
            valMax
        } else {
            value
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val altoDeseado = (altoNumeros + altoRegla + altoBar
                + paddingBottom + paddingTop)
        val alto = obtenDimension(heightMeasureSpec, altoDeseado)
        val anchoDeseado = 2 * altoDeseado
        val ancho = obtenDimension(widthMeasureSpec, anchoDeseado)
        setMeasuredDimension(ancho, alto)
    }

    private fun obtenDimension(measureSpec: Int, deseado: Int): Int {
        val dimension = View.MeasureSpec.getSize(measureSpec)
        val modo = View.MeasureSpec.getMode(measureSpec)
        return if (modo == View.MeasureSpec.EXACTLY) {
            dimension
        } else if (modo == View.MeasureSpec.AT_MOST) {
            Math.min(dimension, deseado)
        } else {
            deseado
        }
    }

    fun getVal(): Int {
        return value
    }

    fun setVal(value: Int) {
        if (valMin <= value && value <= valMax) {
            this.value = value
            escalaMin = Math.min(escalaMin, value)
            escalaMax = Math.max(escalaMax, value)
            invalidate()

            this.escuchador?.onChangeVal(value)
        }
    }

    fun setValNoEvent(value: Int) {
        if (valMin <= value && value <= valMax) {
            this.value = value
            escalaMin = Math.min(escalaMin, value)
            escalaMax = Math.max(escalaMax, value)
            invalidate()
        }
    }



    fun setOnValListener(escuchador: OnValListener) {
        this.escuchador = escuchador
    }


}
