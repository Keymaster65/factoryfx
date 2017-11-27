package de.factoryfx.data.merge;

import de.factoryfx.data.Data;
import de.factoryfx.data.jackson.ObjectMapperBuilder;
import de.factoryfx.data.jackson.SimpleObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class MergeResult<R extends Data> {
    final R previousRoot;
    final R currentRoot;

    final List<AttributeDiffInfo> mergeInfos = new ArrayList<>();
    final List<AttributeDiffInfo> conflictInfos = new ArrayList<>();
    final List<AttributeDiffInfo> mergePermissionViolations = new ArrayList<>();

    final List<Runnable> mergeExecutions = new ArrayList<>();

    public MergeResult(R currentRoot) {
        this.previousRoot = currentRoot.internal().copy();
        this.currentRoot = currentRoot;
    }

    public void addConflictInfo(AttributeDiffInfo conflictInfo) {
        conflictInfos.add(conflictInfo);
    }

    public void addMergeExecutions(Runnable mergeAction) {
        mergeExecutions.add(mergeAction);
    }

    public void addMergeInfo(AttributeDiffInfo mergeInfo) {
        mergeInfos.add(mergeInfo);
    }

    public void addPermissionViolationInfo(AttributeDiffInfo permissionViolation) {
        mergePermissionViolations.add(permissionViolation);
    }

    @SuppressWarnings("unchecked")
    public MergeDiffInfo<R> executeMerge() {
        if (hasNoConflicts() && hasNoPermissionViolation()){
            for (Runnable mergeAction : mergeExecutions) {
                mergeAction.run();
            }
            currentRoot.internal().fixDuplicateData();
        }
        return new MergeDiffInfo<R>(mergeInfos, conflictInfos, mergePermissionViolations, previousRoot, currentRoot, (Class<R>) currentRoot.getClass());

    }

    private boolean hasNoPermissionViolation() {
        return mergePermissionViolations.isEmpty();
    }

    private boolean hasNoConflicts() {
        return conflictInfos.isEmpty();
    }

}
