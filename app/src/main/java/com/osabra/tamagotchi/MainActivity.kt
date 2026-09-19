package com.osabra.tamagotchi

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.max

private data class PetState(
    val hunger: Int = 80, val happiness: Int = 80, val energy: Int = 80, val hygiene: Int = 80,
    val coins: Int = 20, val xp: Int = 0, val level: Int = 1, val food: Int = 2, val toys: Int = 0, val wins: Int = 0
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
            p.getInt("coins", 20), p.getInt("xp", 0), p.getInt("level", 1), p.getInt("food", 2), p.getInt("toys", 0), p.getInt("wins", 0)
        ).let { it.copy(hunger=max(0,it.hunger), happiness=max(0,it.happiness), energy=max(0,it.energy), hygiene=max(0,it.hygiene)) }
    }
    fun save(s: PetState) = p.edit().putInt("hunger",s.hunger).putInt("happiness",s.happiness).putInt("energy",s.energy).putInt("hygiene",s.hygiene).putInt("coins",s.coins).putInt("xp",s.xp).putInt("level",s.level).putInt("food",s.food).putInt("toys",s.toys).putInt("wins",s.wins).putLong("last",System.currentTimeMillis()).apply()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { TamagotchiApp(PetStore(this)) } }
}

@Composable
private fun TamagotchiApp(store: PetStore) {
    var pet by remember { mutableStateOf(store.load()) }
    var message by remember { mutableStateOf("¡Hola! Soy tu mascota") }
    fun update(next: PetState, text: String) { pet=next; store.save(next); message=text }
    LaunchedEffect(Unit) { while(true){ delay(60_000); update(pet.copy(hunger=max(0,pet.hunger-2), happiness=max(0,pet.happiness-1), energy=max(0,pet.energy-1), hygiene=max(0,pet.hygiene-1)), "Necesito cuidados…") } }
    MaterialTheme {
        Surface(Modifier.fillMaxSize(), Color(0xFFFFF7E8)) {
            Column(Modifier.fillMaxSize().padding(18.dp), horizontalAlignment=Alignment.CenterHorizontally) {
                Text("Mi Mascota", fontSize=30.sp, fontWeight=FontWeight.Bold)
                Text("Nivel ${pet.level}   🪙 ${pet.coins}", fontSize=18.sp)
                Spacer(Modifier.height(12.dp))
                Box(Modifier.size(220.dp).background(Color(0xFFFFDFA8), RoundedCornerShape(32.dp)), Alignment.Center) { Column(horizontalAlignment=Alignment.CenterHorizontally){ Text(if(pet.level<3) "🐣" else if(pet.level<6) "🐥" else "🐰", fontSize=105.sp); Text(message) } }
                Spacer(Modifier.height(14.dp))
                Stat("🍎 Hambre",pet.hunger); Stat("❤️ Felicidad",pet.happiness); Stat("⚡ Energía",pet.energy); Stat("🛁 Higiene",pet.hygiene)
                Text("XP ${pet.xp}/100", Modifier.padding(top=5.dp))
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceEvenly) {
                    Action("🍎","Comer"){ if(pet.food > 0) update(pet.copy(food=pet.food-1,hunger=(pet.hunger+20).coerceAtMost(100),happiness=(pet.happiness+3).coerceAtMost(100),xp=pet.xp+5),"¡Ñam!") }
                    Action("🎾","Jugar"){ if(pet.energy>=8) update(pet.copy(happiness=(pet.happiness+15).coerceAtMost(100),energy=pet.energy-8,coins=pet.coins+3,xp=pet.xp+10),"¡Qué divertido!") else message="Estoy cansado…" }
                    Action("🛁","Limpiar"){ update(pet.copy(hygiene=100,happiness=(pet.happiness+5).coerceAtMost(100),xp=pet.xp+5),"¡Qué limpio!") }
                    Action("💤","Dormir"){ update(pet.copy(energy=100,xp=pet.xp+5),"Zzz…") }
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick={ if(pet.coins>=10) update(pet.copy(coins=pet.coins-10,hunger=(pet.hunger+35).coerceAtMost(100)),"¡Compraste comida!") else message="Necesito 10 monedas" }) { Text("🛒 Tienda · Comida 10🪙") }
                LaunchedEffect(pet.xp) { if(pet.xp>=100){ update(pet.copy(xp=pet.xp-100,level=pet.level+1),"¡He subido de nivel!") } }
            }
        }
    }
}

@Composable private fun Stat(label:String,value:Int){ Column(Modifier.fillMaxWidth().padding(vertical=2.dp)){ Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween){Text(label);Text("$value%")} LinearProgressIndicator({value/100f},Modifier.fillMaxWidth().height(7.dp)) } }
@Composable private fun Action(icon:String,text:String,onClick:()->Unit){ Button(onClick=onClick,Modifier.size(78.dp),shape=RoundedCornerShape(18.dp),contentPadding=PaddingValues(2.dp)){Column(horizontalAlignment=Alignment.CenterHorizontally){Text(icon,fontSize=23.sp);Text(text,fontSize=10.sp)}} }
