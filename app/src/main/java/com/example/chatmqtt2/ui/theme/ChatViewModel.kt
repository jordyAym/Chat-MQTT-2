package com.example.chatmqtt2.ui.theme

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages

    private lateinit var mqttClient: MqttAndroidClient
    private val brokerUrl = "tcp://broker.hivemq.com:1883" // Broker MQTT público
    private val topic = "chat/jordy_carlos" // Topic del chat compartido

    var userName: String = ""

    // Función para conectar al broker MQTT
    fun connect(context: Context) {
        val clientId = MqttClient.generateClientId()
        mqttClient = MqttAndroidClient(context, brokerUrl, clientId)

        val options = MqttConnectOptions().apply {
            isCleanSession = true
        }

        mqttClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                Log.e("MQTT", "Conexión perdida")
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                message?.let {
                    addMessage(it.toString()) // Ya viene con el nombre
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {}
        })

        mqttClient.connect(options, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                mqttClient.subscribe(topic, 1) // Nos suscribimos al canal
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.e("MQTT", "Error al conectar: ${exception?.message}")
            }
        })
    }

    // Función para enviar mensaje al topic con nombre incluido
    fun sendMessage(message: String) {
        if (message.isBlank()) return
        val fullMessage = "$userName: $message"
        val mqttMessage = MqttMessage(fullMessage.toByteArray())
        mqttClient.publish(topic, mqttMessage)
        addMessage(fullMessage) // Mostrar mensaje localmente
    }

    // Agrega mensaje a la lista actual (Compose lo detecta)
    private fun addMessage(msg: String) {
        _messages.value = _messages.value + msg
    }
}