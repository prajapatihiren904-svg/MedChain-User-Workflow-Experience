@echo off
echo ==================================================
echo             MEDCHAIN BUILD MANAGER               
echo ==================================================
echo.
echo Creating bin/ output directory...
if not exist bin mkdir bin

echo Compiling MedChain Java classes...
javac -d bin -sourcepath src src/com/medchain/main/Main.java

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Compilation failed! Please verify JDK is in your PATH.
    pause
    exit /b %errorlevel%
)

echo [SUCCESS] Compilation complete. Classes stored in 'bin/'.
echo.
echo To launch the application, ensure MySQL (XAMPP) is running,
echo and check that your JDBC Driver .jar is inside the 'lib/' folder.
echo then execute: run.bat
echo.
pause
