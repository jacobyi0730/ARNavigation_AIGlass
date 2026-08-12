# ARNavigation 회고 및 공유 지식

## 1. 목적

작업 중 발생한 실패와 해결 과정, 기술 결정, 다음 에이전트가 반드시 알아야 할 제약을 누적한다. 개인 평가가 아니라 재현 가능한 사실과 재발 방지에 집중하며, 각 에이전트는 작업 전에 자신의 영역과 관련된 항목을 확인한다.

## 2. 기록 기준

다음 중 하나에 해당하면 회고를 작성하거나 기존 항목을 갱신한다.

- 명령, 빌드, 테스트, 도구 또는 실기기 검증 실패
- 기존 가정과 실제 환경의 불일치
- 반복될 가능성이 있는 오류나 임시 우회
- SDK·권한·기기·좌표계·생명주기의 비직관적 제약
- 후속 에이전트의 설계, 작업 순서 또는 검증 방법을 바꾸는 결정

API 키, 인증 비밀, 정밀 위치와 개인정보는 기록하지 않는다. 전체 로그 대신 핵심 오류, 재현 명령, 관련 경로만 남긴다.

## 3. 현재 공유 지식

| 구분 | 내용 |
|---|---|
| 프로젝트 | `ARNavigation` 단일 `app` 모듈, 패키지 `com.wjs.arnav` |
| 초기 상태 | Jetpack Compose 기본 템플릿이며 지도·위치·AR 의존성은 아직 미구성 |
| SDK 기준 | `minSdk 31`, `targetSdk 37`, `compileSdk 37` |
| 기준 검증 | 2026-08-12 `gradlew.bat testDebugUnitTest` 성공 |
| MVP 경로 | 도로 탐색이 아닌 사용자가 지정한 경유지 순서의 직선 방향 안내 |
| 도착 정책 | 초기값 10 m 반경 안에서 3초 연속 유지 |
| 품질 정책 | 위치 정확도 25 m 초과 시 낮은 품질로 취급 |
| 보안 | Maps API 키와 정밀 좌표를 저장소·release 로그에 남기지 않음 |
| 실기기 | ARCore 평면, GPS, 나침반, 방향 오차와 FPS는 지원 실기기 검증 필수 |
| 작업 트리 | 저장소 초기 파일 다수가 아직 추적/커밋되지 않았으므로 기존 파일을 사용자 자산으로 간주하고 보존 |

## 4. 확정 결정

### DEC-001: 기능 작업 기준

- 상태: Active
- 결정: `docs/TodoList.md`를 작업 순서와 완료 판정의 단일 기준으로 사용한다.
- 이유: 기획 요구사항과 구현·테스트 항목을 추적 가능하게 연결하기 위해서다.

### DEC-002: 에이전트와 스킬 계층

- 상태: Active
- 결정: 루트 오케스트레이터가 기능 단위를 선택하고 Android 기반, 내비게이션 엔진, 지도, AR, 검증 전문 에이전트에 배정한다. 각 역할은 `skills/`의 동명 목적 스킬을 따른다.
- 이유: 기능 경계를 유지하고 병렬 작업의 수정 충돌을 줄이기 위해서다.

### DEC-003: 공용 파일 소유권

- 상태: Active
- 결정: 병렬 작업 중 Version Catalog, Gradle, Manifest, 공용 모델, 앱 내비게이션 그래프, TodoList와 본 회고 문서는 지정된 한 명만 수정한다.
- 이유: 기능 에이전트 간 계약 불일치와 병합 충돌을 예방하기 위해서다.

### DEC-004: Todo 점진적 공개 구조

- 상태: Active
- 결정: `docs/TodoList.md`는 현재 단계와 링크만 제공하고, 세부 체크박스는 `docs/tasks/stage-XX.md`로 분리한다. 진행률과 작업 ID는 `tools/todo-status.ps1`로 조회한다.
- 이유: 매 작업마다 수백 개 체크박스를 모델 컨텍스트에 반복 적재하지 않고 현재 기능에 필요한 범위만 읽기 위해서다.

## 5. 회고 로그

### RET-2026-08-12-004: Maps Compose 추가 시 Kotlin 메타데이터 버전 충돌

- 상태: Resolved
- 단계: 단계 0 `S00-T006`, `S00-T007`
- 맥락: Maps SDK 설정 경로와 `maps-compose` 의존성을 프로젝트에 추가한 뒤 빌드를 검증했다.
- 증상/증거: `.\gradlew.bat testDebugUnitTest`와 `.\gradlew.bat assembleDebug`에서 `maps-compose-8.4.0` 및 `kotlin-stdlib-2.4.10`이 Kotlin metadata `2.4.0`으로 컴파일되었는데 프로젝트 Kotlin 플러그인이 `2.2.10`이라 `compileDebugKotlin`이 실패했다.
- 영향: 지도 SDK 준비 작업 직후 앱 빌드가 깨져 후속 단계가 바로 막혔다.
- 근본 원인: 최신 안정 `maps-compose`가 현재 프로젝트보다 높은 Kotlin 메이저 라인에 맞춰 배포되는데, 프로젝트 Kotlin 버전 갱신을 함께 하지 않았다.
- 해결: Version Catalog의 Kotlin 플러그인 버전을 `2.4.10`으로 올려 라이브러리 메타데이터와 맞췄다.
- 재발 방지: 새 AndroidX/Google Compose 계열 의존성을 추가할 때는 라이브러리 버전만 보지 말고 Kotlin metadata 또는 요구 Compose/Kotlin 라인도 함께 확인한다.
- 후속 조치:
  - [x] Kotlin 버전 상향 후 `testDebugUnitTest` 재실행
  - [x] Kotlin 버전 상향 후 `assembleDebug` 재실행
- 관련 경로: `ARNavigation/gradle/libs.versions.toml`

### RET-2026-08-12-001: 스킬 UI 설명 길이 검증 실패

- 상태: Resolved
- 단계: 프로젝트 스킬 초기화
- 맥락: `skill-creator`의 `init_skill.py`로 `implement-navigation-engine`을 생성했다.
- 증상/증거: 도구가 `short_description must be 25-64 characters (got 22)`를 출력하고 종료 코드 1로 중단됐다. 폴더와 `SKILL.md`는 이미 생성된 부분 성공 상태였다.
- 영향: 반복 초기화가 기존 폴더와 충돌할 수 있어 남은 스킬 생성이 일시 중단됐다.
- 근본 원인: UI 메타데이터의 문자 수 제약을 실행 전에 확인하지 않았다.
- 해결: 설명을 25자 이상으로 보정하고, 부분 생성된 스킬은 다시 초기화하지 않고 메타데이터 생성 도구로 완성했다.
- 재발 방지: 초기화 전에 모든 `short_description`이 25~64자인지 확인하고, 실패 후 재실행 전 생성된 경로를 먼저 검사한다.
- 후속 조치:
  - [x] 7개 스킬의 UI 설명 길이를 허용 범위로 보정
  - [x] 부분 생성된 `implement-navigation-engine`의 메타데이터 생성
- 관련 경로: `skills/*/agents/openai.yaml`

### RET-2026-08-12-002: Windows 기본 인코딩으로 스킬 메타데이터 생성 실패

- 상태: Resolved
- 단계: 프로젝트 스킬 메타데이터 생성
- 맥락: UTF-8 한국어가 포함된 `SKILL.md`를 `generate_openai_yaml.py`가 읽었다.
- 증상/증거: 기본 실행에서 `UnicodeDecodeError: 'cp949' codec can't decode byte 0xec`가 발생했다. 초기화 명령으로 전달한 한국어 UI 메타데이터도 일부 파일에서 깨진 상태로 보였다.
- 영향: `agents/openai.yaml`을 신뢰할 수 없었고 `implement-navigation-engine`에는 메타데이터 파일이 없었다.
- 근본 원인: Windows의 Python 기본 텍스트 인코딩이 cp949인 반면 스킬 문서는 UTF-8이었다.
- 해결: 생성 도구를 `python -X utf8 .../generate_openai_yaml.py`로 실행하고 UI 메타데이터를 ASCII 기반 영어로 재생성했다.
- 재발 방지: 이 저장소의 스킬 생성·검증 Python 도구는 항상 `python -X utf8`로 실행하고 생성 후 대체 문자와 YAML 내용을 확인한다.
- 후속 조치:
  - [x] 모든 `agents/openai.yaml` 재생성
  - [x] 모든 스킬에 `quick_validate.py`를 UTF-8 모드로 실행해 최종 확인
- 관련 경로: `skills/*/SKILL.md`, `skills/*/agents/openai.yaml`

### RET-2026-08-12-003: Todo 상태 조회 시 작업 ID 소실

- 상태: Resolved
- 단계: Todo 관리 구조 개선
- 맥락: 새 `tools/todo-status.ps1`로 단계 0과 `S00-T001`을 조회했다.
- 증상/증거: 단계 집계는 맞았지만 작업 목록의 `Id`와 `Title`이 비었고 `-TaskId S00-T001`이 `Task ID not found`로 실패했다.
- 영향: 에이전트가 안정적인 작업 ID로 단일 작업을 조회할 수 없었다.
- 근본 원인: PowerShell 자동 변수 `$matches`에 저장된 작업 정규식 결과가 단계 번호를 추출하는 두 번째 정규식에 의해 덮어써졌다.
- 해결: 작업 ID, 제목, 체크 상태를 지역 변수에 먼저 복사한 뒤 단계 번호를 계산하도록 변경했다.
- 재발 방지: 한 블록에서 복수 정규식을 사용할 때 `$matches`를 장기간 참조하지 않고 즉시 명시적 변수로 복사한다. 집계뿐 아니라 목록·단일 ID·DOD 조회를 모두 실행한다.
- 후속 조치:
  - [x] 단계별 집계 조회 검증
  - [x] 단계 미완료 목록 조회 검증
  - [x] 단일 작업 ID와 DOD ID 조회 검증
- 관련 경로: `tools/todo-status.ps1`

## 6. 새 회고 템플릿

```markdown
### RET-YYYY-MM-DD-NNN: 짧은 제목

- 상태: Open | Monitoring | Resolved
- 단계: TodoList 단계 또는 작업명
- 맥락: 무엇을 하던 중이었는가
- 증상/증거: 재현 명령, 핵심 오류, 관련 경로
- 영향: 기능·일정·품질에 미친 영향
- 근본 원인: 확인된 원인. 미확인이면 `추정` 표시
- 해결: 실제 적용한 해결 또는 현재 우회
- 재발 방지: 다음 작업자가 따라야 할 규칙이나 자동 검증
- 후속 조치:
  - [ ] 담당 가능한 구체 작업
- 관련 경로: `path/to/file`
```

상태는 해결과 재현 방지 검증까지 끝난 경우에만 `Resolved`로 변경한다.
