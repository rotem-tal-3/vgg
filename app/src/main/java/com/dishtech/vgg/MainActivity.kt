package com.dishtech.vgg

import android.media.MediaPlayer
import android.opengl.Matrix
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.dishtech.vgg.audio.AudioPlayerSyncer
import com.dishtech.vgg.audio.SpectrumFeeder
import com.dishtech.vgg.audio.WavHandler
import com.dishtech.vgg.engine.Projections
import com.dishtech.vgg.engine.World
import com.dishtech.vgg.quadrenderer.QuadRenderer
import com.dishtech.vgg.quadrenderer.QuadRendererView
import com.dishtech.vgg.shaders.VertexHandler
import com.dishtech.vgg.ui.gestures.Gesture
import com.dishtech.vgg.ui.gestures.GestureDelegate
import com.dishtech.vgg.ui.gestures.Gestures
import com.dishtech.vgg.ui.gestures.SwipeRecognizer
import com.dishtech.vgg.viewmodel.*
import java.lang.ref.WeakReference
import kotlin.math.PI


class MainActivity : AppCompatActivity(), GestureDelegate {
    lateinit var swipeRecognizer: SwipeRecognizer
    lateinit var quadRendererView: QuadRendererView
    lateinit var defaultViewModel: DefaultViewModel
    lateinit var configurations: Array<DefaultViewModelConfiguration>
    var currentConfigurationIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = RelativeLayout(this)
        layout.layoutParams = ConstraintLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        setContentView(layout)
        configurations = createConfigurations()
        VertexHandler.projection.value = Projections.perspectiveProjection(PI.toFloat() / 4f,
                                                                           0.1f,
                                                                           100f)
        val viewMatrix =  FloatArray(16)
        Matrix.setIdentityM(viewMatrix, 0)
        Matrix.translateM(viewMatrix, 0, 0f, 0f, -4f)
        VertexHandler.view.value = viewMatrix

        defaultViewModel = DefaultViewModel(configurations[currentConfigurationIndex])
        setupSwipeRecognizer()
        val quadRenderer = QuadRenderer(WeakReference(defaultViewModel))
        quadRendererView = QuadRendererView(this, quadRenderer = quadRenderer)
            .also { view ->
                RelativeLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.MATCH_PARENT
                ).also {
                    it.addRule(RelativeLayout.CENTER_HORIZONTAL)
                    it.addRule(RelativeLayout.CENTER_VERTICAL)
                    it.setMargins(0, 162, 0, 0)
                    view.layoutParams = it
                }
                view.setOnTouchListener { touchedView, motionEvent ->
                    touchedView.performClick()
                    swipeRecognizer.onTouch(touchedView, motionEvent)
                    val x = motionEvent.getX() / touchedView.width
                    val y = motionEvent.getY() / touchedView.height
                    defaultViewModel.onGesture(Gesture(Gestures.TOUCH, floatArrayOf(x, y)))
                    return@setOnTouchListener true
                }
                layout.addView(view)
            }
    }

    private fun createConfigurations(): Array<DefaultViewModelConfiguration> {
        val syncer = audioPlayerSyncer()
        val spectrumViewModel = SpectrumViewModel(syncer)
        val schemeViewModel = SchemeViewModel()
        val timedViewModel = TimedViewModel({ f, _ ->
                                                return@TimedViewModel f % 30f
                                            }, null, arrayOf())
        val tunnelHandler = TunnelHandler(
            loadBitmapForTexture(resources, R.drawable.img0),
            loadBitmapForTexture(resources, R.drawable.leaf),
            schemeViewModel.initialScheme
        )
        val barViewModel = ParameterlessShaderHandler("bar")
        val starTunnelViewModel = ParameterlessShaderHandler("fibo")
        return arrayOf(
            // Bar
            DefaultViewModelConfiguration(
                arrayOf(spectrumViewModel),
                arrayOf(),
                arrayOf(schemeViewModel),
                barViewModel,
                spectrumViewModel
            ),
            // Tunnel
            DefaultViewModelConfiguration(
                arrayOf(spectrumViewModel, timedViewModel),
                arrayOf(tunnelHandler),
                arrayOf(schemeViewModel, tunnelHandler),
                tunnelHandler,
                spectrumViewModel
            ),
            // StarTunnel
            DefaultViewModelConfiguration(
                arrayOf(timedViewModel, spectrumViewModel),
                arrayOf(),
                arrayOf(schemeViewModel),
                starTunnelViewModel,
                null
            )
        )
    }

    private fun setupSwipeRecognizer() {
        swipeRecognizer = SwipeRecognizer(this, WeakReference(this))
    }

    private fun audioPlayerSyncer(): AudioPlayerSyncer {
        val wav = R.raw.mus3
        val handler = WavHandler(AssetInputStreamHandler.streamHandler(this, wav))
        val specProvider = SpectrumFeeder(handler)
        val player = MediaPlayer.create(this, wav)
        val syncer = AudioPlayerSyncer(specProvider, player)
        return syncer
    }

    override fun onGesture(gesture: Gesture) {
        quadRendererView.queueEvent(Runnable {
            defaultViewModel.onGesture(gesture)
            if (gesture.gesture == Gestures.SWIPE_DOWN) {
                currentConfigurationIndex = (currentConfigurationIndex + 1) % configurations.size
                defaultViewModel.configuration = configurations[currentConfigurationIndex]
            }
        })
    }
}