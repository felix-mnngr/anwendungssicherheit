<template>
 <table class="table">
  <thead>
    <tr>
      <th scope="col">Bezeichnung</th>
      <th scope="col">Betrag</th>
      <th scope="col">Datum</th>
    </tr>
  </thead>
  <tbody class="table-group-divider table-divider-color">
    <tr v-for="bewegung of bewegungen">
      <td>{{ bewegung.beschreibung }}</td>
      <td>{{ bewegung.betrag }}€</td>
      <td>{{ bewegung.datum.toLocaleTimeString("de-DE", { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }) }}</td>
    </tr>
  </tbody>
</table>
  <button type="button" class="btn btn-outline-secondary" data-mdb-ripple-color="dark" @click="$router.push('/konten')">Zurück</button> 
</template>

<script lang="ts">
import type { KontoBewegung } from '@/model/models';
import { getKontobewegungen } from '@/service/api';
import { defineComponent } from 'vue'

export default defineComponent({
  // type inference enabled
  data() {
    return {
      bewegungen: [] as KontoBewegung[]
    }
  },
  methods: {
    getKontoBewegungen() {
      getKontobewegungen(this.$route.query.uuid as string).then(bewegungen => {
        this.bewegungen = bewegungen;
      }).catch(error => console.log(error))
    }
  },
  beforeMount() {
    this.getKontoBewegungen()
  }
})
</script>
