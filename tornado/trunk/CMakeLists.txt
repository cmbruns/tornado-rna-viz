PROJECT(tornado Java)

SET(JAR_OUTPUT_PATH ${CMAKE_BINARY_DIR}/jar CACHE PATH "jar dir")
SET(CLASS_OUTPUT_PATH ${CMAKE_BINARY_DIR}/classes CACHE PATH "class dir")

SET(
  CLASSPATH
  ${CMAKE_SOURCE_DIR}/jnlp.jar
  ${CMAKE_SOURCE_DIR}/webstart/jogl.jar
  ${CMAKE_SOURCE_DIR}/webstart/vtk44.jar
  ${CMAKE_SOURCE_DIR}/webstart/s_BrowserLauncher.jar
  ${CMAKE_SOURCE_DIR}/src
)

FILE(GLOB_RECURSE SOURCE_FILES src/org/*.java)
SET(JAR_FILE_NAME tornado.jar)
INCLUDE(${CMAKE_SOURCE_DIR}/JavaBuild.cmake)

