# ༓ Builds images for spoon.net/selenium ༓

### Full list of images

* selenium/selenium-grid
* selenium/selenium-grid-node
* selenium/selenium-grid-plugin


* selenium/selenium-chrome-driver
* selenium/selenium-ie-driver
* selenium/selenium-server-standalone


and the browsers used are:

* mozila/firefox
* google/chrome
* spoonbrew/ie-selenium

### Build images

Prepare

    turbo login user pass
    turbo config --hub=https://dev-stage.spoon.net

##### selenium-grid-plugin, selenium-grid and selenium-grid-node

`build-all.bat` and then `push-all.bat`

For the selenium drivers, use 

`build-others.bat` and `push-others.bat`

##### selenium-ie-driver

`turbo build -n=selenium/selenium-ie-driver --overwrite --no-base selenium-ie-driver.me`

##### selenium-chrome-driver

`turbo build -n=selenium/selenium-chrome-driver --overwrite --no-base selenium-chrome-driver.me`

##### selenium-server-standalone

`turbo build -n=selenium/selenium-server-standalone --overwrite --no-base selenium-server-standalone`

