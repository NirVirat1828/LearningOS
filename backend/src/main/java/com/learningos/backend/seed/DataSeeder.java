package com.learningos.backend.seed;

import com.learningos.backend.entity.Course;
import com.learningos.backend.entity.Difficulty;
import com.learningos.backend.entity.Module;
import com.learningos.backend.entity.Priority;
import com.learningos.backend.entity.Progress;
import com.learningos.backend.entity.ProgressStatus;
import com.learningos.backend.entity.Resource;
import com.learningos.backend.entity.ResourceType;
import com.learningos.backend.entity.Roadmap;
import com.learningos.backend.entity.Task;
import com.learningos.backend.entity.TaskStatus;
import com.learningos.backend.entity.Topic;
import com.learningos.backend.entity.TopicDependency;
import com.learningos.backend.repository.RoadmapRepository;
import com.learningos.backend.repository.TopicDependencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Populates one coherent example Roadmap on startup so the schema and every
 * relationship can be explored immediately. Skips seeding if a Roadmap
 * already exists, so this stays safe to run against a persistent database.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoadmapRepository roadmapRepository;
    private final TopicDependencyRepository topicDependencyRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (roadmapRepository.count() > 0) {
            log.info("Skipping database seed: data already present");
            return;
        }

        Roadmap roadmap = new Roadmap();
        roadmap.setTitle("Full-Stack Web Development");
        roadmap.setDescription("A guided path from backend fundamentals to a deployed full-stack app.");

        Course backendCourse = new Course();
        backendCourse.setTitle("Backend with Spring Boot");
        backendCourse.setDescription("Building REST APIs with Spring Boot and JPA.");
        roadmap.addCourse(backendCourse);

        Module springBasicsModule = new Module();
        springBasicsModule.setTitle("Spring Boot Basics");
        springBasicsModule.setDescription("Core concepts of Spring Boot applications.");
        backendCourse.addModule(springBasicsModule);

        Topic springFundamentalsTopic = new Topic();
        springFundamentalsTopic.setTitle("Java & Spring Fundamentals");
        springFundamentalsTopic.setDescription("Setting up a Spring Boot project and understanding its structure.");
        springFundamentalsTopic.setDifficulty(Difficulty.BEGINNER);
        springFundamentalsTopic.setEstimatedMinutes(25);
        springBasicsModule.addTopic(springFundamentalsTopic);

        Topic dependencyInjectionTopic = new Topic();
        dependencyInjectionTopic.setTitle("Dependency Injection");
        dependencyInjectionTopic.setDescription("Understanding IoC and DI in Spring.");
        dependencyInjectionTopic.setDifficulty(Difficulty.INTERMEDIATE);
        dependencyInjectionTopic.setEstimatedMinutes(45);
        springBasicsModule.addTopic(dependencyInjectionTopic);

        Topic restControllersTopic = new Topic();
        restControllersTopic.setTitle("REST Controllers");
        restControllersTopic.setDescription("Building REST endpoints with @RestController.");
        restControllersTopic.setDifficulty(Difficulty.BEGINNER);
        restControllersTopic.setEstimatedMinutes(30);
        springBasicsModule.addTopic(restControllersTopic);

        Module dataJpaModule = new Module();
        dataJpaModule.setTitle("Data Persistence with JPA");
        dataJpaModule.setDescription("Mapping domain models to a relational database.");
        backendCourse.addModule(dataJpaModule);

        Topic entityMappingTopic = new Topic();
        entityMappingTopic.setTitle("Entity Mapping & Relationships");
        entityMappingTopic.setDescription("Modeling one-to-many and many-to-many relationships with JPA.");
        entityMappingTopic.setDifficulty(Difficulty.ADVANCED);
        entityMappingTopic.setEstimatedMinutes(90);
        dataJpaModule.addTopic(entityMappingTopic);

        Course frontendCourse = new Course();
        frontendCourse.setTitle("Frontend with React");
        frontendCourse.setDescription("Building interactive UIs with React and TypeScript.");
        roadmap.addCourse(frontendCourse);

        Module reactBasicsModule = new Module();
        reactBasicsModule.setTitle("React Basics");
        reactBasicsModule.setDescription("Components, props, and state.");
        frontendCourse.addModule(reactBasicsModule);

        Topic componentsTopic = new Topic();
        componentsTopic.setTitle("Components & Props");
        componentsTopic.setDescription("Building reusable UI pieces.");
        componentsTopic.setDifficulty(Difficulty.BEGINNER);
        componentsTopic.setEstimatedMinutes(20);
        reactBasicsModule.addTopic(componentsTopic);

        LocalDate today = LocalDate.now();

        // Daily planner demo data: a mix that exercises every branch of
        // TaskPlannerService.generateTodaysTasks() -- see that class for the
        // algorithm this is designed to demonstrate. Completed tasks also
        // carry completedDate/timeSpentMinutes, spread across recent days,
        // to give the learning calendar real activity to render.
        springFundamentalsTopic.addTask(newCompletedTask(
                "Generate a project with Spring Initializr",
                Priority.MEDIUM, today.minusDays(3), 20));
        dependencyInjectionTopic.addTask(newCompletedTask(
                "Read the Spring DI documentation",
                Priority.MEDIUM, today.minusDays(1), 30));
        // Left PENDING from yesterday -- carries forward onto today's plan.
        dependencyInjectionTopic.addTask(newTask(
                "Write a @Service/@Autowired example",
                TaskStatus.PENDING, Priority.HIGH, today.minusDays(1)));
        // SKIPPED tasks are resolved too, so this one stays in the past.
        dependencyInjectionTopic.addTask(newTask(
                "Watch the Spring DI conference talk",
                TaskStatus.SKIPPED, Priority.LOW, today.minusDays(1)));
        // Already on today's plan before generation even runs.
        componentsTopic.addTask(newTask(
                "Review React Router basics",
                TaskStatus.PENDING, Priority.MEDIUM, today));
        // Backlog (no scheduledDate) -- ordered here HIGH/MEDIUM/LOW to show
        // generation pulls them in priority order, not insertion order.
        componentsTopic.addTask(newTask(
                "Build a reusable Button component",
                TaskStatus.PENDING, Priority.HIGH, null));
        restControllersTopic.addTask(newTask(
                "Build a sample REST controller",
                TaskStatus.PENDING, Priority.MEDIUM, null));
        entityMappingTopic.addTask(newTask(
                "Model a one-to-many relationship",
                TaskStatus.PENDING, Priority.LOW, null));

        // More completed tasks, further back, purely to spread out learning
        // calendar activity -- none of these are PENDING/backlog, so they
        // don't interact with the planner algorithm above.
        componentsTopic.addTask(newCompletedTask(
                "Set up a Vite + React project", Priority.LOW, today.minusDays(8), 35));
        entityMappingTopic.addTask(newCompletedTask(
                "Read Hibernate mapping annotations overview", Priority.LOW, today.minusDays(6), 40));
        restControllersTopic.addTask(newCompletedTask(
                "Read Spring MVC overview", Priority.LOW, today.minusDays(4), 25));

        springFundamentalsTopic.addResource(newResource(
                "Spring Boot Reference Documentation",
                "https://docs.spring.io/spring-boot/documentation.html",
                ResourceType.OFFICIAL_DOCS));

        // Dependency Injection carries one of each of the 5 resource types,
        // so its Resources tab is a complete demo of every filter.
        dependencyInjectionTopic.addResource(newResource(
                "Spring Framework IoC Container",
                "https://docs.spring.io/spring-framework/reference/core/beans.html",
                ResourceType.OFFICIAL_DOCS));
        dependencyInjectionTopic.addResource(newResource(
                "Spring Dependency Injection Explained",
                "https://www.youtube.com/results?search_query=spring+dependency+injection+explained",
                ResourceType.YOUTUBE));
        dependencyInjectionTopic.addResource(newResource(
                "Spring in Action",
                "https://www.manning.com/books/spring-in-action-sixth-edition",
                ResourceType.BOOK));
        dependencyInjectionTopic.addResource(newResource(
                "spring-projects/spring-framework",
                "https://github.com/spring-projects/spring-framework",
                ResourceType.GITHUB));
        dependencyInjectionTopic.addResource(newResource(
                "Inversion of Control Containers and the Dependency Injection pattern",
                "https://martinfowler.com/articles/injection.html",
                ResourceType.ARTICLE));

        restControllersTopic.addResource(newResource(
                "Building a RESTful Web Service",
                "https://spring.io/guides/gs/rest-service/",
                ResourceType.ARTICLE));
        restControllersTopic.addResource(newResource(
                "spring-guides/gs-rest-service",
                "https://github.com/spring-guides/gs-rest-service",
                ResourceType.GITHUB));

        entityMappingTopic.addResource(newResource(
                "Hibernate ORM User Guide",
                "https://docs.jboss.org/hibernate/orm/6.5/userguide/html_single/Hibernate_User_Guide.html",
                ResourceType.OFFICIAL_DOCS));
        entityMappingTopic.addResource(newResource(
                "Java Persistence with Hibernate",
                "https://www.manning.com/books/java-persistence-with-hibernate-second-edition",
                ResourceType.BOOK));

        componentsTopic.addResource(newResource(
                "Thinking in React",
                "https://react.dev/learn/thinking-in-react",
                ResourceType.ARTICLE));
        componentsTopic.addResource(newResource(
                "React Crash Course",
                "https://www.youtube.com/results?search_query=react+crash+course",
                ResourceType.YOUTUBE));
        componentsTopic.addResource(newResource(
                "facebook/react",
                "https://github.com/facebook/react",
                ResourceType.GITHUB));

        springFundamentalsTopic.setProgress(newProgress(ProgressStatus.COMPLETED, 100));
        springFundamentalsTopic.getProgress().setCompletedDate(today.minusDays(3));
        dependencyInjectionTopic.setProgress(newProgress(ProgressStatus.IN_PROGRESS, 60));
        restControllersTopic.setProgress(newProgress(ProgressStatus.NOT_STARTED, 0));
        entityMappingTopic.setProgress(newProgress(ProgressStatus.NOT_STARTED, 0));
        componentsTopic.setProgress(newProgress(ProgressStatus.NOT_STARTED, 0));

        roadmapRepository.save(roadmap);

        // Prerequisite chain, walking the dependency graph's four node states end to end:
        // Java & Spring Fundamentals (COMPLETED, no prerequisites)
        //   -> Dependency Injection (CURRENT: in progress regardless of its prerequisite being done)
        //     -> REST Controllers (LOCKED: its prerequisite isn't COMPLETED yet)
        //       -> Entity Mapping & Relationships (LOCKED, same reason one level further out)
        // Components & Props has no prerequisites and is NOT_STARTED, so it's UNLOCKED.
        topicDependencyRepository.save(new TopicDependency(dependencyInjectionTopic, springFundamentalsTopic));
        topicDependencyRepository.save(new TopicDependency(restControllersTopic, dependencyInjectionTopic));
        topicDependencyRepository.save(new TopicDependency(entityMappingTopic, restControllersTopic));

        log.info("Seeded database with example roadmap '{}'", roadmap.getTitle());
    }

    private Task newTask(String title, TaskStatus status, Priority priority, LocalDate scheduledDate) {
        Task task = new Task();
        task.setTitle(title);
        task.setStatus(status);
        task.setPriority(priority);
        task.setScheduledDate(scheduledDate);
        return task;
    }

    /** A task completed on the given day, with time logged -- feeds the learning calendar. */
    private Task newCompletedTask(String title, Priority priority, LocalDate completedDate, int timeSpentMinutes) {
        Task task = newTask(title, TaskStatus.COMPLETED, priority, completedDate);
        task.setCompletedDate(completedDate);
        task.setTimeSpentMinutes(timeSpentMinutes);
        return task;
    }

    private Resource newResource(String title, String url, ResourceType type) {
        Resource resource = new Resource();
        resource.setTitle(title);
        resource.setUrl(url);
        resource.setType(type);
        return resource;
    }

    private Progress newProgress(ProgressStatus status, int percentage) {
        Progress progress = new Progress();
        progress.setStatus(status);
        progress.setCompletionPercentage(percentage);
        return progress;
    }
}
