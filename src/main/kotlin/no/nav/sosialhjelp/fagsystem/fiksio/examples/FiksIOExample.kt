package no.nav.sosialhjelp.fagsystem.fiksio.examples

import no.nav.sosialhjelp.fagsystem.fiksio.*
import java.util.UUID

object FiksIOExample {
    
    fun createProdConfiguration(
        integrasjonId: String,
        integrasjonPassord: String,
        klientId: String,
        kontoId: String,
        privatNokkel: String
    ): FiksIOKonfigurasjon {
        return FiksIOKonfigurasjon.builder()
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
                    .build()
            )
            .fiksIntegrasjonKonfigurasjon(
                FiksIntegrasjonKonfigurasjon.builder()
                    .integrasjonId(integrasjonId)
                    .integrasjonPassord(integrasjonPassord)
                    .idPortenKonfigurasjon(
                        IdPortenKonfigurasjon.builder()
                            .accessTokenUri("https://maskinporten.no/token")
                            .idPortenAudience("https://maskinporten.no/")
                            .klientId(klientId)
                            .build()
                    )
                    .build()
            )
            .kontoKonfigurasjon(
                KontoKonfigurasjon.builder()
                    .kontoId(UUID.fromString(kontoId))
                    .privatNokkel(privatNokkel)
                    .build()
            )
            .build()
    }
    
    fun sendMessage(klient: FiksIOKlient, mottakerId: UUID) {
        val sendtMelding = klient.send(
            mottakerKontoId = KontoId(mottakerId),
            meldingType = "no.ks.fiks.io.test.v1",
            payload = "Test message".toByteArray(Charsets.UTF_8),
            headere = mapOf("custom-header" to "custom-value")
        )
        println("Sent message: ${sendtMelding.meldingId}")
    }
    
    fun subscribeToMessages(klient: FiksIOKlient) {
        klient.newSubscription { mottattMelding, svarSender ->
            println("Received: ${mottattMelding.meldingType}")
            svarSender.svar(
                meldingType = "no.ks.fiks.io.test.reply.v1",
                payload = "Thank you".toByteArray(Charsets.UTF_8)
            )
        }
    }
}
