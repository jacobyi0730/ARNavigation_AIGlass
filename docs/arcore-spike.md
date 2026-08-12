# ARCore 스파이크 메모

## 범위

- 대상 작업: `S00-T008`, `S00-T009`, `S00-T011`, `S00-T013`
- 기준 날짜: 2026-08-12

## 1. 결론

- 현재 프로토타입의 AR 화면은 `AndroidView` 안에 CameraX `PreviewView`를 올려 후면 카메라 프리뷰를 보여준다.
- Compose 오버레이 카드와 지도 전환 버튼을 카메라 프리뷰 위에 함께 표시할 수 있다.
- 지원 대상 실기기에서 AR 프로토타입 화면과 지도 프로토타입 화면이 모두 실행되는 것을 확인했다.
- 아직 ARCore `Session` 생성, 평면 탐지, AR 오브젝트 렌더링은 붙이지 않았다.

## 2. 현재 구현

- 의존성
  - `com.google.ar:core:1.33.0`
  - CameraX `1.6.1`
- Manifest
  - `CAMERA` 권한 선언
  - `com.google.ar.core = optional` 메타데이터 선언
- Compose 화면
  - `ArCoreSpikeScreen`
  - `AndroidView` 내부 `PreviewView` 생성
  - CameraX 후면 카메라 프리뷰 바인딩
  - `ArCoreApk.checkAvailability()` 결과 표시
  - `Open Map` 버튼으로 지도 프로토타입 화면 이동

## 3. 실기기 검증

- 대상 기기
  - Samsung `SM-G991N`
- 실행 결과
  - 2026-08-12: 앱 실행 후 AR 프로토타입 화면에서 후면 카메라 프리뷰 표시 확인
  - 2026-08-12: 같은 기기에서 `Open Map` 버튼으로 지도 프로토타입 화면 전환 확인
  - 2026-08-12: 지도 화면에서 Google 지도 렌더링과 하단 조작 UI 표시 확인
- 사용 명령
  - `.\gradlew.bat assembleDebug --no-daemon`
  - `adb install -r app-debug.apk`
  - `adb shell am start -n com.wjs.arnav/.MainActivity`

## 4. 아직 남은 검증

- `S00-T008`
  - ARCore `requestInstall()` 및 `Session` 생성 확인
- `S00-T010`
  - 평면 탐지와 평면 위 안내 모델 렌더링 가능 여부 검증
- `S00-T011`
  - AR 화면과 지도 화면 전환 시 카메라/AR 세션 pause-resume 확인

## 5. 다음 작업 메모

- 현재 프로토타입은 "AR처럼 보이는 카메라 기반 셸" 단계다.
- 다음 단계에서는 CameraX 프리뷰 뒤에 ARCore 세션 수명주기와 렌더러를 연결해야 한다.
