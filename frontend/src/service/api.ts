import type { Konto, KontoBewegung, RequestBody } from "@/model/models";
import axios from 'axios';

const apiUrl = `/api/konto`

export async function getKonten(): Promise<Konto[]> {
    const response = await axios.get(apiUrl);
    return await response.data as Konto[];
}

export async function createKonto(body: RequestBody) {
    const response = await axios.post(apiUrl, {beschreibung: body.beschreibung})
    return await response.data;
}

export async function getKontobewegungen(uuid: string): Promise<KontoBewegung[]> {
    const response = await axios.get(`${apiUrl}/bewegungen/${uuid}`);
    for(let element of response.data) {
        element.datum = new Date(element.timestamp * 1000);
    }
    return await response.data as KontoBewegung[];
}

export async function createKontoBewegung(uuid: string, body: RequestBody) {
    const response = await axios.post(`${apiUrl}/bewegungen/${uuid}`, body)
    return await response.data;
}

export async function deleteKonto(uuid: string) {
    const response = await axios.delete(`${apiUrl}/${uuid}`)
    return await response.data;
}
