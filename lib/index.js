const HttpFast = Java.type('br.com.softbox.thrust.httpfast.HttpFast')

const resources = {
  routesFilePath: `${__ROOT_DIR__}/routes.js`,
  middlewaresFilePath: null,
  afterRequestFnFilePath: null
};

const setRoutesFilePath = routesFP => {
    if (routesFP) {
        resources.routesFilePath = `${__ROOT_DIR__}/${routesFP}`
    }
}

const setMiddlewaresFilePath = middlewaresFP => {
    if (middlewaresFP) {
        resources.middlewaresFilePath = `${__ROOT_DIR__}/${middlewaresFP}`
    }
}

const setAfterRequestFnFilePath = afterRequestFnFP => {
    if (afterRequestFnFP) {
        resources.afterRequestFnFilePath = `${__ROOT_DIR__}/${afterRequestFnFP}`
    }
}

const startServer = (port, minThreads, maxThreads) => {
    const threads = minThreads || 8
    maxThreads = maxThreads || minThreads
    const httpServer = new HttpFast(threads, maxThreads, __ROOT_DIR__, resources.routesFilePath, resources.middlewaresFilePath, resources.afterRequestFnFilePath)
    httpServer.go(port)
}

exports = {
    setRoutesFilePath,
    setMiddlewaresFilePath,
    setAfterRequestFnFilePath,
    startServer
}
