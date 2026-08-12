# Maps SDK 설정 메모

## 범위

- 대상 작업: `S00-T006`, `S00-T007`
- 기준 날짜: 2026-08-12
- 대상 앱 패키지명: `com.wjs.arnav`

## 1. 기술 선택

- 지도 UI는 Jetpack Compose 기반이므로 공식 Compose 래퍼 라이브러리 `com.google.maps.android:maps-compose`를 사용한다.
- 2026-08-12 기준 공식 GitHub 릴리스 목록에서 `v9.0.0-rc01`은 pre-release이며, 가장 최신 안정 릴리스는 `v8.4.0`이다.
- 앱 내부 API 키 메타데이터는 Google 권장 이름인 `com.google.android.geo.API_KEY`를 사용한다.

## 2. Google Cloud 프로젝트 및 Maps SDK 설정 절차

1. Google Cloud Console에서 ARNavigation 전용 프로젝트를 생성한다.
2. 프로젝트에 결제를 연결한다.
3. `Maps SDK for Android` API를 활성화한다.
4. Credentials에서 Android 앱 전용 API 키를 새로 만든다.
5. 저장소에는 키를 넣지 않고, 로컬 개발자는 `ARNavigation/secrets.properties.example`을 복사해 `ARNavigation/secrets.properties`를 만든다.
6. `ARNavigation/secrets.properties`에 `MAPS_API_KEY=...` 형식으로 실제 키를 넣는다.
7. Gradle sync 후 `AndroidManifest.xml`의 `com.google.android.geo.API_KEY` 메타데이터로 키가 주입되는지 확인한다.
8. 키가 없더라도 빌드는 가능해야 하며, 실제 지도 렌더링 검증은 유효한 키를 넣은 뒤 수행한다.

## 3. 저장소 반영 규칙

- `secrets.properties`는 `.gitignore`에 유지하고 커밋하지 않는다.
- 예시 파일은 `secrets.properties.example`만 추적한다.
- 키는 Gradle property, `secrets.properties`, 환경 변수 `MAPS_API_KEY` 순서 중 하나로 공급할 수 있게 유지한다.
- 문서, 테스트 로그, 스크린샷에 실제 키 문자열을 남기지 않는다.

## 4. API 키 제한 정책

### 개발 키

- 키 이름 예시: `arnavigation-android-dev`
- Application restriction: `Android apps`
- 허용 앱:
  - package name: `com.wjs.arnav`
  - SHA-1 fingerprint: 각 개발자 로컬 debug keystore 또는 팀 공유 debug keystore의 SHA-1
- API restriction:
  - `Maps SDK for Android`
- 운영 원칙:
  - 개발자는 개인 debug keystore를 쓰거나 팀 공용 debug keystore를 명확히 관리한다.
  - 새 개발자가 합류하면 해당 SHA-1을 기존 dev key에 추가하거나 새 dev key를 발급한다.
  - 로컬 테스트와 CI용 키를 분리할 수 있으면 분리한다.

### 배포 키

- 키 이름 예시: `arnavigation-android-prod`
- Application restriction: `Android apps`
- 허용 앱:
  - package name: `com.wjs.arnav`
  - SHA-1 fingerprint: Google Play App Signing의 app signing certificate SHA-1
  - 추가 배포 채널이 있으면 해당 release keystore SHA-1만 별도 등록
- API restriction:
  - `Maps SDK for Android`
- 운영 원칙:
  - 개발 키와 배포 키를 절대 공유하지 않는다.
  - key rotation은 기존 키 사용량을 확인한 뒤 점진적으로 수행한다.
  - 배포 키는 테스트 앱, 웹, 서버 호출에 재사용하지 않는다.

## 5. SHA-1 확인 절차

### 로컬 debug / self-signed release

```powershell
cd ARNavigation
.\gradlew.bat signingReport
```

- 위 명령으로 debug keystore SHA-1을 확인할 수 있다.
- 별도 release keystore를 쓰면 `keytool -list -v -alias <alias> -keystore <path>`로 release SHA-1을 확인한다.

### Play App Signing 사용 시

- Google Play Console의 `Release > Setup > App integrity`에서 app signing certificate SHA-1을 확인한다.
- 배포 키 제한은 upload certificate가 아니라 실제 app signing certificate를 기준으로 잡는다.

## 6. 이번 변경으로 만든 코드 경로

- `ARNavigation/app/build.gradle.kts`
- `ARNavigation/app/src/main/AndroidManifest.xml`
- `ARNavigation/gradle/libs.versions.toml`
- `ARNavigation/secrets.properties.example`

## 7. 검증 체크리스트

- `.\gradlew.bat signingReport`가 실행되어 debug SHA-1 조회가 가능하다.
- `.\gradlew.bat testDebugUnitTest`가 통과한다.
- `.\gradlew.bat assembleDebug`가 통과한다.
- Git diff에 실제 API 키가 없다.

## 8. 참고

- Google Maps SDK for Android setup page, last updated 2026-08-07
- Google Maps Platform security guidance
- Google Play services client authentication guide
- `googlemaps/android-maps-compose` official releases
