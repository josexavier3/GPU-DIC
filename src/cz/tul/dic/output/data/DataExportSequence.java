package cz.tul.dic.output.data;

import cz.tul.dic.data.task.TaskContainer;
import cz.tul.dic.data.task.TaskContainerUtils;
import cz.tul.dic.output.Direction;
import java.util.ArrayList;
import java.util.List;

public class DataExportSequence implements IDataExport<List<double[][]>> {

    @Override
    public List<double[][]> exportData(TaskContainer tc, Direction direction, int... params) {
        final int roundCount = TaskContainerUtils.getRoundCount(tc);
        final List<double[][]> result = new ArrayList<>(roundCount);
        final DataExportMap mapExporter = new DataExportMap();
        
        for (int i = 0; i < roundCount; i++) {
            result.add(mapExporter.exportData(tc, direction, i));
        }

        return result;
    }

}
