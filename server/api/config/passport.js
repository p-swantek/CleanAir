var passport = require('passport');
var LocalStrategy = require('passport-local').Strategy;
var models = require('../models/db.js');

passport.use(new LocalStrategy({
        usernameField: 'email',
        passwordField: 'password'
    },
    function(username, password, done) {
        models.User.findOne({ username:username }, function(user){

            if (!user) {
                return done(null, false, {
                    message: 'Invalid credentials.'
                });
            }

            user.checkPassword(password, function(res) {
                if (res) {
                    return done(null, user);
                } else {
                    return done(null, false, {
                        message: 'Invalid credentials.'
                    });
                }
            });
        });
    }
));