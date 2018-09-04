'use strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);


//----Request notifcation

exports.sendNotification = functions.database.ref('/notifications/{user_id}/{notification_id}').onWrite((change, context) =>{
  const user_id = context.params.user_id;
  const notification_id  = context.params.notification_id;
  console.log('We have a friend request to send to : ', user_id);

  if(!change.after.exists()){
    return console.log('A notification has been deleted from the database : ', notification_id);
  }

  const fromUser = admin.database().ref(`/notifications/${user_id}/${notification_id}`).once('value');
  return fromUser.then(fromUserResult => {
    const from_user_id = fromUserResult.val().from;

    console.log('You have new notification from : ', from_user_id);

    const userQuery = admin.database().ref(`/Users/${from_user_id}/name`).once('value');
    const deviceToken = admin.database().ref(`/Users/${user_id}/device_token`).once('value');
    return Promise.all([userQuery, deviceToken]).then(result =>{

      const userName = result[0].val();
      const token_id = result[1].val();

      const payload = {
        notification: {
          title : "Friend Request",
          body: `${userName} has sent you request.`,
          icon: "default",
          sound: "default",
          click_action: "com.mahija.teenchapit.chapitchat_TARGET_NOTIFICATION"
        },
        data: {
          from_user_id: from_user_id
        }
      };

      return admin.messaging().sendToDevice(token_id, payload).then(response =>{
        return console.log('This was the notification feature');
      });

    });

  });

});

// ----Message notification

exports.sendChatNotification = functions.database.ref('/Message_notification/{receiver_id}/{sender_id}').onWrite((change, context) =>{
  const receiver_id = context.params.receiver_id;
  const sender_id  = context.params.sender_id;
  console.log("ASIF: ", change.after);
  // const message = change.after.DataSnapshot._data;
  const message = "Hi bros";
  console.log('We have a message to send to : ', receiver_id);

  if(!change.after.exists()){
    return console.log('A notification has been deleted from the database : ', receiver_id);
  }

console.log("KHAN: ", const ghuso = admin.database().ref(`/Message_notification/${receiver_id}`));

  const ghuso = admin.database().ref(`/Message_notification/${receiver_id}`).once('value');

  return ghuso.then(ghusoResult => {

    console.log('Message: ', message);

    const deviceToken = admin.database().ref(`/Users/${receiver_id}/device_token`).once('value');
    const userQuery = admin.database().ref(`/Users/${sender_id}/name`).once('value');

    return Promise.all([userQuery, deviceToken]).then(result =>{

      const userName = result[0].val();
      const token_id = result[1].val();

      const payload = {
        notification:{
          title : `${userName}'s message`,
          body : `${message}`,
          click_action: "com.mahija.teenchapit.chapitchat_TARGET_CHAT_NOTIFICATION",
          sound: "default",
          icon: "default"
        },
        data: {
          from_user_id: sender_id
        }
      };

      return admin.messaging().sendToDevice(token_id, payload).then(response =>{
            return console.log('This was the notification feature');
      });

    });

  });
});
