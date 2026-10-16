@echo off
echo ==============================================
echo Compiling Parcel Delivery System...
echo ==============================================

if not exist bin mkdir bin

javac -d bin src\com\parcel\exception\*.java src\com\parcel\strategy\*.java src\com\parcel\model\*.java src\com\parcel\service\*.java src\com\parcel\ui\*.java src\com\parcel\*.java

if %ERRORLEVEL% equ 0 (
    echo.
    echo Compilation successful! Class files located in .\bin
) else (
    echo.
    echo Compilation failed with error code %ERRORLEVEL%.
)
