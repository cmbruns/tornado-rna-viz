#include "org_simtk_moleculardynamics_RelaxCoordinates.h"
#include "relaxCoordinates.h"
#include <assert.h>

JNIEXPORT jint JNICALL Java_org_simtk_moleculardynamics_RelaxCoordinates_relaxCoordinates1
  (JNIEnv *env, 
   jclass obj, 
   jint jnAtoms, 
   jfloatArray jcoords, 
   jfloatArray jcoordsRef, 
   jfloat jresourceLimit, 
   jint jnRigidBodies, 
   jintArray jrigidBodies, 
   jfloat jkChain, 
   jfloat jkRigid, 
   jint jverbose)
{
    jfloat * coordsRef = (*env)->GetFloatArrayElements(env, jcoordsRef, 0);
    jfloat * coords    = (*env)->GetFloatArrayElements(env, jcoords, 0);    
    jint * rigidBodies = (*env)->GetIntArrayElements(env, jrigidBodies, 0);

	assert(sizeof(jfloat) == sizeof(float));
	assert(sizeof(jint) == sizeof(int));
	assert(sizeof(Coordinate3D) == FLOATS_PER_COORDINATE * sizeof(float));
	assert(sizeof(DuplexRange) == INTS_PER_DUPLEX_RANGE * sizeof(int));

	assert(FLOATS_PER_COORDINATE * jnAtoms == (*env)->GetArrayLength(env, jcoordsRef));
	assert(FLOATS_PER_COORDINATE * jnAtoms == (*env)->GetArrayLength(env, jcoords));
	assert(INTS_PER_DUPLEX_RANGE * jnRigidBodies == (*env)->GetArrayLength(env, jrigidBodies));	
	
	Coordinate3D * usecoordsRef = (Coordinate3D *) coordsRef;
	Coordinate3D * usecoords = (Coordinate3D *) coords;
	DuplexRange * userigidBodies = (DuplexRange *) rigidBodies;
	
    relaxCoordinates(jnAtoms,
                     usecoords,
                     usecoordsRef,
                     jresourceLimit,
                     jnRigidBodies,
                     userigidBodies,
                     jkChain,
                     jkRigid,
                     jverbose);

	/* Release memory back to Java */
     (*env)->ReleaseFloatArrayElements(env, jcoords, coords, 0);
     (*env)->ReleaseFloatArrayElements(env, jcoordsRef, coordsRef, 0);
     (*env)->ReleaseIntArrayElements(env, jrigidBodies, rigidBodies, 0);
}
