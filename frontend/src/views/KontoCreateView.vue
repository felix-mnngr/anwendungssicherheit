<template>
  <p>
    <hr>
    <h4 class="mb-3">Hier können Sie ein Konto erstellen:</h4>
  </p>

  <div class="form-outline">
    <input type="text" id="typeText" class="form-control" v-model="beschreibung" />
    <label class="form-label" for="typeText">Name des neuen Kontos</label>
  </div>
    <br>
  <button type="button" class="btn btn-outline-primary" data-mdb-ripple-color="dark" @click="createKonto()">Konto erstellen</button>
  <button type="button" class="btn btn-outline-secondary" data-mdb-ripple-color="dark" @click="$router.push('/')">Zurück zur Startseite</button> 
</template>

<script lang="ts">
import type { Konto, RequestBody } from '@/model/models';
import { createKonto } from '@/service/api';
import { defineComponent } from 'vue'

export default defineComponent({
  // type inference enabled
  data() {
    return {
      konten: [] as Konto[],
      beschreibung: ""
    }
  },
  methods: {
    createKonto() {
      createKonto({ beschreibung: this.beschreibung} as RequestBody).then(() => this.$router.push('/')).catch(error => console.log(error))
    }
  }
})
</script>
