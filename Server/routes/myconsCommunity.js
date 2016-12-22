/**
 * Created by Ido on 17/09/16.
 */

var express = require('express');
var mongo = require('mongodb');
var router = express.Router();

var mongoDbMycons = 'mongodb://localhost:27017/MyconsWeb';
var myconsCollection = 'mycons';
var tagsCollection = 'tags';
var reportCollection = 'report';

// router.post('/addVote', function(req, res, next) {
//
//
// }
//
// router.get('/userMyconsScore', function(req, res, next) {
//
//
// }


module.exports = router;