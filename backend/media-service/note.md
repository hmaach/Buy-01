## HTTP APIs

### Upload Images
**Endpoint:** `POST /media`  
**Consumes:** `multipart/form-data` with one or more files under the `files` parameter.  
**Returns:** `201 Created` with a JSON array of `MediaResponse` objects describing each uploaded file.

```
POST /media
Content-Type: multipart/form-data; boundary=---XYZ

-----XYZ
Content-Disposition: form-data; name="files"; filename="photo.jpg"
Content-Type: image/jpeg

<binary data>
-----XYZ--
```

Example response:
```json
[
  {
    "id": "69a071b99cb3d3c6f5dfd64e",
    "filename": "photo.jpg",
    "status": "PENDING",
    "productId": null
  }
]
```

### Get Image
**Endpoint:** `GET /media/{id}`  
**Returns:** the binary resource as the original file content.  
**Headers:** sets `Content-Type` to the detected media type and a `Content-Disposition` inline header.

```
GET /media/69a071b99cb3d3c6f5dfd64e
```

---

## Kafka Listener and Local Testing
The `MediaStatusListener` component listens to the `images-linked` topic for events signaling that one or more media files have been associated with a product.  When a message arrives, it:

1. Logs the event (`productId` and count).
2. Iterates over each media ID in the payload.
3. Fetches the media from the repository; if status is `PENDING`, updates it to `LINKED` and sets the `productId`.
4. Persists the changes.  Exceptions are logged and could be routed to a DLQ.

### Event format
```json
{
  "productId": "<product-id>",
  "mediaIds": ["<media-id-1>", "<media-id-2>"]
}
```

### Testing locally
Use the Kafka console producer (assuming a broker named `kafka-broker` in Docker Compose):

```
echo '{"productId":"test-prod-001","mediaIds":["69a071b99cb3d3c6f5dfd64e"]}' | \
  docker exec -i kafka-broker /opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic images-linked
```

The listener will pick up the message and perform the status update.  Check the database or application logs for confirmation.
