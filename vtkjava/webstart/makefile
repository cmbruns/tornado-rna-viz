IMAGES = -C .. cylinder_icon.png \
         -C .. rbglasses.png \
         -C .. tornado_icon.gif \
         -C .. nuc_fill_icon.png \
         -C .. simtk2.png \
         -C .. tornado_icon.png \
         -C .. po4_stick_icon.png \
         -C .. thinker_icon.png

default: Tornado.jnlp
	javaws Tornado.jnlp

Tornado.jar: never_up_to_date
	jar cf Tornado.jar $(IMAGES) -C ../bin org/simtk
	jarsigner -keystore myKeystore Tornado.jar myself

never_up_to_date:
	
HelloVtk.jar: never_up_to_date
	cp ../bin/HelloVtk*.class .
	jar cf HelloVtk.jar HelloVtk*.class
	jarsigner -keystore myKeystore HelloVtk.jar myself

