/*
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
route(function (id) {
    app.log('PAGE ID=' + id)
    switch (id) {
        case "dashboard":
            app.currentPage = "dashboard";
            globalEvents.trigger('pageselected:dashboard');
            break;
        case "account":
            if (app.user.name) {
                app.currentPage = "account";
                globalEvents.trigger('pageselected:account');
            } else {
                app.currentPage = "main";
                globalEvents.trigger('pageselected:main');
            }
            break;
        case "mydashboards":
            if (app.user.name && !app.user.guest) {
                app.currentPage = "mydashboards";
            } else {
                app.currentPage = "main";
                globalEvents.trigger('pageselected:main');
            }
            break;
        case "mydevices":
            if (app.user.name && !app.user.guest) {
                app.currentPage = "mydevices";
            } else {
                app.currentPage = "main";
                globalEvents.trigger('pageselected:main');
            }
            break;
        case "alerts":
            if (app.user.name && !app.user.guest) {
                app.currentPage = "alerts";
                globalEvents.trigger('pageselected:alerts');
            } else {
                app.currentPage = "main";
                globalEvents.trigger('pageselected:main');
            }
            break;
        case "login":
            app.currentPage = "login";
            globalEvents.trigger('pageselected:login');
            break;
        case "logout":
            app.currentPage = "logout";
            globalEvents.trigger('pageselected:logout');
            break;
        case "register":
            app.currentPage = "register";
            break;
        case "subscribe":
            app.currentPage = "subscribe";
            break;
        case "resetpass":
            app.currentPage = "resetPassword";
            break;
        case "unregister":
            app.currentPage = "unregister";
            break;
        case "legal":
            app.currentPage = "legal";
            break;
        case "":
            app.currentPage = "main";
            globalEvents.trigger('pageselected:main');
            break;
        case "main":
            app.currentPage = "main";
            globalEvents.trigger('pageselected:main');
            break;
        case "pl":
            app.language = 'pl'
            riot.mount('raw')
            riot.update()
            globalEvents.trigger('language')
            break;
        case "en":
            app.language = 'en'
            riot.mount('raw')
            riot.update()
            globalEvents.trigger('language')
            break;
        case "fr":
            app.language = 'fr'
            riot.mount('raw')
            riot.update()
            globalEvents.trigger('language')
            break;
        case "it":
            app.language = 'it'
            riot.mount('raw')
            riot.update()
            globalEvents.trigger('language')
            break;
        default:
            if (id.startsWith('dashboard,')) {
                app.currentPage = 'dashboard'
                app.log('DASHBOARD=' + id.substring(10))
                try {
                    app.user.dashboardID = id.substring(10)
                } catch (ex) {
                }
                globalEvents.trigger('pageselected:dashboard');
            } else if (id.startsWith('doc')) {
                app.currentPage = 'documents'
                app.docPath = id.substring(3).replace(/,/g, "/");
                globalEvents.trigger('pageselected:doc');
            } else if (id.startsWith('blog')) {
                app.currentPage = 'blog'
                app.docPath = id.substring(4).replace(/,/g, "/");
                globalEvents.trigger('pageselected:blog');
            } else if (id.startsWith('help')) {
                app.currentPage = 'help'
                app.docPath = '/' + id.replace(/,/g, "/");
                globalEvents.trigger('pageselected:help');
            } else {
                app.currentPage = "main";
                globalEvents.trigger('pageselected:main');
            }
            route(id)
            break;
    }
    globalEvents.trigger('pageselected');
    riot.update();
})