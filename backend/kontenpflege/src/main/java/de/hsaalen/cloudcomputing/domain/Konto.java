package de.hsaalen.cloudcomputing.domain;

import de.hsaalen.cloudcomputing.repository.AbstractEntity;
import de.hsaalen.cloudcomputing.repository.annotation.Column;
import de.hsaalen.cloudcomputing.repository.annotation.Entity;
import de.hsaalen.cloudcomputing.repository.annotation.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Konto extends AbstractEntity {

    public final static String EMAIL_KONTOINHABER_PROPERTY = "emailKontoinhaber";
    public final static String KONTOBEWEGUNG_FAMILY = "kontobewegung";

    @Column
    private String emailKontoinhaber;

    @Column
    private BigDecimal kontostand;

    @Column
    private String beschreibung;

    @OneToMany(family = KONTOBEWEGUNG_FAMILY, columnNamePrefix = "kontobewegung-")
    private List<Kontobewegung> kontobewegung;

    public Konto(String emailKontoinhaber, String beschreibung) {
        this.emailKontoinhaber = emailKontoinhaber;
        this.beschreibung = beschreibung;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Kontobewegung implements Serializable {
        private BigDecimal betrag;
        private String beschreibung;
        private LocalDateTime zeitpunkt;
    }
}
