# Dialog-App
Dialog App with Akka cluster

1. Start Server1
2. Start Server2
3. Use following command in terminal to send requests:

*Port 8080 shoul be changed to needed port of server*

**Send message**

curl -X POST http://localhost:8080/api/sendMessage -H "Content-Type: application/json" -d '{"userId": "userId", "message": "message"}'


**Reply on message**

curl -X POST http://localhost:8080/api/reply -H "Content-Type: application/json" -d '{"userId": "userId", "name": "name", "reply": "message"}'


**Get dialog**

curl http://localhost:8080/api/getDialog/userId
