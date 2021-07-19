package seamcarving

import java.awt.image.BufferedImage
import kotlin.math.max
import kotlin.math.sqrt

fun BufferedImage.myGetRGB(x: Int, y: Int): Triple<Int, Int, Int> {
    val color = getRGB(x, y)
    val blue = color and 0xff
    val green = color and 0xff00 shr 8
    val red = color and 0xff0000 shr 16
    return Triple(red, green, blue)
}

fun BufferedImage.mySetRGB(x: Int, y: Int, r: Int, g: Int, b: Int) {
    setRGB(x, y, (r shl 16) + (g shl 8) + b)
}

fun BufferedImage.deepCopy(): BufferedImage {
    val cm = colorModel
    val isAlphaPremultiplied = cm.isAlphaPremultiplied
    val raster = copyData(raster.createCompatibleWritableRaster())
    return BufferedImage(cm, raster, isAlphaPremultiplied, null)
}

fun BufferedImage.getTransposed(): BufferedImage {
    val newImage = BufferedImage(height, width, BufferedImage.TYPE_INT_RGB)
    for (x in 0 until height) {
        for (y in 0 until width) {
            newImage.setRGB(x, y, getRGB(y, x))
        }
    }
    return newImage
}

class SeamCarving(val image: BufferedImage) {
    fun getReducedImage(w: Int, h: Int): BufferedImage {
        var resImage = image.deepCopy()
        repeat(w) {
            val scr = SeamRemover(resImage)
            resImage = scr.removeSeamDP()
        }
        resImage = resImage.getTransposed()
        repeat(h) {
            val scr = SeamRemover(resImage)
            resImage = scr.removeSeamDP()
        }
        return resImage.getTransposed()
    }

    class SeamRemover(val image: BufferedImage) {
        private var maxEnergy = 0.0
        private val energyArr = Array(image.width) { Array(image.height) { 0.0 } }

        init {
            calculateEnergy()
        }

        data class DpData(var energy: Double, var par: Int)

        fun removeSeamDP(): BufferedImage {
            val dp = Array(image.width) { Array(image.height) { DpData(-1.0, -1) } }
            for (x in 0 until image.width) {
                dp[x][0].energy = energyArr[x][0]
            }
            for (y in 1 until image.height) {
                for (x in 0 until image.width) {
                    val t = (-1..1).mapNotNull { xm ->
                        if (x + xm in 0 until image.width) dp[x + xm][y - 1].energy to x + xm else null
                    }.minByOrNull { it.first }!!
                    dp[x][y].energy = energyArr[x][y] + t.first
                    dp[x][y].par = t.second
                }
            }
            val minOnLastRow = Array(image.width) { i -> dp[i][image.height - 1].energy to i }.minByOrNull { it.first }!!
            var seamX = minOnLastRow.second

            for (y in image.height - 1 downTo 0) {
                removePixel(seamX, y)
                seamX = dp[seamX][y].par
            }
            return image.getSubimage(0, 0, image.width - 1, image.height)
        }

        private fun removePixel(x: Int, y: Int) {
            var curX = x
            while (curX < image.width - 1) {
                image.setRGB(curX, y, image.getRGB(curX + 1, y))
                curX++
            }
        }


        private fun calculateEnergy() {
            for (x in 0 until image.width) {
                for (y in 0 until image.height) {
                    val xs = shiftCoordinateX(x)
                    val ys = shiftCoordinateY(y)

                    energyArr[x][y] = sqrt((calcGradientX(xs, y) + calcGradientY(x, ys)).toDouble())
                    maxEnergy = max(maxEnergy, energyArr[x][y])
                }
            }
        }

        fun createIntensityImage(): BufferedImage {
            val newImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
            for (x in 0 until image.width) {
                for (y in 0 until image.height) {
                    val intensity = if (maxEnergy != 0.0) (255.0 * energyArr[x][y] / maxEnergy).toInt() else 255
                    newImage.mySetRGB(x, y, intensity, intensity, intensity)
                }
            }
            return newImage
        }

        private fun shiftCoordinateX(x: Int): Int {
            return when (x) {
                0 -> x + 1
                image.width - 1 -> x - 1
                else -> x
            }
        }

        private fun shiftCoordinateY(y: Int): Int {
            return when (y) {
                0 -> y + 1
                image.height - 1 -> y - 1
                else -> y
            }
        }

        private fun calcGradientX(x: Int, y: Int): Int {
            val (r1, g1, b1) = image.myGetRGB(x - 1, y)
            val (r2, g2, b2) = image.myGetRGB(x + 1, y)
            val d1 = r2 - r1
            val d2 = g2 - g1
            val d3 = b2 - b1
            return d1 * d1 + d2 * d2 + d3 * d3
        }

        private fun calcGradientY(x: Int, y: Int): Int {
            val (r1, g1, b1) = image.myGetRGB(x, y - 1)
            val (r2, g2, b2) = image.myGetRGB(x, y + 1)
            val d1 = r2 - r1
            val d2 = g2 - g1
            val d3 = b2 - b1
            return d1 * d1 + d2 * d2 + d3 * d3
        }

    }

}