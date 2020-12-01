const functions = require('firebase-functions');
const moment = require('moment-timezone');
// const cors = require('cors');
// const express = require('express');
// const { requestBody, validationResult, body, header, param, query } = require('express-validator');
// const jwt = require('jsonwebtoken');

const admin = require('firebase-admin');
const { HandlerBuilder } = require('firebase-functions/lib/handler-builder');
//const { request, response } = require('express');
// admin.initializeApp({
//     credential: admin.credential.cert(serviceAccount),
// });

admin.initializeApp();

// var token;
// var decodedUid;

// const app = express();

// var verifyToken = function(req,res,next){
//     var headerValue = req.header("Authorization");
//     if(!headerValue){
//         return res.status(400).json({"error":"Authorization header needs to be provided for using API"});
//     }

//     var authData = headerValue.split(' ');

//     if(authData && authData.length==2 && authData[0]==='Bearer'){
//         token = authData[1];
//             // admin.auth().verifyIdToken(token).then((decodedToken) => {
//             //     if(!decodedToken || !decodedToken.uid){
//             //         return res.status(400).json({"error":"Some error occured. Cannot proceed"});
//             //     }
//             //     decodedUid = decodedToken.uid;
//             //     next();
//             // })
//             // .catch((error) => {
//             //     console.log(error);
//             //     return res.status(400).json({"error":error})
//             // });
//             //temporary..remove after client integration 
//             decodedUid = token;
//             next();
//     }
//     else {
//         return res.status(400).json({"error":"Appropriate authentication information needs to be provided"})
//     }
// }


// app.use(cors({ origin: true }));
// app.use('/users',verifyToken);

// app.post('/signup',[
//     body("firstName","firstName cannot be empty").notEmpty().trim().escape(),
//     body("firstName","firstName can have only alphabets").isAlpha().trim().escape(),
//     body("lastName","lastName cannot be empty").notEmpty().trim().escape(),
//     body("lastName","lastName can have only alphabets").isAlpha().trim().escape(),
//     body("email","email cannot be empty").notEmpty().trim().escape(),
//     body("email","invalid email format").isEmail(),
//     body("password","password cannot be empty").notEmpty().trim(),
//     body("password","password should have atleast 6 and at max 20 characters").isLength({min:6,max:20}),
//     body("balance","balance cannot be empty and should be a numerical value greater than 0").notEmpty().isFloat({gt:0.0})
// ],(request,response)=>{
//     const err = validationResult(request);
//     if(!err.isEmpty()){
//         return response.status(400).json({"error":err});
//     }

//     admin.auth().createUser({
//         email: request.body.email,
//         emailVerified: false,
//         password: request.body.password,
//         displayName: request.body.firstName+' '+request.body.lastName,
//         disabled: false
//     }).then(userRecord=>{
//         if(!userRecord){
//             return response.status(400).json({error:'some error occured. could not create user'});
//         }
//         admin.firestore().collection('users').doc(userRecord.uid).set({
//             balance: request.body.balance,
//             name: request.body.firstName+' '+request.body.lastName
//         }).then(result=>{
//             if(!result){
//                 return response.status(400).json({error:'some error occured. could not create user'});
//             }
//             admin.auth().createCustomToken(userRecord.uid).then(token=>{
//                 if(!token){
//                     return response.status(400).json({error:'some error occured. could not create user token'});
//                 }
//                 var rslt = {
//                     uid:userRecord.uid,
//                     token:token
//                 }
//                 return response.status(200).json(rslt);
//             }).catch(e=>{
//                 return response.status(400).json({error:e});
//             })
//             //return response.status(200).json({result:userRecord.uid});
//         })
//         .catch(err=>{
//             return response.status(400).json({error:err.toString()});
//         })
//     })
//     .catch(error=>{
//         return response.status(400).json({error:error});
//     });
    
// });

// app.put('/users/balance',[
//     body('amount','amount cannot be empty and should be a numerical value greater than 0').notEmpty().isFloat({gt:0})
// ],async(request,response)=>{
//     const err = validationResult(request);
//     if(!err.isEmpty()){
//         return response.status(400).json({"error":err});
//     }
//     const userRef = admin.firestore().collection('users').doc(decodedUid);

//     try{
//         await admin.firestore().runTransaction(async(transaction)=>{
//              const doc = await transaction.get(userRef);
//              const updatedBalance = doc.data().balance + request.body.amount;
//              const res = await transaction.update(userRef, {balance: updatedBalance});
//              if(!res){
//                  return response.status(400).json({error:"Some error occured. Balance could not be updated"});
//              }
//              return response.status(200).json({"result":"user balance updated"});
//         });
//     }
//     catch(e){
//         return response.status(400).json({error:e.toString()});
//     }
// });



// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//   functions.logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

exports.sayHello = functions.https.onRequest(async(req, res)=>{
    // console.log(req.query.name);
    var n = req.query.name;
    res.status(200).send("Hello!! "+n);
  });

//exports.api = functions.https.onRequest(app);

var isDebug = false;

exports.createUser = functions.https.onCall(async(data, context) => {
    var uid = isAuthenticated(data,context);

    
    if(!data.firstName || !(typeof data.firstName === 'string') || data.firstName.length<=0){
        throw new functions.https.HttpsError('firstName', 'first name should be present and it should be a string with length greater than 0');
    }
    if(!data.lastName || !(typeof data.lastName === 'string') || data.lastName.length<=0){
        throw new functions.https.HttpsError('lastName', 'lastName name should be present and it should be a string with length greater than 0');
    }

    if(!data.balance || !(typeof data.balance === 'number') || data.balance<=0){
        throw new functions.https.HttpsError('balance', 'balance should be present and it should be a number with value greater than 0');
    }

    return admin.firestore().collection('users').doc(""+uid).set({
        balance: data.balance,
        hold:0,
        name: data.firstName+' '+data.lastName
    }).then(result=>{
        if(!result){
            return {error:'some error occured. could not create user'};
        }
        return {result:"user created successfully"};
    })
    .catch(err=>{
        throw new functions.https.HttpsError('db error', err);
    });
});

exports.addBalance = functions.https.onCall(async(data,context)=>{
    var uid = isAuthenticated(data,context);

    if(!data.amount || !(typeof data.amount === 'number') || data.amount<=0){
        throw new functions.https.HttpsError('amount', 'amount should be present and it should be a number with value greater than 0');
    }

    const userRef = admin.firestore().collection('users').doc(""+uid);

    try{
        return await admin.firestore().runTransaction(async(transaction)=>{
             const doc = await transaction.get(userRef);
             const updatedBalance = doc.data().balance + data.amount;
             const res = await transaction.update(userRef, {balance: updatedBalance});
             if(!res){
                 return {error:"Some error occured. Balance could not be updated"};
             }
             return {result:"user balance updated"};
        });
    }
    catch(e){
        return {error:e.toString()};
    }
});

exports.postNewItem = functions.https.onCall(async(data,context)=>{
    var uid = isAuthenticated(data,context);

    if(!data.itemName || !(typeof data.itemName === 'string') || data.itemName.length<=0){
        throw new functions.https.HttpsError('itemName', 'item name should be present and it should be a string with length greater than 0');
    }
    if(!data.startBid || !(typeof data.startBid === 'number') || data.startBid<=0){
        throw new functions.https.HttpsError('startBid', 'start bid should be present and it should be a number with value greater than 0');
    }
    if(!data.minFinalBid || !(typeof data.minFinalBid === 'number') || data.minFinalBid<=0){
        throw new functions.https.HttpsError('minFinalBid', 'minimum final bid should be present and it should be a number with value greater than 0');
    }

    const userRef = admin.firestore().collection('users').doc(""+uid);
    var msg = "";

    try{
        return await admin.firestore().runTransaction(async(transaction)=>{
             const doc = await transaction.get(userRef);
             var currentBalance = doc.data().balance;
             if(currentBalance<1){
                 msg="insufficient balance to create post an item"
                throw new functions.https.HttpsError('balance','insufficient balance to create post an item');
             }
             const updatedBalance = currentBalance - 1;
             const res = await transaction.update(userRef, {balance: updatedBalance});
             if(!res){
                 msg="balance could not be update"
                throw new functions.https.HttpsError('balance', 'balance could not be updated');
             }
             var date = new Date();
            var dateWrapper = moment(date);
            var dateString = dateWrapper.tz('America/New_York').format("YYYY MMM D HH:mm:ss"); 
             return admin.firestore().collection('Auctions').add({
                 item_name:data.itemName,
                 start_bid:data.startBid,
                 min_final_bid:data.minFinalBid,
                 owner_id:uid,
                 auction_status:"created",
                 auction_start_date:dateString
             }).then(res=>{
                if(!res){
                    throw new functions.https.HttpsError('auction', 'auction could not be created');
                }
                return {'result':"auction created","itemId":res.id};
             }).catch(err=>{
                throw new functions.https.HttpsError('error', err);
             })
        });
    }
    catch(e){
        throw new functions.https.HttpsError('error', e);
    }
});

exports.bidOnItem = functions.https.onCall(async(data,context)=>{
    var uid = isAuthenticated(data,context);

    if(!data.itemId || !(typeof data.itemId === 'string')){
        throw new functions.https.HttpsError('itemId', 'items should be present and it should be a string value');
    }
    if(!data.bid || !(typeof data.bid === 'number') || data.bid<=0){
        throw new functions.https.HttpsError('bid', 'bid should be present and it should be a number with value greater than 0');
    }

    const userRef = admin.firestore().collection('users').doc(""+uid);
    const auctionRef = admin.firestore().collection('Auctions').doc(data.itemId);

    if(!auctionRef){
        throw new functions.https.HttpsError('itemId', 'no such auction available');
    }

    try{
        return await admin.firestore().runTransaction(async(transaction)=>{
            const userDoc = await transaction.get(userRef);
            const auctionDoc = await transaction.get(auctionRef);

            bidders = new Map();
            var startingBid = auctionDoc.data().start_bid;
            var currentBid = auctionDoc.data().current_highest_bid;
            var currentBalance = userDoc.data().balance;
            var auctionStatus = auctionDoc.data().auction_status
            var ownerId = auctionDoc.data().owner_id;
            var currentBidUser = auctionDoc.data().current_highest_bid_user
            bidders = auctionDoc.data().bidders;

            

            if(currentBidUser == uid){
                throw new functions.https.HttpsError('uid', 'user is already the highest bidder');
            }

            if(ownerId == uid){
                throw new functions.https.HttpsError('uid', 'item owner cannot bid on his own item');
            }

            if(auctionStatus == 'complete'){
                throw new functions.https.HttpsError('itemId', 'item bid already complete');
            }

            if(currentBid!=undefined && currentBid!=null){
                if(data.bid<=currentBid){
                    throw new functions.https.HttpsError('bid', 'cannot place a smaller or equal bid than current highest bid');
                }
            }
            else{
                if(data.bid<startingBid){
                    throw new functions.https.HttpsError('bid', 'cannot place a smaller bid than the starting bid');
                }
            }

            if(currentBid!=undefined && currentBid!=null){
                if(currentBalance<currentBid+1){
                    throw new functions.https.HttpsError('balance', 'insufficient balance to place bid');
                }
            }
            else{
                if(currentBalance<startingBid+1){
                    throw new functions.https.HttpsError('balance', 'insufficient balance to place bid');
                }
            }

            
            if(currentBidUser){
                const oldUserRef = admin.firestore().collection('users').doc(""+currentBidUser);
                const oldUserDoc = await transaction.get(oldUserRef);
                var oldBalance = oldUserDoc.data().balance + currentBid;
                var oldHold = oldUserDoc.data().hold - currentBid;

                const oldRes = await transaction.update(oldUserRef, {balance: oldBalance,hold:oldHold});
                if(!oldRes){
                    throw new functions.https.HttpsError('error', 'some error occured.');
                }
            }

            var newBalance = currentBalance - data.bid - 1;
            var newHold = userDoc.data().hold + data.bid;

            const res = await transaction.update(userRef, {balance: newBalance,hold:newHold});
            if(!res){
                throw new functions.https.HttpsError('error', 'some error occured.');
            }

            var date = new Date();
            var dateWrapper = moment(date);
            var dateString = dateWrapper.tz('America/New_York').format("YYYY MMM D HH:mm:ss"); 

            var newBidder = {};
            if(bidders){
                newBidder = bidders
            }
            newBidder[uid]=data.bid;

            var newAuction = {
                current_highest_bid : data.bid,
                current_highest_bid_user : uid,
                auction_update_date : dateString,
                bidders : newBidder,
                auction_status : 'in_progress'
            }

            const auctionRes = await transaction.update(auctionRef,newAuction,{merge:true});
            
            if(!auctionRes){
                throw new functions.https.HttpsError('error', 'some error occured.');
            }

            // const addBiddersRes = await transaction.update(auctionRef,newBidders);
            // if(!addBiddersRes){
            //     throw new functions.https.HttpsError('error', 'some error occured.');
            // }

             return {'result':'bid placed successfully'};

            //const updatedBalance = doc.data().balance + data.amount;
            //const res = await transaction.update(userRef, {balance: updatedBalance});
            // if(!res){
            //     return {error:"Some error occured. Balance could not be updated"};
            // }
            // return {result:"user balance updated"};
       });
    }
    catch(e){
        throw new functions.https.HttpsError('error', e);
    }

});

exports.acceptBidOnItem = functions.https.onCall(async(data,context)=>{
    var uid = isAuthenticated(data,context);

    if(!data.itemId || !(typeof data.itemId === 'string')){
        throw new functions.https.HttpsError('itemId', 'items should be present and it should be a string value');
    }

    const userRef = admin.firestore().collection('users').doc(""+uid);
    const auctionRef = admin.firestore().collection('Auctions').doc(data.itemId);

    if(!auctionRef){
        throw new functions.https.HttpsError('itemId', 'no such auction available');
    }

    try{
        return await admin.firestore().runTransaction(async(transaction)=>{
            const userDoc = await transaction.get(userRef);
            const auctionDoc = await transaction.get(auctionRef);
    
            var currentBid = auctionDoc.data().current_highest_bid;
            var auctionStatus = auctionDoc.data().auction_status
            var ownerId = auctionDoc.data().owner_id;
            var currentBidUser = auctionDoc.data().current_highest_bid_user
    
            if(ownerId!=uid){
                throw new functions.https.HttpsError('uid', 'only the owner can settle an auction');
            }
    
            if(auctionStatus!='in_progress'){
                console.log(auctionStatus);
                throw new functions.https.HttpsError('auction status', 'invalid auction status. cannot settle this auction');
            }
    
            const buyerRef = admin.firestore().collection('users').doc(""+currentBidUser);
            const buyerDoc = await transaction.get(buyerRef);
    
            var date = new Date();
            var dateWrapper = moment(date);
            var dateString = dateWrapper.tz('America/New_York').format("YYYY MMM D HH:mm:ss"); 
    
            var buyerHold = buyerDoc.data().hold - currentBid;
            //add bid info
            var newItem = {};
            var wonItems = buyerDoc.data().won_items;
    
            if(wonItems){
                newItem = wonItems;
            }
            newItem[data.itemId]={
                item_name:auctionDoc.data().item_name,
                buying_date : dateString,
                item_price:currentBid
            }
    
            const buyerRes = await transaction.update(buyerRef, {hold:buyerHold,won_items:newItem},{merge:true});
            if(!buyerRes){
                throw new functions.https.HttpsError('error1', 'some error occured.');
            }
    
            var sellerBalance = userDoc.data().balance + currentBid;
            const sellerRes = await transaction.update(userRef, {balance:sellerBalance});
            if(!sellerRes){
                throw new functions.https.HttpsError('error', 'some error occured.');
            }
    
            var newAuction = {
                auction_status : 'complete',
                auction_end_date : dateString
            }
    
            const auctionRes = await transaction.update(auctionRef, newAuction);
            if(!auctionRes){
                throw new functions.https.HttpsError('error', 'some error occured.');
            }
    
            return {'result':"auction settled"};
        });
    }
    catch(e){
        throw new functions.https.HttpsError('error', e);
    }
});

exports.getAuctionItems = functions.https.onCall(async(data,context)=>{
    var uid = isAuthenticated(data,context);

    const auctionRef = admin.firestore().collection('Auctions').where('auction_status','==','in_progress');

    try{
        return await admin.firestore().runTransaction(async(transaction)=>{
            const snapshot = await transaction.get(auctionRef);
            var inProgressAuctions = [];
            snapshot.forEach(doc=>{
                inProgressAuctions.push({"id":doc.id,"data":doc.data()});
                
            });

            return {'result':inProgressAuctions};
        });
    }
    catch(e){
        return {error:e.toString()};
    }

});

exports.getWonItem = functions.https.onCall(async(data,context)=>{
    var uid = isAuthenticated(data,context);

    const userRef = admin.firestore().collection('users').doc(""+uid);

    try{
        return await admin.firestore().runTransaction(async(transaction)=>{
            const docs = await transaction.get(userRef);
            var history = docs.data().won_items;
            return {'result':history};
        });
    }
    catch(e){
        return {error:e.toString()};
    }

});

exports.getCurrentAuctionItems = functions.https.onCall(async(data,context)=>{
    var uid = isAuthenticated(data,context);
    const auctionRef = admin.firestore().collection('Auctions').where('auction_status','==','in_progress');

    try{
        return await admin.firestore().runTransaction(async(transaction)=>{
            const snapshot = await transaction.get(auctionRef);
            
            var items=[];
            snapshot.forEach(doc=>{
                var bidders = doc.data().bidders;
                if(bidders && bidders[uid]){
                    items.push({id:doc.id,data:doc.data()});
                }
            });

            return {result:items};
        });
    }
    catch(e){
        return {error:e.toString()};
    }


});

function isAuthenticated(data,context){
    if(isDebug){
        return data.uid;
    }

    if(!context.auth){
        throw new functions.https.HttpsError('unauthorized', 'User unauthorized');
    }
    return context.auth.uid;
}