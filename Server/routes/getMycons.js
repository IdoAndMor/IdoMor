var express = require('express');
var mongo = require('mongodb');
var router = express.Router();

var mongoDbMycons = 'mongodb://localhost:27017/MyconsWeb';
var myconsCollection = 'mycons';
var tagsCollection = 'tags';
var reportCollection = 'report';
var categoriesCollection = 'Categories';


var categoriesNames = ['Choose Category', 'Animals', 'Art', 'Beauty', 'Books', 'Cartoon', 'Celebrities', 'Design', 'Drinks', 'Fashion', 'Film', 'Fitness', 'Food',
  'Funny', 'General', 'History', 'Holidays and events', 'Home', 'Humor', 'Kids', 'Music', 'Sports', 'People', 'Products', 'Tattoos', 'Tech', 'Travel', 'Wedding'];
var categoriesNamesIndexes = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 100];
var myconsInPageNum = 20;

router.post('/searchByTag', function(req, res, next) {
  var data = req.body.searchTags;
  var searchTags = data.split(" ");
  var searchTagsLength = searchTags.length;
  var myconsArray = [];
  var continueLoop = true;

  var pageNum = req.body.page;
  var startFrom = pageNum * myconsInPageNum;
  var lastMycon = (pageNum + 1) * myconsInPageNum ;
  var counter = 0;

  mongo.connect(mongoDbMycons, function(err, db) {
    db.collection(myconsCollection).find().toArray(function (err2, allMycons) {
      var allMyconsLength = allMycons.length;
      var tagInLowerCase;
      var tagInUpperCase;
      if (searchTags.length === 1 && searchTags[0] == ""){
        myconsArray = allMycons.slice(startFrom, lastMycon);
        db.close();
        res.send(myconsArray);
      }
      else {
        allMycons.forEach(function (element) {
          if (!!element.tags && continueLoop){
             for(var i=0; i<searchTagsLength; i++){
               if(!!element.tags) {
                 var elementTagsLength = element.tags.length;
                 for (var j=0; j < elementTagsLength; j++) {
                   tagInUpperCase = element.tags[j].charAt(0).toUpperCase() + element.tags[j].slice(1);
                   tagInLowerCase = element.tags[j].charAt(0).toLowerCase() + element.tags[j].slice(1);
                   //if (element.tags[j].indexOf(searchTags[i]) !== -1) {
                   if (tagInUpperCase.indexOf(searchTags[i]) !== -1 || tagInLowerCase.indexOf(searchTags[i]) !== -1) {
                     myconsArray.push(element);
                     counter++;
                     if (counter === lastMycon){
                       continueLoop = false;
                     }
                     j = elementTagsLength;
                     i = searchTagsLength;
                   }
                 }
               }
             }
          }
          if(allMycons[allMyconsLength-1]._id === element._id ){
            myconsArray = myconsArray.slice(startFrom, lastMycon);
            db.close();
            res.send(myconsArray);
          }
        });
      }
    });
  });
});



// router.post('/searchByTag', function(req, res, next) {
//   var data = req.body.searchTags;
//   var searchTags = data.split(" ");
//   var myconsArray = [];
//   var categoryTypeLength = categoriesNamesIndexes.length;
//   var closeFlag = false;
//
//   mongo.connect(mongoDbMycons, function(err, db) {
//     console.log('error: ' +  err);
//     for (var i=0; i<categoryTypeLength; i++) {
//       var categoriesIndex = categoriesNamesIndexes[i];
//       function outer1(categoriesIndex, k) {
//         db.collection(tagsCollection).find({category: categoriesIndex}).toArray(function (err1, tagsColl) {
//           db.collection(myconsCollection).find({category: categoriesIndex}).toArray(function (err2, categoryMycons) {
//
//             var tagsCount = tagsColl.length;
//             if (tagsCount !== 0 || k + 1 === categoryTypeLength) {
//               if (tagsCount === 0) {
//                 db.close();
//                 res.send(myconsArray);
//               }
//               else {
//                 var categoryTags = tagsColl[0].tags;
//                 if (!!categoryTags) {
//                   function outer2(categoryTagsVar, categoryMyconsVar) {
//                     categoryTagsVar.forEach(function (element1) {
//                       searchTags.forEach(function (element2) {
//                         if (!closeFlag && element1.indexOf(element2) !== -1 ) {
//                           categoryMyconsVar.forEach(function (mycon) {
//                             if (mycon.name.indexOf(element2) !== -1){
//                               myconsArray.push(mycon);
//                             }
//                             else if (!!mycon.tags) {
//                               if (mycon.tags.indexOf(element2) !== -1) {
//                                 myconsArray.push(mycon);
//                               }
//                             }
//                             if (k + 1 === categoryTypeLength && searchTags[searchTags.length - 1] === element2 &&  categoryMyconsVar[categoryMyconsVar.length - 1]._id === mycon._id) {
//                               db.close();
//                               res.send(myconsArray);
//                               closeFlag = true;
//                             }
//                           });
//                         }
//                       });
//                     });
//                   }
//                   outer2(categoryTags, categoryMycons);
//                 }
//                 else if(k + 1 === categoryTypeLength){
//                   db.close();
//                   res.send(myconsArray);
//                 }
//               }
//             }
//           });
//         });
//       }
//       outer1(categoriesIndex, i);
//     }
//   });
// });

// router.get('/categories', function(req, res, next) {
//   var response = {};
//   mongo.connect(mongoDbMycons, function(err, db) {
//
//     db.collection(categoriesCollection).find().toArray(function (err2, allCategories) {
//         response.code = 0;
//         response.data = allCategories;
//         res.send(response);
//     });
//   });
// });

router.post('/categoriesNames', function(req, res, next) {
  res.send(categoriesNames);
});

router.post('/pagesNumByCategory', function(req, res, next) {
  var categoryName = req.body.category;
  var categoryIndex = categoryNameTOIndex(categoryName);

  mongo.connect(mongoDbMycons, function(err, db) {
    //assert.equal(null, err);
    console.log('error: ' +  err);
    db.collection(myconsCollection).find({category:categoryIndex}).toArray(function(err, document) {
      var count = document.length;
      //db.collection(myconsCollection).find({category:categoryIndex}, res).count.length;
      var pagesNumber = count == 0 ? 0 : parseInt(count / myconsInPageNum) +1;
      console.log(pagesNumber.toString());
      db.close();
      res.send(pagesNumber.toString());
    });
  });
});

router.post('/pagesNumByTag', function(req, res, next) {
  var data = req.body.searchTags;
  var searchTags = data.split(" ");
  var searchTagsLength = searchTags.length;
  var continueLoop = true;

  var counter = 0;
  var pagesNumber = 0;

  mongo.connect(mongoDbMycons, function(err, db) {
    db.collection(myconsCollection).find().toArray(function (err2, allMycons) {
      var allMyconsLength = allMycons.length;
      if (searchTags.length === 1 && searchTags[0] == ""){
        pagesNumber = allMyconsLength == 0 ? 0 : parseInt(allMyconsLength / myconsInPageNum) +1;
        db.close();
        res.send(pagesNumber.toString());
      }
      else {
        allMycons.forEach(function (element) {
          if (!!element.tags && continueLoop){
            for(var i=0; i<searchTagsLength; i++){
              if(!!element.tags) {
                var elementTagsLength = element.tags.length;
                for (var j=0; j < elementTagsLength; j++) {
                  if (element.tags[j].indexOf(searchTags[i]) !== -1) {
                    counter++;
                    j = elementTagsLength;
                    i = searchTagsLength;
                  }
                }
              }
            }
          }
          if(allMycons[allMyconsLength-1]._id === element._id ){
            pagesNumber = counter == 0 ? 0 : parseInt(counter / myconsInPageNum) +1;
            db.close();
            res.send(pagesNumber.toString());
          }
        });
      }
    });
  });
});

router.post('/categoryMycons', function(req, res, next) {
  var categoryName = req.body.category;
  var categoryIndex = categoryNameTOIndex(categoryName);

  var pageNum = req.body.page;
  var startFrom = pageNum * myconsInPageNum;
  var lastMycon = (pageNum + 1) * myconsInPageNum ;

  var resultArray = [];
  mongo.connect(mongoDbMycons, function(err, db) {
    //assert.equal(null, err);
    console.log('error: ' +  err);
     db.collection(myconsCollection).find({category:categoryIndex}).toArray(function(err, document) {
       var count = document.length;
       lastMycon = count >= lastMycon? lastMycon : count;
       for (var i=startFrom; i<lastMycon; i++){
         resultArray.push(document[i]);
       }
       console.log(pageNum.toString());
       db.close();
       res.send(resultArray);
     });
  });
});

router.post('/getReportByType', function (req, res, next) {
  var reqData = req.body;
  var logType = reqData.logType;

  var errorMessage = "logType is not valid!";

  if (!logType || (logType != "myconAbuse" && logType != "appFailure")) {
    res.send(errorMessage);
    return;
  }

  var resultArray = [];
  mongo.connect(mongoDbMycons, function(err, db) {
    //assert.equal(null, err);
    console.log('error: ' +  err);
    db.collection(reportCollection).find({logType:logType}).toArray(function(err, document) {
      var count = document.length;
      for (var i=0; i<count; i++){
        resultArray.push(document[i]);
      }
      db.close();
      res.send(resultArray);
    });
  });
});

router.post('/pagesNum', function(req, res, next) {
  var categoryName = req.body.category;

  mongo.connect(mongodbUrl, function(err, db) {
    //assert.equal(null, err);
    console.log('error: ' +  err);
    var count = 0;
    var cursor = db.collection(categoryName).find();

    cursor.forEach(function(doc, err) {
      //assert.equal(null, err);
      console.log('error: ' +  err);

      count++;
    }, function() {
      var pagesNumber = count == 0 ? 0 : parseInt(count / myconsInPageNum) +1;
      console.log(pagesNumber.toString());
      db.close();
      res.send(pagesNumber.toString());
    });
  });
});


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
