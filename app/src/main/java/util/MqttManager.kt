package util

import org.eclipse.paho.client.mqttv3.*
import java.io.ByteArrayOutputStream

class MqttManager : MqttCallback
{
    companion object
    {
        private const val host = "tcp://183.230.40.39:6002"
        private const val userName = "150983" // 产品Id
        private const val passWord = "SpringClient001" // 鉴权信息
        private const val clientId = "35075885" // 设备Id
        private const val valueTopic = "\$dp"

        val instance: MqttManager by lazy { MqttManager() }
    }

    private var mMqttClient: MqttClient? = null

    fun connect()
    {
        mMqttClient = MqttClient(host, clientId, null)
        val options = MqttConnectOptions()
        options.userName = userName
        options.password = passWord.toCharArray()
        options.isCleanSession = false
        options.mqttVersion = MqttConnectOptions.MQTT_VERSION_3_1_1
        mMqttClient!!.setCallback(this)
        Task.DelayHandler.post(MqttConnectTask(options, mMqttClient!!))
    }

    fun push(msg: String)
    {
        if (mMqttClient == null) {
            return
        }
        log(msg)
        val out = ByteArrayOutputStream()
        val len = msg.length
        out.write(0x01)
        out.write(len shr 8)
        out.write(len and 0xFF)
        out.write(msg.toByteArray())
        val mqttMsg = MqttMessage(out.toByteArray())
        mMqttClient!!.publish(valueTopic, mqttMsg)
    }

    override fun messageArrived(topic: String, message: MqttMessage)
    {
        log("messageArrived:$topic,${String(message.payload)}")
    }

    override fun connectionLost(cause: Throwable)
    {
        log("connectionLost")
        connect()
    }

    override fun deliveryComplete(token: IMqttDeliveryToken)
    {
        log("deliveryComplete")
    }

}

private class MqttConnectTask(val options: MqttConnectOptions, val mqttClient: MqttClient) : Runnable
{
    override fun run()
    {
        try {
            mqttClient.connect(options)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (mqttClient.isConnected) {
            log("MQTT连接成功")
            return
        }
        log("MQTT连接失败")
        Task.DelayHandler.postDelayed(this, 1000)
    }
}

