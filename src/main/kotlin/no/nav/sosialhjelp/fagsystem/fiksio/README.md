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

⚠️ **This is a basic implementation** - The current version provides the core structure and interfaces but requires additional implementation for:

1. **RabbitMQ/AMQP Integration** - Message queue connection and subscription
2. **Maskinporten Token Handling** - OAuth2 token acquisition and refresh
3. **Message Encryption/Decryption** - End-to-end message security
4. **HTTP Client** - API calls to Fiks platform endpoints
5. **Error Handling** - Comprehensive error handling and retry logic

## Dependencies

To use this client, you'll need to add dependencies for:
- RabbitMQ client (AMQP)
- HTTP client (e.g., Ktor client)
- Cryptography libraries
- JWT/OAuth2 libraries for Maskinporten

## License

This implementation follows the patterns established by the official Java client from KS.
