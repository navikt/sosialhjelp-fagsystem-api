# Fiks IO Client Implementation

This directory contains a Kotlin implementation of a Fiks IO client for this project.

## Structure

The Fiks IO client is organized as follows:

```
src/main/kotlin/no/nav/sosialhjelp/fagsystem/fiksio/
├── FiksIOKlient.kt           # Main client interface and implementation
├── FiksIOKonfigurasjon.kt    # Configuration classes
├── Meldinger.kt              # Message models and types
├── AmqpConnection.kt         # RabbitMQ/AMQP connection management
└── README.md                 # Detailed usage documentation
```

## Quick Start

### 1. Configuration

```kotlin
import no.nav.sosialhjelp.fagsystem.fiksio.*
import java.util.UUID

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
            .useSsl(true)
            .build()
    )
    .fiksIntegrasjonKonfigurasjon(
        FiksIntegrasjonKonfigurasjon.builder()
            .integrasjonId(UUID.fromString("your-integration-id"))
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
            .kontoId(UUID.fromString("your-account-id"))
            .privatNokkel("-----BEGIN PRIVATE KEY-----\n...")
            .build()
    )
    .build()
```

### 2. Create Client

```kotlin
val factory = FiksIOKlientFactory(konfigurasjon)
val klient = factory.build()
```

### 3. Send and Receive Messages

```kotlin
// Subscribe to messages
klient.newSubscription { mottattMelding, svarSender ->
    println("Mottatt melding: ${mottattMelding.meldingType}")
    
    // Send reply
    svarSender.svar(
        meldingType = "no.ks.fiks.reply.v1",
        payload = "Svar".toByteArray()
    )
}

// Send a message
val sendt = klient.send(
    mottakerKontoId = KontoId(UUID.fromString("recipient-id")),
    meldingType = "no.ks.fiks.test.v1",
    payload = "Hello".toByteArray()
)
```

## Key Classes

### FiksIOKlient
Main interface for interacting with Fiks IO:
- `send()` - Send messages
- `newSubscription()` - Subscribe to incoming messages
- `lookup()` - Look up account information
- `close()` - Clean up resources

### FiksIOKonfigurasjon
Configuration builder with support for:
- API endpoints configuration
- AMQP/RabbitMQ settings
- Integration credentials
- Maskinporten (ID-porten) settings
- Account configuration

### Message Types
- `SendtMelding` - Represents sent messages
- `MottattMelding` - Represents received messages
- `Vedlegg` - Represents message attachments
- `SvarSender` - Interface for sending replies

### AmqpConnection
RabbitMQ connection management:
- Connection lifecycle management
- SSL/TLS support
- Automatic recovery
- Queue subscription and message consumption
- Message acknowledgment handling

## Implementation Status

✅ **Implemented:**
- Core configuration classes with builder pattern
- Message models and data classes
- Client interface and factory
- **RabbitMQ/AMQP connection management**
- **Message queue subscription shell**
- **Automatic connection recovery**
- **SSL/TLS support**
- **Message acknowledgment**

⚠️ **Pending Implementation:**
- Maskinporten OAuth2 token handling
- Message encryption/decryption (ASiC-E format)
- HTTP client for API calls
- Complete message publishing implementation
- Advanced error handling and retry logic

## References

- [Fiks IO Documentation](https://developers.fiks.ks.no/tjenester/fiksprotokoll/fiksio/)
- [Fiks IO Java Client](https://github.com/ks-no/fiks-io-klient-java)
- [Fiks Platform Documentation](https://ks-no.github.io/fiks-plattform/)

## License

This implementation follows the patterns from the KS Fiks IO Java client.
