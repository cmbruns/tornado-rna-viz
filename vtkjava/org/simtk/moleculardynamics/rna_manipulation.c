#include "rna_manipulation.h"
#include <stdio.h>

int main() {
    int nAtoms = 50;
    Coordinate3D * atoms;

    atoms = (Coordinate3D *) malloc(nAtoms * sizeof(Coordinate3D));
    relaxCoordinates(nAtoms, atoms, 5.0, 0, NULL);
    printf("Hello\n");
}

Coordinate3D * relaxCoordinates(
	int nAtoms, Coordinate3D * atoms,
    float resourceLimit,
    int nRigidBodies, DuplexRange * rigidBodies) 
{
  return atoms;
}


