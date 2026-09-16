package com.ccdd.project.service;

import com.ccdd.common.api.BusinessException;
import com.ccdd.project.dto.TaskCpmAnalysisDto;
import com.ccdd.project.entity.*;
import com.ccdd.project.exception.CyclicTaskDependencyException;
import com.ccdd.project.repository.ProjectGateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * WBS 任务分解、依赖防环探测与 CPM 关键路径分析服务
 * 严格执行任务三态解耦 (Progress != Approval != Gate)
 */
@Service
public class WbsTaskService {

    private static final Logger log = LoggerFactory.getLogger(WbsTaskService.class);

    private final ProjectGateRepository repository;

    // 内存交付物版本提审存储 (支持测试与运行时动态演进)
    private final Map<Long, List<DeliverableSubmissionEntity>> submissionStore = new HashMap<>();

    public WbsTaskService(ProjectGateRepository repository) {
        this.repository = repository;
    }

    /**
     * 获取 WBS 分解与任务依赖网络全貌
     */
    public Map<String, Object> getWbsAndTasks(Long projectId) {
        List<WbsNodeEntity> wbsNodes = repository.findWbsNodesByProjectId(projectId);
        List<TaskEntity> tasks = repository.findTasksByProjectId(projectId);
        List<TaskDependencyEntity> dependencies = repository.findDependenciesByProjectId(projectId);

        Map<String, Object> map = new HashMap<>();
        map.put("projectId", projectId);
        map.put("wbsNodes", wbsNodes);
        map.put("tasks", tasks);
        map.put("dependencies", dependencies);
        return map;
    }

    /**
     * 获取指定任务的交付物规约与多版本提审历史
     */
    public List<Map<String, Object>> getTaskDeliverables(Long taskId) {
        List<DeliverableRequirementEntity> reqs = repository.findDeliverableRequirementsByTaskId(taskId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (DeliverableRequirementEntity req : reqs) {
            Map<String, Object> item = new HashMap<>();
            item.put("requirement", req);

            List<DeliverableSubmissionEntity> subs = submissionStore.get(req.getDelivReqId());
            if (subs == null) {
                subs = new ArrayList<>(repository.findSubmissionsByReqId(req.getDelivReqId()));
                submissionStore.put(req.getDelivReqId(), subs);
            }
            item.put("submissions", subs);
            result.add(item);
        }
        return result;
    }

    /**
     * 交付物多版本提审
     * 严格遵从：旧版本保留历史记录并标记 isLatest = false，三态彻底独立
     */
    public DeliverableSubmissionEntity submitDeliverable(Long taskId, Long reqId, String notes, String user) {
        List<DeliverableSubmissionEntity> subs = submissionStore.computeIfAbsent(reqId,
                k -> new ArrayList<>(repository.findSubmissionsByReqId(reqId)));

        // 将之前的所有提交标记为非最新 (SUPERSEDED 留档)
        for (DeliverableSubmissionEntity sub : subs) {
            sub.setIsLatest(false);
        }

        long nextRevisionId = 8800L + subs.size() + 1;
        long nextSubmissionId = 7000L + System.currentTimeMillis() % 10000;
        String hash = UUID.randomUUID().toString().replace("-", "");

        DeliverableSubmissionEntity newSub = DeliverableSubmissionEntity.builder()
                .submissionId(nextSubmissionId)
                .delivReqId(reqId)
                .revisionId(nextRevisionId)
                .artifactHash(hash)
                .submissionNotes(notes != null ? notes : "版本迭代交付归档")
                .isLatest(true)
                .submittedBy(user != null ? user : "CURRENT_USER")
                .submittedAt(Instant.now())
                .build();

        subs.add(newSub);
        log.info("交付物要求 [{}] 新增版本提交: Rev {}", reqId, nextRevisionId);
        return newSub;
    }

    /**
     * 任务网络分析：DFS 三色标记法防环检测 + CPM 关键路径 (TF=0) 推导
     */
    public TaskCpmAnalysisDto analyzeCpmAndDetectCycles(Long projectId) {
        List<TaskEntity> tasks = repository.findTasksByProjectId(projectId);
        List<TaskDependencyEntity> dependencies = repository.findDependenciesByProjectId(projectId);

        return computeCpm(projectId, tasks, dependencies);
    }

    /**
     * 核心计算引擎：支持外部传入自定义任务与依赖以供严密单元测试
     */
    public TaskCpmAnalysisDto computeCpm(Long projectId, List<TaskEntity> tasks, List<TaskDependencyEntity> dependencies) {
        if (tasks == null || tasks.isEmpty()) {
            return new TaskCpmAnalysisDto();
        }

        Map<Long, TaskEntity> taskMap = tasks.stream()
                .collect(Collectors.toMap(TaskEntity::getTaskId, t -> t));

        // 构建邻接表
        Map<Long, List<TaskDependencyEntity>> outgoing = new HashMap<>();
        Map<Long, List<TaskDependencyEntity>> incoming = new HashMap<>();
        for (TaskEntity task : tasks) {
            outgoing.put(task.getTaskId(), new ArrayList<>());
            incoming.put(task.getTaskId(), new ArrayList<>());
        }

        for (TaskDependencyEntity dep : dependencies) {
            if (outgoing.containsKey(dep.getPredecessorTaskId()) && incoming.containsKey(dep.getSuccessorTaskId())) {
                outgoing.get(dep.getPredecessorTaskId()).add(dep);
                incoming.get(dep.getSuccessorTaskId()).add(dep);
            }
        }

        // ==========================================
        // 1. DFS 三色标记法防环探测 (0: 未访问, 1: 正在访问, 2: 已完成)
        // ==========================================
        Map<Long, Integer> color = new HashMap<>();
        Map<Long, Long> parent = new HashMap<>();
        for (Long taskId : taskMap.keySet()) {
            color.put(taskId, 0);
        }

        for (Long taskId : taskMap.keySet()) {
            if (color.get(taskId) == 0) {
                dfsCycleDetect(taskId, outgoing, color, parent, taskMap);
            }
        }

        // ==========================================
        // 2. 拓扑排序 (Kahn 算法)
        // ==========================================
        Map<Long, Integer> inDegree = new HashMap<>();
        for (Long taskId : taskMap.keySet()) {
            inDegree.put(taskId, incoming.get(taskId).size());
        }

        Queue<Long> queue = new LinkedList<>();
        for (Map.Entry<Long, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }

        List<Long> topoOrder = new ArrayList<>();
        while (!queue.isEmpty()) {
            Long u = queue.poll();
            topoOrder.add(u);
            for (TaskDependencyEntity dep : outgoing.get(u)) {
                Long v = dep.getSuccessorTaskId();
                int deg = inDegree.get(v) - 1;
                inDegree.put(v, deg);
                if (deg == 0) {
                    queue.offer(v);
                }
            }
        }

        if (topoOrder.size() < tasks.size()) {
            throw new CyclicTaskDependencyException("任务网络拓扑排序失败，存在不可达或隐式依赖回路！");
        }

        // ==========================================
        // 3. CPM 前向推导 (计算最早开始 ES 与最早完成 EF)
        // ==========================================
        Map<Long, Integer> earlyStart = new HashMap<>();
        Map<Long, Integer> earlyFinish = new HashMap<>();

        for (Long taskId : topoOrder) {
            TaskEntity task = taskMap.get(taskId);
            int duration = task.getDurationDays() != null ? task.getDurationDays() : 0;
            int maxPredecessorEf = 0;

            for (TaskDependencyEntity dep : incoming.get(taskId)) {
                Long predId = dep.getPredecessorTaskId();
                int lag = dep.getLagDays() != null ? dep.getLagDays() : 0;
                int predEf = earlyFinish.getOrDefault(predId, 0);
                int requiredEs = predEf + lag;
                if (dep.getDepType() == DependencyType.SS) {
                    requiredEs = earlyStart.getOrDefault(predId, 0) + lag;
                }
                if (requiredEs > maxPredecessorEf) {
                    maxPredecessorEf = requiredEs;
                }
            }

            earlyStart.put(taskId, maxPredecessorEf);
            earlyFinish.put(taskId, maxPredecessorEf + duration);
        }

        int projectDuration = earlyFinish.values().stream().max(Integer::compareTo).orElse(0);

        // ==========================================
        // 4. CPM 后向推导 (计算最迟完成 LF 与最迟开始 LS)
        // ==========================================
        Map<Long, Integer> lateStart = new HashMap<>();
        Map<Long, Integer> lateFinish = new HashMap<>();

        List<Long> reverseTopo = new ArrayList<>(topoOrder);
        Collections.reverse(reverseTopo);

        for (Long taskId : reverseTopo) {
            TaskEntity task = taskMap.get(taskId);
            int duration = task.getDurationDays() != null ? task.getDurationDays() : 0;
            int minSuccessorLs = projectDuration;

            if (outgoing.get(taskId).isEmpty()) {
                minSuccessorLs = projectDuration;
            } else {
                for (TaskDependencyEntity dep : outgoing.get(taskId)) {
                    Long succId = dep.getSuccessorTaskId();
                    int lag = dep.getLagDays() != null ? dep.getLagDays() : 0;
                    int succLs = lateStart.getOrDefault(succId, projectDuration);
                    int allowableLf = succLs - lag;
                    if (dep.getDepType() == DependencyType.SS) {
                        allowableLf = lateStart.getOrDefault(succId, projectDuration) - lag + duration;
                    }
                    if (allowableLf < minSuccessorLs) {
                        minSuccessorLs = allowableLf;
                    }
                }
            }

            lateFinish.put(taskId, minSuccessorLs);
            lateStart.put(taskId, minSuccessorLs - duration);
        }

        // ==========================================
        // 5. 计算总时差 TF = LS - ES，识别关键路径 (TF == 0)
        // ==========================================
        List<TaskCpmAnalysisDto.TaskScheduleMetricDto> metrics = new ArrayList<>();
        List<String> criticalTaskCodes = new ArrayList<>();

        for (TaskEntity task : tasks) {
            Long tid = task.getTaskId();
            int es = earlyStart.getOrDefault(tid, 0);
            int ef = earlyFinish.getOrDefault(tid, 0);
            int ls = lateStart.getOrDefault(tid, 0);
            int lf = lateFinish.getOrDefault(tid, 0);
            int tf = ls - es;
            boolean isCritical = (tf == 0);

            if (isCritical) {
                criticalTaskCodes.add(task.getTaskCode());
            }

            metrics.add(new TaskCpmAnalysisDto.TaskScheduleMetricDto(
                    tid,
                    task.getTaskCode(),
                    task.getName(),
                    task.getDurationDays(),
                    es,
                    ef,
                    ls,
                    lf,
                    tf,
                    isCritical
            ));
        }

        TaskCpmAnalysisDto result = new TaskCpmAnalysisDto();
        result.setProjectId(projectId);
        result.setCriticalPathLengthDays(projectDuration);
        result.setCriticalPathTaskCodes(criticalTaskCodes);
        result.setTaskMetrics(metrics);

        return result;
    }

    private void dfsCycleDetect(Long u, Map<Long, List<TaskDependencyEntity>> outgoing,
                                Map<Long, Integer> color, Map<Long, Long> parent,
                                Map<Long, TaskEntity> taskMap) {
        color.put(u, 1); // GRAY

        for (TaskDependencyEntity dep : outgoing.get(u)) {
            Long v = dep.getSuccessorTaskId();
            if (color.get(v) == 1) {
                // 发现回路
                List<String> cyclePath = new ArrayList<>();
                cyclePath.add(taskMap.get(v).getTaskCode());
                Long curr = u;
                while (curr != null && !curr.equals(v)) {
                    cyclePath.add(taskMap.get(curr).getTaskCode());
                    curr = parent.get(curr);
                }
                cyclePath.add(taskMap.get(v).getTaskCode());
                Collections.reverse(cyclePath);

                throw new CyclicTaskDependencyException(String.join(" -> ", cyclePath));
            } else if (color.get(v) == 0) {
                parent.put(v, u);
                dfsCycleDetect(v, outgoing, color, parent, taskMap);
            }
        }

        color.put(u, 2); // BLACK
    }
}
