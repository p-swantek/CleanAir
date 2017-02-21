var express = require('express');
var router = express.Router();
var jwt = require('express-jwt');

var auth = jwt	({
    secret: 'Test20!14Test20!15Test61!02',
    userProperty: 'payload'
}); // TODO: INSECURE!! Fix before release. Pushing to source control as this is only a secret key used for testing.

var nodeController = require('./controllers/node.js');
var authController = require('./controllers/auth.js');

router.get('/nodes', nodeController.getNodes);
router.post('/nodes', nodeController.createNode);

router.post('/register', authController.register);
router.post('/login', authController.login);

module.exports = router;