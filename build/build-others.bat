REM Builds the selenium drivers for the different browsers
turbo build --no-base --overwrite selenium-server-standalone.me
turbo build --no-base --overwrite selenium-ie-driver.me
turbo build --no-base --overwrite selenium-chrome-driver.me
turbo build --no-base --overwrite selenium-gecko-driver.me