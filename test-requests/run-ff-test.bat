@echo off
powershell -ExecutionPolicy Bypass -NonInteractive -Command "& { . '%~dp0test-ford-fulkerson.ps1' }"
