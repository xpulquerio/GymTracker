package com.org2.workout.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org2.workout.backend.model.CompletedWorkout;
import com.org2.workout.backend.model.User;

public interface CompletedWorkoutRespository extends JpaRepository<CompletedWorkout, Long> {
    List<CompletedWorkout> findByUserOrderByStartedAtDesc(User user);

    Optional<CompletedWorkout> findById(Long id);

    Optional<CompletedWorkout> findByIdAndUser(Long id, User user);

    Optional<CompletedWorkout> findFirstByUserAndFinishedAtIsNullOrderByStartedAtDesc(User user);

}
