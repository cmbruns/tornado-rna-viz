# Requires:
#
# These are specific to this directory
# 1) ${JAR_FILE} - local name of jar file to create, minus "s_" prefix when signed
# 2) ${JAR_INPUTS} - names of root directories and files to pack into jars - subversion files will be automatically removed
# 3) ${CLASS_JAR_INPUTS} - directories to send unmodified into the jar file
#
# These should be set in a top level CMakeLists.text
# 3) ${DO_SIGN_JARS} - whether to sign, and if so:
#   3.1) ${JARSIGNER_KEYSTORE} filename of keystore
#   3.2) ${JARSIGNER_USER_NAME} 
#   3.3) ${JARSIGNER_PASSWORD}
#

# Create a location for newly created jar files
SET(JAR_DIR ${CMAKE_BINARY_DIR}/jars)
FILE(MAKE_DIRECTORY ${JAR_DIR})

SET(JAR_FILE_PATH "${JAR_DIR}/${JAR_FILE}")

# Enumerate all files for jar archive in JAR_INPUT_FILES_INTERNAL variable
# Assume they are all directories for now... TODO - support files too
SET(JAR_INPUT_FILES "")
FOREACH(JAR_INPUT ${JAR_INPUTS})
    # Divide into parent directory and name
	GET_FILENAME_COMPONENT(JAR_INPUT_DIR ${JAR_INPUT} PATH)
	FILE(RELATIVE_PATH JAR_INPUT_NAME ${JAR_INPUT_DIR} ${JAR_INPUT})
	
	# MESSAGE("JAR_INPUT = ${JAR_INPUT}")
	# MESSAGE("JAR_INPUT_DIR = ${JAR_INPUT_DIR}")
	# MESSAGE("JAR_INPUT_NAME = ${JAR_INPUT_NAME}")

	# Enumerate all files in the subdirectory
	FILE(GLOB_RECURSE JAR_INPUT_FILES_INTERNAL RELATIVE ${JAR_INPUT_DIR} ${JAR_INPUT}/*)

	# MESSAGE("JAR_INPUT_FILES = ${JAR_INPUT_FILES_INTERNAL}")

	# Do not use contents of .svn directories from the resource areas
	# Remove all stretches containing ".svn" without semicolons
	STRING(REGEX REPLACE "[^;]*\\.svn[^;]*" "" JAR_INPUT_FILES_INTERNAL "${JAR_INPUT_FILES_INTERNAL}")
	# Remove artifactual multiple semicolons
	STRING(REGEX REPLACE ";+" ";" JAR_INPUT_FILES_INTERNAL "${JAR_INPUT_FILES_INTERNAL}")
	# Remove initial semicolon, if any
	STRING(REGEX REPLACE "^;" "" JAR_INPUT_FILES_INTERNAL "${JAR_INPUT_FILES_INTERNAL}")
	# Remove final semicolon, if any
	STRING(REGEX REPLACE ";\$" "" JAR_INPUT_FILES_INTERNAL "${JAR_INPUT_FILES_INTERNAL}")

	# Include "-C dir" directive for each file
	#   Put one after each semicolon
	STRING(REGEX REPLACE ";" ";-C ${JAR_INPUT_DIR};" JAR_INPUT_FILES_INTERNAL "${JAR_INPUT_FILES_INTERNAL}")	
	#   And one at the very beginning
	IF(JAR_INPUT_FILES_INTERNAL)
		SET(JAR_INPUT_FILES_INTERNAL "-C ${JAR_INPUT_DIR}" ${JAR_INPUT_FILES_INTERNAL})
	ENDIF(JAR_INPUT_FILES_INTERNAL)
	
	# Write filtered files to monolithic file list
	SET(JAR_INPUT_FILES ${JAR_INPUT_FILES} ${JAR_INPUT_FILES_INTERNAL})
ENDFOREACH(JAR_INPUT ${JAR_INPUTS})

# Jar up EVERYTHING in the class directories
FOREACH(JAR_INPUT ${CLASS_JAR_INPUTS})
		SET(JAR_INPUT_FILES ${JAR_INPUT_FILES} "-C ${JAR_INPUT}" ".")
ENDFOREACH(JAR_INPUT ${CLASS_JAR_INPUTS})

# Write the source list to a file to avoid command line overflow
SET(JAR_CONTENT_FILE ${CMAKE_BINARY_DIR}/${JAR_FILE}.contentlist)
STRING(REGEX REPLACE ";" "\n" JAR_INPUT_FILES_LINES "${JAR_INPUT_FILES}")
FILE(WRITE ${JAR_CONTENT_FILE} ${JAR_INPUT_FILES_LINES})

# Create the jar command
ADD_CUSTOM_COMMAND(
	OUTPUT ${JAR_FILE_PATH}
	COMMAND ${JAVA_ARCHIVE} -cf ${JAR_FILE_PATH} @${JAR_CONTENT_FILE}
	DEPENDS ${JAR_CONTENT_FILE}
	)
ADD_CUSTOM_TARGET(
	${JAR_FILE}-jar ALL
	DEPENDS ${JAR_CONTENT_FILE} ${JAR_FILE_PATH}
	)

IF(DO_SIGN_JARS)
	INCLUDE(${CMAKE_SOURCE_DIR}/SignJar.cmake)
	
	# Install the signed jar file
	INSTALL_FILES(/bin
		FILES
		"${SIGNED_JAR_PATH}"
		)

ELSE(DO_SIGN_JARS)

	# Only install the regular unsignedjar file if there is no signed jar file
	INSTALL_FILES(/bin
		FILES
		"${JAR_FILE_PATH}"
		)

ENDIF(DO_SIGN_JARS)
	