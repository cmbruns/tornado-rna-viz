# JavaBuild.cmake
#
# The following variables must be defined before the INCLUDES()
# declaration in your CMakeLists.txt
#
# SOURCE_FILES: the Java source files to compile
# CLASSPATH: the classpath to use when compiling
# CLASS_OUTPUT_PATH: where to put the compiled classes
# JAR_OUTPUT_PATH: where to put the generated jar file
# JAR_FILE_NAME: the name of the jar file to create
#

FILE(MAKE_DIRECTORY ${CLASS_OUTPUT_PATH})
FILE(MAKE_DIRECTORY ${JAR_OUTPUT_PATH})

IF(NOT WIN32)
  STRING(REGEX REPLACE ";" ":" CLASSPATH_INTERNAL "${CLASSPATH}")
ELSE(NOT WIN32)
  SET(CLASSPATH_INTERNAL ${CLASSPATH})
ENDIF(NOT WIN32)

#write the source list to a file to avoid command line overflow
SET(SOURCE_LIST_FILE ${CMAKE_BINARY_DIR}/${JAR_FILE_NAME}.srclist)
STRING(REGEX REPLACE ";" "\n" SOURCE_FILES_INTERNAL "${SOURCE_FILES}")
FILE(WRITE ${SOURCE_LIST_FILE} ${SOURCE_FILES_INTERNAL})

#Write the resources list to a file so I can remove .svn items
SET(RESOURCE_LIST_FILE ${CMAKE_BINARY_DIR}/${JAR_FILE_NAME}.resourcelist)
STRING(REGEX REPLACE ";" "\n-C ${JAR_RESOURCE_PATH} " RESOURCE_FILES "${RESOURCE_FILES}")
FILE(WRITE ${RESOURCE_LIST_FILE} ${RESOURCE_FILES})

ADD_CUSTOM_TARGET(${JAR_FILE_NAME}-javac ALL ${JAVA_TOOLS_PATH}/javac -classpath "${CLASSPATH_INTERNAL}" -d ${CLASS_OUTPUT_PATH} @${SOURCE_LIST_FILE})

ADD_CUSTOM_TARGET(${JAR_FILE_NAME}-jar ALL ${JAVA_TOOLS_PATH}/jar -cf ${JAR_OUTPUT_PATH}/${JAR_FILE_NAME} -C ${CLASS_OUTPUT_PATH} . @${RESOURCE_LIST_FILE})

ADD_CUSTOM_TARGET(${JAR_FILE_NAME}-jarsigner ALL ${JAVA_TOOLS_PATH}/jarsigner -keystore ${KEYSTORE_FILE_NAME} -storepass ${KEYSTORE_PASSWORD} -signedJar ${JAR_OUTPUT_PATH}/s_${JAR_FILE_NAME} ${JAR_OUTPUT_PATH}/${JAR_FILE_NAME} ${KEYSTORE_USERNAME})

ADD_DEPENDENCIES(${JAR_FILE_NAME}-jar ${JAR_FILE_NAME}-javac)
ADD_DEPENDENCIES(${JAR_FILE_NAME}-jarsigner ${JAR_FILE_NAME}-jar)

