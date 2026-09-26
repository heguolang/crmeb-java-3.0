$mysql = 'C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe'
$env:MYSQL_PWD = 'root'
$work = Join-Path $env:TEMP 'form_tips_clean'
if (Test-Path $work) { Remove-Item $work -Recurse -Force }
New-Item -ItemType Directory -Path $work | Out-Null

$ids = @(67,68,74,76,82,83,122,127,128,129,134,145)
$rx = New-Object System.Text.RegularExpressions.Regex '"tipsLink":"https?://help\.crmeb\.net[^"]*"'
foreach ($id in $ids) {
  $rawFile = Join-Path $work ("{0}.raw" -f $id)
  & $mysql --host=127.0.0.1 -uroot --default-character-set=utf8mb4 --batch --raw --skip-column-names crmeb -e ("SELECT content FROM eb_system_form_temp WHERE id={0}" -f $id) | Set-Content -Path $rawFile -Encoding UTF8
  $content = [System.IO.File]::ReadAllText($rawFile).Trim()
  $new = $rx.Replace($content, '"tipsLink":""')
  if ($new -eq $content) {
    Write-Host ("nochange id={0}" -f $id)
    continue
  }
  $sqlFile = Join-Path $work ("{0}.sql" -f $id)
  $esc = $new.Replace('\', '\\').Replace("'", "''")
  $sql = "UPDATE eb_system_form_temp SET content='" + $esc + "' WHERE id=" + $id + ";"
  [System.IO.File]::WriteAllText($sqlFile, $sql, [System.Text.UTF8Encoding]::new($false))
  Get-Content -Raw -Encoding UTF8 $sqlFile | & $mysql --host=127.0.0.1 -uroot --default-character-set=utf8mb4 crmeb
  Write-Host ("cleaned id={0} exit={1}" -f $id, $LASTEXITCODE)
}

& $mysql --host=127.0.0.1 -uroot --default-character-set=utf8mb4 crmeb -N -e "SELECT COUNT(*) FROM eb_system_form_temp WHERE content LIKE '%help.crmeb.net%'"
& $mysql --host=127.0.0.1 -uroot --default-character-set=utf8mb4 crmeb -N -e "SELECT COUNT(*) FROM eb_system_form_temp WHERE content LIKE '%点击查看详细%'"
