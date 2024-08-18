@echo off

if exist "build" rmdir /S /Q "build"
mkdir "build"
mkdir "build\arm64"
mkdir "build\x64"

cmake -S . -B "build\arm64" -A arm64
cmake --build "build\arm64" --config "RelWithDebInfo"

cmake -S . -B "build\x64" -A x64
cmake --build "build\x64" --config "RelWithDebInfo"

if exist "..\src\main\resources" rmdir /S /Q "..\src\main\resources"
mkdir "..\src\main\resources"
mkdir "..\src\main\resources\arm64"
mkdir "..\src\main\resources\x64"

copy "build\arm64\RelWithDebInfo\DefFileGenerator.exe" "..\src\main\resources\arm64"
copy "build\x64\RelWithDebInfo\DefFileGenerator.exe" "..\src\main\resources\x64"
