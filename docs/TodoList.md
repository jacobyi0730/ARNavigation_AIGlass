# ARNavigation 작업 인덱스

## 1. 목적

이 문서는 전체 작업의 상태와 다음 읽을 파일만 제공하는 경량 제어 문서다. 세부 체크박스는 `docs/tasks/stage-XX.md`에 분리되어 있다. 에이전트는 이 파일과 현재 단계 파일만 읽고, 모든 단계 파일을 한 번에 읽지 않는다.

기능 의도와 인수 조건은 [앱 기획서](./AR_내비게이션_앱_기획서.md), 실패와 공유 지식은 [Retrospective](./Retrospective.md), 에이전트 운영 규칙은 [AGENTS.md](../AGENTS.md)를 따른다.

## 2. 토큰 절약 작업 절차

1. 이 인덱스에서 현재 단계와 선행 조건만 확인한다.
2. `tools/todo-status.ps1`로 진행률 또는 작업 ID를 조회한다.
3. 배정된 `docs/tasks/stage-XX.md` 하나만 읽는다.
4. 필요한 기획 내용은 제목·요구사항 ID로 검색해 해당 절만 읽는다.
5. 관련 회고는 키워드 또는 회고 ID로 검색해 해당 항목만 읽는다.
6. 구현과 검증이 끝난 작업 ID의 체크박스만 갱신한다.
7. 이미 요구 수준까지 검증을 끝낸 작업은 새 변경이 생기기 전까지 재검증 우선순위로 다시 올리지 않는다.
8. 구현 작업과 직접 연결된 선행 구성을 먼저 진행하고, 후속 검증·평가성 작업은 구현 이후로 미룬다.
9. 단계 상태나 다음 작업이 바뀔 때만 이 인덱스를 갱신한다.

### 권장 조회 명령

```powershell
# 전체 단계별 완료 수만 조회
.\tools\todo-status.ps1

# 현재 단계의 미완료 작업만 조회
.\tools\todo-status.ps1 -Stage 0 -PendingOnly

# 한 작업만 조회
.\tools\todo-status.ps1 -TaskId S00-T001

# 관련 회고만 검색
rg -n "ARCore|권한|RET-" docs/Retrospective.md
```

> 체크박스 수정은 스크립트가 아니라 `apply_patch`로 해당 작업 ID 한 줄만 변경한다. 작업 ID는 제목이 바뀌어도 유지하고, 새 작업에는 해당 단계의 마지막 번호 다음 ID를 부여한다.

## 3. 현재 상태

| 항목 | 상태 |
|---|---|
| 현재 단계 | 단계 1: 프로젝트 및 빌드 기반 구성 |
| 현재 단계 상태 | `in_progress` |
| 활성 작업 | `S01-T007`, `S01-T009` |
| 다음 후보 | 단계 1 완료 검토 |
| 전체 구현 작업 | 단계 파일 15개, 체크박스 276개 |
| 병렬 작업 체크리스트 | [coordination.md](./tasks/coordination.md) |
| MVP 완료 정의 | [mvp-dod.md](./tasks/mvp-dod.md) |
| MVP 이후 백로그 | [backlog.md](./tasks/backlog.md) |
| 프로토타입 상태 | 폰에서 AR 화면, 지도 화면, AR↔Map 전환, 지도 탭 목적지/경유지 편집 확인 가능 |

### 프로젝트 기준선

| 항목 | 상태 |
|---|---|
| 단일 `app` 모듈 | 완료 |
| Kotlin/Jetpack Compose/Material 3 기본 설정 | 완료 |
| 패키지/application ID `com.wjs.arnav` | 완료 |
| `minSdk 31`, `targetSdk 37`, `compileSdk 37` | 완료 |
| `testDebugUnitTest` 기준 실행 | 2026-08-12 성공 |
| 기능 구현 | AR↔Map 프로토타입 화면 전환 및 지도 탭 마커 편집 가능 |

### 확정된 MVP 정책

| 정책 | 결정 |
|---|---|
| 경로 방식 | 도로 탐색이 아닌 사용자 지정 좌표의 직선 순서 안내 |
| 경유지 순서 | 지도에 추가한 순서 |
| 진행 중 경로 편집 | 불가, 지도 조회만 허용 |
| ARCore 미지원 정책 | 지도 전용 제한 모드 허용, AR 진입 차단 |
| 화면 방향 | 세로 모드 고정 |
| 경유지 중복/개수 | 15 m 이내 중복 금지, 최대 5개 |
| 시작 정확도 초과 정책 | 25 m 초과 시 경고 후 사용자 확인 시에만 시작 |
| 종료/도착 초기화 | 종료 확인 후 세션 초기화, 도착 확인 후 새 세션 상태로 복귀 |
| 뒤로가기 종료 정책 | 2초 이내 두 번 연속 터치 시에만 종료, 첫 터치 시 안내 메시지 표시 |
| 지도 현재 위치 버튼 | 지도 화면에서 현재 위치 중심으로 다시 이동하는 버튼 기본 제공 |
| 2D 보조 화살표 | MVP 제외 |
| 강제 종료 후 세션 복원 | MVP 제외 |
| 도착 판정 초기값 | 10 m 반경 안에서 3초 연속 유지 |
| 위치 품질 경고 초기값 | 정확도 25 m 초과 |

남은 미확정 정책은 단계 0의 실기기 검증 항목과 함께 좁힌다.

## 4. 진행 흐름 체크박스

- [x] 단계 0: 정책 확정 및 기술 스파이크 (`complete`) [stage-00.md](./tasks/stage-00.md)
- [ ] 단계 1: 프로젝트 및 빌드 기반 구성 (`in_progress`) [stage-01.md](./tasks/stage-01.md)
- [ ] 단계 2: 도메인·세션 상태 (`blocked`) [stage-02.md](./tasks/stage-02.md)
- [ ] 단계 3: 권한·기기 준비 상태 (`blocked`) [stage-03.md](./tasks/stage-03.md)
- [ ] 단계 4: 위치 기능 (`blocked`) [stage-04.md](./tasks/stage-04.md)
- [ ] 단계 5: 방향 센서 기능 (`blocked`) [stage-05.md](./tasks/stage-05.md)
- [ ] 단계 6: 내비게이션 엔진 (`blocked`) [stage-06.md](./tasks/stage-06.md)
- [ ] 단계 7: 앱 셸·화면 전환 (`blocked`) [stage-07.md](./tasks/stage-07.md)
- [ ] 단계 8: 지도 기능 (`blocked`) [stage-08.md](./tasks/stage-08.md)
- [ ] 단계 9: AR 기반 기능 (`blocked`) [stage-09.md](./tasks/stage-09.md)
- [ ] 단계 10: AR 내비게이션 UI 통합 (`blocked`) [stage-10.md](./tasks/stage-10.md)
- [ ] 단계 11: 지도↔AR 전체 흐름 통합 (`blocked`) [stage-11.md](./tasks/stage-11.md)
- [ ] 단계 12: 오류 처리·생명주기·접근성 (`blocked`) [stage-12.md](./tasks/stage-12.md)
- [ ] 단계 13: 테스트 및 현장 검증 (`blocked`) [stage-13.md](./tasks/stage-13.md)
- [ ] 단계 14: 릴리스 준비 (`blocked`) [stage-14.md](./tasks/stage-14.md)

### 현재 단계 체크박스

 - [x] [S01-T001] Version Catalog에 Navigation Compose를 추가한다.
 - [x] [S01-T002] Lifecycle ViewModel/Compose 및 lifecycle-aware state 수집 의존성을 추가한다.
 - [x] [S01-T003] Kotlin Coroutines Android/Test 의존성을 추가한다.
 - [x] [S01-T004] Google Maps SDK와 Maps Compose 의존성을 추가한다.
 - [x] [S01-T005] Google Play services Location 의존성을 추가한다.
 - [x] [S01-T006] ARCore 및 단계 0에서 결정한 3D 렌더링 의존성을 추가한다.
 - [ ] [S01-T007] 단위 테스트용 coroutine-test와 assertion/mock 도구 필요 여부를 결정해 추가한다.
 - [x] [S01-T008] Instrumentation/Compose UI 테스트 의존성을 정리한다.
 - [ ] [S01-T009] 추가한 라이브러리의 최신 안정 버전, 라이선스, 최소/타깃 SDK 호환성을 확인한다.
 - [x] [S01-T010] Maps API 키를 `local.properties` 또는 별도 비공개 속성에서 읽도록 구성한다.
 - [x] [S01-T011] API 키를 BuildConfig 또는 Manifest placeholder로 안전하게 전달한다.
 - [x] [S01-T012] 저장소에 커밋 가능한 샘플 설정 파일과 설정 방법을 문서화한다.
 - [x] [S01-T013] 실제 API 키가 Git 추적 대상이 아님을 확인한다.
 - [x] [S01-T014] debug/release 인증서별 API 키 제한과 빌드 방법을 문서화한다.
 - [x] [S01-T015] `CAMERA`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `INTERNET` 권한을 선언한다.
 - [x] [S01-T016] 백그라운드 위치 권한을 선언하지 않았는지 확인한다.
 - [x] [S01-T017] ARCore 앱 정책에 맞는 required/optional feature 및 metadata를 선언한다.
 - [x] [S01-T018] Maps API 키 metadata를 application에 연결한다.
 - [x] [S01-T019] 앱 이름, 권한 설명, 오류 메시지, 버튼 라벨을 문자열 리소스로 이동한다.
 - [x] [S01-T020] 필요 시 화면 방향과 카메라 기능 요구사항을 Manifest에 설정한다.
 - [x] [S01-T021] `app`, `domain`, `data`, `feature/map`, `feature/ar`, `core/ui` 등 패키지 경계를 만든다.
 - [x] [S01-T022] `MainActivity`의 템플릿 UI를 제거하고 `ARNavigationApp` 루트 Composable로 교체한다.
 - [x] [S01-T023] 공통 dispatcher/time provider를 추상화해 거리 유지 시간 로직을 테스트 가능하게 만든다.
 - [x] [S01-T024] 개발용 로깅 정책을 만들고 정밀 위치 좌표가 release 로그에 남지 않게 한다.
 - [x] [S01-T025] `assembleDebug`가 성공한다.
 - [x] [S01-T026] `testDebugUnitTest`가 성공한다.
 - [x] [S01-T027] API 키가 없는 환경에서도 원인을 알 수 있는 빌드 또는 실행 오류를 제공한다.

### 완료한 이전 단계 체크박스

- [x] [S00-T001] 위의 미확정 MVP 정책 3건을 결정하고 기획서 또는 ADR에 반영한다.
- [x] [S00-T002] 지원 화면 방향을 세로 모드로 고정할지 확정한다.
- [x] [S00-T003] 목적지/경유지 최소 중복 거리와 최대 경유지 개수를 확정한다.
- [x] [S00-T004] 위치 정확도가 25 m를 초과할 때 시작을 차단할지, 경고 후 허용할지 확정한다.
- [x] [S00-T005] 길찾기 종료 확인 다이얼로그와 목적지 도착 후 초기화 방식을 확정한다.
- [x] [S00-T006] Google Cloud 프로젝트와 Android용 Maps SDK 사용 설정 절차를 정리한다.
- [x] [S00-T007] 개발/배포용 Maps API 키 제한 정책을 패키지명과 인증서 지문 기준으로 정한다.
- [x] [S00-T008] ARCore가 현재 `minSdk 31`, `targetSdk 37` 및 선택할 렌더링 계층에서 빌드·실행되는지 검증한다.
- [x] [S00-T009] 선택할 3D 렌더링 계층으로 Compose 화면 내부에 AR 카메라 뷰를 포함할 수 있는지 스파이크를 만든다.
- [x] [S00-T010] 수평 상향 평면 탐지와 평면 위 화살표 모델 렌더링이 가능한지 실기기에서 검증한다.
- [x] [S00-T011] 지도 화면과 AR 화면을 전환했을 때 카메라/AR 세션이 정상 pause/resume 되는지 검증한다.
- [x] [S00-T012] 기술 선택과 미확정 정책이 문서화되어 이후 작업자가 동일한 전제를 사용한다.
- [x] [S00-T013] 지원 대상 실기기에서 지도 및 AR 최소 샘플이 각각 실행된다.

## 5. 단계 인덱스

`ready`는 바로 시작 가능, `blocked`는 선행 단계가 남음, `in_progress`는 활성 작업 존재, `complete`는 모든 필수 체크와 검증 완료를 뜻한다. 동적 완료 수는 상태 조회 스크립트를 사용한다.

| 단계 | 상태 | 선행 단계 | 기본 스킬 | 세부 작업 |
|---:|---|---|---|---|
| 0 | `complete` | 없음 | `$orchestrate-arnavigation`, `$implement-android-foundation` | [stage-00.md](./tasks/stage-00.md) |
| 1 | `in_progress` | 0 | `$implement-android-foundation` | [stage-01.md](./tasks/stage-01.md) |
| 2 | `blocked` | 1 | `$implement-navigation-engine` | [stage-02.md](./tasks/stage-02.md) |
| 3 | `blocked` | 1 | `$implement-android-foundation` | [stage-03.md](./tasks/stage-03.md) |
| 4 | `blocked` | 1 | `$implement-navigation-engine` | [stage-04.md](./tasks/stage-04.md) |
| 5 | `blocked` | 1 | `$implement-navigation-engine` | [stage-05.md](./tasks/stage-05.md) |
| 6 | `blocked` | 2, 4, 5의 계약 | `$implement-navigation-engine` | [stage-06.md](./tasks/stage-06.md) |
| 7 | `blocked` | 2, 3 | `$implement-android-foundation` | [stage-07.md](./tasks/stage-07.md) |
| 8 | `blocked` | 2, 4, 7 | `$implement-map-workflow` | [stage-08.md](./tasks/stage-08.md) |
| 9 | `blocked` | 3, 5, 7 | `$implement-ar-guidance` | [stage-09.md](./tasks/stage-09.md) |
| 10 | `blocked` | 6, 9 | `$implement-ar-guidance` | [stage-10.md](./tasks/stage-10.md) |
| 11 | `blocked` | 8, 10 | `$orchestrate-arnavigation` | [stage-11.md](./tasks/stage-11.md) |
| 12 | `blocked` | 11 | `$implement-android-foundation`, `$verify-arnavigation` | [stage-12.md](./tasks/stage-12.md) |
| 13 | `blocked` | 단계별 누적 | `$verify-arnavigation` | [stage-13.md](./tasks/stage-13.md) |
| 14 | `blocked` | 12, 13 | `$orchestrate-arnavigation`, `$verify-arnavigation` | [stage-14.md](./tasks/stage-14.md) |

## 6. 상태 갱신 규칙

- 코드 구현과 요구 수준의 검증이 끝난 작업만 `[x]`로 변경한다.
- 부분 완료, mock만 완료, 검증 실패, 실기기 미확인 작업은 `[ ]`로 유지한다.
- 새 작업은 해당 단계 파일에 안정적인 ID와 함께 추가한다.
- 작업을 시작하면 단계 파일과 이 인덱스의 상태를 `in_progress`로 바꾸고 활성 작업 ID를 적는다.
- 이미 검증 완료한 작업은 관련 구현이 다시 바뀌지 않는 한 재검증 후보로 되돌리지 않는다.
- 구현 우선순위가 검증·평가성 작업보다 앞선다. 검증 작업은 해당 구현이 준비된 뒤에만 활성화한다.
- 단계의 필수 작업과 검증이 모두 끝나면 단계 파일과 이 인덱스를 `complete`로 바꾸고 후속 단계의 차단을 재평가한다.
- 차단 원인이나 다음 작업자가 알아야 할 제약은 Retrospective에 기록한다.
- 병렬 작업 중 단계 파일과 이 인덱스는 오케스트레이터 또는 지정된 한 명만 갱신한다.
- 병렬 작업을 실제로 시작할 때만 `coordination.md`를 읽고 해당 체크리스트를 사용한다.
