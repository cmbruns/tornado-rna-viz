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
 
 /**
***  Calc Bond forces and energy
**/

#include "rna_manipulation.h"
#include <math.h>
#include <stdio.h>

float harmonicForces(Coordinate3D *coords, Force3D *forces,
                     int nPairs, IndexPair *atomPairs,
                     float *k, float *dr0,
                     int verbose) {
  int i;
  int iAtomA, iAtomB;
  float dx, dy, dz;
  float dr, R, f;
  float ePTotal=0.0;
  float *refDist;

  for (i=0; i<nPairs; i++){
    iAtomA = atomPairs[i][0];
    iAtomB = atomPairs[i][1];
    dx = coords[iAtomB][0]-coords[iAtomA][0];
    dy = coords[iAtomB][1]-coords[iAtomA][1];
    dz = coords[iAtomB][2]-coords[iAtomA][2];
    dr = sqrt( dx*dx + dy*dy + dz*dz );
    R = dr-dr0[i];
    ePTotal += 0.5*k[i]*R*R;

//    printf("%3d %3d: %20.3f\n", iAtomA, iAtomB, R);

    f = -k[i]*R/dr;
    forces[iAtomA][0] -= f*dx;
    forces[iAtomB][0] += f*dx;
    forces[iAtomA][1] -= f*dy;
    forces[iAtomB][1] += f*dy;
    forces[iAtomA][2] -= f*dz;
    forces[iAtomB][2] += f*dz;
  }

//  for (i=0; i<nPairs; i++){
//    iAtomA = atomPairs[i][0];
//    iAtomB = atomPairs[i][1];
//    printf("%20.3f %20.3f   %20.3f %20.3f\n",
//           coords[iAtomA][0], forces[iAtomA][0], coords[iAtomB][0], forces[iAtomB][0]);
//  }
    if (verbose){
      printf("%20.3f %20.3f   %20.3f %20.3f\n",
             coords[0][0], forces[0][0], coords[1][0], forces[1][0]);
    }

  return ePTotal;
}
