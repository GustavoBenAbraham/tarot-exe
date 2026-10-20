package com.tarotexe.domain;

import java.time.LocalDate;
import java.time.LocalTime;

/** Optional consultation context; never an input to the random engine. */
public record ConsultantProfile(String name, LocalDate birthDate, LocalTime birthTime, String birthCity, ZodiacSign sign) {
    public ConsultantProfile {
        name = name == null ? "" : name.strip();
        birthCity = birthCity == null ? "" : birthCity.strip();
        if (name.length() > 80) throw new IllegalArgumentException("Nome deve ter até 80 caracteres.");
        if (birthCity.length() > 120) throw new IllegalArgumentException("Cidade deve ter até 120 caracteres.");
        if (birthTime != null && birthDate == null) throw new IllegalArgumentException("Informe a data de nascimento junto do horário.");
    }
    public static ConsultantProfile empty() { return new ConsultantProfile("", null, null, "", null); }
}
