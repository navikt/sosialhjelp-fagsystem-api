package no.nav.sosialhjelp.fagsystem.fiksio

import org.slf4j.LoggerFactory
import java.util.UUID

/**
 * Main interface for Fiks IO client.
 * Use FiksIOKlientFactory to create instances.
 */
interface FiksIOKlient {
    /**
     * Send a message to a recipient account
     */
    fun send(
        mottakerKontoId: KontoId,
        meldingType: String,
        payload: ByteArray? = null,
        vedlegg: List<Vedlegg> = emptyList(),
        headere: Map<String, String> = emptyMap(),
        klientKorrelasjonId: String? = null
    ): SendtMelding

    /**
     * Subscribe to incoming messages
     * @param handler function to handle incoming messages and optionally send replies
     */
    fun newSubscription(handler: (MottattMelding, SvarSender) -> Unit)

    /**
     * Look up a Fiks IO account
     */
    fun lookup(identifikator: String, identifikatorType: String): Konto?

    /**
     * Close the client and release resources
     */
    fun close()
}

/**
 * Represents a Fiks IO account (Konto)
 */
data class Konto(
    val kontoId: KontoId,
    val navn: String? = null,
    val organisasjonsnummer: String? = null
)

/**
 * Factory for creating FiksIOKlient instances
 */
class FiksIOKlientFactory(
    private val konfigurasjon: FiksIOKonfigurasjon
) {
    /**
     * Build a FiksIOKlient instance
     */
    fun build(): FiksIOKlient {
        return FiksIOKlientImpl(konfigurasjon)
    }
}

/**
 * Default implementation of FiksIOKlient
 */
internal class FiksIOKlientImpl(
    private val konfigurasjon: FiksIOKonfigurasjon
) : FiksIOKlient {

    private val logger = LoggerFactory.getLogger(FiksIOKlientImpl::class.java)
    private val amqpConnection: AmqpConnection = AmqpConnection(konfigurasjon.amqpKonfigurasjon)
    private var isSubscribed = false

    override fun send(
        mottakerKontoId: KontoId,
        meldingType: String,
        payload: ByteArray?,
        vedlegg: List<Vedlegg>,
        headere: Map<String, String>,
        klientKorrelasjonId: String?
    ): SendtMelding {
        // TODO: Implement actual message sending
        // This would involve:
        // 1. Getting Maskinporten token
        // 2. Encrypting payload
        // 3. Publishing via AMQP
        
        val avsenderKontoId = konfigurasjon.kontoKonfigurasjon?.kontoId
            ?: throw IllegalStateException("Sender account not configured")
        
        logger.info("Sending message type: {} to: {}", meldingType, mottakerKontoId)
        
        return SendtMelding(
            meldingId = UUID.randomUUID(),
            avsenderKontoId = avsenderKontoId,
            mottakerKontoId = mottakerKontoId,
            meldingType = meldingType,
            headere = headere,
            klientKorrelasjonId = klientKorrelasjonId
        )
    }

    override fun newSubscription(handler: (MottattMelding, SvarSender) -> Unit) {
        if (isSubscribed) {
            logger.warn("Already subscribed to messages")
            return
        }
        
        val kontoId = konfigurasjon.kontoKonfigurasjon?.kontoId
            ?: throw IllegalStateException("Account configuration required for subscription")
        
        logger.info("Setting up subscription for account: {}", kontoId)
        
        // Connect to AMQP server
        amqpConnection.connect()
        
        // Queue name based on account ID (following Fiks IO convention)
        val queueName = kontoId.toString()
        
        // Subscribe to the queue
        // Note: Fiks IO queues are pre-configured by the platform, so we don't declare them
        amqpConnection.subscribe(queueName, autoAck = false, declareQueue = false) { body, headers ->
            try {
                // Parse message from AMQP delivery
                // TODO: Implement actual message parsing and decryption
                val mottattMelding = parseMelding(body, headers, kontoId)
                
                // Create reply sender
                val svarSender = createSvarSender(mottattMelding)
                
                // Call user handler
                handler(mottattMelding, svarSender)
            } catch (e: Exception) {
                logger.error("Error processing received message", e)
                throw e
            }
        }
        
        isSubscribed = true
        logger.info("Successfully subscribed to messages for account: {}", kontoId)
    }

    override fun lookup(identifikator: String, identifikatorType: String): Konto? {
        // TODO: Implement account lookup via Fiks API
        // This would involve:
        // 1. Getting Maskinporten token
        // 2. Making HTTP call to lookup endpoint
        // 3. Parsing response
        
        logger.info("Looking up account with {}: {}", identifikatorType, identifikator)
        return null
    }

    override fun close() {
        logger.info("Closing Fiks IO client")
        
        try {
            amqpConnection.close()
        } catch (e: Exception) {
            logger.error("Error closing AMQP connection", e)
        }
        
        isSubscribed = false
        logger.info("Fiks IO client closed")
    }
    
    /**
     * Parse received message from AMQP delivery
     * TODO: Implement actual parsing and decryption
     */
    private fun parseMelding(
        body: ByteArray,
        headers: Map<String, Any>,
        mottakerKontoId: KontoId
    ): MottattMelding {
        // For now, create a basic message
        // In a real implementation, this would:
        // 1. Extract message metadata from headers
        // 2. Decrypt the message body
        // 3. Parse attachments
        
        val meldingType = headers["meldingType"]?.toString() ?: "unknown"
        val avsenderKontoId = headers["avsenderKontoId"]?.toString()?.let { 
            KontoId(UUID.fromString(it)) 
        } ?: KontoId(UUID.randomUUID())
        
        return MottattMelding(
            meldingId = UUID.randomUUID(),
            avsenderKontoId = avsenderKontoId,
            mottakerKontoId = mottakerKontoId,
            meldingType = meldingType,
            ttl = null,
            headere = headers.mapValues { it.value.toString() },
            klientKorrelasjonId = headers["klientKorrelasjonId"]?.toString(),
            payload = body,
            vedlegg = emptyList()
        )
    }
    
    /**
     * Create a reply sender for a received message
     */
    private fun createSvarSender(mottattMelding: MottattMelding): SvarSender {
        return object : SvarSender {
            override fun svar(
                meldingType: String,
                payload: ByteArray?,
                vedlegg: List<Vedlegg>,
                headere: Map<String, String>
            ): SendtMelding {
                // Send reply to the original sender
                return send(
                    mottakerKontoId = mottattMelding.avsenderKontoId,
                    meldingType = meldingType,
                    payload = payload,
                    vedlegg = vedlegg,
                    headere = headere,
                    klientKorrelasjonId = mottattMelding.klientKorrelasjonId
                )
            }
        }
    }
}
