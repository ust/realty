set "TOMCAT_HOME=D:\java\apache-tomcat-7.0.27"
set "MONGO_HOME=D:\java\mongodb-win32-i386-2.4.0"

start "mongod" %MONGO_HOME%\bin\mongod.exe --dbpath D:\java\data\mongo
start "mongo" %MONGO_HOME%\bin\mongo.exe
start "tomcat" %TOMCAT_HOME%\bin\startup.bat