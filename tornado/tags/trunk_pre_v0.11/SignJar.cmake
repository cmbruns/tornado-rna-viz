# INPUT - set the following variables before the INCLUDE(SignJar.cmake) command
# 1) JAR_FILE_PATH = the location of the jar file that is to be signed
# 2) JARSIGNER_KEYSTORE FILEPATH = Name of keystore file containing your private key for jar signing
# 3) JARSIGNER_USER_NAME = User name for key in keystore file
# 4) JARSIGNER_PASSWORD = Password for key in keystore file
#
# OUTPUT
# 1) SIGNED_JAR_PATH = the location of the signed jar file

GET_FILENAME_COMPONENT(JAR_FILE_NAME ${JAR_FILE_PATH} NAME)

# Use "s_" prefix to show that the jar file is signed
SET(SIGNED_JAR_PATH ${CMAKE_BINARY_DIR}/s_${JAR_FILE_NAME})
	
ADD_CUSTOM_COMMAND(
	OUTPUT ${SIGNED_JAR_PATH}
	COMMAND ${JAVA_SIGNER} -keystore ${JARSIGNER_KEYSTORE} -storepass ${JARSIGNER_PASSWORD} -signedJar ${SIGNED_JAR_PATH} ${JAR_FILE_PATH} ${JARSIGNER_USER_NAME}
	DEPENDS ${JAR_FILE_PATH}
	)
	
ADD_CUSTOM_TARGET(
	${JAR_FILE_NAME}-jarsigner ALL
	DEPENDS ${JAR_FILE_PATH} ${SIGNED_JAR_PATH}
	)
