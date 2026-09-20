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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.max
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
                Text("🐾  Mi Mascota  🐾", fontSize=28.sp, fontWeight=FontWeight.Bold)
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
private fun PetScene(level:Int, health:Int) {
    val stage = when {
        level < 3 -> 0
        level < 6 -> 1
        level < 10 -> 2
        level < 15 -> 3
        else -> 4
    }
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        val w = size.width
        val h = size.height
        val u = minOf(w / 360f, h / 300f)
        fun X(v:Float) = v * u + (w - 360f*u) / 2f
        fun Y(v:Float) = v * u + (h - 300f*u) / 2f
        fun S(v:Float) = v * u

        val sky = when(stage) {
            0 -> Brush.verticalGradient(listOf(Color(0xFFFFD69A), Color(0xFFF2A36F)))
            1 -> Brush.verticalGradient(listOf(Color(0xFF6FC9FF), Color(0xFF7BCB69)))
            2 -> Brush.verticalGradient(listOf(Color(0xFFFFC98A), Color(0xFFB9684E)))
            3 -> Brush.verticalGradient(listOf(Color(0xFF4CA96A), Color(0xFF1F6149)))
            else -> Brush.verticalGradient(listOf(Color(0xFF6551B7), Color(0xFF172A62)))
        }
        drawRoundRect(
            brush = sky,
            topLeft = Offset(X(0f), Y(0f)),
            size = Size(S(360f), S(300f)),
            cornerRadius = CornerRadius(S(30f), S(30f))
        )

        if (stage == 1) {
            drawCircle(Color(0x66FFFFFF), S(28f), Offset(X(65f),Y(55f)))
            drawCircle(Color(0x55FFFFFF), S(22f), Offset(X(290f),Y(70f)))
            drawCircle(Color(0x77FFFFFF), S(18f), Offset(X(315f),Y(42f)))
            for (i in 0..5) {
                val tx = 15f + i * 68f
                drawCircle(Color(0xFF3D824B), S(34f), Offset(X(tx),Y(185f)))
                drawCircle(Color(0xFF4D9554), S(27f), Offset(X(tx+25f),Y(178f)))
                drawRect(Color(0xFF76502D), Offset(X(tx+8f),Y(180f)), Size(S(10f),S(70f)))
            }
            drawRect(Color(0xFF6EAD4F), Offset(X(0f),Y(230f)), Size(S(360f),S(70f)))
        } else if (stage == 2) {
            drawCircle(Color(0xFFFFE4A6), S(28f), Offset(X(285f),Y(48f)))
            drawCircle(Color(0xFFFFB17A), S(8f), Offset(X(55f),Y(55f)))
            drawCircle(Color(0xFFFFB17A), S(8f), Offset(X(88f),Y(70f)))
            drawRect(Color(0xFF80513D), Offset(X(0f),Y(232f)), Size(S(360f),S(68f)))
            for (i in 0..3) {
                val tx = 25f + i * 95f
                drawCircle(Color(0xFF9B5B43), S(36f), Offset(X(tx),Y(215f)))
            }
        } else if (stage == 3) {
            drawCircle(Color(0xFFB6F2C1), S(45f), Offset(X(50f),Y(50f)))
            drawCircle(Color(0xFF7FD69B), S(30f), Offset(X(90f),Y(70f)))
            drawRect(Color(0xFF24583E), Offset(X(0f),Y(230f)), Size(S(360f),S(70f)))
            for (i in 0..4) {
                val tx=15f+i*82f
                drawCircle(Color(0xFF173F32),S(38f),Offset(X(tx),Y(205f)))
                drawCircle(Color(0xFF2D7650),S(30f),Offset(X(tx+22f),Y(190f)))
                drawRect(Color(0xFF69452C),Offset(X(tx+12f),Y(195f)),Size(S(9f),S(55f)))
            }
            drawCircle(Color(0xFFFFE9A8), S(3f), Offset(X(38f),Y(38f)))
            drawCircle(Color(0xFFFFE9A8), S(3f), Offset(X(315f),Y(62f)))
        } else if (stage == 4) {
            drawCircle(Color(0xFFFFF2B5), S(31f), Offset(X(286f),Y(50f)))
            drawCircle(Color(0xFF6551B7), S(27f), Offset(X(274f),Y(44f)))
            for (p in listOf(25f to 40f, 82f to 72f, 145f to 36f, 220f to 78f, 320f to 35f)) {
                drawCircle(Color(0xFFFFE9A8), S(2.5f), Offset(X(p.first),Y(p.second)))
                drawCircle(Color(0xFFFFE9A8), S(1.5f), Offset(X(p.first+18f),Y(p.second+18f)))
            }
            drawRect(Color(0xFF1B315E), Offset(X(0f),Y(232f)), Size(S(360f),S(68f)))
            for (i in 0..5) {
                val tx=5f+i*70f
                drawCircle(Color(0xFF122A4A),S(30f),Offset(X(tx),Y(220f)))
            }
        } else {
            drawCircle(Color(0xFFFFE4A6), S(30f), Offset(X(285f),Y(50f)))
            drawCircle(Color(0xFFFFC27A), S(7f), Offset(X(55f),Y(55f)))
            drawCircle(Color(0xFFFFB56D), S(6f), Offset(X(92f),Y(75f)))
            drawRect(Color(0xFF9A6449), Offset(X(0f),Y(232f)), Size(S(360f),S(68f)))
            for (i in 0..4) { val tx = 20f + i * 82f; drawCircle(Color(0xFFB16B4D), S(34f), Offset(X(tx),Y(215f))) }
        }

        // Character shadow
        drawOval(
            Color(0x55000000),
            Offset(X(115f),Y(244f)),
            Size(S(130f),S(22f))
        )

        val body = when(stage) {
            0 -> Color(0xFFF4D9B8)
            1 -> Color(0xFFF1E5D0)
            2 -> Color(0xFFE4D4BC)
            3 -> Color(0xFFD4A77E)
            else -> Color(0xFFF3F0E8)
        }
        val shade = when(stage) {
            0 -> Color(0xFFC28D63)
            1 -> Color(0xFFC5B7A2)
            2 -> Color(0xFFBCA58B)
            3 -> Color(0xFF9E704F)
            else -> Color(0xFFC9C3B8)
        }
        val cx=X(180f)
        val cy=Y(165f)
        val sc=when(stage){0->0.45f;1->0.52f;2->0.57f;3->0.60f;else->0.62f}

        // Feet behind body
        drawOval(shade,Offset(cx-S(72f*sc),cy+S(55f*sc)),Size(S(48f*sc),S(30f*sc)))
        drawOval(shade,Offset(cx+S(24f*sc),cy+S(55f*sc)),Size(S(48f*sc),S(30f*sc)))
        drawOval(Color(0xFFFFB1B4),Offset(cx-S(58f*sc),cy+S(62f*sc)),Size(S(20f*sc),S(10f*sc)))
        drawOval(Color(0xFFFFB1B4),Offset(cx+S(38f*sc),cy+S(62f*sc)),Size(S(20f*sc),S(10f*sc)))

        // Body
        drawOval(shade,Offset(cx-S(78f*sc),cy-S(4f*sc)),Size(S(156f*sc),S(145f*sc)))
        drawOval(body,Offset(cx-S(68f*sc),cy-S(12f*sc)),Size(S(136f*sc),S(137f*sc)))

        // Belly
        drawOval(Color(0xFFF8F4EA),Offset(cx-S(40f*sc),cy+S(25f*sc)),Size(S(80f*sc),S(83f*sc)))

        // Arms
        drawOval(body,Offset(cx-S(87f*sc),cy+S(12f*sc)),Size(S(34f*sc),S(80f*sc)))
        drawOval(body,Offset(cx+S(53f*sc),cy+S(12f*sc)),Size(S(34f*sc),S(80f*sc)))

        // Head
        drawOval(shade,Offset(cx-S(67f*sc),cy-S(112f*sc)),Size(S(134f*sc),S(128f*sc)))
        drawOval(body,Offset(cx-S(58f*sc),cy-S(120f*sc)),Size(S(116f*sc),S(118f*sc)))

        // Ears with depth
        drawOval(shade,Offset(cx-S(58f*sc),cy-S(220f*sc)),Size(S(48f*sc),S(118f*sc)))
        drawOval(shade,Offset(cx+S(10f*sc),cy-S(220f*sc)),Size(S(48f*sc),S(118f*sc)))
        drawOval(body,Offset(cx-S(51f*sc),cy-S(213f*sc)),Size(S(34f*sc),S(103f*sc)))
        drawOval(body,Offset(cx+S(17f*sc),cy-S(213f*sc)),Size(S(34f*sc),S(103f*sc)))
        drawOval(Color(0xFFFF9DA4),Offset(cx-S(43f*sc),cy-S(204f*sc)),Size(S(18f*sc),S(86f*sc)))
        drawOval(Color(0xFFFF9DA4),Offset(cx+S(25f*sc),cy-S(204f*sc)),Size(S(18f*sc),S(86f*sc)))

        // Muzzle, cheeks and eyes
        drawOval(Color(0xFFFDFBF5),Offset(cx-S(43f*sc),cy-S(65f*sc)),Size(S(86f*sc),S(62f*sc)))
        drawCircle(Color(0xFFFFC7C7), S(9f*sc), Offset(cx-S(45f*sc),cy-S(43f*sc)))
        drawCircle(Color(0xFFFFC7C7), S(9f*sc), Offset(cx+S(45f*sc),cy-S(43f*sc)))
        drawCircle(Color(0xFF2A2422),S(12f*sc),Offset(cx-S(25f*sc),cy-S(73f*sc)))
        drawCircle(Color(0xFF2A2422),S(12f*sc),Offset(cx+S(25f*sc),cy-S(73f*sc)))
        drawCircle(Color.White,S(4f*sc),Offset(cx-S(21f*sc),cy-S(78f*sc)))
        drawCircle(Color.White,S(4f*sc),Offset(cx+S(29f*sc),cy-S(78f*sc)))
        drawOval(Color(0xFFFF8C8C),Offset(cx-S(10f*sc),cy-S(52f*sc)),Size(S(20f*sc),S(13f*sc)))
        drawLine(Color(0xFF352722),Offset(cx,cy-S(40f*sc)),Offset(cx-S(11f*sc),cy-S(30f*sc)),S(3f))
        drawLine(Color(0xFF352722),Offset(cx,cy-S(40f*sc)),Offset(cx+S(11f*sc),cy-S(30f*sc)),S(3f))

        // Whiskers
        for (dy in listOf(-6f,6f)) {
            drawLine(Color(0xFF77736E),Offset(cx-S(45f*sc),cy+S(dy),),Offset(cx-S(82f*sc),cy+S(dy-8f)),S(2f))
            drawLine(Color(0xFF77736E),Offset(cx+S(45f*sc),cy+S(dy)),Offset(cx+S(82f*sc),cy+S(dy-8f)),S(2f))
        }

        if (stage >= 3) {
            drawCircle(Color(0xFFFFD95A),S(5f),Offset(cx-S(92f),cy-S(105f)))
            drawCircle(Color(0xFFFFD95A),S(5f),Offset(cx+S(92f),cy-S(105f)))
        }
        if (stage == 4) {
            drawCircle(Color(0xFF8D7BFF),S(7f),Offset(cx-S(104f),cy-S(70f)))
            drawCircle(Color(0xFF8D7BFF),S(7f),Offset(cx+S(104f),cy-S(70f)))
        }
        if (health < 30) {
            drawCircle(Color(0xFFFFD54F),S(9f),Offset(cx+S(92f),cy-S(120f)))
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
