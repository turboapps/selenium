turbo config

@echo off
REM BUILDS the selenium grid based on the turbo plugin.

IF "%SELENIUM_CONTAINER_VER%"=="" exit /B

echo Building version %SELENIUM_CONTAINER_VER%

@echo on

turbo build -n=selenium/selenium-grid:%SELENIUM_CONTAINER_VER% --no-base --overwrite selenium-grid.me %SELENIUM_CONTAINER_VER%
if %errorlevel% neq 0 exit /b %errorlevel%

turbo build -n=selenium/selenium-grid-node:%SELENIUM_CONTAINER_VER% --no-base --overwrite selenium-grid-node.me %SELENIUM_CONTAINER_VER%
if %errorlevel% neq 0 exit /b %errorlevel%