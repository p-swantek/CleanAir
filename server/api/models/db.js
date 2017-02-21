var fs        = require('fs');
var path      = require('path');
var Sequelize = require('sequelize');
var username = process.env.MU;
var pass = process.env.MP;

var sequelize = new Sequelize('se491', username, pass, {
    host: '127.0.0.1',
    dialect: 'mysql',
    user: username,
    password: pass,
    pool: {
        max: 5,
        min: 0,
        idle: 10000
    }
});
var db        = {};

fs
    .readdirSync(__dirname)
    .filter(function(file) {
        return (file.indexOf('.') !== 0) && (file !== 'db.js');
    })
    .forEach(function(file) {
        var model = sequelize.import(path.join(__dirname, file));
        db[model.name] = model;
    });

Object.keys(db).forEach(function(modelName) {
    if ('associate' in db[modelName]) {
        db[modelName].associate(db);
    }
});

db.sequelize = sequelize;
db.Sequelize = Sequelize;

module.exports = db;