/**
 * Краткая имитация RequireJS для полностью оптимизированных проектов
 */
var requirejs, require, define;

(function () {
    var modules = {
        config: {
            i18n: {
                locale: 'ru'
            }
        }
    },
    ObjProto = Object.prototype,
    toString = ObjProto.toString;

    function isFunction(it) {
        return toString.call(it) == '[object Function]';
    }

    function isArray(it) {
        return toString.call(it) == '[object Array]';
    }
    
    function find(deps, module) {
        var imports = [];
        if (!isArray(deps)) deps = [].concat(deps);
        for (var i = 0, l = deps.length; i < l; i++) {
            var id = deps[i];
            if (id == 'module') {
                imports.push(module);
            } else if (id == 'require') {
                imports.push(require);
            } else if (id == 'exports') {
                imports.push(module.exports);
            } else {
                if (!modules[id]) {
                    var j = id.indexOf('!');
                    if (j !== -1) {
                        var prefix = id.substring(0, j), 
                        plugin = require(prefix),
                        name = id.substring(j + 1, id.length);
                        plugin.load(name, require, function (value) {
                            define(id, value);
                        }, modules.config ? modules.config[prefix] : undefined );
                    } 
                }
                if (!modules[id]) {
                    throw "module " + id + " not found";
                }
                imports.push(modules[id].exports);
            }
        }
        return imports;
    }

    requirejs = require = function (deps, factory) {
        if (!isArray(deps) && typeof deps !== 'string') {
            // deps is a config object
            for (var key in deps) {
                modules[key] = deps[key];
            }
        } else {
            var imports = find(deps);
            if (isFunction(factory)) {
                factory.apply(this, imports);
            }
            return (imports.length == 1 ? imports[0] : undefined);
        }
    };

    define = function (id, deps, factory) {
        if (modules[id]) {
            throw "module " + id + " already defined";
        }
        var module = modules[id] = {
            id: id,
            exports: {},
            require: require,
            config: moduleconfig
        };
        if (!isArray(deps)) {
            factory = deps;
            if (isFunction(factory)) {
                factory(require, module.exports, module);
            } else {
                module.exports = factory;
            }
        } else {
            var imports = find(deps, module);
            module.exports = factory.apply(module, imports)
        }
    };

    define.remove = function (id) {
        delete modules[id];
    };

    require.config = function (config) {
        if (config) require(config);
    };

    define.amd = {
        jQuery: true
    };

    moduleconfig = function () {
        return modules.config;
    }
})();

//Export for use in node
if (typeof module === "object" && typeof require === "function") {
    module.exports.require = require;
    module.exports.define = define;
}
