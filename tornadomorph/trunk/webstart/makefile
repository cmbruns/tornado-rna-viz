default: test

TornadoMorph.jar: ../bin/org/simtk/tornadomorph/*.class ../bin/resources/*
	jar cf TornadoMorph.jar -C ../bin .

s_TornadoMorph.jar: TornadoMorph.jar
	jarsigner -keystore /cygwin/home/cmbruns/private/myKeystore -signedJar s_TornadoMorph.jar TornadoMorph.jar myself

test: s_TornadoMorph.jar
	svn commit -m "latest jar file"
	javaws TornadoMorph.jnlp
