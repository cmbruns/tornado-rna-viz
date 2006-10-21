# JavaBuild.cmake
#
# The following variables must be defined before the INCLUDES()
# declaration in your CMakeLists.txt
#
# SOURCE_FILES: the Java source files to compile
# CLASSPATH: the classpath to use when compiling
# CLASS_OUTPUT_PATH: where to put the compiled classes
# JAR_FILE_NAME: the name of the jar file to create
# PROJECT_MAJOR_VERSION: Major version number of this software product
# PROJECT_MINOR_VERSION: Minor version number of this software product
# JAR_RESOURCE_PATH: Parent directory for non-class files to put in jar file
# RESOURCE_FILES: Non-class files to add to jar
#

# Decide whether to sign the jar files
SET(DO_SIGN_JARS ON CACHE BOOL "Sign the jar files?")

IF (DO_SIGN_JARS)
  SET(SIGNED_JAR_PREFIX "s_")
ELSE(DO_SIGN_JARS)
  SET(SIGNED_JAR_PREFIX "")
ENDIF(DO_SIGN_JARS)

# Change of build version suggests a new build for bug fix, patch, or any other small reason
# Get the subversion revision number if we can
# It's possible that WIN32 installs use svnversion through cygwin.
FIND_PROGRAM (SVNVERSION svnversion)
IF (SVNVERSION-NOTFOUND)
  MESSAGE (STATUS 
    "Could not find svnversion executable. Suggest you install a
Subversion client with svnversion in path."
    )
  SET (SVN_REVISION unknown) 
ELSE (SVNVERSION-NOTFOUND)
  EXEC_PROGRAM (${SVNVERSION}
    ARGS "${PROJECT_SOURCE_DIR}"
    OUTPUT_VARIABLE SVN_REVISION)
ENDIF (SVNVERSION-NOTFOUND)
# Remove colon from build version, for easier placement in directory names
STRING(REGEX REPLACE ":" "_" SVN_REVISION ${SVN_REVISION})
SET(PROJECT_BUILD_VERSION ${SVN_REVISION})
# 
SET(PROJECT_VERSION 
	"${PROJECT_MAJOR_VERSION}.${PROJECT_MINOR_VERSION}.${PROJECT_BUILD_VERSION}"
	CACHE 
	STRING
	"Version number of ${PROJECT_NAME}"
	FORCE
	)

IF (DO_SIGN_JARS)
  # Find jarsigner program, which is not ordinarily found by CMake's java routines
  FIND_PROGRAM(Java_JARSIGNER jarsigner DOC "Location of java jarsigner program")
ENDIF(DO_SIGN_JARS)

# Decide whether to make local and/or distribution JNLP files
SET(DO_BUILD_TEST ON CACHE BOOL "Create JNLP for local testing?")
SET(DO_BUILD_DIST OFF CACHE BOOL "Create JNLP for distribution?")
IF(DO_BUILD_DIST)
  SET(DO_SIGN_JARS_DIST ON)
ELSE(DO_BUILD_DIST)
  SET(DO_SIGN_JARS_DIST OFF)
ENDIF(DO_BUILD_DIST)

SET(JAR_OUTPUT_PATH ${CMAKE_BINARY_DIR}/jars CACHE PATH "jar dir")
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
  SET(WEB_START_TEST_BASE_URL "file://localhost/${CMAKE_INSTALL_PREFIX}/bin" CACHE STRING "URL where Java Webstart local test files will be located")
  SET(JNLP_TEST_TEMPLATE_FILE_PATH ${CMAKE_SOURCE_DIR}/webstart/${PROJECT_NAME}_local.jnlp.template)
  SET(JNLP_TEST_OUTPUT_FILE_PATH ${JAR_OUTPUT_PATH}/${PROJECT_NAME}_local.jnlp)
  CONFIGURE_FILE(${JNLP_TEST_TEMPLATE_FILE_PATH} ${JNLP_TEST_OUTPUT_FILE_PATH} @ONLY)
ENDIF(DO_BUILD_TEST)
IF(DO_BUILD_DIST)
  SET(WEB_START_DIST_BASE_URL "http://public.simtk.org/distrib/${PROJECT_NAME}/${PROJECT_VERSION}" CACHE STRING "URL where Java Webstart distributed files will be located")
  SET(JNLP_DIST_TEMPLATE_FILE_PATH ${CMAKE_SOURCE_DIR}/webstart/${PROJECT_NAME}_dist.jnlp.template)
  SET(JNLP_DIST_OUTPUT_FILE_PATH ${JAR_OUTPUT_PATH}/${PROJECT_NAME}_dist.jnlp)
  CONFIGURE_FILE(${JNLP_DIST_TEMPLATE_FILE_PATH} ${JNLP_DIST_OUTPUT_FILE_PATH} @ONLY)
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

FILE(MAKE_DIRECTORY ${CLASS_OUTPUT_PATH})
FILE(MAKE_DIRECTORY ${JAR_OUTPUT_PATH})

# Directory for autogenerated source files, such as that encoding the product version
SET(AUTOGEN_SRC_DIR ${CMAKE_BINARY_DIR}/src)

SET(CLASSPATH_INTERNAL ${CLASSPATH} ${AUTOGEN_SRC_DIR})
IF(NOT WIN32)
  STRING(REGEX REPLACE ";" ":" CLASSPATH_INTERNAL "${CLASSPATH_INTERNAL}")
ENDIF(NOT WIN32)

#write the source list to a file to avoid command line overflow
SET(SOURCE_LIST_FILE ${CMAKE_BINARY_DIR}/${JAR_FILE_NAME}.srclist)
STRING(REGEX REPLACE ";" "\n" SOURCE_FILES_INTERNAL "${SOURCE_FILES}")
FILE(WRITE ${SOURCE_LIST_FILE} ${SOURCE_FILES_INTERNAL})

#Write the resources list to a file so I can remove .svn items
SET(RESOURCE_LIST_FILE ${CMAKE_BINARY_DIR}/${JAR_FILE_NAME}.resourcelist)
STRING(REGEX REPLACE ";" "\n-C ${JAR_RESOURCE_PATH} " RESOURCE_FILES "${RESOURCE_FILES}")
FILE(WRITE ${RESOURCE_LIST_FILE} ${RESOURCE_FILES})

ADD_CUSTOM_TARGET(${JAR_FILE_NAME}-javac ALL ${CMAKE_Java_COMPILER} -classpath "${CLASSPATH_INTERNAL}" -d ${CLASS_OUTPUT_PATH} @${SOURCE_LIST_FILE})

ADD_CUSTOM_TARGET(${JAR_FILE_NAME}-jar ALL ${CMAKE_Java_ARCHIVE} -cf ${JAR_OUTPUT_PATH}/${JAR_FILE_NAME} -C ${CLASS_OUTPUT_PATH} . @${RESOURCE_LIST_FILE})
ADD_DEPENDENCIES(${JAR_FILE_NAME}-jar ${JAR_FILE_NAME}-javac)

IF(DO_SIGN_JARS)
  ADD_CUSTOM_TARGET(${JAR_FILE_NAME}-jarsigner ALL ${Java_JARSIGNER} -keystore ${KEYSTORE_FILE_NAME} -storepass ${KEYSTORE_PASSWORD} -signedJar ${JAR_OUTPUT_PATH}/${SIGNED_JAR_PREFIX}${JAR_FILE_NAME} ${JAR_OUTPUT_PATH}/${JAR_FILE_NAME} ${KEYSTORE_USER_NAME})
  ADD_DEPENDENCIES(${JAR_FILE_NAME}-jarsigner ${JAR_FILE_NAME}-jar)
ENDIF(DO_SIGN_JARS)

# Autogenerate source file for internal product version
GET_FILENAME_COMPONENT(VERSION_TEMPLATE_FILE_NAME ${PRODUCT_VERSION_JAVA_TEMPLATE} NAME)
STRING(REPLACE ".template" "" VERSION_SOURCE_FILE_NAME ${VERSION_TEMPLATE_FILE_NAME})
CONFIGURE_FILE(${PRODUCT_VERSION_JAVA_TEMPLATE} ${AUTOGEN_SRC_DIR}/version/${VERSION_SOURCE_FILE_NAME})
ADD_CUSTOM_TARGET(${JAR_FILE_NAME}-version ALL ${CMAKE_Java_COMPILER} -d ${CLASS_OUTPUT_PATH} ${AUTOGEN_SRC_DIR}/version/${VERSION_SOURCE_FILE_NAME})
ADD_DEPENDENCIES(${JAR_FILE_NAME}-javac ${JAR_FILE_NAME}-version)

# Generate API documentation
SET(DOC_DIR ${CMAKE_BINARY_DIR}/doc)
FILE(MAKE_DIRECTORY ${DOC_DIR})
SET(API_DOC_DIR ${DOC_DIR}/javadoc)
FILE(MAKE_DIRECTORY ${API_DOC_DIR})
SET(JAVADOC_PACKAGES org)
SET(JAVADOC_STRING "${PROJECT_NAME} API v${PROJECT_VERSION}")
ADD_CUSTOM_TARGET(${JAR_FILE_NAME}-javadoc 
	ALL 
	${Java_JAVADOC}
	-sourcepath ${CMAKE_SOURCE_DIR}/src
	-classpath "${CLASSPATH_INTERNAL}"
	-d ${API_DOC_DIR}
	-subpackages ${JAVADOC_PACKAGES}
	-doctitle "${JAVADOC_STRING}"
	-windowtitle "${JAVADOC_STRING}"
	-use
	)

# Kludge to install full directory structure of javadoc files
CONFIGURE_FILE(
	${CMAKE_CURRENT_SOURCE_DIR}/install_javadoc.cmake.in
	${CMAKE_CURRENT_BINARY_DIR}/install_javadoc.cmake
	@ONLY
	)
SET_TARGET_PROPERTIES(
	${JAR_FILE_NAME}-javadoc
	PROPERTIES
	PRE_INSTALL_SCRIPT
	"${CMAKE_CURRENT_BINARY_DIR}/install_javadoc.cmake"
	)
