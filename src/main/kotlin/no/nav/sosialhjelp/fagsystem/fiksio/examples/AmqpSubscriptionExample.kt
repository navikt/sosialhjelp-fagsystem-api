package no.nav.sosialhjelp.fagsystem.fiksio

import java.util.UUID

/**
 * Example demonstrating AMQP subscription usage
 */
object AmqpSubscriptionExample {
    
    /**
     * Example: Set up AMQP subscription with full configuration
     */
    fun setupSubscription() {
        val konfigurasjon = FiksIOKonfigurasjon.builder()
            .fiksApiKonfigurasjon(
                FiksApiKonfigurasjon.builder()
                    .host("api.fiks.ks.no")
                    .port(443)
                    .scheme("https")
                    .build()
            )
            .amqpKonfigurasjon(
                AmqpKonfigurasjon.builder()
                    .host("io.fiks.ks.no")
                    .port(5671)
                    .username("your-username")
                    .password("your-password")
                    .virtualHost("/")
                    .useSsl(true)
                    .build()
            )
            .fiksIntegrasjonKonfigurasjon(
                FiksIntegrasjonKonfigurasjon.builder()
                    .integrasjonId("your-integration-id")
                    .integrasjonPassord("your-password")
                    .idPortenKonfigurasjon(
                        IdPortenKonfigurasjon.builder()
                            .accessTokenUri("https://maskinporten.no/token")
                            .idPortenAudience("https://maskinporten.no/")
                            .klientId("your-client-id")
                            .build()
                    )
                    .build()
            )
            .kontoKonfigurasjon(
                KontoKonfigurasjon.builder()
                    .kontoId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                    .privatNokkel("-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----")
                    .build()
            )
            .build()
        
        val klient = FiksIOKlientFactory(konfigurasjon).build()
        
        try {
            // Subscribe to messages
            klient.newSubscription { mottattMelding, svarSender ->
                println("Received message:")
                println("  Type: ${mottattMelding.meldingType}")
                println("  From: ${mottattMelding.avsenderKontoId}")
                println("  To: ${mottattMelding.mottakerKontoId}")
                println("  Payload: ${mottattMelding.payloadAsString()}")
                
                // Send a reply
                svarSender.svar(
                    meldingType = "no.ks.fiks.reply.v1",
                    payload = "Message received and processed".toByteArray()
                )
                
                println("Reply sent")
            }
            
            println("Subscription active. Press Ctrl+C to exit...")
            
            // Keep the application running
            Thread.currentThread().join()
        } finally {
            klient.close()
        }
    }
    
    /**
     * Example: Test AMQP connection without full integration
     */
    fun testAmqpConnection() {
        val amqpKonfig = AmqpKonfigurasjon.builder()
            .host("localhost")  // For local testing
            .port(5672)
            .username("guest")
            .password("guest")
            .virtualHost("/")
            .useSsl(false)
            .build()
        
        val connection = AmqpConnection(amqpKonfig)
        
        try {
            println("Connecting to AMQP server...")
            connection.connect()
            
            println("Connected successfully!")
            println("Connection status: ${connection.isConnected()}")
            
            // Subscribe to a test queue
            connection.subscribe("test-queue") { deliveryTag, body, headers ->
                println("Received message:")
                println("  Delivery Tag: $deliveryTag")
                println("  Body: ${String(body)}")
                println("  Headers: $headers")
            }
            
            println("Subscribed to test-queue")
            
            // Publish a test message
            connection.publish(
                exchange = "",
                routingKey = "test-queue",
                body = "Hello from AMQP!".toByteArray(),
                headers = mapOf("test-header" to "test-value")
            )
            
            println("Published test message")
            
            // Wait a bit for message to be processed
            Thread.sleep(1000)
        } catch (e: Exception) {
            println("Error: ${e.message}")
            e.printStackTrace()
        } finally {
            connection.close()
            println("Connection closed")
        }
    }
    
    @JvmStatic
    fun main(args: Array<String>) {
        println("AMQP Subscription Examples")
        println("==========================")
        println()
        
        when {
            args.contains("--test") -> {
                println("Running AMQP connection test...")
                testAmqpConnection()
            }
            else -> {
                println("Setting up full subscription...")
                setupSubscription()
            }
        }
    }
}
