SRC_DIR = ..
SRCS = $(SRC_DIR)/org/simtk/*/*.java $(SRC_DIR)/org/simtk/*/*/*.java $(SRC_DIR)/Jama/*.java $(SRC_DIR)/Jama/*/*.java
BUILD_DIR = build
MANIFEST_FILE = simtkjava_manifest.tmp
JAR_FILE = SimtkSharedJava.jar

default: test

### Create initial jar file using compiled classes, manifest, and resources
$(JAR_FILE): all_classes $(MANIFEST_FILE) $(SRC_DIR)/resources/*/*
	jar cf $(JAR_FILE) -m $(MANIFEST_FILE) -C $(BUILD_DIR) . -C $(SRC_DIR) resources

# deprecate this rule
# SimtkSharedJava.jar: ../../bin/org/simtk/*/*.class ../../bin/resources/*
# 	jar cf SimtkSharedJava.jar -C ../../bin .

s_$(JAR_FILE): $(JAR_FILE)
	jarsigner -keystore /cygwin/home/cmbruns/private/myKeystore -signedJar s_$(JAR_FILE) $(JAR_FILE) myself

test: s_$(JAR_FILE)
	svn commit -m "latest jar file"
	javaws SimtkSharedJava.jnlp

all_classes: $(SRCS)
	mkdir -p $(BUILD_DIR)
	javac -deprecation -d $(BUILD_DIR) -classpath "." -sourcepath "$(ISIM_SRC_DIR)" $(SRCS)

### In this case, no special content is need in the manifest file
$(MANIFEST_FILE):
	touch $(MANIFEST_FILE)

clean:
	rm -rf $(BUILD_DIR)
	rm $(JAR_FILE)

