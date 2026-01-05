plugins {
    id("java")
}

group = "org.halosky"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // lucene核心包
    implementation("org.apache.lucene:lucene-core:10.3.2")
    implementation("org.apache.lucene:lucene-queryparser:10.3.2")
    implementation("org.apache.lucene:lucene-analysis-common:10.3.2")
    implementation("cn.shenyanchao.ik-analyzer:ik-analyzer:9.0.0")


    implementation("com.alibaba:fastjson:2.0.60")

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