const httpFastServer = require('ozairjr/http-fast2')
const port = 3010
httpFastServer.startServer(port, () => console.log(`Listening on port ${port}`))