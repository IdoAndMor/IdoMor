var express = require('express');
var mongo = require('mongodb');
var router = express.Router();
var ObjectId = require('mongodb').ObjectID;

var mongoDbMycons = 'mongodb://localhost:27017/MyconsWeb';
var myconsCollection = 'mycons';
var tagsCollection = 'tags';
var reportCollection = 'report';

router.post('/removeMyconById', function (req, res, next) {
    var myconId = req.body.myconId;
    var token = req.body.token;
    var errorMessage = "Sorry, you cannot remove this mycon. Token is incorrect!";
    var succesMessage = "Your mycon removed from db!";
    var notFoundMessage = "Sorry, we did not fount your mycon";
    if (token !== "ksi3msfke39rj9wjd92jkks") {
        res.send(errorMessage);
    }
    else {
        mongo.connect(mongoDbMycons, function (err, db) {
            //assert.equal(null, err);
            console.log('error: ' + err);
            db.collection(myconsCollection).find({_id: myconId}, function (err, document) {
                if (!!document) {
                    db.collection(myconsCollection).findOneAndDelete({"_id": ObjectId(myconId)}, function (err, document) {
                        db.close();
                        res.send(succesMessage);
                    });
                }
                else {
                    db.close();
                    res.send(notFoundMessage);
                }
            });
        });
    }
});

router.get('/addNameToTags', function (req, res, next) {
    mongo.connect(mongoDbMycons, function (err, db) {
        db.collection(myconsCollection).find().toArray(function (err, document) {
            var count = document.length;
            document.forEach(function (element) {
                if (!element.tags || element.tags.indexOf(element.name) === -1) {
                    var imageTags = [];
                    if (!!element.tags) {
                        imageTags = element.tags;
                    }

                    imageTags.unshift(element.name);
                    db.collection(myconsCollection).update({_id: element._id}, {
                        name: element.name,
                        category: element.category,
                        image: element.image,
                        tags: imageTags
                    }, function (err, result4) {
                        console.log('err2: ' + err);
                        if (document[count - 1]._id === element._id) {
                            db.close();
                            res.send("tags fixing accomplished!");
                        }

                    });

                }
                else if (document[count - 1]._id === element._id) {
                    db.close();
                    res.send("tags fixing accomplished!");
                }
            });

        });
    });
});

router.get('/fixTags', function (req, res, next) {
    mongo.connect(mongoDbMycons, function (err, db) {
        db.collection(myconsCollection).find().toArray(function (err, document) {
            var count = document.length;
            document.forEach(function (element) {
                if (typeof element.tags === "string") {
                    var imageTags = tagsToArray(element.tags);

                    db.collection(myconsCollection).update({_id: element._id}, {
                        name: element.name,
                        category: element.category,
                        image: element.image,
                        tags: imageTags
                    }, function (err, result4) {
                        console.log('err2: ' + err);
                        if (document[count - 1]._id === element._id) {
                            db.close();
                            res.send("tags fixing accomplished!");
                        }

                    });

                }
                else if (document[count - 1]._id === element._id) {
                    db.close();
                    res.send("tags fixing accomplished!");
                }
            });

        });
    });
});


router.post('/addTags', function (req, res, next) {
    var tagsData = req.body;

    var categoryTags = tagsToArray(tagsData.tags);
    var categoryType = categoryNameTOIndex(tagsData.category);
    var item = {
        tags: categoryTags,
        category: categoryType,
    };

    mongo.connect(mongoDbMycons, function (err, db) {
        db.collection(tagsCollection).insertOne(item, function (err, result) {
            console.log('err: ' + err);
            db.close();
            res.send("Category tags added!");
        });
    });
});

router.get('/indexing', function (req, res, next) {
    mongo.connect(mongoDbMycons, function (err, db) {
        db.collection(myconsCollection).ensureIndex({"category": 1}, function (err, result) {
            console.log('err: ' + err);
            db.collection(tagsCollection).ensureIndex({"category": 1}, function (err, result) {
                console.log('err: ' + err);
                db.close();
                res.send("Indexing accomplished!");
            });
        });
    });
});

router.post('/', function (req, res, next) {
    console.log("this is request: ");
    var reqData = req.body;
    console.log(reqData);
    var categoryName = reqData.category;
    var imageName = req.body.name;
    var requestTags = reqData.tags;
    var categoryType = categoryNameTOIndex(categoryName);
    var imageTags = tagsToArray(requestTags);
    imageTags.unshift(imageName);

    mongo.connect(mongoDbMycons, function (err, db) {
        //assert.equal(null, err);
        console.log('error: ' + err);
        var item = {
            name: imageName,
            category: categoryType,
            image: req.body.image,
            tags: imageTags
        };

        db.collection(myconsCollection).insertOne(item, function (err, result1) {
            //assert.equal(null, err);
            console.log('err1: ' + err);

            db.collection(tagsCollection).find({category: categoryType}).toArray(function (err, document) {
                console.log('promise3: ' + document[0]);
                var resultArr = [];

                if (!document[0]) {
                    var tagsItem = {
                        category: categoryType,
                        tags: imageTags
                    };
                    db.collection(tagsCollection).insertOne(tagsItem, function (err, result1) {
                        db.close();
                        res.send("What an ugly mycon! But we saved it anyway!");
                    });
                }
                else if (!imageTags) {
                    db.close();
                    res.send("What an ugly mycon! But we saved it anyway!");
                }
                else {
                    resultArr = imageTags.concat(document[0].tags);
                    for (var i = 0; i < resultArr.length; ++i) {
                        for (var j = i + 1; j < resultArr.length; ++j) {
                            if (resultArr[i] === resultArr[j])
                                resultArr.splice(j--, 1);
                        }
                    }
                    db.collection(tagsCollection).update({category: categoryType}, {
                        tags: resultArr,
                        category: categoryType
                    }, function (err, result4) {
                        console.log('err2: ' + err);
                        console.log('promise4');
                        db.close();
                        res.send("Mycon saved successfully!");
                    });
                }
            });
        });
    });
});

router.post('/addLogForReport', function (req, res, next) {
    var reqData = req.body;
    var logType = reqData.logType;
    var message = reqData.message;
    var currentDate = getCurrentTimeFormat();

    var errorMessage = "Log not received!";
    var successMessage = "Log received!";

    if (!logType || !message) {
        res.send(errorMessage);
        return;
    }
    var myconId;

    var item = {
        date: currentDate,
        logType: logType,
        message: message
    };

    if (logType == "myconAbuse") {
        myconId = reqData.myconId;
        item.myconId = myconId;
        if (!myconId) {
            res.send(errorMessage);
            return;
        }
    }
    else if (logType != "appFailure") {
        res.send(errorMessage);
        return;
    }

    mongo.connect(mongoDbMycons, function (err, db) {
        //assert.equal(null, err);
        console.log('error: ' + err);

        db.collection(reportCollection).insertOne(item, function (err, result) {
            //assert.equal(null, err);
            console.log('err: ' + err);
            db.close();
            res.send(successMessage);
        });
    });
});

function getCurrentTimeFormat() {
    var now = new Date();
    return now.getDate().toString() + '/' + (now.getMonth() + 1).toString() + '/' + now.getFullYear().toString() + '  '
        + now.getHours().toString() + ':' + now.getMinutes().toString() + ':' + now.getSeconds().toString()
}

function tagsToArray(tags) {
    var result = []
    if (!tags) {
        return result;
    }

    result = tags.split(',');

    for (var i = 0; i < result.length; i++) {
        result[i] = result[i].trim();
    }

    return result;
}

function categoryNameTOIndex(categoryName) {
    switch (categoryName) {
        case 'Animals':
            return 1;
        case 'Art':
            return 2;
        case 'Beauty':
            return 3;
        case 'Books':
            return 4;
        case 'Cartoon':
            return 5;
        case 'Celebrities':
            return 6;
        case 'Design':
            return 7;
        case 'Drinks':
            return 8;
        case 'Fashion':
            return 9;
        case 'Film':
            return 10;
        case 'Fitness':
            return 11;
        case 'Food':
            return 12;
        case 'Funny':
            return 13;
        case 'General':
            return 14;
        case 'History':
            return 15;
        case 'Holidays and events':
            return 16;
        case 'Home':
            return 17;
        case 'Humor':
            return 18;
        case 'Kids':
            return 19;
        case 'Music':
            return 20;
        case 'Sports':
            return 21;
        case 'People ':
            return 22;
        case 'Products':
            return 23;
        case 'Tattoos':
            return 24;
        case 'Tech ':
            return 25;
        case 'Test':
            return 100;
        case 'Travel':
            return 26;
        case 'Wedding':
            return 27;
        default:
            return 14;
    }
}

module.exports = router;