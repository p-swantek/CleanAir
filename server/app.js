var express = require('express');
var bodyParser = require('body-parser');
var path = require('path');
var morgan = require('morgan');

var port = process.env.APP_PORT || 5000;

var models = require('./api/models/db.js');
var apiRoutes = require('./api/routes.js');

var app = express();

app.use(morgan('combined'));
app.use(bodyParser.urlencoded({extended: true}));
app.use(bodyParser.json({ type: 'application/vnd.api+json' }));

app.use('/api', apiRoutes);
app.use('/', express.static(__dirname + '/../web'));
console.log("Server serving files from " + __dirname + '/../web');

models.sequelize.sync().then(function() {
    app.listen(port);
    console.log("Web server listening on port " + process.env.APP_PORT);
});
