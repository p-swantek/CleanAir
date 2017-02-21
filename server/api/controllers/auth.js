var passport = require('passport');
var models = require('../models/db.js');

var sendJsonResponse = function(res, status, result) {
    var results = {
        posts: []
    };

    if (status !== 200) {
        console.log(result);

        results.error = {
            'status': status,
            'message': result.message
        };
    } else {
        results.posts = result;
    }
    res.status(status);
    res.json(result);
};

module.exports.register = function(req, res) {

    var user = models.User.build({
        fullname: (req.body.fullname) ? req.body.fullname : null,
        username: (req.body.username) ? req.body.username : null,
        email: (req.body.email) ? req.body.email : null,
        hash: (req.body.password) ? req.body.password : null
    });

    user.save().then(function(user) {
        var token = user.generateJwt();
        sendJsonResponse(res, 200, {
            'token': token
        });
    }).catch(function(error){
        res.status(400).send(error);
    });
};

module.exports.login = function(req, res) {
    if (!req.body.email || !req.body.password) {
        sendJsonResponse(res, 400, 'All fields required.');
        return;
    }

    models.User.findOne({ username:req.body.email }, function(user){

        if (!user) {
            sendJsonResponse(res, 400, 'Invalid credentials.');
        }

        user.checkPassword(req.body.password, function(res) {
            if (res) {
                var token = user.generateJwt();
                sendJsonResponse(res, 200, {
                    'token': token
                });
            } else {
                sendJsonResponse(res, 400, 'Invalid credentials.');
            }
        });
    });
};