package de.hsaalen.cloudcomputing.repository;

import com.google.cloud.bigtable.data.v2.models.Filters;
import de.hsaalen.cloudcomputing.domain.Konto;
import de.hsaalen.cloudcomputing.repository.annotation.Column;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class RepositoryTest {

    private static final String EMAIL = "test@test.test";
    private static final String BESCHREIBUNG = "Test";
    private static final LocalDateTime DATE_TIME = LocalDateTime.now();

    @Inject
    Repository<Konto> kontoRepository;

    @Test
    public void testRepository_CRUD() {
        // Create
        Konto konto = kontoRepository.create(new Konto(EMAIL, BESCHREIBUNG));
        assertNotNull(konto.getUuid());
        assertEquals(EMAIL, konto.getEmailKontoinhaber());
        assertEquals(BESCHREIBUNG, konto.getBeschreibung());
        assertNull(konto.getKontobewegung());

        // Update
        konto.setKontobewegung(Collections.singletonList(new Konto.Kontobewegung(BigDecimal.TEN, BESCHREIBUNG, DATE_TIME)));
        konto.setKontostand(BigDecimal.TEN);
        kontoRepository.update(konto);

        // GetById
        Optional<Konto> kontoOptional = kontoRepository.findById(konto.getUuid());
        assertTrue(kontoOptional.isPresent());
        assertEquals(konto.getUuid(), kontoOptional.get().getUuid());
        assertEquals(EMAIL, kontoOptional.get().getEmailKontoinhaber());
        assertEquals(BigDecimal.TEN, kontoOptional.get().getKontostand());

        // GetByFilter
        Optional<Konto> kontoOptional1 = kontoRepository.findByIdWithFilter(konto.getUuid(), Filters.FILTERS.pass());
        assertTrue(kontoOptional1.isPresent());
        assertEquals(BESCHREIBUNG, kontoOptional1.get().getKontobewegung().get(0).getBeschreibung());
        assertEquals(BigDecimal.TEN, kontoOptional1.get().getKontobewegung().get(0).getBetrag());
        assertEquals(DATE_TIME, kontoOptional1.get().getKontobewegung().get(0).getZeitpunkt());

        // Delete
        kontoRepository.deleteById(konto.getUuid());

        // GetById
        kontoRepository.findById(konto.getUuid()).ifPresent(ignored -> fail("Es wurde ein Konto gefunden."));
    }

    @Test
    public void testRepository_findByQuery() {
        Konto konto1 = kontoRepository.create(new Konto(EMAIL, BESCHREIBUNG + "1"));
        Konto konto2 = kontoRepository.create(new Konto(EMAIL, BESCHREIBUNG + "2"));
        Konto konto3 = kontoRepository.create(new Konto("other.user@test.test", BESCHREIBUNG));

        List<Konto> kontoList = kontoRepository.findByFilter(Filters.FILTERS.condition(Filters.FILTERS
                        .chain()
                        .filter(Filters.FILTERS.qualifier().exactMatch(Konto.EMAIL_KONTOINHABER_PROPERTY))
                        .filter(Filters.FILTERS.value().exactMatch(RepositoryUtils.mapObjectToByteArrayString(EMAIL))))
                .then(Filters.FILTERS.family().exactMatch(Column.DEFAULT_FAMILY))
        );

        assertEquals(2, kontoList.size());

        kontoRepository.deleteById(konto1.getUuid());
        kontoRepository.deleteById(konto2.getUuid());
        kontoRepository.deleteById(konto3.getUuid());
    }

}
