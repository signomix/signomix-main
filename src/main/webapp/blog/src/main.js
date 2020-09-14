import App from './App.svelte';

let app = new App({
    target: document.body,
    props: {
        devModePort: '5000',
        defaultLanguage: 'pl',
        languages: ['pl','en'], 
        language: 'pl',
        docuid: '',
        csAPI: '/api/cs',
        cmsMode: false,
        texts: {'hello': 'Hello!',"navigation":{},"article":{}}
    }
});

export default app;