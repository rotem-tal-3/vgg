package com.dishtech.vgg


class Color(val R: Int, val G: Int, val B: Int, val A: Int = 0xff) {
    constructor(hex: String) : this(BitmapUtils.R(hex),
                                    BitmapUtils.G(hex),
                                    BitmapUtils.B(hex),
                                    BitmapUtils.A(hex))
    companion object {

        val BLACK = Color("000000")
        val WHITE = Color("FFFFFF")

        // Rainbow Scheme
        val RED = Color("FF0000")
        val ORANGE = Color("FFA500")
        val YELLOW = Color("FFFF00")
        val GREEN =  Color("008000")
        val BLUE = Color("0000FF")
        val INDIGO = Color("4B0082")
        val VIOLET = Color("EE82EE")
        val RAINBOW_SCHEME_7 = arrayOf(RED, ORANGE, YELLOW, GREEN, BLUE, INDIGO, VIOLET)

        // Autumn scheme.
        val ENGLISH_VIOLET = Color("56305D")
        val PALATINATE_PURPLE = Color("693668")
        val MAXIMUM_RED_PURPLE = Color("A74482")
        val PINK_PANTONE = Color("D04795")
        val MAGENTA_CRAYOLA = Color("F84AA7")
        val BRINK_PINK = Color("FF587D")
        val ULTRA_RED = Color("FF6789")
        val AUTOMN_SCHEME_7 = arrayOf(ENGLISH_VIOLET, PALATINATE_PURPLE, MAXIMUM_RED_PURPLE,
                                      PINK_PANTONE, MAGENTA_CRAYOLA, BRINK_PINK, ULTRA_RED)

        // Desert scheme.
        val GOLD_CRAYOLA = Color("DCC48E")
        var DUTCH_WHITE = Color("E3DAB1")
        var PALE_SPRING_BUD = Color("E7E5C2")
        var BEIGE = Color("EAEFD3")
        var LAUREL_GREEN_LIGHT = Color("C5CFB3")
        var LAUREL_GREEN_DARK = Color("B3C0A4")
        var MORNING_BLUE = Color("9BA595")
        val DESERT_SCHEME_7 = arrayOf(GOLD_CRAYOLA, DUTCH_WHITE, PALE_SPRING_BUD, BEIGE,
                                      LAUREL_GREEN_LIGHT, LAUREL_GREEN_DARK, MORNING_BLUE)

        val SCHEMES = arrayOf(RAINBOW_SCHEME_7, AUTOMN_SCHEME_7, DESERT_SCHEME_7)

    }
}