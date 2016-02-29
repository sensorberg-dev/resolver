#Create a Service Hub and write Data in Blob Store via Stream Analytics


##Create a service hub

- In the azure console navigate to service bus, click create to create a service bus namespace.

- Click on the namespace created. Select event hubs. Click on new. Click quick create.

- Create shared access policies to write to the event hub. Select the newly created hub, click configure.

- Under shared access policies enter new rule "SendRule", select permission "Send". Click save.

- The value under shared access key generator is needed in Java to access the event hub. 

[Get started with Event Hubs](https://azure.microsoft.com/en-us/documentation/articles/event-hubs-java-ephcs-getstarted/)



##Storage

- Create a new storage account in the azure console under storage. 
- Add a new container for a blob storage for the backend
- Add a new container for a blob storage for the resolver


[Verwenden des Blob-Speichers mit Java](https://azure.microsoft.com/de-de/documentation/articles/storage-java-how-to-use-blob-storage/)


##Stream Analytics

To write data from the event hub to the blob container, we need stream analytics. Select stream analytics and create a new job. 

- create a new input, source is event hub, source type is data stream
- create a query 



```
SELECT * INTO {ContainerNameResoler} FROM {inputName} WHERE messageSource LIKE 'RESOLVER'
SELECT * INTO {ContainerNameBackend} FROM {inputName} WHERE messageSource LIKE 'BACKEND'
```

- create 2 outputs, sink is here the blob storage, created before. One output for each container created under Storage.
- start the job

[Erste Schritte mit Azure Stream Analytics](https://azure.microsoft.com/de-de/documentation/articles/stream-analytics-get-started/)






