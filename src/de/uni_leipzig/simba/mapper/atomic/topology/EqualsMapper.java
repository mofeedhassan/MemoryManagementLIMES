/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.mapper.atomic.topology;

import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.mapper.atomic.hausdorff.Polygon;
import java.util.*;

/**
 * Mapper that checks for the topological relation equals.
 * @author psmeros
 */
public class EqualsMapper implements TopologicRelationMapper {

    float theta = 10;

    /**
     *
     * @param sourceData Set of Polygons
     * @param targetData Set of Polygons
     * @return Mapping
     */
    @Override
    public Mapping getMapping(Set<Polygon> sourceData, Set<Polygon> targetData) {
        TopologicalRelationUtils.theta = this.theta;
        return TopologicalRelationUtils.getMapping(sourceData, targetData, TopologicalRelationUtils.EQUALS);
    }
}
