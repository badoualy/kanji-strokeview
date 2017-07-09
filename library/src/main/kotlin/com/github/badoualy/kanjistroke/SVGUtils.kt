package com.github.badoualy.kanjistroke

import android.graphics.Path
import java.util.regex.Pattern

/**
 * Helper class to extract and convert an SVG input into a {@code Path} object.
 */
internal object SVGHelper {

    // Extract an instruction from an SVG command string
    private val svgInstructionPattern = Pattern.compile("([a-zA-Z])([^a-zA-Z]+)")
    // Extract coordinates
    private val svgCoordinatesPattern = Pattern.compile("-?\\d*\\.?\\d*")
    // Extract SVG path data fields from an SVG input
    private val svgPathPattern = Pattern.compile("""<path .*(?<= )d="([^"]+)".*/>""")

    /**
     * Extract the path data fields from the given input
     * @return the list of extracted path data fields values
     */
    fun extractPathData(input: String) = svgPathPattern.collect(input, 1)

    // see https://github.com/bigfishcat/svg-android/blob/master/src/main/java/com/larvalabs/svgandroid/SVGParser.java
    /**
     * Construct a {@code Path} object from the input pathData data
     * @param[pathData] a valid svg path data string
     * @return the constructed {@code Path} object
     * */
    fun buildPath(pathData: String): Path {
        val p = Path()
        try {
            val matcher = svgInstructionPattern.matcher(pathData)

            var lastX = 0f
            var lastY = 0f
            var lastX1 = 0f
            var lastY1 = 0f
            var subPathStartX = 0f
            var subPathStartY = 0f
            var wasCurve = false

            while (matcher.find()) {
                val command = matcher.group(1)[0]
                val coordinatesStr = matcher.group(2)
                //Log.v(TAG, "[$command]: $coordinatesStr")

                when (command.toLowerCase()) {
                    'm' -> coordinatesStr.split(',').let {
                        val x = it[0].toFloat()
                        val y = it[1].toFloat()
                        if (command.isUpperCase()) {
                            subPathStartX = x
                            subPathStartY = y
                            p.moveTo(x, y)
                            lastX = x
                            lastY = y
                        } else {
                            subPathStartX += x
                            subPathStartY += y
                            p.rMoveTo(x, y)
                            lastX += x
                            lastY += y
                        }
                    }
                    'l' -> coordinatesStr.split(',').let {
                        val x = it[0].toFloat()
                        val y = it[1].toFloat()
                        if (command.isUpperCase()) {
                            p.lineTo(x, y)
                            lastX = x
                            lastY = y
                        } else {
                            p.rLineTo(x, y)
                            lastX += x
                            lastY += y
                        }
                    }
                    'v' -> {
                        val coordinates = svgCoordinatesPattern.collect(coordinatesStr).map { it.toFloat() }
                        val y = coordinates[0]

                        if (command.isUpperCase()) {
                            p.lineTo(lastX, y)
                            lastY = y
                        } else {
                            p.rLineTo(0f, y)
                            lastY += y
                        }
                    }
                    'h' -> {
                        val coordinates = svgCoordinatesPattern.collect(coordinatesStr).map { it.toFloat() }
                        val x = coordinates[0]

                        if (command.isUpperCase()) {
                            p.lineTo(x, lastY)
                            lastX = x
                        } else {
                            p.rLineTo(x, 0f)
                            lastX += x
                        }
                    }
                    'c' -> {
                        wasCurve = true
                        val coordinates = svgCoordinatesPattern.collect(coordinatesStr).map { it.toFloat() }

                        var x1 = coordinates[0]
                        var y1 = coordinates[1]

                        var x2 = coordinates[2]
                        var y2 = coordinates[3]

                        var x = coordinates[4]
                        var y = coordinates[5]

                        if (command.isLowerCase()) {
                            x1 += lastX
                            x2 += lastX
                            x += lastX
                            y1 += lastY
                            y2 += lastY
                            y += lastY
                        }

                        p.cubicTo(x1, y1, x2, y2, x, y)

                        lastX1 = x2
                        lastY1 = y2
                        lastX = x
                        lastY = y
                    }
                    's' -> {
                        wasCurve = true
                        val coordinates = svgCoordinatesPattern.collect(coordinatesStr).map { it.toFloat() }

                        var x2 = coordinates[0]
                        var y2 = coordinates[1]

                        var x = coordinates[2]
                        var y = coordinates[3]

                        if (command.isLowerCase()) {
                            x2 += lastX
                            x += lastX
                            y2 += lastY
                            y += lastY
                        }

                        val x1 = 2 * lastX - lastX1
                        val y1 = 2 * lastY - lastY1

                        p.cubicTo(x1, y1, x2, y2, x, y)
                        lastX1 = x2
                        lastY1 = y2
                        lastX = x
                        lastY = y
                    }
                    'z' -> {
                        p.close()
                        p.moveTo(subPathStartX, subPathStartY)
                        lastX = subPathStartX
                        lastY = subPathStartY
                        lastX1 = subPathStartX
                        lastY1 = subPathStartY
                        wasCurve = true
                    }
                    else -> throw IllegalArgumentException("Unknown command $command for input $pathData")
                }
                if (!wasCurve) {
                    lastX1 = lastX
                    lastY1 = lastY
                }
            }
            return p
        } catch (e: Exception) {
            throw IllegalArgumentException("Incorrect input $pathData", e)
        }
    }

    /** Collect all the match at the given group index in the input string from this pattern */
    private fun Pattern.collect(input: String, groupIndex: Int = 0): List<String> {
        val matcher = matcher(input)
        val list = ArrayList<String>()
        while (matcher.find()) {
            val value = matcher.group(groupIndex)
            if (value.isNotBlank())
                list.add(value)
        }

        return list
    }
}