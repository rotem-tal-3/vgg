package com.dishtech.vgg

import android.content.res.Resources
import android.media.MediaPlayer
import android.opengl.Matrix
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.dishtech.vgg.audio.AudioPlayerSyncer
import com.dishtech.vgg.audio.SpectrumFeeder
import com.dishtech.vgg.audio.WavHandler
import com.dishtech.vgg.engine.Camera
import com.dishtech.vgg.engine.Projections
import com.dishtech.vgg.engine.Vec3f
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
    lateinit var shaderViewModel: ShaderViewModel
    lateinit var swipeRecognizer: SwipeRecognizer
    lateinit var quadRendererView: QuadRendererView
    lateinit var spectrumViewModel: SpectrumViewModel
    lateinit var schemeViewModel: SchemeViewModel
    lateinit var timedViewModel: TimedViewModel
    lateinit var syncer: AudioPlayerSyncer
    lateinit var defaultViewModel: DefaultViewModel
    lateinit var configurations: Array<DefaultViewModelConfiguration>
    private val world = World(Vec3f(0.2f), Vec3f(1.1f, 0.2f, 1f),
                              Vec3f(1f, 1.4f, 0.5f), 1f, 10f)
    private var camera = cameraWithPosition(Vec3f(1f))
    var currentConfigurationIndex = 0

    private fun cameraWithPosition(position: Vec3f) = Camera(position, Vec3f(1f, 0.1f, 0.5f),
                                                             Vec3f(1f, 1.2f, 0.4f),
                                                             PI.toFloat() / 2f)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = RelativeLayout(this)
        layout.layoutParams = ConstraintLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        setContentView(layout)
        syncer = audioPlayerSyncer()
        VertexHandler.matrixMVP = Projections.mvpProjection(world, camera)
        spectrumViewModel = SpectrumViewModel(syncer)
        schemeViewModel = SchemeViewModel()
        timedViewModel = TimedViewModel({ f, _ -> return@TimedViewModel f }, null, arrayOf())
        configurations = createConfigurations()
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
                    camera = cameraWithPosition(Vec3f(x, 1f, y))
                    VertexHandler.matrixMVP = Projections.mvpProjection(world, camera)
                    defaultViewModel.onGesture(Gesture(Gestures.TOUCH, floatArrayOf(x, y)))
                    return@setOnTouchListener true
                }
                layout.addView(view)
            }
    }

    private fun createConfigurations(): Array<DefaultViewModelConfiguration> {
        val tunnelViewModel = TunnelViewModel(
            loadBitmapForTexture(resources, R.drawable.img0),
            loadBitmapForTexture(resources, R.drawable.leaf),
            schemeViewModel.initialScheme
        )
        val barViewModel = BarViewModel(schemeViewModel.initialScheme)
        val starTunnelViewModel = ParameterlessShaderViewModel("frag")
        shaderViewModel = barViewModel
        return arrayOf(
            // Bar
            DefaultViewModelConfiguration(
                arrayOf(spectrumViewModel),
                arrayOf(barViewModel),
                arrayOf(schemeViewModel),
                barViewModel,
                spectrumViewModel
            ),
            // Tunnel
            DefaultViewModelConfiguration(
                arrayOf(spectrumViewModel, TimedViewModel({ f, _ ->
                                                              return@TimedViewModel f % 30f
                                                          },null, arrayOf())),
                arrayOf(tunnelViewModel),
                arrayOf(schemeViewModel, tunnelViewModel),
                tunnelViewModel,
                spectrumViewModel
            ),
            // StarTunnel
            DefaultViewModelConfiguration(
                arrayOf(timedViewModel),
                arrayOf(starTunnelViewModel),
                arrayOf(),
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