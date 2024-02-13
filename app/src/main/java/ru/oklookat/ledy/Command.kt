package ru.oklookat.ledy

import androidx.compose.ui.graphics.Color

data class RGB(val R: Int, val G: Int, val B: Int)

enum class Command {
    SET_COLORS,
    SET_COLOR_CORRECTION,
    SET_COLOR_TEMPERATURE
}

fun newCommandSetColors(leds: List<RGB>): ByteArray {
    val ledsBytes = rgbToByteArray(leds)
    val ledsLen = uint16toByteArray(ledsBytes.size.toUShort())

    val data = ByteArray(2 + ledsBytes.size)
    data[0] = ledsLen[0]
    data[1] = ledsLen[1]

    for (i in 2 until data.size) {
        data[i] = ledsBytes[i - 2]
    }

    return newCommand(Command.SET_COLORS, data)
}

fun newCommandSetColorCorrection(v: Int): ByteArray {
    return newCommand(Command.SET_COLOR_CORRECTION, uint32toByteArray(v.toUInt()))
}

fun newCommandSetColorTemperature(v: Int): ByteArray {
    return newCommand(Command.SET_COLOR_TEMPERATURE, uint32toByteArray(v.toUInt()))
}

fun newCommand(cmd: Command, data: ByteArray): ByteArray {
    val result = ByteArray(1 + data.size)
    result[0] = cmd.ordinal.toByte()

    for (i in 1 until result.size) {
        result[i] = data[i - 1]
    }

    return result
}

fun rgbToByteArray(leds: List<RGB>): ByteArray {
    // 3 bytes per led.
    val result = ByteArray(leds.size * 3)
    for (i in leds.indices) {
        result[i * 3 + 2] = leds[i].R.toByte()
        result[i * 3 + 1] = leds[i].G.toByte()
        result[i * 3] = leds[i].B.toByte()
    }
    return result
}

fun uint16toByteArray(v: UShort): ByteArray {
    val result = ByteArray(2)
    result[0] = v.toByte()
    result[1] = (v.toInt() shr 8).toByte()
    return result
}

fun uint32toByteArray(v: UInt): ByteArray {
    val result = ByteArray(4)
    result[0] = v.toByte()
    result[1] = (v shr 8).toByte()
    result[2] = (v shr 16).toByte()
    result[3] = (v shr 24).toByte()
    return result
}

fun colorToRGB(color: Color): RGB {
    val r = (color.red * 255).toInt()
    val g = (color.green * 255).toInt()
    val b = (color.blue * 255).toInt()

    return RGB(r, g, b)
}