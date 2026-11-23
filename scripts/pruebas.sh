#!/usr/bin/env bash
set -euo pipefail
URL="${URL:-http://localhost:8080}"

login() {
  curl -sS -i -X POST "$URL/api/auth/login" -H "Content-Type: application/json" -d "$1"
}

sin_fugas() {
  for patron in '"trace"' 'org\.' 'java\.' 'Exception' 'hibernate' 'springframework' '\$2a\$'; do
    if grep -qi "$patron" "$1"; then
      echo "FUGA DETECTADA: $patron"
      exit 1
    fi
  done
}

echo "=== Paso 1: autenticacion exitosa con BCrypt (carlos_mora / carlos2026) ==="
login '{"username":"carlos_mora","password":"carlos2026"}' | tee /tmp/paso1.txt
echo
head -1 /tmp/paso1.txt | grep -q " 200"
TOKEN=$(tail -1 /tmp/paso1.txt | jq -r .token)
test -n "$TOKEN" && test "$TOKEN" != "null"
echo "Token JWT: ${TOKEN:0:20}..."
sin_fugas /tmp/paso1.txt

echo
echo "=== Paso 2: credencial invalida sin fuga tecnica ==="
login '{"username":"carlos_mora","password":"clave_erronea"}' | tee /tmp/paso2.txt
echo
head -1 /tmp/paso2.txt | grep -q " 401"
grep -qi "application/problem+json" /tmp/paso2.txt
sin_fugas /tmp/paso2.txt

echo
echo "=== Extra: usuario inexistente (misma respuesta, sin enumeracion de cuentas) ==="
login '{"username":"no_existe","password":"x"}' | tee /tmp/extra1.txt
echo
head -1 /tmp/extra1.txt | grep -q " 401"
test "$(tail -1 /tmp/paso2.txt | jq -c 'del(.timestamp)')" = "$(tail -1 /tmp/extra1.txt | jq -c 'del(.timestamp)')"
echo "Cuerpo identico al de clave erronea"

echo
echo "=== Extra: la contrasena antigua en texto plano ya no funciona (carlos123) ==="
login '{"username":"carlos_mora","password":"carlos123"}' > /tmp/extra2.txt
head -1 /tmp/extra2.txt
head -1 /tmp/extra2.txt | grep -q " 401"

echo
echo "=== Extra: semillas BCrypt de admin y maria_rojas ==="
for par in admin:admin123 maria_rojas:maria2026; do
  CODIGO=$(curl -sS -o /dev/null -w "%{http_code}" -X POST "$URL/api/auth/login" -H "Content-Type: application/json" \
    -d "{\"username\":\"${par%%:*}\",\"password\":\"${par#*:}\"}")
  echo "${par%%:*}: HTTP $CODIGO"
  test "$CODIGO" = "200"
done

echo
echo "=== Extra: JSON malformado sin stack trace ==="
curl -sS -i -X POST "$URL/api/auth/login" -H "Content-Type: application/json" -d '{"username":' | tee /tmp/extra3.txt
echo
head -1 /tmp/extra3.txt | grep -q " 400"
sin_fugas /tmp/extra3.txt

echo
echo "=== Extra: error 500 forzado sin stack trace (transaccion inexistente) ==="
curl -sS -i "$URL/api/transacciones/999999" -H "Authorization: Bearer $TOKEN" | tee /tmp/extra4.txt
echo
head -1 /tmp/extra4.txt | grep -q " 500"
sin_fugas /tmp/extra4.txt

echo
echo "RESULTADO: todas las verificaciones pasaron"
