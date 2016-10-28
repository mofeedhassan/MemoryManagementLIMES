/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.mapper.atomic;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.mapper.AtomicMapper;
import de.uni_leipzig.simba.mapper.SetOperations;

/**
 *
 * @author ngonga
 */
public class SymmetricHausdorffMapper implements AtomicMapper{

    public Mapping getMapping(Cache source, Cache target, String sourceVar, String targetVar, String expression, double threshold) {
        OrchidMapper hm = new OrchidMapper();
        Mapping m1 = hm.getMapping(source, target, sourceVar, targetVar, expression, threshold);
        Mapping m2 = hm.getMapping(target, source, targetVar, sourceVar, expression, threshold);
        m2 = m2.reverseSourceTarget();
        m1 = SetOperations.intersection(m1, m2);
        return m1;
    }

    public String getName() {
        return "Symmetric Hausdorff";
    }

    public double getRuntimeApproximation(int sourceSize, int targetSize, double theta, Language language) {
        return (new OrchidMapper()).getRuntimeApproximation(sourceSize, targetSize, theta, language);
    }

    public double getMappingSizeApproximation(int sourceSize, int targetSize, double theta, Language language) {
        return (new OrchidMapper()).getMappingSizeApproximation(sourceSize, targetSize, theta, language);
    }
    
}
