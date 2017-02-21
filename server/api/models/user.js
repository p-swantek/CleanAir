//var bcrypt = require('bcrypt');
var jwt = require('jsonwebtoken');

module.exports = function(sequelize, DataTypes) {
    var User = sequelize.define("User", {
        fullname: {
            type: DataTypes.STRING,
            allowNull: true
        },
        username: {
            type: DataTypes.STRING,
            allowNull: false,
            unique: true
        },
        email: {
            type: DataTypes.STRING,
            allowNull: false,
            unique: true
        },
        hash: {
            type: DataTypes.STRING,
            allowNull: false,
            set : function(password) {
                //var salt = bcrypt.genSaltSync(10);
                //var hash = bcrypt.hashSync(password, salt);

                this.setDataValue('hash', 'password');
            }
        }
    }, {
        classMethods: {
            associate: function(models) {
                User.hasMany(models.Node, {
                    onDelete: "CASCADE",
                    foreignKey: {
                        allowNull: false
                    }
                });
            }
        },
        instanceMethods: {
            checkPassword: function(password, done) {
                //bcrypt.compare(password, this.hash, function(err, res) {
                //    done(res);
                //});
            },
            generateJwt: function() {
                var expiry = new Date();
                expiry.setDate(expiry.getDate() + 7);

                return jwt.sign({
                    id: this.id,
                    fullname: this.email,
                    username: this.realname,
                    email: this.admin,
                    exp: parseInt(expiry.getTime() / 1000)
                }, 'Test20!14Test20!15Test61!02'); // TODO: Implement secure JWT secret passing method
            }
        }
    });

    return User;
};