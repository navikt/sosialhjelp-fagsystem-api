package no.nav.sosialhjelp.fagsystem.fiksio

import java.util.UUID

/**
 * Configuration class for Fiks IO client.
 * Use the builder to create an instance with all required settings.
 */
data class FiksIOKonfigurasjon(
    val fiksApiKonfigurasjon: FiksApiKonfigurasjon,
    val amqpKonfigurasjon: AmqpKonfigurasjon,
    val fiksIntegrasjonKonfigurasjon: FiksIntegrasjonKonfigurasjon,
    val kontoKonfigurasjon: KontoKonfigurasjon? = null
) {
    companion object {
        fun builder() = Builder()
    }

    class Builder {
        private var fiksApiKonfigurasjon: FiksApiKonfigurasjon? = null
        private var amqpKonfigurasjon: AmqpKonfigurasjon? = null
        private var fiksIntegrasjonKonfigurasjon: FiksIntegrasjonKonfigurasjon? = null
        private var kontoKonfigurasjon: KontoKonfigurasjon? = null

        fun fiksApiKonfigurasjon(config: FiksApiKonfigurasjon) = apply {
            this.fiksApiKonfigurasjon = config
        }

        fun amqpKonfigurasjon(config: AmqpKonfigurasjon) = apply {
            this.amqpKonfigurasjon = config
        }

        fun fiksIntegrasjonKonfigurasjon(config: FiksIntegrasjonKonfigurasjon) = apply {
            this.fiksIntegrasjonKonfigurasjon = config
        }

        fun kontoKonfigurasjon(config: KontoKonfigurasjon) = apply {
            this.kontoKonfigurasjon = config
        }

        fun build(): FiksIOKonfigurasjon {
            requireNotNull(fiksApiKonfigurasjon) { "fiksApiKonfigurasjon must be set" }
            requireNotNull(amqpKonfigurasjon) { "amqpKonfigurasjon must be set" }
            requireNotNull(fiksIntegrasjonKonfigurasjon) { "fiksIntegrasjonKonfigurasjon must be set" }

            return FiksIOKonfigurasjon(
                fiksApiKonfigurasjon = fiksApiKonfigurasjon!!,
                amqpKonfigurasjon = amqpKonfigurasjon!!,
                fiksIntegrasjonKonfigurasjon = fiksIntegrasjonKonfigurasjon!!,
                kontoKonfigurasjon = kontoKonfigurasjon
            )
        }
    }
}

/**
 * Configuration for Fiks API endpoints
 */
data class FiksApiKonfigurasjon(
    val host: String,
    val port: Int,
    val scheme: String
) {
    companion object {
        fun builder() = Builder()
    }

    class Builder {
        private var host: String = "api.fiks.ks.no"
        private var port: Int = 443
        private var scheme: String = "https"

        fun host(host: String) = apply { this.host = host }
        fun port(port: Int) = apply { this.port = port }
        fun scheme(scheme: String) = apply { this.scheme = scheme }

        fun build() = FiksApiKonfigurasjon(host, port, scheme)
    }
}

/**
 * Configuration for AMQP (RabbitMQ) connection
 */
data class AmqpKonfigurasjon(
    val host: String,
    val port: Int,
    val username: String = "guest",
    val password: String = "guest",
    val virtualHost: String = "/",
    val useSsl: Boolean = true
) {
    companion object {
        fun builder() = Builder()
    }

    class Builder {
        private var host: String = "io.fiks.ks.no"
        private var port: Int = 5671
        private var username: String = "guest"
        private var password: String = "guest"
        private var virtualHost: String = "/"
        private var useSsl: Boolean = true

        fun host(host: String) = apply { this.host = host }
        fun port(port: Int) = apply { this.port = port }
        fun username(username: String) = apply { this.username = username }
        fun password(password: String) = apply { this.password = password }
        fun virtualHost(virtualHost: String) = apply { this.virtualHost = virtualHost }
        fun useSsl(useSsl: Boolean) = apply { this.useSsl = useSsl }

        fun build() = AmqpKonfigurasjon(host, port, username, password, virtualHost, useSsl)
    }
}

/**
 * Configuration for Fiks Integration
 */
data class FiksIntegrasjonKonfigurasjon(
    val integrasjonId: UUID,
    val integrasjonPassord: String,
    val idPortenKonfigurasjon: IdPortenKonfigurasjon
) {
    companion object {
        fun builder() = Builder()
    }

    class Builder {
        private var integrasjonId: UUID? = null
        private var integrasjonPassord: String? = null
        private var idPortenKonfigurasjon: IdPortenKonfigurasjon? = null

        fun integrasjonId(id: UUID) = apply { this.integrasjonId = id }
        fun integrasjonId(id: String) = apply { this.integrasjonId = UUID.fromString(id) }
        fun integrasjonPassord(password: String) = apply { this.integrasjonPassord = password }
        fun idPortenKonfigurasjon(config: IdPortenKonfigurasjon) = apply {
            this.idPortenKonfigurasjon = config
        }

        fun build(): FiksIntegrasjonKonfigurasjon {
            requireNotNull(integrasjonId) { "integrasjonId must be set" }
            requireNotNull(integrasjonPassord) { "integrasjonPassord must be set" }
            requireNotNull(idPortenKonfigurasjon) { "idPortenKonfigurasjon must be set" }

            return FiksIntegrasjonKonfigurasjon(
                integrasjonId = integrasjonId!!,
                integrasjonPassord = integrasjonPassord!!,
                idPortenKonfigurasjon = idPortenKonfigurasjon!!
            )
        }
    }
}

/**
 * Configuration for Maskinporten (ID-porten)
 */
data class IdPortenKonfigurasjon(
    val accessTokenUri: String,
    val idPortenAudience: String,
    val klientId: String
) {
    companion object {
        fun builder() = Builder()
    }

    class Builder {
        private var accessTokenUri: String? = null
        private var idPortenAudience: String? = null
        private var klientId: String? = null

        fun accessTokenUri(uri: String) = apply { this.accessTokenUri = uri }
        fun idPortenAudience(audience: String) = apply { this.idPortenAudience = audience }
        fun klientId(id: String) = apply { this.klientId = id }

        fun build(): IdPortenKonfigurasjon {
            requireNotNull(accessTokenUri) { "accessTokenUri must be set" }
            requireNotNull(idPortenAudience) { "idPortenAudience must be set" }
            requireNotNull(klientId) { "klientId must be set" }

            return IdPortenKonfigurasjon(
                accessTokenUri = accessTokenUri!!,
                idPortenAudience = idPortenAudience!!,
                klientId = klientId!!
            )
        }
    }
}

/**
 * Configuration for Fiks IO account (Konto)
 */
data class KontoKonfigurasjon(
    val kontoId: KontoId,
    val privateNokler: List<String>
) {
    companion object {
        fun builder() = Builder()
    }

    class Builder {
        private var kontoId: KontoId? = null
        private val privateNokler = mutableListOf<String>()

        fun kontoId(id: KontoId) = apply { this.kontoId = id }
        fun kontoId(id: UUID) = apply { this.kontoId = KontoId(id) }
        fun privatNokkel(key: String) = apply { this.privateNokler.add(key) }
        fun privateNokler(keys: List<String>) = apply { this.privateNokler.addAll(keys) }

        fun build(): KontoKonfigurasjon {
            requireNotNull(kontoId) { "kontoId must be set" }
            require(privateNokler.isNotEmpty()) { "At least one private key must be provided" }

            return KontoKonfigurasjon(
                kontoId = kontoId!!,
                privateNokler = privateNokler.toList()
            )
        }
    }
}

/**
 * Represents a Fiks IO account ID
 */
data class KontoId(val id: UUID) {
    override fun toString(): String = id.toString()
}
