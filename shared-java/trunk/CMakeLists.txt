PROJECT(SimtkSharedJava Java)

# Decide whether to sign the jar files
SET(DO_SIGN_JARS ON CACHE BOOL "Sign the jar files?")
IF (DO_SIGN_JARS)
  SET(SIGNED_JAR_PREFIX "s_")
  # Find jarsigner program, which is not ordinarily found by CMake's java routines
  FIND_PROGRAM(Java_JARSIGNER jarsigner DOC "Location of java jarsigner program")
ELSE(DO_SIGN_JARS)
  SET(SIGNED_JAR_PREFIX "")
ENDIF(DO_SIGN_JARS)

# Decide whether to make local and/or distribution JNLP files
SET(DO_BUILD_TEST ON CACHE BOOL "Create JNLP for local testing?")
SET(DO_BUILD_DIST OFF CACHE BOOL "Create JNLP for distribution?")
IF(DO_BUILD_DIST)
  SET(DO_SIGN_JARS_DIST ON)
ELSE(DO_BUILD_DIST)
  SET(DO_SIGN_JARS_DIST OFF)
ENDIF(DO_BUILD_DIST)

# Choose release version number and subdirectory name
SET(SIMTK_SHARED_JAVA_VERSION "1.0.0rc1" CACHE STRING "Simtk shared Java version number for new build")
SET(INSTALL_SUBDIR ${PROJECT_NAME}/${SIMTK_SHARED_JAVA_VERSION} CACHE PATH "RELATIVE directory name where files will be installed")

SET(JAR_OUTPUT_PATH ${CMAKE_SOURCE_DIR}/webstart CACHE PATH "jar dir")
SET(CLASS_OUTPUT_PATH ${CMAKE_BINARY_DIR}/classes CACHE PATH "class dir")

IF(DO_SIGN_JARS)
  SET(KEYSTORE_FILE_NAME "${CMAKE_SOURCE_DIR}/webstart/testing_keystore.keys" CACHE FILEPATH "Name of keystore file containing your private key for jar signing")
  SET(KEYSTORE_USER_NAME "testing" CACHE STRING "User name for key in keystore file")
  SET(KEYSTORE_PASSWORD "testing" CACHE STRING "Password for key in keystore file")
ENDIF(DO_SIGN_JARS)

# Variables related to Java Web Start installation
# Variables related to private key signing of jar file
# The private key must remain on CDs inserted only during signing
# If the build is for a distribution, we should use the official key for signing
# Otherwise, it's OK to use a lame test key for signing
IF(DO_BUILD_TEST)
  # Create jnlp files with appropriately modified code base URLs
  SET(WEB_START_TEST_BASE_URL "file://localhost/${CMAKE_INSTALL_PREFIX}/${INSTALL_SUBDIR}" CACHE STRING "URL where Java Webstart local test files will be located")
  SET(JNLP_TEST_TEMPLATE_FILE_NAME ${CMAKE_SOURCE_DIR}/webstart/${PROJECT_NAME}_local.jnlp.template)
  SET(JNLP_TEST_OUTPUT_FILE_NAME ${JAR_OUTPUT_PATH}/${PROJECT_NAME}_local.jnlp)
  CONFIGURE_FILE(${JNLP_TEST_TEMPLATE_FILE_NAME} ${JNLP_TEST_OUTPUT_FILE_NAME} @ONLY)
ENDIF(DO_BUILD_TEST)
IF(DO_BUILD_DIST)
  SET(WEB_START_DIST_BASE_URL "http://public.simtk.org/distrib/simtk_shared_java/${SIMTK_SHARED_JAVA_VERSION}" CACHE STRING "URL where Java Webstart distributed files will be located")
  SET(JNLP_DIST_TEMPLATE_FILE_NAME ${CMAKE_SOURCE_DIR}/webstart/${PROJECT_NAME}_dist.jnlp.template)
  SET(JNLP_DIST_OUTPUT_FILE_NAME ${JAR_OUTPUT_PATH}/${PROJECT_NAME}_dist.jnlp)
  CONFIGURE_FILE(${JNLP_DIST_TEMPLATE_FILE_NAME} ${JNLP_DIST_OUTPUT_FILE_NAME} @ONLY)
  IF(DO_SIGN_JARS)
    IF(NOT WAS_BUILD_DIST)
      # For security, keystore is located on a removable CD
      SET(KEYSTORE_FILE_NAME "D:/simtkjava_signing/simtkjava.store" CACHE FILEPATH "Name of keystore file containing your private key for jar signing" FORCE)
      SET(KEYSTORE_USER_NAME "simtkjava" CACHE STRING "User name for key in keystore file" FORCE)
      SET(KEYSTORE_PASSWORD "ENTER_PASSWORD_HERE" CACHE STRING "Password for key in keystore file" FORCE)
      SET(WAS_BUILD_DIST ${DO_BUILD_DIST} CACHE INTERNAL "Variable to keep track of when the DO_BUILD_DIST variable has changed" FORCE)
    ENDIF(NOT WAS_BUILD_DIST)
  ENDIF(DO_SIGN_JARS)
ELSE(DO_BUILD_DIST)
  IF(DO_SIGN_JARS)
    IF(WAS_BUILD_DIST)
      SET(KEYSTORE_FILE_NAME "${CMAKE_SOURCE_DIR}/webstart/testing_keystore.keys" CACHE FILEPATH "Name of keystore file containing your private key for jar signing" FORCE)
      SET(KEYSTORE_USER_NAME "testing" CACHE STRING "User name for key in keystore file" FORCE)
      SET(KEYSTORE_PASSWORD "testing" CACHE STRING "Password for key in keystore file" FORCE)
      SET(WAS_BUILD_DIST ${DO_BUILD_DIST} CACHE INTERNAL "Variable to keep track of when the DO_BUILD_DIST variable has changed" FORCE)
    ENDIF(WAS_BUILD_DIST)
  ENDIF(DO_SIGN_JARS)  
ENDIF(DO_BUILD_DIST)

SET(
  CLASSPATH
  ${CMAKE_SOURCE_DIR}/webstart/${SIGNED_JAR_PREFIX}BrowserLauncher.jar
  ${CMAKE_SOURCE_DIR}/webstart/${SIGNED_JAR_PREFIX}jdom.jar
  ${CMAKE_SOURCE_DIR}
)

FILE(GLOB_RECURSE SOURCE_FILES src/*.java)
SET(JAR_FILE_NAME SimtkSharedJava.jar)

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
    ${JNLP_TEST_OUTPUT_FILE_NAME}
    ${JNLP_DIST_OUTPUT_FILE_NAME}
)

