# ARNavigation 에이전트 지침 및 문서 인덱스

## 1. 목적과 적용 범위

이 문서는 저장소에서 작업하는 모든 에이전트의 시작점이다. 저장소 전체에 적용하며, `docs/TodoList.md`를 단일 작업 기준으로 삼아 오케스트레이터가 전문 에이전트에 기능 단위를 배정하고 각 에이전트는 프로젝트 스킬에 따라 구현·검증·회고한다.

작업을 시작하기 전에 이 문서와 배정된 작업에 필요한 세부 문서를 읽는다. 코드의 실제 상태가 문서와 다르면 임의로 한쪽을 선택하지 말고 차이를 확인하고, 이후 작업에 영향을 주는 차이는 `docs/Retrospective.md`에 기록한다.

---

## 2. 문서 인덱스

### 2.1 핵심 문서

| 문서 | 역할 | 읽는 시점 |
|---|---|---|
| [`docs/AR_내비게이션_앱_기획서.md`](docs/AR_내비게이션_앱_기획서.md) | 제품 목표, 화면 동작, 상태, 인수 조건 | 기능 의도와 완료 조건을 확인할 때 |
| [`docs/TodoList.md`](docs/TodoList.md) | 현재 단계와 세부 파일을 가리키는 경량 작업 인덱스 | 모든 구현·검증 작업의 시작과 종료 시 |
| [`docs/tasks/`](docs/tasks/) | 단계별 작업 ID와 체크박스, MVP 완료 정의와 백로그 | 배정된 단계 파일 하나만 작업 시 |
| [`docs/Retrospective.md`](docs/Retrospective.md) | 실패, 기술 결정, 공유 지식, 재발 방지 | 작업 시작 전 관련 항목 확인 및 실패·학습 발생 시 |
| [`ARNavigation/`](ARNavigation/) | Android 앱 소스와 빌드 설정 | 구현 및 실제 상태 확인 시 |
| [`tools/todo-status.ps1`](tools/todo-status.ps1) | Todo 전체를 읽지 않고 진행률과 작업 ID 조회 | 작업 선택과 완료 확인 시 |

문서의 용도별 기준은 다음과 같다.

1. 기능 의도와 사용자 경험은 기획서를 따른다.
2. 작업 순서와 완료 여부는 TodoList를 따른다.
3. 환경 제약과 기존 학습은 Retrospective를 따른다.
4. 이미 구현된 실제 동작은 코드와 테스트를 직접 확인한다.

### 2.2 원본 요청 문서

`mds/` 문서는 산출물의 입력 기록이다. 새로운 요청과 현재 핵심 문서가 충돌하면 최신 사용자 지시를 우선하고 핵심 문서도 함께 갱신한다.

| 문서 | 연결된 산출물 |
|---|---|
| [`mds/1. 기획요청.md`](mds/1.%20기획요청.md) | 앱 기획서 |
| [`mds/2. TodoList 요청.md`](mds/2.%20TodoList%20요청.md) | TodoList |
| [`mds/3. 회고문서 작성 요청.md`](mds/3.%20회고문서%20작성%20요청.md) | 스킬 구조와 Retrospective |
| [`mds/4. AGENTS.md 생성 요쳥.md`](mds/4.%20AGENTS.md%20생성%20요쳥.md) | 현재 AGENTS.md |
| [`mds/5. TodoList 관리 개선 요청.md`](mds/5.%20TodoList%20관리%20개선%20요청.md) | 경량 작업 인덱스와 단계별 Todo 구조 |

### 2.3 프로젝트 스킬 인덱스

각 전문 에이전트는 배정된 스킬의 `SKILL.md`를 끝까지 읽은 뒤 작업한다.

| 스킬 | 경로 | 담당 작업 |
|---|---|---|
| `$orchestrate-arnavigation` | [`skills/orchestrate-arnavigation/SKILL.md`](skills/orchestrate-arnavigation/SKILL.md) | 작업 선택, 분해, 배정, 통합 |
| `$implement-android-foundation` | [`skills/implement-android-foundation/SKILL.md`](skills/implement-android-foundation/SKILL.md) | Gradle, Manifest, 앱 셸, 권한, 생명주기 |
| `$implement-navigation-engine` | [`skills/implement-navigation-engine/SKILL.md`](skills/implement-navigation-engine/SKILL.md) | 도메인, 세션, 위치, 방향, 계산 엔진 |
| `$implement-map-workflow` | [`skills/implement-map-workflow/SKILL.md`](skills/implement-map-workflow/SKILL.md) | Maps Compose, 목적지·경유지 편집 |
| `$implement-ar-guidance` | [`skills/implement-ar-guidance/SKILL.md`](skills/implement-ar-guidance/SKILL.md) | ARCore 세션, 평면, 3D 방향 안내 |
| `$verify-arnavigation` | [`skills/verify-arnavigation/SKILL.md`](skills/verify-arnavigation/SKILL.md) | 빌드, 테스트, 인수 조건, 실기기 검증 |
| `$record-retrospective` | [`skills/record-retrospective/SKILL.md`](skills/record-retrospective/SKILL.md) | 실패, 결정, 학습, 재발 방지 기록 |

---

## 3. 오케스트레이터→에이전트→스킬 계층

```text
오케스트레이터
└─ $orchestrate-arnavigation
   ├─ Android 기반 에이전트 ─ $implement-android-foundation
   ├─ 내비게이션 엔진 에이전트 ─ $implement-navigation-engine
   ├─ 지도 에이전트 ─ $implement-map-workflow
   ├─ AR 에이전트 ─ $implement-ar-guidance
   └─ 검증 에이전트 ─ $verify-arnavigation
      └─ 모든 역할의 학습 기록 ─ $record-retrospective
```

### 3.1 역할과 소유권

| 역할 | 기본 스킬 | 주 소유 영역 |
|---|---|---|
| 오케스트레이터 | `$orchestrate-arnavigation` | 작업 분해, 공용 계약, 통합, TodoList |
| Android 기반 에이전트 | `$implement-android-foundation` | Gradle, Manifest, 앱 셸, 권한, 생명주기 |
| 내비게이션 엔진 에이전트 | `$implement-navigation-engine` | domain, session, location, orientation, 계산 로직 |
| 지도 에이전트 | `$implement-map-workflow` | Maps Compose, 목적지·경유지 UI |
| AR 에이전트 | `$implement-ar-guidance` | ARCore session, plane, renderer, AR overlay |
| 검증 에이전트 | `$verify-arnavigation` | test, 인수 조건, 기기 검증 결과 |

모든 역할은 실패나 재사용할 지식이 생기면 `$record-retrospective`를 함께 사용한다.

### 3.2 오케스트레이션 규칙

- 한 에이전트에는 하나의 관찰 가능한 결과와 명확한 완료 조건만 배정한다.
- 사용자 또는 상위 지침이 병렬 에이전트 작업을 허용한 경우에만 하위 에이전트를 생성한다.
- 병렬 작업은 수정 경로가 겹치지 않을 때만 수행한다.
- `libs.versions.toml`, Gradle 파일, Manifest, 공용 도메인 모델, 앱 내비게이션 그래프, 작업 인덱스·단계 파일, `Retrospective.md`는 공통 충돌 지점이다. 병렬 작업 중에는 오케스트레이터 또는 지정된 한 명만 수정한다.
- 공용 인터페이스가 필요한 작업은 오케스트레이터가 계약을 먼저 확정한 뒤 전문 작업을 시작한다.
- 에이전트가 범위 밖의 문제를 발견하면 임의로 확장하지 말고 증거와 후속 작업을 인계한다.
- AR·GPS·나침반 실기기 항목은 mock, 빌드 또는 에뮬레이터 결과만으로 완료 처리하지 않는다.

---

## 4. 공통 작업 절차

### 4.1 시작

1. `git status --short`로 작업 트리 상태를 확인하고 사용자 변경을 보존한다.
2. `docs/TodoList.md`에서 현재 단계와 선행 조건을 확인한다.
3. `.\tools\todo-status.ps1 -Stage <번호> -PendingOnly`로 후보를 조회한다.
4. 배정된 `docs/tasks/stage-XX.md` 하나와 필요한 기획서 절만 읽는다.
5. `rg`로 Retrospective의 관련 실패와 미완료 후속 조치만 찾는다.
6. 배정된 스킬의 `SKILL.md`를 끝까지 읽는다.
7. 관련 소스와 테스트를 읽은 뒤 가장 작은 안전한 변경부터 구현한다.

### 4.2 구현

- Composable, ViewModel, Repository, 도메인 로직의 책임을 분리한다.
- 기존 코드와 사용자 변경을 보존하고 범위 밖의 리팩터링을 섞지 않는다.
- 공개 계약을 변경하면 소비자와 테스트를 같은 작업에서 갱신한다.
- 외부 SDK와 라이브러리의 버전·동작을 확정해야 하면 공식 문서를 기준으로 확인한다.
- API 키, 인증 정보, 정밀 좌표와 개인정보를 소스 또는 로그에 남기지 않는다.

### 4.3 검증

- 순수 로직 변경은 관련 단위 테스트를 실행한다.
- Android 통합 변경은 최소 `gradlew.bat testDebugUnitTest`와 `gradlew.bat assembleDebug`를 검토 범위에 포함한다.
- UI 변경은 가능한 경우 Compose UI 테스트를 실행한다.
- 권한, 지도, AR, GPS와 센서 기능은 필요 수준에 따라 계측 또는 실기기 검증을 수행한다.
- 실행하지 못한 검증은 통과한 것처럼 표현하지 않고 이유와 남은 위험을 인계한다.

### 4.4 Todo 체크박스 갱신

- 작업 시작 시 안정적인 작업 ID와 하위 완료 조건을 식별한다.
- 코드 구현과 해당 검증이 모두 완료된 항목만 `- [ ]`에서 `- [x]`로 변경한다.
- 부분 완료, mock만 완료, 검증 실패 또는 실기기 미확인 항목은 체크하지 않는다.
- 일부 하위 작업만 끝났다면 완료한 하위 항목만 체크하고 상위 완료 조건은 유지한다.
- 새로 발견한 필수 작업은 해당 단계 파일의 마지막 번호 다음 ID로 추가하고 선행 관계를 명시한다.
- 차단된 작업은 체크하지 않고 원인과 후속 조치를 Retrospective에 기록한다.
- 병렬 작업 중 단계 파일과 `docs/TodoList.md`는 오케스트레이터 또는 지정된 한 명만 갱신한다.
- 개별 작업 완료 시에는 단계 파일과 `docs/TodoList.md`의 현재 단계 체크박스를 같은 턴에 함께 갱신한다.
- 단계 상태·활성 작업·다음 후보가 바뀌면 `docs/TodoList.md` 상단 상태 표도 같은 턴에 갱신한다.
- 작업 종료 전 diff를 확인해 실제 완료 항목과 체크박스가 일치하는지 검수한다.
- 진행률 확인을 위해 모든 단계 파일을 한 번에 읽지 말고 상태 스크립트의 집계 결과를 사용한다.

### 4.5 인계 형식

각 에이전트는 다음 순서로 결과를 반환한다.

1. 완료한 결과
2. 변경한 파일
3. 실행한 검증과 결과
4. 실행하지 못한 검증과 이유
5. 남은 위험 또는 후속 작업
6. TodoList에서 갱신한 항목
7. Retrospective 기록 여부와 항목 ID

---

## 5. Android 코딩 컨벤션

### 5.1 Kotlin 기본 스타일

- Kotlin 공식 스타일과 Android Kotlin 스타일을 따르며 들여쓰기는 공백 4칸을 사용한다.
- 일반 코드의 한 줄은 100자 이내를 기본으로 한다. URL, import처럼 분리할 수 없는 경우만 예외로 둔다.
- wildcard import를 사용하지 않고 사용하지 않는 import를 남기지 않는다.
- 패키지 이름은 소문자, 클래스·인터페이스·object·Composable은 `PascalCase`, 함수·변수·프로퍼티는 `camelCase`, 상수는 `UPPER_SNAKE_CASE`를 사용한다.
- 파일 이름은 주 public 타입 또는 주요 Composable 이름과 일치시킨다.
- 불변 `val`과 불변 컬렉션을 우선하고 변경 가능한 상태의 소유 범위를 최소화한다.
- nullable 값, 예외, 실패 상태를 숨기지 말고 타입 또는 명시적인 결과로 표현한다.
- `!!`와 비지역적 상태를 피한다. 불가피하면 안전성이 보장되는 이유를 인접 코드에서 드러낸다.
- 함수는 한 가지 책임을 가지며 불명확한 축약어와 boolean 의미가 모호한 인자를 피한다.
- public 계약과 복잡한 좌표·센서 계산에는 의도와 단위를 설명하는 KDoc을 작성한다. 코드가 그대로 말하는 내용을 반복해서 주석 처리하지 않는다.

### 5.2 Jetpack Compose

- UI는 `UiState`와 이벤트를 받는 단방향 데이터 흐름으로 작성하고 상태를 가능한 상위로 끌어올린다.
- 화면 Composable은 `Route`와 순수한 `Screen/Content` 영역으로 분리해 Preview와 테스트가 가능하게 한다.
- UI를 그리는 Composable에서 위치, 센서, AR 세션, 네트워크 또는 저장소를 직접 생성·소유하지 않는다.
- Composable 이름은 명사형 `PascalCase`로 작성하고 값을 반환하기보다 UI를 표현한다.
- `Modifier`는 첫 번째 선택 매개변수로 받고 호출자가 전달한 modifier를 최상위 노드에 적용한다.
- side effect는 `LaunchedEffect`, `DisposableEffect`, `rememberUpdatedState` 등 목적에 맞는 API로 제한한다.
- Flow는 수명주기를 고려해 수집하고, 재구성마다 구독이나 객체가 새로 생성되지 않게 한다.
- 표시 문자열과 접근성 설명을 하드코딩하지 않고 문자열 리소스를 사용한다.
- 주요 화면과 재사용 컴포넌트에 대표 상태 Preview를 제공한다.
- 색상만으로 상태를 구분하지 않으며 최소 48 dp 터치 영역과 TalkBack 설명을 제공한다.

### 5.3 아키텍처와 상태

- `domain`은 Android UI·지도·AR SDK 타입에 의존하지 않게 한다.
- SDK의 위치·센서·지도·AR 타입은 data 또는 feature 경계에서 도메인 타입으로 변환한다.
- ViewModel이 화면 상태와 사용자 이벤트를 관리하고 Repository가 외부 데이터 소스를 캡슐화한다.
- 화면 간에 가변 객체를 route 인자로 전달하지 않고 ID 또는 공유 상태 소유자를 사용한다.
- 확정된 `NavigationSession`과 지도 편집 draft를 분리한다.
- 시간, dispatcher와 센서/위치 공급자는 테스트 가능한 인터페이스로 주입한다.
- 상태는 불변 모델과 `StateFlow`를 우선하며 일회성 메시지·화면 이동 효과를 지속 상태와 구분한다.

### 5.4 코루틴과 Flow

- 구조화된 동시성을 사용하고 임의의 전역 CoroutineScope를 만들지 않는다.
- 메인 스레드에서 블로킹 I/O나 무거운 거리·렌더링 계산을 수행하지 않는다.
- Coroutine dispatcher를 하드코딩하지 말고 테스트가 필요한 계층에서는 주입한다.
- Flow의 수집은 소유자의 생명주기에 맞춰 시작·중지하고 위치·센서 구독 중복을 방지한다.
- 취소를 정상 제어 흐름으로 취급하고 `CancellationException`을 일반 오류로 삼키지 않는다.

### 5.5 Android 리소스와 보안

- 리소스 이름은 소문자 `snake_case`를 사용하고 기능 접두어를 붙여 충돌을 줄인다.
- 사용자에게 보이는 텍스트, 단위, 오류 문구는 리소스로 관리한다.
- Maps API 키는 비공개 속성과 Manifest placeholder를 통해 제공하고 Git에 커밋하지 않는다.
- 백그라운드 위치 권한은 기획 범위에 없으므로 선언하거나 요청하지 않는다.
- release 로그에 정밀 좌표, API 키, 센서 원시 데이터와 개인정보를 남기지 않는다.
- 3D 모델과 외부 자산은 출처와 라이선스를 기록한다.

### 5.6 테스트 규칙

- 순수 계산과 상태 머신은 JVM 단위 테스트를 우선한다.
- 테스트 이름은 조건과 기대 결과가 드러나게 작성하고 필요하면 Kotlin backtick 이름을 사용한다.
- 시간·위치·방향·Repository는 Fake 또는 test dispatcher로 결정론적으로 제어한다.
- 버그 수정에는 가능하면 실패를 재현하는 회귀 테스트를 먼저 추가한다.
- 지도 SDK 렌더링, ARCore, 실제 GPS와 나침반 동작은 단위 테스트 결과로 대체하지 않는다.
- 테스트가 환경에 의존하면 필요한 기기, 권한, API 키와 재현 절차를 인계에 기록한다.

---

## 6. 회고 의무

다음 상황이 발생하면 같은 작업 안에서 `docs/Retrospective.md`를 갱신하고 `$record-retrospective` 지침을 따른다.

- 빌드, 테스트, 명령, 도구 또는 실기기 검증 실패
- 잘못된 전제나 문서·코드 불일치
- SDK, 기기, 권한, 좌표계 또는 생명주기의 비직관적 제약
- 반복될 가능성이 있는 우회 또는 미해결 문제
- 다른 에이전트의 설계·순서·검증에 영향을 주는 결정이나 학습

해결과 재발 방지 검증이 끝난 경우에만 회고 상태를 `Resolved`로 변경한다.
