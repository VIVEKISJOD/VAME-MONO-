package com.ameos.music

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// step71: the new Home / Search / Library building blocks (from the Lime and Nova design pictures).

private class HeroSlide(val title: String, val sub: String, val cover: String?, val play: () -> Unit)

// Big hero card ("Feel the Vibe"): cover, dark fade, title, one round accent play button, page dots.
// It changes by itself every 6 seconds between your mix, the song you heard last and your likes.
@Composable
fun HeroCarousel(vm: MainViewModel) {
    val rec = vm.recommended.toList()
    val liked = vm.liked.toList()
    val played = vm.played.toList()
    val slides = ArrayList<HeroSlide>()
    if (rec.isNotEmpty()) {
        slides.add(HeroSlide("Made for this moment", "A mix built around your taste", rec.first().cover) { vm.play(rec.first(), rec) })
    }
    if (played.isNotEmpty()) {
        val song = played.first()
        slides.add(HeroSlide("Pick up where you left off", artistName(song.artist), song.cover) { vm.play(song, played) })
    }
    if (liked.isNotEmpty()) {
        slides.add(HeroSlide("Your own collection", liked.size.toString() + " tracks you love", liked.first().cover) { vm.play(liked.first(), liked) })
    }
    var idx by remember { mutableIntStateOf(0) }
    LaunchedEffect(slides.size) {
        while (slides.size > 1) {
            delay(6500)
            idx = (idx + 1) % slides.size
        }
    }
    val wide = LocalConfiguration.current.screenWidthDp >= 600
    val h = if (wide) 360.dp else 310.dp
    val shape = RoundedCornerShape(28.dp)
    if (slides.isEmpty()) {
        Box(Modifier.fillMaxWidth().height(h).clip(shape).background(shimmerBrush()))
        return
    }
    val cur = idx.coerceIn(0, slides.lastIndex)
    val slide = slides[cur]
    val tick = rememberTick()
    Column(Modifier.fillMaxWidth()) {
        Box(
            Modifier.fillMaxWidth().height(h).clip(shape)
                .background(Color(0xFF171717))
                .border(1.dp, Color(0x22FFFFFF), shape)
                .clickable { tick(); slide.play() }
        ) {
            Crossfade(cur, animationSpec = tween(850), label = "monoHeroArtwork") { i ->
                Cover(slides.getOrNull(i)?.cover, Modifier.fillMaxSize(), RoundedCornerShape(0.dp), hiRes = true)
            }
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0f to Color(0x22000000),
                        0.34f to Color(0x12000000),
                        0.68f to Color(0x99000000),
                        1f to Color(0xF5000000)
                    )
                )
            )
            Column(
                Modifier.align(Alignment.TopStart).padding(start = 20.dp, top = 20.dp)
            ) {
                Text("V A M E   /   M O N O", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp, color = Color.White.copy(alpha = 0.86f))
                Text("THE DAILY EDIT", fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.7.sp, color = Color.White.copy(alpha = 0.62f),
                    modifier = Modifier.padding(top = 7.dp))
            }
            Column(
                Modifier.align(Alignment.BottomStart).padding(start = 20.dp, end = 88.dp, bottom = 22.dp)
            ) {
                Text(slide.title, fontSize = if (wide) 34.sp else 29.sp, lineHeight = 33.sp,
                    fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = (-0.8).sp,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(slide.sub, fontSize = 13.sp, color = Color(0xFFD0D0D0),
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 7.dp))
                Text("CURATED FOR YOU", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 1.6.sp, color = Color(0xFFB0B0B0),
                    modifier = Modifier.padding(top = 13.dp))
            }
            Box(
                Modifier.align(Alignment.BottomEnd).padding(end = 18.dp, bottom = 22.dp)
                    .size(56.dp).clip(CircleShape).background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PlayArrow, null, tint = Color(0xFF090909), modifier = Modifier.size(32.dp))
            }
        }
        if (slides.size > 1) {
            Row(Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                slides.forEachIndexed { i, _ ->
                    val w by animateDpAsState(if (i == cur) 22.dp else 5.dp, tween(280), label = "monoHeroDot")
                    Box(Modifier.padding(horizontal = 3.dp).size(width = w, height = 4.dp)
                        .clip(CircleShape).background(if (i == cur) Color.White else Color.White.copy(alpha = 0.25f)))
                }
            }
        }
    }
}

// "Made for You": wide cover cards with the name and "Mix . 50 songs" on a dark fade
@Composable
fun MadeForYou(vm: MainViewModel) {
    val mixes = vm.genreShelves.filter { it.songs.size >= 3 }.take(8)
    if (mixes.isEmpty()) return
    val w = shelfW()
    ShelfTitle("Made for You")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(mixes) { m ->
            val shape = RoundedCornerShape(St.cardR)
            Box(
                Modifier
                    .width(w)
                    .height(w * 0.78f)
                    .pressScale(0.97f)
                    .clip(shape)
                    .background(Pal.surface)
                    .border(1.dp, Stroke, shape)
                    .clickable { vm.play(m.songs.first(), m.songs) }
            ) {
                Cover(m.songs.first().cover, Modifier.fillMaxSize(), RoundedCornerShape(0.dp), hiRes = true)
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(0.4f to Color.Transparent, 1f to Color(0xE6050505))
                    )
                )
                Column(Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                    Text(
                        m.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "Mix • " + m.songs.size + " songs", fontSize = 11.sp, color = Color(0xB3FFFFFF),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// "Recently Played": a row of square covers
@Composable
fun RecentStrip(vm: MainViewModel) {
    val list = vm.played.toList().take(14)
    if (list.isEmpty()) return
    ShelfTitle("Recently Played")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(list) { s ->
            val on = vm.current?.url == s.url
            val shape = RoundedCornerShape(St.cardR)
            Cover(
                s.cover,
                Modifier
                    .size(84.dp)
                    .pressScale(0.95f)
                    .border(if (on) 2.dp else 1.dp, if (on) Color.White else Stroke, shape)
                    .clickable { vm.playOne(s, list) },
                shape
            )
        }
    }
}

// "Trending Now" rows on the Search page: cover, title, artist, round outlined play button
@Composable
fun TrendingList(vm: MainViewModel) {
    val list = vm.recommended.take(6)
    if (list.isEmpty()) return
    ShelfTitle("Trending Now")
    Column {
        list.forEach { s ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { vm.playOne(s, list) }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Cover(s.cover, Modifier.size(48.dp), RoundedCornerShape(12.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        s.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Pal.fg,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        artistName(s.artist), fontSize = 12.sp, color = Pal.textDim,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    Modifier.size(38.dp).clip(CircleShape).border(1.5.dp, Color.White.copy(alpha = 0.7f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

// Library list row: big cover, name, "148 songs", three dots
@Composable
fun LibListRow(cover: String?, title: String, sub: String, circle: Boolean = false, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .pressScale(0.985f)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Cover(cover, Modifier.size(62.dp), if (circle) CircleShape else RoundedCornerShape(St.coverR))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Pal.fg,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(sub, fontSize = 13.sp, color = Pal.textDim, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Filled.MoreVert, null, tint = Pal.muted)
    }
}

// wide outlined "+ New Playlist" button
@Composable
fun LibWideButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val shape = RoundedCornerShape(St.cardR)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .pressScale(0.98f)
            .clip(shape)
            .glassFill()
            .border(1.dp, Stroke, shape)
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Pal.fg, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Pal.fg)
    }
}

@Suppress("unused")
private val keepDp: Dp = 0.dp
