import App from './App.svelte';

let app = new App({
    target: document.body,
    props: {
        devModePort: '5000',
        language: 'en',
        docuid: '',
        csAPI: '/api/cs',
        texts: {'hello': 'Hello!',"navigation":{},"article":{}}
    }
});

export default app;