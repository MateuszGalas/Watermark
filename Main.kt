package watermark

import java.awt.Color
import java.awt.Transparency
import java.awt.image.BufferedImage
import java.io.File
import java.lang.Exception
import javax.imageio.ImageIO

fun main() {
    try {
        var alphaChannel = false
        println("Input the image filename:")
        val image = File(readln())
        if (checkImage(image, "image")) return

        println("Input the watermark image filename:")
        val watermark = File(readln())
        if (checkImage(watermark, "watermark")) return

        val myImage = ImageIO.read(image)
        val myWatermark = ImageIO.read(watermark)
        if (myImage.width != myWatermark.width || myImage.height != myWatermark.height) {
            return println("The image and watermark dimensions are different.")
        }

        if (myWatermark.transparency == Transparency.TRANSLUCENT) {
            println("Do you want to use the watermark's Alpha channel?")
            val choice = readln()
            alphaChannel = choice.equals("yes", true)
        }

        println("Input the watermark transparency percentage (Integer 0-100):")
        val weight = readln().let {
            if (it.matches("""\d+""".toRegex())) {
                it.toInt()
            } else {
                return println("The transparency percentage isn't an integer number.")
            }
        }.let { if (it in 0..100) it else return println("The transparency percentage is out of range.") }

        println("Input the output image filename (jpg or png extension):")
        val output = File(readln().let {
            if (it.contains(".png") || it.contains(".jpg")) {
                it
            } else {
                return println("The output file extension isn't \"jpg\" or \"png\".")
            }
        }).apply { if (!this.exists()) this.createNewFile() }

        val myOutput = BufferedImage(myImage.width, myImage.height, BufferedImage.TYPE_INT_RGB)

        for (x in 0 until myImage.width) {
            for (y in 0 until myImage.height) {
                val i = Color(myImage.getRGB(x, y))
                val w = Color(myWatermark.getRGB(x, y), alphaChannel)

                val color = Color(
                    (weight * w.red + (100 - weight) * i.red) / 100,
                    (weight * w.green + (100 - weight) * i.green) / 100,
                    (weight * w.blue + (100 - weight) * i.blue) / 100
                )

                myOutput.setRGB(x, y, color.rgb)
            }
        }

        ImageIO.write(myOutput, output.name.substring(output.name.lastIndex - 2), output)
        println("The watermarked image ${output.path} has been created.")
    } catch (e: NullPointerException) {
        return println(e.message)
    }
}

fun checkImage(image: File, choice: String): Boolean {
    val name = if (choice == "image") "image" else "watermark"

    if (!image.exists()) {
        println("The file ${image.path} doesn't exist.")
        return true
    }
    val myImage = ImageIO.read(image)


    if (myImage.colorModel.numColorComponents != 3) {
        println("The number of $name color components isn't 3.")
        return true
    }

    if (myImage.colorModel.pixelSize != 24 && myImage.colorModel.pixelSize != 32) {
        println("The $name isn't 24 or 32-bit.")
        return true
    }

    return false
}