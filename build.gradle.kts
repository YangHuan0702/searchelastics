plugins {
    id("java")
}

group = "org.halosky"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


java{
    toolchain {
        targetCompatibility=JavaVersion.VERSION_21
        sourceCompatibility=JavaVersion.VERSION_21
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // lucene核心包
    implementation("org.apache.lucene:lucene-core:10.3.2")
    implementation("org.apache.lucene:lucene-queryparser:10.3.2")
    implementation("org.apache.lucene:lucene-analysis-common:10.3.2")
    implementation("cn.shenyanchao.ik-analyzer:ik-analyzer:9.0.0")

    implementation("io.netty:netty-all:4.1.118.Final")

    implementation("com.google.guava:guava:33.5.0-jre")
    implementation("com.twelvemonkeys.common:common-lang:3.13.0")

    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-core:1.4.11")
    implementation("ch.qos.logback:logback-classic:1.4.11")

    implementation("com.alibaba:fastjson:2.0.60")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.1")
    implementation("org.yaml:snakeyaml:2.5")

    implementation("co.cask.common:common-io:0.11.0")

    implementation("org.projectlombok:lombok:1.18.42")
    testImplementation("org.projectlombok:lombok:1.18.42")
    testCompileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.42")
    compileOnly("org.projectlombok:lombok:1.18.42")

}

tasks.test {
    useJUnitPlatform()
}