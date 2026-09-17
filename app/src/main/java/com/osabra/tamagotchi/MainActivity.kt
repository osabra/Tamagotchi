package com.osabra.tamagotchi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TamagotchiApp() }
    }
}

data class PetState(val hunger: Int = 80, val happiness: Int = 80, val energy: Int = 80, val hygiene: Int = 80)

@Composable
fun TamagotchiApp() {
    var pet by remember { mutableStateOf(PetState()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            pet = pet.copy(
                hunger = (pet.hunger - 2).coerceAtLeast(0),
                happiness = (pet.happiness - 1).coerceAtLeast(0),
                energy = (pet.energy - 1).coerceAtLeast(0),
                hygiene = (pet.hygiene - 1).coerceAtLeast(0)
            )
        }
    }
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFFFF7E8)) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Mi Mascota", fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text("Nivel de cuidado: ${listOf(pet.hunger, pet.happiness, pet.energy, pet.hygiene).average().toInt()}%")
                Spacer(Modifier.height(20.dp))
                Box(modifier = Modifier.size(230.dp).background(Color(0xFFFFDFA8), RoundedCornerShape(32.dp)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🐣", fontSize = 110.sp)
                        Text(if (pet.happiness > 30) "¡Hola!" else "Estoy triste…", fontSize = 22.sp)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Stat("🍎 Hambre", pet.hunger)
                Stat("❤️ Felicidad", pet.happiness)
                Stat("⚡ Energía", pet.energy)
                Stat("🛁 Higiene", pet.hygiene)
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionButton("🍎", "Comer") { pet = pet.copy(hunger = (pet.hunger + 20).coerceAtMost(100), happiness = (pet.happiness + 3).coerceAtMost(100)) }
                    ActionButton("🎾", "Jugar") { pet = pet.copy(happiness = (pet.happiness + 15).coerceAtMost(100), energy = (pet.energy - 8).coerceAtLeast(0)) }
                    ActionButton("🛁", "Limpiar") { pet = pet.copy(hygiene = 100, happiness = (pet.happiness + 5).coerceAtMost(100)) }
                    ActionButton("💤", "Dormir") { pet = pet.copy(energy = 100) }
                }
            }
        }
    }
}

@Composable fun Stat(label: String, value: Int) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label); Text("$value%") }
        LinearProgressIndicator(progress = { value / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp))
    }
}

@Composable fun ActionButton(icon: String, text: String, onClick: () -> Unit) {
    Button(onClick = onClick, shape = CircleShape, modifier = Modifier.size(76.dp), contentPadding = PaddingValues(2.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(icon, fontSize = 23.sp); Text(text, fontSize = 9.sp) }
    }
}
