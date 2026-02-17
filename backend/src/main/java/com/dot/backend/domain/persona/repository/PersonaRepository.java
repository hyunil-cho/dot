package com.dot.backend.domain.persona.repository;

import com.dot.backend.domain.persona.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {

    // 활성 Persona 조회 (삭제되지 않은 것만)
    @Query("SELECT p FROM Persona p WHERE p.user.id = :userId AND p.isDeleted = false")
    List<Persona> findActiveByUserId(@Param("userId") Long userId);

    // 삭제된 Persona 조회 (복원 기능용)
    @Query("SELECT p FROM Persona p WHERE p.user.id = :userId AND p.isDeleted = true")
    List<Persona> findDeletedByUserId(@Param("userId") Long userId);

    // 전화번호로 Persona 조회
    @Query("SELECT p FROM Persona p WHERE p.user.id = :userId AND p.phoneNumber = :phoneNumber AND p.isDeleted = false")
    Optional<Persona> findByUserIdAndPhoneNumber(@Param("userId") Long userId, @Param("phoneNumber") String phoneNumber);

    // ID와 User로 조회 (권한 검증용)
    @Query("SELECT p FROM Persona p WHERE p.id = :id AND p.user.id = :userId AND p.isDeleted = false")
    Optional<Persona> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    // 30일 경과된 삭제 Persona 조회 (배치 작업용)
    @Query("SELECT p FROM Persona p WHERE p.isDeleted = true AND p.deletedAt < :threshold")
    List<Persona> findExpiredDeletedPersonas(@Param("threshold") LocalDateTime threshold);

    // 전화번호 중복 확인
    boolean existsByUserIdAndPhoneNumberAndIsDeletedFalse(Long userId, String phoneNumber);

    // AI Job ID로 Persona 조회 (Webhook 처리용)
    Optional<Persona> findByLastTrainingJobId(String jobId);

    // 사용자의 모든 Persona 삭제 (회원 탈퇴용)
    @Modifying
    @Query("DELETE FROM Persona p WHERE p.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}


