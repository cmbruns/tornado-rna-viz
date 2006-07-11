CLASSPATH = ../../../bin/
SOURCEPATH = ../../../
CLASS = org.simtk.moleculardynamics.RelaxCoordinates
CLASSFILE = $(CLASSPATH)org/simtk/moleculardynamics/RelaxCoordinates.class
JAVAFILE = $(SOURCEPATH)org/simtk/moleculardynamics/RelaxCoordinates.java
JNIROOT = org_simtk_moleculardynamics_RelaxCoordinates

INCLUDE = -I/cygdrive/c/ProgramFiles/Java/jdk1.5.0_02/include -I/cygdrive/c/ProgramFiles/Java/jdk1.5.0_02/include/win32
CFLAGS = -mno-cygwin -D_REENTRANT -D_GNU_SOURCE -D__int64="long long"
OBJS = $(JNIROOT).o getDist.o  harmonicForces.o  relaxCoordinates.o
DLLFILE = relaxCoordinates.dll
LIBDIR = /home/cmbruns/lib/

default: install_dll

$(JNIROOT).h: $(CLASSFILE)
	javah -jni -classpath $(CLASSPATH) $(CLASS)

$(CLASSFILE): $(JAVAFILE)
	javac -sourcepath $(SOURCEPATH) -classpath $(CLASSPATH) -d $(CLASSPATH) $(JAVAFILE)

$(JNIROOT).o: $(JNIROOT).h $(JNIROOT).c
	gcc $(CFLAGS) $(INCLUDE) -c $(JNIROOT).c

$(DLLFILE): $(OBJS)
	gcc -shared -o $(DLLFILE) -mno-cygwin -Wl,--add-stdcall-alias $(OBJS)

install_dll: $(DLLFILE)
	cp -f $(DLLFILE) $(LIBDIR)

