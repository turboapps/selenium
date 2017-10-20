turbo config

IF "%SELENIUM_CONTAINER_VER%"=="" exit /B

echo Building version %SELENIUM_CONTAINER_VER%

@echo on

turbo build --mount=%USERPROFILE%\.ivy2 --overwrite --no-base -n=selenium/selenium-grid-plugin:%SELENIUM_CONTAINER_VER% turbo.me
