# 오픈 마켓 프로젝트

일반 사용자는 중고거래를, 사업자는 인터넷 쇼핑몰을 운영할 수 있게 해주는 쇼핑몰 사이트를 만들어보자.

요구사항의 내용은 프런트엔드 없이, 백엔드만 개발한다. 별도의 프런트엔드 클라이언트가 존재한다고 생각하고 서버를 만들고, Postman으로 테스트를 한다. 단, CORS는 지금은 고려하지 않는다.

## 스택
- Spring Boot 3.3.3
    - Spring Boot Starter Web
    - Spring Boot Starter Data JPA
    - Spring Boot Starter Security
    - Lombok
- Java JWT 0.11.5

## 실행
1. 본 Repository를 clone 받는다.
2. Intellij IDEA를 이용해 clone 받은 폴더를 연다.
3. `MarketApplication.java`의 `main`을 실행한다.

### JAR로 실행하는 경우 (Macos, Linux)

Java 17 버전 필요

터미널에서,
1. `./gradlew clean`
2. `./gradlew bootJar`
3. `cd build/lib` (폴더 이동)
4. `java -jar market-0.0.1-SNAPSHOT.jar`

### JAR로 실행하는 경우 (Macos, Linux)

Java 17 버전 필요

CMD에서,
1. `gradlew.bat clean`
2. `gradlew.bat bootJar`
3. `cd build/lib` (폴더 이동)
4. `java -jar market-0.0.1-SNAPSHOT.jar`

## 필수 기능

- [공유 기능](md/0_common.md)
- [관리자 기능](md/1_admin.md)
- [사용자 인증 및 권한 처리](md/2_auth.md)
- [쇼핑몰 운영하기](md/3_shop.md)