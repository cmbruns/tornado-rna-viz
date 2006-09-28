PROJECT(SimtkSharedJava Java)

### Product version number: this should be different for each source change ###
# Change of major version suggests significant change in features, scope, and/or API
SET(PROJECT_MAJOR_VERSION "1")

# Change of minor version suggests backwards compatible change in API and documentation
SET(PROJECT_MINOR_VERSION "0")

# File 
SET(PRODUCT_VERSION_JAVA_TEMPLATE src/SimtkSharedJavaVersion.java.template)

SET(
  CLASSPATH
  ${CMAKE_SOURCE_DIR}/webstart/s_BrowserLauncher.jar
  ${CMAKE_SOURCE_DIR}/webstart/s_jdom.jar
  ${CMAKE_SOURCE_DIR}/src
)

FILE(GLOB_RECURSE SOURCE_FILES src/*.java)
SET(JAR_FILE_NAME ${PROJECT_NAME}.jar)

# Include certain non-class files ("resources") in the jar archive
SET(JAR_RESOURCE_PATH ${CMAKE_SOURCE_DIR})
FILE(GLOB_RECURSE RESOURCE_FILES RELATIVE ${JAR_RESOURCE_PATH} resources/*)
# Do not use contents of .svn directories from the resource areas
STRING(REGEX REPLACE "[^;]*\\.svn[^;]*" "" RESOURCE_FILES "${RESOURCE_FILES}")
STRING(REGEX REPLACE ";+" ";" RESOURCE_FILES "${RESOURCE_FILES}")

INCLUDE(${CMAKE_SOURCE_DIR}/JavaBuild.cmake)

INSTALL_FILES(
	/${INSTALL_SUBDIR}
	FILES 
	${JAR_OUTPUT_PATH}/${SIGNED_JAR_PREFIX}${JAR_FILE_NAME}
	${CMAKE_SOURCE_DIR}/webstart/${SIGNED_JAR_PREFIX}BrowserLauncher.jar
	${CMAKE_SOURCE_DIR}/webstart/${SIGNED_JAR_PREFIX}jdom.jar
    ${JNLP_TEST_OUTPUT_FILE_PATH}
    ${JNLP_DIST_OUTPUT_FILE_PATH}
)

