/**
 * Created by Ido on 17/09/16.
 */
var express = require('express');
var mongo = require('mongodb').MongoClient;
//var uuid = require('node-uuid');
var router = express.Router();
var ObjectId = require('mongodb').ObjectID;

var mongoDbMycons = 'mongodb://localhost:27017/MyconsWeb';
var myconsCollection = 'mycons';
var usersCollection = 'users';

router.post('/register', function(req, res, next) { //original register function
    var requeseBody = req.body;
    var userName = requeseBody.userName;
    var userEmail = requeseBody.userEmail;
    var userPhone = requeseBody.userPhone;
    var userPassword = requeseBody.userPassword;
    var successMessage = "User Registered Successfully";
    var phoneExistError = "Phone Is Already Exist!";
    var emailExistError = "Email Is Already Exist!";

    mongo.connect(mongoDbMycons, function (err, db) {
        //assert.equal(null, err);
        console.log('error: ' + err);
        db.collection(usersCollection).find({phone: userPhone}).toArray(function (err, userObj) {
            if (userObj.length === 0){
                db.collection(usersCollection).find({email: userEmail}).toArray(function (err, userObj) {
                    if (userObj.length === 0) {
                        var token = uuid.v1();
                        var user = {
                            name: userName,
                            email: userEmail,
                            phone: userPhone,
                            password: userPassword,
                            token: token
                        };

                        db.collection(usersCollection).insertOne(user, function (err, result) {
                            console.log('err: ' + err);
                            if (!err) {
                                db.close();
                                res.send(token.toString());
                            }
                        });
                    }
                    else{
                        db.close();
                        res.send(emailExistError);
                    }
                });
            }
            else{
                db.close();
                res.send(phoneExistError);
            }
        });
    });

});

router.post('/login', function(req, res, next) {
    var requeseBody = req.body;
    var userName = requeseBody.userName;
    var userEmail = requeseBody.userEmail;
    var userPhone = requeseBody.userPhone;
    var userPassword = requeseBody.userPassword;
    var userToken = requeseBody.userToken;
    var failMessage = "User Login Failed";
    var successMessage = "User Registered Successfully";

    mongo.connect(mongoDbMycons, function (err, db) {
        //assert.equal(null, err);
        console.log('error: ' + err);
        db.collection(usersCollection).find({phone: userPhone}).toArray(function (err, userObj) {
            if (userObj.length === 0){
                db.collection(usersCollection).find({email: userEmail}).toArray(function (err, userObj) {
                    if (userObj.length === 0) {
                        db.close();
                        res.send(failMessage);
                    }
                    else{
                        if (userObj.password == 'userPassword' || userObj.token == userToken) {
                            db.close();
                            res.send(successMessage);
                        }
                        else{
                            db.close();
                            res.send(failMessage);
                        }
                    }
                });
            }
            else{
                if (userObj.password == 'userPassword' || userObj.token == userToken) {
                    db.close();
                    res.send(successMessage);
                }
                else{
                    db.close();
                    res.send(failMessage);
                }
            }
        });
    });
});

// router.post('/changePassword', function(req, res, next) {
//
//
// }
//
// router.post('/changePhone', function(req, res, next) {
//
//
// }
//
// router.post('/changeEmail', function(req, res, next) {
//
//
// }

function generateToken (){
    var token;

    return token;
}

module.exports = router;



