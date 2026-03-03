package com.org2.workout.backend.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.org2.workout.backend.dto.catalog.EquipmentDTO;
import com.org2.workout.backend.dto.catalog.ExerciseCatalogDTO;
import com.org2.workout.backend.dto.catalog.ExerciseCatalogUpsertDTO;
import com.org2.workout.backend.dto.catalog.MuscleDTO;
import com.org2.workout.backend.dto.catalog.NameOnlyUpsertDTO;
import com.org2.workout.backend.model.Equipment;
import com.org2.workout.backend.model.Exercise;
import com.org2.workout.backend.model.Muscle;
import com.org2.workout.backend.repository.CompletedWorkoutItemRepository;
import com.org2.workout.backend.repository.EquipmentRepository;
import com.org2.workout.backend.repository.ExerciseMuscleRepository;
import com.org2.workout.backend.repository.ExerciseRepository;
import com.org2.workout.backend.repository.ExerciseStatsRepository;
import com.org2.workout.backend.repository.MuscleRepository;
import com.org2.workout.backend.repository.MuscleStatsRepository;
import com.org2.workout.backend.repository.PlannedWorkoutItemRepository;

@Service
public class CatalogService {
    private final EquipmentRepository equipmentRepository;
    private final ExerciseRepository exerciseRepository;
    private final MuscleRepository muscleRepository;
    private final PlannedWorkoutItemRepository plannedWorkoutItemRepository;
    private final CompletedWorkoutItemRepository completedWorkoutItemRepository;
    private final ExerciseMuscleRepository exerciseMuscleRepository;
    private final ExerciseStatsRepository exerciseStatsRepository;
    private final MuscleStatsRepository muscleStatsRepository;

    public CatalogService(
            EquipmentRepository equipmentRepository,
            ExerciseRepository exerciseRepository,
            MuscleRepository muscleRepository,
            PlannedWorkoutItemRepository plannedWorkoutItemRepository,
            CompletedWorkoutItemRepository completedWorkoutItemRepository,
            ExerciseMuscleRepository exerciseMuscleRepository,
            ExerciseStatsRepository exerciseStatsRepository,
            MuscleStatsRepository muscleStatsRepository) {
        this.equipmentRepository = equipmentRepository;
        this.exerciseRepository = exerciseRepository;
        this.muscleRepository = muscleRepository;
        this.plannedWorkoutItemRepository = plannedWorkoutItemRepository;
        this.completedWorkoutItemRepository = completedWorkoutItemRepository;
        this.exerciseMuscleRepository = exerciseMuscleRepository;
        this.exerciseStatsRepository = exerciseStatsRepository;
        this.muscleStatsRepository = muscleStatsRepository;
    }

    public List<EquipmentDTO> listEquipments() {
        return equipmentRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Equipment::getDescription, String.CASE_INSENSITIVE_ORDER))
                .map(this::toEquipmentDTO)
                .toList();
    }

    public EquipmentDTO createEquipment(NameOnlyUpsertDTO request) {
        Equipment equipment = new Equipment();
        equipment.setDescription(normalizeDescription(request.getDescription()));
        Equipment saved = equipmentRepository.save(equipment);
        return toEquipmentDTO(saved);
    }

    public EquipmentDTO updateEquipment(Long id, NameOnlyUpsertDTO request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ferramenta nao encontrada"));
        equipment.setDescription(normalizeDescription(request.getDescription()));
        Equipment saved = equipmentRepository.save(equipment);
        return toEquipmentDTO(saved);
    }

    public void deleteEquipment(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ferramenta nao encontrada"));

        if (exerciseRepository.existsByEquipmentId(id)) {
            throw new RuntimeException("Ferramenta em uso por exercicios. Remova ou altere os exercicios primeiro.");
        }

        equipmentRepository.delete(equipment);
    }

    public List<MuscleDTO> listMuscles() {
        return muscleRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Muscle::getDescription, String.CASE_INSENSITIVE_ORDER))
                .map(this::toMuscleDTO)
                .toList();
    }

    public MuscleDTO createMuscle(NameOnlyUpsertDTO request) {
        Muscle muscle = new Muscle();
        muscle.setDescription(normalizeDescription(request.getDescription()));
        Muscle saved = muscleRepository.save(muscle);
        return toMuscleDTO(saved);
    }

    public MuscleDTO updateMuscle(Long id, NameOnlyUpsertDTO request) {
        Muscle muscle = muscleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Musculo nao encontrado"));
        muscle.setDescription(normalizeDescription(request.getDescription()));
        Muscle saved = muscleRepository.save(muscle);
        return toMuscleDTO(saved);
    }

    public void deleteMuscle(Long id) {
        Muscle muscle = muscleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Musculo nao encontrado"));

        exerciseMuscleRepository.deleteByMuscleId(id);
        muscleStatsRepository.deleteByMuscleId(id);
        muscleRepository.delete(muscle);
    }

    public List<ExerciseCatalogDTO> listExercises() {
        return exerciseRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Exercise::getDescription, String.CASE_INSENSITIVE_ORDER))
                .map(this::toExerciseDTO)
                .toList();
    }

    public ExerciseCatalogDTO createExercise(ExerciseCatalogUpsertDTO request) {
        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
                .orElseThrow(() -> new RuntimeException("Ferramenta nao encontrada"));

        Exercise exercise = new Exercise();
        exercise.setDescription(normalizeDescription(request.getDescription()));
        exercise.setType(parseType(request.getType()));
        exercise.setEquipment(equipment);
        Exercise saved = exerciseRepository.save(exercise);
        return toExerciseDTO(saved);
    }

    public ExerciseCatalogDTO updateExercise(Long id, ExerciseCatalogUpsertDTO request) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercicio nao encontrado"));
        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
                .orElseThrow(() -> new RuntimeException("Ferramenta nao encontrada"));

        exercise.setDescription(normalizeDescription(request.getDescription()));
        exercise.setType(parseType(request.getType()));
        exercise.setEquipment(equipment);
        Exercise saved = exerciseRepository.save(exercise);
        return toExerciseDTO(saved);
    }

    public void deleteExercise(Long id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercicio nao encontrado"));

        if (plannedWorkoutItemRepository.existsByExerciseId(id) || completedWorkoutItemRepository.existsByExerciseId(id)) {
            throw new RuntimeException("Exercicio em uso por treinos. Nao e possivel remover.");
        }

        exerciseMuscleRepository.deleteByExerciseId(id);
        exerciseStatsRepository.deleteByExerciseId(id);

        try {
            exerciseRepository.delete(exercise);
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Exercicio em uso por outros registros. Nao e possivel remover.");
        }
    }

    private EquipmentDTO toEquipmentDTO(Equipment equipment) {
        EquipmentDTO dto = new EquipmentDTO();
        dto.setId(equipment.getId());
        dto.setDescription(equipment.getDescription());
        return dto;
    }

    private MuscleDTO toMuscleDTO(Muscle muscle) {
        MuscleDTO dto = new MuscleDTO();
        dto.setId(muscle.getId());
        dto.setDescription(muscle.getDescription());
        return dto;
    }

    private ExerciseCatalogDTO toExerciseDTO(Exercise exercise) {
        ExerciseCatalogDTO dto = new ExerciseCatalogDTO();
        dto.setId(exercise.getId());
        dto.setDescription(exercise.getDescription());
        dto.setType(exercise.getType().name());
        dto.setEquipmentId(exercise.getEquipment().getId());
        dto.setEquipmentDescription(exercise.getEquipment().getDescription());
        return dto;
    }

    private String normalizeDescription(String description) {
        return description == null ? "" : description.trim();
    }

    private Exercise.Tipo parseType(String type) {
        try {
            return Exercise.Tipo.valueOf(type.trim().toUpperCase());
        } catch (Exception ex) {
            throw new RuntimeException("Tipo de exercicio invalido. Use STRENGTH ou CARDIO.");
        }
    }
}

