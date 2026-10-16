@echo off
if not exist bin\com\parcel\Main.class (
    echo Building project before running...
    call compile.bat
)

if %ERRORLEVEL% equ 0 (
    echo Starting Parcel Delivery System...
    echo.
    java -cp bin com.parcel.Main
)
