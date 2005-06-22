#include "rna_manipulation.h"
#include <math.h>

float getDist(Coordinate3D coord1, Coordinate3D coord2) {
  float dx, dy, dz;
  dx = coord2[0]-coord1[0];
  dy = coord2[1]-coord1[1];
  dz = coord2[2]-coord1[2];
  return sqrt( dx*dx + dy*dy + dz*dz );
}
