package ru.oklookat.ledy

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor

@Composable
fun SetColorsScreen() {
    ClassicColorPicker(modifier = Modifier.padding(8.dp),
        color = HsvColor.from(
            Color.Blue
        ),
        showAlphaBar = false,
        onColorChanged = {

            val leds = mutableListOf<RGB>()
            val col = it.toColor()
            
            val colRgb =
                colorToRGB(col)
            for (i in 0..240) {
                leds.add(colRgb)
            }

            cl.setColors(leds)
        })
}