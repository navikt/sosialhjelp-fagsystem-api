# Fiks IO Client for Kotlin

This is a Kotlin client implementation for Fiks IO, a secure messaging system for the Norwegian public sector.

## About Fiks IO

Fiks IO is a messaging system that enables secure communication between public sector organizations in Norway. For more information, visit:
- [Fiks IO Documentation](https://developers.fiks.ks.no/tjenester/fiksprotokoll/fiksio/)
- [Java Client Reference](https://github.com/ks-no/fiks-io-klient-java)

## Features

This client simplifies:
- Authentication via Maskinporten
- Message encryption and decryption
- Communication with the Fiks IO messaging system
- Managing required protocol headers

## Usage

### Configuration

Create a configuration instance using the builder pattern:

```kotlin
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
            .integrasjonPassord("your-integration-password")
            .idPortenKonfigurasjon(
                IdPortenKonfigurasjon.builder()
                    .accessTokenUri("https://test.maskinporten.no/token")
                    .idPortenAudience("https://test.maskinporten.no/")
                    .klientId("your-client-id")
                    .build()
            )
            .build()
    )
    .kontoKonfigurasjon(
        KontoKonfigurasjon.builder()
            .kontoId(UUID.fromString("your-account-id"))
            .privatNokkel("your-private-key-pkcs8")
            .build()
    )
    .build()
```

### Creating a Client

```kotlin
val factory = FiksIOKlientFactory(konfigurasjon)
val klient = factory.build()
```

### Sending Messages

```kotlin
val sendtMelding = klient.send(
    mottakerKontoId = KontoId(UUID.fromString("recipient-account-id")),
    meldingType = "no.ks.fiks.io.test.v1",
    payload = "Hello, Fiks IO!".toByteArray(),
    headere = mapOf("custom-header" to "value")
)
```

### Receiving Messages

```kotlin
klient.newSubscription { mottattMelding, svarSender ->
    println("Received message: ${mottattMelding.meldingType}")
    println("Payload: ${mottattMelding.payloadAsString()}")
    
    // Send a reply
    svarSender.svar(
        meldingType = "no.ks.fiks.io.test.reply.v1",
        payload = "Reply message".toByteArray()
    )
}
```

### Looking up Accounts

```kotlin
val konto = klient.lookup(
    identifikator = "123456789",
    identifikatorType = "organisasjonsnummer"
)
```

### Cleanup

```kotlin
klient.close()
```

## Current Status

✅ **AMQP Subscription Implemented** - The current version provides:

1. ✅ **RabbitMQ/AMQP Connection** - Full connection management with SSL support
2. ✅ **Message Queue Subscription** - Subscribe to queues and receive messages
3. ✅ **Automatic Recovery** - Connection recovery and error handling
4. ✅ **Message Acknowledgment** - Manual message acknowledgment with error handling

⚠️ **Still Requires Implementation:**

1. **Maskinporten Token Handling** - OAuth2 token acquisition and refresh
2. **Message Encryption/Decryption** - End-to-end message security (ASiC-E format)
3. **HTTP Client** - API calls to Fiks platform endpoints
4. **Message Publishing** - Complete implementation for sending messages via AMQP

## Dependencies

Current dependencies:
- ✅ RabbitMQ AMQP Client (v5.21.0)
- ✅ Logback for logging

Additional dependencies needed for full implementation:
- HTTP client (e.g., Ktor client)
- Cryptography libraries
- JWT/OAuth2 libraries for Maskinporten

## License

This implementation follows the patterns established by the official Java client from KS.
