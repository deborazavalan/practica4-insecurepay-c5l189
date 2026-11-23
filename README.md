# Práctica Preparatoria 4 — InsecurePay

IF0009 Desarrollo de Software IV · UCR Sede del Atlántico · Débora Zavala (C5L189)

Testing móvil, hashing con BCrypt (OWASP A02:2021), hardening de errores (OWASP A05:2021) y empaquetado AAB.

| Parte | Ubicación |
| :--- | :--- |
| Parte 1 — Teoría aplicada | [`RESPUESTAS.md`](RESPUESTAS.md) |
| 2.1.1 Bean `PasswordEncoder` (BCrypt, costo 12) | `backend/src/main/java/cr/ac/ucr/if0009/insecurepay/config/SecurityConfig.java`, `backend/pom.xml` |
| 2.1.2 `AuthService` con `passwordEncoder.matches()` | `backend/src/main/java/cr/ac/ucr/if0009/insecurepay/service/AuthService.java` |
| 2.1.3 Semillas BCrypt | `backend/src/main/resources/data.sql` |
| 2.2 Hardening y 401 RFC 7807 | `backend/src/main/resources/application.properties`, `exception/GlobalExceptionHandler.java` |
| 2.2 Retroalimentación en Angular | `frontend/src/app/components/login/login.component.ts` y `.html` |
| 2.3 Verificación | `scripts/pruebas.sh`, `.github/workflows/verificacion.yml` |

Los hashes que trae el enunciado están truncados (`...`) y no son válidos, así que en `data.sql` se generaron hashes BCrypt reales con costo 12:

| Usuario | Contraseña |
| :--- | :--- |
| `admin` | `admin123` |
| `auditor_seguridad` | `auditor2026` |
| `carlos_mora` | `carlos2026` |
| `maria_rojas` | `maria2026` |
| `juan_perez` | `juan123` |

Para tener `BCryptPasswordEncoder` y `BadCredentialsException` se agregó solo `spring-security-core`, sin `spring-boot-starter-security`, para que no se active la cadena de filtros por defecto que bloquearía todos los endpoints.

Las respuestas de usuario inexistente y de clave incorrecta son idénticas (`401`, "Usuario o clave incorrectos.") para evitar la enumeración de cuentas.

## Ejecución

```bash
cd backend && ./mvnw spring-boot:run
cd frontend && npm install && npm start
bash scripts/pruebas.sh
```

El workflow `.github/workflows/verificacion.yml` compila backend y frontend, levanta la API y ejecuta `scripts/pruebas.sh` en cada push.
