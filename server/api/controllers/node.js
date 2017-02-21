var models = require('../models/db.js');

module.exports.createNode = function(req, res) {
    var node = models.Node.build({
        lat: (req.body.lat) ? req.body.lat : null,
        long: (req.body.long) ? req.body.long : null,
        co_Lvl: (req.body.co_Lvl) ? req.body.co_Lvl : null,
        co2_Lvl: (req.body.co2_Lvl) ? req.body.co2_Lvl : null,
        o3_Lvl: (req.body.o3_Lvl) ? req.body.o3_Lvl : null,
        no2_Lvl: (req.body.no2_Lvl) ? req.body.no2_Lvl : null,
        UserId: (req.body.userId) ? req.body.userId : null
    });

    node.save().then(function(node) {
        res.status(201).send();
    }).catch(function(error){
        res.status(400).send(error);
    });
};

module.exports.getNodes = function(req, res) {

    var request = {};

    if (req.query.limit) {
        request.limit = req.query.limit;
    } else {
        request.limit = 1000;
    }

    if (req.query.offset) {
        request.offset = req.query.offset;
    } else {
        request.offset = 0;
    }

    if (req.query.attrib) {
        request.attributes = ['id', 'lat', 'long', 'createdAt'].concat(req.query.attrib);
    }

    if (req.query.gas) {

        var reqGas = '';

        if (req.query.gas === 'co') {
            reqGas = 'co_Lvl';
        } else if (req.query.gas === 'co2') {
            reqGas = 'co2_Lvl';
        } else if (req.query.gas === 'o3') {
            reqGas = 'o3_Lvl';
        } else if (req.query.gas === 'no2') {
            reqGas = 'no2_Lvl';
        }

        request.attributes = ['id', 'lat', 'long', 'createdAt', reqGas];
    }

    if (req.query.minLat && req.query.maxLat) {
        if(!request.where) {
            request.where = {};
        }

        var lats = [].concat(req.query.minLat).concat(req.query.maxLat);

        request.where.lat = {
            between: lats
        }
    }

    if (req.query.minLong && req.query.maxLong) {
        if(!request.where) {
            request.where = {};
        }

        var longs = [].concat(req.query.minLong).concat(req.query.maxLong);

        request.where.long = {
            between: longs
        }
    }

    if (req.query.minDate && req.query.maxDate) {
        if (!request.where) {
            request.where = {};
        }

        var dates = [].concat(req.query.minDate).concat(req.query.maxDate);

        request.where.createdAt = {
            between: dates
        }

    } else if (req.query.minDate) {
        if (!request.where) {
            request.where = {};
        }

        var dates = [].concat(req.query.minDate).concat(new Date().getTime());

        request.where.createdAt = {
            between: dates
        }

    } else if (req.query.maxDate) {
        if (!request.where) {
            request.where = {};
        }

        var dates = [].concat(new Date().getTime()).concat(req.query.maxDate);

        request.where.createdAt = {
            between: dates
        }

    }

    models.Node.findAll(request).then(function(nodes) {

        if (req.query.attrib === 'heat') {
            var obj = {};
            obj.data = nodes;
            obj.min = 0;

            if (req.query.gas === 'co') {
                obj.max = 100;
            } else if (req.query.gas === 'co2') {
                obj.max = 2500;
            } else if (req.query.gas === 'o3') {
                obj.max = 0.1;
            } else if (req.query.gas === 'no2') {
                obj.max = 0.05;
            } else {
                obj.max = 0;
            }

            res.status(200).send(obj);
        } else {
            res.status(200).send(nodes);
        }
    });
};