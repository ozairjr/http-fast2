exports = [
    // Health
    ['/api/v1/health', 'src/api/health/health-rest/getHealth', {method: 'GET'}],
    ['/api/v1/health/version', 'src/api/health/health-rest/getVersion', {method: 'GET'}],
    // Products
    ['/api/v1/products/find-all', 'src/api/products/products-rest/findAll', {method: 'GET'}],
    ['/api/v1/products/find/:id', 'src/api/products/products-rest/findById', {method: 'GET'}]
]