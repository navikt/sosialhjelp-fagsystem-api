plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor.plugin)
}

group = "no.nav.sosialhjelp.fagsystem"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(libs.bundles.ktor.server)
    implementation(libs.logback.classic)
    implementation(libs.rabbitmq.amqp.client)

    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.ktor.server.test.host)
}
