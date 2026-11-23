package com.dlstudio.app.ui.topic

import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.dlstudio.app.content.Block
import com.dlstudio.app.content.Media
import com.dlstudio.app.content.MediaKind
import com.dlstudio.app.ui.components.ProgressBar
import com.dlstudio.app.ui.components.SettingCard
import com.dlstudio.app.ui.theme.LocalStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

private const val MAX_IMAGE_SIDE = 1600
private const val PROGRESS_TICK_MS = 250L

/** Картинка, звук или видео внутри страницы теории с подписью. Нет файла на устройстве — спокойное сообщение вместо ошибки. */
@Composable
fun MediaView(block: Block.Media, mediaDir: File?) {
    val s = LocalStrings.current
    val file = Media.local(mediaDir, block.src)
    val url = block.src.takeIf { Media.isUrl(it) && block.kind != MediaKind.IMAGE }
    Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp)) {
        when {
            file == null && url == null -> Text("⚠️ ${s.mediaMissing}: ${block.src}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            block.kind == MediaKind.IMAGE -> ImageBox(file!!, block.caption)
            block.kind == MediaKind.AUDIO -> AudioBox(file?.let { Uri.fromFile(it) } ?: Uri.parse(url!!), block.caption)
            else -> VideoBox(file?.let { Uri.fromFile(it) } ?: Uri.parse(url!!))
        }
        if (block.caption.isNotBlank() && block.kind != MediaKind.AUDIO) Text(block.caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ImageBox(file: File, caption: String) {
    var bitmap by remember(file) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(file) { bitmap = withContext(Dispatchers.IO) { decode(file) } }
    val shape = RoundedCornerShape(16.dp)
    bitmap?.let { Image(it, caption, Modifier.fillMaxWidth().clip(shape), contentScale = ContentScale.FillWidth) }
}

/** Читает картинку, уменьшая слишком большую, чтобы она не занимала лишнюю память. */
private fun decode(file: File): ImageBitmap? = runCatching {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.path, bounds)
    var sample = 1
    while (bounds.outWidth / sample > MAX_IMAGE_SIDE) sample *= 2
    BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample })?.asImageBitmap()
}.getOrNull()

/** Плеер звука: кнопка «играть / пауза» и полоса прогресса; проигрыватель создаётся при первом нажатии и освобождается, когда страница уходит с экрана. */
@Composable
private fun AudioBox(uri: Uri, caption: String) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    var player by remember(uri) { mutableStateOf<MediaPlayer?>(null) }
    var playing by remember(uri) { mutableStateOf(false) }
    var progress by remember(uri) { mutableFloatStateOf(0f) }
    DisposableEffect(uri) { onDispose { player?.release() } }
    LaunchedEffect(playing) {
        while (playing) {
            val p = player
            if (p != null && p.duration > 0) progress = p.currentPosition / p.duration.toFloat()
            delay(PROGRESS_TICK_MS)
        }
    }
    fun toggle() {
        val p = player ?: runCatching {
            MediaPlayer().apply {
                setDataSource(context, uri)
                setOnCompletionListener { playing = false; progress = 0f }
                prepare()
            }
        }.getOrNull()?.also { player = it } ?: return
        if (playing) { p.pause(); playing = false } else { p.start(); playing = true }
    }
    SettingCard(Modifier.fillMaxWidth(), ::toggle, 6.dp) {
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp), Alignment.CenterVertically) {
            Text(if (playing) "⏸" else "▶", Modifier.background(colors.primaryContainer, RoundedCornerShape(14.dp)).padding(horizontal = 14.dp, vertical = 8.dp), style = MaterialTheme.typography.titleLarge)
            Text(caption.ifBlank { "♪" }, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, maxLines = 2)
        }
        ProgressBar(progress)
    }
}

/** Видео со стандартными элементами управления Android. */
@Composable
private fun VideoBox(uri: Uri) {
    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                setMediaController(MediaController(ctx).also { it.setAnchorView(this) })
                setVideoURI(uri)
                seekTo(1)
            }
        },
        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(16.dp)),
        onRelease = { it.stopPlayback() }
    )
}
