@echo off
setlocal

rem Create output directory if it does not exist
if not exist out mkdir out

rem Compile Java sources
javac -encoding UTF-8 -d out src\Main.java
if errorlevel 1 (
  echo Build failed.
  exit /b 1
)

echo Build succeeded. Classes in .\out
exit /b 0


