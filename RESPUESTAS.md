# Práctica Preparatoria 4 — Parte 1: Teoría Aplicada

## 1.1 Widget Testing en Flutter

1. **`pump()` vs `pumpAndSettle()`:** `pump([duration])` dispara **un solo** frame: avanza el reloj simulado la duración indicada (o cero), ejecuta los timers y microtareas vencidos y reconstruye los widgets marcados como sucios. `pumpAndSettle()` llama a `pump` repetidamente (cada 100 ms simulados por defecto) hasta que ya no queden frames programados, es decir, hasta que terminen animaciones, transiciones y reconstrucciones pendientes; devuelve cuántos frames bombeó. En la prueba, tras `pumpWidget` el `FutureBuilder` está esperando y se ve el `CircularProgressIndicator`; `pumpAndSettle` deja que el `Future` del repositorio se complete y que la UI llegue al estado final con el saldo.
2. **Animación infinita:** `pumpAndSettle` nunca encuentra un frame "quieto" porque cada frame programa el siguiente (`repeat()`, un `CircularProgressIndicator` indeterminado que sigue visible, un `Timer.periodic` que llama a `setState`). Al alcanzar el tiempo límite (10 minutos simulados por defecto) la prueba falla con `FlutterError: pumpAndSettle timed out`. En esos casos se usa `pump(const Duration(...))` con una duración concreta, o se detiene/mockea la animación.
3. **Mocks:** la prueba de widget valida la interfaz, no el backend. Con llamadas reales la prueba depende de la red, del servidor encendido y de los datos que tenga (el saldo cambia), por lo que deja de ser determinista y aparecen falsos fallos. Además `testWidgets` usa un `HttpClient` falso que responde 400 a toda petición, y el tiempo es simulado, así que las peticiones reales no funcionan bien ahí. Con un repositorio simulado (por ejemplo con `mocktail` o `mockito`) la prueba es rápida, repetible, no toca datos reales ni credenciales, y permite forzar los casos difíciles: error de red, saldo cero, respuesta lenta.

## 1.2 Pruebas unitarias con `flutter_test`

1. **Excepción asíncrona:**

```dart
test('transferir lanza SaldoInsuficienteException', () async {
  await expectLater(
    () => bancoService.transferir(cuentaOrigen, 999999999.0),
    throwsA(isA<SaldoInsuficienteException>()),
  );
});
```

Con una función que devuelve `Future` se usa `expectLater` (o `await expect(...)`) para que la prueba espere a que el `Future` falle; si no se espera, la prueba podría terminar antes de evaluar la aserción. Equivale a `expect(bancoService.transferir(cuentaOrigen, monto), throwsA(isA<SaldoInsuficienteException>()))` pasando el `Future` directamente.

2. **Matchers:**
   - `isA<T>()`: verifica que el valor sea de tipo `T` (o subtipo). Se puede encadenar con `.having(...)` para revisar propiedades: `isA<SaldoInsuficienteException>().having((e) => e.monto, 'monto', 5000)`.
   - `equals(x)`: igualdad por valor con `==`; en listas y mapas compara elemento por elemento. Es el matcher implícito cuando se pasa un valor directamente: `expect(saldo, 895000)`.
   - `hasLength(n)`: verifica la propiedad `length` de una lista, mapa o cadena: `expect(movimientos, hasLength(3))`.
   - `throwsA(matcher)`: recibe una función o un `Future`, la ejecuta y pasa solo si lanza una excepción que cumpla el matcher interno. Si no lanza nada, o lanza otro tipo, la prueba falla.

## 1.3 Criptografía de contraseñas (OWASP A02:2021)

1. **Funciones rápidas:** MD5 y SHA-256 están diseñadas para calcularse lo más rápido posible (verificar integridad de archivos grandes). Esa velocidad favorece al atacante: una GPU actual calcula decenas de miles de millones de SHA-256 por segundo, así que probar diccionarios completos y todas las combinaciones cortas toma minutos. Además son deterministas y sin salt: `SHA-256("carlos2026")` es el mismo en cualquier sistema, por lo que basta buscar el hash en una tabla arcoíris o tabla precalculada, y usuarios con la misma contraseña quedan con el mismo hash (si se rompe uno, se rompen todos).
2. **Salt:** valor aleatorio único (en BCrypt 128 bits) que se genera para cada usuario y se combina con la contraseña antes de calcular el hash; se guarda junto al hash (en BCrypt dentro de la misma cadena `$2a$12$<salt 22 caracteres><hash 31 caracteres>`). Dos usuarios con `carlos2026` tienen salts distintos y por lo tanto hashes distintos. Las tablas arcoíris dejan de servir porque habría que precalcular una tabla por cada salt posible, y el atacante tiene que atacar cada hash por separado.
3. **Work factor:** BCrypt y Argon2id son lentos **a propósito** y su costo es configurable. En BCrypt el factor es logarítmico: costo 12 = 2^12 = 4096 rondas de expansión de clave; cada +1 duplica el tiempo. Un login tarda unos cientos de milisegundos (imperceptible para el usuario) pero el atacante pasa de miles de millones a unos pocos miles de intentos por segundo por GPU. Como el costo queda guardado en cada hash (`$2a$12$`), cuando el hardware mejora se sube a 13 o 14 sin cambiar de algoritmo: los hashes viejos se siguen verificando y se vuelven a generar con el costo nuevo cuando el usuario inicia sesión. Argon2id además exige mucha memoria, lo que limita el paralelismo en GPU y ASIC.

## 1.4 Fuga de información en errores (OWASP A05:2021)

1. **Clasificación:** OWASP **A05:2021 – Security Misconfiguration**, **CWE-209** (Generation of Error Message Containing Sensitive Information). Relacionadas: CWE-497 (exposición de información del sistema) y CWE-200.
2. **Uso por el atacante:** la traza es reconocimiento gratuito. Revela el stack (Spring Boot, Hibernate, H2 y sus versiones) para buscar CVE conocidos y exploits listos; los nombres de paquetes y clases (`cr.ac.ucr...TransaccionService`) muestran la arquitectura; los mensajes de Hibernate/SQL revelan nombres de tablas y columnas y el motor de base de datos, lo que permite escribir inyecciones SQL precisas (sintaxis H2, `UNION` con el número de columnas correcto). El atacante envía entradas malformadas a propósito y usa las diferencias en los errores como oráculo para mapear la aplicación.
3. **Configuración:**

```properties
server.error.include-stacktrace=never
server.error.include-message=never
server.error.include-binding-errors=never
server.error.include-exception=false
```

Además: no activar DevTools en producción (activa trazas), `spring.h2.console.enabled=false`, `spring.jpa.show-sql=false`, y un `@RestControllerAdvice` que devuelva `ProblemDetail` (RFC 7807) con mensajes genéricos y registre el detalle solo en el log del servidor.

## 1.5 Android App Bundle y firma

1. **keytool:**

```bash
keytool -genkeypair -v \
  -keystore upload-keystore.jks \
  -storetype JKS \
  -keyalg RSA -keysize 2048 \
  -validity 10000 \
  -alias upload
```

(`-genkey` es el alias antiguo de `-genkeypair`.) El comando pide la contraseña del almacén y los datos del certificado (CN, OU, O, L, ST, C). El `.jks` y sus contraseñas no se suben al repositorio; se referencian desde `android/key.properties` (en `.gitignore`) en `build.gradle`, y se compila con `flutter build appbundle --release`.

2. **AAB vs APK:** un APK universal empaqueta todo para todos los dispositivos: código nativo de todas las ABI (`armeabi-v7a`, `arm64-v8a`, `x86_64`), recursos de todas las densidades (`mdpi` … `xxxhdpi`) y todos los idiomas; cada usuario descarga mucho que nunca usará. El `.aab` es un formato de **publicación**, no de instalación: contiene el código y los recursos organizados por módulos y dimensiones. Google Play genera con él **Split APKs** (*Dynamic Delivery*): un APK base + un split de configuración por ABI + uno por densidad + uno por idioma. Un teléfono `arm64-v8a` `xxhdpi` en español solo recibe las bibliotecas `.so` de `arm64-v8a`, las imágenes `xxhdpi` y los textos en español. Las descargas son más pequeñas (Google reporta en promedio alrededor de 15 % menos, y bastante más en apps con mucho código nativo), aumentan las instalaciones completadas y permite módulos de funciones bajo demanda (*Play Feature Delivery*). Desde agosto de 2021 Google Play exige AAB para apps nuevas.
3. **Play App Signing:** como Play genera los APK a partir del AAB, debe firmarlos él. El desarrollador firma el AAB con su **upload key** (solo prueba ante Google que la subida es legítima); Google quita esa firma y firma los APK finales con la **app signing key**, que guarda en su infraestructura segura (KMS/HSM). La identidad de la app en los dispositivos depende de la app signing key, no de la upload key. Si el desarrollador pierde o se le compromete la upload key, el propietario de la cuenta solicita en Play Console un restablecimiento: genera una nueva, exporta el certificado `.pem` y Google la registra. La app sigue actualizándose para todos los usuarios con el mismo nombre de paquete. Sin Play App Signing, perder la clave significaba no poder publicar nunca más actualizaciones y tener que lanzar una app nueva.
