package com.example.chatmqtt2

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun NameInputScreen(viewModel: ChatViewModel = viewModel()) {
    var name by remember { mutableStateOf("") }         // Guarda el nombre que el usuario escribe
    var startChat by remember { mutableStateOf(false) } // Controla si debe pasar a la pantalla del chat

    // Si ya ingresó nombre, mostramos el chat
    if (startChat) {
        ChatScreen(viewModel)
    } else {
        // Pantalla para ingresar el nombre
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Ingresa tu nombre")
            Spacer(modifier = Modifier.height(16.dp))

            // Input de texto para el nombre
            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Ej: Jordy") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para entrar al chat
            Button(onClick = {
                if (name.isNotBlank()) {
                    viewModel.userName = name  // Guardamos el nombre en el ViewModel
                    startChat = true           // Cambiamos la pantalla al chat
                }
            }) {
                Text("Entrar al chat")
            }
        }
    }
}

