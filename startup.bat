set "TOMCAT_HOME=D:\java\apache-tomcat-7.0.27"
set "MONGO_HOME=D:\java\mongodb-win32-x86_64-2.4.4"

start "mongod" %MONGO_HOME%\bin\mongod.exe --config %MONGO_HOME%\mongodb.conf --dbpath D:\java\data\mongo
start "mongo" %MONGO_HOME%\bin\mongo.exe
rem start "tomcat" %TOMCAT_HOME%\bin\startup.bat