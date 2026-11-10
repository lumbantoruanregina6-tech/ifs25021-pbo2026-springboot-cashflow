@echo off
REM Build + jalankan mode PRODUCTION (skor Lighthouse Performance jauh lebih tinggi daripada mode dev)
call mvnw.cmd clean package -Pproduction -DskipTests
if errorlevel 1 exit /b 1
java -jar target\ifs25021-pbo2026-springboot-cashflow-0.0.1-SNAPSHOT.jar
