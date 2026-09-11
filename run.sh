#!/bin/bash
echo "Running MedChain Console Application..."
java -cp "bin:lib/*" com.medchain.main.Main
if [ $? -ne 0 ]; then
    echo ""
    echo "Application exited with error. Make sure you have the MySQL Connector JDBC driver jar in the 'lib/' folder."
fi
