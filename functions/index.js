// // Create and Deploy Your First Cloud Functions

// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp();


// Listens for new messages added to /products to send a push notification to all users
exports.sendNotifs = functions.database.ref('/products')
    .onWrite(event=>{

        // The topic name can be optionally prefixed with "/topics/".
        var topic = 'new_products';

        var message = {
          data: {
            title: 'New Product Added to catalog',
            body: 'Tap here to check it out!'
          },
          topic: topic
        };

        // Send a message to devices subscribed to the provided topic.
        return admin.messaging().send(message)
          .then((response) => {
            // Response is a message ID string.
            console.log('Successfully sent message:', response);
          })
          .catch((error) => {
            console.log('Error sending message:', error);
          });

//        console.log('sending notifications to all users');
//        // Get the Messaging service for the default app
//        var defaultMessaging = admin.messaging();
//        const messages = [];
//        messages.push({
//          notification: {title: 'New Product Added to catalog', body: 'Tap here to check it out!'},
//          topic: "new_products"
//        });
//
//        return defaultMessaging.send(messages).then((response) => {
//                                                             console.log(response.successCount + ' messages were sent successfully');
//                                                           });
    })
