const JString = Java.type('java.lang.String')
const StandardCharsets = Java.type('java.nio.charset.StandardCharsets')

const sendData = (channel, data) => {
    while (data.hasRemaining()) {
        channel.write(data)
    }
}

function mountResponse(channel) {
    const headerReturned = (headers) => {
        if (!headers) {
            return ''
        }

        return Object
            .keys(headers)
            .reduce((acc, header) => {
                acc += `${header}: ${headers[header]}\r\n`
                return acc
            }, '')
    }
    let response = {
        httpResponse: channel,
        status: 200,
        contentLength: 0,
        contentType: 'text/html',
        charset: 'UTF-8',
        headers: {},

        clean: function () {
            this.headers = {}
            this.contentLength = 0
            this.contentType = 'text/html'
            this.charset = 'utf-8'
        },

        /**
         * Escreve em formato *texto* o conteúdo passado no parâmetro *content* como resposta
         * a requisição. Modifica o valor do *content-type* para *'text/html'*.
         * @param {Object} data - dado a ser enviado para o cliente.
         * @param {Number} statusCode - (opcional) status de retorno do request htttp.
         * @param {Object} headers - (opcional) configurações a serem definidas no header http.
         */
        write: function (content) {
            this.html(content)
        },

        plain: function (content) {
            let sizeBody = (StandardCharsets.UTF_8.encode(content)).remaining()
            let response = new JString(`HTTP/1.1 200 OK\r\nDate: ${new Date().toString()}\r\nContent-Type: text/plain\r\nContent-Length: ${sizeBody}\r\nServer: thrust\r\nConnection: close\r\n${headerReturned(this.headers)}\r\n${content}`)

            sendData(channel, StandardCharsets.UTF_8.encode(response))
        },

        json: function (data, headers) {
            let body = (typeof (data) === 'object') ? JSON.stringify(data) : data
            let sizeBody = (StandardCharsets.UTF_8.encode(body)).remaining()
            let response = new JString(`HTTP/1.1 200 OK\r\nDate: ${new Date().toString()}\r\nContent-Type: application/json\r\nContent-Length: ${sizeBody}\r\nServer: thrust\r\nConnection: close\r\n${headerReturned(this.headers)}\r\n${body}`)

            sendData(channel, StandardCharsets.UTF_8.encode(response))
        },

        html: function (content) {
            let sizeBody = (StandardCharsets.UTF_8.encode(content)).remaining()
            let response = new JString(`HTTP/1.1 200 OK\r\nDate: ${new Date().toString()}\r\nContent-Type: text/html\r\nContent-Length: ${sizeBody}\r\nServer: thrust\r\nConnection: close\r\n${headerReturned(this.headers)}\r\n${content}`)

            sendData(channel, StandardCharsets.UTF_8.encode(response))
        },

        binary: function (content) {
            let sizeBody = (StandardCharsets.UTF_8.encode(content)).remaining()
            let response = new JString(`HTTP/1.1 200 OK\r\nDate: ${new Date().toString()}\r\n'Content-Type: application/octet-stream\r\nContent-Length: ${sizeBody}\r\nServer: thrust\r\nConnection: close\r\n${headerReturned(this.headers)}\r\n${content}`)

            sendData(channel, StandardCharsets.UTF_8.encode(response))
        },

        /**
         * Objeto que encapsula os métodos de retornos quando ocorre um erro na requisição http.
         * @ignore
         */
        error: {
            /**
             * Escreve em formato *JSON* uma mensagem de erro como resposta a requisição no
             * formato {message: *message*, status: *statusCode*}. Modifica o valor
             * do *content-type* para *'application/json'*.
             * @alias error.json
             * @memberof! http.Response#
             * @instance error.json
             * @param {String} message - mensagem de erro a ser enviada no retorno da chamada do browser.
             * @param {Number} statusCode - (opcional) status de retorno do request htttp.
             * @param {Object} headers - (opcional) configurações a serem definidas no header http.
             */
            json: function (message, statusCode, headers) {
                let code = statusCode || 200
                let body = JSON.stringify({
                    status: statusCode,
                    message: message
                })
                let textResponse = `HTTP/1.1 ${code}\r\nDate: ${new Date().toString()}\r\nContent-Type: application/json\r\nConnection: close\r\n${headerReturned(this.headers)}`

                for (let opt in (headers || {})) {
                    textResponse += opt + ': ' + headers[opt] + '\r\n'
                }

                textResponse += '\r\n' + body
                sendData(channel, StandardCharsets.UTF_8.encode(textResponse))
            }
        }
    }

    return response
}

exports = mountResponse
