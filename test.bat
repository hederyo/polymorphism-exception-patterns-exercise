@echo off
if not exist bin\com\parcel\ParcelSystemTest.class (
    echo Compiling test suite...
    call compile.bat
)

if %ERRORLEVEL% equ 0 (
    echo.
    echo Running automated test suite...
    java -cp bin com.parcel.ParcelSystemTest
)
