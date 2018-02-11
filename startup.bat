@echo on
set JAVA_HOME=C:\Program Files (x86)\Java\jdk1.8.0_131
set classpath=.;%JAVA_HOME%\lib\dt.jar;%JAVA_HOME%\lib\tools.jar;
set path=C:\Program Files (x86)\Java\jdk1.8.0_131\bin
java -Djava.library.path="G:/projects/wawa-client/src/main/jniLibs;" -jar target\wawa-client.jar
pause