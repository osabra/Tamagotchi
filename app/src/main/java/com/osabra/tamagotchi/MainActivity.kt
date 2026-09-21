package com.osabra.tamagotchi

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.PI
import kotlin.random.Random

private data class PetState(
    val hunger: Int = 80, val happiness: Int = 80, val energy: Int = 80, val hygiene: Int = 80,
    val health: Int = 100, val coins: Int = 20, val xp: Int = 0, val level: Int = 1, val food: Int = 2, val toys: Int = 0, val medicine: Int = 0, val games: Int = 0, val wins: Int = 0
)

private class PetStore(context: Context) {
    private val p = context.getSharedPreferences("pet", Context.MODE_PRIVATE)
    fun load(): PetState {
        val now = System.currentTimeMillis()
        val last = p.getLong("last", now)
        val minutes = ((now - last) / 60_000L).coerceAtMost(24 * 60)
        return PetState(
            p.getInt("hunger", 80) - (minutes / 30).toInt(),
            p.getInt("happiness", 80) - (minutes / 45).toInt(),
            p.getInt("energy", 80) - (minutes / 60).toInt(),
            p.getInt("hygiene", 80) - (minutes / 40).toInt(),
            p.getInt("health", 100), p.getInt("coins", 20), p.getInt("xp", 0), p.getInt("level", 1), p.getInt("food", 2), p.getInt("toys", 0), p.getInt("medicine", 0), p.getInt("games", 0), p.getInt("wins", 0)
        ).let { it.copy(hunger=max(0,it.hunger), happiness=max(0,it.happiness), energy=max(0,it.energy), hygiene=max(0,it.hygiene)) }
    }
    fun save(s: PetState) = p.edit().putInt("hunger",s.hunger).putInt("happiness",s.happiness).putInt("energy",s.energy).putInt("hygiene",s.hygiene).putInt("health",s.health).putInt("coins",s.coins).putInt("xp",s.xp).putInt("level",s.level).putInt("food",s.food).putInt("toys",s.toys).putInt("medicine",s.medicine).putInt("games",s.games).putInt("wins",s.wins).putLong("last",System.currentTimeMillis()).apply()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { TamagotchiApp(PetStore(this)) } }
}

@Composable
private fun TamagotchiApp(store: PetStore) {
    var pet by remember { mutableStateOf(store.load()) }
    var message by remember { mutableStateOf("¡Hola! Soy tu mascota") }
    var shopOpen by remember { mutableStateOf(false) }
    var gameOpen by remember { mutableStateOf(false) }
    var secret by remember { mutableIntStateOf(Random.nextInt(1,4)) }
    fun update(next: PetState, text: String) { var n=next; if(n.xp>=100) n=n.copy(level=n.level+1,xp=n.xp-100); pet=n; store.save(n); message=text }
    LaunchedEffect(Unit) { while(true){ delay(60_000); update(pet.copy(hunger=max(0,pet.hunger-2), happiness=max(0,pet.happiness-1), energy=max(0,pet.energy-1), hygiene=max(0,pet.hygiene-1)), "Necesito cuidados…") } }
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFFFF7E8)) {
            Column(Modifier.fillMaxSize().padding(horizontal=12.dp, vertical=10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🐾  Mi Mascota  🐾", fontSize=28.sp, fontWeight=FontWeight.Bold, color=Color(0xFF15131A))
                Text("Nivel " + pet.level + "   🪙 " + pet.coins, fontSize=20.sp, fontWeight=FontWeight.Medium)
                Spacer(Modifier.height(10.dp))
                PetScene(pet.level, pet.health)
                Text(message, fontSize=18.sp, fontWeight=FontWeight.Bold)
                Text("❤️ Salud " + pet.health + "%", fontSize=15.sp)
                Spacer(Modifier.height(12.dp))
                Stat("🍎 Hambre",pet.hunger)
                Stat("❤️ Felicidad",pet.happiness)
                Stat("⚡ Energía",pet.energy)
                Stat("🛁 Higiene",pet.hygiene)
                Column(horizontalAlignment=Alignment.CenterHorizontally, modifier=Modifier.fillMaxWidth()) { Text("⭐ XP " + pet.xp + "/100", fontSize=18.sp, fontWeight=FontWeight.Medium); Spacer(Modifier.height(4.dp)); Box(Modifier.fillMaxWidth(0.65f).height(12.dp).background(Color(0xFFD6D1D8), RoundedCornerShape(8.dp))) { Box(Modifier.fillMaxWidth((pet.xp.coerceIn(0,100)/100f)).fillMaxHeight().background(Color(0xFFFFC107), RoundedCornerShape(8.dp))) } }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceEvenly) {
                    Action("🍎","Comer"){
                        if(pet.food>0) update(pet.copy(food=pet.food-1,hunger=(pet.hunger+25).coerceAtMost(100),happiness=(pet.happiness+3).coerceAtMost(100),health=(pet.health+2).coerceAtMost(100),xp=pet.xp+5),"¡Ñam! 😋")
                        else message="No tengo comida..."
                    }
                    Action("🎾","Jugar"){
                        if(pet.energy>=10) update(pet.copy(happiness=(pet.happiness+15).coerceAtMost(100),energy=pet.energy-10,coins=pet.coins+3,xp=pet.xp+10,games=pet.games+1),"¡Qué divertido! 🎉")
                        else message="Estoy cansado..."
                    }
                    Action("🛁","Limpiar"){ update(pet.copy(hygiene=100,happiness=(pet.happiness+5).coerceAtMost(100),health=(pet.health+3).coerceAtMost(100),xp=pet.xp+5),"¡Qué limpio! ✨") }
                    Action("💤","Dormir"){ update(pet.copy(energy=100,xp=pet.xp+5),"Zzz... 😴") }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.Center) {
                    Button(onClick={shopOpen=true}, modifier=Modifier.width(150.dp).height(52.dp), shape=RoundedCornerShape(28.dp)) { Text("🛒  Tienda", fontSize=17.sp) }
                    Spacer(Modifier.width(12.dp))
                    Button(onClick={secret=Random.nextInt(1,4); gameOpen=true}, modifier=Modifier.width(150.dp).height(52.dp), shape=RoundedCornerShape(28.dp)) { Text("🎮  Juego", fontSize=17.sp) }
                }
                Text("🍎 " + pet.food + "     🎾 " + pet.toys + "     💊 " + pet.medicine, fontSize=17.sp)
                Text("🏆 Partidas " + pet.games + " · Victorias " + pet.wins, fontSize=16.sp)
                if(shopOpen) {
                    AlertDialog(
                        onDismissRequest={shopOpen=false},
                        title={Text("🛒 Tienda · " + pet.coins + " 🪙")},
                        text={
                            Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
                                Button(onClick={if(pet.coins>=10) update(pet.copy(coins=pet.coins-10,food=pet.food+3),"¡+3 comidas! 🍎") else message="Necesito más monedas"}) { Text("🍎 3 comidas · 10 🪙") }
                                Button(onClick={if(pet.coins>=25) update(pet.copy(coins=pet.coins-25,toys=pet.toys+1),"¡Nuevo juguete! 🎾") else message="Necesito 25 monedas"}) { Text("🎾 Juguete · 25 🪙") }
                                Button(onClick={if(pet.coins>=30) update(pet.copy(coins=pet.coins-30,medicine=pet.medicine+1),"¡Medicina guardada! 💊") else message="Necesito 30 monedas"}) { Text("💊 Medicina · 30 🪙") }
                            }
                        },
                        confirmButton={TextButton(onClick={shopOpen=false}){Text("Cerrar")}}
                    )
                }
                if(gameOpen) {
                    val secret=remember { Random.nextInt(1,4) }
                    AlertDialog(
                        onDismissRequest={gameOpen=false},
                        title={Text("🎮 Adivina el número")},
                        text={
                            Column(horizontalAlignment=Alignment.CenterHorizontally) {
                                Text("Elige 1, 2 o 3. Si aciertas ganas 12 monedas.")
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                                    Button(onClick={gameOpen=false; if(secret==1) update(pet.copy(coins=pet.coins+12,happiness=(pet.happiness+10).coerceAtMost(100),xp=pet.xp+15,wins=pet.wins+1,games=pet.games+1),"¡Has ganado! 🏆") else update(pet.copy(games=pet.games+1),"¡Casi! 😄")}){Text("1")}
                                    Button(onClick={gameOpen=false; if(secret==2) update(pet.copy(coins=pet.coins+12,happiness=(pet.happiness+10).coerceAtMost(100),xp=pet.xp+15,wins=pet.wins+1,games=pet.games+1),"¡Has ganado! 🏆") else update(pet.copy(games=pet.games+1),"¡Casi! 😄")}){Text("2")}
                                    Button(onClick={gameOpen=false; if(secret==3) update(pet.copy(coins=pet.coins+12,happiness=(pet.happiness+10).coerceAtMost(100),xp=pet.xp+15,wins=pet.wins+1,games=pet.games+1),"¡Has ganado! 🏆") else update(pet.copy(games=pet.games+1),"¡Casi! 😄")}){Text("3")}
                                }
                            }
                        },
                        confirmButton={TextButton(onClick={gameOpen=false}){Text("Salir")}}
                    )
                }
            }
        }
    }
}

@Composable
private fun PetScene(level: Int, health: Int) {
    val transition = rememberInfiniteTransition(label = "pet3d")
    val breath by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "breath"
    )
    val sway by transition.animateFloat(
        initialValue = -2.8f, targetValue = 2.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "earSway"
    )
    val blinkPhase by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "blink"
    )
    val floatPhase by transition.animateFloat(
        initialValue = -1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "float"
    )

    val stage = when {
        level < 3 -> 0
        level < 6 -> 1
        level < 10 -> 2
        level < 15 -> 3
        else -> 4
    }

    Canvas(Modifier.fillMaxWidth().height(300.dp)) {
        val w = size.width
        val h = size.height
        val u = minOf(w / 360f, h / 300f)
        fun X(v: Float) = v * u + (w - 360f * u) / 2f
        fun Y(v: Float) = v * u + (h - 300f * u) / 2f
        fun S(v: Float) = v * u

        val sky = when (stage) {
            0 -> Brush.verticalGradient(listOf(Color(0xFFFFDDA9), Color(0xFFE99A6D)))
            1 -> Brush.verticalGradient(listOf(Color(0xFF72D1FF), Color(0xFF68B96D)))
            2 -> Brush.verticalGradient(listOf(Color(0xFFFFC88F), Color(0xFFB96A4F)))
            3 -> Brush.verticalGradient(listOf(Color(0xFF61C27D), Color(0xFF1F6047)))
            else -> Brush.verticalGradient(listOf(Color(0xFF7865D0), Color(0xFF1C336B)))
        }
        drawRoundRect(
            brush = sky,
            topLeft = Offset(X(0f), Y(0f)),
            size = Size(S(360f), S(300f)),
            cornerRadius = CornerRadius(S(30f), S(30f))
        )

        // Fondo evolutivo: más profundidad, luz y pequeños detalles en cada etapa.
        when (stage) {
            0 -> {
                drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF1BD), Color(0x00FFF1BD)), radius = S(42f)), S(31f), Offset(X(285f), Y(50f)))
                drawCircle(Color(0xFFFFB47D), S(8f), Offset(X(55f), Y(55f)))
                drawCircle(Color(0xFFFFB47D), S(6f), Offset(X(90f), Y(72f)))
                drawRect(Color(0xFF8F5B46), Offset(X(0f), Y(232f)), Size(S(360f), S(68f)))
                for (i in 0..4) {
                    drawCircle(Color(0xFFB56E50), S(34f), Offset(X(20f + i * 82f), Y(215f)))
                    drawCircle(Color(0xFFCA805C), S(24f), Offset(X(13f + i * 82f), Y(205f)))
                }
            }
            1 -> {
                // Jardín luminoso: más profundidad y acabado de videojuego 3D.
                drawCircle(Brush.radialGradient(listOf(Color(0xCCFFF7C2), Color(0x00FFF7C2)), radius = S(62f)), S(48f), Offset(X(58f), Y(50f)))
                drawCircle(Color(0xFFFFE36B), S(7f), Offset(X(58f), Y(48f)))
                // Nubes suaves.
                drawCircle(Color(0xBFFFFFFF), S(18f), Offset(X(128f), Y(38f)))
                drawCircle(Color(0xBFFFFFFF), S(24f), Offset(X(151f), Y(39f)))
                drawCircle(Color(0xBFFFFFFF), S(17f), Offset(X(176f), Y(42f)))
                drawCircle(Color(0xBFFFFFFF), S(15f), Offset(X(286f), Y(63f)))
                drawCircle(Color(0xBFFFFFFF), S(21f), Offset(X(307f), Y(62f)))
                // Línea de fondo y colinas.
                drawOval(Color(0xFF76C86A), Offset(X(-45f), Y(145f)), Size(S(230f), S(125f)))
                drawOval(Color(0xFF63B85F), Offset(X(175f), Y(142f)), Size(S(240f), S(128f)))
                // Árboles con varias capas para dar volumen.
                for (i in 0..5) {
                    val tx = 8f + i * 68f
                    drawRect(Color(0xFF765034), Offset(X(tx + 18f), Y(187f)), Size(S(10f), S(62f)))
                    drawCircle(Color(0xFF276B4D), S(34f), Offset(X(tx + 18f), Y(191f)))
                    drawCircle(Color(0xFF3D8E55), S(29f), Offset(X(tx + 38f), Y(178f)))
                    drawCircle(Color(0xFF58AA5E), S(21f), Offset(X(tx + 28f), Y(164f)))
                }
                // Césped y flores.
                drawRect(Color(0xFF5FA94F), Offset(X(0f), Y(226f)), Size(S(360f), S(74f)))
                for (x in listOf(22f, 72f, 120f, 252f, 305f, 340f)) {
                    drawCircle(Color(0xFFFFF2D0), S(4f), Offset(X(x), Y(246f)))
                    drawCircle(Color(0xFFFFA7A7), S(2.2f), Offset(X(x - 3f), Y(243f)))
                    drawCircle(Color(0xFFFFA7A7), S(2.2f), Offset(X(x + 3f), Y(243f)))
                }
                // Valla discreta detrás de la mascota.
                for (x in listOf(70f, 120f, 240f, 290f)) {
                    drawRoundRect(Color(0xFFB98255), Offset(X(x), Y(205f)), Size(S(28f), S(38f)), CornerRadius(S(4f), S(4f)))
                    drawRoundRect(Color(0xFFD19A68), Offset(X(x + 2f), Y(207f)), Size(S(24f), S(34f)), CornerRadius(S(3f), S(3f)))
                }
            }
            2 -> {
                drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF0C0), Color(0x00FFF0C0)), radius = S(38f)), S(28f), Offset(X(285f), Y(48f)))
                drawCircle(Color(0xFFFFB17A), S(8f), Offset(X(55f), Y(55f)))
                drawCircle(Color(0xFFFFB17A), S(7f), Offset(X(88f), Y(70f)))
                drawRect(Color(0xFF7D4F3D), Offset(X(0f), Y(232f)), Size(S(360f), S(68f)))
                for (i in 0..3) {
                    drawCircle(Color(0xFF8F5441), S(36f), Offset(X(25f + i * 95f), Y(215f)))
                    drawCircle(Color(0xFFAA654B), S(25f), Offset(X(17f + i * 95f), Y(205f)))
                }
            }
            3 -> {
                drawCircle(Brush.radialGradient(listOf(Color(0xA9E5FFCF), Color(0x00E5FFCF)), radius = S(60f)), S(45f), Offset(X(50f), Y(50f)))
                drawCircle(Color(0xFF7ED59A), S(30f), Offset(X(90f), Y(70f)))
                drawRect(Color(0xFF23573D), Offset(X(0f), Y(230f)), Size(S(360f), S(70f)))
                for (i in 0..4) {
                    val tx = 15f + i * 82f
                    drawCircle(Color(0xFF153D31), S(38f), Offset(X(tx), Y(205f)))
                    drawCircle(Color(0xFF2D7650), S(30f), Offset(X(tx + 22f), Y(190f)))
                    drawRect(Color(0xFF69452C), Offset(X(tx + 12f), Y(195f)), Size(S(9f), S(55f)))
                }
            }
            else -> {
                drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF7C6), Color(0x00FFF7C6)), radius = S(48f)), S(31f), Offset(X(286f), Y(50f)))
                drawCircle(Color(0xFF7560C8), S(27f), Offset(X(274f), Y(44f)))
                for (p in listOf(25f to 40f, 82f to 72f, 145f to 36f, 220f to 78f, 320f to 35f)) {
                    drawCircle(Color(0xFFFFE9A8), S(2.5f), Offset(X(p.first), Y(p.second)))
                    drawCircle(Color(0xFFFFE9A8), S(1.5f), Offset(X(p.first + 18f), Y(p.second + 18f)))
                }
                drawRect(Color(0xFF1B315E), Offset(X(0f), Y(232f)), Size(S(360f), S(68f)))
                for (i in 0..5) drawCircle(Color(0xFF122A4A), S(30f), Offset(X(5f + i * 70f), Y(220f)))
            }
        }

        // Mascota 3D de videojuego: respiración, flotación, orejas, parpadeo y brillo.
        val bob = sin((breath + 0.5f) * PI.toFloat()) * 2.2f + floatPhase * 0.6f
        val cx = X(180f)
        val cy = Y(164f + bob)
        // Mantener las proporciones y posiciones del personaje completas evita que el cuerpo se desmonte durante la animación.
        // La respiración se aplica de forma muy sutil, no escalando cada pieza por separado.
        val sc = 0.94f
        val sx = sc
        val sy = sc
        val earSway = sway * sc
        val blink = if (blinkPhase > 0.91f && blinkPhase < 0.965f) 0.16f else 1f

        // Sombra suave bajo la mascota.
        drawOval(
            brush = Brush.radialGradient(listOf(Color(0x66000000), Color(0x00000000)), radius = S(78f)),
            topLeft = Offset(X(102f), Y(239f)),
            size = Size(S(156f), S(30f))
        )

        val body = when (stage) {
            0 -> Color(0xFFF1D4B1)
            1 -> Color(0xFFF0E4CE)
            2 -> Color(0xFFE3D1B7)
            3 -> Color(0xFFD0A47A)
            else -> Color(0xFFF1EEE5)
        }
        val bodyDark = when (stage) {
            0 -> Color(0xFFC28C63)
            1 -> Color(0xFFC0B39F)
            2 -> Color(0xFFB99F83)
            3 -> Color(0xFF986B4A)
            else -> Color(0xFFC0BAAE)
        }
        val bodyLight = when (stage) {
            0 -> Color(0xFFFFE9CC)
            1 -> Color(0xFFFFF7E9)
            2 -> Color(0xFFF2E5D1)
            3 -> Color(0xFFE3BC92)
            else -> Color(0xFFFFFCF4)
        }

        fun oval3d(left: Float, top: Float, width: Float, height: Float, base: Color, dark: Color, light: Color) {
            val c = Offset(X(left + width * 0.30f), Y(top + height * 0.27f))
            drawOval(
                brush = Brush.radialGradient(
                    listOf(light, base, dark),
                    center = c,
                    radius = S(max(width, height) * 0.72f)
                ),
                topLeft = Offset(X(left), Y(top)),
                size = Size(S(width), S(height))
            )
        }

        // Cuerpo volumétrico.
        // Cuerpo redondeado y compacto, con volumen tipo personaje de videojuego 3D.
        oval3d(116f, 158f, 128f * sx, 132f * sy, body, bodyDark, bodyLight)
        oval3d(142f, 190f, 76f * sx, 72f * sy, Color(0xFFF7F2E7), Color(0xFFD9D0C1), Color.White)

        // Brazos y patas integrados visualmente con el cuerpo.
        oval3d(96f, 180f, 30f * sx, 66f * sy, body, bodyDark, bodyLight)
        oval3d(234f, 180f, 30f * sx, 66f * sy, body, bodyDark, bodyLight)
        oval3d(112f, 230f, 40f * sx, 25f * sy, bodyDark, bodyDark, bodyLight)
        oval3d(208f, 230f, 40f * sx, 25f * sy, bodyDark, bodyDark, bodyLight)
        drawOval(Color(0xFFFFA8AD), Offset(X(122f), Y(237f + bob)), Size(S(17f * sx), S(8f * sy)))
        drawOval(Color(0xFFFFA8AD), Offset(X(221f), Y(237f + bob)), Size(S(17f * sx), S(8f * sy)))

        // Cabeza algo más pequeña y proporcionada.
        oval3d(128f, 58f + bob, 104f * sx, 108f * sy, body, bodyDark, bodyLight)

        // Orejas 3D articuladas.
        withTransform({ rotate(earSway, Offset(cx - S(34f * sx), cy - S(160f * sy))) }) {
            oval3d(132f, 14f + bob, 38f * sx, 94f * sy, bodyDark, bodyDark, bodyLight)
            drawOval(Color(0xFFFFA2A8), Offset(X(142f), Y(22f + bob)), Size(S(17f * sx), S(72f * sy)))
            drawOval(Color(0x88FFFFFF), Offset(X(145f), Y(25f + bob)), Size(S(5f * sx), S(52f * sy)))
        }
        withTransform({ rotate(-earSway, Offset(cx + S(34f * sx), cy - S(160f * sy))) }) {
            oval3d(190f, 14f + bob, 38f * sx, 94f * sy, bodyDark, bodyDark, bodyLight)
            drawOval(Color(0xFFFFA2A8), Offset(X(201f), Y(22f + bob)), Size(S(17f * sx), S(72f * sy)))
            drawOval(Color(0x88FFFFFF), Offset(X(204f), Y(25f + bob)), Size(S(5f * sx), S(52f * sy)))
        }

        // Mejillas y hocico con iluminación suave.
        drawCircle(Color(0xFFFFC3C7), S(9f * sx), Offset(X(137f), Y(120f + bob)))
        drawCircle(Color(0xFFFFC3C7), S(9f * sx), Offset(X(223f), Y(120f + bob)))
        oval3d(140f, 105f + bob, 80f * sx, 55f * sy, Color(0xFFFDFBF5), Color(0xFFE2DDD3), Color.White)

        // Ojos grandes, expresivos y con reflejo.
        drawOval(
            brush = Brush.radialGradient(listOf(Color.White, Color(0xFFF4F4F0), Color(0xFF24201F))),
            topLeft = Offset(X(148f), Y(91f + bob)),
            size = Size(S(23f * sx), S(25f * sy * blink))
        )
        drawOval(
            brush = Brush.radialGradient(listOf(Color.White, Color(0xFFF4F4F0), Color(0xFF24201F))),
            topLeft = Offset(X(189f), Y(91f + bob)),
            size = Size(S(23f * sx), S(25f * sy * blink))
        )
        if (blink > 0.5f) {
            drawCircle(Color.White, S(4.5f * sx), Offset(X(154f), Y(97f + bob)))
            drawCircle(Color.White, S(4.5f * sx), Offset(X(195f), Y(97f + bob)))
            drawCircle(Color(0xAAFFFFFF), S(2f * sx), Offset(X(160f), Y(105f + bob)))
            drawCircle(Color(0xAAFFFFFF), S(2f * sx), Offset(X(201f), Y(105f + bob)))
        }

        // Nariz, boca y bigotes.
        drawOval(Color(0xFFFF8D91), Offset(X(171f), Y(116f + bob)), Size(S(18f * sx), S(12f * sy)))
        drawLine(Color(0xFF3B2B28), Offset(X(180f), Y(126f + bob)), Offset(X(171f), Y(135f + bob)), S(2.8f))
        drawLine(Color(0xFF3B2B28), Offset(X(180f), Y(126f + bob)), Offset(X(189f), Y(135f + bob)), S(2.8f))
        for (dy in listOf(114f, 124f)) {
            drawLine(Color(0xFF77736E), Offset(X(139f), Y(dy + bob)), Offset(X(105f), Y(dy - 7f + bob)), S(2f))
            drawLine(Color(0xFF77736E), Offset(X(221f), Y(dy + bob)), Offset(X(255f), Y(dy - 7f + bob)), S(2f))
        }

        // Brillo ambiental de personaje y partículas de evolución.
        if (stage >= 3) {
            drawCircle(Color(0xFFFFD95A), S(5f), Offset(X(91f), Y(74f + bob)))
            drawCircle(Color(0xFFFFD95A), S(5f), Offset(X(269f), Y(74f + bob)))
        }
        if (stage == 4) {
            drawCircle(Color(0xFF9B8AFF), S(7f), Offset(X(90f), Y(110f + bob)))
            drawCircle(Color(0xFF9B8AFF), S(7f), Offset(X(270f), Y(110f + bob)))
            drawCircle(Color(0xFFFFE7A0), S(2.5f), Offset(X(110f), Y(55f + bob)))
            drawCircle(Color(0xFFFFE7A0), S(2.5f), Offset(X(250f), Y(52f + bob)))
        }
        if (health < 30) {
            drawCircle(Color(0xFFFFD54F), S(9f), Offset(X(272f), Y(34f + bob)))
        }
    }
}

@Composable
private fun Stat(label: String, value: Int) {
    Column(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 17.sp)
            Text("$value%", fontSize = 17.sp)
        }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth().height(13.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(10) { i ->
                Box(Modifier.weight(1f).fillMaxHeight().background(if (i < value / 10) Color(0xFF48C96B) else Color(0xFFE1DDD8), RoundedCornerShape(2.dp)))
            }
        }
    }
}
@Composable
private fun Action(icon:String,text:String,onClick:()->Unit) {
    Button(onClick=onClick, Modifier.width(82.dp).height(104.dp), shape=RoundedCornerShape(20.dp), contentPadding=PaddingValues(4.dp)) {
        Column(horizontalAlignment=Alignment.CenterHorizontally, verticalArrangement=Arrangement.Center) {
            Text(icon, fontSize=30.sp)
            Spacer(Modifier.height(5.dp))
            Text(text, fontSize=13.sp)
        }
    }
}
