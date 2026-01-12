# Lost & Found (Java + SQLite)

## IntelliJ Açılış
- IntelliJ > Open > `lostfound-java-sqlite` klasörünü aç
- Java 17 kullan

## Çalıştırma
Windows:
- `mvnw.cmd -pl backend spring-boot:run`

Linux/Mac:
- `./mvnw -pl backend spring-boot:run`

URL: http://localhost:8080

Demo kullanıcılar:
- admin@demo.com / admin123
- staff@demo.com / staff123
- user@demo.com / user123

## Testler
Unit:
- `./mvnw -pl backend test`

Integration:
- `./mvnw -pl backend verify`

> E2E selenium testleri şimdilik placeholder ve default skip (skipE2E=true).

aliimrantasdemir