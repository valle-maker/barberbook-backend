package com.udea.barberbook.barberbook_backend.config;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Hibernate (ddl-auto=update) creates a CHECK constraint listing the allowed values of an enum
// column when it creates the table, and never updates it afterwards. Adding a value to one of
// these enums would then fail at insert time on every database created before the change (local
// and the deployed one). The columns below are the enums that grow with the features, so their
// constraints are dropped at startup (idempotent); the application validates the values itself.
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class EnumConstraintRelaxer implements ApplicationRunner {

    private record EnumColumn(String table, String column) {}

    private static final List<EnumColumn> GROWING_ENUMS = List.of(
        new EnumColumn("audit_logs", "event_type"),
        new EnumColumn("appointments", "status"),
        new EnumColumn("notifications", "type")
    );

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        for (EnumColumn target : GROWING_ENUMS) {
            String constraint = target.table() + "_" + target.column() + "_check";
            try {
                jdbcTemplate.execute("ALTER TABLE " + target.table() + " DROP CONSTRAINT IF EXISTS " + constraint);
            } catch (RuntimeException e) {
                // e.g. the table doesn't exist yet or the database isn't PostgreSQL: nothing to relax.
                log.debug("No se pudo relajar {}: {}", constraint, e.getMessage());
            }
        }
    }
}
