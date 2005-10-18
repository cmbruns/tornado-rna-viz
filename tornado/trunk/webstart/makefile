default: Tornado.jnlp
	javaws Tornado.jnlp

Tornado.jar: never_up_to_date
	jar cf Tornado.jar -C ../bin resources -C ../bin org/simtk -C ../bin Jama

s_Tornado.jar: Tornado.jar
	jarsigner -keystore /cygwin/home/cmbruns/private/myKeystore -signedJar s_Tornado.jar Tornado.jar myself

never_up_to_date:
	
HelloVtk.jar: never_up_to_date
	cp ../bin/HelloVtk*.class .
	jar cf HelloVtk.jar HelloVtk*.class
	jarsigner -keystore myKeystore HelloVtk.jar myself

