/* Copyright (C) LENAM, s.r.o. - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Petr Jecmen <petr.jecmen@tul.cz>, 2015
 */
package cz.tul.dic.data.result;

import cz.tul.dic.data.roi.AbstractROI;
import cz.tul.dic.data.subset.AbstractSubset;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Lenam s.r.o.
 */
public class Result implements Serializable {

    private final HashMap<AbstractROI, List<AbstractSubset>> subsets;
    private final HashMap<AbstractROI, List<CorrelationResult>> correlations;
    private final DisplacementResult displacementResult;
    private final StrainResult strainResult;

    public Result(final HashMap<AbstractROI, List<AbstractSubset>> subsets, final HashMap<AbstractROI, List<CorrelationResult>> correlations, final DisplacementResult displacement) {
        this.subsets = subsets;
        this.correlations = correlations;
        this.displacementResult = displacement;
        strainResult = null;
    }

    public Result(final DisplacementResult displacement) {
        this.displacementResult = displacement;
        subsets = null;
        correlations = null;
        strainResult = null;
    }

    public Result(final Result subResult, final StrainResult strainResult) {
        this.subsets = subResult.getSubsets();
        this.correlations = subResult.getCorrelations();
        this.displacementResult = subResult.getDisplacementResult();
        this.strainResult = strainResult;
    }

    public HashMap<AbstractROI, List<AbstractSubset>> getSubsets() {
        return subsets;
    }

    public HashMap<AbstractROI, List<CorrelationResult>> getCorrelations() {
        return correlations;
    }

    public DisplacementResult getDisplacementResult() {
        return displacementResult;
    }

    public StrainResult getStrainResult() {
        return strainResult;
    }
}
