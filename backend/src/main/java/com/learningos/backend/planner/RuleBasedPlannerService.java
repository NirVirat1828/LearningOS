package com.learningos.backend.planner;

import com.learningos.backend.dto.TaskResponse;
import com.learningos.backend.entity.Task;
import com.learningos.backend.entity.TaskStatus;
import com.learningos.backend.mapper.TaskMapper;
import com.learningos.backend.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Deterministic planner — active when {@code spring.profiles.active=rule-based}.
 *
 * <p>Generates today's task plan using three deterministic rules applied in order:
 * <ol>
 *   <li><b>Carry forward</b> — any PENDING task from a prior day is re-scheduled
 *       to today, ensuring nothing gets silently lost.</li>
 *   <li><b>Top-up from backlog</b> — unscheduled tasks are pulled from the backlog
 *       and sorted by:
 *       <ul>
 *         <li>Priority (HIGH → MEDIUM → LOW)</li>
 *         <li>Difficulty (via the topic's difficulty field if available)</li>
 *         <li>Creation time (oldest first) as a tiebreaker</li>
 *       </ul>
 *       until {@code learningos.planner.daily-capacity} is reached.</li>
 *   <li><b>Return</b> — the full list of tasks scheduled for today is returned.</li>
 * </ol>
 */
@Service
@Profile("rule-based")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RuleBasedPlannerService implements PlannerService {

    private final TaskRepository taskRepository;

    @Value("${learningos.planner.daily-capacity:5}")
    private int dailyCapacity;

    @Override
    @Transactional
    public List<TaskResponse> generateTodaysTasks() {
        LocalDate today = LocalDate.now();

        // Rule 1: carry forward overdue PENDING tasks.
        List<Task> overdue = taskRepository.findByStatusAndScheduledDateBefore(TaskStatus.PENDING, today);
        overdue.forEach(t -> t.setScheduledDate(today));

        // Rule 2: top up from backlog, sorted by Priority → CreatedAt.
        long scheduledToday = taskRepository.countByScheduledDateAndStatus(today, TaskStatus.PENDING);
        int remaining = (int) Math.max(0, dailyCapacity - scheduledToday);
        if (remaining > 0) {
            taskRepository.findByScheduledDateIsNullAndStatus(TaskStatus.PENDING).stream()
                    .sorted(Comparator.comparing(Task::getPriority)
                            .thenComparing(Task::getCreatedAt))
                    .limit(remaining)
                    .forEach(t -> t.setScheduledDate(today));
        }

        // Rule 3: return today's full plan.
        return taskRepository.findByScheduledDate(today).stream()
                .map(TaskMapper::toResponse)
                .toList();
    }
}
