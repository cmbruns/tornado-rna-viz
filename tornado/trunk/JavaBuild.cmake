# JavaBuild.cmake
#
# The following variables must be defined before the INCLUDES()
# declaration in your CMakeLists.txt
#
# SOURCE_DIRS: the top level Java source directories to compile
# CLASSPATH: the classpath to use when compiling
# JAR_FILE: the name of the jar file to create
# RESOURCE_DIRS: non-source directories to add to jar archive

SET(CLASS_OUTPUT_PATH ${CMAKE_BINARY_DIR}/classes/${JAR_FILE}.dir)
FILE(MAKE_DIRECTORY ${CLASS_OUTPUT_PATH})

# Remember directory name for later jarring	
SET(CLASS_DIRS "${CLASS_OUTPUT_PATH}")

### 1) Compile sources
SET(JAR_SOURCE_FILES "")
FOREACH(SOURCE_DIR ${SOURCE_DIRS})

	# MESSAGE("SOURCE_DIR = ${SOURCE_DIR}")

	# Parent directory
	GET_FILENAME_COMPONENT(PARENT_DIR ${SOURCE_DIR} PATH)	
	# Minor directory name
	FILE(RELATIVE_PATH JAR_SOURCE_NAME ${PARENT_DIR} ${SOURCE_DIR})

	
	FILE(GLOB_RECURSE SOURCE_FILES ${SOURCE_DIR}/*.java)
	FOREACH(SOURCE_FILE ${SOURCE_FILES})
		IF (JAR_SOURCE_FILES)
			SET(JAR_SOURCE_FILES "${JAR_SOURCE_FILES}" ${SOURCE_FILE})
		ELSE (JAR_SOURCE_FILES)
			SET(JAR_SOURCE_FILES ${SOURCE_FILE})
		ENDIF (JAR_SOURCE_FILES)
	ENDFOREACH(SOURCE_FILE ${SOURCE_FILES})

	# MESSAGE("JAR_SOURCE_FILES = ${JAR_SOURCE_FILES}")

ENDFOREACH(SOURCE_DIR ${SOURCE_DIRS})

# Use colons as separator in SOURCEPATH and CLASSPATH
IF(NOT WIN32)
	STRING(REGEX REPLACE ";" ":" CLASSPATH "${CLASSPATH}")
ENDIF(NOT WIN32)

# Write the source list to a file to avoid command line overflow
SET(SOURCE_LIST_FILE ${CMAKE_BINARY_DIR}/${JAR_FILE}.srclist)
# Use carriage returns instead of semicolons as separators
STRING(REGEX REPLACE ";" "\n" JAR_SOURCE_FILES_LINES "${JAR_SOURCE_FILES}")
FILE(WRITE ${SOURCE_LIST_FILE} ${JAR_SOURCE_FILES_LINES})

ADD_CUSTOM_TARGET(
	${JAR_FILE}-javac ALL 
	${JAVA_COMPILE} 
	-classpath "${CLASSPATH}" 
	-d "${CLASS_OUTPUT_PATH}"
	@"${SOURCE_LIST_FILE}"
	DEPENDS ${SOURCE_LIST_FILE} ${JAR_SOURCE_FILES}
	)

### 2) build jar file with classes and resources

SET(JAR_INPUTS ${RESOURCE_DIRS})
SET(CLASS_JAR_INPUTS ${CLASS_DIRS})
INCLUDE(${CMAKE_SOURCE_DIR}/JavaJar.cmake)

# Compile before jarring
ADD_DEPENDENCIES(${JAR_FILE}-jar ${JAR_FILE}-javac)
