PROJECT(Tornado)

Include(Dart)

# Fork cmake's incomplete Java methods
INCLUDE(MyFindJava.cmake)

# TODO 
MESSAGE(STATUS "SimTK_SDK is set to ${SimTK_SDK}")
IF(SimTK_SDK)
	SET(MY_INSTALL_PREFIX "/${PROJECT_NAME}")
ELSE(SimTK_SDK)
	SET(MY_INSTALL_PREFIX "")
ENDIF(SimTK_SDK)

SET(SHARED_JAVA_DIST_VERSION "0.7")
FIND_PATH(
	SHARED_JAVA_PATH
	s_SimtkSharedJava.jar
	"${SimTK_SDK}/SimtkSharedJava/bin"
	"${SimTK_SDK}/bin"
	"C:/Program Files/SimtkSharedJava/bin"
	)
MESSAGE(STATUS "SHARED_JAVA_PATH is set to ${SHARED_JAVA_PATH}")

### Product version number: this should be different for each source change ###
# Change of major version suggests significant change in features, scope, and/or API
SET(PROJECT_MAJOR_VERSION "1")
# Change of minor version suggests backwards compatible change in API and documentation
SET(PROJECT_MINOR_VERSION "0")

INCLUDE(${CMAKE_SOURCE_DIR}/SvnRevision.cmake)
SET(PRODUCT_VERSION "${PROJECT_MAJOR_VERSION}.${PROJECT_MINOR_VERSION}.${SVN_REVISION}")

# Default to sign jar files, if not already set
SET(DO_SIGN_JARS ON CACHE BOOL "Sign the jar files with secure digital signature?")

# If we are building for distribution from simtk.org, sign using our private key
# otherwise, sign using a simple test key
IF(DO_SIGN_JARS)
	SET(DO_BUILD_DIST OFF CACHE BOOL "Create JNLP for distribution?")

	SET(JARSIGNER_KEYSTORE "${CMAKE_SOURCE_DIR}/webstart/testing_keystore.keys" CACHE FILEPATH "Name of keystore file containing your private key for jar signing")
	SET(JARSIGNER_USER_NAME "testing" CACHE STRING "User name for key in keystore file")
	SET(JARSIGNER_PASSWORD "testing" CACHE STRING "Password for key in keystore file")

	IF(DO_BUILD_DIST)
	  SET(WEB_START_DIST_BASE_URL "http://public.simtk.org/distrib/${PROJECT_NAME}/${PROJECT_MAJOR_VERSION}.${PROJECT_MINOR_VERSION}/bin" CACHE STRING "URL where Java Webstart distributed files will be located")
	  SET(JNLP_DIST_TEMPLATE_FILE_PATH ${CMAKE_SOURCE_DIR}/webstart/${PROJECT_NAME}_dist.jnlp.template)
	  SET(JNLP_DIST_OUTPUT_FILE_PATH ${JAR_OUTPUT_PATH}/${PROJECT_NAME}_dist.jnlp)
	  CONFIGURE_FILE(${JNLP_DIST_TEMPLATE_FILE_PATH} ${JNLP_DIST_OUTPUT_FILE_PATH} @ONLY)
	  IF(NOT WAS_BUILD_DIST)
	      # For security, keystore is located on a removable CD
	      SET(JARSIGNER_KEYSTORE "D:/simtkjava_signing/simtkjava.store" CACHE FILEPATH "Name of keystore file containing your private key for jar signing" FORCE)
	      SET(JARSIGNER_USER_NAME "simtkjava" CACHE STRING "User name for key in keystore file" FORCE)
	      SET(JARSIGNER_PASSWORD "ENTER_PASSWORD_HERE" CACHE STRING "Password for key in keystore file" FORCE)
	      SET(WAS_BUILD_DIST ${DO_BUILD_DIST} CACHE INTERNAL "Variable to keep track of when the DO_BUILD_DIST variable has changed" FORCE)
	  ENDIF(NOT WAS_BUILD_DIST)
	ELSE(DO_BUILD_DIST)

    IF(WAS_BUILD_DIST)
		# Set default values for JARSIGNER variables that will usually work
		# For a real release, the user should set these to private values in the interface
		SET(JARSIGNER_KEYSTORE "${CMAKE_SOURCE_DIR}/webstart/testing_keystore.keys" CACHE FILEPATH "Name of keystore file containing your private key for jar signing" FORCE)
		SET(JARSIGNER_USER_NAME "testing" CACHE STRING "User name for key in keystore file" FORCE)
		SET(JARSIGNER_PASSWORD "testing" CACHE STRING "Password for key in keystore file" FORCE)
		SET(WAS_BUILD_DIST ${DO_BUILD_DIST} CACHE INTERNAL "Variable to keep track of when the DO_BUILD_DIST variable has changed" FORCE)		
    ENDIF(WAS_BUILD_DIST)
		
	ENDIF(DO_BUILD_DIST)
ENDIF(DO_SIGN_JARS)

# VTK
# TODO - vtk.jar should eventually become s_vtk.jar, a signed jar file
FIND_PATH(
	VTK_PATH
	s_vtk.jar
	"${SimTK_SDK}/vtk/bin"
	"C:/Program Files/VTK/bin"
	)
SET(VTK_JAR "${VTK_PATH}/s_vtk.jar" CACHE FILEPATH "Location of VTK jar file")

SET(
  CLASSPATH
  ${CMAKE_SOURCE_DIR}/jnlp.jar
  ${SHARED_JAVA_PATH}/s_SimtkSharedJava.jar
  ${SHARED_JAVA_PATH}/s_BrowserLauncher.jar
  ${SHARED_JAVA_PATH}/s_jdom.jar
  ${SHARED_JAVA_PATH}/s_Jama-1.0.2.jar
  ${CMAKE_SOURCE_DIR}/src
  ${VTK_JAR}
  ${CMAKE_SOURCE_DIR}/webstart/jogl-1_0_0-webstart/jogl.jar
)

# Create source file with current version information
# Autogenerate source file for internal product version
SET(AUTOGEN_SRC_DIR "${CMAKE_BINARY_DIR}/autosource")
FILE(MAKE_DIRECTORY ${AUTOGEN_SRC_DIR})
SET(CLASSPATH ${CLASSPATH} ${AUTOGEN_SRC_DIR})
MESSAGE(STATUS "CLASSPATH is set to ${CLASSPATH}")

SET(PRODUCT_VERSION_JAVA_TEMPLATE "${CMAKE_SOURCE_DIR}/src/version/TornadoVersion.java.template")
GET_FILENAME_COMPONENT(VERSION_TEMPLATE_FILE_NAME ${PRODUCT_VERSION_JAVA_TEMPLATE} NAME)
STRING(REPLACE ".template" "" VERSION_SOURCE_FILE_NAME ${VERSION_TEMPLATE_FILE_NAME})
FILE(MAKE_DIRECTORY ${AUTOGEN_SRC_DIR}/version)
CONFIGURE_FILE(${PRODUCT_VERSION_JAVA_TEMPLATE} ${AUTOGEN_SRC_DIR}/version/${VERSION_SOURCE_FILE_NAME})

SET(JAR_FILE "Tornado.jar")
SET(SOURCE_DIRS "${CMAKE_CURRENT_SOURCE_DIR}/src/org" "${AUTOGEN_SRC_DIR}/version")
SET(RESOURCE_DIRS 
	"${CMAKE_CURRENT_SOURCE_DIR}/resources/images" 
	"${CMAKE_CURRENT_SOURCE_DIR}/resources/rnaml" 
	"${CMAKE_CURRENT_SOURCE_DIR}/resources/sstructs" 
	"${CMAKE_CURRENT_SOURCE_DIR}/resources/structures" 
	"${CMAKE_CURRENT_SOURCE_DIR}/resources/structures_with_rnaml" 
	)
INCLUDE(${CMAKE_SOURCE_DIR}/JavaBuild.cmake)

ADD_CUSTOM_TARGET(
	${JAR_FILE}-version ALL 
	DEPENDS "${AUTOGEN_SRC_DIR}/version/${VERSION_SOURCE_FILE_NAME}"
	)
ADD_DEPENDENCIES(${JAR_FILE}-javac ${JAR_FILE}-version)


# Generate jnlp file for java webstart
# Create jnlp files with appropriately modified code base URLs
SET(WEB_START_TEST_BASE_URL "file://localhost/${CMAKE_INSTALL_PREFIX}/bin" CACHE STRING "URL where Java Webstart local test files will be located")
SET(JNLP_TEST_TEMPLATE_FILE_PATH ${CMAKE_SOURCE_DIR}/webstart/Tornado_local.jnlp.template)
SET(JNLP_TEST_OUTPUT_FILE_PATH ${CMAKE_BINARY_DIR}/Tornado_local.jnlp)
CONFIGURE_FILE(${JNLP_TEST_TEMPLATE_FILE_PATH} ${JNLP_TEST_OUTPUT_FILE_PATH} @ONLY)

SET(WEB_START_DIST_BASE_URL "http://public.simtk.org/distrib/Tornado/${PROJECT_MAJOR_VERSION}.${PROJECT_MINOR_VERSION}/bin" CACHE STRING "URL where Java Webstart public files will be located")
SET(JNLP_DIST_TEMPLATE_FILE_PATH ${CMAKE_SOURCE_DIR}/webstart/Tornado_dist.jnlp.template)
SET(JNLP_DIST_OUTPUT_FILE_PATH ${CMAKE_BINARY_DIR}/Tornado_dist.jnlp)
CONFIGURE_FILE(${JNLP_DIST_TEMPLATE_FILE_PATH} ${JNLP_DIST_OUTPUT_FILE_PATH} @ONLY)

# Autogenerate Javadoc files
SET(DOC_NAME "Tornado.jar")
SET(DOC_SRC_DIR "${CMAKE_SOURCE_DIR}/src")
SET(DOC_PACKAGES "org")
INCLUDE(JavaDoc.cmake)

# Just make one configuration type for nightly builds
SET(CMAKE_CONFIGURATION_TYPES "Release" CACHE STRING  "" FORCE)

# Install jar files in bin directory
INSTALL_FILES(
	${MY_INSTALL_PREFIX}/bin
	FILES 
    ${JNLP_TEST_OUTPUT_FILE_PATH}
    ${JNLP_DIST_OUTPUT_FILE_PATH}
)
