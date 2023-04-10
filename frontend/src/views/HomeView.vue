<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>

<template>
  <main>
    <h2 class="mb-3">Vielen Dank, dass Sie unseren Budgetplanner nutzen!</h2>
          <hr>
          <br>
          <h4 class="mb-3">Was möchten Sie tun?</h4>
          <br>
          <div class="dropdown">
            <a
              class="btn btn-primary dropdown-toggle"
              href="#"
              role="button"
              id="dropdownMenuLink"
              data-mdb-toggle="dropdown"
              aria-expanded="false"
            >
              Aktion auswählen
            </a>
            <ul class="dropdown-menu" aria-labelledby="dropdownMenuLink">
              <li><RouterLink class="dropdown-item" to="/create" >Konto erstellen</RouterLink></li>
              <li><RouterLink class="dropdown-item" to="/konten" >Sichteinlagen meiner Konten</RouterLink></li>
            </ul>
          </div>
  </main>
</template>
