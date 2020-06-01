package com.pandulapeter.beagle.appExample

import android.app.Application
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.pandulapeter.beagle.Beagle
import com.pandulapeter.beagle.common.configuration.Appearance
import com.pandulapeter.beagle.common.listeners.OverlayListener
import com.pandulapeter.beagle.modules.SwitchModule
import com.pandulapeter.beagle.modules.TextModule

@Suppress("unused")
class BeagleExampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            var shouldDrawCircle = false
            Beagle.initialize(
                application = this,
                appearance = Appearance(
                    themeResourceId = R.style.BeagleTheme
                )
            )
            Beagle.setModules(
                TextModule(
                    text = "This is a green text",
                    color = Color.GREEN
                ),
                TextModule(
                    text = "This is a red text",
                    color = Color.RED
                ),
                TextModule(
                    text = "This is a blue text",
                    color = Color.BLUE
                ),
                TextModule(
                    text = "This text uses the default color"
                ),
                SwitchModule(
                    text = "Should draw circle",
                    onValueChanged = {
                        shouldDrawCircle = it
                        Beagle.invalidateOverlay()
                    }
                )
            )
            Beagle.addOverlayListener(object : OverlayListener {

                private val paint = Paint().apply {
                    style = Paint.Style.FILL
                    color = Color.CYAN
                }

                override fun onDrawOver(canvas: Canvas) {
                    if (shouldDrawCircle) {
                        canvas.drawCircle(100f, 100f, 50f, paint)
                    }
                }
            })
        }
    }
}