package watermark

import java.awt.Color
import java.awt.Transparency
import java.awt.image.BufferedImage
import java.io.File
import java.lang.Exception
import javax.imageio.ImageIO
import kotlin.math.sin

fun main() {
    try {
        var alphaChannel = false
        var transparency = false
        var transparencyColor = Color(0, 0, 0)
        var single = false

        println("Input the image filename:")
        val image = File(readln())
        if (checkImage(image, "image")) return

        println("Input the watermark image filename:")
        val watermark = File(readln())
        if (checkImage(watermark, "watermark")) return

        val myImage = ImageIO.read(image)
        val myWatermark = ImageIO.read(watermark)
        if (myImage.width < myWatermark.width || myImage.height < myWatermark.height) {
            return println("The watermark's dimensions are larger.")
        }

        if (myWatermark.transparency == Transparency.TRANSLUCENT) {
            println("Do you want to use the watermark's Alpha channel?")
            val choice = readln()
            alphaChannel = choice.equals("yes", true)
        }

        if (myWatermark.transparency != Transparency.TRANSLUCENT) {
            println("Do you want to set a transparency color?")
            val choice = readln()
            transparency = choice.equals("yes", true)
        }

        if (transparency) {
            println("Input a transparency color ([Red] [Green] [Blue]):")
            try {
                val colors = readln().split(" ").map { it.toInt() }
                if (colors.size != 3) throw Exception()
                transparencyColor = Color(colors[0], colors[1], colors[2])
            } catch (e: Exception) {
                println("The transparency color input is invalid.")
                return
            }
        }

        println("Input the watermark transparency percentage (Integer 0-100):")
        val weight = readln().let {
            if (it.matches("""\d+""".toRegex())) {
                it.toInt()
            } else {
                return println("The transparency percentage isn't an integer number.")
            }
        }.let { if (it in 0..100) it else return println("The transparency percentage is out of range.") }

        println("Choose the position method (single, grid):")
        single = readln().let {
            if (it.equals("single", true)) {
                true
            } else if (it.equals("grid", true)) {
                false
            } else {
                return println("The position method input is invalid.")
            }
        }

        var positionX = 0
        var positionY = 0
        if (single) {
            println("Input the watermark position " +
                    "([x 0-${myImage.width - myWatermark.width}] [y 0-${myImage.height - myWatermark.height}]):")
            val position = readln().let {
                if (it.matches("""-?\d+\s-?\d+""".toRegex())) {
                    it.split(" ")
                } else {
                    return println("The position input is invalid.")
                }
            }
            positionX = position[0].toInt().apply {
                if (this !in 0..(myImage.width - myWatermark.width)) {
                    return println("The position input is out of range.")
                }
            }
            positionY = position[1].toInt().apply {
                if (this !in 0..(myImage.height - myWatermark.height)) {
                    return println("The position input is out of range.")
                }
            }
        }

        println("Input the output image filename (jpg or png extension):")
        val output = File(readln().let {
            if (it.contains(".png") || it.contains(".jpg")) {
                it
            } else {
                return println("The output file extension isn't \"jpg\" or \"png\".")
            }
        }).apply { if (!this.exists()) this.createNewFile() }

        val myOutput = BufferedImage(myImage.width, myImage.height, BufferedImage.TYPE_INT_RGB)
        var x2 = 0
        var y2 = 0
        var singleX = myImage.width
        var singleY = myImage.height + 1

        for (x in 0 until myImage.width) {
            for (y in 0 until myImage.height) {
                if (x2 >= myWatermark.width) x2 = 0
                if (y2 >= myWatermark.height) y2 = 0
                if (x == positionX && y == positionY) {
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