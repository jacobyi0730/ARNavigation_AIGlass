---
name: implement-android-foundation
description: ARNavigation Android 프로젝트의 Gradle, Version Catalog, Manifest, Compose 앱 셸, 화면 전환, 권한, 준비 상태와 생명주기 기반을 구현한다. TodoList 단계 0·1·3·7·12의 공통 Android 기반 작업이나 빌드 설정 변경을 수행할 때 사용한다.
---

# Android 기반 구현

## 준비

1. `docs/TodoList.md`에서 현재 단계를 확인하고 배정된 `docs/tasks/stage-XX.md` 작업 ID와 선행 조건만 읽는다.
2. `docs/Retrospective.md`에서 Gradle, 권한, ARCore 호환성 관련 항목을 확인한다.
3. `ARNavigation/gradle/libs.versions.toml`, `app/build.gradle.kts`, Manifest, `MainActivity.kt`와 관련 테스트를 읽는다.
4. 라이브러리 버전이나 Android/Google API 동작을 확정해야 하면 공식 문서를 확인한다.

## 구현

- 기존 단일 Activity와 Compose 구조를 유지하고 UI 상태는 ViewModel에서 노출한다.
- Composable에 권한, 센서, 위치 또는 AR 세션 비즈니스 로직을 직접 보관하지 않는다.
- 의존성은 Version Catalog에 선언하고 최신 안정 버전 및 현재 SDK와의 호환성을 확인한다.
- Maps API 키는 비공개 속성에서 Manifest placeholder로 전달한다. 실제 키를 소스나 로그에 넣지 않는다.
- 카메라·정밀/대략 위치·인터넷만 필요한 범위로 선언하고 백그라운드 위치는 요청하지 않는다.
- 권한의 미요청, 허용, 일시 거부, 영구 거부와 위치 서비스/ARCore 준비 상태를 명시적으로 구분한다.
- foreground/background 전환에서 카메라, AR, 위치, 센서 소유자가 안전하게 pause/resume 하도록 계약을 만든다.
- 공용 파일을 바꿀 때 지도·AR·엔진 작업자의 진행 중 변경과 충돌하지 않는지 확인한다.

## 검증과 인계

1. 최소 `gradlew.bat testDebugUnitTest`와 `gradlew.bat assembleDebug`를 실행한다.
2. 권한 흐름을 변경했으면 관련 Compose/Instrumentation 테스트 또는 수동 검증 절차를 남긴다.
3. 실제 API 키가 Git diff에 없는지 확인한다.
4. 완료 조건을 충족한 단계 파일의 작업 ID만 체크한다.
5. 실패나 새 호환성 지식은 `$record-retrospective`로 기록한다.
