/*
 * Created on May 3, 2005
 *
 */
package org.simtk.geometry3d;

import java.util.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * Container to speed up finding objects that are near one another in 3D space.
  * 
  * Only one object should be placed at each unique position.  If another one is placed
  * at the same location, the one that was there earlier will be gone.
 */
public class Hash3D <E> {
    private double cubeletSize = 1.0;
    private Hashtable<String, Cubelet> cubelets = new Hashtable<String, Cubelet>();

    // private Vector<Vector3D> positions = new Vector<Vector3D>();
    private Hashtable<Vector3D, E > positionObjects = new Hashtable<Vector3D, E >();
    private Hashtable<E, Vector3D> objectPositions = new Hashtable<E, Vector3D>();
    
    public Hash3D(double r) {
        cubeletSize = r;
    }
    
    public void put(Vector3D position, E object) {
        Cubelet cubelet = getCubelet(position);
        cubelet.put(position, object);
        positionObjects.put(position, object);
        objectPositions.put(object, position);
    }
    
    public Collection<Vector3D> positions() {
        return positionObjects.keySet();
    }
    
    public E get(Vector3D position) {
        return positionObjects.get(position);
    }

    /**
     * Return the closest object to a particular position
     * If no object is within the specified radius, return null.
     * @param position
     * @param radius
     * @return
     */
    public E getClosest(Vector3D position, double radius) {
        // TODO - more efficient implementation, starting with central cubelet
        //  follow expanding shells as required
        E answer = null;

        double radiusSquared = radius * radius; // Absolute cutoff distance
        double minDistanceSquared = radiusSquared + 1; // Distance to the closest thing we have found so far
        
        // TODO - sort neighboring cubelets by minimum distance to position
        // TODO - terminate when the closest object found is closer than the next cubelet's minimum distance
        
        // TODO - but, for now, just loop over all objects within the radius
        for (E object : values(position, radius)) {
            double d = position.distanceSquared(objectPositions.get(object));
            if ((d < radiusSquared) && (d < minDistanceSquared)) {
                minDistanceSquared = d;
                answer = object;
            }
        }
        
        return answer;
    }
    
    /**
     * Find all objects within a specified radius of a specified point
     * @param position
     * @param radius
     * @return
     */
    public Collection<E> values(Vector3D position, double radius) {
        Vector<E> neighbors = new Vector<E>();
        double dSquared = radius * radius;
        
        int minX = hashKey(position.getX() - radius);
        int minY = hashKey(position.getY() - radius);
        int minZ = hashKey(position.getZ() - radius);
        int maxX = hashKey(position.getX() + radius);
        int maxY = hashKey(position.getY() + radius);
        int maxZ = hashKey(position.getZ() + radius);
        // Loop over all possible cublets that might contain a neighbor
        for (int x = minX; x <= maxX; x ++)
            for (int y = minY; y <= maxY; y ++)
                for (int z = minZ; z <= maxZ; z ++) {
                    String key = hashKey(x,y,z);
                    if (!cubelets.containsKey(key)) continue; // no such cubelet
                    Cubelet cubelet = cubelets.get(key);
                    for (Vector3D v : cubelet.keySet()) {
                        // Check distance
                        if (position.distanceSquared(v) > dSquared) continue; // too far apart
                        neighbors.add(cubelet.get(v));
                    }
                }

        return neighbors;
    }

    private int hashKey(double d) {
        return (int)Math.floor(d/cubeletSize);
    }
    
    private String hashKey(int x, int y, int z) {
        return "" + x + "#" + y + "#" + z;        
    }
    
    private String hashKey(Vector3D position) {
        int x = hashKey(position.getX());
        int y = hashKey(position.getY());
        int z = hashKey(position.getZ());
        return hashKey(x,y,z);
    }

    private Cubelet getCubelet(Vector3D position) {
        String key = hashKey(position);
        if ( !cubelets.containsKey(key) )
            cubelets.put( key, new Cubelet() );
        return cubelets.get(key);        
    }

    private class Cubelet extends Hashtable<Vector3D, E > {
        static final long serialVersionUID = 1L;
    }
}
