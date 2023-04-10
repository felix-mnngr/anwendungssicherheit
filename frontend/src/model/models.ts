export interface RequestBody {
    beschreibung: string;
    betrag: number;
}

export interface Konto {
    uuid: string;
    beschreibung: string;
    kontostand: number;
}

export interface KontoBewegung {
    beschreibung: string;
    betrag: number;
    datum: Date;
}