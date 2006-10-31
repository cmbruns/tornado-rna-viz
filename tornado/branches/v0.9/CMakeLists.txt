PROJECT(Tornado Java)

Include(Dart)

### Product version number: this should be different for each source change ###
# Change of major version suggests significant change in features, scope, and/or API
SET(PROJECT_MAJOR_VERSION "0")

# Change of minor version suggests backwards compatible change in API and documentation
SET(PROJECT_MINOR_VERSION "9")

# File 
SET(PRODUCT_VERSION_JAVA_TEMPLATE src/version/TornadoVersion.java.template)

SET(SHARED_JAVA_DEFAULT_VERSION "0.3")
SET(SHARED_JAVA_PATH "C:/Program Files/SimtkSharedJava/bin" CACHE FILEPATH "Directory containing local SimtkSharedJava jar files")
SET(SHARED_JAVA_LOCAL_URL "file://localhost/${SHARED_JAVA_PATH}/SimtkSharedJava_local.jnlp" CACHE STRING "URL of local Simtk Shared Java jnlp file")
SET(SHARED_JAVA_DIST_URL "http://public.simtk.org/distrib/SimtkSharedJava/${SHARED_JAVA_DEFAULT_VERSION}/bin/SimtkSharedJava_dist.jnlp" CACHE STRING "URL of web Simtk Shared Java jnlp file")
SET(VTK_JAR "C:/Program Files/VTK/bin/vtk.jar" CACHE FILEPATH "Location of VTK jar file")

SET(
  CLASSPATH
  ${CMAKE_SOURCE_DIR}/jnlp.jar
  ${SHARED_JAVA_PATH}/s_SimtkSharedJava.jar
  ${SHARED_JAVA_PATH}/s_BrowserLauncher.jar
  ${SHARED_JAVA_DIR}/s_jdom.jar
  ${CMAKE_SOURCE_DIR}/src
  ${VTK_JAR}
  ${CMAKE_SOURCE_DIR}/webstart/jogl-1_0_0-webstart/jogl.jar
)

FILE(GLOB_RECURSE SOURCE_FILES src/*.java)
SET(JAR_FILE_NAME ${PROJECT_NAME}.jar)

# Include certain non-class files ("resources") in the jar archive
SET(JAR_RESOURCE_PATH ${CMAKE_SOURCE_DIR}/resources)
FILE(GLOB_RECURSE RESOURCE_FILES RELATIVE ${JAR_RESOURCE_PATH} resources/*)
# Do not use contents of .svn directories from the resource areas
STRING(REGEX REPLACE "[^;]*\\.svn[^;]*" "" RESOURCE_FILES "${RESOURCE_FILES}")
STRING(REGEX REPLACE ";+" ";" RESOURCE_FILES "${RESOURCE_FILES}")

INCLUDE(${CMAKE_SOURCE_DIR}/JavaBuild.cmake)

# Just make one configuration type for nightly builds
SET(CMAKE_CONFIGURATION_TYPES "Release" CACHE STRING  "" FORCE)

# Install jar files in bin directory
INSTALL_FILES(
	/bin
	FILES 
	${JAR_OUTPUT_PATH}/${SIGNED_JAR_PREFIX}${JAR_FILE_NAME}
    ${JNLP_TEST_OUTPUT_FILE_PATH}
    ${JNLP_DIST_OUTPUT_FILE_PATH}
)
