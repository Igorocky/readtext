@echo off

set DIR_TO_STORE_BACKUP_TO="D:\temp\backups"
set DB_URL="jdbc:h2:tcp://192.168.56.1:9092/playexample2"
set DB_USER="playexample2-user"
set DB_PASSWORD="playexample2-pass"
set DIR_WITH_IMAGES="D:/temp/uploaded"
set PATH_TO_H2_JAR="D:/Install/H2/h2-2016-05-26/h2/bin/h2-1.4.192.jar"
set PATH_TO_7Z_EXE="C:/Program Files/7-Zip/7z.exe"


For /f "tokens=1-3 delims=/. " %%a in ('date /t') do (set mydate=%%c_%%b_%%a)
For /f "tokens=1-2 delims=/:" %%a in ('time /t') do (set mytime=%%a_%%b)

set CURR_TIME=%mydate%__%mytime%
set CURR_BACKUP_DIR=%DIR_TO_STORE_BACKUP_TO%\%CURR_TIME%

mkdir %CURR_BACKUP_DIR%

java -cp %PATH_TO_H2_JAR% org.h2.tools.Script -url %DB_URL% -user %DB_USER% -password %DB_PASSWORD% -script %CURR_BACKUP_DIR%/%DB_USER%-db.zip -options compression zip

%PATH_TO_7Z_EXE% a -r %CURR_BACKUP_DIR%/%DB_USER%-images.zip %DIR_WITH_IMAGES%/*