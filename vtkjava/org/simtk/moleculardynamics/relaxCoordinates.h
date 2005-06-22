#ifndef __RELAX_COORDINATES_H__
#define __RELAX_COORDINATES_H__

#define FLOATS_PER_COORDINATE 3
#define INTS_PER_DUPLEX_RANGE 4

typedef float Coordinate3D[FLOATS_PER_COORDINATE];
typedef float Momentum3D[3];
typedef float Force3D[3];
typedef int DuplexRange[INTS_PER_DUPLEX_RANGE];
typedef int IndexPair[2];

#define min(x,y) ((x) < (y) ? (x) : (y))
#define max(x,y) ((x) > (y) ? (x) : (y))

int relaxCoordinates(int nAtoms,
                     Coordinate3D *coords,
                     Coordinate3D *coordsRef,
                     float resourceLimit,
                     int nRigidBodies,
                     DuplexRange *rigidBodies,
                     float kChain,
                     float kRigid,
                     int verbose);

#endif
