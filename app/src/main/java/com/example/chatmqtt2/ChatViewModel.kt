package com.example.chatmqtt2

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

    var userName: String = "" // Nombre del usuario actual
    private var isConnected = false

    // Función para conectar al broker MQTT
    fun connect(context: Context) {
        if (::mqttClient.isInitialized && isConnected) {
            return // Ya está conectado
        }

        val clientId = MqttClient.generateClientId()
        mqttClient = MqttAndroidClient(context, brokerUrl, clientId)

        val options = MqttConnectOptions().apply {
            isCleanSession = true // No guarda sesiones anteriores
            connectionTimeout = 30
            keepAliveInterval = 60
        }

        // Escucha los eventos del servidor MQTT
        mqttClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                Log.e("MQTT", "Conexión perdida: ${cause?.message}")
                isConnected = false
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                message?.let {
                    val receivedMessage = it.toString()
                    // Solo agregamos el mensaje si no es nuestro propio mensaje
                    if (!receivedMessage.startsWith("$userName:")) {
                        addMessage(receivedMessage)
                    }
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Log.d("MQTT", "Mensaje entregado")
            }
        })

        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT", "Conectado exitosamente")
                    isConnected = true

                    // Nos suscribimos al canal
                    mqttClient.subscribe(topic, 1, null, object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            Log.d("MQTT", "Suscrito al topic: $topic")
                        }

                        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                            Log.e("MQTT", "Error al suscribirse: ${exception?.message}")
                        }
                    })
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e("MQTT", "Error al conectar: ${exception?.message}")
                    isConnected = false
                }
            })
        } catch (e: Exception) {
            Log.e("MQTT", "Excepción al conectar: ${e.message}")
        }
    }

    // Función para enviar mensaje al topic con nombre incluido
    fun sendMessage(message: String) {
        if (message.isBlank() || !isConnected) return

        val fullMessage = "$userName: $message"
        val mqttMessage = MqttMessage(fullMessage.toByteArray()).apply {
            qos = 1
        }

        try {
            mqttClient.publish(topic, mqttMessage, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    // Agregamos nuestro mensaje localmente
                    addMessage(fullMessage)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e("MQTT", "Error al enviar mensaje: ${exception?.message}")
                }
            })
        } catch (e: Exception) {
            Log.e("MQTT", "Excepción al enviar mensaje: ${e.message}")
        }
    }

    // Agrega mensaje a la lista actual (Compose lo detecta)
    private fun addMessage(msg: String) {
        _messages.value = _messages.value + msg
    }

    // Función para desconectar (opcional, para cleanup)
    fun disconnect() {
        if (::mqttClient.isInitialized && isConnected) {
            try {
                mqttClient.disconnect(null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        // Primero desconectado, luego limpiamos la conexión al servicio
                        mqttClient.unregisterResources()
                        mqttClient.close()
                        isConnected = false
                        Log.d("MQTT", "Desconectado y recursos liberados")
                    }
                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.e("MQTT", "Error al desconectar: ${exception?.message}")
                    }
                })
            } catch (e: Exception) {
                Log.e("MQTT", "Excepción al desconectar: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}