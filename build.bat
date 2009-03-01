@echo off


if not "%JAVA_HOME%"=="" goto process
echo You must set the JAVA_HOME environment variable
goto end

:process
"%JAVA_HOME%\bin\java" -Dant.home=lib\build\ant -jar lib\build\ant\ant-launcher.jar %1 %2 %3 %4 %5 %6 %7 %8 %9

:end