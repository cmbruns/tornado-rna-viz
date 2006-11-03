# Location of private key - I insert a CD into my laptop for signing
PROD_KEYSTORE = D:/simtkjava_signing/simtkjava.store

test: s_Tornado.jar s_Jama.jar
	svn commit -m "Latest web start files"
	javaws tornado_test.jnlp

Tornado.jar: ../bin/images/* ../bin/org/simtk/moleculargraphics/*.class
	jar cf Tornado.jar -C ../bin images -C ../bin org/simtk

# Implicit rule to sign jar files for production
s_%.jar: %.jar
	jarsigner -keystore $(PROD_KEYSTORE) -signedJar $@ $? simtkjava

