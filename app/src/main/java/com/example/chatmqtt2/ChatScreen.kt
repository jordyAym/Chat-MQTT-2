package com.example.chatmqtt2

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val messages by viewModel.messages.collectAsState() // Observar mensajes
    val context = LocalContext.current
    var input by remember { mutableStateOf("") } // Estado del mensaje actual

    // Conectarse automáticamente al iniciar
    LaunchedEffect(Unit) {
        viewModel.connect(context)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Chat")
        Spacer(modifier = Modifier.height(8.dp))

        // Lista de mensajes
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            items(messages) { msg ->
                Text(msg)
            }
        }

        // Caja de entrada y botón de enviar
        Row {
            TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe un mensaje...") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                viewModel.sendMessage(input)
                input = "" // Limpiar campo
            }) {
                Text("Enviar")
            }
        }
    }
}