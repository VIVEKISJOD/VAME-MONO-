package com.ameos.music

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.IconButton
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.layout.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ScreenPad = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 200.dp)

@Composable
fun ScreenTitle(text: String, trailing: @Composable () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = St.titleSp.sp, fontWeight = FontWeight.Bold, color = Pal.fg,
            letterSpacing = (-0.6).sp, modifier = Modifier.weight(1f))
        trailing()
    }
}

// Top right button: your Google photo when signed in, otherwise the plain account icon
@Composable
fun ProfileButton(vm: MainViewModel, size: androidx.compose.ui.unit.Dp, tappable: Boolean) {
    val photo = vm.ytPhoto
    val base = Modifier.size(size).clip(CircleShape)
    val mod = if (tappable) base.clickable { vm.showSettings = true } else base
    if (vm.ytSignedIn && photo != null) {
        Cover(photo, mod, CircleShape)
    } else {
        Icon(Icons.Filled.AccountCircle, null, tint = Pal.fg, modifier = mod)
    }
}

// Gear button: opens Settings (the profile picture is only a picture now)
@Composable
fun GearButton(vm: MainViewModel, size: androidx.compose.ui.unit.Dp = 44.dp) {
    Box(
        Modifier.size(size).clip(CircleShape).glassFill().clickable { vm.showSettings = true },
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Settings, "Settings", tint = Pal.fg, modifier = Modifier.size(size * 0.55f))
    }
}

internal fun songItem(vm: MainViewModel, s: Song, list: List<Song>): ShelfItem =
    ShelfItem(s.cover, s.title, "Song \u2022 " + artistName(s.artist), false, vm.current?.url == s.url) {
        vm.playOne(s, list)
    }

internal fun ytItems(vm: MainViewModel, sh: YtShelf): List<ShelfItem> = sh.cards.map { c ->
    ShelfItem(
        c.cover, c.title, c.sub, c.kind == "artist",
        c.kind == "song" && vm.current?.url == c.id
    ) { vm.openYtCard(c, sh.cards) }
}

// ---------------- HOME ----------------
// Everything on Home is a big cover card with the name under the picture (no tiny picture with
// a wide name). Shelves: Recents, YouTube Music shelves, your own songs, albums, genres, artists.

@Composable
fun HomeScreen(vm: MainViewModel) {
    LaunchedEffect(Unit) { vm.warmUp() }
    if (Net.on) { OfflineHome(vm); return }
    val width = LocalConfiguration.current.screenWidthDp
    val cardWidth = shelfW()
    val played = vm.played.toList()
    val picks = (if (vm.liked.isNotEmpty()) vm.liked.toList() else vm.recommended.take(12))
    val ytShelves = if (UiOpts.homeYt) vm.ytHome else emptyList()
    Box(Modifier.fillMaxSize().background(Color(0xFF080808))) {
        val tint = rememberCoverTint(vm.current?.cover ?: played.firstOrNull()?.cover)
        Box(Modifier.fillMaxWidth().height(430.dp).background(
            Brush.verticalGradient(listOf(tint.copy(alpha = 0.20f), Color.Transparent))))
        LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 190.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)) {
            item {
                Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 26.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("V A M E   /   M O N O", color = Color(0xFF8B8B8B), fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = 2.8.sp)
                        Text("SOUND, WITHOUT LIMITS.", color = Color.White, fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold, letterSpacing = 1.8.sp,
                            modifier = Modifier.padding(top = 9.dp))
                    }
                    GearButton(vm, 42.dp)
                }
            }
            item {
                Column(Modifier.padding(bottom = 20.dp)) {
                    Text("Your world.\nYour frequency.", color = Color.White, fontSize = 38.sp,
                        lineHeight = 41.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1.8).sp)
                    Text("A soundtrack for right now.", color = Color(0xFF929292), fontSize = 14.sp,
                        modifier = Modifier.padding(top = 9.dp))
                }
            }
            if (vm.current != null) item {
                val s = vm.current!!
                val shape = RoundedCornerShape(26.dp)
                Column(Modifier.fillMaxWidth().clip(shape)
                    .background(Color(0xFF171717)).border(1.dp, Color(0x20FFFFFF), shape)
                    .clickable { vm.showPlayer = true }.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ON YOUR FREQUENCY", color = Color(0xFF929292), fontSize = 9.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = Modifier.weight(1f))
                        Text("NOW PLAYING", color = Color.White, fontSize = 9.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Cover(s.cover, Modifier.size(88.dp), RoundedCornerShape(18.dp), hiRes = true)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(s.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                                maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(artistName(s.artist), color = Color(0xFF929292), fontSize = 13.sp,
                                modifier = Modifier.padding(top = 4.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        IconButton(onClick = { vm.toggle() }) {
                            Icon(if (vm.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                null, tint = Color.White, modifier = Modifier.size(30.dp))
                        }
                    }
                }
            }
            item { ShelfTitle("Made for this moment") }
            item {
                if (picks.isEmpty()) ShimmerShelf(cardWidth)
                else CardShelf(picks.take(12).map { songItem(vm, it, picks) }, cardWidth)
            }
            if (played.isNotEmpty()) {
                item { ShelfTitle("Pick up where you left off") }
                item { CardShelf(played.take(14).map { songItem(vm, it, played) }, cardWidth) }
            }
            if (vm.recommendedAlbums.isNotEmpty()) {
                item { ShelfTitle("Albums worth a listen") }
                item { CardShelf(vm.recommendedAlbums.map { a ->
                    ShelfItem(a.cover, a.title, "Album • " + a.artist) { vm.openAlbum(a) }
                }, cardWidth) }
            }
            val quick = vm.recommended.drop(4).take(12)
            if (quick.isNotEmpty()) {
                item { ShelfTitle("Quick picks") }
                item { CardShelf(quick.map { songItem(vm, it, quick) }, cardWidth) }
            }
            if (ytShelves.isNotEmpty()) {
                for (sh in ytShelves) {
                    item { ShelfTitle(sh.title, sh.sub) }
                    item { CardShelf(ytItems(vm, sh), cardWidth) }
                }
            } else if (UiOpts.homeYt && vm.ytHomeLoading) {
                item { ShelfTitle("Your YouTube Music") }
                item { ShimmerShelf(cardWidth) }
            }
            if (UiOpts.homeShelves && vm.genreShelves.isNotEmpty()) {
                for (sh in vm.genreShelves) {
                    item { ShelfTitle(sh.name) }
                    item { CardShelf(sh.songs.map { songItem(vm, it, sh.songs) }, cardWidth) }
                }
            }
            val artists = (vm.likedArtists + vm.recap().topArtists.map { ArtistItem(it.name, it.cover) })
                .distinctBy { it.name.lowercase() }
            if (artists.isNotEmpty()) {
                item { ShelfTitle("Your artists") }
                item { CardShelf(artists.take(18).map { a ->
                    ShelfItem(a.cover, a.name, "Artist", true) { vm.openArtist(a.name) }
                }, if (width >= 600) 150.dp else 118.dp) }
            }
        }
    }
}

// One calm card for the song you heard last: big cover, one button.
@Composable
private fun JumpBackIn(vm: MainViewModel) {
    val s = vm.played.firstOrNull() ?: return
    val tint = rememberCoverTint(s.cover)
    val tick = rememberTick()
    val playing = vm.current?.url == s.url
    val shape = RoundedCornerShape(24.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 22.dp)
            .pressScale(0.98f)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(tint.copy(alpha = if (Pal.light) 0.45f else 1f), if (Pal.light) Color(0x99FFFFFF) else Color(0x22FFFFFF))
                )
            )
            .border(1.dp, Stroke, shape)
            .clickable {
                tick()
                if (playing) vm.showPlayer = true else vm.play(s, vm.played.toList())
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Cover(s.cover, Modifier.size(84.dp), RoundedCornerShape(16.dp), hiRes = true)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                if (playing) "NOW PLAYING" else "JUMP BACK IN",
                fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp,
                color = Pal.textDim
            )
            Text(
                s.title, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Pal.fg,
                maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 3.dp)
            )
            Text(
                artistName(s.artist), fontSize = 13.sp, color = Pal.textDim,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier.size(52.dp).clip(CircleShape).background(Pal.fg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (playing && vm.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                null, tint = Pal.onFg, modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun HomeList(vm: MainViewModel) {
    LaunchedEffect(vm.recommended.size, vm.played.size) { vm.warmUp() }
    if (Net.on) {
        OfflineHome(vm)
        return
    }
    val big = LocalConfiguration.current.screenWidthDp >= 600
    val cw = shelfW()
    val cols = if (big) 5 else 2
    val ytShelves = if (UiOpts.homeYt) vm.ytHome else emptyList()

    // chips: 0 Everything, 1 For you, 2 Discover, 3 Artists (vm.homeFilter keeps the choice)
    val f = 0 // the chip row is gone (Dashboard Home); everything is shown
    val all = f == 0
    val forYou = f == 0 || f == 1
    val discover = f == 0 || f == 2
    val artistsOn = f == 0 || f == 3
    val recapNow = remember(vm.statMinutes, vm.statPlays) { vm.recap() }

    LazyColumn(contentPadding = ScreenPad) {
        item {
            val hour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
            val hi = if (hour < 5) "Late night" else if (hour < 12) "Good morning"
            else if (hour < 17) "Good afternoon" else if (hour < 22) "Good evening" else "Late night"
            val nm = vm.userName.ifBlank { vm.ytName }
            Column(
                Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "V A M E   /   M O N O", fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 2.4.sp,
                        color = Color(0xFF929292), modifier = Modifier.weight(1f)
                    )
                    GearButton(vm, 40.dp)
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    if (nm.isBlank()) "YOUR SOUND," else "WELCOME BACK,",
                    fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 2.2.sp, color = Color(0xFF777777)
                )
                Text(
                    if (nm.isBlank()) "in focus." else nm,
                    fontSize = 36.sp, lineHeight = 39.sp, fontWeight = FontWeight.Bold,
                    color = Color.White, letterSpacing = (-1.4).sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 3.dp)
                )
                Text(
                    "A soundtrack for right now.", fontSize = 13.sp,
                    color = Color(0xFF8A8A8A), modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
        // ---- Dashboard: tiles you use most, now playing strip, quick picks, mixes ----
        // step73: Spotify style opens with quick-play tiles; Material and Apple open with the big hero card
        if (Pal.style == 2) {
            item { DashGrid(vm) }
            item { MadeForYou(vm) }
            item { RecentStrip(vm) }
            item { DashQuickPicks(vm) }
        } else {
            item { HeroCarousel(vm) }
            item { MadeForYou(vm) }
            item { RecentStrip(vm) }
            item { ShelfTitle("Jump in") }
            item { DashGrid(vm) }
            item { DashQuickPicks(vm) }
        }
        if (forYou && vm.dials.isNotEmpty()) {
            item { SpeedDialShelf(vm) }
        }
        if (forYou) {
            // Recents: cards, and the grid button opens all of them as a grid
            if (UiOpts.homeLastAlbums && vm.played.isNotEmpty()) {
                item {
                    val showAll = remember { mutableStateOf(false) }
                    val rec = vm.played.toList()
                    val cards = rec.map { s -> songItem(vm, s, rec) }
                    Column(Modifier.animateContentSize()) {
                        ShelfTitle("Keep listening") {
                            Box(
                                Modifier.size(40.dp).clip(CircleShape).clickable { showAll.value = !showAll.value },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.GridView, null,
                                    tint = if (showAll.value) Pal.accent else Pal.fg,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        if (showAll.value) CardGrid(cards, cols) else CardShelf(cards.take(14), cw)
                    }
                }
            }
        }
        if (discover) {
            // first two YouTube Music shelves (Listen again, Quick picks...)
            for (sh in ytShelves.take(2)) {
                item { ShelfTitle(sh.title, sh.sub) }
                item { CardShelf(ytItems(vm, sh), cw) }
            }
        }
        if (forYou) {
            // songs you liked (or songs for you)
            item {
                ShelfTitle(if (vm.liked.isNotEmpty()) "Liked songs" else "Made for you") {
                    if (vm.liked.isNotEmpty()) {
                        Text(
                            "See all", fontSize = 13.sp, color = Pal.textDim,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { vm.likedOpen = true }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            item {
                val shelf = if (vm.liked.isNotEmpty()) vm.liked.toList() else vm.recommended.take(12)
                if (shelf.isEmpty()) {
                    ShimmerShelf(cw)
                } else {
                    CardShelf(shelf.map { s -> songItem(vm, s, shelf) }, cw)
                }
            }
            if (vm.recommendedAlbums.isNotEmpty()) {
                item { ShelfTitle("Albums for you") }
                item {
                    CardShelf(
                        vm.recommendedAlbums.map { a ->
                            ShelfItem(a.cover, a.title, "Album \u2022 " + a.artist) { vm.openAlbum(a) }
                        },
                        cw
                    )
                }
            }
            val picks = vm.recommended.drop(4).take(12)
            if (UiOpts.homeQuick && picks.isNotEmpty()) {
                item { ShelfTitle("Quick picks") }
                item { CardShelf(picks.map { s -> songItem(vm, s, picks) }, cw) }
            }
        }
        if (discover) {
            for (sh in (if (UiOpts.homeShelves) vm.genreShelves else emptyList())) {
                item { ShelfTitle(sh.name) }
                item { CardShelf(sh.songs.map { s -> songItem(vm, s, sh.songs) }, cw) }
            }
            // the rest of the YouTube Music shelves, or a loading / error state
            for (sh in ytShelves.drop(2)) {
                item { ShelfTitle(sh.title, sh.sub) }
                item { CardShelf(ytItems(vm, sh), cw) }
            }
            if (UiOpts.homeYt && vm.ytHome.isEmpty()) {
                if (vm.ytHomeLoading) {
                    item { ShelfTitle("Loading YouTube Music") }
                    item { ShimmerShelf(cw) }
                } else if (vm.ytHomeError != null) {
                    item {
                        EmptyState(
                            Icons.Filled.CloudOff, "Could not load YouTube Music shelves",
                            "Check your internet and try again.", "Try again"
                        ) { vm.loadYtHome() }
                    }
                }
            }
        }
        if (artistsOn) {
            val shown = ArrayList<ArtistItem>()
            shown.addAll(vm.likedArtists)
            for (a in recapNow.topArtists) {
                if (shown.none { it.name.equals(a.name, ignoreCase = true) }) shown.add(ArtistItem(a.name, a.cover))
            }
            if (UiOpts.homeArtists && shown.isNotEmpty()) {
                item { ShelfTitle("Your artists") }
                item {
                    CardShelf(
                        shown.take(20).map { a ->
                            ShelfItem(a.cover, a.name, "Artist", true) { vm.openArtist(a.name) }
                        },
                        if (big) 150.dp else 118.dp
                    )
                }
            }
            // artist cards that YouTube Music offers (Fans might also like...)
            for (sh in ytShelves) {
                val ar = sh.cards.filter { it.kind == "artist" }
                if (ar.size >= 3) {
                    item { ShelfTitle(sh.title) }
                    item {
                        CardShelf(
                            ar.map { c -> ShelfItem(c.cover, c.title, "Artist", true) { vm.openArtist(c.title) } },
                            if (big) 150.dp else 118.dp
                        )
                    }
                }
            }
            if (f == 3 && shown.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Filled.Person, "No artists yet",
                        "Like an artist or play some songs and they show up here."
                    )
                }
            }
        }
        if (discover && vm.suggestions.isNotEmpty()) {
            item { ShelfTitle("Suggested for you") }
            item {
                val sg = vm.suggestions.take(12)
                CardShelf(sg.map { s -> songItem(vm, s, vm.suggestions) }, cw)
            }
        }
    }
}

// ---------------- DASHBOARD HOME ----------------

private class DashT(
    val cover: String?,
    val icon: androidx.compose.ui.graphics.vector.ImageVector?,
    val title: String,
    val go: () -> Unit
)

@Composable
private fun DashTile(t: DashT, modifier: Modifier) {
    val tick = rememberTick()
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier
            .height(60.dp)
            .pressScale(0.97f)
            .clip(shape)
            .glassFill()
            .border(1.dp, Stroke, shape)
            .clickable {
                tick()
                t.go()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(60.dp).background(Pal.accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            if (t.cover != null) {
                Cover(t.cover, Modifier.fillMaxSize(), RoundedCornerShape(0.dp))
            } else if (t.icon != null) {
                Icon(t.icon, null, tint = Pal.accent, modifier = Modifier.size(26.dp))
            }
        }
        Text(
            t.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Pal.fg,
            maxLines = 2, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = 10.dp)
        )
    }
}

// 2 columns, up to 8 tiles: everything you use is one tap away
@Composable
private fun DashGrid(vm: MainViewModel) {
    val tiles = ArrayList<DashT>()
    tiles.add(DashT(null, Icons.Filled.Favorite, "Liked songs") { vm.likedOpen = true })
    val last = vm.played.firstOrNull()
    if (last != null) {
        tiles.add(DashT(last.cover, null, "Last played") { vm.play(last, vm.played.toList()) })
    }
    tiles.add(DashT(null, Icons.Filled.FileDownload, "Downloads") { vm.downloadsOpen = true })
    tiles.add(DashT(null, Icons.Filled.Sensors, "Radio") { vm.tab = 2 })
    for (pl in vm.playlists.take(4)) {
        tiles.add(DashT(pl.shownCover(), Icons.Filled.QueueMusic, pl.name) { vm.openPlaylist(pl.id) })
    }
    Column(
        Modifier.fillMaxWidth().padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        tiles.take(8).chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { t -> DashTile(t, Modifier.weight(1f)) }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

// slim glowing card, only while a song is loaded
@Composable
private fun DashNowPlaying(vm: MainViewModel) {
    val s = vm.current ?: return
    val tint = rememberCoverTint(s.cover)
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
            .pressScale(0.98f)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(tint.copy(alpha = if (Pal.light) 0.5f else 0.9f), if (Pal.light) Color(0x99FFFFFF) else Color(0x22FFFFFF))
                )
            )
            .border(1.dp, Stroke, shape)
            .clickable { vm.showPlayer = true }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Cover(s.cover, Modifier.size(48.dp), RoundedCornerShape(12.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "NOW PLAYING", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp,
                color = Pal.textDim
            )
            Text(
                s.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Pal.fg,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(Pal.fg).clickable { vm.toggle() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (vm.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                null, tint = Pal.onFg, modifier = Modifier.size(24.dp)
            )
        }
    }
}

// songs for you in swipeable columns of 4
@Composable
private fun DashQuickPicks(vm: MainViewModel) {
    val qp = vm.recommended.take(16)
    if (qp.isEmpty()) return
    val w = (LocalConfiguration.current.screenWidthDp * 0.84f).coerceAtMost(380f).dp
    ShelfTitle("Quick picks")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(qp.chunked(4)) { col ->
            Column(Modifier.width(w)) {
                col.forEach { s ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { vm.playOne(s, qp) }
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Cover(s.cover, Modifier.size(50.dp), RoundedCornerShape(10.dp))
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
                    }
                }
            }
        }
    }
}

// stacked-cover mix cards from your genre shelves
@Composable
private fun DashMixes(vm: MainViewModel) {
    val mixes = vm.genreShelves.filter { it.songs.size >= 3 }.take(8)
    if (mixes.isEmpty()) return
    ShelfTitle("Your mixes")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        items(mixes) { m ->
            Column(
                Modifier
                    .width(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { vm.play(m.songs.first(), m.songs) }
            ) {
                Box(Modifier.size(150.dp, 176.dp)) {
                    Cover(
                        m.songs[2].cover,
                        Modifier.align(Alignment.TopCenter).size(112.dp).graphicsLayer { alpha = 0.55f },
                        RoundedCornerShape(14.dp)
                    )
                    Cover(
                        m.songs[1].cover,
                        Modifier.align(Alignment.TopCenter).padding(top = 12.dp).size(130.dp).graphicsLayer { alpha = 0.8f },
                        RoundedCornerShape(14.dp)
                    )
                    Cover(
                        m.songs[0].cover,
                        Modifier.align(Alignment.BottomCenter).size(150.dp),
                        RoundedCornerShape(16.dp)
                    )
                }
                Text(
                    m.name + " mix", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Pal.fg,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

// shown instead of Home while there is no internet (Net.kt): only what works offline
@Composable
private fun OfflineHome(vm: MainViewModel) {
    LazyColumn(contentPadding = ScreenPad) {
        item {
            Row(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Offline", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Pal.fg, letterSpacing = (-1).sp)
                    Text("No internet. Your downloads and playlists still work.", fontSize = 13.sp, color = Pal.textDim)
                }
                GearButton(vm)
            }
        }
        if (vm.playlists.isNotEmpty()) {
            item { ShelfTitle("Your playlists") }
            item {
                CardShelf(
                    vm.playlists.map { pl ->
                        ShelfItem(
                            pl.shownCover(), pl.name,
                            pl.songs.count { vm.isDownloaded(it) }.toString() + " of " + pl.songs.size + " downloaded"
                        ) { vm.openPlaylist(pl.id) }
                    },
                    shelfW()
                )
            }
        }
        item { ShelfTitle("Downloaded songs") }
        if (vm.downloads.isEmpty()) {
            item {
                EmptyState(
                    Icons.Filled.CloudOff, "No downloads yet",
                    "Download songs when you have internet and they play here without it."
                )
            }
        } else {
            val list = vm.downloads.toList()
            items(list.size) { i -> SongRow(list[i], vm, list) }
        }
    }
}

// ---------------- SEARCH ----------------

private val TileColors = listOf(
    Color(0xFF3B3B3B), Color(0xFF2E5A6B), Color(0xFF8A2B6B), Color(0xFF5A3A12)
)

@Composable
private fun GenreTileView(tile: GenreTile, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(150.dp)
            .pressScale()
            .clip(RoundedCornerShape(18.dp))
            .background(color)
            .clickable { onClick() }
    ) {
        if (tile.covers.size >= 4) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.weight(1f)) {
                    Cover(tile.covers[0], Modifier.weight(1f).fillMaxHeight(), RoundedCornerShape(0.dp))
                    Cover(tile.covers[1], Modifier.weight(1f).fillMaxHeight(), RoundedCornerShape(0.dp))
                }
                Row(Modifier.weight(1f)) {
                    Cover(tile.covers[2], Modifier.weight(1f).fillMaxHeight(), RoundedCornerShape(0.dp))
                    Cover(tile.covers[3], Modifier.weight(1f).fillMaxHeight(), RoundedCornerShape(0.dp))
                }
            }
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0x22000000), Color(0xCC000000)))
            )
        )
        Column(Modifier.align(Alignment.BottomStart).padding(12.dp)) {
            Text(tile.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (tile.artists.isNotEmpty()) {
                Text(tile.artists, fontSize = 11.sp, color = Color(0xCCFFFFFF), maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun AlbumCard(a: Album, vm: MainViewModel) {
    Column(Modifier.width(140.dp).pressScale().clickable { vm.openAlbum(a) }) {
        Cover(a.cover, Modifier.size(140.dp), RoundedCornerShape(16.dp), hiRes = true)
        Text(a.title, fontSize = 14.sp, color = Pal.fg, maxLines = 1,
            overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
        Text(a.artist, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun SearchArtistCard(a: ArtistHit, vm: MainViewModel) {
    Column(
        Modifier.width(104.dp).pressScale().clickable { vm.openArtist(a.name) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Cover(a.cover, Modifier.size(104.dp), CircleShape, hiRes = true)
        Text(
            a.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Pal.fg,
            maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text("Artist", fontSize = 12.sp, color = Pal.muted)
    }
}

private data class Mood(val name: String, val sub: String, val query: String, val color: Color)

private val Moods = listOf(
    Mood("Chill", "Coffee shop blend", "chill coffee shop songs", Color(0xFF8FA8E0)),
    Mood("Commute", "Feel-good hip hop and R&B", "feel good hip hop r&b songs", Color(0xFFD9A93A)),
    Mood("Energize", "Pump-up pop", "pump up pop songs", Color(0xFFD6C36A)),
    Mood("Feel good", "Sunshine indie", "sunshine indie songs", Color(0xFF8FDB93)),
    Mood("Focus", "Lofi loft", "lofi focus beats", Color(0xFF9E9E9E)),
    Mood("Gaming", "Gaming hits", "gaming music mix", Color(0xFF707070)),
    Mood("Party", "Bollywood dance", "bollywood dance songs", Color(0xFF8E63D6)),
    Mood("Romance", "Bollywood romantic moments", "bollywood romantic songs", Color(0xFFB01E14)),
    Mood("Sad", "Bollywood melancholy", "bollywood sad songs", Color(0xFF7C7C7C)),
    Mood("Sleep", "Classical for sleeping", "calm piano sleep music", Color(0xFF6A3FC4))
)

// mood tile: cover with a colour wash and the name at the bottom left (like the design)
@Composable
private fun MoodTile(m: Mood, cover: String?, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(St.cardR)
    Box(
        modifier
            .height(96.dp)
            .appear()
            .pressScale(0.97f)
            .clip(shape)
            .clickable { onClick() }
    ) {
        Cover(cover, Modifier.fillMaxSize(), RoundedCornerShape(0.dp))
        Box(Modifier.fillMaxSize().background(m.color.copy(alpha = 0.42f)))
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(0.3f to Color.Transparent, 1f to Color(0xCC000000))
            )
        )
        Text(
            m.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White,
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 14.dp, bottom = 11.dp)
        )
        Box(Modifier.fillMaxSize().border(1.dp, GlassEdge, shape))
    }
}

// Explore (was Search): big title, search box, colourful mood tiles
@Composable
fun ExploreScreen(vm: MainViewModel) {
    LaunchedEffect(Unit) { vm.loadTiles() }
    BackHandler(enabled = vm.searched) { vm.clearSearch() }
    val cols = if (LocalConfiguration.current.screenWidthDp >= 600) 3 else 2
    val pool = (vm.recommended + vm.discover + vm.played.toList() + vm.liked.toList())
        .mapNotNull { it.cover }.distinct()
    val flt = remember { androidx.compose.runtime.mutableIntStateOf(0) }
    Box(Modifier.fillMaxSize().background(Color(0xFF080808))) {
        LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 190.dp)) {
            item {
                Row(Modifier.fillMaxWidth().padding(bottom = 22.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("FIND YOUR NEXT", color = Color(0xFF8C8C8C), fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = 2.5.sp)
                        Text("Sound.", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Bold,
                            letterSpacing = (-1.5).sp, modifier = Modifier.padding(top = 3.dp))
                    }
                    GearButton(vm, 42.dp)
                }
            }
            item {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF191919)).border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(18.dp))
                    .padding(horizontal = 15.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Search, null, tint = Color(0xFF9B9B9B), modifier = Modifier.size(23.dp))
                    TextField(value = vm.query, onValueChange = { vm.query = it },
                        placeholder = { Text("Tracks, artists, albums...", color = Color(0xFF777777)) },
                        trailingIcon = { if (vm.query.isNotEmpty()) IconButton(onClick = { vm.clearSearch() }) {
                            Icon(Icons.Filled.Close, null, tint = Color.White)
                        } },
                        singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { vm.search() }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.White, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.weight(1f))
                }
            }
            if (vm.searched) {
                item { KitChips(flt.intValue, listOf("All", "Songs", "Artists", "Albums")) { flt.intValue = it } }
                if (vm.loading) item { ShimmerShelf(shelfW()) }
                val top = vm.results.firstOrNull()
                if (flt.intValue == 0 && top != null) item {
                    Column(Modifier.fillMaxWidth().padding(top = 16.dp).clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF191919)).clickable { vm.playOne(top, vm.results) }.padding(15.dp)) {
                        Text("BEST MATCH", color = Color(0xFF8B8B8B), fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Cover(top.cover, Modifier.size(88.dp), RoundedCornerShape(16.dp), hiRes = true)
                            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                                Text(top.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(artistName(top.artist), color = Color(0xFF999999), fontSize = 13.sp,
                                    modifier = Modifier.padding(top = 4.dp))
                            }
                            Icon(Icons.Filled.PlayCircleFilled, null, tint = Color.White, modifier = Modifier.size(34.dp))
                        }
                    }
                }
                if ((flt.intValue == 0 || flt.intValue == 2) && vm.artistResults.isNotEmpty()) {
                    item { ShelfTitle("Artists") }
                    item { LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(vm.artistResults) { a -> SearchArtistCard(a, vm) }
                    } }
                }
                if ((flt.intValue == 0 || flt.intValue == 3) && vm.albumResults.isNotEmpty()) {
                    item { ShelfTitle("Albums") }
                    item { LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(vm.albumResults) { a -> AlbumCard(a, vm) }
                    } }
                }
                if (flt.intValue == 0 || flt.intValue == 1) {
                    item { ShelfTitle("Tracks") }
                    items(vm.results) { s -> SongRow(s, vm, vm.results, true) }
                }
            } else {
                if (vm.recentSearches.isNotEmpty()) {
                    item {
                        Row(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("RECENT SEARCHES", color = Color(0xFF8B8B8B), fontSize = 10.sp,
                                fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = Modifier.weight(1f))
                            Text("Clear", color = Color.White, fontSize = 12.sp,
                                modifier = Modifier.clickable { vm.clearRecent() }.padding(8.dp))
                        }
                    }
                    items(vm.recentSearches.toList()) { r ->
                        Row(Modifier.fillMaxWidth().clickable { vm.searchFor(r) }.padding(vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.History, null, tint = Color(0xFF888888))
                            Text(r, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f).padding(start = 12.dp))
                            Icon(Icons.Filled.NorthWest, null, tint = Color(0xFF777777))
                        }
                    }
                }
                item {
                    Text("EXPLORE BY MOOD", color = Color(0xFF8B8B8B), fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 2.sp,
                        modifier = Modifier.padding(top = 28.dp, bottom = 14.dp))
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        Moods.chunked(cols).forEachIndexed { r, row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                                row.forEachIndexed { c, m ->
                                    val idx = r * cols + c
                                    val cover = if (pool.isEmpty()) null else pool[(idx * 3) % pool.size]
                                    MoodTile(m, cover, Modifier.weight(1f)) { vm.searchFor(m.query) }
                                }
                                repeat(cols - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
                item { TrendingList(vm) }
                if (vm.tiles.isNotEmpty()) {
                    item { ShelfTitle("Your genres") }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            vm.tiles.chunked(2).forEachIndexed { r, pair ->
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    pair.forEachIndexed { c, t ->
                                        GenreTileView(t, TileColors[(r * 2 + c) % TileColors.size],
                                            Modifier.weight(1f)) { vm.searchFor(t.name + " music") }
                                    }
                                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- YOUR TYPE ----------------

@Composable
fun YourTypeScreen(vm: MainViewModel) {
    LaunchedEffect(Unit) {
        if (vm.discover.isEmpty()) vm.loadDiscover()
    }
    val collage = (vm.discover + vm.played + vm.liked + vm.recommended)
        .mapNotNull { it.cover }.distinct().take(8)
    LazyColumn(contentPadding = ScreenPad) {
        item {
            ScreenTitle("Your Type") {
                RoundIcon(Icons.Filled.Refresh) { vm.loadDiscover() }
            }
        }
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                if (collage.size >= 8) {
                    Column(Modifier.fillMaxSize().blur(18.dp).alpha(0.55f)) {
                        Row(Modifier.weight(1f)) {
                            for (i in 0 until 4) {
                                Cover(collage[i], Modifier.weight(1f).fillMaxHeight(), RoundedCornerShape(0.dp))
                            }
                        }
                        Row(Modifier.weight(1f)) {
                            for (i in 4 until 8) {
                                Cover(collage[i], Modifier.weight(1f).fillMaxHeight(), RoundedCornerShape(0.dp))
                            }
                        }
                    }
                }
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color(0x33000000), Color(0xAA0E0B0B)))
                    )
                )
                Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Discover new Music", fontSize = 26.sp, fontWeight = FontWeight.SemiBold,
                        color = Color.White, textAlign = TextAlign.Center)
                    Text("Picked for you", fontSize = 13.sp, color = Color(0xCCFFFFFF),
                        modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
        if (vm.discoverReason.isNotEmpty() && vm.discover.isNotEmpty()) {
            item {
                Text(
                    vm.discoverReason, fontSize = 13.sp, color = Pal.muted,
                    modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
                )
            }
        }
        if (vm.discover.isEmpty()) {
            item {
                Column(
                    Modifier.fillMaxWidth().padding(top = 50.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("We're ready, user.", fontSize = 16.sp, color = Pal.fg)
                    Spacer(Modifier.height(10.dp))
                    Box(
                        Modifier.size(48.dp).pressScale(0.9f).clip(CircleShape)
                            .background(Stroke)
                            .clickable { vm.loadDiscover() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.KeyboardArrowDown, null, tint = Pal.fg,
                            modifier = Modifier.size(30.dp))
                    }
                }
            }
        } else {
            items(vm.discover) { s -> SongRow(s, vm, vm.discover, true) }
        }
    }
}

// ---------------- LIBRARY ----------------

@Composable
private fun <T> TwoColumn(list: List<T>, cell: @Composable (T, Modifier) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        list.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { c -> cell(c, Modifier.weight(1f)) }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

// grid with big tiles: 2 columns on a phone, 4 on a tablet
@Composable
private fun <T> BigGrid(list: List<T>, cell: @Composable (T, Modifier) -> Unit) {
    val cols = if (LocalConfiguration.current.screenWidthDp >= 600) 4 else 2
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        list.chunked(cols).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                row.forEach { c -> cell(c, Modifier.weight(1f)) }
                repeat(cols - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun BigTile(
    modifier: Modifier, title: String, sub: String, cover: String?, onClick: () -> Unit
) {
    Column(modifier.clip(RoundedCornerShape(18.dp)).clickable { onClick() }) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f)) {
            Cover(cover, Modifier.fillMaxSize(), RoundedCornerShape(18.dp), hiRes = true)
            if (cover == null) {
                Icon(
                    Icons.Filled.LibraryMusic, null, tint = Pal.muted,
                    modifier = Modifier.align(Alignment.Center).size(44.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Pal.fg,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        Text(sub, fontSize = 12.sp, color = Pal.muted, maxLines = 1)
    }
}

@Composable
private fun LibItem(
    modifier: Modifier, title: String, sub: String?, cover: String?,
    circle: Boolean = false, onClick: () -> Unit
) {
    Row(modifier.clickable { onClick() }, verticalAlignment = Alignment.CenterVertically) {
        Cover(cover, Modifier.size(46.dp), if (circle) CircleShape else RoundedCornerShape(10.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, fontSize = 15.sp, color = Pal.fg, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (sub != null) {
                Text(sub, fontSize = 11.sp, color = Pal.muted, maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// ---- Library (stats cards, quick rows, big tiles) ----

private data class LibTile(
    val title: String,
    val sub: String,
    val cover: String?,
    val icon: androidx.compose.ui.graphics.vector.ImageVector?,
    val grad: List<Color>?,
    val onClick: () -> Unit
)

@Composable
private fun PlTile(t: LibTile, modifier: Modifier) {
    val shape = RoundedCornerShape(24.dp)
    Column(modifier.appear().pressScale(0.97f).clip(RoundedCornerShape(24.dp)).clickable { t.onClick() }) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(shape)
                .background(
                    if (t.grad != null) Brush.linearGradient(t.grad) else Brush.verticalGradient(listOf(Pal.surface2, Pal.surface))
                )
                .border(1.dp, GlassEdge, shape),
            contentAlignment = Alignment.Center
        ) {
            if (t.cover != null) {
                Cover(t.cover, Modifier.fillMaxSize(), shape, hiRes = true)
            } else if (t.icon != null) {
                Icon(t.icon, null, tint = if (t.grad != null) Color.White else Pal.fg, modifier = Modifier.size(46.dp))
            }
        }
        Text(
            t.title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Pal.fg,
            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 10.dp)
        )
        Text(t.sub, fontSize = 14.sp, color = Pal.textDim, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun LibRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, sub: String?, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { onClick() }
                .padding(vertical = 14.dp, horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Pal.fg, modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(22.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 21.sp, color = Pal.fg)
                if (sub != null) Text(sub, fontSize = 13.sp, color = Pal.muted)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = Pal.muted)
        }
        Box(Modifier.fillMaxWidth().padding(start = 52.dp).height(1.dp).background(Stroke))
    }
}

@Composable
private fun StatCard(width: androidx.compose.ui.unit.Dp, label: String, bigText: String, bigSize: Int,
                     caption: String, bottomLeft: String, bottomLeftSub: String, bottomRight: String?,
                     bottomRightSub: String?, onClick: () -> Unit) {
    val shape = RoundedCornerShape(30.dp)
    Box(
        Modifier
            .width(width)
            .height(210.dp)
            .pressScale(0.98f)
            .clip(shape)
            .background(Brush.linearGradient(listOf(Pal.accentDeep.copy(alpha = 0.55f), Pal.surface)))
            .glassFill()
            .border(1.dp, GlassEdge, shape)
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Text(
            label, fontSize = 12.sp, color = Pal.textDim, letterSpacing = 2.sp,
            fontWeight = FontWeight.SemiBold, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
        Column(Modifier.align(Alignment.CenterStart).padding(top = 10.dp)) {
            Text(
                bigText, fontSize = bigSize.sp, fontWeight = FontWeight.Bold, color = Pal.fg,
                maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = (bigSize + 2).sp
            )
            Text(
                caption, fontSize = 13.sp, color = Pal.textDim, letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
        Row(Modifier.align(Alignment.BottomStart).fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Text(
                    bottomLeft, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Pal.fg, maxLines = 1,
                    overflow = TextOverflow.Ellipsis, letterSpacing = 2.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Text(bottomLeftSub, fontSize = 13.sp, color = Pal.textDim, maxLines = 1)
            }
            if (bottomRight != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(bottomRightSub ?: "", fontSize = 10.sp, color = Pal.textDim, letterSpacing = 1.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    Text(bottomRight, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Pal.fg,
                        letterSpacing = 2.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun LibraryScreen(vm: MainViewModel) {
    LaunchedEffect(vm.ytSignedIn) {
        if (vm.ytSignedIn && vm.ytPlaylists.isEmpty()) vm.refreshYt()
    }
    val w = LocalConfiguration.current.screenWidthDp
    val tileSize = if (w >= 600) 190.dp else 160.dp
    val libTab = remember { androidx.compose.runtime.mutableIntStateOf(0) }
    val recap = remember(vm.statMinutes, vm.statPlays) { vm.recap() }
    val artists = (vm.likedArtists + recap.topArtists.map { ArtistItem(it.name, it.cover) })
        .distinctBy { it.name.lowercase() }
    val member = vm.userName.ifBlank { vm.ytName }.ifBlank { "VAME LISTENER" }
    Box(Modifier.fillMaxSize().background(Color(0xFF080808))) {
        LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 190.dp)) {
            item {
                Row(Modifier.fillMaxWidth().padding(bottom = 22.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("YOUR COLLECTION", color = Color(0xFF8D8D8D), fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = 2.5.sp)
                        Text("Library.", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Bold,
                            letterSpacing = (-1.5).sp, modifier = Modifier.padding(top = 3.dp))
                    }
                    IconButton(onClick = { vm.recapOpen = true }) {
                        Icon(Icons.Filled.AutoAwesome, null, tint = Color.White)
                    }
                    GearButton(vm, 42.dp)
                }
            }
            item {
                val shape = RoundedCornerShape(24.dp)
                Column(Modifier.fillMaxWidth().clip(shape).background(Color(0xFF171717))
                    .border(1.dp, Color(0x20FFFFFF), shape).clickable { vm.recapOpen = true }.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("YOUR LISTENING, IN NUMBERS", color = Color(0xFF999999), fontSize = 9.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp, modifier = Modifier.weight(1f))
                        Icon(Icons.Filled.ArrowOutward, null, tint = Color(0xFFAAAAAA), modifier = Modifier.size(17.dp))
                    }
                    Text(vm.statMinutes.toString(), color = Color.White, fontSize = 42.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = (-1.5).sp,
                        modifier = Modifier.padding(top = 8.dp))
                    Text("minutes listened", color = Color(0xFF999999), fontSize = 13.sp)
                    Spacer(Modifier.height(18.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(vm.statPlays.toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("PLAYS", color = Color(0xFF777777), fontSize = 9.sp, letterSpacing = 1.5.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(artists.firstOrNull()?.name ?: "Discover", color = Color.White, fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("TOP ARTIST", color = Color(0xFF777777), fontSize = 9.sp, letterSpacing = 1.5.sp)
                        }
                    }
                }
            }
            item {
                Text("YOUR MUSIC", color = Color(0xFF8B8B8B), fontSize = 10.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 28.dp, bottom = 12.dp))
                KitChips(libTab.intValue, listOf("Playlists", "Albums", "Artists", "Downloads")) {
                    if (it == 3) { vm.refreshDownloadBytes(); vm.downloadsOpen = true }
                    else libTab.intValue = it
                }
            }
            if (libTab.intValue == 0) {
                item {
                    LibWideButton("Create a playlist", Icons.Filled.Add) {
                        vm.editPlaylistId = null; vm.pendingSong = null; vm.showPlaylistDialog = true
                    }
                }
                item { LibListRow(null, "Liked Songs", vm.liked.size.toString() + " tracks") { vm.likedOpen = true } }
                items(vm.playlists.toList()) { p ->
                    LibListRow(p.shownCover(), p.name, p.songs.size.toString() + " tracks") { vm.openPlaylist(p.id) }
                }
                item {
                    LibWideButton("Import playlist", Icons.Filled.FileDownload) {
                        vm.importStatus = ""; vm.importedId = null; vm.showImportDialog = true
                    }
                }
                item { LibWideButton("Spotify library", Icons.Filled.Cloud) { SpotifyHub.open = true } }
            } else if (libTab.intValue == 1) {
                if (vm.savedAlbums.isEmpty()) item {
                    Text("Albums you save will appear here.", color = Color(0xFF888888),
                        fontSize = 13.sp, modifier = Modifier.padding(vertical = 18.dp))
                }
                items(vm.savedAlbums.toList()) { a ->
                    LibListRow(a.cover, a.title, a.artist.ifBlank { "Album" }) { vm.openAlbum(a) }
                }
            } else if (libTab.intValue == 2) {
                if (artists.isEmpty()) item {
                    Text("Artists you listen to will appear here.", color = Color(0xFF888888),
                        fontSize = 13.sp, modifier = Modifier.padding(vertical = 18.dp))
                }
                items(artists.take(50)) { a ->
                    LibListRow(a.cover, a.name, "Artist", circle = true) { vm.openArtist(a.name) }
                }
            }
            item {
                Text("QUICK ACCESS", color = Color(0xFF8B8B8B), fontSize = 10.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 28.dp, bottom = 8.dp))
                LibRow(Icons.Filled.Download, "Downloads", vm.downloads.size.toString() + " tracks available offline") {
                    vm.refreshDownloadBytes(); vm.downloadsOpen = true
                }
                LibRow(Icons.Filled.Favorite, "Liked songs", vm.liked.size.toString() + " tracks") { vm.likedOpen = true }
                LibRow(Icons.Filled.AutoAwesome, "Your listening recap", "Your top tracks and artists") { vm.recapOpen = true }
            }
            if (vm.ytSignedIn) {
                item {
                    Row(Modifier.fillMaxWidth().padding(top = 26.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("YouTube Music", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("Connected collection", color = Color(0xFF888888), fontSize = 12.sp)
                        }
                        Text(if (vm.ytBusy) "Loading" else "Refresh", color = Color.White, fontSize = 12.sp,
                            modifier = Modifier.clickable { vm.refreshYt() }.padding(8.dp))
                    }
                }
                item {
                    val tiles = ArrayList<LibTile>()
                    tiles.add(LibTile("Liked songs", "YouTube Music", null, Icons.Filled.ThumbUp, null) { vm.openYtLiked() })
                    vm.ytPlaylistsShown.forEach { yp ->
                        tiles.add(LibTile(yp.title, yp.subtitle.ifBlank { "Playlist" }, yp.cover,
                            Icons.Filled.LibraryMusic, null) { vm.openYtPlaylist(yp) })
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(tiles) { t -> PlTile(t, Modifier.width(tileSize)) }
                    }
                }
                if (vm.ytArtists.isNotEmpty()) {
                    item { ShelfTitle("Your YouTube artists") }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(vm.ytArtists.toList()) { a ->
                                PlTile(LibTile(a.name, "Artist", a.cover, Icons.Filled.Person, null) {
                                    vm.openArtist(a.name)
                                }, Modifier.width(tileSize))
                            }
                        }
                    }
                }
                if (vm.ytStatus.startsWith("Library load fail")) item {
                    Text(vm.ytStatus, color = Color(0xFFFF8A80), fontSize = 12.sp)
                }
            } else {
                item {
                    LibRow(Icons.Filled.AccountCircle, "Connect YouTube Music",
                        "Optional: access your personal music library") { vm.showLogin = true }
                }
            }
            if (vm.played.isNotEmpty()) {
                item { ShelfTitle("Recently played") }
                item {
                    val recent = vm.played.toList().take(14)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(recent) { s ->
                            PlTile(LibTile(s.title, artistName(s.artist), s.cover, Icons.Filled.MusicNote, null) {
                                vm.play(s, recent)
                            }, Modifier.width(tileSize))
                        }
                    }
                }
            }
        }
    }
}
