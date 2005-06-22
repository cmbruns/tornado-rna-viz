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
