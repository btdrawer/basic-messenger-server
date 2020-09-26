# basic-messenger-server

A basic messenger API built using Akka HTTP and PostgreSQL.

## Authentication

The API uses basic HTTP authentication that requires a user's username-password combination.

## Response format

Endpoints that either operate on, or retrieve, a single item have the following response format, if successful:

````JSON
{
  "success": true,
  "result": {},
  "message": "Item created successfully"
}
````

The `result` object is usually null for `PUT` and `DELETE` endpoint; the `message` is usually null for `GET` endpoints.

Endpoints that retrieve a list of items will simply return the list at the top level.

If an endpoint fails, this will be the response format (in this case, a user was not found):

````JSON
{
  "success": false,
  "result": null,
  "message": "User not found."
}
````

## Endpoints

### Servers

Resource structure:

````JSON
{
  "id": 1,
  "name": "Example Server",
  "address": "exampleserver",
  "users": [
    {
      "user": {
        "id": 1,
        "username": "ben",
        "status": "ONLINE"
      },
      "role": "ADMIN"
    }
  ],
  "messages": [
    {
      "id": 1,
      "content": "Hello",
      "user": {
        "id": 1,
        "username": "ben",
        "status": "ONLINE"
      },
      "createdAt": 16011364370000
    }
  ]
}
````

#### Roles

Every user on a server has one of these roles:

* `ADMIN` - Can send messages, add and remove users, update the server's name, and delete the server
* `MODERATOR` - Can send messages, add and remove users
* `MEMBER` - Can send messages

#### `POST /servers`

##### Request body

| Parameter | Type   | Required? |
|-----------|--------|-----------|
| name      | String | Yes       |
| address   | String | Yes       |

Sample request:

````JSON
{
  "name": "Example Server",
  "address": "exampleserver"
}
````

##### Response (success)

Status: `200 OK`

Sample response:

````JSON
{
  "success": true,
  "result": {
    "id": 1,
    "name": "Example Server",
    "address": "exampleserver",
    "users": [
      {
        "user": {
          "id": 1,
          "username": "ben",
          "status": "ONLINE"
        },
        "role": "ADMIN"
      }
    ],
    "messages": []
  }
}
````

#### `GET /servers/search/<name>`

Searches for servers by name.

##### Response (success)

Status: `200 OK`

Sample response:

````JSON
[
  {
    "id": 1,
    "name": "Example Server",
    "address": "exampleserver"
  },
  {
    "id": 2,
    "name": "Another Server",
    "address": "anotherserver"
  }
]
````

#### `GET /servers/id/<id>`

Retrieve a single server by its ID.

##### Response (success)

Status: `200 OK`

Sample response:

````JSON
{
  "success": true,
  "result": {
    "id": 1,
    "name": "Example Server",
    "address": "exampleserver",
    "users": [
      {
        "user": {
          "id": 1,
          "username": "ben",
          "status": "ONLINE" 
        },
        "role": "ADMIN" 
      }
    ],
    "messages": [
      {
        "id": 1,
        "content": "Hello",
        "user": {
          "id": 1,
          "username": "ben",
          "status": "ONLINE"
        },
        "createdAt": 16011364370000
      }
    ]
  },
  "message": null
}
````

#### `GET /servers/address/<address>`

Retrieve a single server by its address.

##### Response (success)

Status: `200 OK`

Sample response:

````JSON
{
  "success": true,
  "result": {
    "id": 1,
    "name": "Example Server",
    "address": "exampleserver",
    "users": [
      {
        "user": {
          "id": 1,
          "username": "ben",
          "status": "ONLINE"
        },
        "role": "ADMIN"
      }
    ],
    "messages": [
      {
        "id": 1,
        "content": "Hello",
        "user": {
          "id": 1,
          "username": "ben",
          "status": "ONLINE"
        },
        "createdAt": 16011364370000
      }
    ]
  },
  "message": null
}
````

#### `PUT /servers/<id>`

Update a server.

##### Permissions level required

* `ADMIN`

##### Request body

| Parameter | Type   | Required? |
|-----------|--------|-----------|
| name      | String | No        |

Sample request:

````JSON
{
  "name": "Updated Server"
}
````

#### `PUT /servers/<server_id>/users/<user_id>`

Add a user to the server.

##### Permissions level required

* `ADMIN`
* `MODERATOR`

##### Response (success)

Status: `200 OK`

Sample response:

````JSON
{
  "success": true,
  "result": null,
  "message": "User added to server."
}
````

#### `PUT /servers/<server_id>/users/<user_id>/roles/<role>`

Change the role of a user on the server.

##### Permissions level required

* `ADMIN`

##### Response (success)

Status: `200 OK`

Sample response:

````JSON
{
  "success": true,
  "result": null,
  "message": "User role updated."
}
````

#### `DELETE /servers/<id>`

Delete a server.

##### Permissions level required

* `ADMIN`

##### Response (success)

Status: `200 OK`

Sample response:

````JSON
{
  "success": true,
  "result": null,
  "message": "Server deleted successfully."
}
````

#### `DELETE /servers/<server_id>/users/<user_id>`

Remove a user from a server.


##### Permissions level required

* `ADMIN`
* `MODERATOR`

##### Response (success)

Status: `200 OK`

Sample response:

````JSON
{
  "success": true,
  "result": null,
  "message": "User removed from server."
}
````
