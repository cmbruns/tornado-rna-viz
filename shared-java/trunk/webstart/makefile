SRC_DIR = ..
SRCS = $(SRC_DIR)/org/simtk/*/*.java
BUILD_DIR = build

default: test

SimtkSharedJava.jar: ../../bin/org/simtk/*/*.class ../../bin/resources/*
	jar cf SimtkSharedJava.jar -C ../../bin .

s_SimtkSharedJava.jar: SimtkSharedJava.jar
	jarsigner -keystore /cygwin/home/cmbruns/private/myKeystore -signedJar s_SimtkSharedJava.jar SimtkSharedJava.jar myself

test: s_SimtkSharedJava.jar
	svn commit -m "latest jar file"
	javaws SimtkSharedJava.jnlp
