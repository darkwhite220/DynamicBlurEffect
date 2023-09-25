package earth.darkwhite.dynamicblureffect

import android.graphics.Bitmap
import android.graphics.Picture
import android.os.Build
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import earth.darkwhite.dynamicblureffect.ui.theme.DynamicBlurEffectTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      DynamicBlurEffectTheme {
        DynamicBlur()
      }
    }
  }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DynamicBlur() {
  val picture = remember { Picture() }
  var onPictureDraw by remember { mutableStateOf(false) }
  var offset by remember { mutableStateOf(IntOffset(0, 0)) }
  
  Box(modifier = Modifier.fillMaxSize()) {
    Image(
      painter = painterResource(id = R.drawable.a), contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .fillMaxSize()
        // 1
        .drawWithCache {
          onDrawWithContent {
            val pictureCanvas =
              Canvas(
                picture.beginRecording(
                  size.width.toInt(),
                  size.height.toInt()
                )
              )
            draw(this, this.layoutDirection, pictureCanvas, this.size) {
              this@onDrawWithContent.drawContent()
            }
            picture.endRecording()
            onPictureDraw = true
            drawIntoCanvas { canvas -> canvas.nativeCanvas.drawPicture(picture) }
          }
        }
        .pointerInteropFilter { motion ->
          offset = IntOffset(motion.x.toInt(), motion.y.toInt())
          true
        }
    )
    
    if (onPictureDraw) { // wait until picture is fully drawn
      // 2
      val bitmap = createBitmapFromPicture(picture)
      
      // 3
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) { // For backward compatibility
        val blurredBitmap = legacyBlurImage(
          bitmap = bitmap,
          blurRadio = 25f,
        )
        BlurImage(
          modifier = Modifier,
          bitmap = blurredBitmap,
          offset = offset
        )
      } else {
        BlurImage(
          modifier = Modifier.blur(25.dp),
          bitmap = bitmap,
          offset = offset
        )
      }
    }
  }
}

@Composable
private fun BlurImage(
  modifier: Modifier = Modifier,
  bitmap: Bitmap,
  offset: IntOffset
) {
  Canvas(modifier = modifier.fillMaxSize()) {
    val blurBoxSize = 200.dp.toPx().toInt()
    drawImage(
      image = bitmap.asImageBitmap(),
      srcOffset = offset,
      srcSize = IntSize(blurBoxSize, blurBoxSize),
      dstOffset = offset
    )
  }
}

@Suppress("DEPRECATION")
@Composable
private fun legacyBlurImage(
  bitmap: Bitmap,
  blurRadio: Float,
): Bitmap {
  val currentBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
  val renderScript = RenderScript.create(LocalContext.current)
  val bitmapAlloc = Allocation.createFromBitmap(renderScript, currentBitmap)
  ScriptIntrinsicBlur.create(renderScript, bitmapAlloc.element).apply {
    setRadius(blurRadio)
    setInput(bitmapAlloc)
    forEach(bitmapAlloc)
  }
  bitmapAlloc.copyTo(currentBitmap)
  renderScript.destroy()
  return currentBitmap
}

private fun createBitmapFromPicture(picture: Picture): Bitmap {
  val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    Bitmap.createBitmap(picture)
  } else {
    val bitmap = Bitmap.createBitmap(
      picture.width,
      picture.height,
      Bitmap.Config.ARGB_8888
    )
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)
    canvas.drawPicture(picture)
    bitmap
  }
  return bitmap
}