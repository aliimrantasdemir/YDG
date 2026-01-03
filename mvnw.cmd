@ECHO OFF
SETLOCAL

REM %~dp0 ends with a backslash. Remove trailing "\" to avoid breaking quoted args.
SET "MAVEN_PROJECTBASEDIR=%~dp0"
SET "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

SET "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
SET "WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"

IF NOT EXIST "%WRAPPER_JAR%" (
  ECHO Downloading Maven wrapper jar...
  FOR /F "usebackq tokens=1* delims==" %%A IN ("%WRAPPER_PROPERTIES%") DO (
    IF "%%A"=="wrapperUrl" SET "WRAPPER_URL=%%B"
  )
  powershell -Command "Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'"
)

java -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*

ENDLOCAL
