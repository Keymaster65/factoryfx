package de.factoryfx.data.merge;

import java.util.Map;
import java.util.function.Function;

import de.factoryfx.data.Data;

public class DataMerger {

    private final Data commonData;
    private final Data currentData;
    private final Data newData;

    public DataMerger(Data currentData, Data commonData, Data newData) {
        this.commonData = commonData;
        this.currentData = currentData;
        this.newData = newData;
    }

    @SuppressWarnings("unchecked")
    public MergeResult createMergeResult(Function<String,Boolean> permissionChecker) {
        MergeResult mergeResult = new MergeResult(currentData);

        Map<String, Data> originalMap = commonData.internal().collectChildDataMap();
        Map<String, Data> currentMap = currentData.internal().collectChildDataMap();
        Map<String, Data> newMap = newData.internal().collectChildDataMap();

        for (Map.Entry<String, Data> entry : currentMap.entrySet()) {
            Data originalValue = originalMap.get(entry.getKey());
            Data newValue = newMap.get(entry.getKey());

            if (newValue==null && originalValue!=null){
                //check for conflict for removed object
                entry.getValue().internal().visitAttributesDualFlat(originalValue, (name, currentAttribute, originalAttribute) -> {
                    if (!currentAttribute.internal_match(originalAttribute)){
                        mergeResult.addConflictInfo(new AttributeDiffInfo(name,entry.getValue().getId()));
                    }
                });
            }

            if (originalValue!=null && newValue!=null){
                entry.getValue().internal().merge(originalValue, newValue, mergeResult, permissionChecker);
            }
        }
        return mergeResult;
    }

    public MergeDiffInfo mergeIntoCurrent(Function<String,Boolean> permissionChecker) {
        return createMergeResult(permissionChecker).executeMerge();
    }
}
