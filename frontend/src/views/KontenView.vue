<template>
 <p>
  <hr>
<h4 class="mb-3">Hier können Sie ihre Konten einsehen:</h4>
</p>

<table class="table">
  <thead>
    <tr>
      <th scope="col">Name des Kontos</th>
      <th scope="col">Aktueller Kontostand</th>
      <th scope="col">Aktionen</th>
    </tr>
  </thead>
  <tbody class="table-group-divider table-divider-color">
    <tr v-for="konto of konten">
      <td>{{ konto.beschreibung }}</td>
      <td>{{ konto.kontostand }}€</td>
      <td>
        <RouterLink :to="'/add?uuid=' + konto.uuid" class="btn btn-link" >Buchung durchführen</RouterLink>
        <RouterLink :to="'/bewegungen?uuid=' + konto.uuid" class="btn btn-link" >Buchungen einsehen</RouterLink>
        <a class="btn btn-outline-secondary" data-mdb-ripple-color="dark" @click="deleteKonto(konto.uuid)">Löschen</a>       
      </td>
    </tr>
  </tbody>
</table>
  <button type="button" class="btn btn-outline-secondary" data-mdb-ripple-color="dark" @click="$router.push('/')">Zurück</button> 
</template>

<script lang="ts">
import type { Konto } from '@/model/models';
import { getKonten, deleteKonto } from '@/service/api';
import { defineComponent } from 'vue'

export default defineComponent({
  // type inference enabled
  data() {
    return {
      konten: [] as Konto[]
    }
  },
  methods: {
    getKonten() {
      getKonten().then(konten => this.konten = konten).catch(error => console.log(error))
    },
    deleteKonto(id: string) {
      deleteKonto(id).then(() => this.$forceUpdate()).catch(error => console.log(error))
    }
  },
  beforeMount() {
    this.getKonten()
  }
})
</script>
