@echo off
setlocal

if not exist out (
  echo No build found. Building now...
  call build.bat || exit /b 1
)

java -cp out Main


