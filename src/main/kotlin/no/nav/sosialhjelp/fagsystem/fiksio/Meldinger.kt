package no.nav.sosialhjelp.fagsystem.fiksio

import java.time.ZonedDateTime
import java.util.UUID

/**
 * Represents a sent message in Fiks IO
 */
data class SendtMelding(
    val meldingId: UUID,
    val avsenderKontoId: KontoId,
    val mottakerKontoId: KontoId,
    val meldingType: String,
    val ttl: Long? = null,
    val headere: Map<String, String> = emptyMap(),
    val klientKorrelasjonId: String? = null,
    val sendtTidspunkt: ZonedDateTime = ZonedDateTime.now()
)

/**
 * Represents a received message in Fiks IO
 */
data class MottattMelding(
    val meldingId: UUID,
    val avsenderKontoId: KontoId,
    val mottakerKontoId: KontoId,
    val meldingType: String,
    val ttl: Long?,
    val headere: Map<String, String>,
    val klientKorrelasjonId: String?,
    val mottattTidspunkt: ZonedDateTime = ZonedDateTime.now(),
    val payload: ByteArray? = null,
    val vedlegg: List<Vedlegg> = emptyList()
) {
    /**
     * Get payload as String
     */
    fun payloadAsString(): String? = payload?.toString(Charsets.UTF_8)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MottattMelding

        if (meldingId != other.meldingId) return false
        if (avsenderKontoId != other.avsenderKontoId) return false
        if (mottakerKontoId != other.mottakerKontoId) return false
        if (meldingType != other.meldingType) return false
        if (ttl != other.ttl) return false
        if (headere != other.headere) return false
        if (klientKorrelasjonId != other.klientKorrelasjonId) return false
        if (mottattTidspunkt != other.mottattTidspunkt) return false
        if (payload != null) {
            if (other.payload == null) return false
            if (!payload.contentEquals(other.payload)) return false
        } else if (other.payload != null) return false
        if (vedlegg != other.vedlegg) return false

        return true
    }

    override fun hashCode(): Int {
        var result = meldingId.hashCode()
        result = 31 * result + avsenderKontoId.hashCode()
        result = 31 * result + mottakerKontoId.hashCode()
        result = 31 * result + meldingType.hashCode()
        result = 31 * result + (ttl?.hashCode() ?: 0)
        result = 31 * result + headere.hashCode()
        result = 31 * result + (klientKorrelasjonId?.hashCode() ?: 0)
        result = 31 * result + mottattTidspunkt.hashCode()
        result = 31 * result + (payload?.contentHashCode() ?: 0)
        result = 31 * result + vedlegg.hashCode()
        return result
    }
}

/**
 * Represents an attachment to a message
 */
data class Vedlegg(
    val filnavn: String,
    val mimeType: String,
    val innhold: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vedlegg

        if (filnavn != other.filnavn) return false
        if (mimeType != other.mimeType) return false
        if (!innhold.contentEquals(other.innhold)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = filnavn.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + innhold.contentHashCode()
        return result
    }
}

/**
 * Interface for sending reply messages
 */
interface SvarSender {
    /**
     * Send a reply message
     */
    fun svar(
        meldingType: String,
        payload: ByteArray? = null,
        vedlegg: List<Vedlegg> = emptyList(),
        headere: Map<String, String> = emptyMap()
    ): SendtMelding
}
