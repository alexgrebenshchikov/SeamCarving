package seamcarving

import java.io.File
import java.lang.IllegalArgumentException
import javax.imageio.ImageIO


fun checkArgs(args: Array<String>) {
    when {
        args.size != 8 -> throw Exception("Expected 8 arguments!")
        args[0] != "-in" -> throw IllegalArgumentException("-in expected as first arg!")
        !args[1].endsWith(".png") -> throw IllegalArgumentException("Expected .png file as input!")
        args[2] != "-out" -> throw IllegalArgumentException("-out expected as third arg!")
        !args[3].endsWith(".png") -> throw IllegalArgumentException("Expected .png file as output!")
        args[4] != "-width" -> throw IllegalArgumentException("-width expected as fifth arg!")
        args[5].toIntOrNull() == null -> throw IllegalArgumentException("Expected number as six arg!")
        args[6] != "-height" -> throw IllegalArgumentException("-height expected as seventh arg!")
        args[7].toIntOrNull() == null -> throw IllegalArgumentException("Expected number as eighth arg!")
    }
}



fun main(args: Array<String>) {
    try {
        checkArgs(args)
        val image = ImageIO.read(File(args[1]))
        val sc = SeamCarving(image)
        val resImage = sc.getReducedImage(args[5].toInt(), args[7].toInt())
        ImageIO.write(resImage, "png", File(args[3]))
    } catch (e: Exception) {
        println(e.message)
    }
}
