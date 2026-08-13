# 단계 1: 프로젝트 및 빌드 기반 구성

- 상태: `in_progress`
- 선행 단계: 0
- 기본 스킬: $implement-android-foundation
- 상위 인덱스: [TodoList](../TodoList.md)

> 이 파일만 읽어 단계 1 작업을 수행한다. 전체 진행률은 `..\..\tools\todo-status.ps1`로 조회한다.

> 우선순위 변경: 이미 검증이 끝난 `S01-T025`~`S01-T027`은 관련 구현 변경이 생기기 전까지 다시 검증 후보로 올리지 않는다. 현재 단계 1의 남은 작업은 `S01-T007`, `S01-T009`이며, 구현 완료 기준으로 필요한 평가·확인 작업만 후속으로 진행한다.


### 1.1 의존성 구성

- [x] [S01-T001] Version Catalog에 Navigation Compose를 추가한다.
- [x] [S01-T002] Lifecycle ViewModel/Compose 및 lifecycle-aware state 수집 의존성을 추가한다.
- [x] [S01-T003] Kotlin Coroutines Android/Test 의존성을 추가한다.
- [x] [S01-T004] Google Maps SDK와 Maps Compose 의존성을 추가한다.
- [x] [S01-T005] Google Play services Location 의존성을 추가한다.
- [x] [S01-T006] ARCore 및 단계 0에서 결정한 3D 렌더링 의존성을 추가한다.
- [ ] [S01-T007] 단위 테스트용 coroutine-test와 assertion/mock 도구 필요 여부를 결정해 추가한다.
- [x] [S01-T008] Instrumentation/Compose UI 테스트 의존성을 정리한다.
- [ ] [S01-T009] 추가한 라이브러리의 최신 안정 버전, 라이선스, 최소/타깃 SDK 호환성을 확인한다.

### 1.2 API 키와 빌드 설정

- [x] [S01-T010] Maps API 키를 `local.properties` 또는 별도 비공개 속성에서 읽도록 구성한다.
- [x] [S01-T011] API 키를 BuildConfig 또는 Manifest placeholder로 안전하게 전달한다.
- [x] [S01-T012] 저장소에 커밋 가능한 샘플 설정 파일과 설정 방법을 문서화한다.
- [x] [S01-T013] 실제 API 키가 Git 추적 대상이 아님을 확인한다.
- [x] [S01-T014] debug/release 인증서별 API 키 제한과 빌드 방법을 문서화한다.

### 1.3 Manifest 및 리소스

- [x] [S01-T015] `CAMERA`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `INTERNET` 권한을 선언한다.
- [x] [S01-T016] 백그라운드 위치 권한을 선언하지 않았는지 확인한다.
- [x] [S01-T017] ARCore 앱 정책에 맞는 required/optional feature 및 metadata를 선언한다.
- [x] [S01-T018] Maps API 키 metadata를 application에 연결한다.
- [x] [S01-T019] 앱 이름, 권한 설명, 오류 메시지, 버튼 라벨을 문자열 리소스로 이동한다.
- [x] [S01-T020] 필요 시 화면 방향과 카메라 기능 요구사항을 Manifest에 설정한다.

### 1.4 패키지 구조와 공통 코드

- [x] [S01-T021] `app`, `domain`, `data`, `feature/map`, `feature/ar`, `core/ui` 등 패키지 경계를 만든다.
- [x] [S01-T022] `MainActivity`의 템플릿 UI를 제거하고 `ARNavigationApp` 루트 Composable로 교체한다.
- [x] [S01-T023] 공통 dispatcher/time provider를 추상화해 거리 유지 시간 로직을 테스트 가능하게 만든다.
- [x] [S01-T024] 개발용 로깅 정책을 만들고 정밀 위치 좌표가 release 로그에 남지 않게 한다.

### 1.5 기반 검증

- [x] [S01-T025] `assembleDebug`가 성공한다.
- [x] [S01-T026] `testDebugUnitTest`가 성공한다.
- [x] [S01-T027] API 키가 없는 환경에서도 원인을 알 수 있는 빌드 또는 실행 오류를 제공한다.
