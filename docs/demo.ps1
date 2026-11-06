$base = "http://localhost:8080"

function Titulo($texto) { Write-Host "`n=== $texto ===" -ForegroundColor Cyan }

Titulo "1. Contenedores en marcha"
docker compose ps

Titulo "2. Tablas de la base de datos"
docker exec segmentacion-db psql -U segmentacion -d segmentacion -c "\dt"

Titulo "3. Archivo con errores: acepta lo bueno y reporta lo malo"
curl.exe -s -w "`nHTTP %{http_code}`n" -F "archivo=@datos/compras_con_errores.csv" "$base/api/v1/archivos"

Titulo "4. Archivo al que le falta una columna: se rechaza"
curl.exe -s -w "`nHTTP %{http_code}`n" -F "archivo=@datos/compras_sin_columna.csv" "$base/api/v1/archivos"

Titulo "5. Archivo sintetico con 530 clientes"
$carga = curl.exe -s -F "archivo=@datos/compras_sinteticas.csv" "$base/api/v1/archivos" | ConvertFrom-Json
$carga | Format-List id_archivo, nombre, estado, registros_leidos, registros_validos

Titulo "6. Segmentacion con K-means (k=4)"
$cuerpo = '{"id_archivo":' + $carga.id_archivo + ',"tecnica":"kmeans","parametros":{"k":4}}'
$ejecucion = $cuerpo | curl.exe -s -X POST -H "Content-Type: application/json" -d "@-" "$base/api/v1/ejecuciones" | ConvertFrom-Json
Start-Sleep -Seconds 2
curl.exe -s "$base/api/v1/ejecuciones/$($ejecucion.id_ejecucion)"

Titulo "7. Segmentos obtenidos"
$segmentos = curl.exe -s "$base/api/v1/ejecuciones/$($ejecucion.id_ejecucion)/segmentos" | ConvertFrom-Json
$segmentos.segmentos | Select-Object etiqueta, num_clientes, proporcion | Format-Table -AutoSize