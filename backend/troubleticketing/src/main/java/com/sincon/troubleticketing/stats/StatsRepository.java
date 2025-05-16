package com.sincon.troubleticketing.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<Stats, Long> {

    List<Stats> findByDateBetween(LocalDate startDate, LocalDate endDate);

    Stats findFirstByOrderByDateDesc();
}
