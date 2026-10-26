# SkyRoute 정적 데모 (GitHub Pages)

SkyRoute Spring Boot 앱을 테스트 데이터로 실행해 역할별 화면과 API 응답을 저장한 **서버 없는 체험용 사이트**입니다.

## 배포 방법

1. GitHub에 **`skyroute`** 라는 이름의 public 저장소를 만듭니다.
2. 이 폴더의 내용 전체(`index.html`, `404.html`, `.nojekyll`, `assets/`, `_demo/`, `guest/`, `user/`, `staff/`, `admin/` …)를 저장소 루트에 올립니다.
3. 저장소 **Settings → Pages → Build and deployment** 에서 Source를 `Deploy from a branch`, Branch를 `main` / `/(root)` 로 저장합니다.
4. 1~2분 뒤 `https://<깃허브아이디>.github.io/skyroute/` 에서 열립니다.

> 모든 링크가 `/skyroute/...` 경로를 기준으로 만들어져 있어요.
> 저장소 이름을 다르게 하고 싶다면 올리기 전에 `python3 rebase.py 새이름` 을 한 번 실행하세요.
> (예: `python3 rebase.py skyroute-demo` → `/skyroute-demo/...`)

## 데모 계정

| 역할 | 아이디 / 비밀번호 | 시작 화면 |
| --- | --- | --- |
| 회원 | user01 / 1234 | `/skyroute/user/main/home` |
| 지상직 | staff01 / 1234 | `/skyroute/staff/staff/schedule/today` |
| 관리자 | admin01 / 1234 | `/skyroute/admin/admin/base1` |

## 동작 방식

- 화면: 실제 서버가 렌더링한 HTML 그대로입니다.
- 링크 · 검색 · 예매 단계 이동: `_demo/demo.js` 가 저장된 화면으로 연결합니다. 저장되지 않은 검색 조건은 가장 가까운 결과로 보여줍니다.
- 화면 안의 조회 요청(fetch / AJAX): 저장된 응답을 돌려줍니다.
- 등록 · 수정 · 삭제 · 결제: 실제로 반영되지 않고 안내 메시지만 표시합니다. 결제는 데모 결제창으로 대체됩니다.
- 데이터 기준일: 2026-10-25
