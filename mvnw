#!/bin/sh
MAVEN_PROJECTBASEDIR="$(cd "$(dirname "$0")"; pwd -P)"
WRAPPER_LAUNCHER="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
if [ ! -f "$WRAPPER_LAUNCHER" ]; then
  echo "Downloading Maven wrapper..."
  mkdir -p "$MAVEN_PROJECTBASEDIR/.mvn/wrapper"
  WRAPPER_URL="$(grep -E '^wrapperUrl=' "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties" | cut -d= -f2-)"
  curl -fsSL "$WRAPPER_URL" -o "$WRAPPER_LAUNCHER" || wget -q "$WRAPPER_URL" -O "$WRAPPER_LAUNCHER"
fi
exec java -jar "$WRAPPER_LAUNCHER" -Dmaven.multiModuleProjectDirectory="$MAVEN_PROJECTBASEDIR" "$@"
