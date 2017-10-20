Turbo Selenium Plugin
=====================

Plugin for Selenium that allows deeper integration between Turbo.net and Selenium.

Responsible for exporting detailed information about Selenium commands being executed, taking screenshots of browsers during test execution.

Build
-----------------

This plugin is published under `selenium/selenium-grid-plugin`

To rebuild the image:

```
build
push
```

This image is used as a dependency by other selenium images (`selenium-grid` and `selenium-grid-node-XYZ`).
To rebuild all those selenium images images run:


    cd ../build
    build
    push


or see [../build/readme.md](../build/readme.md) for more details.

