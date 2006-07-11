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
ENDIF(NOT WIN32)

#write the source list to a file to avoid command line overflow
SET(SOURCE_LIST_FILE ${CMAKE_BINARY_DIR}/${JAR_FILE_NAME}.srclist)
STRING(REGEX REPLACE ";" "\n" SOURCE_FILES_INTERNAL "${SOURCE_FILES}")
FILE(WRITE ${SOURCE_LIST_FILE} ${SOURCE_FILES_INTERNAL})

ADD_CUSTOM_TARGET(${JAR_FILE_NAME}-javac javac -classpath "${CLASSPATH_INTERNAL}" -d ${CLASS_OUTPUT_PATH} @${SOURCE_LIST_FILE})
ADD_CUSTOM_TARGET(${JAR_FILE_NAME}-jar ALL jar cf ${JAR_OUTPUT_PATH}/${JAR_FILE_NAME} -C ${CLASS_OUTPUT_PATH} .)
ADD_DEPENDENCIES(${JAR_FILE_NAME}-jar ${JAR_FILE_NAME}-javac)
