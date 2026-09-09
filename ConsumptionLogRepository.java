package com.beanbrew.repository;

import com.beanbrew.entity.ConsumptionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ConsumptionLogRepository extends JpaRepository<ConsumptionLog, Long> {

    List<ConsumptionLog> findByMachineId(String machineId);

    List<ConsumptionLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<ConsumptionLog> findByMachineIdAndTimestampBetween(String machineId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT c FROM ConsumptionLog c WHERE c.timestamp >= :start ORDER BY c.timestamp DESC")
    List<ConsumptionLog> findRecentSince(@Param("start") LocalDateTime start);

    @Query("SELECT COALESCE(SUM(c.cupsDispensed),0) FROM ConsumptionLog c WHERE c.timestamp BETWEEN :start AND :end")
    Long sumCupsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(c.cupsDispensed),0) FROM ConsumptionLog c WHERE c.machineId = :machineId AND c.timestamp BETWEEN :start AND :end")
    Long sumCupsForMachineBetween(@Param("machineId") String machineId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
