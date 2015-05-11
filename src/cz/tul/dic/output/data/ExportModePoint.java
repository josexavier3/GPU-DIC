/* Copyright (C) LENAM, s.r.o. - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Petr Jecmen <petr.jecmen@tul.cz>, 2015
 */
package cz.tul.dic.output.data;

import cz.tul.dic.ComputationException;
import cz.tul.dic.ComputationExceptionCause;
import cz.tul.dic.FpsManager;
import cz.tul.dic.data.task.TaskContainer;
import cz.tul.dic.data.task.TaskContainerUtils;
import cz.tul.dic.data.task.TaskParameter;
import cz.tul.dic.data.result.DisplacementResult;
import cz.tul.dic.output.Direction;
import cz.tul.dic.output.ExportUtils;
import java.util.EnumMap;
import java.util.Map;

public class ExportModePoint implements IExportMode<Map<Direction, double[]>> {

    @Override
    public Map<Direction, double[]> exportData(final TaskContainer tc, final Direction direction, final int[] dataParams) throws ComputationException {
        if (dataParams == null || dataParams.length < 2) {
            throw new IllegalArgumentException("Not enough input parameters (position [x, y] required).");
        }

        final int roundCount = TaskContainerUtils.getMaxRoundCount(tc);
        final int roundZero = TaskContainerUtils.getFirstRound(tc);
        final Map<Direction, double[]> result = new EnumMap<>(Direction.class);
        for (Direction d : Direction.values()) {
            result.put(d, new double[roundCount]);
        }

        final int x = dataParams[0];
        final int y = dataParams[1];

        final FpsManager fpsM = new FpsManager(tc);
        final double time = fpsM.getTickLength();

        final double pxToMm = 1 / (double) tc.getParameter(TaskParameter.MM_TO_PX_RATIO);

        DisplacementResult displacement;
        double[][][] results;
        double[] data;
        for (Direction dir : Direction.values()) {
            data = result.get(dir);
            for (int r = 0; r < roundCount; r++) {
                switch (dir) {
                    case dDx:
                    case dDy:
                    case dDabs:
                    case rDx:
                    case rDy:
                    case rDabs:
                        displacement = tc.getResult(r - 1, r).getDisplacementResult();
                        results = displacement != null ? displacement.getDisplacement() : null;
                        break;
                    case Dx:
                    case Dy:
                    case Dabs:
                        displacement = tc.getResult(roundZero, r).getDisplacementResult();
                        results = displacement != null ? displacement.getDisplacement() : null;
                        break;
                    case dExx:
                    case dEyy:
                    case dExy:
                    case dEabs:
                        results = tc.getResult(r - 1, r).getStrainResult().getStrain();
                        break;
                    case Exx:
                    case Eyy:
                    case Exy:
                    case Eabs:
                        results = tc.getResult(roundZero, r).getStrainResult().getStrain();
                        break;
                    default:
                        throw new ComputationException(ComputationExceptionCause.ILLEGAL_TASK_DATA, "Unsupported direction - " + dir);
                }

                if (results == null || results.length < x || results[0].length < y || results[x][y] == null) {
                    data[r] = 0;
                } else {
                    switch (dir) {
                        case dDx:
                        case dDy:
                        case dDabs:
                        case Dx:
                        case Dy:
                        case Dabs:
                            data[r] = ExportUtils.calculateDisplacement(results[x][y], dir);
                            break;
                        case dExx:
                        case dEyy:
                        case dExy:
                        case dEabs:
                        case Exx:
                        case Eyy:
                        case Exy:
                        case Eabs:
                            data[r] = ExportUtils.calculateStrain(results[x][y], dir);
                            break;
                        case rDx:
                        case rDy:
                        case rDabs:
                            data[r] = ExportUtils.calculateSpeed(results[x][y], dir, time);
                            break;
                        default:
                            throw new ComputationException(ComputationExceptionCause.ILLEGAL_TASK_DATA, "Unsupported direction.");
                    }

                    if (dir.isMm()) {
                        data[r] *= pxToMm;
                    }
                }
            }
        }

        return result;
    }
}
