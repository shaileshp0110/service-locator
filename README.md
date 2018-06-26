# Service Locator
Service locator is a microservice that helps services to know about other services they are interested in.

### Create Registration
Services use this endpoint to register with the Service Locator.

request:
```
POST /registration
```
Payload:
```
{
   "serviceName":"hello-world",
   "serviceUrl":"http://hello-world.example.com",
   "metadata": {
      "key1":"value1",
      "key2":"value2"
   }
}
```
response:
```
204 NO CONTENT
```

Upon registration all subscribers are notified of the service.

### Fetch Service Details
Consumers use this endpoint to fetch details of another service.

request:
```
GET /service/:name
```
response:
```
{
   "serviceName":"hello-world",
   "serviceUrl":"http://hello-world.example.com",
   "metadata": {
      "key1":"value1",
      "key2":"value2"
   }
}
```
### Subscription
Consumers use this endpoint to subscribe to service start-up events.

request:
```
POST /subscription
```
Payload:
```
{
   "serviceName":"api-publisher",
   "serviceUrl":"http://api-publisher.example.com"
}
```
response:
```
204 NO CONTENT
```

---

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
