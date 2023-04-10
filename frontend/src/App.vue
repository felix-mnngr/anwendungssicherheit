<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <div class="container">
      <div class="d-flex justify-content-center align-items-center" style="height: 100vh">
        <div class="text-center">
          <RouterView />
        </div>
      </div>
    </div>
</template>

<style scoped>
#app {
  font-family: Roboto, Helvetica, Arial, sans-serif;
}
</style>
