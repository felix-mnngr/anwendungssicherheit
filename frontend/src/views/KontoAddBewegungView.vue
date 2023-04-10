<template>
<div class="form-check">
  <input class="form-check-input" type="radio" name="flexRadioDefault" id="flexRadioDefault1" v-model="vorzeichen" value="1"/>
  <label class="form-check-label" for="flexRadioDefault1"> Betrag zubuchen </label>
</div>

<div class="form-check">
  <input class="form-check-input" type="radio" name="flexRadioDefault" id="flexRadioDefault2" v-model="vorzeichen" value="-1"/>
  <label class="form-check-label" for="flexRadioDefault2"> Betrag abbuchen </label>
</div>
<br>

<div class="form-outline">
  <input type="text" id="form12" class="form-control" v-model="beschreibung"/>
  <label class="form-label" for="form12">Bezeichnung der Buchung eingeben</label>
</div>

<br>
<div class="form-outline">
  <input type="number" id="form12" class="form-control" v-model="betrag"/>
  <label class="form-label" for="form12">Betrag der Buchung eingeben</label>
</div>
<br>

<button type="button" class="btn btn-outline-primary" data-mdb-ripple-color="dark" @click="addBewegung()">Betrag buchen</button>
  <button type="button" class="btn btn-outline-secondary" data-mdb-ripple-color="dark" @click="$router.push('/konten')">Zurück</button> 
</template>

<script lang="ts">
import type { Konto, RequestBody } from '@/model/models';
import { createKontoBewegung } from '@/service/api';
import { defineComponent } from 'vue'

export default defineComponent({
  // type inference enabled
  data() {
    return {
      konten: [] as Konto[],
      beschreibung: "",
      betrag: undefined as unknown,
      vorzeichen: -1
    }
  },
  methods: {
    addBewegung() {
      createKontoBewegung(this.$route.query.uuid as string, {beschreibung: this.beschreibung, betrag: this.betrag as number * this.vorzeichen}).then(() => this.$router.push('/konten'))
      .catch(error => console.log(error))
    }
  }
})
</script>
