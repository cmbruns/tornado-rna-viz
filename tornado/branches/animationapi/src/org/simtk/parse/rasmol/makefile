# JAVACC_DIR = C:/cygwin/home/cmbruns/projects/javacc/javacc-3.2/bin/lib
JAVACC_DIR = "C:/Program Files/eclipse/plugins/sf.eclipse.javacc_1.5.1"

JAVACC_JAR = $(JAVACC_DIR)/javacc.jar
JAVACC = java -classpath $(JAVACC_JAR) javacc

RasMolParser.java: RasMolParser.jj
	$(JAVACC) $<

.jj.java:
	$(JAVACC) $<

