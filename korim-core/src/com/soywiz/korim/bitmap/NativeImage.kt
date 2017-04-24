package com.soywiz.korim.bitmap

import com.soywiz.korim.format.PNG
import com.soywiz.korim.format.nativeImageFormatProvider
import com.soywiz.korim.vector.Context2d
import com.soywiz.korio.crypto.Base64

abstract class NativeImage(width: Int, height: Int, val data: Any?) : Bitmap(width, height, 32) {
	abstract fun toNonNativeBmp(): Bitmap
	override fun swapRows(y0: Int, y1: Int) = throw UnsupportedOperationException()
	fun toBmp32(): Bitmap32 = toNonNativeBmp().toBMP32()
	open fun toUri(): String = "data:image/png;base64," + Base64.encode(PNG().encode(this, "out.png"))
	override fun toString(): String = this.javaClass.simpleName + "($width, $height)"
}

fun Bitmap.scaled(scale: Double): NativeImage {
	val out = NativeImage(Math.ceil(this.width * scale).toInt(), Math.ceil(this.height * scale).toInt())
	out.getContext2d(antialiasing = true).renderer.drawImage(this, 0, 0, out.width, out.height)
	return out
}

fun Bitmap.toUri(): String {
	if (this is NativeImage) return this.toUri()
	return "data:image/png;base64," + Base64.encode(PNG().encode(this, "out.png"))
}

fun NativeImage(width: Int, height: Int) = nativeImageFormatProvider.create(width, height)

fun NativeImage(width: Int, height: Int, d: Context2d.Drawable, scaleX: Double = 1.0, scaleY: Double = scaleX): NativeImage {
	val bmp = NativeImage(width, height)
	try {
		val ctx = bmp.getContext2d()
		ctx.keep {
			ctx.scale(scaleX, scaleY)
			ctx.draw(d)
		}
	} catch (e: Throwable) {
		e.printStackTrace()
	}
	return bmp
}

fun NativeImage(d: Context2d.SizedDrawable, scaleX: Double = 1.0, scaleY: Double = scaleX): NativeImage {
	return NativeImage((d.width * scaleX).toInt(), (d.height * scaleY).toInt(), d, scaleX, scaleY)
}

fun Bitmap.ensureNative() = when (this) {
	is NativeImage -> this
	else -> nativeImageFormatProvider.copy(this)
}

fun Context2d.SizedDrawable.raster(scaleX: Double = 1.0, scaleY: Double = scaleX) = NativeImage(this, scaleX, scaleY)