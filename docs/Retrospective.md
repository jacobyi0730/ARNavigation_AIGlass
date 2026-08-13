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

### RET-2026-08-12-005: Kotlin 버전 변경 직후 daemon 증분 캐시 충돌

- 상태: Resolved
- 단계: 단계 0 `S00-T008`, `S00-T009`
- 맥락: Kotlin 버전을 `2.4.10`으로 올린 직후 기존 daemon 상태에서 다시 빌드를 검증했다.
- 증상/증거: 일반 `testDebugUnitTest`와 `assembleDebug` 실행에서 `Storage ... is already registered`, `Could not close incremental caches` 오류가 발생했다.
- 영향: 실제 코드 오류와 무관한 캐시 문제 때문에 AR 스파이크 검증 결과를 신뢰하기 어려웠다.
- 근본 원인: Kotlin/Gradle daemon이 버전 변경 전 증분 캐시 상태를 잡고 있어 새 컴파일러 상태와 충돌한 것으로 보인다.
- 해결: `clean`과 `--no-daemon` 기준으로 다시 빌드해 캐시를 초기화했고 `assembleDebug`를 성공시켰다.
- 재발 방지: Kotlin 또는 Compose compiler 계열 버전을 바꾼 직후 첫 검증은 `clean`과 `--no-daemon` 기준으로 한 번 실행한다.
- 후속 조치:
  - [x] `clean assembleDebug --no-daemon` 재검증
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

### RET-2026-08-12-006: CameraX 프리뷰는 열려도 ADB screencap 검증은 불안정할 수 있음

- 상태: Resolved
- 단계: 단계 0 `S00-T011`
- 맥락: 실기기에서 `AR -> Map -> AR` 전환 후 카메라 pause/resume 을 검증했다.
- 증상/증거:
  - `adb shell dumpsys media.camera` 에서는 `DISCONNECT device 0 client for package com.wjs.arnav` 뒤에 다시 `CONNECT device 0 client for package com.wjs.arnav` 와 `Device 0 is open` 이 확인됐다.
  - 같은 시점 `adb shell screencap -p` 결과 이미지에서는 AR 화면 미리보기가 검게 캡처됐다.
- 영향: 카메라 리소스 반환과 재연결은 확인됐지만, 화면에 실제 프리뷰 프레임이 정상 복귀하는지 ADB 캡처만으로는 단정하기 어렵다.
- 근본 원인: 추정. Samsung 기기 + CameraX `PreviewView` + ADB screencap 조합에서 프리뷰 레이어가 검게 캡처되거나, 복귀 시 프리뷰 렌더링이 늦게 붙을 가능성이 있다.
- 해결: ARCore `GLSurfaceView` 기반 스파이크로 전환한 뒤 `uiautomator dump` 상태 문자열과 세션 로그를 함께 사용해 `S00-T011` 을 실기기에서 완료 검증했다.
- 재발 방지:
  - CameraX/ARCore 전환 검증에서는 `dumpsys media.camera` 와 화면 캡처를 함께 보되, 필요하면 사람이 직접 기기 화면을 보는 확인 절차를 추가한다.
  - 전환 안정화 리팩터링은 별도 작은 변경으로 나누고, 첫 진입 프리뷰와 복귀 프리뷰를 각각 검증한다.
- 후속 조치:
  - [x] `S00-T011` 재검증 시 첫 진입 화면과 복귀 화면을 모두 실기기 상태 문자열과 로그로 확인
  - [x] 필요하면 `PreviewView` 수명주기 처리와 `lifecycle-runtime-compose` 기반 lifecycle owner 사용 검토
- 관련 경로: `ARNavigation/app/src/main/java/com/wjs/arnav/spike/ArCoreSpikeScreen.kt`

### RET-2026-08-13-009: 공식 `hello_ar_kotlin` 샘플은 현재 Windows/JDK 조합에서 바로 빌드되지 않음

- 상태: Open
- 단계: 단계 0 `S00-T010`, `S00-T011`
- 맥락: 실기기 ARCore 추적 문제를 대조군으로 분리하기 위해 Google `hello_ar_kotlin` 샘플 빌드를 시도했다.
- 증상/증거: `C:\Projects\ARCoreSdkSample\samples\hello_ar_kotlin` 에서 `.\gradlew.bat assembleDebug --no-daemon` 실행 시 `Unsupported class file major version 69` 로 실패했다.
- 영향: 동일 기기에서 공식 샘플을 즉시 올려 비교하는 경로가 막혀, 실기기 검증은 저장소 앱 자체의 로그와 상태 문자열에 의존해야 했다.
- 근본 원인: 추정. 샘플의 Gradle 8.6 빌드 스크립트가 현재 로컬 JDK class version 69 환경과 호환되지 않는다.
- 해결: 이번 단계에서는 저장소 앱의 실기기 상태 문자열, `uiautomator dump`, `adb logcat` 증거로 검증을 완료했다.
- 재발 방지:
  - 외부 샘플을 대조군으로 사용할 때는 빌드 전에 샘플 Gradle/JDK 호환성을 먼저 확인한다.
  - 필요하면 샘플용 별도 JDK 또는 Gradle wrapper 업그레이드 계획을 분리 작업으로 남긴다.
- 후속 조치:
  - [ ] 공식 ARCore 샘플 검증이 다시 필요해지면 샘플 저장소 전용 JDK/Gradle 호환 조합을 먼저 맞춘다.
- 관련 경로: `C:/Projects/ARCoreSdkSample/samples/hello_ar_kotlin`

### RET-2026-08-13-010: 같은 워크트리에서 Gradle 검증 작업을 병렬 실행하면 Kotlin 증분 캐시가 충돌할 수 있음

- 상태: Resolved
- 단계: 단계 1 `S01-T025`, `S01-T026`
- 맥락: 멀티 에이전트 요청에 맞춰 검증 속도를 높이기 위해 `assembleDebug`와 `testDebugUnitTest`를 같은 시점에 실행했다.
- 증상/증거: 병렬 실행 시 `Storage ... is already registered`, `Incremental compilation failed`, `NoSuchFileException`가 발생하며 `compileDebugKotlin`이 실패했다. 같은 명령을 순차로 다시 실행하면 `assembleDebug`와 `testDebugUnitTest`가 모두 성공했다.
- 영향: 코드가 정상이어도 병렬 검증만으로 빌드 실패처럼 보일 수 있어 단계 완료 판단을 왜곡한다.
- 근본 원인: 같은 모듈의 Kotlin/Gradle 증분 캐시를 두 Gradle 프로세스가 동시에 건드리며 충돌한 것으로 보인다.
- 해결: 이후 검증은 같은 워크트리 기준으로 반드시 순차 실행하고, 성공 여부는 순차 실행 결과만 근거로 사용했다.
- 재발 방지:
  - 같은 앱 모듈에 대한 `assembleDebug`, `testDebugUnitTest`, `lint`는 병렬로 실행하지 않는다.
  - 멀티 에이전트 턴에서도 빌드 검증은 하나의 실행 흐름으로 직렬화한다.
- 후속 조치:
  - [x] `assembleDebug --no-daemon` 순차 재실행 성공 확인
  - [x] `testDebugUnitTest --no-daemon` 순차 재실행 성공 확인
- 관련 경로: `ARNavigation/app/build`, `ARNavigation/.kotlin`

### RET-2026-08-12-007: Fused Location 사용 시 Play services 위치 의존성 누락으로 컴파일 실패

- 상태: Resolved
- 단계: 단계 8 `S08-T006`
- 맥락: 지도 화면에 현재 위치 재중심 버튼을 추가하면서 `LocationServices.getFusedLocationProviderClient()`를 연결했다.
- 증상/증거: `.\gradlew.bat testDebugUnitTest assembleDebug --no-daemon` 실행 시 `PrototypeMapScreen.kt`의 `LocationServices`와 `com.google.android.gms.location` import가 해석되지 않아 `compileDebugKotlin`이 실패했다.
- 영향: 지도 현재 위치 버튼 구현이 코드상으로는 완료되어도 실제 빌드가 막혀 작업 완료 체크를 진행할 수 없었다.
- 근본 원인: `libs.versions.toml`에는 지도 SDK만 연결되어 있었고, Fused Location API가 속한 `play-services-location` 의존성이 앱 모듈에 추가되지 않았다.
- 해결: Version Catalog에 `play-services-location:21.4.0`을 추가하고 `app/build.gradle.kts`에 `implementation(libs.google.play.services.location)`를 연결한 뒤 빌드를 다시 통과시켰다.
- 재발 방지:
  - Google Maps Compose와 Fused Location을 함께 쓸 때는 지도 렌더링 의존성과 위치 공급자 의존성을 분리해서 확인한다.
  - 새 SDK 타입 import를 추가한 직후에는 `assembleDebug --no-daemon`까지 바로 실행해 누락 의존성을 같은 작업 안에서 닫는다.
- 후속 조치:
  - [x] 위치 의존성 추가 후 `testDebugUnitTest` 통과 확인
  - [x] 위치 의존성 추가 후 `assembleDebug` 통과 확인
  - [x] 실기기에서 지도 화면과 `Current Location` 버튼 노출 확인
- 관련 경로: `ARNavigation/gradle/libs.versions.toml`, `ARNavigation/app/build.gradle.kts`, `ARNavigation/app/src/main/java/com/wjs/arnav/prototype/PrototypeMapScreen.kt`

### RET-2026-08-13-008: Windows에서 `clean assembleDebug`가 `app/build` 잠금으로 실패할 수 있음

- 상태: Resolved
- 단계: 단계 0 `S00-T008`
- 맥락: ARCore 세션 프로브를 추가한 뒤 `.\gradlew.bat clean testDebugUnitTest --no-daemon` 와 `.\gradlew.bat clean assembleDebug --no-daemon` 로 Stage 0 검증을 마무리했다.
- 증상/증거: 두 번째 명령의 `:app:clean` 단계에서 `Unable to delete directory '...\\app\\build'` 와 `Failed to delete some children` 오류가 발생했다. 같은 변경에서 이어서 `.\gradlew.bat assembleDebug --no-daemon` 는 성공했다.
- 영향: 코드나 의존성 문제와 무관하게 Windows 파일 잠금 때문에 APK 산출 검증이 일시적으로 실패한 것처럼 보일 수 있다.
- 근본 원인: 추정. 직전 빌드 또는 외부 프로세스가 `app/build` 내부 산출물을 잡고 있어 Gradle `clean` 이 디렉터리를 즉시 지우지 못했다.
- 해결: `clean testDebugUnitTest --no-daemon` 로 캐시 초기화와 컴파일 검증을 끝낸 뒤, APK 확인은 같은 작업 트리에서 `assembleDebug --no-daemon` 를 별도로 실행해 마무리했다.
- 재발 방지:
  - Windows에서 `clean` 실패가 파일 삭제 단계에만 국한되면 코드 오류로 단정하지 말고 잠금 여부를 먼저 의심한다.
  - 같은 턴에서 이미 `clean` 이 한 번 성공했다면 후속 APK 검증은 `assembleDebug --no-daemon` 단독 실행으로 충분한지 먼저 확인한다.
- 후속 조치:
  - [x] `assembleDebug --no-daemon` 재실행으로 APK 산출 확인
- 관련 경로: `ARNavigation/app/build`

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
