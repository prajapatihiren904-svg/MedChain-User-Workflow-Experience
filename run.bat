@echo off
echo Running MedChain Console Application...
java -cp "bin;lib/*" com.medchain.main.Main
if %errorlevel% neq 0 (
    echo.
    echo Application exited with error code %errorlevel%
    echo Make sure you have placed the mysql-connector-j driver jar inside the 'lib/' folder.
)
pause
