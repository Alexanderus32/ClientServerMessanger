## Client-server messanger

Сервер у нас работает на Node.js и запущен на Heroku - [здесь](https://mysterious-lake-76125.herokuapp.com/)

Клиент написан на андроде.

Для взаимодействия всего этого дела использовалась библиотека  `socket.io`

# Код сервера написанного на Node.js
```js
const express = require('express')
const path = require('path')
var app = express();
var http = require('http').Server(app);
var io = require('socket.io')(http);

app.set('port', (process.env.PORT || 5000));

app.use(express.static(__dirname + '/public'));

app.set('views', __dirname + '/views');
app.set('view engine', 'ejs');

console.log("outside io");

var connectedUsers = {};

io.on('connection', function(socket){

  console.log('User Conncetion');

  socket.on('connect user', function(user){
    console.log("Connected user " + user['username']);    
    socket.username=user['username'];
    connectedUsers[user['username']] = socket;
    io.emit('connect user', user);
  });

  socket.on('disconnect user', function(user){
    console.log("Disconnect user ");
    delete connectedUsers[user['username']];
    io.emit('disconnect user', user);
  });

  socket.on('private_chat',function(data){
    const to = data.to,
        fromuser=data.username,
          uniqueId=data.uniqueId;
            message = data.message;
    console.log(to);
    if(connectedUsers.hasOwnProperty(to)){
        connectedUsers[to].emit('private_chat',{
            username : fromuser,
            uniqueId:uniqueId,
            message : message
        });
    }

}); 

  socket.on('on typing', function(typing){
    console.log("Typing.... ");
    io.emit('on typing', typing);
  });

  socket.on('chat message', function(msg){
    console.log("Message " + msg['message']);
    io.emit('chat message', msg);
  });
  
  socket.on('create room', function(room) { 
	socket.join(room); 
	io.emit('new wroom', room);	
}); 

 socket.on('room message', function(msg){
    console.log("Message " + msg['message']);
    socket.to(msg['room']).emit( 'room message', msg); 
  });

  
});


http.listen(app.get('port'), function() {
  console.log('Node app is running on port', app.get('port'));
});

```

+  Приветственное окно, для ввода имени.
 
![alt text](https://i.ibb.co/NjcNnZz/35.png)

+  Групповая беседа. С возможностью выбрать собеседника для приватного чата

![alt text](https://i.ibb.co/fv1sykw/4.png)
![alt text](https://i.ibb.co/JCHqDCD/5.png)

+ Личные сообщения пользователей

![alt text](https://i.ibb.co/64BZdSL/6.png)
