const parseParams = (strParams, contentType) => {
    let params = {}

    const parseValue = (value) => {
        if (value === 'true') {
            return true
        }
        if (value === 'false') {
            return false
        }
        return isNaN(value) ? value : Number(value)
    }

    const parseKey = (skey, value) => {
        let patt = /\w+|\[\w*\]/g
        let k; let ko; let key = patt.exec(skey)[0]
        let p = params
        while ((ko = patt.exec(skey)) != null) {
            k = ko.toString().replace(/\[|\]/g, '')
            let m = k.match(/\d+/gi)
            if ((m != null && m.toString().length === k.length) || ko === '[]') {
                k = parseInt(k)
                p[key] = p[key] || []
            } else {
                p[key] = p[key] || {}
            }
            p = p[key]
            key = k
        }
        if (typeof (key) === 'number' && isNaN(key)) {
            p.push(parseValue(value))
        } else {
            p[key] = parseValue(value)
        }
    }

    const parseParam = (sparam) => {
        const unescapedSParam = unescape(sparam)
        const firstEqualIndex = unescapedSParam.indexOf('=')
        const paramKey = unescapedSParam.substr(0, firstEqualIndex)
        const paramValue = unescapedSParam.substr(firstEqualIndex + 1)

        parseKey(paramKey, paramValue)
    }

    if (strParams !== undefined && strParams !== '') {
        if (contentType && contentType.startsWith('application/json')) {
            params = JSON.parse(strParams)
        } else if (contentType.startsWith('multipart/form-data')) {
            params = strParams
        } else {
            let arrParams = strParams.split('&')

            for (let i = 0; i < arrParams.length; i++) {
                parseParam(arrParams[i])
            }
        }
    }
    return params
}

exports = parseParams
