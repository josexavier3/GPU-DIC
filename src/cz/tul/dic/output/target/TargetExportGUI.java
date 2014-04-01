package cz.tul.dic.output.target;

import cz.tul.dic.data.task.TaskContainer;
import cz.tul.dic.gui.Context;
import cz.tul.dic.output.Direction;
import cz.tul.dic.output.ExportMode;
import cz.tul.dic.output.ExportUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class TargetExportGUI implements ITargetExport {

    @Override
    public void exportData(Object data, Direction direction, Object targetParam, int[] dataParams, TaskContainer tc) throws IOException {
        if (!(targetParam instanceof Context)) {
            throw new IllegalArgumentException("Illegal type of target parameter - " + targetParam.getClass());
        }
        if (data instanceof double[][]) {
            exportImage((double[][]) data, direction, targetParam, dataParams, tc);
        } else if (data instanceof double[]) {
            exportLine((double[]) data, direction, targetParam, dataParams, tc);
        } else {
            throw new IllegalArgumentException("Illegal type of data - " + targetParam.getClass());
        }

    }

    private void exportImage(final double[][] data, Direction direction, final Object targetParam, int[] dataParams, final TaskContainer tc) {
        if (dataParams.length < 1) {
            throw new IllegalArgumentException("Not enough data parameters.");
        }

        final int position = dataParams[0];
        final BufferedImage background = tc.getImage(position);
        final BufferedImage overlay = ExportUtils.createImageFromMap((double[][]) data, direction);

        final Context context = (Context) targetParam;
        context.storeMapExport(ExportUtils.overlayImage(background, overlay), position, ExportMode.MAP, direction);
    }

    private void exportLine(final double[] data, Direction direction, final Object targetParam, int[] dataParams, final TaskContainer tc) {
        if (dataParams.length < 1) {
            throw new IllegalArgumentException("Not enough data parameters.");
        }
        final Context context = (Context) targetParam;
        context.storeMapExport(data, dataParams[0], ExportMode.LINE, direction);
    }

    @Override
    public boolean supportsMode(ExportMode mode) {
        return !ExportMode.SEQUENCE.equals(mode);
    }

}
