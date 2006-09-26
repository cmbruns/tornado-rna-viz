/*
 * Copyright (c) 2005, Christopher M. Bruns and Stanford University. 
 * All rights reserved. 
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

/*
 * Created on May 3, 2005
 *
 * I have previously implemented this algorithm/data structure in perl, 
 * C++, and Java.  This is one case where the Java implementation is by 
 * far the prettiest of the three.
 *
 * -CMB
 */

package org.simtk.geometry3d;

import java.util.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * Container class to speed up finding objects that are near one another in 
  * 3D Cartesian space.
  * 
  * This data structure and algorithm can be used to answer the question "Find 
  * all pairs of objects withing 50 meters of one another" in linear time and 
  * space complexity, O(n), where n is the number of objects.  The naive method
  * takes O(n^2) time.  Linear time complexity can be achieved under the following
  * circumstances:
  *
  * 1) There is an upper bound on the density of objects in the volume.  
  * (Collections of objects with arbitrarily high densities may tend to have 
  * O(n^2) asymptotic *size* complexity for the *answer* to the question "How 
  * many pairs with distance less than X, and thus cannot have linear time 
  * complexity for producing that answer.)
  * 
  * 2) Each object is located at a single distinct point in space.  This works well
  * for atoms and other spherically symmetric objects in Cartesian space.
  * 
  * This implementation does NOT require that the objects in the volume
  * satisfy any minimimum density criterion, unlike some neighbor list
  * methods in molecular simulation.
  * 
  * Only one object should be placed at each unique position.  If another one is 
  * placed at the same location, the one that was there earlier will be gone.
  * There may also be a problem with having the same object at multiple positions.
  * TODO - modify data structure to remove these restrictions.
  * 
  * Set the size parameter in the constructor a bit lower than the
  * distances you intend to query.
  * 
  * A size parameter, s, much larger than the test distance will tend to slow
  * the method by a factor of O(s^3).
  * 
  * A size parameter, s, much smaller than the test distance will tend to slow
  * the method by a factor of O((1/s)^3 * ln(1/s)).  This effect will kick in
  * more slowly than the "s too big" effect.
 */
public class Hash3D<V> extends HashMap<Vector3D, V>
// implements Map<Vector3D, V> // Java 1.5 only...
{
    // The base class hashtable maps exact positions to the object at that position
    // Use of this map is one source of the one-position:one-object restriction

    private double cubeletSize; // Fundamental distance of this data structure

    // Store little cubes at canonical positions
    // The possible positions of these cubes are independent of the data.
    // Deciding which cubes are instantiated does depend upon the data.
    private Hashtable cubelets = new Hashtable();


    // Constructor
    public Hash3D(double r) {
        cubeletSize = r;
    }
    
    /**
     * Inserts an object into the data structure at a particular position.
     * This method is the primary means of populating the data structure.
     * 
     * @param position 
     * @param object
     * @return
     */
    public V put(Vector3D position, V object) {
        // Place the object into its parent cubelet
        Cubelet cubelet = getCubelet(position);
        cubelet.put(position, object);

        return super.put(position, object);
    }
    
    /**
     * Remove the object at a particular position
     * 
     * @param key the position of the object to remove
     */
    public Object remove(Vector3D vec) {
        Object value = get(vec);
        if (value == null) return null;
        
        Cubelet cubelet = getCubelet(vec);
        cubelet.remove(vec);

        return super.remove(vec);
    }
    
    // Empty the entire data structure
    public void clear() {
        cubelets.clear();
        super.clear();
    }
    
    // Duplicate the data structure
    // (deep copy of container and positions)
    // (shallow copy of contained objects)
    public Object clone() {
        Hash3D answer = new Hash3D(cubeletSize);
        Iterator i = keySet().iterator();
        while (i.hasNext()) {
            Vector3D v = (Vector3D) i.next();
            answer.put(v, get(v));
        }
        return answer;
    }
    
    /**
     * Return the closest object to a particular position
     * If no object is within the specified radius, return null.
     * This method has constant asymptotic time complexity.
     * 
     * @param position
     * @param radius
     * @return
     */
    public V getClosest(Vector3D position, double radius) {
        V answer = null;

        // Use squared distances to minimize expensive flops (i.e. sqrt())
        // The code complexity:optimization tradeoff is not bad for this optimization.
        double radiusSquared = radius * radius; // Absolute cutoff distance
        double minDistanceSquared = radiusSquared + 1; // Distance to the closest thing we have found so far
        
        // TODO - sort neighboring cubelets by minimum distance to position
        // (and terminate when the closest object found is closer than the next 
        // cubelet's minimum distance)
        // But for now, just loop over all objects within the radius
        // This is all that is needed to preserve linear time complexity.
        // Those other TODO above are just light (and possibly premature) 
        // optimization

        Vector3D closestPoint = null;
        Iterator i = neighborKeys(position, radius).iterator();
        while (i.hasNext()) {
            Vector3D v = (Vector3D) i.next();
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
     * This method has constant asymptotic time complexity.
     * 
     * @param position
     * @param radius
     * @return
     */
    public Collection<V> neighborValues(Vector3D position, double radius) {
        Vector neighbors = new Vector();
        Iterator i = neighborKeys(position, radius).iterator();
        while (i.hasNext()) {
            Vector3D v = (Vector3D) i.next();
            Object object = get(v);
            if (object != null) neighbors.add(object);
        }
        return neighbors;
    }

    /**
     * Find positions of all objects within a specified radius of a specified point.
     * This method has constant asymptotic time complexity.
     * 
     * @param position
     * @param radius
     * @return
     */
    public Collection neighborKeys(Vector3D position, double radius) {
        Vector neighbors = new Vector();
        double dSquared = radius * radius;
        
        // Figure out the set of nearby cubelets that we might need to search
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
                    Cubelet cubelet = (Cubelet) cubelets.get(key);
                    for (Iterator i = cubelet.keySet().iterator(); i.hasNext(); ) {
                        Vector3D v = (Vector3D) i.next();
                        // Check distance
                        if (position.distanceSquared(v) > dSquared) continue; // too far apart
                        neighbors.add(v);
                    }
                }

        return neighbors;
    }

    // Generate the integer index for one dimension of a cubelet containing a
    // particular position along that dimension.
    private int hashKey(double d) {
        // Note: be careful about what happens as you cross the origin.
        // Being less efficient there might be OK,
        // But doing the wrong thing is not OK.
        return (int)Math.floor(d/cubeletSize);
    }

    // Generate a hash key for a cubelet with the particular integer indices.
    // Basically convert a triple of integers to a unique string.
    private String hashKey(int x, int y, int z) {
        return "" + x + "#" + y + "#" + z;
    }
    
    // Generate a hash key containing the integer indices of a cubelet that
    // contains the given point
    private String hashKey(Vector3D position) {
        int x = hashKey(position.getX());
        int y = hashKey(position.getY());
        int z = hashKey(position.getZ());
        return hashKey(x,y,z);
    }

    // If there is not yet a cubelet containing the specified position,
    // this routine will create one.
    private Cubelet getCubelet(Vector3D position) {
        String key = hashKey(position);
        if ( !cubelets.containsKey(key) )
            cubelets.put( key, new Cubelet() );
        return (Cubelet) cubelets.get(key);        
    }

    private class Cubelet extends Hashtable {
        static final long serialVersionUID = 1L; // serialization tag
    }

    public static final long serialVersionUID = 3L; // serialization tag
}
