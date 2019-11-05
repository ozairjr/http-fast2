const HttpFast = Java.type('br.com.softbox.thrust.httpfast.HttpFast')
const resources = {
  routesFilePath: `${__ROOT_DIR__}/routes.js`,
  middlewaresFilePath: null,
  afterRequestFnFilePath: null
}
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
const toNumberInteger = (n,defaultValue=null) => Number.isInteger(Number(n)) ? Number(n) : defaultValue
const startServer = (port, minThreads, maxThreads, callback) => {
	callback = [port, minThreads, maxThreads, callback].reduce((fn, obj) => !fn && typeof obj === 'function'
		? obj
		: fn, 
	null)
	port = toNumberInteger(port)
	minThreads = toNumberInteger(minThreads, 8)
	maxThreads = toNumberInteger(maxThreads, minThreads)
	const httpServer = HttpFast.startServer(minThreads, maxThreads, __ROOT_DIR__, resources.routesFilePath, resources.middlewaresFilePath, resources.afterRequestFnFilePath)
	if (callback) {
		callback(httpServer)
	}
    httpServer.go(port)
}
const stopServer = () => {
    const httpServer = HttpFast.getInstance()
    if (!httpServer) {
        throw new Error('HttpFastServer was not initiated')
    }
    httpServer.stopServer()
}

exports = {
    setRoutesFilePath,
    setMiddlewaresFilePath,
    setAfterRequestFnFilePath,
    startServer,
    stopServer,
}
