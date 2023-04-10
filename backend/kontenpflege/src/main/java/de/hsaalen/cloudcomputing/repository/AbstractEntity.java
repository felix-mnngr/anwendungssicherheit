package de.hsaalen.cloudcomputing.repository;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class AbstractEntity {

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private UUID uuid;
}
