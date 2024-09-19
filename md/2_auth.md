# 사용자 인증

## JWT

Java JWT(jjwt)와 Spring Security를 이용한 인증 방식을 채택하였다.
이를 위해 사용자 정보를 바탕으로 JWT를 발급, 검증하는 역할을 진행하는 `JwtTokenUtils`와

```java
@Slf4j
@Component
public class JwtTokenUtils {
    private final Key signingKey;
    private final JwtParser jwtParser;
    
    // ...

    // JWT를 발급하는 메서드
    public String generateToken(UserDetails userDetails) {
        Instant now = Instant.now();
        Claims jwtClaims = Jwts.claims()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(60 * 60 * 24 * 30)));

        return Jwts.builder()
                .setClaims(jwtClaims)
                .signWith(this.signingKey)
                .compact();
    }

    // 정상적인 JWT인지를 판단하는 메서드
    public boolean validate(String token) {
        try {
            // 정상적이지 않은 JWT라면 예외(Exception)가 발생한다.
            jwtParser.parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.warn("invalid jwt");
        }
        return false;
    }

    // 실제 데이터(Payload)를 반환하는 메서드
    public Claims parseClaims(String token) {
        return jwtParser
                .parseClaimsJws(token)
                .getBody();
    }

}
```

Spring Security 내부에서 인증을 위해 사용할 `JwtTokenFilter`를 작성하였다.
```java
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {
    private final JwtTokenUtils jwtTokenUtils;
    private final UserDetailsService service;

    public JwtTokenFilter(
            JwtTokenUtils jwtTokenUtils,
            UserDetailsService service
    ) {
        this.jwtTokenUtils = jwtTokenUtils;
        this.service = service;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // Authorization 확인
        String authHeader
                = request.getHeader(HttpHeaders.AUTHORIZATION);
        // JWT가 존재한다면
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.split(" ")[1];
            // 내부에서 JWT의 검증을 진행한다.
        }
        filterChain.doFilter(request, response);
    }
}

```

## 사용자 정보 관리

사용자 정보를 데이터베이스에 저장하고, JPA를 이용해 관리할 것이다.
이를 위해 `UserEntity`를 만들었다.

```java
@Entity
@Table(name = "user_table")
public class UserEntity extends BaseEntity {
    @Column(unique = true)
    private String username;
    private String password;
    private Integer age;
    @Column(unique = true)
    private String email;
    @Column(unique = true)
    private String phone;
    private String profileImg;
    private String roles = "ROLE_INACTIVE";
}
```

그리고 Spring Security 내부에서는 사용자 정보를 `UserDetailsService`를 이용해 다룬다.
`UserDetailsService`는 `username`을 사용자 정보를 조회하기 위한 기준으로 활용하는 `interface`이다.
여기서는 JPA를 이용하기 때문에, `UserRepo`를 만들고 `findByUsername()` 메서드를 이용해 구현했다.
또한 회원가입 과정에서 `username`이 중복인지 체크하기 위한 메서드도 포함된다.

```java
public interface UserRepo extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    boolean existsByUsername(String username);
}
```

그리고 `UserDetailsService`에서 실제로 주고받는 클래스는 `UserDetails`의 구현체이므로,
`UserEntity`의 정보를 바탕으로 새 `UserDetails` 구현체를 제작하였다.
```java
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketUserDetails implements UserDetails {
    private Long id;
    private String username;
    private String password;
    private Integer age;
    private String email;
    private String phone;
    private String profileImg;
    private String rolesRaw;
    @Getter
    private UserEntity entity;
    // ...
}
```

그 외에 회원가입을 위한 `createUser`와 로그인(JWT 발급)을 위한 `signin`메서드를 추가하였다.

```java
public class UserService implements UserDetailsService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtils jwtTokenUtils;
    
    // ...

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findByUsername(username)
                .map(MarketUserDetails::fromEntity)
                .orElseThrow(() -> new UsernameNotFoundException("not found"));
    }

    @Transactional
    public UserDto createUser(CreateUserDto dto) {
        if (!dto.getPassword().equals(dto.getPasswordCheck()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if (userRepo.existsByUsername(dto.getUsername()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        return UserDto.fromEntity(userRepo.save(UserEntity.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .build()));
    }

    public JwtResponseDto signin(JwtRequestDto dto) {
        UserEntity userEntity = userRepo.findByUsername(dto.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(
                dto.getPassword(),
                userEntity.getPassword()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        String jwt = jwtTokenUtils.generateToken(MarketUserDetails.fromEntity(userEntity));
        JwtResponseDto response = new JwtResponseDto();
        response.setToken(jwt);
        return response;
    }

}

```

## 권한 관리

기본적으로 4종류의 사용자가 있다.
이를 Spring Security의 Role로 지정하기 위해 4종류의 Role의 개념을 만들었다.
- `ROLE_INACTIVE`: 비활성 사용자
- `ROLE_ACTIVE`: 활성 사용자 (일반, 사업자, 관리자)
- `ROLE_OWNER`: 사업자 사용자
- `ROLE_ADMIN`: 관리자

기본적으로 회원가입을 진행하면 비활성 사용자가 되며, 이후 정보를 전부 입력하면 활성 사용자 권한이 부여된다.
각각의 권한은 계층 구조를 가지지 않는다. 즉 사업자 사용자라면 `ROLE_OWNER`와 `ROLE_ACTIVE`를 둘다 가지고 있는 사용자가 된다.

Role의 갯수가 많지 않고, 일반적인 서비스 흐름에서 최대 두개의 권한을 가지게 됨으로 문자열의 형태로 저장하여 사용한다.
`UserDetails`로 전환할때는 `,`를 이용해 구분된 Role을 `split`하여 정리한다.

```java
public class MarketUserDetails implements UserDetails {
    // ...
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.stream(entity.getRoles().split(","))
                .map(role -> (GrantedAuthority) () -> role)
                .toList();
    }
}
```

이렇게 진행하면 기본적으로 일반 사용자 권한이 있어야 전반적 서비스의 접근을 허용하고,
그 외 인증이 필요 없거나 더 높은 권한이 필요한 내용을 따로 정의해줄 수 있다.

```java
@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {
    // ...

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(
                            "/users/signin",
                            "/users/signup"
                    )
                    .anonymous();
                    auth.requestMatchers(
                            "/users/details",
                            "/users/profile",
                            "/users/get-user-info"
                    )
                    .authenticated();
                    auth.requestMatchers(
                            "/admin/**"
                    )
                    .hasRole("ADMIN");
                    auth.requestMatchers(
                            "/shops/my-shop/**"
                    )
                    .hasRole("OWNER");
                    auth.requestMatchers("/error", "/static/**", "/views/**", "/")
                            .permitAll();
                    auth.anyRequest()
                            .hasRole("ACTIVE");
                })
        //...
        ;

        return http.build();
    }
}

```

기본 요구사항에서는 비활성 사용자가 나이, 이메일, 전화번호 정보를 제공하면 일반 사용자로 자동으로 전환된다.
이를 위해 회원정보를 갱신하는 메서드에 해당 로직을 추가했다.

단, 이 정보는 현재는 다시 갱신이 가능하기 때문에 상황에 따라 갱신하지 못하게 하거나 다시 비활성 사용자로
전환하는 등의 기능을 고려해야 한다.

```java
public class UserService implements UserDetailsService {
    // ...
    public UserDto updateUser(UpdateUserDto dto) {
        UserEntity userEntity = authFacade.extractUser();
        userEntity.setAge(dto.getAge());
        userEntity.setPhone(dto.getPhone());
        userEntity.setEmail(dto.getEmail());
        if (
                userEntity.getAge() != null &&
                userEntity.getEmail() != null &&
                userEntity.getPhone() != null &&
                userEntity.getRoles().equals("ROLE_INACTIVE")
        )
            userEntity.setRoles("ROLE_ACTIVE");
        return UserDto.fromEntity(userRepo.save(userEntity));
    }
    // ...
}
```

사업자 사용자로 업그레이드를 하기 위해선 사업자 등록번호(가정)을 전달해야 한다. 신청 내역을 저장하기 위한 `Entity`를 만들고,

```java
public class UserUpgrade extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity target;

    private Boolean approved;
}
```

이를 만드는 메서드를 만들었다.

```java
public class UserService implements UserDetailsService {
    // ...
    public void upgradeRoleRequest(RequestUpgradeDto dto) {
        UserEntity target = authFacade.extractUser();
        userUpgradeRepo.save(UserUpgrade.builder()
                .target(target)
                .build()
        );
    }
}
```

이는 나중에 관리자 기능에서 수락하면 권한이 갱신되도록 조정한다.
