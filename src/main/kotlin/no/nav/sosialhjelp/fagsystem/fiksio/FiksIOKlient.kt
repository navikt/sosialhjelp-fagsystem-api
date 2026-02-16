package no.nav.sosialhjelp.fagsystem.fiksio

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
 * This is a placeholder implementation - actual implementation would need:
 * - RabbitMQ/AMQP integration
 * - Maskinporten token handling
 * - Message encryption/decryption
 * - HTTP client for API calls
 */
internal class FiksIOKlientImpl(
    private val konfigurasjon: FiksIOKonfigurasjon
) : FiksIOKlient {

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
        // 3. Sending via AMQP
        
        val avsenderKontoId = konfigurasjon.kontoKonfigurasjon?.kontoId
            ?: throw IllegalStateException("Sender account not configured")
        
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
        // TODO: Implement AMQP subscription
        // This would involve:
        // 1. Setting up RabbitMQ connection
        // 2. Creating queue and binding
        // 3. Setting up message consumer
        // 4. Decrypting incoming messages
        // 5. Calling handler with message and reply sender
        
        throw NotImplementedError("Message subscription not yet implemented")
    }

    override fun lookup(identifikator: String, identifikatorType: String): Konto? {
        // TODO: Implement account lookup via Fiks API
        // This would involve:
        // 1. Getting Maskinporten token
        // 2. Making HTTP call to lookup endpoint
        // 3. Parsing response
        
        return null
    }

    override fun close() {
        // TODO: Implement resource cleanup
        // This would involve:
        // 1. Closing AMQP connection
        // 2. Closing HTTP client
        // 3. Shutting down thread pools
    }
}
