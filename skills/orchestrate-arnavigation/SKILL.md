---
name: orchestrate-arnavigation
description: ARNavigation의 TodoList 작업을 선후관계에 따라 선택하고 전문 에이전트와 프로젝트 스킬에 배정하며 결과를 통합·검증한다. 사용자가 TodoList 실행, 다음 단계 구현, 여러 기능의 병렬 작업, 전문 에이전트 위임, 구현 결과 통합을 요청할 때 사용한다.
---

# ARNavigation 작업 오케스트레이션

## 준비

1. 저장소 루트의 `AGENTS.md`를 읽는다.
2. 경량 인덱스 `docs/TodoList.md`에서 현재 단계와 선행 조건만 확인한다.
3. `.\tools\todo-status.ps1 -Stage <번호> -PendingOnly`로 후보를 조회한다.
4. 선택한 `docs/tasks/stage-XX.md` 하나만 읽고 작업 ID를 확정한다.
5. 필요한 기획서 절과 `rg`로 찾은 관련 회고 항목만 읽는다.
6. `git status --short`와 관련 소스·테스트를 확인한다. 기존 사용자 변경을 보존한다.

## 작업 분해와 배정

- 빌드, Manifest, Compose 셸, 권한, 생명주기는 `$implement-android-foundation`에 배정한다.
- 세션, 위치, 방향 센서, 거리·방위·도착 계산은 `$implement-navigation-engine`에 배정한다.
- Google Maps Compose와 목적지·경유지 편집은 `$implement-map-workflow`에 배정한다.
- ARCore 세션, 평면 탐지, 3D 인디케이터는 `$implement-ar-guidance`에 배정한다.
- 빌드, 자동화 테스트, 인수 조건 및 실기기 검증은 `$verify-arnavigation`에 배정한다.
- 실패, 잘못된 가정, 새 제약, 재사용할 학습은 `$record-retrospective`로 기록한다.

에이전트 작업은 하나의 관찰 가능한 결과로 제한한다. 요청에 다음을 포함한다.

- 단계 파일의 작업 ID와 완료 조건
- 수정 소유 경로와 수정 금지 경로
- 이미 확정된 인터페이스·정책
- 실행할 검증 명령
- 결과 보고 형식: 결과, 변경 파일, 검증, 남은 위험, 회고 필요 여부

병렬 실행은 사용자 또는 상위 지침이 허용하고 수정 경로가 겹치지 않을 때만 한다. Version Catalog, Gradle, Manifest, 공용 모델, 앱 내비게이션 그래프, 작업 인덱스와 단계 파일은 공통 충돌 지점이므로 계약을 먼저 확정하고 한 작업자만 수정한다.

## 통합

1. 에이전트 결과를 그대로 신뢰하지 말고 diff와 실제 파일을 확인한다.
2. 공용 계약과 화면 간 상태 흐름의 불일치를 해소한다.
3. `$verify-arnavigation`으로 변경 위험에 맞는 검증을 실행한다.
4. 코드와 검증이 완료된 작업 ID만 해당 `docs/tasks/stage-XX.md`에서 `[x]`로 바꾼다.
5. 단계 상태·활성 작업·다음 후보가 바뀔 때만 `docs/TodoList.md`를 갱신한다.
6. 실패나 향후 작업자가 알아야 할 지식이 생겼으면 같은 작업 안에서 `docs/Retrospective.md`를 갱신한다.

## 중단 조건

- API 키, 실기기, 제품 정책처럼 결과를 바꾸는 입력이 없으면 추측으로 완료 처리하지 않는다.
- 실기기 전용 항목은 빌드 또는 에뮬레이터 결과만으로 완료 처리하지 않는다.
- 같은 실패가 반복되면 추가 변경 전에 회고의 기존 해결책과 미완료 후속 조치를 확인한다.
- 진행률 확인을 위해 모든 단계 파일을 읽지 말고 `tools/todo-status.ps1`의 집계를 사용한다.
