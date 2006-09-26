PROJECT(SimtkSharedJava Java)

SET(JAR_OUTPUT_PATH ${CMAKE_SOURCE_DIR}/webstart CACHE PATH "jar dir")
SET(CLASS_OUTPUT_PATH ${CMAKE_BINARY_DIR}/classes CACHE PATH "class dir")
SET(JAVA_TOOLS_PATH JAVA_TOOLS_PATH_UNKNOWN CACHE PATH "Directory containing java, jarsigner, and jar commands")
IF($ENV{JAVA_HOME})
  SET(JAVA_TOOLS_PATH $ENV{JAVA_HOME}/bin)
ENDIF($ENV{JAVA_HOME})

# Variables related to Java Web Start installation
SET(INSTALL_SUBDIR ${PROJECT_NAME} CACHE PATH "RELATIVE directory name where files will be installed")
SET(WEB_START_BASE_URL "file://localhost/${CMAKE_INSTALL_PREFIX}/${INSTALL_SUBDIR}" CACHE STRING "URL where Java Webstart files will be located")
IF(WIN32)
  STRING(REGEX REPLACE "C:" "C:" WEB_START_BASE_URL ${WEB_START_BASE_URL})
ENDIF(WIN32)

SET(JNLP_TEMPLATE_FILE_NAME ${CMAKE_SOURCE_DIR}/webstart/${PROJECT_NAME}.jnlp.template)
SET(JNLP_OUTPUT_FILE_NAME ${JAR_OUTPUT_PATH}/${PROJECT_NAME}.jnlp)
CONFIGURE_FILE(${JNLP_TEMPLATE_FILE_NAME} ${JNLP_OUTPUT_FILE_NAME} @ONLY)

# Variables related to private key signing of jar file
# The private key must remain on CDs inserted only during signing
SET(KEYSTORE_FILE_NAME "${CMAKE_SOURCE_DIR}/webstart/testing_keystore.keys" CACHE FILEPATH "What is the location of the Java keystore file containing your private key?")
SET(KEYSTORE_USERNAME "testing" CACHE STRING "What is the keystore username?")
SET(KEYSTORE_PASSWORD "testing" CACHE STRING "What is the keystore password?")

SET(
  CLASSPATH
  ${CMAKE_SOURCE_DIR}/webstart/s_BrowserLauncher.jar
  ${CMAKE_SOURCE_DIR}
)


FILE(GLOB_RECURSE SOURCE_FILES org/*.java)
SET(JAR_FILE_NAME SimtkSharedJava.jar)

SET(JAR_RESOURCE_PATH ${CMAKE_SOURCE_DIR})
FILE(GLOB_RECURSE RESOURCE_FILES RELATIVE ${JAR_RESOURCE_PATH} resources/*)
# Remove all contents of .svn directories
STRING(REGEX REPLACE "[^;]*\\.svn[^;]*" "" RESOURCE_FILES "${RESOURCE_FILES}")
STRING(REGEX REPLACE ";+" ";" RESOURCE_FILES "${RESOURCE_FILES}")

INCLUDE(${CMAKE_SOURCE_DIR}/JavaBuild.cmake)

INSTALL_FILES(
	/${INSTALL_SUBDIR}
	FILES 
	${JAR_OUTPUT_PATH}/s_${JAR_FILE_NAME}
	${CMAKE_SOURCE_DIR}/webstart/s_BrowserLauncher.jar
	${CMAKE_SOURCE_DIR}/webstart/s_jdom.jar
        ${JNLP_OUTPUT_FILE_NAME}
)

