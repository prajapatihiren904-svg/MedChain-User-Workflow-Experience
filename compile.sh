#!/bin/bash
echo "=================================================="
echo "             MEDCHAIN BUILD MANAGER               "
echo "=================================================="
echo ""
echo "Creating bin/ output directory..."
mkdir -p bin

echo "Compiling MedChain Java classes..."
javac -d bin -sourcepath src src/com/medchain/main/Main.java

if [ $? -ne 0 ]; then
    echo ""
    echo "[ERROR] Compilation failed! Please verify JDK is installed."
    exit 1
fi

echo "[SUCCESS] Compilation complete. Classes stored in 'bin/'."
echo ""
echo "To launch the application, ensure MySQL is running,"
echo "and check that your JDBC Driver .jar is inside the 'lib/' folder."
echo "then execute: ./run.sh"
echo ""
chmod +x run.sh 2>/dev/null
