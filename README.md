# 파일 동기화 프로그램
로컬 PC 의 `~/sync` 디렉토리 아래의 모든 파일(서브 디렉토리 포함)들을 원격 PC 의 `~/backup` 디렉토리에 지속적(주기적)으로 동기화(백업) 하는 서버와 클라이언트 프로그램입니다.

원격 PC의 IP와 PORT를 변경하고 싶다면 [`Const.java`](Const.java) 파일의 `SERVER_NAME` 과 `SERVER_PORT`를 변경하면 됩니다.

즉, 서버에서 실행하기 전에 서버의 IP와 PORT를 수정한 후 실행해주세요.


## 실행방법
- jar 파일 생성 : `make`
  - 서버 실행 방법 : `make server`
  - 클라이언트 실행 방법 : `make client`

## Server 순서도
![](image/server-diagram.svg)
## Client 순서도
![](image/client-diagram.svg)
