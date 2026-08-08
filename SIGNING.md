# 서명 및 업데이트 연속성

`com.example.bloodmoonnightfall`의 v1.0.0과 v2.0.0은 같은 인증서로 서명되어 기존 설치본 위에 업데이트할 수 있습니다.

- 인증서 SHA-256: `41B6CCB282C142826227EB6D0B6108E5A8F78FC2CFF08E845B9458B2F08FA99D`
- 로컬 전용 키 백업: `%USERPROFILE%\.android\bloodmoon-nightfall-update.keystore`
- 별칭: `androiddebugkey`

키 파일은 `.gitignore`의 `*.keystore` 규칙으로 GitHub 업로드에서 제외합니다. 이 키를 잃거나 다른 키로 서명하면 같은 패키지의 기존 앱 위에 업데이트할 수 없습니다. 공개 저장소, 메신저, APK 배포 폴더에는 키 파일을 복사하지 마세요.

R8이 적용된 릴리스의 크래시 분석에는 해당 버전의 `dist/mapping-v2.0.0.txt`를 사용합니다.
