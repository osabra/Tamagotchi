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
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.PI
import kotlin.random.Random
import io.github.sceneview.SceneView
import io.github.sceneview.SurfaceType
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberRenderer

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
    val stage = when {
        level < 3 -> 0
        level < 6 -> 1
        level < 10 -> 2
        level < 15 -> 3
        else -> 4
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(30.dp))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val sky = when (stage) {
                0 -> Brush.verticalGradient(listOf(Color(0xFFFFDDA9), Color(0xFFE99A6D)))
                1 -> Brush.verticalGradient(listOf(Color(0xFF72D1FF), Color(0xFF68B96D)))
                2 -> Brush.verticalGradient(listOf(Color(0xFFFFC88F), Color(0xFFB96A4F)))
                3 -> Brush.verticalGradient(listOf(Color(0xFF61C27D), Color(0xFF1F6047)))
                else -> Brush.verticalGradient(listOf(Color(0xFF7865D0), Color(0xFF1C336B)))
            }
            drawRect(sky)

            when (stage) {
                0 -> {
                    drawCircle(Color(0xFFFFF0BE), h * 0.16f, Offset(w * 0.79f, h * 0.17f))
                    drawCircle(Color(0xFFFFB47D), h * 0.035f, Offset(w * 0.15f, h * 0.20f))
                    drawCircle(Color(0xFFFFB47D), h * 0.025f, Offset(w * 0.25f, h * 0.28f))
                    drawRect(Color(0xFF8F5B46), Offset(0f, h * 0.77f), Size(w, h * 0.23f))
                    for (i in 0..4) {
                        val x = w * (0.06f + i * 0.23f)
                        drawCircle(Color(0xFFB56E50), h * 0.12f, Offset(x, h * 0.72f))
                        drawCircle(Color(0xFFCA805C), h * 0.08f, Offset(x - h * 0.02f, h * 0.68f))
                    }
                }
                1 -> {
                    drawCircle(Color(0x99FFF7C2), h * 0.20f, Offset(w * 0.15f, h * 0.18f))
                    drawCircle(Color(0xFFFFE36B), h * 0.028f, Offset(w * 0.15f, h * 0.17f))
                    drawRect(Color(0xFF5FA94F), Offset(0f, h * 0.75f), Size(w, h * 0.25f))
                    for (i in 0..5) {
                        val x = w * (0.06f + i * 0.19f)
                        drawRect(Color(0xFF765034), Offset(x, h * 0.62f), Size(w * 0.028f, h * 0.18f))
                        drawCircle(Color(0xFF276B4D), h * 0.115f, Offset(x, h * 0.64f))
                        drawCircle(Color(0xFF3D8E55), h * 0.10f, Offset(x + w * 0.055f, h * 0.60f))
                        drawCircle(Color(0xFF58AA5E), h * 0.07f, Offset(x + w * 0.03f, h * 0.54f))
                    }
                }
                2 -> {
                    drawCircle(Color(0xFFFFF0C0), h * 0.12f, Offset(w * 0.79f, h * 0.16f))
                    drawCircle(Color(0xFFFFB17A), h * 0.035f, Offset(w * 0.15f, h * 0.20f))
                    drawRect(Color(0xFF7D4F3D), Offset(0f, h * 0.77f), Size(w, h * 0.23f))
                    for (i in 0..3) {
                        val x = w * (0.07f + i * 0.29f)
                        drawCircle(Color(0xFF8F5441), h * 0.12f, Offset(x, h * 0.72f))
                        drawCircle(Color(0xFFAA654B), h * 0.08f, Offset(x - h * 0.02f, h * 0.68f))
                    }
                }
                3 -> {
                    drawCircle(Color(0x88E5FFCF), h * 0.17f, Offset(w * 0.14f, h * 0.18f))
                    drawCircle(Color(0xFF7ED59A), h * 0.10f, Offset(w * 0.25f, h * 0.23f))
                    drawRect(Color(0xFF23573D), Offset(0f, h * 0.77f), Size(w, h * 0.23f))
                    for (i in 0..4) {
                        val x = w * (0.03f + i * 0.24f)
                        drawCircle(Color(0xFF153D31), h * 0.125f, Offset(x, h * 0.69f))
                        drawCircle(Color(0xFF2D7650), h * 0.10f, Offset(x + w * 0.06f, h * 0.64f))
                        drawRect(Color(0xFF69452C), Offset(x + w * 0.035f, h * 0.65f), Size(w * 0.025f, h * 0.18f))
                    }
                }
                else -> {
                    drawCircle(Color(0xFFFFF7C6), h * 0.11f, Offset(w * 0.80f, h * 0.17f))
                    drawCircle(Color(0xFF7560C8), h * 0.09f, Offset(w * 0.76f, h * 0.15f))
                    for (p in listOf(0.07f to 0.13f, 0.23f to 0.24f, 0.40f to 0.12f, 0.61f to 0.26f, 0.89f to 0.12f)) {
                        drawCircle(Color(0xFFFFE9A8), h * 0.012f, Offset(w * p.first, h * p.second))
                    }
                    drawRect(Color(0xFF1B315E), Offset(0f, h * 0.77f), Size(w, h * 0.23f))
                    for (i in 0..5) {
                        drawCircle(Color(0xFF122A4A), h * 0.10f, Offset(w * (0.02f + i * 0.20f), h * 0.73f))
                    }
                }
            }
        }

        // SceneView 4.x: keep the 3D scene deliberately minimal so the build
        // is compatible with the stable API of 4.0.2. The GLB's embedded
        // animations are played automatically by ModelNode.
        val engine = rememberEngine()
        val modelLoader = rememberModelLoader(engine)
        val renderer = rememberRenderer(engine).apply {
            clearOptions.clear = true
            clearOptions.clearColor = floatArrayOf(0f, 0f, 0f, 0f)
        }
        val environmentLoader = rememberEnvironmentLoader(engine)
        val environment = rememberEnvironment(environmentLoader, isOpaque = false)
        val modelInstance = rememberModelInstance(modelLoader, "models/conejitos_pixar.glb")
        val visibleRange = when {
            level <= 1 -> 0 until 20
            level == 2 -> 20 until 40
            level == 3 -> 40 until 60
            else -> 60 until 80
        }

        SceneView(
            modifier = Modifier.fillMaxSize(),
            surfaceType = SurfaceType.TextureSurface,
            isOpaque = false,
            engine = engine,
            modelLoader = modelLoader,
            renderer = renderer,
            environment = environment,
            autoCenterContent = true,
            autoFitContent = true,
            framingPadding = 0.08f,
            cameraManipulator = null
        ) {
            modelInstance?.let { instance ->
                ModelNode(
                    modelInstance = instance,
                    scaleToUnits = 1.0f,
                    autoAnimate = true,
                    animationLoop = true,
                    animationSpeed = 1.0f,
                    apply = {
                        renderableNodes.forEachIndexed { index, node ->
                            node.isVisible = index in visibleRange
                        }
                    }
                )
            }
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
