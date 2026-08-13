# ARCore 스파이크 메모

## 범위

- 대상 작업: `S00-T008`, `S00-T009`, `S00-T010`, `S00-T011`, `S00-T013`
- 기준 날짜: 2026-08-13

## 1. 결론

- Compose 내부에 ARCore 기반 `GLSurfaceView`를 포함하는 스파이크를 구성했다.
- 지원 대상 실기기에서 ARCore 설치 가능 여부와 `Session` 생성이 확인됐다.
- 같은 실기기에서 추적 활성화, 수평 상향 평면 탐지, 평면 위 화살표 앵커 배치를 확인했다.
- 같은 실기기에서 `AR -> Map -> AR` 전환 후 세션 `pause/resume`이 정상 동작함을 확인했다.

## 2. 현재 구현

- 의존성
  - `com.google.ar:core:1.33.0`
  - `de.javagl:obj:0.4.0`
- 화면
  - `ArCoreSpikeScreen`
  - `ArCoreSessionLifecycleHelper`
  - `ArCorePlaneSpikeRenderer`
- 렌더링
  - ARCore 샘플의 `BackgroundRenderer`, `PlaneRenderer`, `SampleRender` 계층 일부를 앱 내부로 복사해 사용
  - 카메라 배경, 평면 그리드, 단순 화살표 메시 렌더링 구현
- 상태 확인
  - `ArCoreApk.checkAvailability()` 결과 표시
  - `Session` 생성 가능 여부 표시
  - tracking/plane/arrow 상태 문자열 표시
  - `ArCore-SessionLifecycle`, `ArCore-ArStatus` 로그 추가

## 3. 실기기 검증

- 대상 기기
  - Samsung `SM-G991N`
- 확인 결과
  - 2026-08-13: `ARCore availability: SUPPORTED_INSTALLED`
  - 2026-08-13: `ARCore session probe: Session created successfully`
  - 2026-08-13: `Tracking: Tracking the scene`
  - 2026-08-13: `Plane: Horizontal upward plane detected`
  - 2026-08-13: `Arrow: Arrow anchored on the detected plane`
  - 2026-08-13: `AR -> Map -> AR` 전환 후 AR 화면 복귀 확인
  - 2026-08-13: `adb logcat` 에서 session/renderer `paused -> resumed` 순서 확인
- 사용 명령
  - `.\gradlew.bat testDebugUnitTest --no-daemon`
  - `.\gradlew.bat assembleDebug --no-daemon`
  - `adb install -r app-debug.apk`
  - `adb shell pm grant com.wjs.arnav android.permission.CAMERA`
  - `adb shell am start -n com.wjs.arnav/.MainActivity`
  - `adb shell uiautomator dump /sdcard/*.xml`
  - `adb logcat -d -s ArCore-SessionLifecycle ArCore-ArStatus`

## 4. 메모

- 실기기 초기 진입 직후에는 tracking 이 잠시 `paused` 상태일 수 있었고, 장면 추적이 붙은 뒤 평면과 화살표 상태가 갱신됐다.
- 다음 단계에서는 현재 스파이크 코드를 단계 1 패키지 구조와 실제 feature 경계에 맞게 정리할 필요가 있다.
