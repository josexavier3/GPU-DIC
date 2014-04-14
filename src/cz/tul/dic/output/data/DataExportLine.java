package cz.tul.dic.output.data;

import cz.tul.dic.ComputationException;
import cz.tul.dic.ComputationExceptionCause;
import cz.tul.dic.data.roi.ROI;
import cz.tul.dic.data.task.TaskContainer;
import cz.tul.dic.data.task.TaskContainerUtils;
import cz.tul.dic.output.Direction;
import cz.tul.dic.output.ExportUtils;

public class DataExportLine implements IDataExport<double[]> {

    @Override
    public double[] exportData(TaskContainer tc, Direction direction, int[] dataParams, ROI... rois) throws ComputationException {
        if (dataParams == null || dataParams.length < 3) {
            throw new IllegalArgumentException("Not enough input parameters (position, x, y required).");
        }

        final int roundCount = TaskContainerUtils.getRoundCount(tc);
        final double[] result = new double[roundCount];

        final int x = dataParams[0];
        final int y = dataParams[1];

        // check if position is inside ROI        
        double[][][] results;
        for (int r = 0; r < roundCount; r++) {
            if (ExportUtils.isPointInsideROIs(x, y, rois, tc, r)) {
                results = tc.getPerPixelResult(r);
                if (results == null || results.length < x || results[0].length < y) {
                    throw new IllegalArgumentException("Illegal result data.");
                }
                if (results[x][y] == null) {
                    continue;
                }

                switch (direction) {
                    case X:
                    case Y:
                    case ABS:
                        result[r] = ExportUtils.calculateDisplacement(results[x][y], direction);
                        break;
                    case DX:
                    case DY:
                    case DABS:
                        throw new UnsupportedOperationException("Not yet supported.");
                    default:
                        throw new ComputationException(ComputationExceptionCause.ILLEGAL_TASK_DATA, "Unsupported direction.");
                }
            }
        }

        return result;
    }
}
