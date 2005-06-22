#include "relaxCoordinates.h"
#include "harmonicForces.h"
#include "getDist.h"
#include <math.h>
#include <stdio.h>

int relaxCoordinates(int nAtoms,
                     Coordinate3D *coords,
                     Coordinate3D *coordsRef,
                     float resourceLimit,
                     int nRigidBodies,
                     DuplexRange *rigidBodies,
                     float kChain,
                     float kRigid,
                     int verbose) {
  int i, j, k, n;
  Momentum3D *p;
  Force3D *forces;

  int iBondPair, nBondPairs;
  IndexPair *bondPairs;
  float *bondK;
  float *bondDist0;

  int iRigidPair, nRigidPairs;
  IndexPair *rigidPairs;
  float *rigidK;
  float *rigidDist0;
  int min1, max1, min2, max2;

  float pEnergy;

  float stepSize = 0.010; // time step is 5fs
  float mass = 325.0;
  float pScale = 0.99;

  if (verbose){
    for (i=0; i<nAtoms; i++){
      printf("%3d: %7.3f %7.3f %7.3f\n", i, coords[i][0], coords[i][1], coords[i][2]);
    }
  }

  p = (Momentum3D *)malloc(nAtoms * sizeof(Momentum3D));
  forces = (Force3D *)malloc(nAtoms * sizeof(Force3D));
  for (i=0; i<nAtoms; i++){
    forces[i][0]=0.0;
    forces[i][1]=0.0;
    forces[i][2]=0.0;
    p[i][0]=0.0;
    p[i][1]=0.0;
    p[i][2]=0.0;
  }

// set bond pairs and parameters
  n = 23;
  n = min(n, nAtoms);
  iBondPair=0;
  nBondPairs=n*(nAtoms-n)+(n*n-n)/2;
  bondPairs = (IndexPair *)malloc(nBondPairs * sizeof(IndexPair));
  bondK = (float *)malloc(nBondPairs * sizeof(float));
  bondDist0 = (float *)malloc(nBondPairs * sizeof(float));
  for (i=0; i<nAtoms-1; i++){
    for (j=i+1; j<=min(i+n, nAtoms-1); j++){
      bondPairs[iBondPair][0]=i;
      bondPairs[iBondPair][1]=j;
      bondK[iBondPair] = kChain;
      bondDist0[iBondPair] = getDist(coordsRef[i], coordsRef[j]);
      if (verbose)
        printf("%3d %3d\n", bondPairs[iBondPair][0], bondPairs[iBondPair][1]);
      iBondPair+=1;
    }
  }
  if (verbose)
    printf("iBondPair=%d nBondPairs=%d\n", iBondPair, nBondPairs);


// set rigid body pairs and parameters.  This is ugly...
  nRigidPairs=0;
  for (i=0; i<nRigidBodies; i++){
    n = abs(rigidBodies[i][1]-rigidBodies[i][0])+1;
    n+= abs(rigidBodies[i][3]-rigidBodies[i][2])+1;
    nRigidPairs+=(n*n-n)/2;
  }

  rigidPairs = (IndexPair *)malloc(nRigidPairs * sizeof(IndexPair));
  rigidK = (float *)malloc(nRigidPairs * sizeof(float));
  rigidDist0 = (float *)malloc(nRigidPairs * sizeof(float));

  iRigidPair=0;
  for (i=0; i<nRigidBodies; i++){
    min1 = min(rigidBodies[i][0], rigidBodies[i][1]);
    max1 = max(rigidBodies[i][0], rigidBodies[i][1]);
    min2 = min(rigidBodies[i][2], rigidBodies[i][3]);
    max2 = max(rigidBodies[i][2], rigidBodies[i][3]);

    for (j=min1; j<max1; j++){
      for (k=j+1; k<=max1; k++){
        bondPairs[iRigidPair][0]=j;
        bondPairs[iRigidPair][1]=k;
        bondK[iRigidPair] = kRigid;
        bondDist0[iRigidPair] = getDist(coordsRef[j], coordsRef[k]);
        iRigidPair+=1;
      }
      for (k=min2; k<=max2; k++){
        bondPairs[iRigidPair][0]=j;
        bondPairs[iRigidPair][1]=k;
        bondK[iRigidPair] = kRigid;
        bondDist0[iRigidPair] = getDist(coordsRef[j], coordsRef[k]);
        iRigidPair+=1;
      }
    }
    for (j=min2; j<max2; j++){
      for (k=j+1; k<=max2; k++){
        bondPairs[iRigidPair][0]=j;
        bondPairs[iRigidPair][1]=k;
        bondK[iRigidPair] = kRigid;
        bondDist0[iRigidPair] = getDist(coordsRef[j], coordsRef[k]);
        iRigidPair+=1;
      }
    }

    if (verbose)
      printf("iRigidPair=%d nRigidPairs=%d\n", iRigidPair, nRigidPairs);
  }
  for (i=0; i<resourceLimit; i++){
// First cacl forces and energy
    for (j=0; j<nAtoms; j++){
      forces[j][0]=0.0;
      forces[j][1]=0.0;
      forces[j][2]=0.0;
    }
    pEnergy = 0.0;
    pEnergy += harmonicForces(coords, forces,
                              nBondPairs, bondPairs,
                              bondK, bondDist0, verbose);
    pEnergy += harmonicForces(coords, forces,
                              nRigidPairs, rigidPairs,
                              rigidK, rigidDist0, verbose);

// Do integration, and yes Sherm, it's crappy...
    for (j=0; j<nAtoms; j++){
      p[j][0]+=stepSize*forces[j][0];
      p[j][1]+=stepSize*forces[j][1];
      p[j][2]+=stepSize*forces[j][2];
      coords[j][0]+=stepSize*p[j][0]/mass;
      coords[j][1]+=stepSize*p[j][1]/mass;
      coords[j][2]+=stepSize*p[j][2]/mass;
    }      

// Remove Energy
    for (j=0; j<nAtoms; j++){
      p[j][0]*=pScale;
      p[j][1]*=pScale;
      p[j][2]*=pScale;
    }
  }

  if (verbose){
    for (i=0; i<nAtoms; i++){
      printf("%3d: %7.3f %7.3f %7.3f\n", i, coords[i][0], coords[i][1], coords[i][2]);
    }
  }
  return 0;
}
