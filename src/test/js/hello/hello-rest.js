exports = {
    hello(params, req, res) {
        const name = params.name
        if (!name) {
            return res.status(500).json({
                err: 1,
                msg: 'No name'
            })
        }
        if (req.headers['x-name']) {
            return res.plain(`|${name}|`)
        }
        res.json({
            hello: name
        })
    },
    bye(params, _, res) {
        try {
            const key = Number(params.key) || 0
            if (key !== 1) {
                res.status(500).json({
                    err: 'invalid key'
                })
            } else {
                const httpFast = require('ozairjr/http-fast2')
                httpFast.stopServer()
                res.status(202).sendNoContent()
            }
        } catch (e) {
            console.error(e)
            res.status(500).json({
                msg: e.message
            })
        }
        
    }
}
