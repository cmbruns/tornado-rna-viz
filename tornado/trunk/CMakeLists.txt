PROJECT(Tornado Java)

SET(JAR_OUTPUT_PATH ${CMAKE_SOURCE_DIR}/webstart CACHE PATH "jar dir")
SET(CLASS_OUTPUT_PATH ${CMAKE_BINARY_DIR}/classes CACHE PATH "class dir")
SET(JAVA_TOOLS_PATH JAVA_TOOLS_PATH_UNKNOWN CACHE PATH "Directory containing java, jarsigner, and jar commands")

# Variables related to private key signing of jar file
# The private key must remain on CDs inserted only during signing
SET(KEYSTORE_FILE_NAME "D:/simtkjava_signing/simtkjava.store" CACHE FILEPATH "What is the location of the Java keystore file containing your private key?")
SET(KEYSTORE_USERNAME "simtkjava" CACHE STRING "What is the keystore username?")
SET(KEYSTORE_PASSWORD PASSWORD_UNKNOWN CACHE STRING "What is the keystore password?")

SET(VTK_JAR ${CMAKE_SOURCE_DIR}/webstart/vtk500ribbonpatch.jar CACHE FILEPATH "Location of VTK jar file")
SET(SIMTK_SHARED_JAVA_JAR "C:/Program Files/SimtkSharedJava/SimtkSharedJavaJar/s_SimtkSharedJava.jar" CACHE FILEPATH "Location of SimtkSharedJava jar file")

SET(
  CLASSPATH
  ${CMAKE_SOURCE_DIR}/jnlp.jar
  ${CMAKE_SOURCE_DIR}/webstart/jogl.jar
  ${CMAKE_SOURCE_DIR}/webstart/s_BrowserLauncher.jar
  ${CMAKE_SOURCE_DIR}/src
  ${VTK_JAR}
  ${SIMTK_SHARED_JAVA_JAR}
)


FILE(GLOB_RECURSE SOURCE_FILES src/org/*.java)
SET(JAR_FILE_NAME Tornado.jar)

SET(JAR_RESOURCE_PATH ${CMAKE_SOURCE_DIR}/resources CACHE PATH "Directory containing resources for jar file")
FILE(GLOB_RECURSE RESOURCE_FILES RELATIVE ${JAR_RESOURCE_PATH} resources/images/*)
# Remove all contents of .svn directories
STRING(REGEX REPLACE "[^;]*\\.svn[^;]*" "" RESOURCE_FILES "${RESOURCE_FILES}")
STRING(REGEX REPLACE ";+" ";" RESOURCE_FILES "${RESOURCE_FILES}")

INCLUDE(${CMAKE_SOURCE_DIR}/JavaBuild.cmake)

INSTALL_FILES(/TornadoJar FILES ${JAR_OUTPUT_PATH}/s_${JAR_FILE_NAME})

