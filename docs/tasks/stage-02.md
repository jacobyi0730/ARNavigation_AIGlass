# 단계 2: 도메인 모델 및 세션 상태 관리

- 상태: `blocked`
- 선행 단계: 1
- 기본 스킬: $implement-navigation-engine
- 상위 인덱스: [TodoList](../TodoList.md)

> 이 파일만 읽어 단계 2 작업을 수행한다. 전체 진행률은 `..\..\tools\todo-status.ps1`로 조회한다.


### 2.1 도메인 모델

- [ ] [S02-T001] `GeoPoint(latitude, longitude)` 값 객체를 구현하고 좌표 범위를 검증한다.
- [ ] [S02-T002] `NavigationPoint`와 `PointType(START, WAYPOINT, DESTINATION)`을 구현한다.
- [ ] [S02-T003] `ArrivalStatus(PENDING, ARRIVED, SKIPPED)`를 구현한다.
- [ ] [S02-T004] `NavigationStatus(IDLE, EDITING, NAVIGATING, PAUSED_QUALITY, ARRIVED)`를 구현한다.
- [ ] [S02-T005] `NavigationSession`을 불변 데이터 모델로 구현한다.
- [ ] [S02-T006] 도착 반경, 유지 시간, 위치 품질 기준을 `NavigationConfig`로 분리한다.

### 2.2 지도 편집 상태

- [ ] [S02-T007] `MapEditMode(DEFAULT, DESTINATION, WAYPOINT)`를 구현한다.
- [ ] [S02-T008] 확정 데이터와 편집용 draft 데이터를 분리한다.
- [ ] [S02-T009] 목적지 지정 시 기존 목적지가 하나만 유지되도록 한다.
- [ ] [S02-T010] 경유지를 추가 순서대로 저장하고 화면용 순번을 계산한다.
- [ ] [S02-T011] 경유지 삭제 후 남은 순서를 재정렬한다.
- [ ] [S02-T012] 최소 거리 이내 중복 경유지를 거부하는 정책을 구현한다.
- [ ] [S02-T013] 편집 완료/취소 이벤트를 구현하고 취소 시 원본 세션을 복원한다.

### 2.3 세션 상태 머신

- [ ] [S02-T014] `IDLE → EDITING → NAVIGATING → ARRIVED → IDLE` 정상 전이를 구현한다.
- [ ] [S02-T015] 센서 품질 저하에 따른 `NAVIGATING ↔ PAUSED_QUALITY` 전이를 구현한다.
- [ ] [S02-T016] 잘못된 상태 전이를 거부하거나 명시적 오류로 처리한다.
- [ ] [S02-T017] 시작 시 최신 유효 위치를 시작점으로 고정한다.
- [ ] [S02-T018] 진행 중 지도 조회에서는 세션을 수정할 수 없게 한다.
- [ ] [S02-T019] 사용자 안내 종료 시 위치/센서/AR 리소스를 해제할 수 있는 종료 이벤트를 제공한다.

### 2.4 ViewModel 계약

- [ ] [S02-T020] 화면에서 사용할 단일 `NavigationUiState` 또는 화면별 파생 UiState를 정의한다.
- [ ] [S02-T021] UI 이벤트와 일회성 효과(메시지, 화면 이동, 설정 열기)를 분리한다.
- [ ] [S02-T022] `SavedStateHandle`로 최소 편집/화면 상태 복구 범위를 정의한다.
- [ ] [S02-T023] 상태 저장소 인터페이스와 인메모리 구현을 먼저 제공한다.

### 단계 2 테스트

- [ ] [S02-T024] 목적지 교체 테스트를 작성한다.
- [ ] [S02-T025] 경유지 추가·삭제·재정렬 테스트를 작성한다.
- [ ] [S02-T026] 편집 취소 시 원본 복원 테스트를 작성한다.
- [ ] [S02-T027] 정상/비정상 상태 전이 테스트를 작성한다.
