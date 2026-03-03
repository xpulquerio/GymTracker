package com.org2.workout.backend.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.org2.workout.backend.model.Equipment;
import com.org2.workout.backend.model.Exercise;
import com.org2.workout.backend.model.PlannedWorkout;
import com.org2.workout.backend.model.PlannedWorkoutItem;
import com.org2.workout.backend.model.User;
import com.org2.workout.backend.repository.EquipmentRepository;
import com.org2.workout.backend.repository.ExerciseRepository;
import com.org2.workout.backend.repository.PlannedWorkoutItemRepository;
import com.org2.workout.backend.repository.PlannedWorkoutRepository;
import com.org2.workout.backend.repository.UserRepository;

@Component
public class DevDataSeeder implements CommandLineRunner {

    private final EquipmentRepository equipmentRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final PlannedWorkoutRepository plannedWorkoutRepository;
    private final PlannedWorkoutItemRepository plannedWorkoutItemRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDataSeeder(
            EquipmentRepository equipmentRepository,
            ExerciseRepository exerciseRepository,
            UserRepository userRepository,
            PlannedWorkoutRepository plannedWorkoutRepository,
            PlannedWorkoutItemRepository plannedWorkoutItemRepository,
            PasswordEncoder passwordEncoder) {
        this.equipmentRepository = equipmentRepository;
        this.exerciseRepository = exerciseRepository;
        this.userRepository = userRepository;
        this.plannedWorkoutRepository = plannedWorkoutRepository;
        this.plannedWorkoutItemRepository = plannedWorkoutItemRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedEquipmentsAndExercises();
        seedUserIfNeeded();
        seedPlannedWorkoutsForUsers();
    }

    private void seedEquipmentsAndExercises() {
        Equipment dumbbell = equipmentRepository.findByDescriptionIgnoreCase("Dumbbell")
                .orElseGet(() -> {
                    Equipment equipment = new Equipment();
                    equipment.setDescription("Dumbbell");
                    return equipmentRepository.save(equipment);
                });

        Equipment bodyweight = equipmentRepository.findByDescriptionIgnoreCase("Bodyweight")
                .orElseGet(() -> {
                    Equipment equipment = new Equipment();
                    equipment.setDescription("Bodyweight");
                    return equipmentRepository.save(equipment);
                });

        Equipment treadmill = equipmentRepository.findByDescriptionIgnoreCase("Treadmill")
                .orElseGet(() -> {
                    Equipment equipment = new Equipment();
                    equipment.setDescription("Treadmill");
                    return equipmentRepository.save(equipment);
                });

        if (exerciseRepository.count() == 0) {
            exerciseRepository.saveAll(List.of(
                    buildExercise("Supino com halteres", Exercise.Tipo.STRENGTH, dumbbell),
                    buildExercise("Agachamento livre", Exercise.Tipo.STRENGTH, bodyweight),
                    buildExercise("Remada unilateral", Exercise.Tipo.STRENGTH, dumbbell),
                    buildExercise("Corrida leve", Exercise.Tipo.CARDIO, treadmill),
                    buildExercise("Prancha", Exercise.Tipo.STRENGTH, bodyweight)));
        }
    }

    private void seedUserIfNeeded() {
        if (userRepository.count() > 0) {
            return;
        }

        User user = new User();
        user.setFirstName("Demo");
        user.setLastName("User");
        user.setUsername("demo");
        user.setEmail("demo@gymtracker.local");
        user.setPassword(passwordEncoder.encode("123456"));
        userRepository.save(user);
    }

    private void seedPlannedWorkoutsForUsers() {
        List<Exercise> exercises = exerciseRepository.findAll();
        if (exercises.size() < 3) {
            return;
        }

        for (User user : userRepository.findAll()) {
            List<PlannedWorkout> planned = plannedWorkoutRepository.findByUserAndActiveTrueOrderByUpdatedAtDesc(user);
            if (!planned.isEmpty()) {
                continue;
            }

            PlannedWorkout upper = new PlannedWorkout();
            upper.setUser(user);
            upper.setDescription("Upper Focus");
            upper.setActive(true);
            upper = plannedWorkoutRepository.save(upper);

            PlannedWorkout cardio = new PlannedWorkout();
            cardio.setUser(user);
            cardio.setDescription("Cardio + Core");
            cardio.setActive(true);
            cardio = plannedWorkoutRepository.save(cardio);

            plannedWorkoutItemRepository.save(buildItem(upper, exercises.get(0), 1, 12, null, null));
            plannedWorkoutItemRepository.save(buildItem(upper, exercises.get(2), 2, 12, null, null));
            plannedWorkoutItemRepository.save(buildItem(upper, exercises.get(4), 3, 30, null, 45L));

            plannedWorkoutItemRepository.save(buildItem(cardio, exercises.get(3), 1, null, BigDecimal.valueOf(2.50), 900L));
            plannedWorkoutItemRepository.save(buildItem(cardio, exercises.get(1), 2, 15, null, null));
        }
    }

    private Exercise buildExercise(String description, Exercise.Tipo type, Equipment equipment) {
        Exercise exercise = new Exercise();
        exercise.setDescription(description);
        exercise.setType(type);
        exercise.setEquipment(equipment);
        return exercise;
    }

    private PlannedWorkoutItem buildItem(
            PlannedWorkout workout,
            Exercise exercise,
            int sequence,
            Integer repetitions,
            BigDecimal distance,
            Long durationSeconds) {
        PlannedWorkoutItem item = new PlannedWorkoutItem();
        item.setPlannedWorkout(workout);
        item.setExercise(exercise);
        item.setSequence(sequence);
        item.setRepetitions(repetitions);
        item.setDistance(distance);
        item.setDurationSeconds(durationSeconds);
        return item;
    }
}
