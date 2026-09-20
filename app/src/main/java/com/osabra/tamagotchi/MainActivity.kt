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
        Surface(Modifier.fillMaxSize(), Color(0xFFFFF7E8)) {
            Column(Modifier.fillMaxSize().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Mi Mascota", fontSize=30.sp, fontWeight=FontWeight.Bold)
                Text("Nivel " + pet.level + "   🪙 " + pet.coins, fontSize=18.sp)
                Spacer(Modifier.height(10.dp))
                PetScene(pet.level, pet.health)
                Text(message, fontSize=17.sp, fontWeight=FontWeight.Medium)
                Text("❤️ Salud " + pet.health + "%", fontSize=13.sp)
                Spacer(Modifier.height(12.dp))
                Stat("🍎 Hambre",pet.hunger)
                Stat("❤️ Felicidad",pet.happiness)
                Stat("⚡ Energía",pet.energy)
                Stat("🛁 Higiene",pet.hygiene)
                Text("⭐ XP " + pet.xp + "/100", Modifier.padding(top=5.dp))
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
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    Button(onClick={shopOpen=true}) { Text("🛒 Tienda") }
                    Button(onClick={secret=Random.nextInt(1,4); gameOpen=true}) { Text("🎮 Juego") }
                }
                Text("🍎 " + pet.food + "   🎾 " + pet.toys + "   💊 " + pet.medicine, fontSize=14.sp)
                Text("🏆 Partidas " + pet.games + " · Victorias " + pet.wins, fontSize=13.sp)
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
    val stage=when { level<3->0; level<6->1; level<10->2; level<15->3; else->4 }
    Canvas(Modifier.fillMaxWidth().height(285.dp)) {
        val w=size.width
        val h=size.height
        val bg=when(stage) {
            0->Brush.verticalGradient(listOf(Color(0xFF6D4327),Color(0xFFB87842)))
            1->Brush.verticalGradient(listOf(Color(0xFF79CFFF),Color(0xFF63AD58)))
            2->Brush.verticalGradient(listOf(Color(0xFFFFD7A1),Color(0xFFB66F4F)))
            3->Brush.verticalGradient(listOf(Color(0xFF75C96A),Color(0xFF286D47)))
            else->Brush.verticalGradient(listOf(Color(0xFF8871D2),Color(0xFF263A77)))
        }
        drawRoundRect(bg,Offset.Zero,Size(w,h),28.dp.toPx())
        if(stage==1||stage==3) for(i in 0..4) {
            val x=70f+i*(w-140f)/4f
            drawCircle(Color(0xFF3C7C43),38f,Offset(x,62f))
            drawRect(Color(0xFF7A4A2B),Offset(x-7f,72f),Size(14f,75f))
        }
        if(stage==2) {
            drawRect(Color(0xFF8B5A3C),Offset(0f,h-82f),Size(w,82f))
            drawRoundRect(Color(0xFFF0D0A0),Offset(w/2-130f,h-165f),Size(260f,90f),18.dp.toPx())
        }
        if(stage==0) {
            drawRect(Color(0xFF8B5A2B),Offset(0f,h-82f),Size(w,82f))
            drawOval(Color(0xFFE9D0A2),Offset(w/2-70f,h-145f),Size(140f,95f))
        }
        val cx=w/2
        val cy=h*0.59f
        val scale=when(stage){0->0.72f;1->0.78f;2->0.90f;3->1f;else->1.04f}
        val fur=when(stage){1->Color(0xFFF0E2C8);2->Color(0xFFD7C1A2);3->Color(0xFF9A633D);else->Color(0xFFF3EFE7)}
        if(stage==0) {
            drawOval(Color(0xFFE9D6A8),Offset(cx-48f,cy-55f),Size(96f,120f))
            drawOval(Color(0xFFB28B55),Offset(cx-37f,cy-45f),Size(74f,100f))
            drawOval(Color(0xFFFFF2D2),Offset(cx-23f,cy+5f),Size(46f,42f))
        } else {
            drawOval(fur,Offset(cx-72f*scale,cy-5f),Size(144f*scale,150f*scale))
            drawOval(fur,Offset(cx-58f*scale,cy-105f*scale),Size(116f*scale,120f*scale))
            drawOval(fur,Offset(cx-52f*scale,cy-200f*scale),Size(34f*scale,120f*scale))
            drawOval(fur,Offset(cx+18f*scale,cy-200f*scale),Size(34f*scale,120f*scale))
            drawOval(Color(0xFFFF9A98),Offset(cx-45f*scale,cy-188f*scale),Size(18f*scale,86f*scale))
            drawOval(Color(0xFFFF9A98),Offset(cx+25f*scale,cy-188f*scale),Size(18f*scale,86f*scale))
            drawOval(Color.White,Offset(cx-43f*scale,cy-95f*scale),Size(34f*scale,44f*scale))
            drawOval(Color.White,Offset(cx+9f*scale,cy-95f*scale),Size(34f*scale,44f*scale))
            drawCircle(Color(0xFF2B211C),10f*scale,Offset(cx-25f*scale,cy-75f*scale))
            drawCircle(Color(0xFF2B211C),10f*scale,Offset(cx+25f*scale,cy-75f*scale))
            drawCircle(Color.White,3.5f*scale,Offset(cx-21f*scale,cy-79f*scale))
            drawCircle(Color.White,3.5f*scale,Offset(cx+29f*scale,cy-79f*scale))
            drawOval(Color(0xFFFF8D86),Offset(cx-8f*scale,cy-52f*scale),Size(16f*scale,11f*scale))
            drawLine(Color(0xFF3A2A24),Offset(cx,cy-42f*scale),Offset(cx-10f*scale,cy-32f*scale),4f*scale)
            drawLine(Color(0xFF3A2A24),Offset(cx,cy-42f*scale),Offset(cx+10f*scale,cy-32f*scale),4f*scale)
            drawCircle(Color(0xFFF4F1EA),9f*scale,Offset(cx-61f*scale,cy+116f*scale))
            drawCircle(Color(0xFFF4F1EA),9f*scale,Offset(cx+61f*scale,cy+116f*scale))
            drawCircle(Color(0xFFFFB0B0),8f*scale,Offset(cx-58f*scale,cy+113f*scale))
            drawCircle(Color(0xFFFFB0B0),8f*scale,Offset(cx+58f*scale,cy+113f*scale))
        }
        if(health<30) drawCircle(Color(0xFFFFD54F),8f,Offset(cx+95f,cy-125f))
    }
}

@Composable
private fun Stat(label: String, value: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label)
            Text(text = "$value%")
        }
        Text(
            text = "█".repeat((value / 10).coerceIn(0, 10)) +
                "░".repeat((10 - value / 10).coerceIn(0, 10)),
            fontSize = 12.sp
        )
    }
}
@Composable private fun Action(icon:String,text:String,onClick:()->Unit){ Button(onClick=onClick,Modifier.size(78.dp),shape=RoundedCornerShape(18.dp),contentPadding=PaddingValues(2.dp)){Column(horizontalAlignment=Alignment.CenterHorizontally){Text(icon,fontSize=23.sp);Text(text,fontSize=10.sp)}} }
