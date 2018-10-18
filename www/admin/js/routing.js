/*
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
route(function(id){
    switch (id){
        case "login":
            app.currentPage = "login";
            //globalEvents.trigger('pageselected:login');
            break;
        case "logout":
            app.currentPage = "logout";
            //globalEvents.trigger('pageselected:logout');
            break;
        case "documents":
            if(app.user.role.includes('redactor')){
                app.currentPage = "documents";
                //globalEvents.trigger('pageselected:documents');
            }else{
                app.currentPage = "main";
            }
            break;
        case "tags":
            if(app.user.role.includes('redactor')){
                app.currentPage = "tags";
            }else{
                app.currentPage = "main";
            }
            //globalEvents.trigger('pageselected:tags');
            break;
        case "users":
            if(app.user.role.includes('admin')){
                app.currentPage = "users";
            }else{
                app.currentPage = "main";
            }
            //globalEvents.trigger('pageselected:users');
            break;
        case "":
            app.currentPage = "main";
            globalEvents.trigger('pageselected:main');
            break;
        case "main":
            app.currentPage = "main";
            //globalEvents.trigger('pageselected:main');
            break;
        case "pl":
            app.language = 'pl'
            riot.mount('raw')
            riot.update()
            break;
        case "en":
            app.language = 'en'
            riot.mount('raw')
            riot.update()
            break;
        default:
            console.log('DEFAULT ROUTE')
            break;
    }
    riot.update();
})

