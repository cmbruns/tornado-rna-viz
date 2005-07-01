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
  * There may be a problem with having the same object at multiple positions.
 */
public class Hash3D <V>
extends Hashtable<BaseVector3D, V>
// implements Map<Vector3D, V>
{
    public static final long serialVersionUID = 2L;
    private double cubeletSize = 1.0;
    private Hashtable<String, Cubelet> cubelets = new Hashtable<String, Cubelet>();
    private Hashtable<BaseVector3D, V > positionObjects = this;
    
    public Hash3D(double r) {
        cubeletSize = r;
    }
    
    @Override
    public void clear() {
        cubelets.clear();
        super.clear();
    }
    
    @Override
    public Object clone() {
        Hash3D<V> answer = new Hash3D<V>(cubeletSize);
        for (BaseVector3D v : positionObjects.keySet())
            answer.put(v, get(v));
        return answer;
    }
    
    @Override
    public V remove(Object key) {
        if (! (key instanceof Vector3D)) return null;

        Vector3D vec = (Vector3D) key;
        V value = get(vec);
        if (value == null) return null;
        
        Cubelet cubelet = getCubelet(vec);
        cubelet.remove(vec);

        return super.remove(key);
    }
    
    @Override
    public V put(BaseVector3D position, V object) {
        V answer = get(position);
        
        Cubelet cubelet = getCubelet(position);
        cubelet.put(position, object);

        return super.put(position, object);
    }
    
//     public Collection<Vector3D> positions() {
//         return positionObjects.keySet();
//     }
    
    /**
     * Return the closest object to a particular position
     * If no object is within the specified radius, return null.
     * @param position
     * @param radius
     * @return
     */
    public V getClosest(BaseVector3D position, double radius) {
        // TODO - more efficient implementation, starting with central cubelet
        //  follow expanding shells as required
        V answer = null;

        double radiusSquared = radius * radius; // Absolute cutoff distance
        double minDistanceSquared = radiusSquared + 1; // Distance to the closest thing we have found so far
        
        // TODO - sort neighboring cubelets by minimum distance to position
        // TODO - terminate when the closest object found is closer than the next cubelet's minimum distance
        
        // TODO - but, for now, just loop over all objects within the radius
        BaseVector3D closestPoint = null;
        for (BaseVector3D v : neighborKeys(position, radius)) {
            double d = position.distanceSquared(v);
            if ((d < radiusSquared) && (d < minDistanceSquared)) {
                minDistanceSquared = d;
                closestPoint = v;
            }
        }
        if (closestPoint != null) answer = get(closestPoint);
        
        return answer;
    }
    
    /**
     * Find all objects within a specified radius of a specified point
     * @param position
     * @param radius
     * @return
     */
    public Collection<V> neighborValues(BaseVector3D position, double radius) {
        Vector<V> neighbors = new Vector<V>();
        for (BaseVector3D v : neighborKeys(position, radius)) {
            V object = get(v);
            if (object != null) neighbors.add(object);
        }
        return neighbors;
    }

    /**
     * Find all objects within a specified radius of a specified point
     * @param position
     * @param radius
     * @return
     */
    public Collection<BaseVector3D> neighborKeys(BaseVector3D position, double radius) {
        Vector<BaseVector3D> neighbors = new Vector<BaseVector3D>();
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
                    for (BaseVector3D v : cubelet.keySet()) {
                        // Check distance
                        if (position.distanceSquared(v) > dSquared) continue; // too far apart
                        neighbors.add(v);
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
    
    private String hashKey(BaseVector3D position) {
        int x = hashKey(position.getX());
        int y = hashKey(position.getY());
        int z = hashKey(position.getZ());
        return hashKey(x,y,z);
    }

    private Cubelet getCubelet(BaseVector3D position) {
        String key = hashKey(position);
        if ( !cubelets.containsKey(key) )
            cubelets.put( key, new Cubelet() );
        return cubelets.get(key);        
    }

    private class Cubelet extends Hashtable<BaseVector3D, V > {
        static final long serialVersionUID = 1L;
    }
}
