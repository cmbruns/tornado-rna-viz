IMAGES = -C .. resources

default: Tornado.jnlp
	javaws Tornado.jnlp

Tornado.jar: never_up_to_date
	jar cf Tornado.jar -C .. resources -C ../bin org/simtk -C ../bin Jama -C ../bin edu
	jarsigner -keystore myKeystore Tornado.jar myself

never_up_to_date:
	
HelloVtk.jar: never_up_to_date
	cp ../bin/HelloVtk*.class .
	jar cf HelloVtk.jar HelloVtk*.class
	jarsigner -keystore myKeystore HelloVtk.jar myself

