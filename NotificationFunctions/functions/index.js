'use strict'


const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendNotification = functions.database
				.ref('Notifications/{userId}/{notificationId}').onWrite(event => {

	const userId = event.params.userId;
	const notificationId = event.params.notificationId;

	console.log("We have a notification sent to : ", userId);

	if (!event.data.val()) {
		return console.log("A Notification has been deleted from the database : "
				, notificationId);
	}

	const fromUser = admin.database().ref(`/Notifications/${userId}/${notificationId}`).once('value');
	return fromUser.then(fromUserResult => {
		const fromUserId = fromUserResult.val().from;
		console.log("You have a notification from : ", fromUserId);

		const userQuery = admin.database().ref(`/Users/${fromUserId}/name`).once('value');
		const deviceToken = admin.database().ref(`/Users/${userId}/deviceToken`).once('value');

        return Promise.all([userQuery, deviceToken]).then(result => {

        	const userName = result[0].val();
            const tokenId = result[1].val();

            const payload = {
                notification : {
                    title : "Friend Request",
                    body : `${userName} has sent you a request`,
                    icon : "default",
                    click_action : "com.chat.bridge.TARGET_FRIEND RERQUEST"
                },
                data : {
                    fromUserId : fromUserId
                }
            };

            return admin.messaging().sendToDevice(tokenId, payload)
                .then(response => {
                    console.log("This was the notification feature")
            });
        });

    });
});