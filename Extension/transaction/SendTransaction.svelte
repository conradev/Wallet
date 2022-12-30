<script lang="ts">
  import TouchButton from "./TouchButton.svelte";
</script>

<main>
  <div class="recipient">
    <img src="https://dummyimage.com/200x200/000/fff" alt="dummy"/>
    <span>conradkramer.eth</span>
  </div>
  <div class="primary">
    <span>$2.50</span>
  </div>
  <div class="info">
    <div class="info-row">
      <span class="label">Action</span>
      <code class="value">mint()</code>
    </div>
    <div class="info-row">
      <span class="label">Fee</span>
      <span class="value">$2.01</span>
    </div>
    <div class="info-row">
      <span class="label">Total</span>
      <span class="value">$4.51</span>
    </div>
  </div>
  <div class="buttons">
    <TouchButton className="cancel" text="Cancel"/>
    <TouchButton className="confirm" text="Sign"/>
  </div>
</main>

<style>
  main {
    display: flex;
    flex-direction: column;
    align-items: stretch;
    justify-content: space-between;
    height: 100%;
  }

  .recipient {
    font-size: 1.8em;
    font-weight: 600;
    flex: 0 0 auto;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.5em;
  }

  .recipient > img {
    height: 3em;
    border-radius: 50%;
    object-fit: cover;
  }

  .primary {
    font-size: 3.2em;
    font-weight: 700;
    display: flex;
    align-items: center;
    justify-content: center;
    flex: 1 1 auto;
  }

  .info {
    font-size: 1.5em;
    flex: 0 0 auto;
    display: flex;
    flex-direction: column;
    align-items: stretch;
    justify-content: space-between;
  }
  .info-row {
    display: flex;
    align-items: stretch;
    justify-content: space-between;
  }
  .info-row > .label {
    user-select: none;
    -webkit-user-select: none;
    font-weight: 600;
  }
  .info-row > .value {
    font-weight: 500;
  }

  .buttons {
    display: flex;
    align-items: stretch;
    justify-content: space-evenly;
    min-height: 40pt;
    user-select: none;
    flex: 0 0 auto;
  }
  :global(.cancel) {
    background: gray;
  }
  :global(.confirm) {
    background: rgb(0, 122, 255);
  }
</style>
