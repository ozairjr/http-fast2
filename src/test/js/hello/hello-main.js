const httpFastServer = require('ozairjr/http-fast2')
httpFastServer.setRoutesFilePath('hello-routes.js')
try {
	const port = 3000
	httpFastServer.startServer(port, 1,2, () => console.log(`Listening on port ${port}`))
} catch (e) {
	console.error('Error from httpFastServer.startServer')
	console.error(e)
} finally {
	console.log('hello-main finished.')
}
