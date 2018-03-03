package com.quantal.javashared.convertors.jpa;

import javax.persistence.AttributeConverter;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LocalDateJpaConverter implements AttributeConverter <LocalDate, Timestamp> {
    @Override
    public Timestamp convertToDatabaseColumn(LocalDate localDate) {
        return localDate != null ? Timestamp.valueOf(localDate.atStartOfDay()) : null;
    }


    @Override
    public LocalDate convertToEntityAttribute(Timestamp dbData) {
        return dbData != null ? dbData.toLocalDateTime().toLocalDate() : null;
    }
}
