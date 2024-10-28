<h1 align="center">
  지식 관리 시스템
  <br />
   <a href="https://www.meetup.com/Angular-Medellin/">
    <img width=100% src="./readme/logo_coconote.png">
  </a> 
  COCONOTE
  <br />
  극대화된 협업 향상을 경험해보세요
</h1>

---

<h2> ️💚️ TEAM 💚 </h2>

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore -->
| [<img src="https://avatars.githubusercontent.com/u/55572356?s=96" width="100px;"/><br /><sub><b>김정은</b></sub>](https://github.com/ara-ro)<br />        | [<img src="https://avatars.githubusercontent.com/u/167521161?s=96" width="100px;"/><br /><sub><b>김민지 </b></sub>](https://github.com/p1p3)<br /> | [<img src="https://avatars.githubusercontent.com/u/117874745?s=96" width="100px;"/><br /><sub><b>김지호</b></sub>](https://github.com/danielcb29)<br /> | [<img src="https://avatars.githubusercontent.com/u/95060314" width="100px;"/><br /><sub><b>전상민</b></sub>](http://co.linkedin.com/in/alejandronanez/)<br /> |          [<img src="https://avatars.githubusercontent.com/u/96410921?s=96" width="100px;"/><br /><sub><b>최세호</b></sub>](https://github.com/MelinaMejia95)<br /> |                             
| :-----------------------------------------------------------------------------------------------------------------------------------------------------------------: | :-----------------------------------------------------------------------------------------------------------------------------------------------------------------------: | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------: | :-------------------------------------------------------------------------------------------------------------------------------------------------------------: | :------------------------------------------------------------------------------------------------------------------------------------------------------------: | 
<!-- ALL-CONTRIBUTORS-LIST:END -->
<br /><br />

## ❤️ 목차

1. [프로젝트 소개](#소개)
2. [기획](#기획)
3. [기술 스택](#기술-스택)
4. [개발](#개발)
- [백엔드 API 요청 및 응답 (Swagger)](#백엔드-api-요청-및-응답-swagger)
- [프론트엔드 기능 시연](#프론트엔드-기능-시연)
5. [구조 및 아키텍처](#구조-및-아키텍처)
6. [배포](#배포)
7. [테스트](#테스트)

<h2 id="프로젝트-소개"> 🧡 프로젝트 소개 </h2>
<h3> 개요 </h3>
<h3> 배경 및 목적 </h3>
<h3> 프로그램 사양서 </h3>

- SNS를 통한 간편 로그인
- workspace 를 통한 section, channel 생성
  - 공개, 비공개 채널을 통한 참가자 제한
  - 초대를 통한 참가자 추가

- channel 별로 구분되어있는 **쓰레드**```(채팅)```, **캔버스**```(노션)```, **드라이브**```(네이버 or 구글 드라이브) ```
- 쓰레드
  - 단체 채팅, 이미지를 통한 정보공유
- 캔버스
  - 단체 문서수정, 유저별로 실시간 동기화 되어있는 문서
- 드라이브
  - 채널 내부의 쓰레드, 캔버스 자동업로드
  - 드라이브 내 파일 업로드를 통한 쓰레드, 캔버스 파일 동기화 기능

---

<h2 id="기획"> 💛 기획 </h2>

<h3> WBS </h3>

[<b> WBS 보러가기 ➡️</b>](https://docs.google.com/spreadsheets/d/1-55RJo0awEfJTcGI6TlrkOE9e4nGFYGOgi4ZCOvZu58/edit?usp=sharing)

<h3> 요구사항 정의서 </h3>

[<b>요구사항 정의서 보러가기 ➡️</b>](https://docs.google.com/spreadsheets/d/e/2PACX-1vRFFDDVRpsGfdgAvZc17fByKMgOEAG-cOA_VBWJCR53_YVacTATFazxp7AeO5hKaCS26RYA9g2NXL-b/pubhtml)

<h3> ERD </h3>

[<b>ERD 보러가기 ➡️</b>](https://www.erdcloud.com/d/9kcavurSDGPf2B6mr)

<h3> 화면설계서 (FIGMA) </h3>

[<b>화면설계서 보러가기 ➡️</b>](https://www.figma.com/design/SBzlObD1tMe49ZCXRpzyTx/COCONOTE---%ED%99%94%EB%A9%B4%EC%84%A4%EA%B3%84?node-id=0-1&t=O8uikTbKJRUR5e9g-1)

<h3> API 명세서 </h3>

[<b>API 명세서 보러가기 ➡️</b>](https://ara-boka.notion.site/COCONOTE-API-11585d64912780a3ac04f2305aed6349?pvs=4)


<h2 id="기술-스택"> ️🛠️ 기술 스택 </h2>
<h3>Backend</h3>

![Spring](https://img.shields.io/badge/Spring-6DB33F?style=flat-square&logo=Spring&logoColor=white)
![SpringBoot](https://img.shields.io/badge/Spring Boot-6DB33F?style=flat-square&logo=Spring Boot&logoColor=white)

<h3>Frontend</h3>
<img src="https://img.shields.io/badge/vue.js-4FC08D?style=flat-square&logo=vue.js&logoColor=white">

<h3>DB</h3>
<img src="https://img.shields.io/badge/MariaDB-003545?style=flat-square&logo=MariaDB&logoColor=white">

<h3>CI/CD</h3>
<img src="https://img.shields.io/badge/Kafka-231F20?style=flat-square&logo=apachekafka&logoColor=white">

<h3>Etc</h3>
<img src="https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=Docker&logoColor=white">

<h3>Communication</h3>

![Discord](https://img.shields.io/badge/Discord-5865F2?style=flat-square&logo=Discord&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=GitHub&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=flat-square&logo=Notion&logoColor=white)



<h2 id="개발"> 💻 개발 </h2>
<h3 id="백엔드-api-요청-및-응답-swagger"> 백엔드 API 요청 및 응답 (Swagger) </h3>
<h3 id="프론트엔드-기능-시연"> 프론트엔드 기능 시연 </h3>
<h2 id="구조-및-아키텍처"> 구조 및 아키텍처 </h2>
<h2 id="배포"> 배포 </h2>