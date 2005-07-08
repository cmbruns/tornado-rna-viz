/*
 * Copyright (c) 2005, Stanford University. All rights reserved. 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions
 * are met: 
 *  - Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer. 
 *  - Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution. 
 *  - Neither the name of the Stanford University nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE. 
 */
 
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
