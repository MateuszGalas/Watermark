package watermark

import java.awt.Color
import java.awt.Transparency
import java.awt.image.BufferedImage
import java.io.File
import java.lang.Exception
import javax.imageio.ImageIO

class Watermark() {
    private var image = File("")
    private var watermark = File("")
    private var myImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)
    private var myWatermark = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)
    private var alphaChannel = false
    private var transparency = false
    private var transparencyColor = Color(0, 0, 0)
    private var single = false
    private var weight = 0
    private var startingPositionX = 0
    private var startingPositionY = 0
    private var output = File("")

    fun mergeImages() {
        try {
            println("Input the image filename:")
            image = File(readln())
            checkImage(image, "image")

            println("Input the watermark image filename:")
            watermark = File(readln())
            checkImage(watermark, "watermark")

            myImage = ImageIO.read(image)
            myWatermark = ImageIO.read(watermark)
            if (myImage.width < myWatermark.width || myImage.height < myWatermark.height) {
                return println("The watermark's dimensions are larger.")
            }

            checkAlphaChannel(myWatermark)
            if (transparency) setTransparencyColor()
            setTransparencyPercentage()
            positionMethod()
            if (single) single()
            output()
            merge()
        } catch (e: Exception) {
            println(e.message)
        }
    }

    private fun merge() {
        val myOutput = BufferedImage(myImage.width, myImage.height, BufferedImage.TYPE_INT_RGB)
        var x2 = 0
        var y2 = 0
        var singleX = myImage.width
        var singleY = myImage.height + 1

        for (x in 0 until myImage.width) {
            for (y in 0 until myImage.height) {
                if (x2 >= myWatermark.width) x2 = 0
                if (y2 >= myWatermark.height) y2 = 0
                if (x == startingPositionX && y == startingPositionY) {
                    singleY = 0
                    singleX = 0
                    x2 = 0
                    y2 = 0
                }


                val i = Color(myImage.getRGB(x, y))
                val w = Color(myWatermark.getRGB(x2, y2), alphaChannel)

                val color = Color(
                    (weight * w.red + (100 - weight) * i.red) / 100,
                    (weight * w.green + (100 - weight) * i.green) / 100,
                    (weight * w.blue + (100 - weight) * i.blue) / 100
                )

                if (alphaChannel) {
                    if (w.alpha == 0) {
                        myOutput.setRGB(x, y, Color(myImage.getRGB(x, y)).rgb)
                    } else if (single && (singleX >= myWatermark.width || singleY >= myWatermark.height)) {
                        myOutput.setRGB(x, y, Color(myImage.getRGB(x, y)).rgb)
                    } else {
                        myOutput.setRGB(x, y, color.rgb)
                    }
                } else {
                    if (transparency) {
                        if (w.rgb == transparencyColor.rgb) {
                            myOutput.setRGB(x, y, Color(myImage.getRGB(x, y)).rgb)
                        } else if (single && (singleX >= myWatermark.width || singleY >= myWatermark.height)) {
                            myOutput.setRGB(x, y, Color(myImage.getRGB(x, y)).rgb)
                        } else {
                            myOutput.setRGB(x, y, color.rgb)
                        }
                    } else {
                        myOutput.setRGB(x, y, color.rgb)
                    }
                }
                if (singleY == myImage.height) singleY = 0
                singleY++
                y2++
            }
            singleX++
            x2++
        }

        ImageIO.write(myOutput, output.name.substring(output.name.lastIndex - 2), output)
        println("The watermarked image ${output.path} has been created.")
    }
    private fun output() {
        println("Input the output image filename (jpg or png extension):")
        output = File(readln().let {
            if (it.contains(".png") || it.contains(".jpg")) {
                it
            } else {
                throw Exception("The output file extension isn't \"jpg\" or \"png\".")
            }
        }).apply { if (!this.exists()) this.createNewFile() }
    }
    private fun single() {
        println("Input the watermark position " +
                "([x 0-${myImage.width - myWatermark.width}] [y 0-${myImage.height - myWatermark.height}]):")
        val position = readln().let {
            if (it.matches("""-?\d+\s-?\d+""".toRegex())) {
                it.split(" ")
            } else {
                throw Exception("The position input is invalid.")
            }
        }
        startingPositionX = position[0].toInt().apply {
            if (this !in 0..(myImage.width - myWatermark.width)) {
                throw Exception("The position input is out of range.")
            }
        }
        startingPositionY = position[1].toInt().apply {
            if (this !in 0..(myImage.height - myWatermark.height)) {
                throw Exception("The position input is out of range.")
            }
        }
    }
    private fun positionMethod() {
        println("Choose the position method (single, grid):")
        single = readln().let {
            if (it.equals("single", true)) {
                true
            } else if (it.equals("grid", true)) {
                false
            } else {
                throw Exception("The position method input is invalid.")
            }
        }
    }
    private fun setTransparencyPercentage() {
        println("Input the watermark transparency percentage (Integer 0-100):")
        weight = readln().let {
            if (it.matches("""\d+""".toRegex())) {
                it.toInt()
            } else {
                throw Exception("The transparency percentage isn't an integer number.")
            }
        }.let { if (it in 0..100) it else throw Exception("The transparency percentage is out of range.") }
    }
    private fun setTransparencyColor() {
        println("Input a transparency color ([Red] [Green] [Blue]):")
        try {
            val colors = readln().split(" ").map { it.toInt() }
            if (colors.size != 3) throw Exception()
            transparencyColor = Color(colors[0], colors[1], colors[2])
        } catch (e: Exception) {
            throw Exception("The transparency color input is invalid.")
        }
    }
    private fun checkAlphaChannel(image: BufferedImage) {
        if (image.transparency == Transparency.TRANSLUCENT) {
            println("Do you want to use the watermark's Alpha channel?")
            val choice = readln()
             alphaChannel = choice.equals("yes", true)
        } else {
            println("Do you want to set a transparency color?")
            val choice = readln()
            transparency = choice.equals("yes", true)
        }
    }

    private fun checkImage(image: File, choice: String) {
        val name = if (choice == "image") "image" else "watermark"

        if (!image.exists()) {
            throw Exception("The file ${image.path} doesn't exist.")
        }
        val myImage = ImageIO.read(image)

        if (myImage.colorModel.numColorComponents != 3) {
            throw Exception("The number of $name color components isn't 3.")
        }

        if (myImage.colorModel.pixelSize != 24 && myImage.colorModel.pixelSize != 32) {
            throw Exception("The $name isn't 24 or 32-bit.")
        }
    }
}
fun main() {
    val watermark = Watermark()
    watermark.mergeImages()
}