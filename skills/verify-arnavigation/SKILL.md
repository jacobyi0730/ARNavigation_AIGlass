---
name: verify-arnavigation
description: ARNavigation 변경의 빌드, 단위·Compose·계측 테스트, 기획서 인수 조건, 위치·방향·ARCore 실기기 시나리오와 보안 항목을 검증한다. 기능 완료 판정, 회귀 검사, 릴리스 준비 또는 에이전트 결과 검수 때 사용한다.
---

# ARNavigation 검증

## 범위 결정

1. 배정된 단계 파일의 작업 ID, 변경 diff와 관련 인수 조건만 읽는다.
2. 코드에서 주장하는 동작과 실제 테스트 범위를 매핑한다.
3. JVM, 계측, 실기기 중 필요한 검증 수준을 변경 위험에 따라 선택한다.

## 자동 검증

- 프로젝트 기준 명령은 `ARNavigation` 폴더에서 실행한다.
- 순수 로직은 `gradlew.bat testDebugUnitTest`로 검증한다.
- 빌드 통합은 `gradlew.bat assembleDebug`로 검증한다.
- 정적 검사는 `gradlew.bat lint`를 사용한다.
- 연결 기기가 있을 때 `gradlew.bat connectedDebugAndroidTest`를 실행한다.
- 실패하면 첫 원인과 재현 명령을 보존하고 무관한 오류를 숨기지 않는다.

## 기능 검증

- 지도: 목적지 교체, 경유지 추가·삭제·재정렬, 취소, 진행 중 조회
- 엔진: 안내 순서, 각도 경계, 10 m/3초 도착, 품질 저하·회복
- 상태: AR↔지도 전환, 회전, background/foreground, 사용자 종료
- AR: 평면 미인식, 추적 손실, 360° 회전, 실제 야외 방향, 30 FPS
- 보안: Maps API 키와 정밀 좌표가 소스·diff·release 로그에 없음
- 접근성: 48 dp 터치 영역, TalkBack 설명, 색상 외 구분, 확대 글꼴

## 판정

- 실행한 명령, 결과, 실행하지 못한 항목과 이유를 구분해 보고한다.
- 실기기 항목을 에뮬레이터나 mock 결과로 대체해 통과 처리하지 않는다.
- 코드·자동 테스트·필요한 수동 검증이 끝난 작업 ID만 단계 파일에서 완료 처리한다.
- 실패가 재사용 가능한 교훈이나 새 제약을 드러내면 `$record-retrospective`를 사용한다.
