/**
 * This plugin handles applet! prefixed modules. It load java applets
 * 
 * Copyright (c) 2013 POSkiosk Team
 * 
 * Changes:
 * 
 * 27.03.2013 Maxim Ryabochkin Project skeleton
 * 
 */

define(['module', 'jquery'], function (module, $) {
   
    function loadApplet(parsedName, onload) {
        var body = document.getElementsByTagName('body')[0],
        node = document.createElement('div');
        node.width = 1;
        node.height = 1;
        body.appendChild(node);
        var params; 
        if (parsedName.code) {
            params = '<param name="archive" value="' + parsedName.modName +'"/>'+
            '<param name="code" value="' + parsedName.code + '"/>'
        } else {
            params = '<param name="jnlp_href" value="' + parsedName.modName + '.jnlp"/>'
        }
        node.innerHTML = '<object type="application/x-java-applet" height="1" width="1">' + 
        params + '</object>';
        onload(node.firstChild);
    }
   
    function parseName(name) {
        var code, index = name.indexOf('!'), 
        modName = name, ext;
        if (index !== -1) {
            code = modName.substring(index + 1, modName.length).replace('/', '.').replace('/', '.');
            modName = modName.substring(0, index);
        } else {
            index = modName.indexOf('.');
            if (index !== -1) {
                ext = name.substring(index + 1, modName.length);
                modName = modName.substring(0, index);
            }
        }
        return {
            modName: modName,
            ext: ext,
            code: code
        };
    }


    return {
        version: '1.0.0',

        load: function (name, req, onload, config) {
            $(function () {
                loadApplet(parseName(name), onload);
            });
        }
    }
    
});

