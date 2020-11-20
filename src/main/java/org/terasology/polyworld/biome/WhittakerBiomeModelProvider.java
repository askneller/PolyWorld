/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.polyworld.biome;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.polyworld.elevation.ElevationModel;
import org.terasology.polyworld.elevation.ElevationModelFacet;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphFacet;
import org.terasology.polyworld.moisture.MoistureModel;
import org.terasology.polyworld.moisture.MoistureModelFacet;
import org.terasology.polyworld.water.WaterModel;
import org.terasology.polyworld.water.WaterModelFacet;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * TODO Type description
 */
@Produces(WhittakerBiomeModelFacet.class)
@Requires({
    @Facet(GraphFacet.class),
    @Facet(ElevationModelFacet.class),
    @Facet(WaterModelFacet.class),
    @Facet(MoistureModelFacet.class)
    })
public class WhittakerBiomeModelProvider implements FacetProvider {

    private static final Logger logger = LoggerFactory.getLogger(WhittakerBiomeModelProvider.class);

    private final Map<Graph, BiomeModel> modelCache;

    /**
     * @param maxCacheSize maximum number of cached models
     */
    public WhittakerBiomeModelProvider(int maxCacheSize) {
        modelCache = Maps.newHashMap(); //CacheBuilder.newBuilder().maximumSize(maxCacheSize).build();
    }

    @Override
    public void setSeed(long seed) {
        // ignore
    }

    @Override
    public void process(GeneratingRegion region) {
        WhittakerBiomeModelFacet facet = new WhittakerBiomeModelFacet();

        GraphFacet graphFacet = region.getRegionFacet(GraphFacet.class);

        MoistureModelFacet moistureFacet = region.getRegionFacet(MoistureModelFacet.class);
        WaterModelFacet waterFacet = region.getRegionFacet(WaterModelFacet.class);
        ElevationModelFacet elevationFacet = region.getRegionFacet(ElevationModelFacet.class);

        for (Graph graph : graphFacet.getAllGraphs()) {
            WaterModel waterModel = waterFacet.get(graph);
            ElevationModel elevationModel = elevationFacet.get(graph);
            MoistureModel moistureModel = moistureFacet.get(graph);
            BiomeModel model = getOrCreate(graph, elevationModel, waterModel, moistureModel);
            facet.add(graph, model);
        }

        region.setRegionFacet(WhittakerBiomeModelFacet.class, facet);
    }

    private BiomeModel getOrCreate(Graph graph, final ElevationModel elevationModel, final WaterModel waterModel, final MoistureModel moistureModel) {
        BiomeModel biomeModel = modelCache.get(graph);
        if (biomeModel == null) {
            biomeModel = call(graph, elevationModel, waterModel, moistureModel);
        }
//        try {
            return biomeModel;
//        } catch (ExecutionException e) {
//            logger.error("Could not create elevation model", e.getCause());
//            return null;
//        }
    }

    public BiomeModel call(Graph graph, final ElevationModel elevationModel, final WaterModel waterModel, final MoistureModel moistureModel) {
        return new DefaultBiomeModel(elevationModel, waterModel, moistureModel);
    }
}
