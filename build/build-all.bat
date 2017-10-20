@echo off
REM Builds new version of the selenium grid
REM First builds the turbo grid plugin, then the apps depending on it
For /f "tokens=2-4 delims=/ " %%a in ('date /t') do (set mydate=%%c.%%a.%%b)
For /f "tokens=1-2 delims=/:" %%a in ("%TIME%") do (set mytime=%%a%%b)

REM trim whitespaces
for /f "tokens=* delims= " %%a in ("%mytime%") do set mytime=%%a

set SELENIUM_CONTAINER_VER=%mydate%.%mytime%

REM pulling latest jre-core as a hack, remove after WEB-2981
turbo pull oracle/jre-core:8.25

cd ../spoonium-grid-plugin
call build.bat
if %errorlevel% neq 0 exit /b %errorlevel%

cd ../app-builds
call build.bat

if %errorlevel% neq 0 exit /b %errorlevel%
