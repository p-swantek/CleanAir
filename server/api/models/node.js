module.exports = function(sequelize, DataTypes) {
    var Node = sequelize.define("Node", {
        lat: {
            type: DataTypes.STRING,
            allowNull: false
        },
        long: {
            type: DataTypes.STRING,
            allowNull: false
        },
        co_Lvl: {
            type: DataTypes.FLOAT,
            allowNull: true
        },
        co2_Lvl: {
            type: DataTypes.FLOAT,
            allowNull: true
        },
        o3_Lvl: {
            type: DataTypes.FLOAT,
            allowNull: true
        },
        no2_Lvl: {
            type: DataTypes.FLOAT,
            allowNull: true
        }
        /*
        classMethods: {
            associate: function(models) {
                Node.belongsTo(models.User, {
                onDelete: "CASCADE",
                foreignKey: {
                    allowNull: false
                }
            });
            }
        }*/
    });

    return Node;
};
