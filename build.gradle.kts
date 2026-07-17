plugins {
    java
    id("org.springframework.boot") version "3.5.7" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {
    group = "fedor.dev"

    repositories {
        mavenCentral()
    }
}
