package no.nav.sosialhjelp.fagsystem.fiksio

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import org.slf4j.LoggerFactory
import java.io.Closeable

/**
 * Manages AMQP (RabbitMQ) connection and subscriptions for Fiks IO
 */
internal class AmqpConnection(
    private val konfigurasjon: AmqpKonfigurasjon
) : Closeable {
    
    private val logger = LoggerFactory.getLogger(AmqpConnection::class.java)
    private var connection: Connection? = null
    private var channel: Channel? = null
    
    /**
     * Connect to RabbitMQ server
     */
    fun connect() {
        if (connection?.isOpen == true) {
            logger.debug("Already connected to AMQP server")
            return
        }
        
        logger.info("Connecting to AMQP server: {}:{}", konfigurasjon.host, konfigurasjon.port)
        
        val factory = ConnectionFactory().apply {
            host = konfigurasjon.host
            port = konfigurasjon.port
            username = konfigurasjon.username
            password = konfigurasjon.password
            virtualHost = konfigurasjon.virtualHost
            
            if (konfigurasjon.useSsl) {
                useSslProtocol()
            }
            
            // Connection settings
            isAutomaticRecoveryEnabled = true
            networkRecoveryInterval = 10000
            requestedHeartbeat = 60
            connectionTimeout = 30000
        }
        
        try {
            connection = factory.newConnection()
            channel = connection?.createChannel()
            
            logger.info("Successfully connected to AMQP server")
        } catch (e: Exception) {
            logger.error("Failed to connect to AMQP server", e)
            throw AmqpConnectionException("Failed to connect to AMQP server: ${e.message}", e)
        }
    }
    
    /**
     * Subscribe to a queue and process messages with the given callback
     * 
     * @param queueName Name of the queue to subscribe to
     * @param autoAck Whether to automatically acknowledge messages
     * @param callback Function to process received messages
     */
    fun subscribe(
        queueName: String,
        autoAck: Boolean = false,
        callback: (deliveryTag: Long, body: ByteArray, headers: Map<String, Any>) -> Unit
    ) {
        val currentChannel = channel ?: run {
            connect()
            channel ?: throw AmqpConnectionException("Failed to create channel")
        }
        
        logger.info("Subscribing to queue: {}", queueName)
        
        try {
            // Declare the queue (idempotent operation)
            currentChannel.queueDeclare(
                queueName,
                true,  // durable
                false, // exclusive
                false, // autoDelete
                null   // arguments
            )
            
            // Set up consumer
            val deliverCallback = DeliverCallback { consumerTag, delivery ->
                try {
                    val headers = delivery.properties.headers?.mapValues { it.value } ?: emptyMap()
                    
                    logger.debug("Received message from queue: {} (deliveryTag: {})", queueName, delivery.envelope.deliveryTag)
                    
                    callback(delivery.envelope.deliveryTag, delivery.body, headers)
                    
                    // Manually acknowledge if not auto-ack
                    if (!autoAck) {
                        currentChannel.basicAck(delivery.envelope.deliveryTag, false)
                    }
                } catch (e: Exception) {
                    logger.error("Error processing message from queue: {}", queueName, e)
                    
                    // Reject and requeue the message on error
                    if (!autoAck) {
                        currentChannel.basicNack(delivery.envelope.deliveryTag, false, true)
                    }
                }
            }
            
            // Start consuming
            currentChannel.basicConsume(queueName, autoAck, deliverCallback) { _ ->
                logger.warn("Consumer for queue {} was cancelled", queueName)
            }
            
            logger.info("Successfully subscribed to queue: {}", queueName)
        } catch (e: Exception) {
            logger.error("Failed to subscribe to queue: {}", queueName, e)
            throw AmqpConnectionException("Failed to subscribe to queue: ${e.message}", e)
        }
    }
    
    /**
     * Publish a message to an exchange
     */
    fun publish(
        exchange: String,
        routingKey: String,
        body: ByteArray,
        headers: Map<String, Any> = emptyMap()
    ) {
        val currentChannel = channel ?: throw AmqpConnectionException("Not connected to AMQP server")
        
        try {
            val properties = com.rabbitmq.client.AMQP.BasicProperties.Builder()
                .headers(headers)
                .build()
            
            currentChannel.basicPublish(exchange, routingKey, properties, body)
            
            logger.debug("Published message to exchange: {}, routingKey: {}", exchange, routingKey)
        } catch (e: Exception) {
            logger.error("Failed to publish message", e)
            throw AmqpConnectionException("Failed to publish message: ${e.message}", e)
        }
    }
    
    /**
     * Check if connection is open
     */
    fun isConnected(): Boolean {
        return connection?.isOpen == true && channel?.isOpen == true
    }
    
    /**
     * Close the connection and channel
     */
    override fun close() {
        logger.info("Closing AMQP connection")
        
        try {
            channel?.close()
        } catch (e: Exception) {
            logger.warn("Error closing channel", e)
        }
        
        try {
            connection?.close()
        } catch (e: Exception) {
            logger.warn("Error closing connection", e)
        }
        
        channel = null
        connection = null
        
        logger.info("AMQP connection closed")
    }
}

/**
 * Exception thrown when AMQP connection operations fail
 */
class AmqpConnectionException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
