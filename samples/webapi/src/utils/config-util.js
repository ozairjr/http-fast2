const fs = require('filesystem')

const CONFIG_JSON = `${__ROOT_DIR__}/config.json`
const configJson = {}

const getConfig = () => {
    if (!configJson.loaded) {
        configJson.json = fs.exists(CONFIG_JSON)
            ? fs.readJson(CONFIG_JSON, 'utf-8')
            : {}
        configJson.loaded = true
    }
    return configJson.json
}

const getDatabaseConfig = () => getConfig().database
const getServerConfig = () => getConfig().server

exports = {
    getConfig,
    getDatabaseConfig,
    getServerConfig,
}