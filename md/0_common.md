# 공통

## 사용자 정보 회수하기

Spring Security에서 정상적으로 인증이 이뤄졌다면, 이후 `SecurityContextHolder`를 이용해 인증 정보를 확인할 수 있다.
이를 좀더 간소화해 사용할 수 있게 해주는 `@Component`를 만들었다.

```java
@Component
public class AuthenticationFacade {
    public Authentication getAuth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public UserEntity extractUser() {
        MarketUserDetails userDetails
                = (MarketUserDetails) getAuth().getPrincipal();
        return userDetails.getEntity();
    }
}
```

## 파일 업로드

파일(이미지)을 업로드하는 기능이 여럿 있기 때문에, 같은 방식으로 동작할 수 있게끔 `FileHandlerUtils`를 만들었다.

```java
@Slf4j
@Component
public class FileHandlerUtils {
    public String saveFile(
            String path,
            String filenameBase,
            MultipartFile file
    ) {
        String pathDir = String.format("media/%s", path);
        try {
            Files.createDirectories(Path.of(pathDir));
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String originalFilename = file.getOriginalFilename();
        String[] fileNameSplit = originalFilename.split("\\.");
        String extension = fileNameSplit[fileNameSplit.length - 1];
        String filename = filenameBase + "." + extension;
        String filePath = pathDir + filename;

        try {
            file.transferTo(Path.of(filePath));
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return String.format("/static/%s", path + filename);
    }
}

```

파일을 저장할 경로와 저장되는 이름을 인자로 전달하면, 설정된 위치에 파일이 저장되고 어떻게 파일을 확인할 수 있는지
나타내는 링크를 반환하는 기능을 가지고 있다.