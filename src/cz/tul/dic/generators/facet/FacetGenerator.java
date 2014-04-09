package cz.tul.dic.generators.facet;

import cz.tul.dic.ComputationException;
import cz.tul.dic.data.task.TaskContainer;
import cz.tul.dic.data.task.TaskParameter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Petr Jecmen
 */
public class FacetGenerator {

    private static final Map<FacetGeneratorMode, AbstractFacetGenerator> generators;

    static {
        generators = new HashMap<>();

        AbstractFacetGenerator fg = new SimpleFacetGenerator();
        generators.put(fg.getMode(), fg);
        fg = new TightFacetGenerator();
        generators.put(fg.getMode(), fg);
    }

    public static void generateFacets(final TaskContainer tc, final int round) throws ComputationException {
        final FacetGeneratorMode mode = (FacetGeneratorMode) tc.getParameter(TaskParameter.FACET_GENERATOR_MODE);
        if (generators.containsKey(mode)) {            
            generators.get(mode).generateFacets(tc, round);
        } else {
            throw new IllegalArgumentException("Unsupported mode of facet generator - " + mode.toString());
        }
    }

}
