<script lang="ts">
  import Canvas from '$lib/components/Canvas.svelte';
  import CodeModal from '$lib/components/modals/CodeModal.svelte';
  import CreateOpModeModal from '$lib/components/modals/CreateOpModeModal.svelte';
  import OpModeSelectModal from '$lib/components/modals/OpModeSelectModal.svelte';
  import Sidebar from '$lib/components/Sidebar.svelte';
  import ToastContainer from '$lib/components/ToastContainer.svelte';
  import TopBar from '$lib/components/TopBar.svelte';
  import { onMount } from 'svelte';
  import { editorState } from '$lib/states/editor.svelte';
  import { flowGraphState } from '$lib/states/flowgraph.svelte';

  onMount(async () => {
    editorState.initWorkspace();
  });

  function handleKeydown(e: KeyboardEvent) {
    if (e.key === 'Delete' || e.key === 'Backspace') {
      const target = e.target as HTMLElement;
      if (
        target.tagName === 'INPUT' ||
        target.tagName === 'SELECT' ||
        target.tagName === 'TEXTAREA'
      )
        return;
      e.preventDefault();
      flowGraphState.deleteSelectedNode();
    }
  }
</script>

<svelte:window on:keydown={handleKeydown} />

<TopBar />

<main class="relative flex flex-1 overflow-hidden">
  <Sidebar />
  <Canvas />
</main>

<OpModeSelectModal />
<CreateOpModeModal />
<CodeModal />
<ToastContainer />
