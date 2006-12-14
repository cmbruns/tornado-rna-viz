# Generate API documentation
# 1) DOC_NAME = root name for javadoc build target
# 2) DOC_SRC_DIR = top level source directory to document
# 3) DOC_PACKAGES = packages to document
# 4) CLASSPATH = java classpath



# Generate API documentation
SET(API_DOC_DIR ${CMAKE_BINARY_DIR}/doc/javadoc)
FILE(MAKE_DIRECTORY ${API_DOC_DIR})

SET(JAVADOC_STRING "${PROJECT_NAME} API v${PRODUCT_VERSION}")
ADD_CUSTOM_TARGET(${DOC_NAME}-javadoc 
	ALL 
	${JAVA_DOC}
	-sourcepath ${DOC_SRC_DIR}
	-classpath "${CLASSPATH}"
	-d ${API_DOC_DIR}
	-subpackages ${DOC_PACKAGES}
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
	${DOC_NAME}-javadoc
	PROPERTIES
	POST_INSTALL_SCRIPT
	"${CMAKE_CURRENT_BINARY_DIR}/install_javadoc.cmake"
	)
