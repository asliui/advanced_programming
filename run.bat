@echo off
cd /d "%~dp0"

rem Run Lab 6 (compulsory + homework)
mvn exec:java "-Dexec.mainClass=ro.uaic.asli.lab6.Lab6App"

pause

