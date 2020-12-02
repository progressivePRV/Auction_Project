const functions = require('firebase-functions');
const moment = require('moment-timezone');

const admin = require('firebase-admin');
const { HandlerBuilder } = require('firebase-functions/lib/handler-builder');

admin.initializeApp();

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
        throw new functions.https.HttpsError('invalid-argument', 'first name should be present and it should be a string with length greater than 0');
    }
    if(!data.lastName || !(typeof data.lastName === 'string') || data.lastName.length<=0){
        throw new functions.https.HttpsError('invalid-argument', 'lastName name should be present and it should be a string with length greater than 0');
    }

    if(!data.deviceToken || !(typeof data.deviceToken === 'string') || data.deviceToken.length<=0){
        throw new functions.https.HttpsError('invalid-argument', 'device token should be present and it should be a string with length greater than 0');
    }

    if(!data.balance || !(typeof data.balance === 'number') || data.balance<1){
        throw new functions.https.HttpsError('invalid-argument', 'balance should be present and it should be a number with minimum value 1');
    }

    return admin.firestore().collection('users').doc(""+uid).set({
        balance: data.balance,
        hold:0,
        name: data.firstName+' '+data.lastName,
        device_token: data.deviceToken
    }).then(result=>{
        if(!result){
            throw new functions.https.HttpsError('invalid-argument', 'Some error occured');
        }
        return {result:"user created successfully"};
    })
    .catch(err=>{
        throw new functions.https.HttpsError('invalid-argument', err.message);
    });
});

exports.updateDeviceToken = functions.https.onCall(async(data,context)=>{
    var uid = isAuthenticated(data,context);

    if(!data.deviceToken || !(typeof data.deviceToken === 'string') || data.deviceToken.length<=0){
        throw new functions.https.HttpsError('invalid-argument', 'device token should be present and it should be a string with length greater than 0');
    }

    const userRef = admin.firestore().collection('users').doc(""+uid);

    try{
        return await admin.firestore().runTransaction(async(transaction)=>{
             //const doc = await transaction.get(userRef);
             const updatedToken = data.deviceToken
             const res = await transaction.update(userRef, {device_token: updatedToken});
             if(!res){
                 throw new functions.https.HttpsError('invalid-argument', 'Some error occured');
             }
             return {result:"device token updated"};
        });
    }
    catch(e){
        throw new functions.https.HttpsError('invalid-argument', e.message);
    }

});

exports.deleteDeviceToken = functions.https.onCall(async(data,context)=>{
    var uid = isAuthenticated(data,context);

    const userRef = admin.firestore().collection('users').doc(""+uid);
    const FieldValue = admin.firestore.FieldValue;

    try{
        return await admin.firestore().runTransaction(async(transaction)=>{
             const res = await transaction.update(userRef, {device_token: FieldValue.delete()});
             if(!res){
                 throw new functions.https.HttpsError('invalid-argument', 'Some error occured');
             }
             return {result:"device token deleted"};
        });
    }
    catch(e){
        throw new functions.https.HttpsError('invalid-argument', e.message);
    }

});
exports.addBalance = functions.https.onCall(async(data,context)=>{
    var uid = isAuthenticated(data,context);

    if(!data.amount || !(typeof data.amount === 'number') || data.amount<1){
        throw new functions.https.HttpsError('invalid-argument', 'amount should be present and it should be a number with with minimum value 1');
    }

    const userRef = admin.firestore().collection('users').doc(""+uid);

    try{
        return await admin.firestore().runTransaction(async(transaction)=>{
             const doc = await transaction.get(userRef);
             const updatedBalance = doc.data().balance + data.amount;
             const res = await transaction.update(userRef, {balance: updatedBalance});
             if(!res){
                throw new functions.https.HttpsError('invalid-argument', 'Some error occured');
             }
             return {result:"user balance updated"};
        });
    }
    catch(e){
        throw new functions.https.HttpsError('invalid-argument', e.message);
    }
});

exports.postNewItem = functions.https.onCall(async(data,context)=>{
    var uid = isAuthenticated(data,context);

    if(!data.itemName || !(typeof data.itemName === 'string') || data.itemName.length<=0){
        throw new functions.https.HttpsError('invalid-argument', 'item name should be present and it should be a string with length greater than 0');
    }
    if(!data.startBid || !(typeof data.startBid === 'number') || data.startBid<=0){
        throw new functions.https.HttpsError('invalid-argument', 'start bid should be present and it should be a number with value greater than 0');
    }
    if(!data.minFinalBid || !(typeof data.minFinalBid === 'number') || data.minFinalBid<=0){
        throw new functions.https.HttpsError('invalid-argument', 'minimum final bid should be present and it should be a number with value greater than 0');
    }

    if(data.startBid>=data.minFinalBid){
        throw new functions.https.HttpsError('invalid-argument', 'minimum final bid cannot be smaller that or equal to the start bid');
    }

    const userRef = admin.firestore().collection('users').doc(""+uid);
    var msg = "";

    try{
        return await admin.firestore().runTransaction(async(transaction)=>{
             const doc = await transaction.get(userRef);
             var currentBalance = doc.data().balance;
             if(currentBalance<1){
                 msg="insufficient balance to create post an item"
                throw new functions.https.HttpsError('invalid-argument','insufficient balance to create post an item');
             }
             const updatedBalance = currentBalance - 1;
             const res = await transaction.update(userRef, {balance: updatedBalance});
             if(!res){
                 msg="balance could not be update"
                throw new functions.https.HttpsError('invalid-argument', 'balance could not be updated');
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
                    throw new functions.https.HttpsError('invalid-argument', 'auction could not be created');
                }
                return {'result':"auction created","itemId":res.id};
             }).catch(err=>{
                throw new functions.https.HttpsError('invalid-argument', err.message);
             })
        });
    }
    catch(e){
        throw new functions.https.HttpsError('invalid-argument', e.message);
    }
});

exports.bidOnItem = functions.https.onCall(async(data,context)=>{
    var uid = isAuthenticated(data,context);

    if(!data.itemId || !(typeof data.itemId === 'string')){
        throw new functions.https.HttpsError('invalid-argument', 'items should be present and it should be a string value');
    }
    if(!data.bid || !(typeof data.bid === 'number') || data.bid<=0){
        throw new functions.https.HttpsError('invalid-argument', 'bid should be present and it should be a number with value greater than 0');
    }

    const userRef = admin.firestore().collection('users').doc(""+uid);
    const auctionRef = admin.firestore().collection('Auctions').doc(data.itemId);

    if(!auctionRef){
        throw new functions.https.HttpsError('invalid-argument', 'no such auction available');
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
            var currentBidUser = auctionDoc.data().current_highest_bid_user;
            var minFinalBid = auctionDoc.data().min_final_bid;
            bidders = auctionDoc.data().bidders;

            const ownerUserRef = admin.firestore().collection('users').doc(""+ownerId);
            const ownerUserDoc = await transaction.get(ownerUserRef);

            

            if(currentBidUser == uid){
                throw new functions.https.HttpsError('invalid-argument', 'user is already the highest bidder');
            }

            if(ownerId == uid){
                throw new functions.https.HttpsError('invalid-argument', 'item owner cannot bid on his own item');
            }

            if(auctionStatus == 'complete'){
                throw new functions.https.HttpsError('invalid-argument', 'item bid already complete');
            }

            if(currentBid!=undefined && currentBid!=null){
                if(data.bid<=currentBid){
                    throw new functions.https.HttpsError('invalid-argument', 'cannot place a smaller or equal bid than current highest bid');
                }
            }
            else{
                if(data.bid<startingBid){
                    throw new functions.https.HttpsError('invalid-argument', 'cannot place a smaller bid than the starting bid');
                }
            }

            if(currentBalance<data.bid+1){
                throw new functions.https.HttpsError('invalid-argument', 'insufficient balance to place bid');
            }

            if(currentBid!=undefined && currentBid!=null){
                if(currentBalance<currentBid+1){
                    throw new functions.https.HttpsError('invalid-argument', 'insufficient balance to place bid');
                }
            }
            else{
                if(currentBalance<startingBid+1){
                    throw new functions.https.HttpsError('invalid-argument', 'insufficient balance to place bid');
                }
            }

            var oldBidderMessage;
            var newBidderMessage;
            var minFinalBidMessage;

            if(currentBidUser){
                const oldUserRef = admin.firestore().collection('users').doc(""+currentBidUser);
                const oldUserDoc = await transaction.get(oldUserRef);
                var oldBalance = oldUserDoc.data().balance + currentBid;
                var oldHold = oldUserDoc.data().hold - currentBid;

                const oldRes = await transaction.update(oldUserRef, {balance: oldBalance,hold:oldHold});
                if(!oldRes){
                    throw new functions.https.HttpsError('invalid-argument', 'some error occured.');
                }
                oldBidderMessage = {
                    notification: {title: 'Loosing bid', body: 'Somebody bid more than you on auction item: '+auctionDoc.data().item_name},
                    data:{itemId:data.itemId,code:"101"},
                    token: oldUserDoc.data().device_token,
                  };
            }

            var newBalance = currentBalance - data.bid - 1;
            var newHold = userDoc.data().hold + data.bid;

            newBidderMessage = {
                notification: {title: 'Highest Bidder', body: 'You are the highest bidder on auction item: '+auctionDoc.data().item_name},
                data:{itemId:data.itemId,code:"102"},
                token: userDoc.data().device_token,
              };

            const res = await transaction.update(userRef, {balance: newBalance,hold:newHold});
            if(!res){
                throw new functions.https.HttpsError('invalid-argument', 'some error occured.');
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
                throw new functions.https.HttpsError('invalid-argument', 'some error occured.');
            }

            if(oldBidderMessage && oldBidderMessage.token && oldBidderMessage.token.length>0){
                var oldMsg = await admin.messaging().send(oldBidderMessage);
                if(!oldMsg){
                    throw new functions.https.HttpsError('invalid-argument', 'some error occured.');
                }
            }
            if(newBidderMessage && newBidderMessage.token && newBidderMessage.token.length>0){
                var newMsg = await admin.messaging().send(newBidderMessage)
                if(!newMsg){
                    throw new functions.https.HttpsError('invalid-argument', 'some error occured.');
                }
            }

            if(currentBid){
                if(currentBid<minFinalBid && data.bid>=minFinalBid){
                    minFinalBidMessage={
                        notification: {title: 'Mimimum Final Bid reached', body: 'The minimum final bid amount has reached on auction item: '+auctionDoc.data().item_name},
                        data:{itemId:data.itemId,code:"103"},
                        token: ownerUserDoc.data().device_token,
                    };
                }
            }
            else{
                if(data.bid >= minFinalBid){
                    minFinalBidMessage={
                        notification: {title: 'Mimimum Final Bid reached', body: 'The minimum final bid amount has reached on auction item: '+auctionDoc.data().item_name},
                        data:{itemId:data.itemId,code:"103"},
                        token: ownerUserDoc.data().device_token,
                    };
                }
            }

            if(minFinalBidMessage && minFinalBidMessage.token && minFinalBidMessage.token.length>0){
                var minBidMsg = await admin.messaging().send(minFinalBidMessage)
                if(!minBidMsg){
                    throw new functions.https.HttpsError('invalid-argument', 'some error occured.');
                }
            }

             return {'result':'bid placed successfully'};
       });
    }
    catch(e){
        throw new functions.https.HttpsError('invalid-argument', e.message);
    }

});

exports.acceptBidOnItem = functions.https.onCall(async(data,context)=>{
    var uid = isAuthenticated(data,context);

    if(!data.itemId || !(typeof data.itemId === 'string')){
        throw new functions.https.HttpsError('invalid-argument', 'items should be present and it should be a string value');
    }

    const userRef = admin.firestore().collection('users').doc(""+uid);
    const auctionRef = admin.firestore().collection('Auctions').doc(data.itemId);

    if(!auctionRef){
        throw new functions.https.HttpsError('invalid-argument', 'no such auction available');
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
                throw new functions.https.HttpsError('invalid-argument', 'only the owner can settle an auction');
            }
    
            if(auctionStatus!='in_progress'){
                console.log(auctionStatus);
                throw new functions.https.HttpsError('invalid-argument', 'invalid auction status. cannot settle this auction');
            }
    
            const buyerRef = admin.firestore().collection('users').doc(""+currentBidUser);
            const buyerDoc = await transaction.get(buyerRef);
    
            var date = new Date();
            var dateWrapper = moment(date);
            var dateString = dateWrapper.tz('America/New_York').format("YYYY MMM D HH:mm:ss"); 
    
            var buyerHold = buyerDoc.data().hold - currentBid;
            //add bid info
            var newItem = [];
            var wonItems = buyerDoc.data().won_items;
    
            if(wonItems){
                newItem = wonItems;
            }
            newItem.push({
                itemId:data.itemId,
                item_name:auctionDoc.data().item_name,
                buying_date : dateString,
                item_price:currentBid
            });
    
            const buyerRes = await transaction.update(buyerRef, {hold:buyerHold,won_items:newItem},{merge:true});
            if(!buyerRes){
                throw new functions.https.HttpsError('invalid-argument', 'some error occured.');
            }
    
            var sellerBalance = userDoc.data().balance + currentBid;
            const sellerRes = await transaction.update(userRef, {balance:sellerBalance});
            if(!sellerRes){
                throw new functions.https.HttpsError('invalid-argument', 'some error occured.');
            }
    
            var newAuction = {
                auction_status : 'complete',
                auction_end_date : dateString
            }
    
            const auctionRes = await transaction.update(auctionRef, newAuction);
            if(!auctionRes){
                throw new functions.https.HttpsError('invalid-argument', 'some error occured.');
            }

            var ownerMessage={
                notification: {title: 'Item Sold', body: 'Auction item: '+auctionDoc.data().item_name+' sold'},
                data:{itemId:data.itemId,code:"104"},
                token: userDoc.data().device_token,
            };

            var buyerMessage={
                notification: {title: 'Item Bought', body: 'Auction item: '+auctionDoc.data().item_name+' is bought by you'},
                data:{itemId:data.itemId,code:"104"},
                token: buyerDoc.data().device_token,
            };

            if(ownerMessage.token && ownerMessage.token.length>0){
                var ownerMsg = await admin.messaging().send(ownerMessage)
                if(!ownerMsg){
                    throw new functions.https.HttpsError('invalid-argument', 'some error occured.');
                }
            }
            
            if(buyerMessage.token && buyerMessage.token.length>0){
                var buyerMsg = await admin.messaging().send(buyerMessage)
                if(!buyerMsg){
                    throw new functions.https.HttpsError('invalid-argument', 'some error occured.');
                }
            }
            
            return {'result':"auction settled"};
        });
    }
    catch(e){
        throw new functions.https.HttpsError('invalid-argument', e.message);
    }
});

exports.getAuctionItems = functions.https.onCall(async(data,context)=>{
    var uid = isAuthenticated(data,context);

    const auctionRef = admin.firestore().collection('Auctions').where('auction_status','!=','complete');

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
        throw new functions.https.HttpsError('invalid-argument', e.message);
    }

});

exports.getCreatedItems = functions.https.onCall(async(data,context)=>{
    var uid = isAuthenticated(data,context);

    const auctionRef = admin.firestore().collection('Auctions').where('owner_id','==',uid);

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
        throw new functions.https.HttpsError('invalid-argument', e.message);
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
        throw new functions.https.HttpsError('invalid-argument', e.message);
    }

});

exports.getUser = functions.https.onCall(async(data,context)=>{
    var uid = isAuthenticated(data,context);

    const userRef = admin.firestore().collection('users').doc(""+uid);

    try{
        return await admin.firestore().runTransaction(async(transaction)=>{
            const docs = await transaction.get(userRef);
            var user = docs.data();
            return {'result':user};
        });
    }
    catch(e){
        throw new functions.https.HttpsError('invalid-argument', e.message);
    }

});


exports.getItem = functions.https.onCall(async(data,context)=>{

    if(!data.itemId || !(typeof data.itemId === 'string') || data.itemId.length<=0){
        throw new functions.https.HttpsError('invalid-argument', 'items should be present and it should be a string value');
    }

    const auctionRef = admin.firestore().collection('Auctions').doc(data.itemId);

    try{
        return await admin.firestore().runTransaction(async(transaction)=>{
            const docs = await transaction.get(auctionRef);
            var item = docs.data();
            return {'result':item};
        });
    }
    catch(e){
        throw new functions.https.HttpsError('invalid-argument', e.message);
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
        throw new functions.https.HttpsError('invalid-argument', e.message);
    }
});

exports.cancelBid = functions.https.onCall(async(data,context)=>{
    var uid = isAuthenticated(data,context);

    if(!data.itemId || !(typeof data.itemId === 'string') || data.itemId.length<=0){
        throw new functions.https.HttpsError('invalid-argument', 'items should be present and it should be a string value');
    }

    const userRef = admin.firestore().collection('users').doc(""+uid);
    const auctionRef = admin.firestore().collection('Auctions').doc(data.itemId);
    const FieldValue = admin.firestore.FieldValue;

    try{
        return await admin.firestore().runTransaction(async(transaction)=>{
            const userDoc = await transaction.get(userRef);
            const auctionDoc = await transaction.get(auctionRef);

            var currentHighestBidUser = auctionDoc.data().current_highest_bid_user;
            var currentHighestBid = auctionDoc.data().current_highest_bid;
            var bidders = auctionDoc.data().bidders;
            var auctionStatus = auctionDoc.data().auction_status

            if(!currentHighestBidUser || currentHighestBidUser != uid){
                throw new functions.https.HttpsError('invalid-argument', 'You cannot revoke this bid. You are not the highest bidder');
            }
            if(auctionStatus != 'in_progress'){
                throw new functions.https.HttpsError('invalid-argument', 'You cannot revoke a bid on an auction which is not in progress');
            }

            isValidBidderFound = false;
            var newBidder;
            var newBid;

            if(bidders){
                var bidArr = []
                var biddersMap = new Map(Object.entries(bidders));
                
                var cnt =0;

                biddersMap.forEach((value,key)=>{
                    bidArr.push(key);
                });

                while(!isValidBidderFound && biddersMap.size!=1){
                    var maxKey="";
                    var maxVal=0;
                    biddersMap.forEach((value,key)=>{
                        if(value>maxVal && key!=uid){
                            maxKey = key;
                            maxVal = value;
                        }
                    });
                    
                    const bidderRef = admin.firestore().collection('users').doc(""+maxKey);
                    const bidderDoc = await transaction.get(bidderRef);
                    var bidderBalance = bidderDoc.data().balance;
                    if(bidderBalance>=maxVal){
                        isValidBidderFound = true;
                        newBidder = maxKey;
                        newBid = maxVal;
                    }
                    else{
                        biddersMap.delete(maxKey);
                    }
                }
                
            }
            
            if(isValidBidderFound){
                console.log(newBidder+':'+newBid);

                //auction update
                var date = new Date();
                var dateWrapper = moment(date);
                var dateString = dateWrapper.tz('America/New_York').format("YYYY MMM D HH:mm:ss"); 

                var newBidders = {}
                if(bidders){
                    newBidders = bidders;
                }
                delete newBidders[uid];

                var newAuction={
                    auction_update_date:dateString,
                    current_highest_bid:newBid,
                    current_highest_bid_user:newBidder,
                    bidders: newBidders
                }

                //oldBidderUpdate
                var oldBalance = userDoc.data().balance + currentHighestBid;
                var oldHold = userDoc.data().hold - currentHighestBid;
                var oldBidder = {
                    balance : oldBalance,
                    hold : oldHold
                }

                //newBidderUpdate
                const newBidderRef = admin.firestore().collection('users').doc(""+newBidder);
                const newBidderDoc = await transaction.get(newBidderRef);
                var newBalance = newBidderDoc.data().balance - newBid;
                var newHold = newBidderDoc.data().hold + newBid;
                var newUserBidder = {
                    balance:newBalance,
                    hold:newHold
                }

                //message
                var message = {
                    notification: {title: 'Highest Bidder', body: 'You are the highest bidder on auction item: '+auctionDoc.data().item_name},
                    data:{itemId:data.itemId,code:"102"},
                    token: newBidderDoc.data().device_token,
                    android:{
                        notification:{
                          priority:"high"
                        }
                    }
                  };

                //old bidder
                const oldUserRef = await transaction.update(userRef,oldBidder);
            
                if(!oldUserRef){
                    throw new functions.https.HttpsError('invalid-argument', 'some error occured.');
                }

                //new bidder
                const newUserRef = await transaction.update(newBidderRef,newUserBidder);
            
                if(!newUserRef){
                    throw new functions.https.HttpsError('invalid-argument', 'some error occured.');
                }

                //auction
                const auctionRes = await transaction.update(auctionRef,newAuction,{merge:true});
            
                if(!auctionRes){
                    throw new functions.https.HttpsError('invalid-argument', 'some error occured.');
                }

                //send message 
                if(message.token && message.token.length>0){
                    var newMsg = await admin.messaging().send(message)
                    if(!newMsg){
                        throw new functions.https.HttpsError('invalid-argument', 'some error occured.');
                    }
                }
                
                return {'result':"bid cancled successfully"}

            }
            else{
                console.log("no bidder");
                //auction update
                var date = new Date();
                var dateWrapper = moment(date);
                var dateString = dateWrapper.tz('America/New_York').format("YYYY MMM D HH:mm:ss"); 

                var newBidders = {}
                if(bidders){
                    newBidders = bidders;
                }
                delete newBidders[uid];

                var newAuction={
                    auction_update_date:dateString,
                    current_highest_bid:FieldValue.delete(),
                    current_highest_bid_user:FieldValue.delete(),
                    bidders: newBidders
                }

                //oldBidderUpdate
                var oldBalance = userDoc.data().balance + currentHighestBid;
                var oldHold = userDoc.data().hold - currentHighestBid;
                var oldBidder = {
                    balance : oldBalance,
                    hold : oldHold
                }

                //old bidder
                const oldUserRef = await transaction.update(userRef,oldBidder);
            
                if(!oldUserRef){
                    throw new functions.https.HttpsError('invalid-argument', 'some error occured.');
                }

                //auction
                const auctionRes = await transaction.update(auctionRef,newAuction,{merge:true});
                            
                if(!auctionRes){
                    throw new functions.https.HttpsError('invalid-argument', 'some error occured.');
                }
                    
                return {'result':"bid cancled successfully"}

            }

        });
    }
    catch(e){
        throw new functions.https.HttpsError('invalid-argument', e.message);
    }

});

exports.cancelItem = functions.https.onCall(async(data,context)=>{
    var uid = isAuthenticated(data,context);

    if(!data.itemId || !(typeof data.itemId === 'string') || data.itemId.length<=0){
        throw new functions.https.HttpsError('invalid-argument', 'items should be present and it should be a string value');
    }

    const userRef = admin.firestore().collection('users').doc(""+uid);
    const auctionRef = admin.firestore().collection('Auctions').doc(data.itemId);

    try{
        return await admin.firestore().runTransaction(async(transaction)=>{
            const auctionDoc = await transaction.get(auctionRef);
            const userDoc = await transaction.get(userRef);

            var ownerId = auctionDoc.data().owner_id;
            var auctionStatus = auctionDoc.data().auction_status;

            if(ownerId != uid){
                throw new functions.https.HttpsError('invalid-argument', 'only auction owner can delete auction');
            }

            if(auctionStatus == 'complete'){
                throw new functions.https.HttpsError('invalid-argument', 'cannot remove a completed auction');
            }

            var currentHighestBidUser = auctionDoc.data().current_highest_bid_user;
            var currentHighestBid = auctionDoc.data().current_highest_bid;
            var message;

            if(currentHighestBidUser){
                const bidderRef = admin.firestore().collection('users').doc(""+currentHighestBidUser)
                const bidderDoc = await transaction.get(bidderRef);
                var newBalance = bidderDoc.data().balance + currentHighestBid;
                var newHold = bidderDoc.data().hold - currentHighestBid;
                var newUser = {
                    balance :  newBalance,
                    hold : newHold
                }

                message = {
                    notification: {title: 'Auction Deleted', body: 'Auction item: '+auctionDoc.data().item_name+ ' has been deleted'},
                    data:{itemId:data.itemId,code:"105"},
                    token: bidderDoc.data().device_token,
                  };
                
                const newUserRef = await transaction.update(bidderRef,newUser);
            
                if(!newUserRef){
                    throw new functions.https.HttpsError('invalid-argument', 'some error occured.');
                }
            }

            const aucRef = await transaction.delete(auctionRef);
            if(!aucRef){
                throw new functions.https.HttpsError('invalid-argument', 'some error occured.');
            }

            if(message && message.token && message.token.length>0){
                var msg = await admin.messaging().send(message);
                if(!msg){
                    throw new functions.https.HttpsError('invalid-argument', 'some error occured.');
                }
            }

            return {'result':"auction successfully deleted"};
            
        });
    }
    catch(e){
        throw new functions.https.HttpsError('invalid-argument', e.message);
    }


});

//remove later
exports.sendNOtificationTrial = functions.https.onRequest(async(req,res)=>{
    // This registration token comes from the client FCM SDKs.
    var registrationToken = req.query.token;
    
    var message = {
      notification: {title: 'testing', body: '1. foreground 2. background'},
      data:{itemId:"9Pl2kOK5cev10rUeuXAB",code:"101"},
      token: registrationToken,
      android:{
          notification:{
            channel_id: "Auction_App_FMC_Channel_ID",
            priority:"high"
          }
      }
    };
    
    // Send a message to the device corresponding to the provided
    // registration token.
    admin.messaging().send(message)
      .then((response) => {
        // Response is a message ID string.
        console.log('Successfully sent message:', response);
      })
      .catch((error) => {
        console.log('Error sending message:', error);
      });
    
      res.status(200).send("result done");
    
    });


function isAuthenticated(data,context){
    if(isDebug){
        return data.uid;
    }

    if(!context.auth){
        throw new functions.https.HttpsError('invalid-argument', 'User unauthorized');
    }
    console.log("context: "+context.auth);
    console.log("context uid: "+context.auth.uid);
    return context.auth.uid;
}