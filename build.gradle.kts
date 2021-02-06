import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    
    id("com.github.johnrengelman.shadow") version "6.1.0"
}


group = "com.sxtanna.mc"
version = "1.0.0"


repositories {
    mavenCentral()
    
    maven {
        url = uri("https://repo.aikar.co/content/groups/aikar/")
    }
    
    maven {
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
    
    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
    
    maven {
        url = uri("https://repo.dmulloy2.net/nexus/repository/public/")
    }
}

dependencies {
    // == meta
    implementation("org.jetbrains:annotations:20.1.0")
    // == meta
    
    // == libraries
    
    // ==== configuration
    implementation("ch.jalu:configme:1.2.0")
    
    // ==== commands
    implementation("co.aikar:acf-paper:0.5.0-SNAPSHOT")
    
    // ==== sqlite
    implementation("org.xerial:sqlite-jdbc:3.34.0.1-SNAPSHOT")
    
    // ==== hikari
    implementation("com.zaxxer:HikariCP:3.4.5")
    implementation("org.mariadb.jdbc:mariadb-java-client:2.7.1")
    
    // ==== placeholders
    compileOnly("me.clip:placeholderapi:2.10.9")
    
    // ==== protocol
    compileOnly("com.comphenix.protocol:ProtocolLib:4.5.1")
    
    // == libraries
    
    // == platform
    compileOnly("com.destroystokyo.paper:paper-api:1.16.4-R0.1-SNAPSHOT")
    // == platform
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

tasks.named<ShadowJar>("shadowJar") {
    relocate("ch.jalu", "com.sxtanna.mc.libs")
    relocate("co.aikar", "com.sxtanna.mc.libs")
    relocate("org.sqlite", "com.sxtanna.mc.libs")
    relocate("com.zaxxer", "com.sxtanna.mc.libs")
    relocate("org.mariadb.jdbc", "com.sxtanna.mc.libs")
}